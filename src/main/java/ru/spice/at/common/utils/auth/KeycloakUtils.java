package ru.spice.at.common.utils.auth;

import io.restassured.http.Cookie;
import io.restassured.http.Cookies;
import io.restassured.response.Response;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.RandomStringUtils;
import ru.spice.at.common.StandProperties;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.apache.http.HttpStatus.SC_MOVED_TEMPORARILY;
import static ru.spice.at.api.utils.ApiUtils.*;
import static ru.spice.at.api.utils.UrlUtils.getSha256DecodeValue;
import static ru.spice.at.common.utils.Assert.*;
import static ru.spice.at.common.utils.JsonHelper.getValueFromResponse;

/**
 * Авторизация в Keycloak
 */
@Log4j2
public class KeycloakUtils implements AuthUtils {
    public static final String KEYCLOAK = "keycloak";

    private static final String CLIENT_ID = "spice-app-client";
    private static final String RESPONSE_MODE = "fragment";
    private static final String RESPONSE_TYPE = "code";
    private static final String SCOPE = "openid";
    private static final String CODE_CHALLENGE_METHOD = "S256";

    private static final StandProperties standProperties = new StandProperties();

    private final String keycloakUrl;
    private final String username;
    private final String password;

    private Cookies cookies;

    public KeycloakUtils(String keycloakUrl, String username, String password) {
        this.keycloakUrl = keycloakUrl;
        this.username = username;
        this.password = password;
    }

    public Response keycloakLogin() {
        return keycloakLogin(username, password);
    }

    /**
     * Получаем данные авторизации
     *
     * @param username имя пользователя
     * @param password пароль
     * @return ответ в виде Response
     */
    public Response keycloakLogin(String username, String password) {
        try {
            log.info("Авторизуемся в Keycloak, пользователь {}", username);
            String codeVerifier = RandomStringUtils.randomAlphanumeric(96);
            String codeChallenge = getSha256DecodeValue(codeVerifier);

            Map<String, String> queryAuthParams = new HashMap<String, String>() {{
                put("client_id", CLIENT_ID);
                put("redirect_uri", standProperties.getFrontendUri());
                put("state", UUID.randomUUID().toString());
                put("response_mode", RESPONSE_MODE);
                put("response_type", RESPONSE_TYPE);
                put("scope", SCOPE);
                put("nonce", UUID.randomUUID().toString());
                put("code_challenge", codeChallenge);
                put("code_challenge_method", CODE_CHALLENGE_METHOD);
            }};

            Response authResponse = sendGetWithoutResponseLog(keycloakUrl + "/auth", new Cookies(), null, queryAuthParams);
            checkResponse(authResponse);

            Cookies authCookies = authResponse.detailedCookies();
            assertAll(
                    () -> notNullOrEmptyParameter(authCookies.getValue("AUTH_SESSION_ID"), "AUTH_SESSION_ID"),
                    () -> notNullOrEmptyParameter(authCookies.getValue("AUTH_SESSION_ID_LEGACY"), "AUTH_SESSION_ID_LEGACY")
            );

            Matcher matcher = Pattern.compile("action=\".+\"\\s").matcher(authResponse.asString());
            matcher.find();
            String action = matcher.group();
            matcher = Pattern.compile("\".+\"").matcher(action);
            matcher.find();
            String authenticateUrl = matcher.group().replaceAll("\"", "").replaceAll("amp;", "");

            Map<String, String> formAuthenticateParams = new HashMap<String, String>() {{
                put("username", username);
                put("password", password);
                put("credentialId", "");
            }};

            Response authenticateResponse = postToUrlWithoutResponseLog(formAuthenticateParams, authenticateUrl, authCookies, null, null);
            checkResponse(authenticateResponse, SC_MOVED_TEMPORARILY);

            Cookies authenticateCookies = authenticateResponse.detailedCookies();
            assertAll(
                    () -> notNullOrEmptyParameter(authenticateCookies.getValue("KEYCLOAK_IDENTITY"), "KEYCLOAK_IDENTITY"),
                    () -> notNullOrEmptyParameter(authenticateCookies.getValue("KEYCLOAK_IDENTITY_LEGACY"), "KEYCLOAK_IDENTITY_LEGACY"),
                    () -> notNullOrEmptyParameter(authenticateCookies.getValue("KEYCLOAK_SESSION"), "KEYCLOAK_SESSION"),
                    () -> notNullOrEmptyParameter(authenticateCookies.getValue("KEYCLOAK_SESSION_LEGACY"), "KEYCLOAK_SESSION_LEGACY")
            );

            List<Cookie> cookiesList = new ArrayList<Cookie>(){{
                addAll(authCookies.asList());
                addAll(authenticateCookies.asList());
            }};
            cookies = new Cookies(cookiesList);

            String location = authenticateResponse.getHeader("location");
            matcher = Pattern.compile("code=.+").matcher(location);
            matcher.find();
            String code = matcher.group().replaceAll("code=", "");

            Map<String, String> formTokenParams = new HashMap<String, String>() {{
                put("code", code);
                put("grant_type", "authorization_code");
                put("client_id", CLIENT_ID);
                put("redirect_uri", standProperties.getFrontendUri());
                put("code_verifier", codeVerifier);
            }};

            Response tokenResponse = postToUrlWithoutResponseLog(formTokenParams, keycloakUrl + "/token", new Cookies(cookiesList), null, null);
            checkResponse(tokenResponse);

            log.info("Авторизация успешна!");
            return tokenResponse;
        } catch (Exception e) {
            log.error("Не удалось авторизоваться", e);
            throw new IllegalStateException("Не удалось авторизоваться");
        }
    }

    @Override
    public String login() {
        String accessToken = getValueFromResponse(keycloakLogin(), "access_token");
        notNullOrEmptyParameter(accessToken, "access_token");
        return accessToken;
    }

    @Override
    public Cookies getLoginCookies() {
        if (cookies == null) {
            keycloakLogin();
        }
        return cookies;
    }

    @Override
    public String getUrl() {
        return keycloakUrl;
    }

    @Override
    public void logout() {
        //todo доработать при добавлении функционала выхода из системы
    }
}
