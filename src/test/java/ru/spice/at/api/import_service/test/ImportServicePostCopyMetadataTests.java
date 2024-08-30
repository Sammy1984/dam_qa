package ru.spice.at.api.import_service.test;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import lombok.SneakyThrows;
import ru.spice.at.api.dto.request.import_service.ExternalImportData;
import ru.spice.at.api.dto.request.import_service.RequestExternalImport;
import ru.spice.at.api.dto.response.metadata.DictionariesItem;
import ru.spice.at.common.emuns.dam.Status;
import ru.spice.at.common.utils.CipherHelper;
import ru.testit.annotations.WorkItemIds;
import io.restassured.response.Response;
import org.apache.commons.lang3.RandomStringUtils;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.spice.at.api.import_service.ImportServiceSettings;
import ru.spice.at.api.import_service.ImportServiceStepDef;
import ru.spice.at.api.metadata_service.MetadataStepDef;
import ru.spice.at.common.base_test.BaseApiTest;
import ru.spice.at.common.dto.dam.ImageData;
import ru.spice.at.common.emuns.ApiServices;
import ru.spice.at.common.emuns.dam.ImageFormat;

import java.time.OffsetDateTime;
import java.util.*;

import static java.time.format.DateTimeFormatter.ISO_INSTANT;
import static ru.spice.at.common.constants.TestConstants.*;
import static ru.spice.at.common.emuns.dam.ImageParameters.*;
import static ru.spice.at.common.utils.Assert.*;
import static ru.spice.at.common.utils.Assert.compareParameters;
import static ru.spice.at.common.utils.ImageHelper.getRandomByteImage;
import static ru.spice.at.common.utils.JsonHelper.getValueFromResponse;

@Feature("Import Service")
@Story("POST import with copy_metadata")
public class ImportServicePostCopyMetadataTests extends BaseApiTest<ImportServiceSettings> {
    private ImportServiceStepDef importServiceStepDef;
    private MetadataStepDef metadataStepDef;
    private List<DictionariesItem> statuses;
    private Map<String, Object> editValues;
    private String externalToken;


    protected ImportServicePostCopyMetadataTests() {
        super(ApiServices.IMPORT_SERVICE);
    }

    @BeforeClass(alwaysRun = true)
    @SneakyThrows
    public void beforeClass() {
        importServiceStepDef = new ImportServiceStepDef();
        metadataStepDef = new MetadataStepDef(importServiceStepDef.getAuthToken());
        externalToken = CipherHelper.decrypt(getData().externalToken());

        statuses = metadataStepDef.getListStatusesMetadata();
        editValues = new HashMap<>() {{
            put(SKU.getName(), RandomStringUtils.randomAlphabetic(10));
            put(PRIORITY.getName(), new Random().nextInt(NINETY_NINE) + 1);
            put(MASTER_CATEGORY_ID.getName(), metadataStepDef.getListCategoriesMetadata().get(0).id());
            put(SOURCE_ID.getName(), metadataStepDef.getListSourcesMetadata().get(0).id());
            put(ASSIGNEE_ID.getName(), metadataStepDef.getListUsersMetadata().get(0).id());
            put(EXTERNAL_TASK_ID.getName(), String.valueOf(new Random().nextInt(NINETY_NINE) + 1));
            put(MASTER_SELLER_ID.getName(), metadataStepDef.getListRetailersMetadata().data().get(0).id());
            put(IS_OWN_TRADEMARK.getName(), true);
            put(IS_COPYRIGHT.getName(), true);
            put(DESCRIPTION.getName(), RandomStringUtils.randomAlphabetic(6));
            put(KEYWORDS.getName(), Collections.singletonList(RandomStringUtils.randomAlphabetic(6)));
        }};
    }

    //todo выключено для использования параллельного сьюта
    //@AfterClass(alwaysRun = true)
    public void afterClass() {
        importServiceStepDef.deleteMedia();
        metadataStepDef.deleteMetadata();
    }

    @Test(description = "Успешный импорт - первый файл Новый - copy_metadata=true", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"291489"})
    public void successImportImageWithCopyMetadataFirstFileNewStatusTest() {
        ImageData imageData = new ImageData(ImageFormat.JPEG).setFilename(RandomStringUtils.randomAlphabetic(6) + "." + ImageFormat.JPEG.getFormatName());
        String imageFirstId = importServiceStepDef.importImageWithCopyMetadata(imageData, false);
        metadataStepDef.successEditMetadata(imageFirstId, editValues);

        String imageSecondId = importServiceStepDef.importImageWithCopyMetadata(imageData, true);
        Response responseSecondFile = metadataStepDef.checkMetadata(imageSecondId);

        assertAll(
                () -> compareParameters(imageData.getFilename(), getValueFromResponse(responseSecondFile, FILENAME.getPath()), "filename"),
                () -> compareParameters(statuses.stream().filter(item -> item.name().equals(Status.NEW.getName())).
                               map(DictionariesItem::id).findFirst().orElse(null), getValueFromResponse(responseSecondFile, STATUS_ID.getPath() + ".id"), "status_id"),
                () -> compareParameters(statuses.stream().filter(item -> item.name().equals(Status.ARCHIVE.getName())).
                        map(DictionariesItem::id).findFirst().orElse(null), getValueFromResponse(metadataStepDef.checkMetadata(imageFirstId), STATUS_ID.getPath() + ".id"), "status_id"),
                () -> compareParameters(editValues.get(SKU.getName()), getValueFromResponse(responseSecondFile, SKU.getPath()), "sku"),
                () -> compareParameters(editValues.get(MASTER_CATEGORY_ID.getName()), getValueFromResponse(responseSecondFile, MASTER_CATEGORY_ID.getPath() + ".id"), "category_id"),
                () -> compareParameters(editValues.get(SOURCE_ID.getName()), getValueFromResponse(responseSecondFile, SOURCE_ID.getPath() + ".id"), "source_id"),
                () -> compareParameters(editValues.get(ASSIGNEE_ID.getName()), getValueFromResponse(responseSecondFile, ASSIGNEE_ID.getPath() + ".id"), "assignee_id"),
                () -> compareParameters(editValues.get(PRIORITY.getName()), getValueFromResponse(responseSecondFile, PRIORITY.getPath()), "priority"),
                () -> compareParameters(editValues.get(EXTERNAL_TASK_ID.getName()), getValueFromResponse(responseSecondFile, EXTERNAL_TASK_ID.getPath()), "external_task_id"),
                () -> compareParameters(editValues.get(MASTER_SELLER_ID.getName()), getValueFromResponse(responseSecondFile, MASTER_SELLER.getPath() + ".id"), "master_seller_id"),
                () -> compareParameters(editValues.get(IS_OWN_TRADEMARK.getName()), getValueFromResponse(responseSecondFile, IS_OWN_TRADEMARK.getPath()), "is_own_trademark"),
                () -> compareParameters(editValues.get(IS_COPYRIGHT.getName()), getValueFromResponse(responseSecondFile, IS_COPYRIGHT.getPath()), "is_copyright"),
                () -> compareParameters(editValues.get(DESCRIPTION.getName()), getValueFromResponse(responseSecondFile, DESCRIPTION.getPath()), "description"),
                () -> compareParameters(editValues.get(KEYWORDS.getName()), getValueFromResponse(responseSecondFile, KEYWORDS.getPath()), "keywords")
        );
    }

    @Test(description = "Успешный импорт - первый файл Готов к проверке - copy_metadata=true", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"291490"})
    public void successImportImageWithCopyMetadataFirstFileReadyForTestStatusTest() {
        String readyForTestStatusId = statuses.stream().filter(item -> item.name().equals(Status.READY_FOR_TEST.getName())).
                map(DictionariesItem::id).findFirst().orElse(null);

        ImageData imageData = new ImageData(ImageFormat.JPEG).setFilename(RandomStringUtils.randomAlphabetic(6) + "." + ImageFormat.JPEG.getFormatName());
        String imageFirstId = importServiceStepDef.importImageWithCopyMetadata(imageData, false);
        metadataStepDef.successEditMetadata(imageFirstId, editValues);
        metadataStepDef.successEditMetadata(imageFirstId, STATUS_ID.getName(), readyForTestStatusId);

        String imageSecondId = importServiceStepDef.importImageWithCopyMetadata(imageData, true);
        Response responseSecondFile = metadataStepDef.checkMetadata(imageSecondId);

        assertAll(
                () -> compareParameters(imageData.getFilename(), getValueFromResponse(responseSecondFile, FILENAME.getPath()), "filename"),
                () -> compareParameters(statuses.stream().filter(item -> item.name().equals(Status.NEW.getName())).
                        map(DictionariesItem::id).findFirst().orElse(null), getValueFromResponse(responseSecondFile, STATUS_ID.getPath() + ".id"), "status_id"),
                () -> compareParameters(statuses.stream().filter(item -> item.name().equals(Status.ARCHIVE.getName())).
                        map(DictionariesItem::id).findFirst().orElse(null), getValueFromResponse(metadataStepDef.checkMetadata(imageFirstId), STATUS_ID.getPath() + ".id"), "status_id"),
                () -> compareParameters(editValues.get(MASTER_CATEGORY_ID.getName()), getValueFromResponse(responseSecondFile, MASTER_CATEGORY_ID.getPath() + ".id"), "category_id"),
                () -> compareParameters(editValues.get(SOURCE_ID.getName()), getValueFromResponse(responseSecondFile, SOURCE_ID.getPath() + ".id"), "source_id"),
                () -> compareParameters(editValues.get(ASSIGNEE_ID.getName()), getValueFromResponse(responseSecondFile, ASSIGNEE_ID.getPath() + ".id"), "assignee_id"),
                () -> compareParameters(editValues.get(PRIORITY.getName()), getValueFromResponse(responseSecondFile, PRIORITY.getPath()), "priority"),
                () -> compareParameters(editValues.get(EXTERNAL_TASK_ID.getName()), getValueFromResponse(responseSecondFile, EXTERNAL_TASK_ID.getPath()), "external_task_id"),
                () -> compareParameters(editValues.get(MASTER_SELLER_ID.getName()), getValueFromResponse(responseSecondFile, MASTER_SELLER.getPath() + ".id"), "master_seller_id"),
                () -> compareParameters(editValues.get(SKU.getName()), getValueFromResponse(responseSecondFile, SKU.getPath()), "sku"),
                () -> compareParameters(editValues.get(IS_OWN_TRADEMARK.getName()), getValueFromResponse(responseSecondFile, IS_OWN_TRADEMARK.getPath()), "is_own_trademark"),
                () -> compareParameters(editValues.get(IS_COPYRIGHT.getName()), getValueFromResponse(responseSecondFile, IS_COPYRIGHT.getPath()), "is_copyright"),
                () -> compareParameters(editValues.get(DESCRIPTION.getName()), getValueFromResponse(responseSecondFile, DESCRIPTION.getPath()), "description"),
                () -> compareParameters(editValues.get(KEYWORDS.getName()), getValueFromResponse(responseSecondFile, KEYWORDS.getPath()), "keywords")
        );
    }

    @Test(description = "Успешный импорт - первый файл В работе - copy_metadata=true", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"291491"})
    public void successImportImageWithCopyMetadataFirstFileInProgressStatusTest() {
        String inProgressStatusId = statuses.stream().filter(item -> item.name().equals(Status.IN_PROGRESS.getName())).
                map(DictionariesItem::id).findFirst().orElse(null);

        ImageData imageData = new ImageData(ImageFormat.JPEG).setFilename(RandomStringUtils.randomAlphabetic(6) + "." + ImageFormat.JPEG.getFormatName());
        String imageFirstId = importServiceStepDef.importImageWithCopyMetadata(imageData, false);
        metadataStepDef.successEditMetadata(imageFirstId, editValues);
        metadataStepDef.successEditMetadata(imageFirstId, STATUS_ID.getName(), inProgressStatusId);


        String imageSecondId = importServiceStepDef.importImageWithCopyMetadata(imageData, true);
        Response responseSecondFile = metadataStepDef.checkMetadata(imageSecondId);

        assertAll(
                () -> compareParameters(imageData.getFilename(), getValueFromResponse(responseSecondFile, FILENAME.getPath()), "filename"),
                () -> compareParameters(statuses.stream().filter(item -> item.name().equals(Status.NEW.getName())).
                        map(DictionariesItem::id).findFirst().orElse(null), getValueFromResponse(responseSecondFile, STATUS_ID.getPath() + ".id"), "status_id"),
                () -> compareParameters(statuses.stream().filter(item -> item.name().equals(Status.ARCHIVE.getName())).
                        map(DictionariesItem::id).findFirst().orElse(null), getValueFromResponse(metadataStepDef.checkMetadata(imageFirstId), STATUS_ID.getPath() + ".id"), "status_id"),
                () -> compareParameters(editValues.get(MASTER_CATEGORY_ID.getName()), getValueFromResponse(responseSecondFile, MASTER_CATEGORY_ID.getPath() + ".id"), "category_id"),
                () -> compareParameters(editValues.get(SOURCE_ID.getName()), getValueFromResponse(responseSecondFile, SOURCE_ID.getPath() + ".id"), "source_id"),
                () -> compareParameters(editValues.get(ASSIGNEE_ID.getName()), getValueFromResponse(responseSecondFile, ASSIGNEE_ID.getPath() + ".id"), "assignee_id"),
                () -> compareParameters(editValues.get(PRIORITY.getName()), getValueFromResponse(responseSecondFile, PRIORITY.getPath()), "priority"),
                () -> compareParameters(editValues.get(EXTERNAL_TASK_ID.getName()), getValueFromResponse(responseSecondFile, EXTERNAL_TASK_ID.getPath()), "external_task_id"),
                () -> compareParameters(editValues.get(MASTER_SELLER_ID.getName()), getValueFromResponse(responseSecondFile, MASTER_SELLER.getPath() + ".id"), "master_seller_id"),
                () -> compareParameters(editValues.get(SKU.getName()), getValueFromResponse(responseSecondFile, SKU.getPath()), "sku"),
                () -> compareParameters(editValues.get(IS_OWN_TRADEMARK.getName()), getValueFromResponse(responseSecondFile, IS_OWN_TRADEMARK.getPath()), "is_own_trademark"),
                () -> compareParameters(editValues.get(IS_COPYRIGHT.getName()), getValueFromResponse(responseSecondFile, IS_COPYRIGHT.getPath()), "is_copyright"),
                () -> compareParameters(editValues.get(DESCRIPTION.getName()), getValueFromResponse(responseSecondFile, DESCRIPTION.getPath()), "description"),
                () -> compareParameters(editValues.get(KEYWORDS.getName()), getValueFromResponse(responseSecondFile, KEYWORDS.getPath()), "keywords")
        );
    }

    @Test(description = "Успешный импорт - первый файл Архивный - copy_metadata=true", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"291492"})
    public void successImportImageWithCopyMetadataFirstFileArchiveStatusTest() {
        String archiveStatusId = statuses.stream().filter(item -> item.name().equals(Status.ARCHIVE.getName())).
                map(DictionariesItem::id).findFirst().orElse(null);

        ImageData imageData = new ImageData(ImageFormat.JPEG).setFilename(RandomStringUtils.randomAlphabetic(6) + "." + ImageFormat.JPEG.getFormatName());
        String imageFirstId = importServiceStepDef.importImageWithCopyMetadata(imageData, false);
        metadataStepDef.successEditMetadata(imageFirstId, editValues);
        metadataStepDef.successEditMetadata(imageFirstId, STATUS_ID.getName(), archiveStatusId);


        String imageSecondId = importServiceStepDef.importImageWithCopyMetadata(imageData, true);
        Response responseSecondFile = metadataStepDef.checkMetadata(imageSecondId);

        assertAll(
                () -> compareParameters(imageData.getFilename(), getValueFromResponse(responseSecondFile, FILENAME.getPath()), "filename"),
                () -> compareParameters(statuses.stream().filter(item -> item.name().equals(Status.NEW.getName())).
                        map(DictionariesItem::id).findFirst().orElse(null), getValueFromResponse(responseSecondFile, STATUS_ID.getPath() + ".id"), "status_id"),
                () -> compareParameters(archiveStatusId, getValueFromResponse(metadataStepDef.checkMetadata(imageFirstId), STATUS_ID.getPath() + ".id"), "status_id"),
                () -> mustBeNullParameter(getValueFromResponse(responseSecondFile, MASTER_CATEGORY_ID.getPath()), "category_id"),
                () -> mustBeNullParameter(getValueFromResponse(responseSecondFile, SOURCE_ID.getPath()), "source_id"),
                () -> mustBeNullParameter(getValueFromResponse(responseSecondFile, ASSIGNEE_ID.getPath()), "assignee_id"),
                () -> mustBeNullParameter(getValueFromResponse(responseSecondFile, PRIORITY.getPath()), "priority"),
                () -> mustBeNullParameter(getValueFromResponse(responseSecondFile, EXTERNAL_TASK_ID.getPath()), "external_task_id"),
                () -> mustBeNullParameter(getValueFromResponse(responseSecondFile, MASTER_SELLER.getPath()), "master_seller_id"),
                () -> mustBeNullParameter(getValueFromResponse(responseSecondFile, SKU.getPath()), "sku"),
                () -> mustBeNullParameter(getValueFromResponse(responseSecondFile, IS_OWN_TRADEMARK.getPath()), "is_own_trademark"),
                () -> mustBeNullParameter(getValueFromResponse(responseSecondFile, IS_COPYRIGHT.getPath()), "is_copyright"),
                () -> mustBeNullParameter(getValueFromResponse(responseSecondFile, DESCRIPTION.getPath()), "description"),
                () -> mustBeNullParameter(getValueFromResponse(responseSecondFile, KEYWORDS.getPath()), "keywords")
        );
    }

    @Test(description = "Успешный импорт - первый файл Удаленный - copy_metadata=true", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"291493"})
    public void successImportImageWithCopyMetadataFirstFileDeleteStatusTest() {
        String deleteStatusId = statuses.stream().filter(item -> item.name().equals(Status.DELETE.getName())).
                map(DictionariesItem::id).findFirst().orElse(null);

        ImageData imageData = new ImageData(ImageFormat.JPEG).setFilename(RandomStringUtils.randomAlphabetic(6) + "." + ImageFormat.JPEG.getFormatName());
        String imageFirstId = importServiceStepDef.importImageWithCopyMetadata(imageData, false);
        metadataStepDef.successEditMetadata(imageFirstId, editValues);
        metadataStepDef.successEditMetadata(imageFirstId, STATUS_ID.getName(), deleteStatusId);

        String imageSecondId = importServiceStepDef.importImageWithCopyMetadata(imageData, true);
        Response responseSecondFile = metadataStepDef.checkMetadata(imageSecondId);

        assertAll(
                () -> compareParameters(imageData.getFilename(), getValueFromResponse(responseSecondFile, FILENAME.getPath()), "filename"),
                () -> compareParameters(statuses.stream().filter(item -> item.name().equals(Status.NEW.getName())).
                        map(DictionariesItem::id).findFirst().orElse(null), getValueFromResponse(responseSecondFile, STATUS_ID.getPath() + ".id"), "status_id"),
                () -> compareParameters(deleteStatusId, getValueFromResponse(metadataStepDef.checkMetadata(imageFirstId), STATUS_ID.getPath() + ".id"), "status_id"),
                () -> mustBeNullParameter(getValueFromResponse(responseSecondFile, MASTER_CATEGORY_ID.getPath()), "category_id"),
                () -> mustBeNullParameter(getValueFromResponse(responseSecondFile, SOURCE_ID.getPath()), "source_id"),
                () -> mustBeNullParameter(getValueFromResponse(responseSecondFile, ASSIGNEE_ID.getPath()), "assignee_id"),
                () -> mustBeNullParameter(getValueFromResponse(responseSecondFile, PRIORITY.getPath()), "priority"),
                () -> mustBeNullParameter(getValueFromResponse(responseSecondFile, EXTERNAL_TASK_ID.getPath()), "external_task_id"),
                () -> mustBeNullParameter(getValueFromResponse(responseSecondFile, MASTER_SELLER.getPath()), "master_seller_id"),
                () -> mustBeNullParameter(getValueFromResponse(responseSecondFile, SKU.getPath()), "sku"),
                () -> mustBeNullParameter(getValueFromResponse(responseSecondFile, IS_OWN_TRADEMARK.getPath()), "is_own_trademark"),
                () -> mustBeNullParameter(getValueFromResponse(responseSecondFile, IS_COPYRIGHT.getPath()), "is_copyright"),
                () -> mustBeNullParameter(getValueFromResponse(responseSecondFile, DESCRIPTION.getPath()), "description"),
                () -> mustBeNullParameter(getValueFromResponse(responseSecondFile, KEYWORDS.getPath()), "keywords")
        );
    }

    @Test(description = "Успешный импорт - первый файл Актуальный - copy_metadata=true", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"291489"})
    public void successImportImageWithCopyMetadataFirstFileActualStatusTest() {
        String actualStatusId = statuses.stream().filter(item -> item.name().equals(Status.ACTUAL.getName())).
                map(DictionariesItem::id).findFirst().orElse(null);

        ImageData imageData = new ImageData(ImageFormat.JPEG).setFilename(RandomStringUtils.randomAlphabetic(6) + "." + ImageFormat.JPEG.getFormatName());
        String imageFirstId = importServiceStepDef.importImageWithCopyMetadata(imageData, false);
        metadataStepDef.successEditMetadata(imageFirstId, editValues);
        metadataStepDef.successEditMetadata(imageFirstId, STATUS_ID.getName(), actualStatusId);

        String imageSecondId = importServiceStepDef.importImageWithCopyMetadata(imageData.setFilename(String.format(FILENAME_MASK, editValues.get(SKU.getName()), editValues.get(PRIORITY.getName()), ImageFormat.JPEG.getFormatName())), true);
        Response responseSecondFile = metadataStepDef.checkMetadata(imageSecondId);

        assertAll(
                () -> compareParameters(imageData.getFilename(), getValueFromResponse(responseSecondFile, FILENAME.getPath()), "filename"),
                () -> compareParameters(getValueFromResponse(metadataStepDef.checkMetadata(imageFirstId), ORIGIN_FILENAME.getPath()), getValueFromResponse(responseSecondFile, ORIGIN_FILENAME.getPath()), "origin_filename"),
                () -> compareParameters(statuses.stream().filter(item -> item.name().equals(Status.NEW.getName())).
                        map(DictionariesItem::id).findFirst().orElse(null), getValueFromResponse(responseSecondFile, STATUS_ID.getPath() + ".id"), "status_id"),
                () -> compareParameters(actualStatusId, getValueFromResponse(metadataStepDef.checkMetadata(imageFirstId), STATUS_ID.getPath() + ".id"), "status_id"),
                () -> compareParameters(editValues.get(MASTER_CATEGORY_ID.getName()), getValueFromResponse(responseSecondFile, MASTER_CATEGORY_ID.getPath() + ".id"), "category_id"),
                () -> compareParameters(editValues.get(SOURCE_ID.getName()), getValueFromResponse(responseSecondFile, SOURCE_ID.getPath() + ".id"), "source_id"),
                () -> compareParameters(editValues.get(ASSIGNEE_ID.getName()), getValueFromResponse(responseSecondFile, ASSIGNEE_ID.getPath() + ".id"), "assignee_id"),
                () -> compareParameters(editValues.get(PRIORITY.getName()), getValueFromResponse(responseSecondFile, PRIORITY.getPath()), "priority"),
                () -> compareParameters(editValues.get(EXTERNAL_TASK_ID.getName()), getValueFromResponse(responseSecondFile, EXTERNAL_TASK_ID.getPath()), "external_task_id"),
                () -> compareParameters(editValues.get(MASTER_SELLER_ID.getName()), getValueFromResponse(responseSecondFile, MASTER_SELLER.getPath() + ".id"), "master_seller_id"),
                () -> compareParameters(editValues.get(SKU.getName()), getValueFromResponse(responseSecondFile, SKU.getPath()), "sku"),
                () -> compareParameters(editValues.get(IS_OWN_TRADEMARK.getName()), getValueFromResponse(responseSecondFile, IS_OWN_TRADEMARK.getPath()), "is_own_trademark"),
                () -> compareParameters(editValues.get(IS_COPYRIGHT.getName()), getValueFromResponse(responseSecondFile, IS_COPYRIGHT.getPath()), "is_copyright"),
                () -> compareParameters(editValues.get(DESCRIPTION.getName()), getValueFromResponse(responseSecondFile, DESCRIPTION.getPath()), "description"),
                () -> compareParameters(editValues.get(KEYWORDS.getName()), getValueFromResponse(responseSecondFile, KEYWORDS.getPath()), "keywords")
        );
    }

    @Test(description = "Успешный импорт - copy_metadata=false", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"291505"})
    public void successImportImageWithCopyMetadataFalseTest() {
        ImageData imageData = new ImageData(ImageFormat.JPEG).setFilename(RandomStringUtils.randomAlphabetic(6) + "." + ImageFormat.JPEG.getFormatName());
        String imageFirstId = importServiceStepDef.importImageWithCopyMetadata(imageData, false);
        metadataStepDef.successEditMetadata(imageFirstId, editValues);

        String imageSecondId = importServiceStepDef.importImageWithCopyMetadata(imageData, false);
        Response responseSecondFile = metadataStepDef.checkMetadata(imageSecondId);

        assertAll(
                () -> compareParameters(imageData.getFilename(), getValueFromResponse(responseSecondFile, FILENAME.getPath()), "filename"),
                () -> compareParameters(statuses.stream().filter(item -> item.name().equals(Status.NEW.getName())).
                        map(DictionariesItem::id).findFirst().orElse(null), getValueFromResponse(responseSecondFile, STATUS_ID.getPath() + ".id"), "status_id"),
                () -> compareParameters(statuses.stream().filter(item -> item.name().equals(Status.NEW.getName())).
                        map(DictionariesItem::id).findFirst().orElse(null), getValueFromResponse(metadataStepDef.checkMetadata(imageFirstId), STATUS_ID.getPath() + ".id"), "status_id"),
                () -> mustBeNullParameter(getValueFromResponse(responseSecondFile, MASTER_CATEGORY_ID.getPath()), "category_id"),
                () -> mustBeNullParameter(getValueFromResponse(responseSecondFile, SOURCE_ID.getPath()), "source_id"),
                () -> mustBeNullParameter(getValueFromResponse(responseSecondFile, ASSIGNEE_ID.getPath()), "assignee_id"),
                () -> mustBeNullParameter(getValueFromResponse(responseSecondFile, PRIORITY.getPath()), "priority"),
                () -> mustBeNullParameter(getValueFromResponse(responseSecondFile, EXTERNAL_TASK_ID.getPath()), "external_task_id"),
                () -> mustBeNullParameter(getValueFromResponse(responseSecondFile, MASTER_SELLER.getPath()), "master_seller_id"),
                () -> mustBeNullParameter(getValueFromResponse(responseSecondFile, SKU.getPath()), "sku"),
                () -> mustBeNullParameter(getValueFromResponse(responseSecondFile, IS_OWN_TRADEMARK.getPath()), "is_own_trademark"),
                () -> mustBeNullParameter(getValueFromResponse(responseSecondFile, IS_COPYRIGHT.getPath()), "is_copyright"),
                () -> mustBeNullParameter(getValueFromResponse(responseSecondFile, DESCRIPTION.getPath()), "description"),
                () -> mustBeNullParameter(getValueFromResponse(responseSecondFile, KEYWORDS.getPath()), "keywords")
        );
    }

    @Test(description = "Успешный импорт - два файла в статусе Новый в системе - copy_metadata=true", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"291492"})
    public void successImportImageWithCopyMetadataTwoFilesNewStatusTest() {
        ImageData imageData = new ImageData(ImageFormat.JPEG).setFilename(RandomStringUtils.randomAlphabetic(6) + "." + ImageFormat.JPEG.getFormatName());
        String imageFirstId = importServiceStepDef.importImageWithCopyMetadata(imageData, false);
        metadataStepDef.successEditMetadata(imageFirstId, editValues);
        String imageSecondId = importServiceStepDef.importImageWithCopyMetadata(imageData, false);

        String imageThirdId = importServiceStepDef.importImageWithCopyMetadata(imageData, true);
        Response responseThirdFile = metadataStepDef.checkMetadata(imageThirdId);

        assertAll(
                () -> compareParameters(imageData.getFilename(), getValueFromResponse(responseThirdFile, FILENAME.getPath()), "filename"),
                () -> compareParameters(statuses.stream().filter(item -> item.name().equals(Status.NEW.getName())).
                        map(DictionariesItem::id).findFirst().orElse(null), getValueFromResponse(metadataStepDef.checkMetadata(imageFirstId), STATUS_ID.getPath() + ".id"), "status_id"),
                () -> compareParameters(statuses.stream().filter(item -> item.name().equals(Status.ARCHIVE.getName())).
                        map(DictionariesItem::id).findFirst().orElse(null), getValueFromResponse(metadataStepDef.checkMetadata(imageSecondId), STATUS_ID.getPath() + ".id"), "status_id"),
                () -> compareParameters(statuses.stream().filter(item -> item.name().equals(Status.NEW.getName())).
                        map(DictionariesItem::id).findFirst().orElse(null), getValueFromResponse(responseThirdFile, STATUS_ID.getPath() + ".id"), "status_id"),
                () -> mustBeNullParameter(getValueFromResponse(responseThirdFile, MASTER_CATEGORY_ID.getPath()), "category_id"),
                () -> mustBeNullParameter(getValueFromResponse(responseThirdFile, SOURCE_ID.getPath()), "source_id"),
                () -> mustBeNullParameter(getValueFromResponse(responseThirdFile, ASSIGNEE_ID.getPath()), "assignee_id"),
                () -> mustBeNullParameter(getValueFromResponse(responseThirdFile, PRIORITY.getPath()), "priority"),
                () -> mustBeNullParameter(getValueFromResponse(responseThirdFile, EXTERNAL_TASK_ID.getPath()), "external_task_id"),
                () -> mustBeNullParameter(getValueFromResponse(responseThirdFile, MASTER_SELLER.getPath()), "master_seller_id"),
                () -> mustBeNullParameter(getValueFromResponse(responseThirdFile, SKU.getPath()), "sku"),
                () -> mustBeNullParameter(getValueFromResponse(responseThirdFile, IS_OWN_TRADEMARK.getPath()), "is_own_trademark"),
                () -> mustBeNullParameter(getValueFromResponse(responseThirdFile, IS_COPYRIGHT.getPath()), "is_copyright"),
                () -> mustBeNullParameter(getValueFromResponse(responseThirdFile, DESCRIPTION.getPath()), "description"),
                () -> mustBeNullParameter(getValueFromResponse(responseThirdFile, KEYWORDS.getPath()), "keywords")
        );
    }

        @Test(description = "Успешный импорт - external_offer_id copy_metadata=true", timeOut = 600000, groups = {"regress"})
        @WorkItemIds({"264274"})
        public void successImportImageWithCopyMetadataExternalOfferIdTest() {
            ImageData image = new ImageData(ImageFormat.JPEG);
            byte[] bytes = getRandomByteImage(NINETY_NINE, NINETY_NINE, image.getFormat().getFormatName());
            String fileBase64 = new String(Base64.getEncoder().encode(bytes));
            RequestExternalImport requestExternalImport = importServiceStepDef.buildEditRequest(
                    fileBase64, image.getFilename(), OffsetDateTime.now().minusDays(1).format(ISO_INSTANT));

            Response responseOriginalFile =  importServiceStepDef.externalImportImage(requestExternalImport, getData().baseExternalUrl(), externalToken);
            ImageData imageData = new ImageData(ImageFormat.JPEG).setFilename(getValueFromResponse(responseOriginalFile, FILENAME.getPath()).toString().replace(ImageFormat.JPEG.getFormatName(), ImageFormat.JPG.getFormatName()));
            String imageNewId = importServiceStepDef.importImageWithCopyMetadata(imageData, true);
            Response responseNewFile = metadataStepDef.checkMetadata(imageNewId);
            ExternalImportData externalImportData = requestExternalImport.externalImportData();

                assertAll(
                        () -> compareParameters(externalImportData.externalOfferId(), getValueFromResponse(responseNewFile, EXTERNAL_OFFER.getPath() + ".id"), "external_offer.id"),
                        () -> compareParameters(externalImportData.externalOfferName(), getValueFromResponse(responseNewFile, EXTERNAL_OFFER.getPath() + ".name"), "external_offer.name"),
                        () -> compareParameters(
                                externalImportData.readyForRetouchAt().substring(0, 19),
                                getValueFromResponse(responseNewFile, READY_FOR_RETOUCH_AT.getPath()).toString().substring(0, 19),
                                "ready_for_retouch_at")
                );
        }

    @Test(description = "Успешный импорт - совместно с business-metadata и parse_filename=true - copy_metadata=true", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"291517"})
    public void successImportImageWithCopyMetadataAndBusinessMetadataAndParseFilenameTest() {
        String newSKU = RandomStringUtils.randomAlphabetic(6);
        Integer newPriority = new Random().nextInt(NINETY_NINE) + 1;
        String filename = String.format(FILENAME_MASK, newSKU, newPriority, ImageFormat.JPEG.getFormatName());
        ImageData imageData = new ImageData(ImageFormat.JPEG).setFilename(filename);
        String imageFirstId = importServiceStepDef.importImageWithCopyMetadata(imageData, false);
        metadataStepDef.successEditMetadata(imageFirstId, editValues);

        Map<String, Object> metadata = new HashMap<>() {{
            put(MASTER_CATEGORY_ID.getName(), metadataStepDef.getListCategoriesMetadata().get(1).id());
            put(SOURCE_ID.getName(), metadataStepDef.getListSourcesMetadata().get(1).id());
            put(KEYWORDS.getName(), Collections.singletonList(RandomStringUtils.randomAlphabetic(6)));
            put(OWN_TRADEMARK.getName(), false);
            put(QUALITY_ID.getName(), metadataStepDef.getListQualitiesMetadata().get(1).id());
            put(EXTERNAL_TASK_ID.getName(), String.valueOf(new Random().nextInt(NINETY_NINE) + 1));
        }};

        String metadataId = importServiceStepDef.successImportBusinessMetadata(Collections.singletonMap("data", metadata));

        List<String> keywords = new ArrayList<>();
        keywords.addAll((Collection<? extends String>) editValues.get(KEYWORDS.getName()));
        keywords.addAll((Collection<? extends String>) metadata.get(KEYWORDS.getName()));

        String imageSecondId = importServiceStepDef.importImageWithCopyMetadataAndBusinessMetadata(imageData.setFilename(filename), true,true, metadataId);
        Response responseSecondFile = metadataStepDef.checkMetadata(imageSecondId);

        assertAll(
                () -> compareParameters(imageData.getFilename(), getValueFromResponse(responseSecondFile, FILENAME.getPath()), "filename"),
                () -> compareParameters(getValueFromResponse(metadataStepDef.checkMetadata(imageFirstId), ORIGIN_FILENAME.getPath()), getValueFromResponse(responseSecondFile, ORIGIN_FILENAME.getPath()), "origin_filename"),
                () -> compareParameters(statuses.stream().filter(item -> item.name().equals(Status.NEW.getName())).
                        map(DictionariesItem::id).findFirst().orElse(null), getValueFromResponse(responseSecondFile, STATUS_ID.getPath() + ".id"), "status_id"),
                () -> compareParameters(statuses.stream().filter(item -> item.name().equals(Status.ARCHIVE.getName())).
                        map(DictionariesItem::id).findFirst().orElse(null), getValueFromResponse(metadataStepDef.checkMetadata(imageFirstId), STATUS_ID.getPath() + ".id"), "status_id"),
                () -> compareParameters(metadata.get(MASTER_CATEGORY_ID.getName()), getValueFromResponse(responseSecondFile, MASTER_CATEGORY_ID.getPath() + ".id"), "category_id"),
                () -> compareParameters(metadata.get(SOURCE_ID.getName()), getValueFromResponse(responseSecondFile, SOURCE_ID.getPath() + ".id"), "source_id"),
                () -> compareParameters(editValues.get(ASSIGNEE_ID.getName()), getValueFromResponse(responseSecondFile, ASSIGNEE_ID.getPath() + ".id"), "assignee_id"),
                () -> compareParameters(newPriority, getValueFromResponse(responseSecondFile, PRIORITY.getPath()), "priority"),
                () -> compareParameters(metadata.get(EXTERNAL_TASK_ID.getName()), getValueFromResponse(responseSecondFile, EXTERNAL_TASK_ID.getPath()), "external_task_id"),
                () -> compareParameters(editValues.get(MASTER_SELLER_ID.getName()), getValueFromResponse(responseSecondFile, MASTER_SELLER.getPath() + ".id"), "master_seller_id"),
                () -> compareParameters(newSKU, getValueFromResponse(responseSecondFile, SKU.getPath()), "sku"),
                () -> equalsFalseParameter(getValueFromResponse(responseSecondFile, IS_OWN_TRADEMARK.getPath()), "is_own_trademark"),
                () -> compareParameters(editValues.get(IS_COPYRIGHT.getName()), getValueFromResponse(responseSecondFile, IS_COPYRIGHT.getPath()), "is_copyright"),
                () -> compareParameters(editValues.get(DESCRIPTION.getName()), getValueFromResponse(responseSecondFile, DESCRIPTION.getPath()), "description"),
                () -> compareParameters(keywords, getValueFromResponse(responseSecondFile, KEYWORDS.getPath()), "keywords"),
                () -> compareParameters(metadata.get(QUALITY_ID.getName()), getValueFromResponse(responseSecondFile, QUALITY_ID.getPath() + ".id"), "quality_id")
        );
    }

    @Test(description = "Успешный импорт - совместно с business-metadata - copy_metadata=true", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"291512"})
    public void successImportImageWithCopyMetadataAndBusinessMetadataTest() {
        ImageData imageData = new ImageData(ImageFormat.JPEG).setFilename(RandomStringUtils.randomAlphabetic(6) + "." + ImageFormat.JPEG.getFormatName());
        String imageFirstId = importServiceStepDef.importImageWithCopyMetadata(imageData, false);
        metadataStepDef.successEditMetadata(imageFirstId, editValues);

        Map<String, Object> metadata = new HashMap<>() {{
            put(MASTER_CATEGORY_ID.getName(), metadataStepDef.getListCategoriesMetadata().get(1).id());
            put(SOURCE_ID.getName(), metadataStepDef.getListSourcesMetadata().get(1).id());
            put(KEYWORDS.getName(), Collections.singletonList(RandomStringUtils.randomAlphabetic(6)));
            put(OWN_TRADEMARK.getName(), false);
            put(QUALITY_ID.getName(), metadataStepDef.getListQualitiesMetadata().get(1).id());
            put(EXTERNAL_TASK_ID.getName(), String.valueOf(new Random().nextInt(NINETY_NINE) + 1));
        }};

        String metadataId = importServiceStepDef.successImportBusinessMetadata(Collections.singletonMap("data", metadata));

        List<String> keywords = new ArrayList<>();
        keywords.addAll((Collection<? extends String>) editValues.get(KEYWORDS.getName()));
        keywords.addAll((Collection<? extends String>) metadata.get(KEYWORDS.getName()));

        String imageSecondId = importServiceStepDef.importImageWithCopyMetadataAndBusinessMetadata(imageData, true,false, metadataId);
        Response responseSecondFile = metadataStepDef.checkMetadata(imageSecondId);

        assertAll(
                () -> compareParameters(imageData.getFilename(), getValueFromResponse(responseSecondFile, FILENAME.getPath()), "filename"),
                () -> compareParameters(getValueFromResponse(metadataStepDef.checkMetadata(imageFirstId), ORIGIN_FILENAME.getPath()), getValueFromResponse(responseSecondFile, ORIGIN_FILENAME.getPath()), "origin_filename"),
                () -> compareParameters(statuses.stream().filter(item -> item.name().equals(Status.NEW.getName())).
                        map(DictionariesItem::id).findFirst().orElse(null), getValueFromResponse(responseSecondFile, STATUS_ID.getPath() + ".id"), "status_id"),
                () -> compareParameters(statuses.stream().filter(item -> item.name().equals(Status.ARCHIVE.getName())).
                        map(DictionariesItem::id).findFirst().orElse(null), getValueFromResponse(metadataStepDef.checkMetadata(imageFirstId), STATUS_ID.getPath() + ".id"), "status_id"),
                () -> compareParameters(metadata.get(MASTER_CATEGORY_ID.getName()), getValueFromResponse(responseSecondFile, MASTER_CATEGORY_ID.getPath() + ".id"), "category_id"),
                () -> compareParameters(metadata.get(SOURCE_ID.getName()), getValueFromResponse(responseSecondFile, SOURCE_ID.getPath() + ".id"), "source_id"),
                () -> compareParameters(editValues.get(ASSIGNEE_ID.getName()), getValueFromResponse(responseSecondFile, ASSIGNEE_ID.getPath() + ".id"), "assignee_id"),
                () -> compareParameters(editValues.get(PRIORITY.getName()), getValueFromResponse(responseSecondFile, PRIORITY.getPath()), "priority"),
                () -> compareParameters(metadata.get(EXTERNAL_TASK_ID.getName()), getValueFromResponse(responseSecondFile, EXTERNAL_TASK_ID.getPath()), "external_task_id"),
                () -> compareParameters(editValues.get(MASTER_SELLER_ID.getName()), getValueFromResponse(responseSecondFile, MASTER_SELLER.getPath() + ".id"), "master_seller_id"),
                () -> compareParameters(editValues.get(SKU.getName()), getValueFromResponse(responseSecondFile, SKU.getPath()), "sku"),
                () -> equalsFalseParameter(getValueFromResponse(responseSecondFile, IS_OWN_TRADEMARK.getPath()), "is_own_trademark"),
                () -> compareParameters(editValues.get(IS_COPYRIGHT.getName()), getValueFromResponse(responseSecondFile, IS_COPYRIGHT.getPath()), "is_copyright"),
                () -> compareParameters(editValues.get(DESCRIPTION.getName()), getValueFromResponse(responseSecondFile, DESCRIPTION.getPath()), "description"),
                () -> compareParameters(keywords, getValueFromResponse(responseSecondFile, KEYWORDS.getPath()), "keywords"),
                () -> compareParameters(metadata.get(QUALITY_ID.getName()), getValueFromResponse(responseSecondFile, QUALITY_ID.getPath() + ".id"), "quality_id")
        );
    }

    @Test(description = "Успешный импорт - совместно с parse_filename=true - copy_metadata=true", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"291516"})
    public void successImportImageWithCopyMetadataAndParseFilenameTest() {
        String newSKU = RandomStringUtils.randomAlphabetic(6);
        Integer newPriority = new Random().nextInt(NINETY_NINE) + 1;
        String filename = String.format(FILENAME_MASK, newSKU, newPriority, ImageFormat.JPEG.getFormatName());
        ImageData imageData = new ImageData(ImageFormat.JPEG).setFilename(filename);
        String imageFirstId = importServiceStepDef.importImageWithCopyMetadata(imageData, false);
        metadataStepDef.successEditMetadata(imageFirstId, editValues);

        String imageSecondId = importServiceStepDef.importImageWithCopyMetadataAndBusinessMetadata(imageData.setFilename(filename), true,true, null);
        Response responseSecondFile = metadataStepDef.checkMetadata(imageSecondId);

        assertAll(
                () -> compareParameters(newPriority, getValueFromResponse(responseSecondFile, PRIORITY.getPath()), "priority"),
                () -> compareParameters(newSKU, getValueFromResponse(responseSecondFile, SKU.getPath()), "sku")
        );
    }
}

