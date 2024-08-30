package ru.spice.at.api.authorization.test;

import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import ru.testit.annotations.WorkItemIds;
import org.testng.annotations.Test;
import ru.spice.at.api.authorization.AuthorizationSettings;
import ru.spice.at.api.authorization.AuthorizationStepDef;
import ru.spice.at.api.dto.response.authorization.PermissionsResponse;
import ru.spice.at.common.base_test.BaseApiTest;
import ru.spice.at.common.emuns.ApiServices;
import ru.spice.at.common.utils.Assert;

import static ru.spice.at.common.emuns.Role.*;

@Feature("Authorization")
@Story("GET permissions")
public class AuthorizationPermissionsTests extends BaseApiTest<AuthorizationSettings> {
    private final AuthorizationStepDef authorizationStepDef;

    protected AuthorizationPermissionsTests() {
        super(ApiServices.AUTHORIZATION);
        authorizationStepDef = new AuthorizationStepDef();
    }

    @Test(description = "Проверка разрешений - роль Administrator", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"226799"})
    public void checkAdministratorPermissionsTest() {
        authorizationStepDef.setRole(ADMINISTRATOR);
        PermissionsResponse permissions = authorizationStepDef.getPermissions();
        Assert.compareParameters(getData().administratorPermissions(), permissions, "разрешения");
    }

    @Test(description = "Проверка разрешений - роль Photoproduction", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"226801"})
    public void checkPhotoproductionPermissionsTest() {
        authorizationStepDef.setRole(PHOTOPRODUCTION);
        PermissionsResponse permissions = authorizationStepDef.getPermissions();
        Assert.compareParameters(getData().photoproductionPermissions(), permissions, "разрешения");
    }

    @Test(description = "Проверка разрешений - роль Content Production", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"226802"})
    public void checkContentProductionPermissionsTest() {
        authorizationStepDef.setRole(CONTENT_PRODUCTION);
        PermissionsResponse permissions = authorizationStepDef.getPermissions();
        Assert.compareParameters(getData().contentProductionPermissions(), permissions, "разрешения");
    }

    @Test(description = "Проверка разрешений - роль Content Support", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"226804"})
    public void checkContentSupportPermissionsTest() {
        authorizationStepDef.setRole(CONTENT_SUPPORT);
        PermissionsResponse permissions = authorizationStepDef.getPermissions();
        Assert.compareParameters(getData().contentSupportPermissions(), permissions, "разрешения");
    }

    @Test(description = "Проверка разрешений - роль Photoproduction Outsource", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"226805"})
    public void checkPhotoproductionOutsourcePermissionsTest() {
        authorizationStepDef.setRole(PHOTOPRODUCTION_OUTSOURCE);
        PermissionsResponse permissions = authorizationStepDef.getPermissions();
        Assert.compareParameters(getData().photoproductionOutsourcePermissions(), permissions, "разрешения");
    }

    @Test(description = "Проверка разрешений - роль Content Production Outsource", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"226807"})
    public void checkContentProductionOutsourcePermissionsTest() {
        authorizationStepDef.setRole(CONTENT_PRODUCTION_OUTSOURCE);
        PermissionsResponse permissions = authorizationStepDef.getPermissions();
        Assert.compareParameters(getData().contentProductionOutsourcePermissions(), permissions, "разрешения");
    }
}
