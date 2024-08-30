package ru.spice.at.common.utils;

import io.qameta.allure.Step;
import lombok.extern.log4j.Log4j2;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.FileHeader;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.awaitility.Awaitility;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Утилитный класс для работы с файлами.
 */
@Log4j2
public final class FileHelper {

    private FileHelper() {
        throw new IllegalAccessError("Это утилитный класс. Создание экземпляра не требуется.");
    }

    /**
     * Проверяем скаченный файл в папке
     *
     * @param downloadPath - путь до папки с файлом
     * @param fileExtension - расширение файла, например, '.txt'
     * @param bytes - размер файла в байтах
     * @param delete - {@code true} удалить после проверки скачивания, {@code false} не удалять
     */
    @Step("Проверяем скаченный файл в папке '{0}' с расширением '{1}' и c размером больше либо равно {2} байт")
    public static void checkDownloadFile(String downloadPath, String fileExtension, long bytes, boolean delete) {
        File direction = new File(downloadPath);

        FileFilter fileFilter = file -> file.isFile() && file.getName().endsWith(fileExtension);
        Awaitility.given().pollDelay(300, TimeUnit.MILLISECONDS)
                .pollInterval(1, TimeUnit.SECONDS)
                .and().timeout(30, TimeUnit.SECONDS)
                .await("Файл не скачен")
                .until(() -> Objects.requireNonNull(direction.listFiles(fileFilter)).length != 0);
        Optional<File> file = Arrays.stream(Objects.requireNonNull(direction.listFiles(fileFilter))).findFirst();


        if (file.isPresent()) {
            assertThat("Файл меньше заданного размера (пуст)", file.get().length() >= bytes);
            if (delete) {
                try {
                    Files.delete(file.get().toPath());
                } catch (IOException e) {
                    log.error("Ошибка при удалении файла {}", e.getMessage());
                }
            }
        }
    }

    /**
     * Создаем файл по пути из байтовой последовательности
     *
     * @param bytes - файл в виде последовательности байт
     * @param path - путь для создания файла
     * @param fileExtension - расширение файла, например, 'zip'
     *
     * @return - имя файла в виде 'test_1234567' без расширения
     */
    public static String createFileFromBytesArray(byte[] bytes, String path, String fileExtension) {
        return createFileFromBytesArray(bytes, null, path, fileExtension);
    }

    /**
     * Создаем файл по пути из байтовой последовательности
     *
     * @param bytes - файл в виде последовательности байт
     * @param name - название файла
     * @param path - путь для создания файла
     * @param fileExtension - расширение файла, например, 'zip'
     *
     * @return - имя файла
     */
    @Step("Создаем файл по пути '{2}' с расширением '{3}'")
    public static String createFileFromBytesArray(byte[] bytes, String name, String path, String fileExtension) {
        log.info("Создаем файл по пути '{}' с расширением '{}'", path, fileExtension);
        String fileName = name == null ?
                String.format("test_%s", LocalDateTime.now().format(DateTimeFormatter.ofPattern("mmssSSS"))) :
                name;
        try (FileOutputStream fos = new FileOutputStream(String.format("%s%s.%s", path, fileName, fileExtension))) {
            fos.write(bytes);
        } catch (FileNotFoundException e) {
            log.error("Ошибка - не найдена директория");
            throw new RuntimeException("Ошибка - не найдена директория", e);
        } catch (IOException e) {
            log.error("Ошибка вывода в файл");
            throw new RuntimeException("Ошибка вывода в файл", e);
        }
        return fileName;
    }

    /**
     * Распаковываем zip архив и возвращаем путь до папки с распакованными файлами
     *
     * @param zipName - название архива
     * @param zipPath - путь до архива
     *
     * @return - путь до папки с распакованными файлами. Название папки такое же как название архива
     */
    @Step("Распаковываем zip архив '{0}'")
    public static String extractZip(String zipName, String zipPath) {
        log.info("Распаковываем zip архив '{}' по пути '{}'", zipName, zipPath);
        String extractZipPath = zipPath.concat(zipName);
        try {
            new ZipFile(extractZipPath.concat(".zip")).extractAll(extractZipPath);
        } catch (ZipException e) {
            log.error("Ошибка разархивирования");
            throw new RuntimeException("Ошибка разархивирования", e);
        }
        return extractZipPath.concat("/");
    }

    /**
     * Распаковываем zip архив для файлов с рандомным названием и одним расширением, возвращаем путь до папки с распакованными файлами
     *
     * @param zipName - название архива
     * @param zipPath - путь до архива
     * @param filesExtension - расширение файлов в виде 'jpeg', 'xlsx' и тд
     *
     * @return - путь до папки с распакованными файлами. Название папки такое же как название архива
     */
    @Step("Распаковываем zip архив '{0}' для файлов с рандомным названием и расширением {2}")
    public static String extractZip(String zipName, String zipPath, String filesExtension) {
        log.info("Распаковываем zip архив '{}' по пути '{}' для файлов с рандомным названием и расширением '{}'",
                zipName, zipPath, filesExtension);
        String extractZipPath = zipPath.concat(zipName);
        try {
            ZipFile zipFile = new ZipFile(extractZipPath.concat(".zip"));
            for (FileHeader header : zipFile.getFileHeaders()) {
                String dateTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("ddHHmmssSSS"));
                zipFile.extractFile(header, extractZipPath, String.format("unzip_%s.%s", dateTime, filesExtension));
            }
        } catch (ZipException e) {
            log.error("Ошибка разархивирования");
            throw new RuntimeException("Ошибка разархивирования", e);
        }
        return extractZipPath.concat("/");
    }

    /**
     * Конвертируем файл в байтовый массив
     *
     * @param namesWithExtension - название файла с расширением
     * @param path - путь до файла
     *
     * @return - файл в виде массива байтов
     */
    @Step("Конвертируем файл '{0}' в байтовый массив")
    public static byte[] covertFileToBytes(String namesWithExtension, String path) {
            try {
                return Files.readAllBytes(Paths.get(path.concat(namesWithExtension)));
            } catch (IOException e) {
                log.error("Ошибка чтения");
                throw new RuntimeException("Ошибка чтения", e);
            }
    }

    /**
     * Получаем таблицу из xlsx файла в виде списка из списков строковых значений
     *
     * @param namesWithExtension - название файла с расширением
     * @param path - путь до файла
     *
     * @return - таблица в виде списка из списков
     */
    @Step("Получаем таблицу из xlsx файла в виде списка из списков строковых значений")
    public static List<List<String>> getTableFromXlsxFile(String namesWithExtension, String path) {
        return getTableFromXlsxFile(Paths.get(path.concat(namesWithExtension)).toFile());
    }

    /**
     * Получаем таблицу из xlsx файла в виде списка из списков строковых значений
     *
     * @param xlsxFile - xlsx файл
     *
     * @return - таблица в виде списка из списков
     */
    @Step("Получаем таблицу из xlsx файла в виде списка из списков строковых значений")
    public static List<List<String>> getTableFromXlsxFile(File xlsxFile) {
        FileInputStream fis;
        try {
            fis = new FileInputStream(xlsxFile);
        } catch (FileNotFoundException e) {
            log.error("Ошибка вывода в поток");
            throw new RuntimeException("Ошибка вывода в поток", e);
        }
        return getTableFromXlsxFile(fis);
    }

    /**
     * Получаем таблицу из xlsx файла в виде списка из списков строковых значений
     *
     * @param xlsxBytes - xlsx файл в байтовом представлении
     *
     * @return - таблица в виде списка из списков
     */
    @Step("Получаем таблицу из xlsx файла в виде списка из списков строковых значений")
    public static List<List<String>> getTableFromXlsxFile(byte[] xlsxBytes) {
        return getTableFromXlsxFile(new ByteArrayInputStream(xlsxBytes));
    }

    private static List<List<String>> getTableFromXlsxFile(InputStream fis) {
        XSSFWorkbook wb;
        try {
            wb = new XSSFWorkbook(fis);
        } catch (IOException e) {
            log.error("Ошибка чтения");
            throw new RuntimeException("Ошибка чтения", e);
        }

        XSSFSheet sheet = wb.getSheetAt(0);
        List<List<String>> table = new ArrayList<>();
        for (Row row : sheet) {
            List<String> rowList = new ArrayList<>();
            for (Cell cell : row) {
                rowList.add(cell.getStringCellValue());
            }
            table.add(rowList);
        }
        return table;
    }
}
