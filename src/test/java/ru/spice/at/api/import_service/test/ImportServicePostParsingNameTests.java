package ru.spice.at.api.import_service.test;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import ru.testit.annotations.WorkItemIds;
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

import java.util.*;

import static ru.spice.at.common.constants.TestConstants.*;
import static ru.spice.at.common.emuns.dam.ImageParameters.*;
import static ru.spice.at.common.utils.Assert.*;
import static ru.spice.at.common.utils.Assert.compareParameters;
import static ru.spice.at.common.utils.JsonHelper.getValueFromResponse;

@Feature("Import Service")
@Story("POST parsing name")
public class ImportServicePostParsingNameTests extends BaseApiTest<ImportServiceSettings> {
    private ImportServiceStepDef importServiceStepDef;
    private MetadataStepDef metadataStepDef;

    protected ImportServicePostParsingNameTests() {
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
        importServiceStepDef.deleteMedia();
        metadataStepDef.deleteMetadata();
    }

    @Test(description = "Успешный импорт - parse_filename=true", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"254590"})
    public void successImportImageWithParsingSKUPriorityTest() {
        String sku = RandomStringUtils.randomAlphabetic(10);
        int priority = new Random().nextInt(FOUR_HUNDRED_NINETY_NINE) + 1;
        String imageId = importServiceStepDef.importRandomImageWithParseFilename(new ImageData(ImageFormat.JPEG).setFilename(String.format(FILENAME_MASK, sku, priority, ImageFormat.JPEG.getFormatName())), true);
        Response response = metadataStepDef.checkMetadata(imageId);
        assertAll(
                () -> compareParameters(sku, getValueFromResponse(response, SKU.getPath()), "sku"),
                () -> compareParameters(priority, getValueFromResponse(response, PRIORITY.getPath()), PRIORITY.getName())
        );
    }

    @Test(description = "Успешный импорт - parse_filename=false", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"254599"})
    public void successImportImageParseFilenameFalseTest() {
        String sku = RandomStringUtils.randomAlphabetic(10);
        int priority = new Random().nextInt(FOUR_HUNDRED_NINETY_NINE) + 1;
        String imageId = importServiceStepDef.importRandomImageWithParseFilename(new ImageData(ImageFormat.JPEG).setFilename(String.format(FILENAME_MASK, sku, priority, ImageFormat.JPEG.getFormatName())), false);
        Response response = metadataStepDef.checkMetadata(imageId);
        assertAll(
                () -> mustBeNullParameter(getValueFromResponse(response, SKU.getPath()), SKU.getName()),
                () -> mustBeNullParameter(getValueFromResponse(response, PRIORITY.getPath()), PRIORITY.getName())
        );
    }

    @Test(description = "Успешный импорт - parse_filename=true - граничные значения", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"255047"})
    public void successImportImageWithParsingSKUPriorityLimitValuesTest() {
        String sku = RandomStringUtils.randomAlphabetic(100);
        int priority = FOUR_HUNDRED_NINETY_NINE + 1;
        String imageId = importServiceStepDef.importRandomImageWithParseFilename(new ImageData(ImageFormat.JPEG).setFilename(String.format(FILENAME_MASK, sku, priority, ImageFormat.JPEG.getFormatName())), true);
        Response response = metadataStepDef.checkMetadata(imageId);
        assertAll(
                () -> compareParameters(sku, getValueFromResponse(response, SKU.getPath()), "sku"),
                () -> compareParameters(priority, getValueFromResponse(response, PRIORITY.getPath()), PRIORITY.getName())
        );
    }

    @Test(description = "Неуспешный импорт (parse_filename=true) - приоритет 0", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"254595"})
    public void unsuccessfulImportImageWithParsingSKUPriorityNULLTest() {
        String invalidFilename = String.format(FILENAME_MASK, RandomStringUtils.randomAlphabetic(10), 0, ImageFormat.JPEG.getFormatName());
        List<InvalidParamsItem> image = importServiceStepDef.unsuccessfulImportRandomImageWithParseFilename(new ImageData(ImageFormat.JPEG).setFilename(invalidFilename), true);
        assertAll(
                () -> compareParameters(1, image.size(), "size"),
                () -> compareParameters(invalidFilename, image.get(0).name(), "name"),
                () -> compareParameters(getData().errorTypes().get(8).type(), image.get(0).type(), "type"),
                () -> compareParameters(getData().errorTypes().get(8).description(), image.get(0).reason(), "reason")
        );
    }

    @Test(description = "Неуспешный импорт (parse_filename=true) - приоритет > 500", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"254609"})
    public void unsuccessfulImportImageWithParsingSKUPriorityMore500Test() {
        String invalidFilename = String.format(FILENAME_MASK, RandomStringUtils.randomAlphabetic(10), FOUR_HUNDRED_NINETY_NINE + 2, ImageFormat.JPEG.getFormatName());
        List<InvalidParamsItem> image = importServiceStepDef.unsuccessfulImportRandomImageWithParseFilename(new ImageData(ImageFormat.JPEG).setFilename(invalidFilename), true);
        assertAll(
                () -> compareParameters(1, image.size(), "size"),
                () -> compareParameters(invalidFilename, image.get(0).name(), "name"),
                () -> compareParameters(getData().errorTypes().get(8).type(), image.get(0).type(), "type"),
                () -> compareParameters(getData().errorTypes().get(8).description(), image.get(0).reason(), "reason")
        );
    }

    @Test(description = "Неуспешный импорт (parse_filename=true) - в приоритете буквы и спецсимволы", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"254610"})
    public void unsuccessfulImportImageWithParsingSKUPriorityWithSymbolsTest() {
        String invalidFilename = RandomStringUtils.randomAlphabetic(10) + "_" + RandomStringUtils.randomAscii(3) + "." + ImageFormat.JPEG.getFormatName();
        List<InvalidParamsItem> image = importServiceStepDef.unsuccessfulImportRandomImageWithParseFilename(new ImageData(ImageFormat.JPEG).setFilename(invalidFilename), true);
        assertAll(
                () -> compareParameters(1, image.size(), "size"),
                () -> compareParameters(invalidFilename, image.get(0).name(), "name"),
                () -> compareParameters(getData().errorTypes().get(8).type(), image.get(0).type(), "type"),
                () -> compareParameters(getData().errorTypes().get(8).description(), image.get(0).reason(), "reason")
        );
    }

    @Test(description = "Неуспешный импорт (parse_filename=true) - SKU > 100", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"255048"})
    public void unsuccessfulImportImageWithParsingSKUMore100PriorityTest() {
        String invalidFilename = String.format(FILENAME_MASK, RandomStringUtils.randomAlphabetic(101), new Random().nextInt(FOUR_HUNDRED_NINETY_NINE) + 1, ImageFormat.JPEG.getFormatName());
        List<InvalidParamsItem> image = importServiceStepDef.unsuccessfulImportRandomImageWithParseFilename(new ImageData(ImageFormat.JPEG).setFilename(invalidFilename), true);
        assertAll(
                () -> compareParameters(1, image.size(), "size"),
                () -> compareParameters(invalidFilename, image.get(0).name(), "name"),
                () -> compareParameters(getData().errorTypes().get(8).type(), image.get(0).type(), "type"),
                () -> compareParameters(getData().errorTypes().get(8).description(), image.get(0).reason(), "reason")
        );
    }

    @Test(description = "Неуспешный импорт (parse_filename=true) - в SKU спецсимволы", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"254611"})
    public void unsuccessfulImportImageWithParsingSKUWithSymbolsPriorityTest() {
        String invalidFilename = String.format(FILENAME_MASK, RandomStringUtils.randomAscii(10) + SPECIAL_CHARACTERS, new Random().nextInt(FOUR_HUNDRED_NINETY_NINE) + 1, ImageFormat.JPEG.getFormatName());
        List<InvalidParamsItem> image = importServiceStepDef.unsuccessfulImportRandomImageWithParseFilename(new ImageData(ImageFormat.JPEG).setFilename(invalidFilename), true);
        assertAll(
                () -> compareParameters(1, image.size(), "size"),
                () -> compareParameters(invalidFilename, image.get(0).name(), "name"),
                () -> compareParameters(getData().errorTypes().get(8).type(), image.get(0).type(), "type"),
                () -> compareParameters(getData().errorTypes().get(8).description(), image.get(0).reason(), "reason")
        );
    }

    @Test(description = "Неуспешный импорт (parse_filename=true) - нет нижнего подчеркивания в названии", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"254607"})
    public void unsuccessfulImportImageWithParsingSKUPriorityWithoutUnderscoreTest() {
        String invalidFilename = RandomStringUtils.randomAlphabetic(10) + (new Random().nextInt(FOUR_HUNDRED_NINETY_NINE) + 1) + "." + ImageFormat.JPEG.getFormatName();
        List<InvalidParamsItem> image = importServiceStepDef.unsuccessfulImportRandomImageWithParseFilename(new ImageData(ImageFormat.JPEG).setFilename(invalidFilename), true);
        assertAll(
                () -> compareParameters(1, image.size(), "size"),
                () -> compareParameters(invalidFilename, image.get(0).name(), "name"),
                () -> compareParameters(getData().errorTypes().get(8).type(), image.get(0).type(), "type"),
                () -> compareParameters(getData().errorTypes().get(8).description(), image.get(0).reason(), "reason")
        );
    }

    @Test(description = "Неуспешный импорт (parse_filename=true) - несколько нижних подчеркиваний в названии", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"254616"})
    public void unsuccessfulImportImageWithParsingSKUPriorityWithSomeUnderscoreTest() {
        String invalidFilename = RandomStringUtils.randomAlphabetic(5) + "_" + RandomStringUtils.randomAlphabetic(5) + "_" + (new Random().nextInt(FOUR_HUNDRED_NINETY_NINE) + 1) + "." + ImageFormat.JPEG.getFormatName();
        List<InvalidParamsItem> image = importServiceStepDef.unsuccessfulImportRandomImageWithParseFilename(new ImageData(ImageFormat.JPEG).setFilename(invalidFilename), true);
        assertAll(
                () -> compareParameters(1, image.size(), "size"),
                () -> compareParameters(invalidFilename, image.get(0).name(), "name"),
                () -> compareParameters(getData().errorTypes().get(8).type(), image.get(0).type(), "type"),
                () -> compareParameters(getData().errorTypes().get(8).description(), image.get(0).reason(), "reason")
        );
    }
}
