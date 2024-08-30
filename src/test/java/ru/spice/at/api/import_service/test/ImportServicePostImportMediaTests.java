package ru.spice.at.api.import_service.test;

import io.qameta.allure.*;
import io.restassured.response.Response;
import org.apache.commons.lang3.RandomStringUtils;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.spice.at.api.dto.response.import_service.InvalidParamsItem;
import ru.spice.at.api.import_service.ImportServiceSettings;
import ru.spice.at.api.import_service.ImportServiceStepDef;
import ru.spice.at.api.metadata_service.MetadataStepDef;
import ru.spice.at.common.base_test.BaseApiTest;
import ru.spice.at.common.dto.dam.ImageData;
import ru.spice.at.common.emuns.ApiServices;
import ru.spice.at.common.emuns.dam.ImageFormat;
import ru.spice.at.common.emuns.dam.Quality;
import ru.spice.at.common.utils.Assert;
import ru.testit.annotations.WorkItemIds;

import java.util.*;
import java.util.stream.Collectors;

import static ru.spice.at.common.constants.TestConstants.DOT_VALUE;
import static ru.spice.at.common.constants.TestConstants.MAX_JPG_FILENAME_SYMBOLS;
import static ru.spice.at.common.emuns.dam.ImageParameters.QUALITY_ID;
import static ru.spice.at.common.utils.ImageHelper.getRandomByteImage;
import static ru.spice.at.common.utils.JsonHelper.getValueFromResponse;

@Feature("Import Service")
@Story("POST Import media with validations")
public class ImportServicePostImportMediaTests extends BaseApiTest<ImportServiceSettings> {
    private ImportServiceStepDef importServiceStepDef;
    private MetadataStepDef metadataStepDef;

    protected ImportServicePostImportMediaTests() {
        super(ApiServices.IMPORT_SERVICE);
    }

    @BeforeClass(alwaysRun = true)
    public void beforeClass() {
        importServiceStepDef = new ImportServiceStepDef();
        metadataStepDef = new MetadataStepDef(importServiceStepDef.getAuthToken());
    }

    //todo выключено для использования параллельного сьюта
    //@AfterClass(alwaysRun = true)
    public void afterClass() {
        importServiceStepDef.deleteImports();
        metadataStepDef.deleteMetadata();
    }

    @Test(description = "Успешный импорт одного валидного файла", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"225833"})
    public void successImportImageTest() {
        importServiceStepDef.importRandomImages(getData().validImages().get(0));
    }

    @Test(description = "Успешный импорт одного валидного файла - длинное название", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"225833"})
    public void successImportImageLongNameTest() {
        ImageData imageData = new ImageData(ImageFormat.JPEG).
                setFilename(RandomStringUtils.randomAlphabetic(MAX_JPG_FILENAME_SYMBOLS) + DOT_VALUE + ImageFormat.JPEG.getFormatName());
        importServiceStepDef.importRandomImages(imageData);
    }

    @Test(description = "Ошибка импорта 'Неверный формат файла'", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"225836"})
    public void unsuccessfulImportInvalidFormatImageTest() {
        ImageData imageData = new ImageData(ImageFormat.INVALID);
        List<InvalidParamsItem> invalidParamsItems = importServiceStepDef.importInvalidRandomImages(imageData);

        List<InvalidParamsItem> invalidParamsItemsExp = Arrays.asList(
                new InvalidParamsItem().
                        name(imageData.getFilename()).
                        type(getData().errorTypes().get(1).type()).
                        reason(getData().errorTypes().get(1).description()),
                new InvalidParamsItem().
                        name(imageData.getFilename()).
                        type(getData().errorTypes().get(4).type()).
                        reason(getData().errorTypes().get(4).description())
        );

        Assert.compareParameters(new LinkedList<>(invalidParamsItemsExp), new LinkedList<>(invalidParamsItems), "сообщение об ошибке");
    }

    @Test(description = "Ошибка импорта 'Битый файл'", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"225839"})
    public void unsuccessfulImportBrokenImageTest() {
        ImageData imageData = new ImageData(ImageFormat.JPEG);
        byte[] imageBytes = getRandomByteImage(imageData.getWidth(), imageData.getHeight(), imageData.getFormat().getFormatName());

        int brokenCount = 30;
        Assert.equalsTrueParameter(imageBytes.length > brokenCount, "количество байтов");
        for (int i = 0; i < brokenCount; i++) {
            imageBytes[i] = 0;
        }
        List<InvalidParamsItem> invalidParamsItems = importServiceStepDef.importInvalidRandomImages(imageData.getFilename(), imageBytes);

        List<InvalidParamsItem> invalidParamsItemsExp = Arrays.asList(
                new InvalidParamsItem().
                        name(imageData.getFilename()).
                        type(getData().errorTypes().get(1).type()).
                        reason(getData().errorTypes().get(1).description()),
                new InvalidParamsItem().
                        name(imageData.getFilename()).
                        type(getData().errorTypes().get(7).type()).
                        reason(getData().errorTypes().get(7).description())
        );

        Assert.compareParameters(new LinkedList<>(invalidParamsItemsExp), new LinkedList<>(invalidParamsItems), "сообщение об ошибке");
    }

    @Test(description = "Ошибка импорта 'Пустой файл'", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"225840"})
    public void unsuccessfulImportEmptyImageTest() {
        ImageData imageData = new ImageData(ImageFormat.JPEG);
        List<InvalidParamsItem> invalidParamsItems = importServiceStepDef.importInvalidRandomImages(imageData.getFilename(), new byte[0]);

        List<InvalidParamsItem> invalidParamsItemsExp = Arrays.asList(
                new InvalidParamsItem().
                        name(imageData.getFilename()).
                        type(getData().errorTypes().get(1).type()).
                        reason(getData().errorTypes().get(1).description()),
                new InvalidParamsItem().
                        name(imageData.getFilename()).
                        type(getData().errorTypes().get(7).type()).
                        reason(getData().errorTypes().get(7).description())
        );

        Assert.compareParameters(new LinkedList<>(invalidParamsItemsExp), new LinkedList<>(invalidParamsItems), "сообщение об ошибке");
    }

    @Test(description = "Ошибка импорта 'Длина наименования'", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"225837"})
    public void unsuccessfulImportLongNameImageTest() {
        ImageData imageData = new ImageData(ImageFormat.JPEG).
                setFilename(RandomStringUtils.randomAlphabetic(MAX_JPG_FILENAME_SYMBOLS + 1) + DOT_VALUE + ImageFormat.JPEG.getFormatName());
        List<InvalidParamsItem> invalidParamsItems = importServiceStepDef.importInvalidRandomImages(imageData);

        List<InvalidParamsItem> invalidParamsItemsExp = Collections.singletonList(
                new InvalidParamsItem().
                        name(imageData.getFilename()).
                        type(getData().errorTypes().get(3).type()).
                        reason(getData().errorTypes().get(3).description())
        );

        Assert.compareParameters(invalidParamsItemsExp, invalidParamsItems, "сообщение об ошибке");
    }

    @Test(description = "Несколько ошибок импорта одного файла", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"225838"})
    public void unsuccessfulImportLongInvalidNameAndFormatImageTest() {
        ImageData imageData = new ImageData(ImageFormat.INVALID).
                setFilename(RandomStringUtils.randomAlphabetic(255) + DOT_VALUE + ImageFormat.INVALID.getFormatName());
        List<InvalidParamsItem> invalidParamsItems = importServiceStepDef.importInvalidRandomImages(imageData);

        List<InvalidParamsItem> invalidParamsItemsExp = Arrays.asList(
                new InvalidParamsItem().
                        name(imageData.getFilename()).
                        type(getData().errorTypes().get(1).type()).
                        reason(getData().errorTypes().get(1).description()),
                new InvalidParamsItem().
                        name(imageData.getFilename()).
                        type(getData().errorTypes().get(3).type()).
                        reason(getData().errorTypes().get(3).description()),
                new InvalidParamsItem().
                        name(imageData.getFilename()).
                        type(getData().errorTypes().get(4).type()).
                        reason(getData().errorTypes().get(4).description())
        );

        Assert.compareParameters(new LinkedList<>(invalidParamsItemsExp), new LinkedList<>(invalidParamsItems), "сообщение об ошибке");
    }

    @Test(description = "Загрузка валидного файла качества - 'Плохое'", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"230064"})
    public void successImportBadQualityImageTest() {
        List<String> ids = getData().badQualityImages().stream().map(image -> importServiceStepDef.importRandomImages(image)).collect(Collectors.toList());
        ids.forEach(id -> {
            Response response = metadataStepDef.checkMetadata(id);
            Assert.compareParameters(
                    Quality.BAD.getName(), getValueFromResponse(response, QUALITY_ID.getPath() + ".name"), "quality.name");
        });
    }

    @Test(description = "Загрузка валидного файла качества - 'Хорошее'", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"230065"})
    public void successImportGoodQualityImageTest() {
        List<String> ids = getData().goodQualityImages().stream().map(image -> importServiceStepDef.importRandomImages(image)).collect(Collectors.toList());
        ids.forEach(id -> {
            Response response = metadataStepDef.checkMetadata(id);
            Assert.compareParameters(
                    Quality.GOOD.getName(), getValueFromResponse(response, QUALITY_ID.getPath() + ".name"), "quality.name");
        });
    }

    @Test(description = "Загрузка валидного файла качества - 'На доработку'", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"230066"})
    public void successImportToRevisionQualityImageTest() {
        List<String> ids = getData().toRevisionQualityImages().stream().map(image -> importServiceStepDef.importRandomImages(image)).collect(Collectors.toList());
        ids.forEach(id -> {
            Response response = metadataStepDef.checkMetadata(id);
            Assert.compareParameters(
                    Quality.TO_REVISION.getName(), getValueFromResponse(response, QUALITY_ID.getPath() + ".name"), "quality.name");
        });
    }
}
