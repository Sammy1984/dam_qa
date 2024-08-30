package ru.spice.at.api.authorization.test;

import io.qameta.allure.Feature;
import io.qameta.allure.Issue;
import io.qameta.allure.Story;
import ru.testit.annotations.WorkItemIds;
import org.apache.commons.lang3.RandomStringUtils;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import ru.spice.at.api.authorization.AuthorizationSettings;
import ru.spice.at.api.dto.request.metadata.RequestMultiEditData;
import ru.spice.at.api.dto.response.metadata.UsersItem;
import ru.spice.at.api.export_service.ExportServiceStepDef;
import ru.spice.at.api.import_service.ImportServiceStepDef;
import ru.spice.at.api.metadata_service.MetadataStepDef;
import ru.spice.at.common.base_test.AbstractApiStepDef;
import ru.spice.at.common.base_test.BaseApiTest;
import ru.spice.at.common.dto.User;
import ru.spice.at.common.dto.dam.ImageData;
import ru.spice.at.common.emuns.ApiServices;
import ru.spice.at.common.emuns.dam.ImageFormat;
import ru.spice.at.common.utils.Assert;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static ru.spice.at.common.constants.TestConstants.ZIP_FORMAT;
import static ru.spice.at.common.emuns.Role.*;
import static ru.spice.at.common.emuns.Role.CONTENT_PRODUCTION_OUTSOURCE;
import static ru.spice.at.common.emuns.dam.ExportsStatus.DONE;

//todo доработать негативные сценарии
@Feature("Authorization")
@Story("Export media (multi)")
public class AuthorizationMultiExportTests extends BaseApiTest<AuthorizationSettings> {
    private final ImportServiceStepDef importServiceStepDef;
    private final MetadataStepDef metadataStepDef;
    private final ExportServiceStepDef exportServiceStepDef;
    private List<String> administratorIds;
    private String archiveName;

    protected AuthorizationMultiExportTests() {
        super(ApiServices.AUTHORIZATION);
        importServiceStepDef = new ImportServiceStepDef();
        metadataStepDef = new MetadataStepDef();
        exportServiceStepDef = new ExportServiceStepDef();
    }

    @BeforeMethod(alwaysRun = true)
    public void beforeMethod() {
        metadataStepDef.deleteMetadata();
        List<ImageData> imageDataList = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            imageDataList.add(new ImageData(ImageFormat.JPEG));
        }
        importServiceStepDef.setRole(ADMINISTRATOR);
        administratorIds = imageDataList.stream().map(importServiceStepDef::importRandomImages).collect(Collectors.toList());
        archiveName = RandomStringUtils.randomAlphabetic(10);
    }

    @AfterMethod(alwaysRun = true)
    public void afterMethod() {
        metadataStepDef.deleteMetadata();
    }

    @Test(description = "Успешный экспорт медиа - роль Administrator", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"226870"})
    public void successAdministratorExportTest() {
        importServiceStepDef.setRole(PHOTOPRODUCTION_OUTSOURCE);
        List<String> ids = Collections.singletonList(importServiceStepDef.importRandomImages(new ImageData().setFormat(ImageFormat.JPEG)));

        exportServiceStepDef.setRole(ADMINISTRATOR);
        administratorIds.addAll(ids);

        exportServiceStepDef.exportImage(administratorIds, archiveName);
        String storageUrl = exportServiceStepDef.successWaitExportsWithParameters(archiveName, DONE, administratorIds.size());
        Assert.contains(archiveName + ZIP_FORMAT, storageUrl, "название архива в ссылке");
    }

    @Test(description = "Успешный экспорт медиа - роль Photoproduction", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"226876"})
    public void successPhotoproductionExportTest() {
        exportServiceStepDef.setRole(PHOTOPRODUCTION);
        exportServiceStepDef.exportImage(administratorIds, archiveName);
        String storageUrl = exportServiceStepDef.successWaitExportsWithParameters(archiveName, DONE, administratorIds.size());
        Assert.contains(archiveName + ZIP_FORMAT, storageUrl, "название архива в ссылке");
    }

    @Test(description = "Успешный экспорт медиа - роль Content Production", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"226877"})
    public void successContentProductionExportTest() {
        exportServiceStepDef.setRole(CONTENT_PRODUCTION);
        exportServiceStepDef.exportImage(administratorIds, archiveName);
        String storageUrl = exportServiceStepDef.successWaitExportsWithParameters(archiveName, DONE, administratorIds.size());
        Assert.contains(archiveName + ZIP_FORMAT, storageUrl, "название архива в ссылке");
    }

    @Test(description = "Успешный экспорт медиа - роль Content Support", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"226878"})
    public void successContentSupportExportTest() {
        exportServiceStepDef.setRole(CONTENT_SUPPORT);
        exportServiceStepDef.exportImage(administratorIds, archiveName);
        String storageUrl = exportServiceStepDef.successWaitExportsWithParameters(archiveName, DONE, administratorIds.size());
        Assert.contains(archiveName + ZIP_FORMAT, storageUrl, "название архива в ссылке");
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

        RequestMultiEditData requestMultiEditData = RequestMultiEditData.builder()
                .ids(administratorIds).assigneeId(assigneeId).build();

        metadataStepDef.successMultiEditMetadata(requestMultiEditData);

        exportServiceStepDef.setRole(PHOTOPRODUCTION_OUTSOURCE);
        importServiceStepDef.setRole(PHOTOPRODUCTION_OUTSOURCE);
        List<String> ids = Collections.singletonList(importServiceStepDef.importRandomImages(new ImageData().setFormat(ImageFormat.JPEG)));
        administratorIds.addAll(ids);

        exportServiceStepDef.exportImage(administratorIds, archiveName);
        String storageUrl = exportServiceStepDef.successWaitExportsWithParameters(archiveName, DONE, administratorIds.size());
        Assert.contains(archiveName + ZIP_FORMAT, storageUrl, "название архива в ссылке");
    }

    @Test(description = "Успешный экспорт медиа - роль Content Production Outsource", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"226892"})
    public void successContentProductionOutsourceExportTest() {
        exportServiceStepDef.setRole(CONTENT_PRODUCTION_OUTSOURCE);
        importServiceStepDef.setRole(CONTENT_PRODUCTION_OUTSOURCE);
        List<String> contentProductionOutsourceIds = Arrays.asList(
                importServiceStepDef.importRandomImages(new ImageData().setFormat(ImageFormat.JPEG)),
                importServiceStepDef.importRandomImages(new ImageData().setFormat(ImageFormat.JPEG)));

        exportServiceStepDef.exportImage(contentProductionOutsourceIds, archiveName);
        String storageUrl = exportServiceStepDef.successWaitExportsWithParameters(archiveName, DONE, contentProductionOutsourceIds.size());
        Assert.contains(archiveName + ZIP_FORMAT, storageUrl, "название архива в ссылке");
    }

    @Issue("SPC-568")//todo включить после закрытия стори
    //@Test(description = "Неуспешный экспорт медиа - роль Photoproduction Outsource", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"226894"})
    public void unsuccessfulPhotoproductionOutsourceExportTest() {
        exportServiceStepDef.setRole(PHOTOPRODUCTION_OUTSOURCE);
        //todo переделать
        Assert.notNullOrEmptyParameter(exportServiceStepDef.exportImage(administratorIds), "файлы в байтовом представлении");
    }

    @Issue("SPC-568")//todo включить после закрытия стори
    //@Test(description = "Неуспешный экспорт медиа - роль Content Production Outsource", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"226898"})
    public void unsuccessfulContentProductionOutsourceExportTest() {
        exportServiceStepDef.setRole(CONTENT_PRODUCTION_OUTSOURCE);
        //todo переделать
        Assert.notNullOrEmptyParameter(exportServiceStepDef.exportImage(administratorIds), "файлы в байтовом представлении");
    }
}
