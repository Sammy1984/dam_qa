package ru.spice.at.api.authorization.test;

import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import ru.spice.at.api.dto.response.metadata.DictionariesItem;
import ru.testit.annotations.WorkItemIds;
import io.restassured.response.Response;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.spice.at.api.authorization.AuthorizationSettings;
import ru.spice.at.api.import_service.ImportServiceStepDef;
import ru.spice.at.api.metadata_service.MetadataStepDef;
import ru.spice.at.common.base_test.BaseApiTest;
import ru.spice.at.common.dto.dam.ImageData;
import ru.spice.at.common.emuns.ApiServices;
import ru.spice.at.common.emuns.dam.ImageFormat;
import ru.spice.at.common.utils.JsonHelper;

import java.util.*;

import static ru.spice.at.common.constants.TestConstants.NINETY_NINE;
import static ru.spice.at.common.emuns.Role.*;
import static ru.spice.at.common.emuns.dam.ImageParameters.*;
import static ru.spice.at.common.emuns.dam.Status.*;
import static ru.spice.at.common.utils.Assert.*;
import static ru.spice.at.common.utils.JsonHelper.getValueFromResponse;

@Feature("Authorization")
@Story("Edit metadata")
public class AuthorizationEditMetadataTests extends BaseApiTest<AuthorizationSettings> {
    private final ImportServiceStepDef importServiceStepDef;
    private final MetadataStepDef metadataStepDef;

    private ImageData imageData;
    private String statusId;
    private String categoryId;
    private String qualityId;
    private String sourceId;
    private String assigneeId;
    private Integer priority;
    private String externalTaskId;
    private String masterSellerId;
    private String editRequest;

    protected AuthorizationEditMetadataTests() {
        super(ApiServices.AUTHORIZATION);
        importServiceStepDef = new ImportServiceStepDef();
        metadataStepDef = new MetadataStepDef();
    }

    @BeforeClass(alwaysRun = true)
    public void beforeClass() {
        imageData = new ImageData(ImageFormat.JPEG);
        statusId = metadataStepDef.getListStatusesMetadata().stream().
                filter(s -> !s.name().equals(ACTUAL.getName())).
                map(DictionariesItem::id).findFirst().orElseThrow(() -> new RuntimeException("Статус не найден"));
        categoryId = metadataStepDef.getListCategoriesMetadata().get(0).id();
        qualityId = metadataStepDef.getListQualitiesMetadata().get(0).id();
        sourceId = metadataStepDef.getListSourcesMetadata().get(0).id();
        assigneeId = metadataStepDef.getListUsersMetadata().get(0).id();
        priority = new Random().nextInt(NINETY_NINE) + 1;
        externalTaskId = String.valueOf(new Random().nextInt(NINETY_NINE) + 1);
        masterSellerId = metadataStepDef.getListRetailersMetadata().data().get(0).id();
        editRequest = metadataStepDef.buildEditRequest(imageData, statusId, categoryId, qualityId,
                sourceId, assigneeId, priority, externalTaskId, masterSellerId);
    }

    //todo выключено для использования параллельного сьюта
    //@AfterClass(alwaysRun = true)
    public void afterClass() {
        metadataStepDef.deleteMetadata();
        importServiceStepDef.deleteMedia();
    }

    @Test(description = "Успешное редактирование метадаты - роль Administrator", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"226951"})
    public void successAdministratorEditMetadataTest() {
        importServiceStepDef.setRole(PHOTOPRODUCTION);
        String id = importServiceStepDef.importRandomImages(new ImageData().setFormat(ImageFormat.JPEG));

        metadataStepDef.setRole(ADMINISTRATOR);
        Response response = metadataStepDef.successEditAllMetadata(id, editRequest);

        assertAll(
                () -> compareParameters(imageData.getFilename(), getValueFromResponse(response, FILENAME.getPath()), "filename"),
                () -> compareParameters(imageData.getSku(), getValueFromResponse(response, SKU.getPath()), "sku"),
                () -> compareParameters(statusId, getValueFromResponse(response, STATUS_ID.getPath() + ".id"), "status_id"),
                () -> compareParameters(categoryId, getValueFromResponse(response, MASTER_CATEGORY_ID.getPath() + ".id"), "category_id"),
                () -> compareParameters(qualityId, getValueFromResponse(response, QUALITY_ID.getPath() + ".id"), "quality_id"),
                () -> compareParameters(sourceId, getValueFromResponse(response, SOURCE_ID.getPath() + ".id"), "source_id"),
                () -> compareParameters(assigneeId, getValueFromResponse(response, ASSIGNEE_ID.getPath() + ".id"), "assignee_id"),
                () -> compareParameters(priority, getValueFromResponse(response, PRIORITY.getPath()), "priority"),
                () -> compareParameters(externalTaskId, getValueFromResponse(response, EXTERNAL_TASK_ID.getPath()), "external_task_id"),
                () -> compareParameters(masterSellerId, getValueFromResponse(response, MASTER_SELLER.getPath()+ ".id"), "master_seller_id")
        );
    }

    @Test(description = "Успешное редактирование метадаты - роль Photoproduction", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"226953"})
    public void successPhotoproductionEditMetadataTest() {
        importServiceStepDef.setRole(PHOTOPRODUCTION);
        String id = importServiceStepDef.importRandomImages(new ImageData().setFormat(ImageFormat.JPEG));

        metadataStepDef.setRole(PHOTOPRODUCTION);
        Map<String, Object> editRequestMap  = JsonHelper.getMapFromJsonString(editRequest);
        editRequestMap.remove(MASTER_SELLER_ID.getName());

        Response response = metadataStepDef.successEditAllMetadata(id, editRequestMap);

        assertAll(
                () -> compareParameters(imageData.getFilename(), getValueFromResponse(response, FILENAME.getPath()), "filename"),
                () -> compareParameters(imageData.getSku(), getValueFromResponse(response, SKU.getPath()), "sku"),
                () -> compareParameters(statusId, getValueFromResponse(response, STATUS_ID.getPath() + ".id"), "status_id"),
                () -> compareParameters(categoryId, getValueFromResponse(response, MASTER_CATEGORY_ID.getPath() + ".id"), "category_id"),
                () -> compareParameters(qualityId, getValueFromResponse(response, QUALITY_ID.getPath() + ".id"), "quality_id"),
                () -> compareParameters(sourceId, getValueFromResponse(response, SOURCE_ID.getPath() + ".id"), "source_id"),
                () -> compareParameters(assigneeId, getValueFromResponse(response, ASSIGNEE_ID.getPath() + ".id"), "assignee_id"),
                () -> compareParameters(priority, getValueFromResponse(response, PRIORITY.getPath()), "priority"),
                () -> compareParameters(externalTaskId, getValueFromResponse(response, EXTERNAL_TASK_ID.getPath()), "external_task_id")
        );
    }

    @Test(description = "Успешное редактирование метадаты - роль Content Production", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"226954"})
    public void successContentProductionEditMetadataTest() {
        importServiceStepDef.setRole(CONTENT_PRODUCTION);
        String id = importServiceStepDef.importRandomImages(new ImageData().setFormat(ImageFormat.JPEG));

        metadataStepDef.setRole(CONTENT_PRODUCTION);
        String editRequest = metadataStepDef.buildEditRequest(imageData, statusId, categoryId, qualityId,
                sourceId, assigneeId, priority, externalTaskId, masterSellerId);

        Map<String, Object> editRequestMap  = JsonHelper.getMapFromJsonString(editRequest);
        editRequestMap.remove(MASTER_SELLER_ID.getName());
        Response response = metadataStepDef.successEditAllMetadata(id, editRequestMap);

        assertAll(
                () -> compareParameters(imageData.getFilename(), getValueFromResponse(response, FILENAME.getPath()), "filename"),
                () -> compareParameters(imageData.getSku(), getValueFromResponse(response, SKU.getPath()), "sku"),
                () -> compareParameters(statusId, getValueFromResponse(response, STATUS_ID.getPath() + ".id"), "status_id"),
                () -> compareParameters(categoryId, getValueFromResponse(response, MASTER_CATEGORY_ID.getPath() + ".id"), "category_id"),
                () -> compareParameters(qualityId, getValueFromResponse(response, QUALITY_ID.getPath() + ".id"), "quality_id"),
                () -> compareParameters(sourceId, getValueFromResponse(response, SOURCE_ID.getPath() + ".id"), "source_id"),
                () -> compareParameters(assigneeId, getValueFromResponse(response, ASSIGNEE_ID.getPath() + ".id"), "assignee_id"),
                () -> compareParameters(priority, getValueFromResponse(response, PRIORITY.getPath()), "priority"),
                () -> compareParameters(externalTaskId, getValueFromResponse(response, EXTERNAL_TASK_ID.getPath()), "external_task_id")
        );
    }

    @Test(description = "Успешное редактирование метадаты - роль Content Support", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"226976"})
    public void successContentSupportEditMetadataTest() {
        importServiceStepDef.setRole(CONTENT_SUPPORT);
        String id = importServiceStepDef.importRandomImages(new ImageData().setFormat(ImageFormat.JPEG));

        metadataStepDef.setRole(CONTENT_SUPPORT);
        Map<String, Object> editValues = new HashMap<>() {{
            put(DESCRIPTION.getName(), getData().description());
            put(STATUS_ID.getName(), statusId);
            put(QUALITY_ID.getName(), qualityId);
            put(SOURCE_ID.getName(), sourceId);
            put(ASSIGNEE_ID.getName(), assigneeId);
            put(IS_RAW_IMAGE.getName(), false);
            put(KEYWORDS.getName(), getData().keywords());
            put(RECEIVED.getName(), getData().received());
            put(PRIORITY.getName(), priority);
            put(EXTERNAL_TASK_ID.getName(), externalTaskId);
        }};

        Response response = metadataStepDef.successEditMetadata(id, editValues);

        assertAll(
                () -> compareParameters(statusId, getValueFromResponse(response, STATUS_ID.getPath() + ".id"), "status_id"),
                () -> compareParameters(qualityId, getValueFromResponse(response, QUALITY_ID.getPath() + ".id"), "quality_id"),
                () -> compareParameters(sourceId, getValueFromResponse(response, SOURCE_ID.getPath() + ".id"), "source_id"),
                () -> compareParameters(assigneeId, getValueFromResponse(response, ASSIGNEE_ID.getPath() + ".id"), "assignee_id"),
                () -> compareParameters(priority, getValueFromResponse(response,  PRIORITY.getPath()), "priority"),
                () -> compareParameters(getData().description(), getValueFromResponse(response, DESCRIPTION.getPath()), "description"),
                () -> compareParameters(getData().keywords(), getValueFromResponse(response, KEYWORDS.getPath()), "keywords"),
                () -> compareParameters(getData().received(), getValueFromResponse(response, RECEIVED.getPath()), "received"),
                () -> equalsFalseParameter(getValueFromResponse(response, IS_RAW_IMAGE.getPath()), "is_raw_image"),
                () -> compareParameters(externalTaskId, getValueFromResponse(response, EXTERNAL_TASK_ID.getPath()), "external_task_id")
        );
    }

    @Test(description = "Успешное редактирование метадаты - роль Photoproduction Outsource", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"226977"})
    public void successPhotoproductionOutsourceEditMetadataTest() {
        importServiceStepDef.setRole(PHOTOPRODUCTION_OUTSOURCE);
        String id = importServiceStepDef.importRandomImages(new ImageData().setFormat(ImageFormat.JPEG));
        String statusIdPhotoproductionOutsorce = metadataStepDef.getListStatusesMetadata().stream().
                filter(s -> !s.name().equals(ACTUAL.getName()) && !s.name().equals(ARCHIVE.getName()) && !s.name().equals(DELETE.getName())).
                map(DictionariesItem::id).findFirst().orElseThrow(() -> new RuntimeException("Статус не найден"));

        metadataStepDef.setRole(PHOTOPRODUCTION_OUTSOURCE);
        Map<String, Object> editValues = new HashMap<>() {{
            put(DESCRIPTION.getName(), getData().description());
            put(KEYWORDS.getName(), getData().keywords());
            put(STATUS_ID.getName(), statusIdPhotoproductionOutsorce);
        }};

        Response response = metadataStepDef.successEditMetadata(id, editValues);

        assertAll(
                () -> compareParameters(statusIdPhotoproductionOutsorce, getValueFromResponse(response, STATUS_ID.getPath() + ".id"), "status_id"),
                () -> compareParameters(getData().description(), getValueFromResponse(response, DESCRIPTION.getPath()), "description"),
                () -> compareParameters(getData().keywords(), getValueFromResponse(response, KEYWORDS.getPath()), "keywords")
        );
    }

    @Test(description = "Успешное редактирование метадаты - роль Content Production Outsource", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"226978"})
    public void successContentProductionOutsourceEditMetadataTest() {
        importServiceStepDef.setRole(CONTENT_PRODUCTION_OUTSOURCE);
        String id = importServiceStepDef.importRandomImages(new ImageData().setFormat(ImageFormat.JPEG));

        metadataStepDef.setRole(CONTENT_PRODUCTION_OUTSOURCE);
        Map<String, Object> editValues = new HashMap<>() {{
            put(DESCRIPTION.getName(), getData().description());
            put(KEYWORDS.getName(), getData().keywords());
        }};

        Response response = metadataStepDef.successEditMetadata(id, editValues);

        assertAll(
                () -> compareParameters(getData().description(), getValueFromResponse(response, DESCRIPTION.getPath()), "description"),
                () -> compareParameters(getData().keywords(), getValueFromResponse(response, KEYWORDS.getPath()), "keywords")
        );
    }

    @Test(description = "Неуспешное редактирование метадаты - роль Photoproduction", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"268785"})
    public void unsuccessfulPhotoproductionEditMetadataTest() {
        importServiceStepDef.setRole(PHOTOPRODUCTION);
        String id = importServiceStepDef.importRandomImages(new ImageData().setFormat(ImageFormat.JPEG));

        metadataStepDef.setRole(PHOTOPRODUCTION);
        metadataStepDef.unsuccessfulForbiddenEditMetadata(id, editRequest);
    }

    @Test(description = "Неуспешное редактирование метадаты - роль Content Production", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"268786"})
    public void unsuccessfulContentProductionEditMetadataTest() {
        importServiceStepDef.setRole(CONTENT_PRODUCTION);
        String id = importServiceStepDef.importRandomImages(new ImageData().setFormat(ImageFormat.JPEG));

        metadataStepDef.setRole(CONTENT_PRODUCTION);
        metadataStepDef.unsuccessfulForbiddenEditMetadata(id, editRequest);
    }

    @Test(description = "Неуспешное редактирование метадаты - роль Content Support", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"226979"})
    public void unsuccessfulContentSupportEditMetadataTest() {
        importServiceStepDef.setRole(CONTENT_SUPPORT);
        String id = importServiceStepDef.importRandomImages(new ImageData().setFormat(ImageFormat.JPEG));

        metadataStepDef.setRole(CONTENT_SUPPORT);
        Map<String, Object> editValues = new HashMap<>() {{
            put(FILENAME.getName(), imageData.getFilename());
            put(SKU.getName(), imageData.getSku());
            put(MASTER_CATEGORY_ID.getName(), categoryId);
            put(IS_COPYRIGHT.getName(), true);
            put(MASTER_SELLER_ID.getName(), masterSellerId);
        }};

        editValues.forEach((k, v) -> metadataStepDef.unsuccessfulForbiddenEditMetadata(id, Collections.singletonMap(k, v)));
    }

    @Test(description = "Неуспешное редактирование метадаты - роль Content Support (несколько параметров)", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"226979"})
    public void unsuccessfulContentSupportSomeEditMetadataTest() {
        importServiceStepDef.setRole(CONTENT_SUPPORT);
        String id = importServiceStepDef.importRandomImages(new ImageData().setFormat(ImageFormat.JPEG));

        metadataStepDef.setRole(CONTENT_SUPPORT);
        Map<String, Object> editValues = new HashMap<String, Object>() {{
            put(DESCRIPTION.getName(), getData().description());
            put(MASTER_CATEGORY_ID.getName(), categoryId);
            put(IS_COPYRIGHT.getName(), true);
        }};

        metadataStepDef.unsuccessfulForbiddenEditMetadata(id, editValues);
    }

    @Test(description = "Неуспешное редактирование метадаты - роль Photoproduction Outsource", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"226980"})
    public void unsuccessfulPhotoproductionOutsourceEditMetadataTest() {
        importServiceStepDef.setRole(PHOTOPRODUCTION_OUTSOURCE);
        String id = importServiceStepDef.importRandomImages(new ImageData().setFormat(ImageFormat.JPEG));

        metadataStepDef.setRole(PHOTOPRODUCTION_OUTSOURCE);
        Map<String, Object> editValues = new HashMap<>() {{
            put(FILENAME.getName(), imageData.getFilename());
            put(SKU.getName(), imageData.getSku());
            put(MASTER_CATEGORY_ID.getName(), categoryId);
            put(SOURCE_ID.getName(), sourceId);
            put(ASSIGNEE_ID.getName(), assigneeId);
            put(PRIORITY.getName(), priority);
            put(EXTERNAL_TASK_ID.getName(), externalTaskId);
            put(MASTER_SELLER_ID.getName(), masterSellerId);
            put(RECEIVED.getName(), getData().received());
            put(IS_RAW_IMAGE.getName(), false);
        }};

        editValues.forEach((k, v) -> metadataStepDef.unsuccessfulForbiddenEditMetadata(id, Collections.singletonMap(k, v)));
    }

    @Test(description = "Неуспешное редактирование метадаты - роль Photoproduction Outsource (несколько параметров)", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"226980"})
    public void unsuccessfulPhotoproductionOutsourceSomeEditMetadataTest() {
        importServiceStepDef.setRole(PHOTOPRODUCTION_OUTSOURCE);
        String id = importServiceStepDef.importRandomImages(new ImageData().setFormat(ImageFormat.JPEG));

        metadataStepDef.setRole(PHOTOPRODUCTION_OUTSOURCE);
        Map<String, Object> editValues = new HashMap<>() {{
            put(KEYWORDS.getName(), getData().keywords());
            put(PRIORITY.getName(), priority);
            put(EXTERNAL_TASK_ID.getName(), externalTaskId);
        }};

        metadataStepDef.unsuccessfulForbiddenEditMetadata(id, editValues);
    }

    @Test(description = "Неуспешное редактирование метадаты - роль Photoproduction Outsource (недоступный файл)", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"268784"})
    public void unsuccessfulPhotoproductionOutsourceContentSupportAdminMetadataTest() {
        importServiceStepDef.setRole(CONTENT_SUPPORT);
        String id = importServiceStepDef.importRandomImages(new ImageData().setFormat(ImageFormat.JPEG));

        metadataStepDef.setRole(PHOTOPRODUCTION_OUTSOURCE);
        metadataStepDef.unsuccessfulForbiddenEditMetadata(id, Collections.singletonMap(DESCRIPTION.getName(), getData().description()));
    }

    @Test(description = "Неуспешное редактирование метадаты - роль Content Production Outsource", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"226981"})
    public void unsuccessfulContentProductionOutsourceEditMetadataTest() {
        importServiceStepDef.setRole(CONTENT_PRODUCTION_OUTSOURCE);
        String id = importServiceStepDef.importRandomImages(new ImageData().setFormat(ImageFormat.JPEG));

        metadataStepDef.setRole(CONTENT_PRODUCTION_OUTSOURCE);
        Map<String, Object> editValues = new HashMap<>() {{
            put(FILENAME.getName(), imageData.getFilename());
            put(SKU.getName(), imageData.getSku());
            put(MASTER_CATEGORY_ID.getName(), categoryId);
            put(QUALITY_ID.getName(), qualityId);
            put(SOURCE_ID.getName(), sourceId);
            put(ASSIGNEE_ID.getName(), assigneeId);
            put(PRIORITY.getName(), priority);
            put(EXTERNAL_TASK_ID.getName(), externalTaskId);
            put(MASTER_SELLER_ID.getName(), masterSellerId);
            put(RECEIVED.getName(), getData().received());
            put(IS_RAW_IMAGE.getName(), false);
            put(STATUS_ID.getName(), statusId);
        }};

        metadataStepDef.unsuccessfulForbiddenEditMetadata(id, editValues);
    }

    @Test(description = "Неуспешное редактирование метадаты - роль Content Production Outsource (несколько параметров)", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"226981"})
    public void unsuccessfulContentProductionOutsourceSomeEditMetadataTest() {
        importServiceStepDef.setRole(CONTENT_PRODUCTION_OUTSOURCE);
        String id = importServiceStepDef.importRandomImages(new ImageData().setFormat(ImageFormat.JPEG));

        metadataStepDef.setRole(CONTENT_PRODUCTION_OUTSOURCE);
        Map<String, Object> editValues = new HashMap<>() {{
            put(KEYWORDS.getName(), getData().keywords());
            put(PRIORITY.getName(), priority);
            put(EXTERNAL_TASK_ID.getName(), externalTaskId);
        }};

        metadataStepDef.unsuccessfulForbiddenEditMetadata(id, editValues);
    }

    @Test(description = "Неуспешное редактирование метадаты - роль Content Production Outsource (недоступный файл)", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"227288"})
    public void unsuccessfulContentProductionOutsourceEditAdminMetadataTest() {
        importServiceStepDef.setRole(ADMINISTRATOR);
        String id = importServiceStepDef.importRandomImages(new ImageData().setFormat(ImageFormat.JPEG));

        metadataStepDef.setRole(CONTENT_PRODUCTION_OUTSOURCE);
        Map<String, Object> editValues = new HashMap<>() {{
            put(DESCRIPTION.getName(), getData().description());
            put(KEYWORDS.getName(), getData().keywords());
        }};

        metadataStepDef.unsuccessfulForbiddenEditMetadata(id, editValues);
    }
}
