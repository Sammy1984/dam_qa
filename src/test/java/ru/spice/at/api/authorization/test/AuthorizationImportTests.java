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
@Story("Import media")
public class AuthorizationImportTests extends BaseApiTest<AuthorizationSettings> {
    private final ImportServiceStepDef importServiceStepDef;

    protected AuthorizationImportTests() {
        super(ApiServices.AUTHORIZATION);
        importServiceStepDef = new ImportServiceStepDef();
    }

    //todo выключено для использования параллельного сьюта
    //@AfterClass(alwaysRun = true)
    public void afterClass() {
        new MetadataStepDef().deleteMetadata();
        importServiceStepDef.deleteMedia();
    }

    @Test(description = "Успешный импорт - роль Administrator", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"226810"})
    public void successAdministratorImportTest() {
        importServiceStepDef.setRole(ADMINISTRATOR);
        importServiceStepDef.importRandomImages(new ImageData().setFormat(ImageFormat.JPEG));
    }

    @Test(description = "Успешный импорт - роль Photoproduction", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"226813"})
    public void successPhotoproductionImportTest() {
        importServiceStepDef.setRole(PHOTOPRODUCTION);
        importServiceStepDef.importRandomImages(new ImageData().setFormat(ImageFormat.JPEG));
    }

    @Test(description = "Успешный импорт - роль Content Production", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"226814"})
    public void successContentProductionImportTest() {
        importServiceStepDef.setRole(CONTENT_PRODUCTION);
        importServiceStepDef.importRandomImages(new ImageData().setFormat(ImageFormat.JPEG));
    }

    @Test(description = "Успешный импорт - роль Content Support", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"226815"})
    public void successContentSupportImportTest() {
        importServiceStepDef.setRole(CONTENT_SUPPORT);
        importServiceStepDef.importRandomImages(new ImageData().setFormat(ImageFormat.JPEG));
    }

    @Test(description = "Успешный импорт - роль Content Production Outsource", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"226816"})
    public void successContentProductionOutsourceImportTest() {
        importServiceStepDef.setRole(CONTENT_PRODUCTION_OUTSOURCE);
        importServiceStepDef.importRandomImages(new ImageData().setFormat(ImageFormat.JPEG));
    }

    @Test(description = "Успешный импорт - роль Photoproduction Outsource", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"226817"})
    public void successPhotoproductionOutsourceImportTest() {
        importServiceStepDef.setRole(PHOTOPRODUCTION_OUTSOURCE);
        importServiceStepDef.importRandomImages(new ImageData().setFormat(ImageFormat.JPEG));
    }
}
