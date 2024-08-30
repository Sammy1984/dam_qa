package ru.spice.at.api.utils;

import io.qameta.allure.Step;
import io.restassured.config.HttpClientConfig;
import lombok.extern.log4j.Log4j2;
import org.apache.http.entity.mime.HttpMultipartMode;
import ru.spice.at.common.StandProperties;

import io.qameta.allure.Allure;
import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.config.RestAssuredConfig;
import io.restassured.http.ContentType;
import io.restassured.http.Cookies;
import io.restassured.response.Response;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import org.testng.Assert;
import ru.spice.at.common.dto.dam.ImageData;

import java.io.File;
import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static io.restassured.RestAssured.given;
import static io.restassured.config.EncoderConfig.encoderConfig;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static java.lang.String.format;
import static org.apache.http.HttpHeaders.CONNECTION;
import static org.apache.http.HttpStatus.SC_OK;
import static org.apache.http.params.CoreConnectionPNames.CONNECTION_TIMEOUT;
import static org.apache.http.params.CoreConnectionPNames.SO_TIMEOUT;
import static org.apache.http.protocol.HTTP.CONN_KEEP_ALIVE;
import static org.hamcrest.Matchers.equalTo;
import static ru.spice.at.common.constants.TestConstants.STABLE_STAND;
import static ru.spice.at.common.utils.ImageHelper.getRandomByteImage;

/**
 * Вспомогательный класс для работы с REST APi
 */
@Log4j2
public final class ApiUtils {
    private static final StandProperties standProperties = new StandProperties();

    private ApiUtils() {
        throw new IllegalAccessError("Это утилитный класс. Создание экземпляра не требуется.");
    }


    /**
     * ------------------------------------------------------------------------------------------
     * ----------------------------Методы для работы с ресурсами---------------------------------
     * ------------------------------------------------------------------------------------------
     */

    public static Optional<Path> getResourcePath(String fileName) {
        if (fileName.isEmpty()) {
            throw new IllegalArgumentException("Не задано имя файла!");
        }
        final Optional<Path> resourcePath;
        try {
            resourcePath = Optional.of(Paths.get(Objects.requireNonNull(
                    ApiUtils.class.getClassLoader().getResource(fileName)).toURI()));
        } catch (URISyntaxException e) {
            Assert.fail("Не удалось найти файл: " + fileName, e);
            return Optional.empty();
        }
        return resourcePath;
    }

    public static String loadDataAsString(String path) {
        final Path json = getPath(path);
        return loadDataAsString(json);
    }

    public static String loadDataAsString(Path path) {
        if (!Files.exists(path)) {
            throw new IllegalArgumentException("Can't find file: " + path);
        }
        try {
            return new String(Files.readAllBytes(path));
        } catch (IOException e) {
            log.error(e.getMessage());
            Allure.addAttachment("Response: ", format("exception: %s", e));
            Assert.fail("Can't get data from: " + path);
            return null;
        }
    }

    public static Path getPath(String path) {
        return ApiUtils.getResourcePath(path)
                .orElseThrow(() -> new IllegalArgumentException("Не найден путь до JSON"));
    }

    /**
     * ------------------------------------------------------------------------------------------
     * -------------------Методы для работы с валидацией ответа, JSON схемы----------------------
     * ------------------------------------------------------------------------------------------
     */

    public static void checkJsonScheme(Response response, String schemePath) {
        if (standProperties.getSettings().checkJsonScheme()) {
            response.then().body(matchesJsonSchemaInClasspath(schemePath));
        }
    }

    public static void checkJsonScheme(ResponseSpecification spec, String schemePath) {
        if (standProperties.getSettings().checkJsonScheme()) {
            spec.body(matchesJsonSchemaInClasspath(schemePath));
        }
    }

    public static ValidatableResponse checkResponseAndJsonScheme(Response response, String schemePath, int status) {
        ValidatableResponse validatableResponse = checkResponse(response, new ResponseSpecBuilder().expectStatusCode(status).build());
        checkJsonScheme(response, schemePath);
        return validatableResponse;
    }

    public static ValidatableResponse checkResponseAndJsonScheme(Response response, String schemePath) {
        ValidatableResponse validatableResponse = checkResponse(response, new ResponseSpecBuilder().expectStatusCode(SC_OK).build());
        checkJsonScheme(response, schemePath);
        return validatableResponse;
    }

    public static ValidatableResponse checkResponse(Response response, int status) {
        return checkResponse(response, new ResponseSpecBuilder().expectStatusCode(status).build());
    }

    public static ValidatableResponse checkResponse(Response response) {
        return checkResponse(response, new ResponseSpecBuilder().expectStatusCode(SC_OK).build());
    }

    public static ValidatableResponse checkResponse(Response response, ResponseSpecification spec) {
        return response.then().spec(spec);
    }

    public static void checkNegativeResponseAndJsonScheme(Response response, String schemaPath) {
        response.then().spec(createResponseSpecification("success", false));
        checkJsonScheme(response, schemaPath);
    }

    public static ResponseSpecification createResponseSpecification(String path, Object expectedValue) {
        return new ResponseSpecBuilder().expectStatusCode(200).expectBody(path, equalTo(expectedValue)).build();
    }

    /**
     * ------------------------------------------------------------------------------------------
     * ----------------------------Методы для работы с POST запросами----------------------------
     * ------------------------------------------------------------------------------------------
     */

    public static Response postJsonToUrl(Object json, String url) {
        return postToUrl(json, url, new Cookies(), null, null, ContentType.JSON);
    }

    public static Response postJsonToUrl(Object json, String url, String token) {
        return postToUrlAuth(json, url, new Cookies(), null, null, ContentType.JSON, token);
    }

    public static Response postJsonToUrl(Object json, String url, Cookies cookies) {
        return postToUrl(json, url, cookies, null, null, ContentType.JSON);
    }

    public static Response postJsonToUrl(Object json, String url, Cookies cookies, String token) {
        return postToUrlAuth(json, url, cookies, null, null, ContentType.JSON, token);
    }

    public static Response postJsonToUrl(Object json, String url, Cookies cookies, Map<String, String> headers,
                                         Map<String, Object> queryParams) {
        return postToUrl(json, url, cookies, headers, queryParams, ContentType.JSON);
    }

    public static Response postJsonToUrl(Object json, String url, Cookies cookies, Map<String, String> headers,
                                         Map<String, Object> queryParams, String token) {
        return postToUrlAuth(json, url, cookies, headers, queryParams, ContentType.JSON, token);
    }

    public static Response postJsonToUrlWithoutHeaders(Object json, String url, Map<String, Object> queryParams, String token) {
        return postToUrlAuth(json, url, new Cookies(), null, queryParams, ContentType.JSON, token);
    }

    public static Response postJsonToUrlWithoutQueryParams(Object json, String url, Map<String, String> headers, String token) {
        return postToUrlAuth(json, url, new Cookies(), headers, null, ContentType.JSON, token);
    }

    public static Response postXMLToUrl(Object xml, String url, Cookies cookies, Map<String, String> headers, String token) {
        return postToUrlAuth(xml, url, cookies, headers, null, ContentType.TEXT, token);
    }

    @Step("Выполняем POST запрос c авторизацией")
    public static Response postToUrlAuth(final Object body, final String url, final Cookies cookies, final Map<String, String> headers,
                                     final Map<String, Object> queryParams, final ContentType contentType, final String token) {
        final Map<String, Object> queryParamsMap = queryParams == null ? new HashMap<>() : queryParams;
        final Map<String, String> headersMap = getHeaders(headers);
        Response response = given()
                .filter(new AllureRestAssured())
                .contentType(contentType.withCharset(StandardCharsets.UTF_8))
                .config(getRestAssuredConfig(contentType))
                .auth().oauth2(token)
                .headers(headersMap)
                .queryParams(queryParamsMap)
                .cookies(cookies)
                .body(body)
                .log().all()
                .post(url)
                .then().log().all().extract().response();
        return attachTimeResponse(response);
    }

    @Step("Выполняем POST запрос")
    public static Response postToUrl(final Object body, final String url, final Cookies cookies, final Map<String, String> headers,
                                         final Map<String, Object> queryParams, final ContentType contentType) {
        final Map<String, Object> queryParamsMap = queryParams == null ? new HashMap<>() : queryParams;
        final Map<String, String> headersMap = getHeaders(headers);
        Response response = given()
                .filter(new AllureRestAssured())
                .contentType(contentType.withCharset(StandardCharsets.UTF_8))
                .config(getRestAssuredConfig(contentType))
                .headers(headersMap)
                .queryParams(queryParamsMap)
                .cookies(cookies)
                .body(body)
                .log().all()
                .post(url)
                .then().log().all().extract().response();
        return attachTimeResponse(response);
    }

    public static Response postToUrlWithoutResponseLog(Map<String, String> formParams, String url, Map<String, String> headersMap, Map<String, String> queryParams) {
        return postToUrlWithoutResponseLog(formParams, url, new Cookies(), headersMap, queryParams);
    }

    public static Response postToUrlWithoutResponseLog(final Map<String, String> formParams, final String url,
                                     final Cookies cookies, final Map<String, String> headers, final Map<String, String> queryParams) {
        log.info("Выполняем POST запрос без логов");
        final Map<String, String> formParamsMap = formParams == null ? new HashMap<>() : formParams;
        final Map<String, String> queryParamsMap = queryParams == null ? new HashMap<>() : queryParams;
        final Map<String, String> headersMap = getHeaders(headers);
        Response response = given()
                .filter(new AllureRestAssured())
                .contentType(ContentType.URLENC)
                .config(getRestAssuredConfig())
                .formParams(formParamsMap)
                .cookies(cookies)
                .headers(headersMap)
                .queryParams(queryParamsMap)
                .log().uri()
                .log().method()
                .log().headers()
                .post(url);
        return attachTimeResponse(response);
    }

    public static Response postFilesToUrl(String url, List<File> files) {
        return postFilesToUrl(url, files, null);
    }

    @Step("Выполняем POST запрос с загрузкой файлов")
    public static Response postFilesToUrl(String url, List<File> files, String token) {
        RequestSpecification specification = buildImageReqSpecification();
        files.forEach(file -> specification.multiPart("files", file));

        return token == null ? postFilesToUrl(url, specification) : postFilesToUrlAuth(url, token, specification);
    }

    public static Response postImageToUrl(String url, ImageData image) {
        return postImageToUrl(url, image, null);
    }

    @Step("Выполняем POST запрос с загрузкой изображения")
    public static Response postImageToUrl(String url, ImageData image, String token) {
        RequestSpecification specification = buildImageReqSpecification()
                .multiPart("file", image.getFilename(),
                        getRandomByteImage(image.getWidth(), image.getHeight(), image.getFormat().getFormatName()));
        return token == null ? postFilesToUrl(url, specification) : postFilesToUrlAuth(url, token, specification);
    }

    public static Response postImagesToUrl(String url, List<ImageData> images) {
        return postImagesToUrl(url, images, null);
    }

    public static Response postImagesToUrl(String url, List<ImageData> images, String token) {
        return postImagesToUrl(url, null, images, token);
    }

    @Step("Выполняем POST запрос с загрузкой изображений")
    public static Response postImagesToUrl(String url, final Map<String, ?> queryParams, List<ImageData> images, String token) {
        final Map<String, ?> queryParameters = Optional.ofNullable(queryParams).orElseGet(HashMap::new);
        RequestSpecification specification = buildImageReqSpecification();
        specification.queryParams(queryParameters);

        images.forEach(image ->
            specification.multiPart("data", image.getFilename(),
                    getRandomByteImage(image.getWidth(), image.getHeight(), image.getFormat().getFormatName()))
        );
        return token == null ? postFilesToUrl(url, specification) : postFilesToUrlAuth(url, token, specification);
    }

    public static Response postImagesToUrl(String url, Map<String, byte[]> images) {
        return postImagesToUrl(url, images, null);
    }

    public static Response postImagesToUrl(String url, Map<String, byte[]> images, String token) {
        return postImagesToUrl(url, null, images, token);
    }

    @Step("Выполняем POST запрос с загрузкой изображений")
    public static Response postImagesToUrl(String url, final Map<String, ?> queryParams, Map<String, byte[]> images, String token) {
        final Map<String, ?> queryParameters = Optional.ofNullable(queryParams).orElseGet(HashMap::new);
        RequestSpecification specification = buildImageReqSpecification();
        specification.queryParams(queryParameters);

        images.forEach((k, v) ->
                specification.multiPart("data", k, v)
        );
        return token == null ? postFilesToUrl(url, specification) : postFilesToUrlAuth(url, token, specification);
    }

    private static Response postFilesToUrlAuth(final String url, final String token, RequestSpecification specification) {
        final Map<String, String> headersMap = getHeaders(null);
        Response response = given()
                .spec(specification)
                .filter(new AllureRestAssured())
                .config(getRestAssuredConfig())
                .headers(headersMap)
                .auth().oauth2(token)
                .log().all()
                .post(url)
                .then().log().all().extract().response();
        return attachTimeResponse(response);
    }

    private static Response postFilesToUrl(final String url, RequestSpecification specification) {
        final Map<String, String> headersMap = getHeaders(null);
        Response response = given()
                .spec(specification)
                .filter(new AllureRestAssured())
                .config(getRestAssuredConfig())
                .headers(headersMap)
                .log().all()
                .post(url)
                .then().log().all().extract().response();
        return attachTimeResponse(response);
    }

    /**
     * ------------------------------------------------------------------------------------------
     * -----------------------------Методы для работы с GET запросами----------------------------
     * ------------------------------------------------------------------------------------------
     */

    public static Response sendGet(final String url) {
        return sendGet(url, (Map<String, ?>) null, null);
    }

    public static Response sendGet(final String url, final String token) {
        return sendGet(url, (Map<String, ?>) null, token);
    }

    public static Response sendGet(final String url, final Cookies cookies, final String token) {
        return sendGet(url, cookies, null, null, token);
    }

    public static Response sendGet(final String url, final Map<String, ?> queryParams) {
        return sendGet(url, null, queryParams, null);
    }

    public static Response sendGet(final String url, final Map<String, ?> queryParams, final String token) {
        return sendGet(url, null, queryParams, token);
    }

    public static Response sendGet(final String url, final Map<String, String> headers,
                                   final Map<String, ?> queryParams, final String token) {
        return sendGet(url, new Cookies(), headers, queryParams, token);
    }

    public static Response sendGet(final String url, final Cookies initCookies, final Map<String, String> headers,
                                   final Map<String, ?> queryParams, final String token) {
        return token == null ? sendGet(url, initCookies, headers, queryParams, buildReqSpecification()) :
                sendGet(url, initCookies, headers, queryParams, token, buildReqSpecification());
    }

    @Step("Выполняем GET запрос с авторизацией")
    public static Response sendGet(final String url, final Cookies initCookies, final Map<String, String> headers,
                                   final Map<String, ?> queryParams, final String token, RequestSpecification requestSpecification) {
        final Map<String, ?> queryParameters = Optional.ofNullable(queryParams).orElseGet(HashMap::new);
        final Cookies cookies = Optional.ofNullable(initCookies).orElseGet(Cookies::new);
        final Map<String, String> headersMap = getHeaders(headers);

        Response response = given()
                .spec(requestSpecification)
                .filter(new AllureRestAssured())
                .config(getRestAssuredConfig())
                .auth().oauth2(token)
                .contentType(ContentType.JSON.withCharset(StandardCharsets.UTF_8))
                .headers(headersMap)
                .queryParams(queryParameters)
                .cookies(cookies)
                .log()
                .all()
                .get(url)
                .then()
                .log()
                .all()
                .extract()
                .response();
        return attachTimeResponse(response);
    }

        @Step("Выполняем GET запрос")
        public static Response sendGet(final String url, final Cookies initCookies, final Map<String, String> headers,
                                       final Map<String, ?> queryParams, RequestSpecification requestSpecification) {
            final Map<String, ?> queryParameters = Optional.ofNullable(queryParams).orElseGet(HashMap::new);
            final Cookies cookies = Optional.ofNullable(initCookies).orElseGet(Cookies::new);
            final Map<String, String> headersMap = getHeaders(headers);

            Response response = given()
                    .spec(requestSpecification)
                    .filter(new AllureRestAssured())
                    .config(getRestAssuredConfig())
                    .contentType(ContentType.JSON.withCharset(StandardCharsets.UTF_8))
                    .headers(headersMap)
                    .queryParams(queryParameters)
                    .cookies(cookies)
                    .log()
                    .all()
                    .get(url)
                    .then()
                    .log()
                    .all()
                    .extract()
                    .response();
            return attachTimeResponse(response);
    }

    public static Response sendGetWithoutResponseLog(final String url, final Cookies initCookies, final Map<String, String> headers,
                                                     final Map<String, ?> queryParams) {
        log.info("Выполняем GET запрос без логов");
        final Map<String, ?> queryParameters = Optional.ofNullable(queryParams).orElseGet(HashMap::new);
        final Cookies cookies = Optional.ofNullable(initCookies).orElseGet(Cookies::new);
        final Map<String, String> headersMap = getHeaders(headers);

        Response response = given()
                .spec(buildReqSpecification())
                .filter(new AllureRestAssured())
                .config(getRestAssuredConfig())
                .contentType(ContentType.JSON.withCharset(StandardCharsets.UTF_8))
                .headers(headersMap)
                .queryParams(queryParameters)
                .cookies(cookies)
                .log().uri()
                .log().method()
                .log().headers()
                .get(url);
        return attachTimeResponse(response);
    }

    /**
     * ------------------------------------------------------------------------------------------
     * ----------------------------Методы для работы с PATCH запросами----------------------------
     * ------------------------------------------------------------------------------------------
     */

    public static Response patchToUrl(final Object json, final String url) {
        return patchToUrl(json, url, new Cookies(), null);
    }

    public static Response patchToUrl(final Object json, final String url, final String token) {
        return patchToUrl(json, url, new Cookies(), token);
    }

    public static Response patchToUrl(final Object json, final String url, final Cookies cookies) {
        return patchToUrl(json, url, cookies, null);
    }

    public static Response patchToUrl(final Object json, final String url, final Cookies cookies, final String token) {
        return patchToUrl(json, url, cookies, null, null, ContentType.JSON, token);
    }

    public static Response patchToUrl(final Object body, final String url, final Cookies cookies, final Map<String, String> headers,
                                      final Map<String, String> queryParams, final ContentType contentType, final String token) {
        return token == null ? patchToUrl(body, url, cookies, headers, queryParams, contentType, buildReqSpecification()) :
                patchToUrl(body, url, cookies, headers, queryParams, contentType, token, buildReqSpecification());
    }

    @Step("Выполняем PATCH запрос с авторизацией")
    public static Response patchToUrl(final Object body, final String url, final Cookies cookies, final Map<String, String> headers,
                                      final Map<String, String> queryParams, final ContentType contentType, final String token, RequestSpecification spec) {
        final Map<String, String> queryParamsMap = queryParams == null ? new HashMap<>() : queryParams;
        final Map<String, String> headersMap = getHeaders(headers);
        Response response = given()
                .spec(spec)
                .filter(new AllureRestAssured())
                .contentType(contentType.withCharset(StandardCharsets.UTF_8))
                .config(getRestAssuredConfig(contentType))
                .auth().oauth2(token)
                .headers(headersMap)
                .queryParams(queryParamsMap)
                .cookies(cookies)
                .body(body)
                .log().all()
                .patch(url)
                .then().log().all().extract().response();
        return attachTimeResponse(response);
    }

    @Step("Выполняем PATCH запрос")
    public static Response patchToUrl(final Object body, final String url, final Cookies cookies, final Map<String, String> headers,
                                      final Map<String, String> queryParams, final ContentType contentType, RequestSpecification spec) {
        final Map<String, String> queryParamsMap = queryParams == null ? new HashMap<>() : queryParams;
        final Map<String, String> headersMap = getHeaders(headers);
        Response response = given()
                .spec(spec)
                .filter(new AllureRestAssured())
                .contentType(contentType.withCharset(StandardCharsets.UTF_8))
                .config(getRestAssuredConfig(contentType))
                .headers(headersMap)
                .queryParams(queryParamsMap)
                .cookies(cookies)
                .body(body)
                .log().all()
                .patch(url)
                .then().log().all().extract().response();
        return attachTimeResponse(response);
    }

    /**
     * ------------------------------------------------------------------------------------------
     * -----------------------------Методы для работы с PUT запросами----------------------------
     * ------------------------------------------------------------------------------------------
     */

    public static Response putToUrl(final Object json, final String url) {
        return putToUrl(json, url, new Cookies(), null);
    }

    public static Response putToUrl(final Object json, final String url, final String token) {
        return putToUrl(json, url, new Cookies(), token);
    }

    public static Response putToUrl(final Object json, final String url, final Cookies cookies) {
        return putToUrl(json, url, cookies, null);
    }

    public static Response putToUrl(final Object json, final String url, final Cookies cookies, final String token) {
        return token == null ? putToUrl(json, url, cookies, null, null, ContentType.JSON, buildReqSpecification()) :
                putToUrl(json, url, cookies, null, null, ContentType.JSON, token, buildReqSpecification());
    }

    @Step("Выполняем PUT запрос с авторизацией")
    public static Response putToUrl(final Object body, final String url, final Cookies cookies, final Map<String, String> headers,
                                    final Map<String, String> queryParams, final ContentType contentType, final String token,
                                    RequestSpecification spec) {
        final Map<String, String> queryParamsMap = queryParams == null ? new HashMap<>() : queryParams;
        final Map<String, String> headersMap = getHeaders(headers);
        Response response = given()
                .spec(spec)
                .filter(new AllureRestAssured())
                .contentType(contentType.withCharset(StandardCharsets.UTF_8))
                .config(getRestAssuredConfig(contentType))
                .auth().oauth2(token)
                .headers(headersMap)
                .queryParams(queryParamsMap)
                .cookies(cookies)
                .body(body)
                .log().all()
                .put(url)
                .then().log().all().extract().response();
        return attachTimeResponse(response);
    }

    @Step("Выполняем PUT запрос")
    public static Response putToUrl(final Object body, final String url, final Cookies cookies, final Map<String, String> headers,
                                    final Map<String, String> queryParams, final ContentType contentType, RequestSpecification spec) {
        final Map<String, String> queryParamsMap = queryParams == null ? new HashMap<>() : queryParams;
        final Map<String, String> headersMap = getHeaders(headers);
        Response response = given()
                .spec(spec)
                .filter(new AllureRestAssured())
                .contentType(contentType.withCharset(StandardCharsets.UTF_8))
                .config(getRestAssuredConfig(contentType))
                .headers(headersMap)
                .queryParams(queryParamsMap)
                .cookies(cookies)
                .body(body)
                .log().all()
                .put(url)
                .then().log().all().extract().response();
        return attachTimeResponse(response);
    }

    /**
     * ------------------------------------------------------------------------------------------
     * ---------------------------Методы для работы с DELETE запросами---------------------------
     * ------------------------------------------------------------------------------------------
     */

    public static Response sendDelete(final String url) {
        return sendDelete(url, new Cookies(), null, null);
    }

    public static Response sendDelete(final String url, final String token) {
        return sendDelete(url, new Cookies(), null, token);
    }

    public static Response sendDelete(final String url, final Map<String, String> headers) {
        return sendDelete(null, url, new Cookies(), headers, null, buildReqSpecification());
    }

    public static Response sendDelete(final String url, final Map<String, String> headers, final String token) {
        return sendDelete(null, url, new Cookies(), headers, null, token, buildReqSpecification());
    }

    public static Response sendDelete(final String url, final Cookies cookies, final String token) {
        return sendDelete(url, cookies, null, token);
    }

    public static Response sendDelete(final String url, final Cookies cookies, final Map<String, String> queryParams, final String token) {
        return token == null ? sendDelete(null, url, cookies, null, queryParams, buildReqSpecification()) :
                sendDelete(null, url, cookies, null, queryParams, token, buildReqSpecification());
    }

    @Step("Выполняем DELETE запрос с авторизацией")
    public static Response sendDelete(Object body, final String url, Cookies cookies, final Map<String, String> headers,
                                      final Map<String, String> queryParams, final String token, RequestSpecification requestSpecification) {
        final Map<String, String> headersMap = getHeaders(headers);
        Response response = given()
                .spec(requestSpecification)
                .filter(new AllureRestAssured())
                .config(getRestAssuredConfig())
                .auth().oauth2(token)
                .headers(headersMap)
                .queryParams(queryParams == null ? new HashMap<>() : queryParams)
                .cookies(cookies)
                .body((body == null) ? "" : body)
                .log()
                .all()
                .delete(url)
                .then()
                .log()
                .all()
                .extract()
                .response();
        return attachTimeResponse(response);
    }

    @Step("Выполняем DELETE запрос")
    public static Response sendDelete(Object body, final String url, Cookies cookies, final Map<String, String> headers,
                                      final Map<String, String> queryParams, RequestSpecification requestSpecification) {
        final Map<String, String> headersMap = getHeaders(headers);
        Response response = given()
                .spec(requestSpecification)
                .filter(new AllureRestAssured())
                .config(getRestAssuredConfig())
                .headers(headersMap)
                .queryParams(queryParams == null ? new HashMap<>() : queryParams)
                .cookies(cookies)
                .body((body == null) ? "" : body)
                .log()
                .all()
                .delete(url)
                .then()
                .log()
                .all()
                .extract()
                .response();
        return attachTimeResponse(response);
    }

    /**
     * ------------------------------------------------------------------------------------------
     * -----------------------Дополнительные методы для работы с REST API------------------------
     * ------------------------------------------------------------------------------------------
     */

    public static RequestSpecification buildReqSpecification() {
        return new RequestSpecBuilder()
                .setContentType("application/json;charset=utf-8")
                .build();
    }

    public static RequestSpecification buildImageReqSpecification() {
        return new RequestSpecBuilder()
                .setContentType("multipart/form-data;charset=UTF-8")
                .build();
    }

    private static RestAssuredConfig getRestAssuredConfig(ContentType contentType) {
        return getRestAssuredConfig()
                .encoderConfig(encoderConfig().encodeContentTypeAs("application/octet-stream", contentType));
    }

    private static RestAssuredConfig getRestAssuredConfig() {
        return RestAssured.config().httpClient(HttpClientConfig.httpClientConfig()
                        .httpMultipartMode(HttpMultipartMode.BROWSER_COMPATIBLE)
                        .setParam(CONNECTION_TIMEOUT, standProperties.getSettings().restTimeout() * 1000)
                        .setParam(SO_TIMEOUT, standProperties.getSettings().restTimeout() * 1000));
    }

    public static Map<String, String> getHeaders(Map<String, String> headers) {
        final Map<String, String> headersMap = new HashMap<>();
        headersMap.put(CONNECTION, CONN_KEEP_ALIVE);

        String standName = standProperties.getSettings().standName();
        if (standName != null && !standName.equals(STABLE_STAND) && standName.contains(STABLE_STAND.substring(0, 6))) {
            headersMap.put("sbm-forward-feature-version-paas-content-spice-dam-import-service", standName);
            headersMap.put("sbm-forward-feature-version-paas-content-spice-dam-export-service", standName);
            headersMap.put("sbm-forward-feature-version-paas-content-spice-dam-metadata", standName);
        }

        if (headers == null) {
            return headersMap;
        }
        headersMap.putAll(headers);
        return headersMap;
    }

    private static Response attachTimeResponse(Response response) {
        long time = response.getTimeIn(TimeUnit.MILLISECONDS);
        Allure.addAttachment("Время выполнения запроса", String.format("%s мс", time));
        return response;
    }
}
