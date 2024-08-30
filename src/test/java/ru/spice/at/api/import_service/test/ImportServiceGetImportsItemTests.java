package ru.spice.at.api.import_service.test;

import io.qameta.allure.Feature;
import io.qameta.allure.Issue;
import io.qameta.allure.Story;
import ru.testit.annotations.WorkItemIds;
import org.apache.commons.lang3.RandomStringUtils;
import org.testng.annotations.*;
import ru.spice.at.api.dto.response.import_service.InvalidParamsItem;
import ru.spice.at.api.dto.response.import_service.imports.item.ImportItemsItem;
import ru.spice.at.api.dto.response.import_service.imports.item.ImportsItemResponse;
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
import java.util.UUID;

import static ru.spice.at.common.utils.Assert.assertAll;
import static ru.spice.at.common.utils.Assert.compareParameters;

@Feature("Import Service")
@Story("GET imports item")
public class ImportServiceGetImportsItemTests extends BaseApiTest<ImportServiceSettings> {
    private final ImportServiceStepDef importServiceStepDef;

    private String importId;

    protected ImportServiceGetImportsItemTests() {
        super(ApiServices.IMPORT_SERVICE);
        importServiceStepDef = new ImportServiceStepDef();
    }

    @BeforeClass(description = "Добавляем файлы", alwaysRun = true)
    public void beforeClass() {
        importId = importServiceStepDef.successImportOpen(6);
        List<ImageData> imageDataList = Arrays.asList(new ImageData(ImageFormat.JPEG), new ImageData(ImageFormat.PNG), new ImageData(ImageFormat.JPG));
        imageDataList.forEach(image -> importServiceStepDef.importRandomImages(image, importId));

        List<ImageData> imageDataInvalidList = Arrays.asList(new ImageData(ImageFormat.INVALID), new ImageData(ImageFormat.INVALID), new ImageData(ImageFormat.INVALID));
        imageDataInvalidList.forEach(image -> importServiceStepDef.importInvalidRandomImages(image, importId));
    }

    //todo выключено для использования параллельного сьюта
    //@AfterClass(alwaysRun = true)
    public void afterClass() {
        importServiceStepDef.deleteImports();
        new MetadataStepDef(importServiceStepDef.getAuthToken()).deleteMetadata();
    }

    @Test(description = "Успешное получение успешной загрузки", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"244595"})
    public void successValidImportsTest() {
        String importId = importServiceStepDef.successImportOpen(3);
        List<ImageData> imageDataList = Arrays.asList(new ImageData(ImageFormat.JPEG), new ImageData(ImageFormat.PNG), new ImageData(ImageFormat.JPG));
        imageDataList.forEach(image -> importServiceStepDef.importRandomImages(image, importId));

        ImportsItemResponse importsItem = importServiceStepDef.getImportsItem(importId);
        Assert.compareParameters(3, importsItem.data().importItems().size(), "size");
    }

    @Test(description = "Успешное получение загрузки с успешными файлами и файлами с ошибкой", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"244596"})
    public void successValidAndInvalidImportsTest() {
        ImportsItemResponse importsItem = importServiceStepDef.getImportsItem(importId);
        Assert.compareParameters(6, importsItem.data().importItems().size(), "size");
    }

    @Test(description = "Успешное получение только успешно загруженных файлов", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"244599"})
    public void successValidOnlyImportsTest() {
        ImportsItemResponse importsItem = importServiceStepDef.getImportsItem(importId, Collections.singletonMap("import_filter", "success"));
        Assert.compareParameters(3, importsItem.data().importItems().size(), "size");

        importsItem.data().importItems().stream().map(ImportItemsItem::validationError).
                forEach(validationErrorList -> Assert.mustBeEmptyList(validationErrorList, "Validation Error List"));
    }

    @Test(description = "Успешное получение только не загруженных файлов с ошибками", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"244600"})
    public void successInvalidOnlyImportsTest() {
        ImportsItemResponse importsItem = importServiceStepDef.getImportsItem(importId, Collections.singletonMap("import_filter", "failed"));
        Assert.compareParameters(3, importsItem.data().importItems().size(), "size");

        importsItem.data().importItems().stream().map(ImportItemsItem::validationError).
                forEach(validationErrorList -> Assert.notNullOrEmptyParameter(validationErrorList.size(), "Validation Error List size"));
    }

    @Issue("SPC-1958")
    @Test(description = "Неуспешное получение загрузки - неверный import_id", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"244601"})
    public void unsuccessfulItemImportsTest() {
        List<InvalidParamsItem> invalidParamsItems = importServiceStepDef.unsuccessfulGetImportsItem(UUID.randomUUID().toString(), Collections.emptyMap());
        assertAll(
                () -> compareParameters(1, invalidParamsItems.size(), "size"),
                () -> compareParameters("import_id", invalidParamsItems.get(0).name(), "name"),
                () -> compareParameters(getData().errorNotFound(), invalidParamsItems.get(0).type(), "type"),
                () -> compareParameters(getData().errorNotFoundReason(), invalidParamsItems.get(0).reason(), "reason")
        );
    }

    @Test(description = "Неуспешное получение загрузки - невалидный import_id", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"244601"})
    public void unsuccessfulInvalidItemImportsTest() {
        List<InvalidParamsItem> invalidParamsItems = importServiceStepDef.unsuccessfulGetImportsItem(RandomStringUtils.randomAlphabetic(5), Collections.emptyMap());
        assertAll(
                () -> compareParameters(1, invalidParamsItems.size(), "size"),
                () -> compareParameters("id", invalidParamsItems.get(0).name(), "name"),
                () -> compareParameters(getData().errorInvalidType(), invalidParamsItems.get(0).type(), "type"),
                () -> compareParameters(getData().errorInvalidTypeReason(), invalidParamsItems.get(0).reason(), "reason")
        );
    }
}