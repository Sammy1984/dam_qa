package ru.spice.at.api.export_service.test;

import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import ru.testit.annotations.WorkItemIds;
import org.apache.commons.lang3.RandomStringUtils;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.spice.at.api.dto.response.export_service.ExportsDataItem;
import ru.spice.at.api.dto.response.metadata.InvalidParamsItem;
import ru.spice.at.api.export_service.ExportServiceSettings;
import ru.spice.at.api.export_service.ExportServiceStepDef;
import ru.spice.at.api.import_service.ImportServiceStepDef;
import ru.spice.at.api.metadata_service.MetadataStepDef;
import ru.spice.at.common.base_test.BaseApiTest;
import ru.spice.at.common.dto.dam.ImageData;
import ru.spice.at.common.emuns.ApiServices;
import ru.spice.at.common.emuns.dam.ImageFormat;
import ru.spice.at.common.utils.Assert;

import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE;
import static ru.spice.at.common.constants.TestConstants.*;
import static ru.spice.at.common.emuns.dam.ExportsStatus.DONE;
import static ru.spice.at.common.utils.ImageHelper.getRandomByteImage;

@Feature("Export Service")
@Story("POST multi exports media")
public class ExportServiceMultiExportsImageTests extends BaseApiTest<ExportServiceSettings> {
    private final ExportServiceStepDef exportServiceStepDef;

    private ImportServiceStepDef importServiceStepDef;
    private List<String> idList;
    private Map<String, byte[]> images;

    protected ExportServiceMultiExportsImageTests() {
        super(ApiServices.EXPORT_SERVICE);
        exportServiceStepDef = new ExportServiceStepDef();
    }

    @BeforeClass(description = "Добавляем файлы в систему", alwaysRun = true)
    public void beforeClass() {
        importServiceStepDef = new ImportServiceStepDef(exportServiceStepDef.getAuthToken());
        createFileDirection();
        images = getData().images().stream().collect(Collectors.toMap(ImageData::getFilename,
                image -> getRandomByteImage(image.getWidth(), image.getHeight(), image.getFormat().getFormatName())));
        idList = importServiceStepDef.importImages(images);

        ImageData image = new ImageData(ImageFormat.JPEG);
        importServiceStepDef.
                importImages(image.getFilename(), getRandomByteImage(image.getWidth(), image.getHeight(), image.getFormat().getFormatName()));
    }

    @AfterClass(alwaysRun = true)
    public void afterClassDelete() {
        deleteFileDirection();
    }

    //todo выключено для использования параллельного сьюта
    //@AfterClass(alwaysRun = true)
    public void afterClass() {
        importServiceStepDef.deleteMedia();
        new MetadataStepDef(exportServiceStepDef.getAuthToken()).deleteMetadata();
    }

    @Test(description = "Успешный экспорт нескольких файлов", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"225866"})
    public void successExportImagesTest() {
        exportServiceStepDef.exportImage(idList);
        String storageUrl = exportServiceStepDef.successWaitExportsWithParameters("Архив", DONE, idList.size());
        byte[] bytesZip = exportServiceStepDef.getExportStorageArchiveImage(storageUrl);
        Assert.notNullOrEmptyParameter(bytesZip.length, "байты");
        exportServiceStepDef.checkFiles(bytesZip, images, downloadPath);
    }

    @Test(description = "Успешный экспорт нескольких файлов - архив с заданным названием", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"232838"})
    public void successExportImagesArchiveWithNameTest() {
        String archiveName = RandomStringUtils.randomAlphabetic(10);
        exportServiceStepDef.exportImage(idList, archiveName);
        String storageUrl = exportServiceStepDef.successWaitExportsWithParameters(archiveName, DONE, idList.size());
        Assert.contains(archiveName + ZIP_FORMAT, storageUrl, "название архива в ссылке");
    }

    @Test(description = "Успешный экспорт нескольких файлов - архив с заданным названием (верхняя граница)", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"232838"})
    public void successExportImagesArchiveWithLongNameTest() {
        String archiveName = RandomStringUtils.randomAlphabetic(200);
        exportServiceStepDef.exportImage(idList, archiveName);
        exportServiceStepDef.successWaitExportsWithParameters(archiveName, null, idList.size());
    }

    @Test(description = "Неуспешный экспорт - длинное название архива", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"232844"})
    public void unsuccessfulExportImageLongNameTest() {
        List<InvalidParamsItem> invalidParamsItems = exportServiceStepDef.unsuccessfulExportImage(idList, RandomStringUtils.randomAlphabetic(201));
        Assert.compareParameters(Collections.singletonList(getData().invalidParams().get(3)), invalidParamsItems, "ошибки");
    }

    @Test(description = "Успешный экспорт нескольких файлов - архив с заданным названием c пробелом и кириллицей", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"232840"})
    public void successExportImagesArchiveWithNameCyrillicSpaceTest() {
        String archiveName = RandomStringUtils.randomAlphabetic(10) + SPACE_VALUE + CYRILLIC_VALUE;
        exportServiceStepDef.exportImage(idList, archiveName);
        String storageUrl = exportServiceStepDef.successWaitExportsWithParameters(archiveName, DONE, idList.size());
        Assert.contains(archiveName + ZIP_FORMAT, storageUrl, "название архива в ссылке");
    }

    @Test(description = "Неуспешный экспорт - спецсимволы (название архива)", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"232843"})
    public void unsuccessfulExportImageSpecialCharactersNameTest() {
        List<InvalidParamsItem> invalidParamsItems = exportServiceStepDef.unsuccessfulExportImage(idList, SPECIAL_CHARACTERS);
        Assert.compareParameters(Collections.singletonList(getData().invalidParams().get(2)), invalidParamsItems, "ошибки");
    }

    @Test(description = "Успешная проверка списка экспорта по дате", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"252697"})
    public void successSortingExportsArchiveTest() {
        idList.forEach(id -> exportServiceStepDef.exportImage(idList));
        List<ExportsDataItem> allExports = exportServiceStepDef.successGetExports();

        //Проверка порядка от большего к меньшему
        List<String> startedAtTimes = allExports.stream().map(ExportsDataItem::createdAt).collect(Collectors.toList());
        long startedAt = Long.MAX_VALUE;
        for (String startedAtTime : startedAtTimes) {
            Instant instant = Instant.parse(startedAtTime.replaceAll("\\+00:00", "Z"));
            long epochSecond = instant.toEpochMilli();
            Assert.equalsTrueParameter(startedAt > epochSecond, "started at");
            startedAt = epochSecond;
        }
    }

    @Test(description = "Успешная фильтрация экспорта по дате", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"252698"})
    public void successFiltrationExportsArchiveTest() {
        exportServiceStepDef.exportImage(idList);
        List<ExportsDataItem> allExports = exportServiceStepDef.successGetExports();

        String nowDate = LocalDate.now().format(ISO_LOCAL_DATE);
        List<ExportsDataItem> todayExports = exportServiceStepDef.successGetExports(nowDate);
        Assert.equalsTrueParameter(allExports.size() >= todayExports.size(), "количество экспортов");

        todayExports.forEach(item -> Assert.contains(nowDate, item.createdAt(), "сегодняшняя дата"));
    }

    @Test(description = "Успешная фильтрация экспорта по дате - старая дата", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"252698"})
    public void successFiltrationExportsArchiveOldDateTest() {
        exportServiceStepDef.exportImage(idList);
        String nowDate = LocalDate.now().minusYears(10).format(ISO_LOCAL_DATE);
        List<ExportsDataItem> exports = exportServiceStepDef.successGetExports(nowDate);
        Assert.mustBeEmptyList(exports, "количество экспортов");
    }

    @Test(description = "Неуспешная фильтрация экспорта по дате - невалидная дата", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"252711"})
    public void unsuccessfulFiltrationExportsArchiveInvalidDateTest() {
        exportServiceStepDef.exportImage(idList);
        Random random = new Random();
        List<InvalidParamsItem> invalidParamsItems =
                exportServiceStepDef.unsuccessfulGetExports(String.format("2023-%d-%d", random.nextInt(80) + 12, random.nextInt(80) + 31));
        getData().invalidParams().get(2).name("export-data");
        Assert.compareParameters(Collections.singletonList(getData().invalidParams().get(2)), invalidParamsItems, "ошибки");
    }

    @Test(description = "Неуспешная фильтрация экспорта по дате - неверный формат даты", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"252711"})
    public void unsuccessfulFiltrationExportsArchiveInvalidDateFormatTest() {
        exportServiceStepDef.exportImage(idList);
        String nowDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy.MM.dd"));
        List<InvalidParamsItem> invalidParamsItems = exportServiceStepDef.unsuccessfulGetExports(nowDate);
        getData().invalidParams().get(2).name("export-data");
        Assert.compareParameters(Collections.singletonList(getData().invalidParams().get(2)), invalidParamsItems, "ошибки");
    }
}
