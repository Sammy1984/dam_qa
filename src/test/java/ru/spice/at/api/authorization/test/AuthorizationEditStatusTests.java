package ru.spice.at.api.authorization.test;

import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import ru.testit.annotations.WorkItemIds;
import io.restassured.response.Response;
import org.apache.commons.lang3.RandomStringUtils;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.spice.at.api.authorization.AuthorizationSettings;
import ru.spice.at.api.dto.response.metadata.DictionariesItem;
import ru.spice.at.api.dto.response.metadata.UsersItem;
import ru.spice.at.api.import_service.ImportServiceStepDef;
import ru.spice.at.api.metadata_service.MetadataStepDef;
import ru.spice.at.common.base_test.AbstractApiStepDef;
import ru.spice.at.common.base_test.BaseApiTest;
import ru.spice.at.common.dto.User;
import ru.spice.at.common.dto.dam.ImageData;
import ru.spice.at.common.emuns.ApiServices;
import ru.spice.at.common.emuns.dam.ImageFormat;
import ru.spice.at.common.utils.Assert;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

import static ru.spice.at.common.constants.TestConstants.NINETY_NINE;
import static ru.spice.at.common.emuns.Role.*;
import static ru.spice.at.common.emuns.dam.ImageParameters.*;
import static ru.spice.at.common.emuns.dam.Status.*;
import static ru.spice.at.common.utils.JsonHelper.getValueFromResponse;

@Feature("Authorization")
@Story("Edit status")
public class AuthorizationEditStatusTests extends BaseApiTest<AuthorizationSettings> {
    private final ImportServiceStepDef importServiceStepDef;
    private final MetadataStepDef metadataStepDef;

    private List<DictionariesItem> statusIds;

    protected AuthorizationEditStatusTests() {
        super(ApiServices.AUTHORIZATION);
        importServiceStepDef = new ImportServiceStepDef();
        metadataStepDef = new MetadataStepDef();
    }

    @BeforeClass(alwaysRun = true)
    public void beforeClass() {
        statusIds = metadataStepDef.getListStatusesMetadata();
    }

    //todo выключено для использования параллельного сьюта
    //@AfterClass(alwaysRun = true)
    public void afterClass() {
        metadataStepDef.deleteMetadata();
        importServiceStepDef.deleteMedia();
    }

    @Test(description = "Успешное редактирование статуса - роль Administrator", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"227009"})
    public void successAdministratorEditStatusTest() {
        importServiceStepDef.setRole(CONTENT_PRODUCTION_OUTSOURCE);
        String id = importServiceStepDef.importRandomImages(new ImageData().setFormat(ImageFormat.JPEG));

        metadataStepDef.setRole(ADMINISTRATOR);
        String statusId = statusIds.stream().map(DictionariesItem::id).collect(Collectors.toList()).get(new Random().nextInt(statusIds.size() - 1));

        Map<String, Object> editValues = new HashMap<>() {{
            put(SKU.getName(), RandomStringUtils.randomAlphabetic(5));
            put(PRIORITY.getName(), new Random().nextInt(NINETY_NINE) + 1);
            put(STATUS_ID.getName(), statusId);
        }};

        Response response = metadataStepDef.successEditMetadata(id, editValues);
        Assert.compareParameters(statusId, getValueFromResponse(response, STATUS_ID.getPath() + ".id"), "status_id");
    }

    @Test(description = "Успешное редактирование статуса - роль Photoproduction", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"227010"})
    public void successPhotoproductionEditStatusTest() {
        importServiceStepDef.setRole(PHOTOPRODUCTION);
        String id = importServiceStepDef.importRandomImages(new ImageData().setFormat(ImageFormat.JPEG));

        metadataStepDef.setRole(PHOTOPRODUCTION);
        String statusId = statusIds.stream().map(DictionariesItem::id).collect(Collectors.toList()).get(new Random().nextInt(statusIds.size() - 1));

        Map<String, Object> editValues = new HashMap<>() {{
            put(SKU.getName(), RandomStringUtils.randomAlphabetic(5));
            put(PRIORITY.getName(), new Random().nextInt(NINETY_NINE) + 1);
            put(STATUS_ID.getName(), statusId);
        }};

        Response response = metadataStepDef.successEditMetadata(id, editValues);
        Assert.compareParameters(statusId, getValueFromResponse(response, STATUS_ID.getPath() + ".id"), "status_id");
    }

    @Test(description = "Успешное редактирование статуса - роль Content Production", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"227011"})
    public void successContentProductionEditStatusTest() {
        importServiceStepDef.setRole(CONTENT_PRODUCTION);
        String id = importServiceStepDef.importRandomImages(new ImageData().setFormat(ImageFormat.JPEG));

        metadataStepDef.setRole(CONTENT_PRODUCTION);
        String statusId = statusIds.stream().map(DictionariesItem::id).collect(Collectors.toList()).get(new Random().nextInt(statusIds.size() - 1));

        Map<String, Object> editValues = new HashMap<>() {{
            put(SKU.getName(), RandomStringUtils.randomAlphabetic(5));
            put(PRIORITY.getName(), new Random().nextInt(NINETY_NINE) + 1);
            put(STATUS_ID.getName(), statusId);
        }};

        Response response = metadataStepDef.successEditMetadata(id, editValues);
        Assert.compareParameters(statusId, getValueFromResponse(response, STATUS_ID.getPath() + ".id"), "status_id");
    }

    @Test(description = "Успешное редактирование статуса - роль Content Support", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"227012"})
    public void successContentSupportEditStatusTest() {
        importServiceStepDef.setRole(CONTENT_SUPPORT);
        String id = importServiceStepDef.importRandomImages(new ImageData().setFormat(ImageFormat.JPEG));

        metadataStepDef.setRole(CONTENT_SUPPORT);
        String statusId = statusIds.stream().filter(status -> !status.name().equals(ACTUAL.getName()) & !status.name().equals(NEW.getName())).map(DictionariesItem::id).
                collect(Collectors.toList()).get(new Random().nextInt(statusIds.size() - 2));
        Response response = metadataStepDef.successEditMetadata(id, STATUS_ID.getName(), statusId);
        Assert.compareParameters(statusId, getValueFromResponse(response, STATUS_ID.getPath() + ".id"), "status_id");
    }

    @Test(description = "Успешное редактирование статуса - роль Photoproduction Outsource", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"227014"})
    public void successPhotoproductionOutsourceEditStatusTest() {
        importServiceStepDef.setRole(PHOTOPRODUCTION_OUTSOURCE);
        String id = importServiceStepDef.importRandomImages(new ImageData().setFormat(ImageFormat.JPEG));

        metadataStepDef.setRole(PHOTOPRODUCTION_OUTSOURCE);
        String statusId = statusIds.stream().filter(status -> status.name().equals(IN_PROGRESS.getName())).
                map(DictionariesItem::id).
                collect(Collectors.toList()).get(new Random().nextInt(1));
        Response response = metadataStepDef.successEditMetadata(id, STATUS_ID.getName(), statusId);
        Assert.compareParameters(statusId, getValueFromResponse(response, STATUS_ID.getPath() + ".id"), "status_id");
    }

    @Test(description = "Успешное редактирование статуса - роль Photoproduction Outsource (с назначением)", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"227014"})
    public void successPhotoproductionOutsourceAssigneeEditStatusTest() {
        importServiceStepDef.setRole(ADMINISTRATOR);
        String id = importServiceStepDef.importRandomImages(new ImageData().setFormat(ImageFormat.JPEG));

        metadataStepDef.setRole(ADMINISTRATOR);
        String name = AbstractApiStepDef.standProperties.getUsers().stream().
                filter(user -> user.role().equals(PHOTOPRODUCTION_OUTSOURCE)).
                map(User::name).findFirst().orElse(null);
        String assigneeId = metadataStepDef.getListUsersMetadata(PHOTOPRODUCTION_OUTSOURCE.getFullName()).stream().
                filter(user -> user.fullName().equals(name)).
                map(UsersItem::id).findFirst().orElseThrow(() -> new RuntimeException("Пользователь не найден"));
        Assert.notNullOrEmptyParameter(name, "id Фотопродакшена Аутсорс");

        metadataStepDef.successEditMetadata(id, ASSIGNEE_ID.getName(), assigneeId);

        metadataStepDef.setRole(PHOTOPRODUCTION_OUTSOURCE);
        String statusId = statusIds.stream().filter(status -> status.name().equals(IN_PROGRESS.getName())).
                map(DictionariesItem::id).
                collect(Collectors.toList()).get(new Random().nextInt(1));
        Response response = metadataStepDef.successEditMetadata(id, STATUS_ID.getName(), statusId);
        Assert.compareParameters(statusId, getValueFromResponse(response, STATUS_ID.getPath() + ".id"), "status_id");
    }

    @Test(description = "Неуспешное редактирование статуса - роль Content Support", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"227015"})
    public void unsuccessfulContentSupportEditStatusTest() {
        importServiceStepDef.setRole(CONTENT_SUPPORT);
        String id = importServiceStepDef.importRandomImages(new ImageData().setFormat(ImageFormat.JPEG));

        metadataStepDef.setRole(CONTENT_SUPPORT);
        String statusId = statusIds.stream().filter(status -> status.name().equals(ACTUAL.getName())).map(DictionariesItem::id).
                findFirst().orElse(null);

        metadataStepDef.unsuccessfulForbiddenEditMetadata(id, STATUS_ID.getName(), statusId);
    }

    @Test(description = "Неуспешное редактирование статуса - роль Photoproduction Outsource", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"227016"})
    public void unsuccessfulPhotoproductionOutsourceEditStatusTest() {
        importServiceStepDef.setRole(PHOTOPRODUCTION_OUTSOURCE);
        String id = importServiceStepDef.importRandomImages(new ImageData().setFormat(ImageFormat.JPEG));

        metadataStepDef.setRole(PHOTOPRODUCTION_OUTSOURCE);
        String statusId = statusIds.stream().filter(status -> !status.name().equals(NEW.getName()) && !status.name().equals(IN_PROGRESS.getName())).
                map(DictionariesItem::id).
                collect(Collectors.toList()).get(new Random().nextInt(statusIds.size() - 3));
        metadataStepDef.unsuccessfulForbiddenEditMetadata(id, STATUS_ID.getName(), statusId);
    }

    @Test(description = "Неуспешное редактирование статуса - роль Content Production Outsource", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"227017"})
    public void unsuccessfulContentProductionOutsourceEditStatusTest() {
        importServiceStepDef.setRole(CONTENT_PRODUCTION_OUTSOURCE);
        String id = importServiceStepDef.importRandomImages(new ImageData().setFormat(ImageFormat.JPEG));

        metadataStepDef.setRole(CONTENT_PRODUCTION_OUTSOURCE);
        String statusId = statusIds.stream().map(DictionariesItem::id).
                collect(Collectors.toList()).get(new Random().nextInt(statusIds.size() - 1));
        metadataStepDef.unsuccessfulForbiddenEditMetadata(id, STATUS_ID.getName(), statusId);
    }

    @Test(description = "Неуспешное редактирование статуса - роль Photoproduction Outsource  (недоступный файл)", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"227306"})
    public void unsuccessfulPhotoproductionOutsourceEditAdminStatusTest() {
        importServiceStepDef.setRole(ADMINISTRATOR);
        String id = importServiceStepDef.importRandomImages(new ImageData().setFormat(ImageFormat.JPEG));

        metadataStepDef.setRole(PHOTOPRODUCTION_OUTSOURCE);
        String statusId = statusIds.stream().filter(status -> status.name().equals(NEW.getName()) || status.name().equals(IN_PROGRESS.getName())).
                map(DictionariesItem::id).
                collect(Collectors.toList()).get(new Random().nextInt(1));
        metadataStepDef.unsuccessfulForbiddenEditMetadata(id, STATUS_ID.getName(), statusId);
    }
}
