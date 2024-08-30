package ru.spice.at.api.authorization;

import io.qameta.allure.Step;
import io.restassured.response.Response;
import ru.spice.at.api.dto.response.authorization.PermissionsResponse;
import ru.spice.at.api.dto.response.metadata.UsersItem;
import ru.spice.at.common.base_test.AbstractApiStepDef;
import ru.spice.at.common.dto.User;
import ru.spice.at.common.emuns.ApiServices;
import ru.spice.at.common.emuns.Role;

import java.util.List;

import static ru.spice.at.api.urls.AuthorizationUrls.PERMISSIONS;
import static ru.spice.at.api.utils.ApiUtils.*;
import static ru.spice.at.common.utils.JsonHelper.jsonParse;

public class AuthorizationStepDef extends AbstractApiStepDef {

    public AuthorizationStepDef() {
        super(ApiServices.AUTHORIZATION);
    }

    @Step("Получаем список исполнителей разрешений")
    public PermissionsResponse getPermissions() {
        Response response = sendGet(baseUrl + PERMISSIONS, getAuthToken());
        checkResponse(response);
        return jsonParse(response, PermissionsResponse.class);
    }

    public String getAssigneeId(List<UsersItem> users, Role role) {
        String username = standProperties.getUsers().stream().
                filter(user -> user.role().equals(role)).map(User::name).findFirst().orElse(null);
        return users.stream().filter(user -> user.fullName().equals(username)).map(UsersItem::id).findFirst().orElseThrow(() -> new RuntimeException("Пользователь не найден"));
    }
}
