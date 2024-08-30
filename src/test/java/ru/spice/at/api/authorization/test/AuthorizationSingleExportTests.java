package ru.spice.at.api.authorization.test;

import io.qameta.allure.Feature;
import io.qameta.allure.Issue;
import io.qameta.allure.Story;
import ru.testit.annotations.WorkItemIds;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import ru.spice.at.api.authorization.AuthorizationSettings;
import ru.spice.at.api.dto.response.metadata.UsersItem;
import ru.spice.at.api.import_service.ImportServiceStepDef;
import ru.spice.at.api.export_service.ExportServiceStepDef;
import ru.spice.at.api.metadata_service.MetadataStepDef;
import ru.spice.at.common.base_test.AbstractApiStepDef;
import ru.spice.at.common.base_test.BaseApiTest;
import ru.spice.at.common.dto.User;
import ru.spice.at.common.dto.dam.ImageData;
import ru.spice.at.common.emuns.ApiServices;
import ru.spice.at.common.emuns.dam.ImageFormat;
import ru.spice.at.common.utils.Assert;

import static ru.spice.at.common.emuns.Role.*;
import static ru.spice.at.common.emuns.dam.ImageParameters.ASSIGNEE_ID;

//todo доработать негативные сценарии
@Feature("Authorization")
@Story("Export media (single)")
public class AuthorizationSingleExportTests extends BaseApiTest<AuthorizationSettings> {
    private final ImportServiceStepDef importServiceStepDef;
    private final MetadataStepDef metadataStepDef;
    private final ExportServiceStepDef exportServiceStepDef;
    private String administratorId;

    protected AuthorizationSingleExportTests() {
        super(ApiServices.AUTHORIZATION);
        importServiceStepDef = new ImportServiceStepDef();
        metadataStepDef = new MetadataStepDef();
        exportServiceStepDef = new ExportServiceStepDef();
    }

    @BeforeMethod(alwaysRun = true)
    public void beforeMethod() {
        metadataStepDef.deleteMetadata();
        importServiceStepDef.setRole(ADMINISTRATOR);
        administratorId = importServiceStepDef.importRandomImages(new ImageData().setFormat(ImageFormat.JPEG));
    }

    @AfterMethod(alwaysRun = true)
    public void afterMethod() {
        metadataStepDef.deleteMetadata();
    }

    @Test(description = "Успешный экспорт медиа - роль Administrator", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"226870"})
    public void successAdministratorExportTest() {
        importServiceStepDef.setRole(PHOTOPRODUCTION_OUTSOURCE);
        String photoproductionOutsourceId = importServiceStepDef.importRandomImages(new ImageData().setFormat(ImageFormat.JPEG));

        exportServiceStepDef.setRole(ADMINISTRATOR);
        Assert.notNullOrEmptyParameter(
                exportServiceStepDef.exportImage(photoproductionOutsourceId).length, "файлы в байтовом представлении");
    }

    @Test(description = "Успешный экспорт медиа - роль Photoproduction", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"226876"})
    public void successPhotoproductionExportTest() {
        exportServiceStepDef.setRole(PHOTOPRODUCTION);
        Assert.notNullOrEmptyParameter(
                exportServiceStepDef.exportImage(administratorId).length, "файлы в байтовом представлении");
    }

    @Test(description = "Успешный экспорт медиа - роль Content Production", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"226877"})
    public void successContentProductionExportTest() {
        exportServiceStepDef.setRole(CONTENT_PRODUCTION);
        Assert.notNullOrEmptyParameter(
                exportServiceStepDef.exportImage(administratorId).length, "файлы в байтовом представлении");
    }

    @Test(description = "Успешный экспорт медиа - роль Content Support", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"226878"})
    public void successContentSupportExportTest() {
        exportServiceStepDef.setRole(CONTENT_SUPPORT);
        Assert.notNullOrEmptyParameter(
                exportServiceStepDef.exportImage(administratorId).length, "файлы в байтовом представлении");
    }

    @Test(description = "Успешный экспорт медиа - роль Photoproduction Outsource", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"226890"})
    public void successPhotoproductionOutsourceExportTest() {
        String name = AbstractApiStepDef.standProperties.getUsers().stream().
                filter(user -> user.role().equals(PHOTOPRODUCTION_OUTSOURCE)).
                map(User::name).findFirst().orElse(null);
        String assigneeId = metadataStepDef.getListUsersMetadata(PHOTOPRODUCTION_OUTSOURCE.getFullName()).stream().
                filter(user -> user.fullName().equals(name)).
                map(UsersItem::id).findFirst().orElseThrow(() -> new RuntimeException("Пользователь не найден"));
        Assert.notNullOrEmptyParameter(name, "id Фотопродакшена Аутсорс");

        metadataStepDef.successEditMetadata(administratorId, ASSIGNEE_ID.getName(), assigneeId);

        exportServiceStepDef.setRole(PHOTOPRODUCTION_OUTSOURCE);
        Assert.notNullOrEmptyParameter(
                exportServiceStepDef.exportImage(administratorId).length, "файлы в байтовом представлении");
    }

    @Test(description = "Успешный экспорт медиа - роль Content Production Outsource", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"226892"})
    public void successContentProductionOutsourceExportTest() {
        String name = AbstractApiStepDef.standProperties.getUsers().stream().
                filter(user -> user.role().equals(CONTENT_PRODUCTION_OUTSOURCE)).
                map(User::name).findFirst().orElse(null);
        String assigneeId = metadataStepDef.getListUsersMetadata(CONTENT_PRODUCTION_OUTSOURCE.getFullName()).stream().
                filter(user -> user.fullName().equals(name)).
                map(UsersItem::id).findFirst().orElseThrow(() -> new RuntimeException("Пользователь не найден"));
        Assert.notNullOrEmptyParameter(name, "id Контент Продакшн Аутсорс");

        metadataStepDef.successEditMetadata(administratorId, ASSIGNEE_ID.getName(), assigneeId);

        exportServiceStepDef.setRole(CONTENT_PRODUCTION_OUTSOURCE);
        Assert.notNullOrEmptyParameter(
                exportServiceStepDef.exportImage(administratorId).length, "файлы в байтовом представлении");
    }

    @Issue("SPC-568")//todo включить после закрытия стори
    //@Test(description = "Неуспешный экспорт медиа - роль Photoproduction Outsource", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"226894"})
    public void unsuccessfulPhotoproductionOutsourceExportTest() {
        exportServiceStepDef.setRole(PHOTOPRODUCTION_OUTSOURCE);
        //todo переделать
        Assert.notNullOrEmptyParameter(
                exportServiceStepDef.exportImage(administratorId).length, "файлы в байтовом представлении");
    }

    @Issue("SPC-568")//todo включить после закрытия стори
    //@Test(description = "Неуспешный экспорт медиа - роль Content Production Outsource", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"226898"})
    public void unsuccessfulContentProductionOutsourceExportTest() {
        exportServiceStepDef.setRole(CONTENT_PRODUCTION_OUTSOURCE);
        //todo переделать
        Assert.notNullOrEmptyParameter(
                exportServiceStepDef.exportImage(administratorId).length, "файлы в байтовом представлении");
    }
}
