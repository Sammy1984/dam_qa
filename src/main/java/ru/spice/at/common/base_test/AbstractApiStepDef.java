package ru.spice.at.common.base_test;

import io.qameta.allure.Step;
import lombok.extern.log4j.Log4j2;
import ru.spice.at.common.emuns.ApiServices;
import ru.spice.at.common.utils.JsonHelper;
import ru.spice.at.common.utils.auth.AuthUtils;

import java.util.Collections;
import java.util.Map;

import static java.lang.String.format;
import static ru.spice.at.api.utils.ApiUtils.loadDataAsString;
import static ru.spice.at.ui.utils.UiUtils.getStandProperties;

/**
 * Базовый класс для API шагов тестовых классов
 *
 * @author Aleksandr Osokin
 */
@Log4j2
public abstract class AbstractApiStepDef extends AbstractStepDef {
    private static final String JSON_REQUEST_PATH = "json/request/%s/";
    private static final String JSON_SCHEMA_PATH = "json/response_scheme/%s/";

    private final String reqJsonPath;
    private final String schemaJsonPath;

    protected String baseUrl;

    protected AbstractApiStepDef(ApiServices service) {
        baseUrl = getStandProperties().getUri();
        reqJsonPath = format(JSON_REQUEST_PATH, service);
        schemaJsonPath = format(JSON_SCHEMA_PATH, service);
    }

    protected AbstractApiStepDef(ApiServices service, String authToken) {
        this(service);
        setAuthToken(authToken);
    }

    @Override
    public String getAuthToken() {
        if (super.getAuthToken() == null) {
            login();
        }
        return super.getAuthToken();
    }

    @Step("Авторизация в системе")
    public boolean login() {
        log.info("Авторизация в системе");
        AuthUtils authUtils = standProperties.getAuthUtils(getRole());
        if (authUtils == null) {
            log.warn("Данные авторизации не указаны, авторизация не удалась!");
            return false;
        }
        setAuthToken((String) authUtils.login());
        return true;
    }

    @Step("Выход из системы")
    public void logout() {
        log.info("Выход из системы");
        standProperties.getAuthUtils(getRole()).logout();
    }

    /**
     * Достаем содержимое из json для запроса
     *
     * @param jsonName название файла json для тела запроса БЕЗ расширения '.json'
     * @return содержимое json
     */
    protected String getReqJson(String jsonName) {
        log.info("Достаем содержимое из json {} для запроса", jsonName);
        return loadDataAsString(format(reqJsonPath.concat("%s.json"), jsonName));
    }

    /**
     * Достаем содержимое из json для запроса с подстановкой параметра
     *
     * @param jsonName название файла json для тела запроса БЕЗ расширения '.json'
     * @param path     json путь до изменяемого параметра
     * @param value    значение параметра
     * @return содержимое json с измененным значением параметра
     */
    protected String getReqJson(String jsonName, String path, Object value) {
        String jsonTmp = getReqJson(jsonName);
        return JsonHelper.setValuesInJson(jsonTmp, Collections.singletonMap(path, value));
    }

    /**
     * Достаем содержимое из json для запроса с подстановкой параметров
     *
     * @param jsonName  название файла json для тела запроса БЕЗ расширения '.json'
     * @param newValues мапа, где ключ - json путь до изменяемого параметра, значение - значение параметра
     * @return содержимое json с измененным значением параметров
     */
    protected String getReqJson(String jsonName, Map<String, ?> newValues) {
        String jsonTmp = getReqJson(jsonName);
        return JsonHelper.setValuesInJson(jsonTmp, newValues);
    }

    /**
     * Определяем путь до json схемы
     *
     * @param jsonSchemaName название схемы json БЕЗ расширения '.json'
     * @return путь до схемы
     */
    protected String getJsonSchemaPath(String jsonSchemaName) {
        log.info("Определяем путь до json схемы {}", jsonSchemaName);
        return format(schemaJsonPath.concat("%s.json"), jsonSchemaName);
    }
}
