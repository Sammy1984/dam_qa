package ru.spice.at.common.base_test;

import io.qameta.allure.Allure;
import lombok.extern.log4j.Log4j2;
import org.testng.ITestListener;
import org.testng.ITestResult;
import ru.spice.at.common.Settings;

import com.google.gson.Gson;
import io.restassured.RestAssured;
import io.restassured.config.RedirectConfig;
import org.aeonbits.owner.ConfigFactory;
import ru.testit.annotations.WorkItemId;
import ru.testit.annotations.WorkItemIds;

import java.io.*;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static io.restassured.config.DecoderConfig.decoderConfig;
import static java.lang.String.format;
import static ru.spice.at.api.utils.ApiUtils.getPath;

/**
 * Базовый класс для тестовых классов
 * @author Aleksandr Osokin
 */
@Log4j2
abstract class BaseTest<T> {
    private static final String DOWNLOAD_PATH = "/target/test-classes/download_%s/";

    private T data;
    protected final Settings settings;
    protected final String downloadPath;

    static {
        RestAssured.useRelaxedHTTPSValidation();
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
        RestAssured.config = RestAssured.config().redirect(RedirectConfig.redirectConfig()
            .followRedirects(false)).decoderConfig(decoderConfig().noContentDecoders());
        try {
            new Properties().load(new FileInputStream(getPath("allure.properties").toString()));
        } catch (IOException e) {
            log.error("Непредвиденная ошибка при обработке allure.properties {}", e.getMessage());
        }
    }

    protected <E> BaseTest(E service, String settingsPath) {
        settings = ConfigFactory.create(Settings.class, System.getenv(), System.getProperties());
        downloadPath = System.getProperty("user.dir") + format(DOWNLOAD_PATH,
                service.toString() + this.getClass().toString().replaceAll(" ", "").replaceAll("\\.", ""));
        Path standJsonPath = Paths.get(".").toAbsolutePath()
                .normalize().resolve(format(settingsPath, service.toString()))
                .toAbsolutePath();
        try (Reader reader = new FileReader(standJsonPath.toString())) {
            Type superclass = getClass().getGenericSuperclass();
            Type t = ((ParameterizedType)superclass).getActualTypeArguments()[0];
            Gson gson = new Gson();
            this.data = gson.fromJson(reader, t);
        } catch (ClassCastException e) {
            log.info("Параметры теста не проинициализированы");
        } catch (FileNotFoundException e) {
            log.warn("Файл {} не найден", standJsonPath);
        } catch (IOException e) {
            log.error("Ошибка разбора json {}", standJsonPath, e);
        }
    }

    protected T getData() {
        return data;
    }

    /**
     * Создаем папку в target для импорта/экспорта файлов
     */
    protected void createFileDirection() {
        try {
            Files.createDirectory(Paths.get(downloadPath));
            log.info("Создана папка для скачивания: {}", downloadPath);
        } catch (IOException e) {
            log.error("Ошибка при создании директории {}", e.getMessage());
        }
    }

    /**
     * Удаляем папку в target для импорта/экспорта файлов
     */
    protected void deleteFileDirection() {
        try {
            Files.walk(Paths.get(downloadPath))
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
            log.info("Удалена папка для скачивания: {}", downloadPath);
        } catch (IOException e) {
            log.error("Ошибка при удалении директории {}", e.getMessage());
        }
    }
}