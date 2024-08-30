package ru.spice.at.common.base_test;

import lombok.Getter;
import lombok.Setter;
import ru.spice.at.common.StandProperties;
import ru.spice.at.common.emuns.Role;
import ru.spice.at.common.utils.ResponseValueExtractable;

import io.restassured.http.Cookie;
import io.restassured.http.Cookies;

import java.util.*;

import static java.util.Collections.emptyMap;

/**
 * Базовый класс для шагов тестовых классов
 * @author Aleksandr Osokin
 */
abstract class AbstractStepDef implements ResponseValueExtractable {
    private Cookies sessionCookies = new Cookies();
    private Map<String, String> headers = emptyMap();

    @Setter
    @Getter
    private String authToken;

    @Getter
    private Role role;

    public static final StandProperties standProperties = new StandProperties();

    AbstractStepDef() {
        this.role = Role.ADMINISTRATOR;
    }

    public Cookies getCookies() {
        return sessionCookies;
    }

    protected void setCookies(Cookies cookies) {
        sessionCookies = cookies;
    }

    protected void joinCookies(Cookies cookies) {
        List<Cookie> oldCookie = new ArrayList<>(getCookies().asList());
        cookies.forEach(cookie -> {
            if (!cookie.getValue().isEmpty() && !oldCookie.contains(cookie)) {
                oldCookie.add(cookie);
            }
        });
        setCookies(new Cookies(oldCookie));
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    protected void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public void setRole(Role role) {
        this.role = role;
        authToken = null;
    }
}