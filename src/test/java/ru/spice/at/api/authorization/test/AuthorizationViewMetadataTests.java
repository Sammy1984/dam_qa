package ru.spice.at.api.authorization.test;

import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import ru.testit.annotations.WorkItemIds;
import org.testng.annotations.Test;
import ru.spice.at.api.authorization.AuthorizationSettings;
import ru.spice.at.api.import_service.ImportServiceStepDef;
import ru.spice.at.api.metadata_service.MetadataStepDef;
import ru.spice.at.common.base_test.BaseApiTest;
import ru.spice.at.common.dto.dam.ImageData;
import ru.spice.at.common.emuns.ApiServices;
import ru.spice.at.common.emuns.dam.ImageFormat;

import static ru.spice.at.common.emuns.Role.*;

@Feature("Authorization")
@Story("View metadata")
public class AuthorizationViewMetadataTests extends BaseApiTest<AuthorizationSettings> {
    private final ImportServiceStepDef importServiceStepDef;
    private final MetadataStepDef metadataStepDef;

    protected AuthorizationViewMetadataTests() {
        super(ApiServices.AUTHORIZATION);
        importServiceStepDef = new ImportServiceStepDef();
        metadataStepDef = new MetadataStepDef();
    }

    //todo выключено для использования параллельного сьюта
    //@AfterClass(alwaysRun = true)
    public void afterClass() {
        metadataStepDef.deleteMetadata();
        importServiceStepDef.deleteMedia();
    }

    @Test(description = "Успешный просмотр метадаты - роль Administrator", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"226925"})
    public void successAdministratorViewMetadataTest() {
        importServiceStepDef.setRole(CONTENT_SUPPORT);
        String id = importServiceStepDef.importRandomImages(new ImageData().setFormat(ImageFormat.JPEG));

        metadataStepDef.setRole(ADMINISTRATOR);
        metadataStepDef.checkMetadata(id);
    }

    @Test(description = "Успешный просмотр метадаты - роль Photoproduction", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"226927"})
    public void successPhotoproductionViewMetadataTest() {
        importServiceStepDef.setRole(PHOTOPRODUCTION);
        String id = importServiceStepDef.importRandomImages(new ImageData().setFormat(ImageFormat.JPEG));

        metadataStepDef.setRole(PHOTOPRODUCTION);
        metadataStepDef.checkMetadata(id);
    }

    @Test(description = "Успешный просмотр метадаты - роль Content Production", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"226928"})
    public void successContentProductionViewMetadataTest() {
        importServiceStepDef.setRole(CONTENT_PRODUCTION);
        String id = importServiceStepDef.importRandomImages(new ImageData().setFormat(ImageFormat.JPEG));

        metadataStepDef.setRole(CONTENT_PRODUCTION);
        metadataStepDef.checkMetadata(id);
    }

    @Test(description = "Успешный просмотр метадаты - роль Content Support", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"226929"})
    public void successContentSupportViewMetadataTest() {
        importServiceStepDef.setRole(CONTENT_SUPPORT);
        String id = importServiceStepDef.importRandomImages(new ImageData().setFormat(ImageFormat.JPEG));

        metadataStepDef.setRole(CONTENT_SUPPORT);
        metadataStepDef.checkMetadata(id);
    }

    @Test(description = "Успешный просмотр метадаты - роль Photoproduction Outsource", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"226930"})
    public void successPhotoproductionOutsourceViewMetadataTest() {
        importServiceStepDef.setRole(PHOTOPRODUCTION_OUTSOURCE);
        String id = importServiceStepDef.importRandomImages(new ImageData().setFormat(ImageFormat.JPEG));

        metadataStepDef.setRole(PHOTOPRODUCTION_OUTSOURCE);
        metadataStepDef.checkMetadata(id);
    }

    @Test(description = "Успешный просмотр метадаты - роль Content Production Outsource", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"226932"})
    public void successContentProductionOutsourceViewMetadataTest() {
        importServiceStepDef.setRole(CONTENT_PRODUCTION_OUTSOURCE);
        String id = importServiceStepDef.importRandomImages(new ImageData().setFormat(ImageFormat.JPEG));

        metadataStepDef.setRole(CONTENT_PRODUCTION_OUTSOURCE);
        metadataStepDef.checkMetadata(id);
    }

    @Test(description = "Неуспешный просмотр метадаты - роль Photoproduction Outsource", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"226935"})
    public void unsuccessfulPhotoproductionOutsourceViewMetadataTest() {
        importServiceStepDef.setRole(ADMINISTRATOR);
        String id = importServiceStepDef.importRandomImages(new ImageData().setFormat(ImageFormat.JPEG));

        metadataStepDef.setRole(PHOTOPRODUCTION_OUTSOURCE);
        metadataStepDef.unsuccessfulCheckMetadata(id);
    }

    @Test(description = "Неуспешный просмотр метадаты - роль Content Production Outsource", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"226933"})
    public void unsuccessfulContentProductionOutsourceViewMetadataTest() {
        importServiceStepDef.setRole(ADMINISTRATOR);
        String id = importServiceStepDef.importRandomImages(new ImageData().setFormat(ImageFormat.JPEG));

        metadataStepDef.setRole(PHOTOPRODUCTION_OUTSOURCE);
        metadataStepDef.unsuccessfulCheckMetadata(id);
    }
}