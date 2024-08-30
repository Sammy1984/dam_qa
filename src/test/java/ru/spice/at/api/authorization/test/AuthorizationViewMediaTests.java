package ru.spice.at.api.authorization.test;

import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import ru.testit.annotations.WorkItemIds;
import org.testng.annotations.*;
import ru.spice.at.api.authorization.AuthorizationSettings;
import ru.spice.at.api.import_service.ImportServiceStepDef;
import ru.spice.at.api.metadata_service.MetadataStepDef;
import ru.spice.at.common.base_test.BaseApiTest;
import ru.spice.at.common.dto.dam.ImageData;
import ru.spice.at.common.emuns.ApiServices;
import ru.spice.at.common.emuns.dam.ImageFormat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static ru.spice.at.common.emuns.Role.*;

@Feature("Authorization")
@Story("View media")
public class AuthorizationViewMediaTests extends BaseApiTest<AuthorizationSettings> {
    private final ImportServiceStepDef importServiceStepDef;
    private final MetadataStepDef metadataStepDef;
    private final List<String> administratorIds = new ArrayList<>();

    protected AuthorizationViewMediaTests() {
        super(ApiServices.AUTHORIZATION);
        importServiceStepDef = new ImportServiceStepDef();
        metadataStepDef = new MetadataStepDef();
    }

    @BeforeClass(alwaysRun = true)
    public void beforeClass() {
        metadataStepDef.deleteMetadata();
        importServiceStepDef.deleteMedia();
        Arrays.stream(values()).forEach(role -> {
                    importServiceStepDef.setRole(role);
                    administratorIds.add(importServiceStepDef.importRandomImages(new ImageData().setFormat(ImageFormat.JPEG)));
                }
        );
    }

    @AfterClass(alwaysRun = true)
    public void afterClass() {
        metadataStepDef.deleteMetadata();
    }

    @Test(description = "Успешный просмотр медиа - роль Administrator", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"226822"})
    public void successAdministratorViewTest() {
        metadataStepDef.setRole(ADMINISTRATOR);
        metadataStepDef.getMediaFilesMetadata(administratorIds.size());
    }

    @Test(description = "Успешный просмотр медиа - роль Photoproduction", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"226824"})
    public void successPhotoproductionViewTest() {
        metadataStepDef.setRole(PHOTOPRODUCTION);
        metadataStepDef.getMediaFilesMetadata(administratorIds.size());
    }

    @Test(description = "Успешный просмотр медиа - роль Content Production", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"226825"})
    public void successContentProductionViewTest() {
        metadataStepDef.setRole(CONTENT_PRODUCTION);
        metadataStepDef.getMediaFilesMetadata(administratorIds.size());
    }

    @Test(description = "Успешный просмотр медиа - роль Content Support", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"226826"})
    public void successContentSupportViewTest() {
        metadataStepDef.setRole(CONTENT_SUPPORT);
        metadataStepDef.getMediaFilesMetadata(administratorIds.size());
    }

    @Test(description = "Успешный просмотр медиа - роль Photoproduction Outsource", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"226829"})
    public void successPhotoproductionOutsourceViewTest() {
        metadataStepDef.setRole(PHOTOPRODUCTION_OUTSOURCE);
        metadataStepDef.getMediaFilesMetadata(1);
    }

    @Test(description = "Успешный просмотр медиа - роль Content Production Outsource", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"226830"})
    public void successContentProductionOutsourceViewTest() {
        metadataStepDef.setRole(CONTENT_PRODUCTION_OUTSOURCE);
        metadataStepDef.getMediaFilesMetadata(1);
    }
}
