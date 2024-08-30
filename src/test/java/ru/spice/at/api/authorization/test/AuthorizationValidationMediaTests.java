package ru.spice.at.api.authorization.test;

import io.qameta.allure.Feature;
import io.qameta.allure.Issue;
import io.qameta.allure.Story;
import ru.testit.annotations.WorkItemIds;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import ru.spice.at.api.authorization.AuthorizationSettings;
import ru.spice.at.api.dto.response.import_service.DataItem;
import ru.spice.at.api.import_service.ImportServiceStepDef;
import ru.spice.at.common.base_test.BaseApiTest;
import ru.spice.at.common.dto.dam.ImageData;
import ru.spice.at.common.emuns.ApiServices;
import ru.spice.at.common.emuns.dam.ImageFormat;
import ru.spice.at.common.utils.Assert;

import java.util.List;

import static ru.spice.at.common.emuns.Role.*;

@Feature("Authorization")
@Story("Validation media")
@Deprecated
public class AuthorizationValidationMediaTests extends BaseApiTest<AuthorizationSettings> {
    private final ImportServiceStepDef importServiceStepDef;

    protected AuthorizationValidationMediaTests() {
        super(ApiServices.AUTHORIZATION);
        importServiceStepDef = new ImportServiceStepDef();
    }

    @BeforeMethod(alwaysRun = true)
    public void beforeMethod() {
        importServiceStepDef.setRole(ADMINISTRATOR);
        importServiceStepDef.deleteValidations();

        importServiceStepDef.importInvalidRandomImages(new ImageData(ImageFormat.INVALID));
    }

    @AfterMethod(alwaysRun = true)
    public void afterMethod() {
        importServiceStepDef.deleteValidations();
    }

    //@Test(description = "Успешный просмотр ошибок валидации - роль Administrator", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"227037"})
    public void successAdministratorValidationsTest() {
        importServiceStepDef.setRole(PHOTOPRODUCTION);
        importServiceStepDef.importInvalidRandomImages(new ImageData(ImageFormat.INVALID));

        importServiceStepDef.setRole(CONTENT_PRODUCTION_OUTSOURCE);
        importServiceStepDef.importInvalidRandomImages(new ImageData(ImageFormat.INVALID));

        importServiceStepDef.setRole(ADMINISTRATOR);
        List<DataItem> actualDataItems = importServiceStepDef.getValidationsDataItems(null);
        Assert.compareParameters(3, actualDataItems.size(), "количество элементов списка ошибок");
    }

    //@Test(description = "Успешный просмотр ошибок валидации - роль Photoproduction", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"227038"})
    public void successPhotoproductionValidationsTest() {
        importServiceStepDef.setRole(PHOTOPRODUCTION);
        List<DataItem> actualDataItems = importServiceStepDef.getValidationsDataItems(null);
        Assert.compareParameters(1, actualDataItems.size(), "количество элементов списка ошибок");
    }

    //@Test(description = "Успешный просмотр ошибок валидации - роль Content Production", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"227039"})
    public void successContentProductionValidationsTest() {
        importServiceStepDef.setRole(CONTENT_PRODUCTION);
        List<DataItem> actualDataItems = importServiceStepDef.getValidationsDataItems(null);
        Assert.compareParameters(1, actualDataItems.size(), "количество элементов списка ошибок");
    }

    @Issue("SPC-904")
    //@Test(description = "Успешный просмотр ошибок валидации - роль Content Support", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"227040"})
    public void successContentSupportValidationsTest() {
        importServiceStepDef.setRole(CONTENT_SUPPORT);
        importServiceStepDef.importInvalidRandomImages(new ImageData(ImageFormat.INVALID));

        List<DataItem> actualDataItems = importServiceStepDef.getValidationsDataItems(null);
        Assert.compareParameters(1, actualDataItems.size(), "количество элементов списка ошибок");
    }

    //@Test(description = "Успешный просмотр ошибок валидации - роль Photoproduction Outsource", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"227041"})
    public void successPhotoproductionOutsourceValidationsTest() {
        importServiceStepDef.setRole(PHOTOPRODUCTION_OUTSOURCE);
        importServiceStepDef.importInvalidRandomImages(new ImageData(ImageFormat.INVALID));

        List<DataItem> actualDataItems = importServiceStepDef.getValidationsDataItems(null);
        Assert.compareParameters(1, actualDataItems.size(), "количество элементов списка ошибок");
    }

    //@Test(description = "Успешный просмотр ошибок валидации - роль Content Production Outsource", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"227042"})
    public void successContentProductionOutsourceValidationsTest() {
        importServiceStepDef.setRole(CONTENT_PRODUCTION_OUTSOURCE);
        importServiceStepDef.importInvalidRandomImages(new ImageData(ImageFormat.INVALID));

        List<DataItem> actualDataItems = importServiceStepDef.getValidationsDataItems(null);
        Assert.compareParameters(1, actualDataItems.size(), "количество элементов списка ошибок");
    }

    @Issue("SPC-904")
    //@Test(description = "Неуспешный просмотр ошибок валидации - роль Content Support", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"227043"})
    public void unsuccessfulContentSupportValidationsTest() {
        importServiceStepDef.setRole(CONTENT_SUPPORT);
        List<DataItem> actualDataItems = importServiceStepDef.getValidationsDataItems(null);
        Assert.mustBeEmptyList(actualDataItems, "количество элементов списка ошибок");
    }

    //@Test(description = "Неуспешный просмотр ошибок валидации - роль Photoproduction Outsource", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"227044"})
    public void unsuccessfulPhotoproductionOutsourceValidationsTest() {
        importServiceStepDef.setRole(PHOTOPRODUCTION_OUTSOURCE);
        List<DataItem> actualDataItems = importServiceStepDef.getValidationsDataItems(null);
        Assert.mustBeEmptyList(actualDataItems, "количество элементов списка ошибок");
    }

    //@Test(description = "Неуспешный просмотр ошибок валидации - роль Content Production Outsource", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"227045"})
    public void unsuccessfulContentProductionOutsourceValidationsTest() {
        importServiceStepDef.setRole(CONTENT_PRODUCTION_OUTSOURCE);
        List<DataItem> actualDataItems = importServiceStepDef.getValidationsDataItems(null);
        Assert.mustBeEmptyList(actualDataItems, "количество элементов списка ошибок");
    }
}
