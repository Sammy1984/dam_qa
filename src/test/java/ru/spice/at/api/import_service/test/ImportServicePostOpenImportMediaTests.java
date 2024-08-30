package ru.spice.at.api.import_service.test;

import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import ru.testit.annotations.WorkItemIds;
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
import ru.spice.at.common.utils.Assert;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

@Feature("Import Service")
@Story("POST import open")
public class ImportServicePostOpenImportMediaTests extends BaseApiTest<ImportServiceSettings> {
    private ImportServiceStepDef importServiceStepDef;

    protected ImportServicePostOpenImportMediaTests() {
        super(ApiServices.IMPORT_SERVICE);
    }

    @BeforeClass(alwaysRun = true)
    public void beforeClass() {
        importServiceStepDef = new ImportServiceStepDef();
    }

    //todo выключено для использования параллельного сьюта
    //@AfterClass(alwaysRun = true)
    public void afterClass() {
        importServiceStepDef.deleteMedia();
        new MetadataStepDef(importServiceStepDef.getAuthToken()).deleteMetadata();
    }

    @Test(description = "Успешный импорт одного файла c import-open", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"239674"})
    public void successOpenImportImageTest() {
        String importId = importServiceStepDef.successImportOpen(1);
        importServiceStepDef.importRandomImages(new ImageData(ImageFormat.JPEG), importId);
    }

    @Test(description = "Успешный импорт одного файла c import-open total_plan = 10000", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"239685"})
    public void successOpenImport10000TotalPlanImageTest() {
        String importId = importServiceStepDef.successImportOpen(10000);
        importServiceStepDef.importRandomImages(new ImageData(ImageFormat.JPEG), importId);
    }

    @Test(description = "Успешный импорт нескольких файлов c import-open", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"239675"})
    public void successOpenImportImagesTest() {
        List<ImageData> imageDataList = Arrays.asList(new ImageData(ImageFormat.JPEG), new ImageData(ImageFormat.PNG), new ImageData(ImageFormat.JPG));

        String importId = importServiceStepDef.successImportOpen(imageDataList.size());
        imageDataList.forEach(image -> importServiceStepDef.importRandomImages(image, importId));
    }

    @Test(description = "Успешный импорт нескольких валидных и невалидных файлов c import-open", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"239676"})
    public void successOpenImportValidAndInvalidImagesTest() {
        List<ImageData> imageDataList = Arrays.asList(new ImageData(ImageFormat.JPEG), new ImageData(ImageFormat.INVALID));

        String importId = importServiceStepDef.successImportOpen(imageDataList.size());

        importServiceStepDef.importRandomImages(imageDataList.get(0), importId);
        importServiceStepDef.importInvalidRandomImages(imageDataList.get(1), importId);
    }

    //todo - доработать после разработки функционала
    //@Test(description = "Неуспешный импорт нескольких файлов в количестве > total_plan", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"239680"})
    public void unsuccessfulOpenImportManyImagesTest() {
        List<ImageData> imageDataList = Arrays.asList(
                new ImageData(ImageFormat.JPEG), new ImageData(ImageFormat.PNG), new ImageData(ImageFormat.JPG), new ImageData(ImageFormat.JPEG));

        String importId = importServiceStepDef.successImportOpen(imageDataList.size() - 1);
        //imageDataList.forEach(image -> importServiceStepDef.importRandomImages(image, importId));
    }

    @Test(description = "Неуспешный импорт total_plan <= 0", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"239677"})
    public void unsuccessfulOpenImportNegativeTotalPlanImageTest() {
        List<InvalidParamsItem> invalidParamsItems = importServiceStepDef.unsuccessfulImportOpen(-new Random().nextInt(10));

        List<InvalidParamsItem> invalidParamsItemsExp = Collections.singletonList(
                new InvalidParamsItem().
                        name("total_plan").
                        type(getData().errorTypes().get(5).type()).
                        reason(getData().errorTypes().get(5).description())
        );

        Assert.compareParameters(invalidParamsItemsExp, invalidParamsItems, "сообщение об ошибке");
    }

    @Test(description = "Неуспешный импорт total_plan > 10000", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"239678"})
    public void unsuccessfulOpenImportManyTotalPlanImageTest() {
        List<InvalidParamsItem> invalidParamsItems = importServiceStepDef.unsuccessfulImportOpen(10001 + new Random().nextInt(100));

        List<InvalidParamsItem> invalidParamsItemsExp = Collections.singletonList(
                new InvalidParamsItem().
                        name("total_plan").
                        type(getData().errorTypes().get(5).type()).
                        reason(getData().errorTypes().get(5).description())
        );

        Assert.compareParameters(invalidParamsItemsExp, invalidParamsItems, "сообщение об ошибке");
    }

    @Test(description = "Неуспешный импорт total_plan - невалидный", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"239679"})
    public void unsuccessfulOpenImportInvalidTotalPlanImageTest() {
        List<InvalidParamsItem> invalidParamsItems = importServiceStepDef.unsuccessfulImportOpen(RandomStringUtils.randomAlphabetic(5));

        List<InvalidParamsItem> invalidParamsItemsExp = Collections.singletonList(
                new InvalidParamsItem().
                        name("total_plan").
                        type(getData().errorTypes().get(0).type()).
                        reason(getData().errorTypes().get(0).description())
        );

        Assert.compareParameters(invalidParamsItemsExp, invalidParamsItems, "сообщение об ошибке");
    }

    @Test(description = "Неуспешный импорт total_plan = null", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"239686"})
    public void unsuccessfulOpenImportNullTotalPlanImageTest() {
        List<InvalidParamsItem> invalidParamsItems = importServiceStepDef.unsuccessfulImportOpen(null);

        List<InvalidParamsItem> invalidParamsItemsExp = Collections.singletonList(
                new InvalidParamsItem().
                        name("total_plan").
                        type(getData().errorTypes().get(0).type()).
                        reason(getData().errorTypes().get(0).description())
        );

        Assert.compareParameters(invalidParamsItemsExp, invalidParamsItems, "сообщение об ошибке");
    }
}
