package ru.spice.at.common.utils.auth;

import io.restassured.http.Cookies;

/**
 * Интерфейс для абстракции процесса авторизации
 */
public interface AuthUtils {
    Object login();

    Cookies getLoginCookies();

    String getUrl();

    void logout();
}
