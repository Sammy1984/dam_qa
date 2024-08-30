package ru.spice.at.api.metadata_service;

import io.qameta.allure.Step;
import io.restassured.response.Response;
import io.restassured.response.ValidatableResponse;
import org.awaitility.Awaitility;
import ru.spice.at.api.dto.request.metadata.ProcessingsData;
import ru.spice.at.api.dto.request.metadata.RequestProcessings;
import ru.spice.at.api.dto.response.metadata.*;
import ru.spice.at.api.dto.response.metadata.filtration.DataItem;
import ru.spice.at.api.dto.response.metadata.filtration.FiltrationResponse;
import ru.spice.at.common.base_test.AbstractApiStepDef;
import ru.spice.at.common.dto.dam.ImageData;
import ru.spice.at.common.dto.dam.grpc.ProductMediaExport;
import ru.spice.at.common.emuns.ApiServices;
import ru.spice.at.common.emuns.dam.ProcessingTypeEnum;
import ru.spice.at.common.utils.Assert;
import ru.spice.at.common.utils.kafka.KafkaConsumerClient;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static org.apache.http.HttpStatus.*;
import static ru.spice.at.api.urls.MetadataServiceUrls.*;
import static ru.spice.at.api.utils.ApiUtils.*;
import static ru.spice.at.common.constants.Topics.CONTENT_PRODUCT_MEDIA;
import static ru.spice.at.common.emuns.dam.ImageParameters.*;
import static ru.spice.at.common.emuns.dam.SearchParameters.Q_LINK;
import static ru.spice.at.common.emuns.dam.SearchParameters.SEARCH;
import static ru.spice.at.common.utils.JsonHelper.getValueFromResponse;
import static ru.spice.at.common.utils.JsonHelper.jsonParse;

public class MetadataStepDef extends AbstractApiStepDef {
    public MetadataStepDef() {
        super(ApiServices.METADATA_SERVICE);
    }

    public MetadataStepDef(String token) {
        super(ApiServices.METADATA_SERVICE, token);
    }

    @Deprecated
    @Step("Фильтрация и проверка метаданных изображений")
    public ValidatableResponse successFiltrationMetadata(Map<String, Object> queryParam, boolean isEmptyBody) {
        AtomicReference<Response> response = new AtomicReference<>();
        String token = getAuthToken();
        Awaitility.await().atMost(10, TimeUnit.SECONDS).pollInterval(1, TimeUnit.SECONDS).until(() ->
        {
            response.set(sendGet(baseUrl + METADATA, queryParam, token));
            return isEmptyBody == response.get().then().extract().as(FiltrationResponse.class).getData().isEmpty();
        });

        checkResponseAndJsonScheme(response.get(), getJsonSchemaPath("metadataSuccessFiltrationScheme"));
        return response.get().then();
    }

    @Deprecated
    @Step("Фильтрация и проверка метаданных изображений")
    public ValidatableResponse successFiltrationMetadata(Map<String, Object> queryParam, int count) {
        AtomicReference<Response> response = new AtomicReference<>();
        String token = getAuthToken();
        Awaitility.await().atMost(15, TimeUnit.SECONDS).pollInterval(1, TimeUnit.SECONDS).until(() ->
        {
            response.set(sendGet(baseUrl + METADATA, queryParam, token));
            return response.get().then().extract().as(FiltrationResponse.class).getData().size() == count;
        });

        checkResponseAndJsonScheme(response.get(), getJsonSchemaPath("metadataSuccessFiltrationScheme"));
        return response.get().then();
    }

    public List<DataItem> successMetadataSearch(Object searchValue) {
        return successMetadataSearch(searchValue, false);
    }

    public List<DataItem> successMetadataSearch(Object searchValue, boolean isEmptyBody) {
        return successMetadataSearching(SEARCH.getName(), searchValue, isEmptyBody);
    }

    public List<DataItem> successMetadataSearch(Object searchValue, int count) {
        return successMetadataSearching(Collections.singletonMap(SEARCH.getName(), searchValue), count);
    }

    public List<DataItem> successMetadataSearching(String searchingParameters, Object searchingValue) {
        return successMetadataSearching(searchingParameters, searchingValue, false);
    }

    public List<DataItem> successMetadataSearching(String searchingParameters, Object searchingValue, boolean isEmptyBody) {
        return successMetadataSearching(Collections.singletonMap(searchingParameters, searchingValue), isEmptyBody);
    }

    @Step("Выполняем запрос на поиск, фильтрацию, сортировку с параметрами {searchingParameters}")
    public List<DataItem> successMetadataSearching(Map<String, Object> searchingParameters, boolean isEmptyBody) {
        AtomicReference<Response> response = new AtomicReference<>();
        Awaitility.await().atMost(10, TimeUnit.SECONDS).pollInterval(1, TimeUnit.SECONDS).until(() ->
        {
            response.set(postJsonToUrl(searchingParameters, baseUrl + METADATA_SEARCHING, getAuthToken()));
            return isEmptyBody == jsonParse(response.get(), FiltrationResponse.class).getData().isEmpty();
        });

        checkResponseAndJsonScheme(response.get(), getJsonSchemaPath("metadataSuccessFiltrationScheme"));
        return jsonParse(response.get(), FiltrationResponse.class).getData();
    }

    @Step("Выполняем запрос на поиск, фильтрацию, сортировку с параметрами {searchingParameters}")
    public List<DataItem> successMetadataSearching(Map<String, Object> searchingParameters, int count) {
        AtomicReference<Response> response = new AtomicReference<>();
        Awaitility.await().atMost(20, TimeUnit.SECONDS).pollInterval(1, TimeUnit.SECONDS).until(() ->
        {
            response.set(postJsonToUrl(searchingParameters, baseUrl + METADATA_SEARCHING, getAuthToken()));
            return count == jsonParse(response.get(), FiltrationResponse.class).getData().size();
        });

        checkResponseAndJsonScheme(response.get(), getJsonSchemaPath("metadataSuccessFiltrationScheme"));
        return jsonParse(response.get(), FiltrationResponse.class).getData();
    }

    public List<DataItem> successMetadataSearchingQLink(Object qLink) {
        return successMetadataSearchingQLink(qLink, false);
    }

    public List<DataItem> successMetadataSearchingQLink(Object qLink, boolean isEmptyBody) {
        return successMetadataSearching(Q_LINK.getName(), qLink, isEmptyBody);
    }

    public List<InvalidParamsItem> unsuccessfulMetadataSearching(String searchingParameters, Object searchingValue) {
        return unsuccessfulMetadataSearching(Collections.singletonMap(searchingParameters, searchingValue));
    }

    @Step("Выполняем неуспешный запрос на поиск, фильтрацию, сортировку с параметрами {searchingParameters}")
    public List<InvalidParamsItem> unsuccessfulMetadataSearching(Map<String, Object> searchingParameters) {
        Response response = postJsonToUrl(searchingParameters, baseUrl + METADATA_SEARCHING, getAuthToken());
        checkResponseAndJsonScheme(response, getJsonSchemaPath("unsuccessfulEditMetadataScheme"), SC_BAD_REQUEST);
        return jsonParse(response, ErrorResponse.class).error().invalidParams();
    }

    public void unsuccessfulForbiddenQLinkSearching(Object qLink) {
        unsuccessfulForbiddenMetadataSearching(Q_LINK.getName(), qLink);
    }

    public void unsuccessfulForbiddenMetadataSearching(String searchingParameters, Object searchingValue) {
        unsuccessfulForbiddenMetadataSearching(Collections.singletonMap(searchingParameters, searchingValue));
    }

    @Step("Выполняем неуспешный запрос на поиск, фильтрацию, сортировку с параметрами {searchingParameters} с ошибкой по доступу forbidden")
    public void unsuccessfulForbiddenMetadataSearching(Map<String, Object> searchingParameters) {
        Response response = postJsonToUrl(searchingParameters, baseUrl + METADATA_SEARCHING, getAuthToken());
        checkResponse(response, SC_FORBIDDEN);
    }

    public List<String> successMetadataSelectionSearch(Object searchValue) {
        return successMetadataSelectionSearch(searchValue, false);
    }

    public List<String> successMetadataSelectionSearch(Object searchValue, boolean isEmptyBody) {
        return successMetadataSelection(SEARCH.getName(), searchValue, isEmptyBody);
    }

    public List<String> successMetadataSelection(String searchingParameters, Object searchingValue) {
        return successMetadataSelection(searchingParameters, searchingValue, false);
    }

    public List<String> successMetadataSelection(String searchingParameters, Object searchingValue, boolean isEmptyBody) {
        return successMetadataSelection(Collections.singletonMap(searchingParameters, searchingValue), isEmptyBody);
    }

    @Step("Выполняем запрос на поиск, фильтрацию, сортировку с параметрами {searchingParameters}")
    public List<String> successMetadataSelection(Map<String, Object> searchingParameters, boolean isEmptyBody) {
        AtomicReference<Response> response = new AtomicReference<>();
        Awaitility.await().atMost(10, TimeUnit.SECONDS).pollInterval(1, TimeUnit.SECONDS).until(() ->
        {
            response.set(postJsonToUrl(searchingParameters, baseUrl + METADATA_SELECTION, getAuthToken()));
            List<String> ids = getValueFromResponse(response.get(), "data");
            return isEmptyBody == ids.isEmpty();
        });

        checkResponseAndJsonScheme(response.get(), getJsonSchemaPath("metadataSuccessSelectionScheme"));
        return getValueFromResponse(response.get(), "data");
    }

    public List<String> successMetadataSelectionQLink(Object qLink) {
        return successMetadataSelectionQLink(qLink, false);
    }

    public List<String> successMetadataSelectionQLink(Object qLink, boolean isEmptyBody) {
        return successMetadataSelection(Q_LINK.getName(), qLink, isEmptyBody);
    }

    public List<InvalidParamsItem> unsuccessfulMetadataSelection(String searchingParameters, Object searchingValue) {
        return unsuccessfulMetadataSelection(Collections.singletonMap(searchingParameters, searchingValue));
    }

    @Step("Выполняем неуспешный запрос на поиск, фильтрацию, сортировку с параметрами {searchingParameters}")
    public List<InvalidParamsItem> unsuccessfulMetadataSelection(Map<String, Object> searchingParameters) {
        Response response = postJsonToUrl(searchingParameters, baseUrl + METADATA_SELECTION, getAuthToken());
        checkResponseAndJsonScheme(response, getJsonSchemaPath("unsuccessfulEditMetadataScheme"), SC_BAD_REQUEST);
        return jsonParse(response, ErrorResponse.class).error().invalidParams();
    }

    @Step("Получаем объект для редактирования всех полей")
    public String buildEditRequest(ImageData imageData, String statusId, String categoryId, String qualityId,
                                   String sourceId, String assigneeId, Integer priority, String externalTaskId, String masterSellerId) {
        Map<String, Object> newValues = new HashMap<>() {{
            put(FILENAME.getName(), imageData.getFilename());
            put(SKU.getName(), imageData.getSku());
            put(STATUS_ID.getName(), statusId);
            put(MASTER_CATEGORY_ID.getName(), categoryId);
            put(QUALITY_ID.getName(), qualityId);
            put(SOURCE_ID.getName(), sourceId);
            put(ASSIGNEE_ID.getName(), assigneeId);
            put(PRIORITY.getName(), priority);
            put(EXTERNAL_TASK_ID.getName(), externalTaskId);
            put(MASTER_SELLER_ID.getName(), masterSellerId);
        }};
        return getReqJson("editRequest", newValues);
    }

    @Step("Загружаем метаданные изображения в метадате")
    public String createMetadataImage(ImageData image) {
        Response response = postJsonToUrl(image, baseUrl + METADATA, getAuthToken());
        checkResponse(response);

        String id = getValueFromResponse(response, "data.id");
        Assert.notNullOrEmptyParameter(id, "id");
        return id;
    }

    @Step("Загружаем метаданные изображения в метадате с ошибкой")
    public List<InvalidParamsItem> unsuccessfulCreateMetadataImage(Object image) {
        Response response = postJsonToUrl(image, baseUrl + METADATA, getAuthToken());
        checkResponseAndJsonScheme(response, getJsonSchemaPath("unsuccessfulEditMetadataScheme"), SC_BAD_REQUEST);
        return jsonParse(response, ErrorResponse.class).error().invalidParams();
    }

    @Step("Получаем доступные медиафайлы в количестве {count}")
    public Response getMediaFilesMetadata(int count) {
        AtomicReference<Response> response = new AtomicReference<>();
        Awaitility.await("Ожидаем медиафайлы").atMost(10, TimeUnit.SECONDS).pollInterval(1, TimeUnit.SECONDS).until(() ->
        {
            response.set(postJsonToUrl(Collections.singletonMap("limit", 50), baseUrl + METADATA_SEARCHING, getAuthToken()));
            return jsonParse(response.get(), FiltrationResponse.class).getData().size() == count;
        });
        checkResponseAndJsonScheme(response.get(), getJsonSchemaPath("metadataSuccessFiltrationScheme"));
        return response.get();
    }

    @Step("Получаем файлы в метадате из списка {names}")
    public List<String> getExistMetadata(List<String> names) {
        Response response = postJsonToUrl(names, baseUrl + METADATA_CHECK, getAuthToken());
        checkResponse(response, SC_OK);

        DataExistResponse dataResponse = jsonParse(response, DataExistResponse.class);
        return dataResponse.data();
    }

    @Step("Получаем данные экспорта в метадате из списка файлов с id {idList}")
    public DataExportResponse getExportMetadata(List<String> idList) {
        Response response = postJsonToUrl(idList, baseUrl + METADATA_EXPORT, getAuthToken());
        checkResponse(response, SC_OK);
        return jsonParse(response, DataExportResponse.class);
    }

    public List<UsersItem> getListUsersMetadata() {
        return getListUsersMetadata(null, null);
    }

    public List<UsersItem> getListUsersMetadata(String search) {
        return getListUsersMetadata(getDictionaryQuery(null, null, search));
    }

    public List<UsersItem> getListUsersMetadata(Object maxPageSize, String pageToken) {
        return getListUsersMetadata(getDictionaryQuery(maxPageSize, pageToken, null));
    }

    public List<UsersItem> getListUsersMetadata(Map<String, Object> queryParams) {
        return getListUsersMetadataResponse(queryParams).data();
    }

    @Step("Получаем список пользователей")
    public UsersResponse getListUsersMetadataResponse(Map<String, Object> queryParams) {
        Response response = sendGet(baseUrl + METADATA_DICTIONARIES_USERS, queryParams, getAuthToken());
        checkResponseAndJsonScheme(response, getJsonSchemaPath("listUsersScheme"));
        return jsonParse(response, UsersResponse.class);
    }

    public List<InvalidParamsItem> getUnsuccessfulListUsersMetadata(Object maxPageSize, String pageToken) {
        return getUnsuccessfulListUsersMetadata(getDictionaryQuery(maxPageSize, pageToken, null));
    }

    @Step("Неуспешно получаем список пользователей")
    public List<InvalidParamsItem> getUnsuccessfulListUsersMetadata(Map<String, Object> queryParams) {
        Response response = sendGet(baseUrl + METADATA_DICTIONARIES_USERS, queryParams, getAuthToken());
        checkResponseAndJsonScheme(response, getJsonSchemaPath("unsuccessfulEditMetadataScheme"), SC_BAD_REQUEST);
        return jsonParse(response, ErrorResponse.class).error().invalidParams();
    }

    @Step("Получаем список качества")
    public List<DictionariesItem> getListStatusesMetadata() {
        Response response = sendGet(baseUrl + METADATA_DICTIONARIES_STATUSES, getAuthToken());
        checkResponseAndJsonScheme(response, getJsonSchemaPath("listStatusesScheme"));
        return jsonParse(response, DictionariesResponse.class).data();
    }

    @Step("Получаем список исполнителей")
    public List<DictionariesItem> getListQualitiesMetadata() {
        Response response = sendGet(baseUrl + METADATA_DICTIONARIES_QUALITIES, getAuthToken());
        checkResponseAndJsonScheme(response, getJsonSchemaPath("listQualitiesScheme"));
        return jsonParse(response, DictionariesResponse.class).data();
    }

    public List<DictionariesItem> getListCategoriesMetadata() {
        return getListCategoriesMetadata(getDictionaryQuery(null, null, null));
    }

    public List<DictionariesItem> getListCategoriesMetadata(String search) {
        return getListCategoriesMetadata(getDictionaryQuery(null, null, search));
    }

    @Step("Получаем список категорий")
    public List<DictionariesItem> getListCategoriesMetadata(Map<String, Object> queryParams) {
        Response response = sendGet(baseUrl + METADATA_DICTIONARIES_CATEGORIES, queryParams, getAuthToken());
        checkResponseAndJsonScheme(response, getJsonSchemaPath("listCategoriesScheme"));
        return jsonParse(response, DictionariesResponse.class).data();
    }

    @Step("Получаем список источников")
    public List<DictionariesItem> getListSourcesMetadata() {
        Response response = sendGet(baseUrl + METADATA_DICTIONARIES_SOURCES, getAuthToken());
        checkResponseAndJsonScheme(response, getJsonSchemaPath("listSourcesScheme"));
        return jsonParse(response, DictionariesResponse.class).data();
    }

    public RetailersResponse getListRetailersMetadata() {
        return getListRetailersMetadata(null, null);
    }

    public RetailersResponse getListRetailersMetadata(Object search) {
        return getListRetailersMetadata(getDictionaryQuery(null, null, search));
    }

    public RetailersResponse getListRetailersMetadata(Object maxPageSize, String pageToken) {
        return getListRetailersMetadata(getDictionaryQuery(maxPageSize, pageToken, null));
    }

    @Step("Получаем список ритейлеров")
    public RetailersResponse getListRetailersMetadata(Map<String, Object> queryParams) {
        Response response = sendGet(baseUrl + METADATA_DICTIONARIES_RETAILERS, queryParams, getAuthToken());
        checkResponseAndJsonScheme(response, getJsonSchemaPath("listRetailersScheme"));
        return jsonParse(response, RetailersResponse.class);
    }

    public List<InvalidParamsItem> getUnsuccessfulListRetailersMetadata(Object maxPageSize, String pageToken) {
        return getUnsuccessfulListRetailersMetadata(getDictionaryQuery(maxPageSize, pageToken, null));
    }

    @Step("Неуспешно получаем список ритейлеров")
    public List<InvalidParamsItem> getUnsuccessfulListRetailersMetadata(Map<String, Object> queryParams) {
        Response response = sendGet(baseUrl + METADATA_DICTIONARIES_RETAILERS, queryParams, getAuthToken());
        checkResponseAndJsonScheme(response, getJsonSchemaPath("unsuccessfulEditMetadataScheme"), SC_BAD_REQUEST);
        return jsonParse(response, ErrorResponse.class).error().invalidParams();
    }

    private Map<String, Object> getDictionaryQuery(Object maxPageSize, String pageToken, Object search) {
        return new HashMap<>() {{
            if (maxPageSize != null)
                put("max_page_size", maxPageSize);
            if (pageToken != null)
                put("page_token", pageToken);
            if (search != null)
                put("search", search);
        }};
    }

    @Step("Удаляем объект в БД")
    public void deleteMetadata() {
        //todo Вернуть проверку на удачное удаление
        //Response response =
        sendDelete(baseUrl + METADATA, getAuthToken());
        //checkResponse(response, SC_NO_CONTENT);
    }

    @Step("Редактирование параметра {param} со значением {value} в метадате")
    public Response successEditMetadata(String id, String param, Object value) {
        return successEditMetadata(id, Collections.singletonMap(param, value));
    }

    @Step("Редактирование параметров в метадате")
    public Response successEditMetadata(String id, Object editRequest) {
        Response response = patchToUrl(editRequest, baseUrl + getMetadata(id), getAuthToken());
        checkResponseAndJsonScheme(response, getJsonSchemaPath("metadataSuccessesScheme"));
        return response;
    }

    @Step("Редактирование всех параметров в метадате")
    public Response successEditAllMetadata(String id, Object editRequest) {
        Response response = patchToUrl(editRequest, baseUrl + getMetadata(id), getAuthToken());
        checkResponseAndJsonScheme(response, getJsonSchemaPath("metadataSuccessesAllEditScheme"));
        return response;
    }

    public List<InvalidParamsItem> unsuccessfulEditMetadata(String id, String param, Object value) {
        return unsuccessfulEditMetadata(id, Collections.singletonMap(param, value));
    }

    @Step("Неуспешное редактирование параметров {editRequest} в метадате")
    public List<InvalidParamsItem> unsuccessfulEditMetadata(String id, Object editRequest) {
        Response response = patchToUrl(editRequest, baseUrl + getMetadata(id), getAuthToken());
        checkResponseAndJsonScheme(response, getJsonSchemaPath("unsuccessfulEditMetadataScheme"), SC_BAD_REQUEST);
        return jsonParse(response, ErrorResponse.class).error().invalidParams();
    }

    @Deprecated
    @Step("Неуспешное массовое редактирование параметров в метадате")
    public List<InvalidParamsItem> unsuccessfulMultiEditMetadata(Object editRequest) {
        Response response = patchToUrl(editRequest, baseUrl + METADATA, getAuthToken());
        checkResponseAndJsonScheme(response, getJsonSchemaPath("unsuccessfulEditMetadataScheme"), SC_BAD_REQUEST);
        return jsonParse(response, ErrorResponse.class).error().invalidParams();
    }

    @Step("Неуспешное редактирование параметров в метадате")
    public Response unsuccessfulServerErrorEditMetadata(String id, Map<String, Object> editValues) {
        Response response = patchToUrl(editValues, baseUrl + getMetadata(id), getAuthToken());
        checkResponse(response, SC_BAD_REQUEST);
        return response;
    }

    public Response unsuccessfulForbiddenEditMetadata(String id, String param, Object value) {
        return unsuccessfulForbiddenEditMetadata(id, Collections.singletonMap(param, value));
    }

    @Step("Неуспешное редактирование параметров в метадате")
    public Response unsuccessfulForbiddenEditMetadata(String id, Object editValues) {
        Response response = patchToUrl(editValues, baseUrl + getMetadata(id), getAuthToken());
        checkResponseAndJsonScheme(response, getJsonSchemaPath("unsuccessfulForbiddenMetadataScheme"), SC_FORBIDDEN);
        return response;
    }

    @Deprecated
    @Step("Успешное редактирование нескольких файлов")
    public void successMultiEditMetadata(Object requestMultiEdit) {
        ValidatableResponse validatableResponse = patchToUrl(requestMultiEdit, baseUrl + METADATA, getAuthToken()).then();
        validatableResponse.statusCode(SC_NO_CONTENT);
    }

    public void successMultiEditMetadata(List<String> ids, String param, Object value) {
        successMultiEditMetadata(ids, Collections.singletonMap(param, value));
    }

    @Step("Успешное редактирование нескольких файлов с ids={ids}")
    public void successMultiEditMetadata(List<String> ids, Map<String, Object> editValues) {
        Map<String, Object> editRequest = new HashMap<>();
        editRequest.put("ids", ids);
        editRequest.putAll(editValues);

        Response response = postJsonToUrl(editRequest, baseUrl + METADATA_UPDATES, getAuthToken());
        checkResponse(response, SC_ACCEPTED);
    }

    public List<InvalidParamsItem> unsuccessfulMultiEditMetadata(List<String> ids, String param, Object value) {
        return unsuccessfulMultiEditMetadata(ids, Collections.singletonMap(param, value));
    }

    @Step("Неуспешное массовое редактирование параметров в метадате")
    public List<InvalidParamsItem> unsuccessfulMultiEditMetadata(List<String> ids, Map<String, Object> editValues) {
        Map<String, Object> editRequest = new HashMap<>();
        editRequest.put("ids", ids);
        editRequest.putAll(editValues);

        Response response = postJsonToUrl(editRequest, baseUrl + METADATA_UPDATES, getAuthToken());
        checkResponseAndJsonScheme(response, getJsonSchemaPath("unsuccessfulEditMetadataScheme"), SC_BAD_REQUEST);
        return jsonParse(response, ErrorResponse.class).error().invalidParams();
    }


    @Step("Успешное получение ссылки qlink для файлов {idList}")
    public String successGetQLink(List<String> idList) {
        Response response = postJsonToUrl(idList, baseUrl + METADATA_LINKS, getAuthToken());
        checkResponse(response);
        return getValueFromResponse(response, "data.qlink");
    }

    @Step("Неуспешное получение ссылки qlink для файлов {idList}")
    public List<InvalidParamsItem> unsuccessfulGetQLink(Object idList) {
        Response response = postJsonToUrl(idList, baseUrl + METADATA_LINKS, getAuthToken());
        checkResponseAndJsonScheme(response, getJsonSchemaPath("unsuccessfulEditMetadataScheme"), SC_BAD_REQUEST);
        return jsonParse(response, ErrorResponse.class).error().invalidParams();
    }

    @Deprecated
    @Step("Успешное получение файлов по qlink = {qLink}")
    public List<DataItem> successGetMetadataQLink(String qLink) {
        Response response = sendGet(baseUrl + METADATA, Collections.singletonMap("qlink", qLink), getAuthToken());
        checkResponse(response);
        return jsonParse(response, FiltrationResponse.class).getData();
    }

    @Deprecated
    @Step("Неуспешное получение файлов по qlink = {qLink}")
    public List<InvalidParamsItem> unsuccessfulGetMetadataQLink(String qLink) {
        Response response = sendGet(baseUrl + METADATA, Collections.singletonMap("qlink", qLink), getAuthToken());
        checkResponseAndJsonScheme(response, getJsonSchemaPath("unsuccessfulEditMetadataScheme"), SC_BAD_REQUEST);
        return jsonParse(response, ErrorResponse.class).error().invalidParams();
    }

    @Deprecated
    @Step("Неуспешное получение файлов (forbidden) по qlink = {qLink}")
    public void unsuccessfulForbiddenGetMetadataQLink(String qLink) {
        Response response = sendGet(baseUrl + METADATA, Collections.singletonMap("qlink", qLink), getAuthToken());
        checkResponse(response, SC_FORBIDDEN);
        Assert.compareParameters("forbidden", getValueFromResponse(response, "error.type"), "error.type");
    }

    @Step("Успешное получение процессов обработки изображения id = {id}")
    public List<ProcessingItem> successGetProcessings(String id) {
        Response response = sendGet(baseUrl + getMetadataProcessings(id), getAuthToken());
        checkResponse(response);
        return jsonParse(response, ProcessingResponse.class).data();
    }

    @Step("Неуспешное получение процессов обработки изображения id = {id}")
    public List<InvalidParamsItem> unsuccessfulGetProcessings(String id) {
        Response response = sendGet(baseUrl + getMetadataProcessings(id), getAuthToken());
        checkResponseAndJsonScheme(response, getJsonSchemaPath("unsuccessfulEditMetadataScheme"), SC_BAD_REQUEST);
        return jsonParse(response, ErrorResponse.class).error().invalidParams();
    }

    @Step("Неуспешное NotFound получение процессов обработки изображения id = {id}")
    public void unsuccessfulGetProcessingsNotFound(String id) {
        Response response = sendGet(baseUrl + getMetadataProcessings(id), getAuthToken());
        checkResponseAndJsonScheme(response, getJsonSchemaPath("notFoundMetadataScheme"), SC_NOT_FOUND);
    }

    @Step("Успешное обновление обработки изображения id = {id}, типы обработки '{processingTypes}'")
    public List<ProcessingItem> successPutProcessings(String id, List<ProcessingTypeEnum> processingTypes) {
        Response response = putToUrl(
                Collections.singletonMap("data", processingTypes.stream().map(ProcessingTypeEnum::getId).collect(Collectors.toList())),
                baseUrl + getMetadataProcessings(id),
                getAuthToken());
        checkResponse(response);
        return jsonParse(response, ProcessingResponse.class).data();
    }

    @Step("Неуспешное обновление обработки изображения id = {id}, типы обработки '{processingTypesId}'")
    public List<InvalidParamsItem> unsuccessfulPutProcessings(String id, List<String> processingTypesId) {
        Response response = putToUrl(
                Collections.singletonMap("data", processingTypesId),
                baseUrl + getMetadataProcessings(id),
                getAuthToken());
        checkResponseAndJsonScheme(response, getJsonSchemaPath("unsuccessfulEditMetadataScheme"), SC_BAD_REQUEST);
        return jsonParse(response, ErrorResponse.class).error().invalidParams();
    }

    @Step("Неуспешное NotFound получение процессов обработки изображения id = {id}, типы обработки '{processingTypes}'")
    public void unsuccessfulPutProcessingsNotFound(String id, List<ProcessingTypeEnum> processingTypes) {
        Response response = putToUrl(
                Collections.singletonMap("data", processingTypes.stream().map(ProcessingTypeEnum::getId).collect(Collectors.toList())),
                baseUrl + getMetadataProcessings(id),
                getAuthToken());
        checkResponseAndJsonScheme(response, getJsonSchemaPath("notFoundMetadataScheme"), SC_NOT_FOUND);
    }

    @Step("Успешное обновление обработки изображений idList = {idList}, типы обработки '{processingTypes}'")
    public void successPostProcessings(List<String> idList, List<ProcessingTypeEnum> processingTypes) {
        Response response = postJsonToUrl(
                RequestProcessings.builder().data(
                        ProcessingsData.builder().
                                processingIds(processingTypes.stream().map(ProcessingTypeEnum::getId).collect(Collectors.toList())).
                                metadataIds(idList).build()).
                        build(),
                baseUrl + METADATA_PROCESSINGS,
                getAuthToken());
        checkResponse(response, SC_ACCEPTED);
    }

    @Step("Неуспешное обновление обработки изображений idList = {idList}, типы обработки '{processingTypesId}'")
    public List<InvalidParamsItem> unsuccessfulPostProcessings(List<String> idList, List<String> processingTypesId) {
        Response response = postJsonToUrl(
                RequestProcessings.builder().data(
                                ProcessingsData.builder().
                                        processingIds(processingTypesId).
                                        metadataIds(idList).build()).
                        build(),
                baseUrl + METADATA_PROCESSINGS,
                getAuthToken());
        checkResponseAndJsonScheme(response, getJsonSchemaPath("unsuccessfulEditMetadataScheme"), SC_BAD_REQUEST);
        return jsonParse(response, ErrorResponse.class).error().invalidParams();
    }

    @Step("Проверка уникальности в списке {dictionaries}")
    public void checkDictionaries(List<DictionariesItem> dictionaries) {
        Assert.compareParameters(dictionaries.size(), new HashSet<>(dictionaries).size(), "список dictionaries");
    }

    @Step("Проверка bad request ответа от экспорта в методате файлов с id {idList}")
    public void checkBadRequestExportMetadata(List<String> idList) {
        Response response = postJsonToUrl(idList, baseUrl + METADATA_EXPORT, getAuthToken());
        checkResponse(response, SC_BAD_REQUEST);
    }

    @Step("Проверка метадаты изображения id = {id} со статусом '{status}'")
    public void checkMetadataStatusImage(String id, String status) {
        Response response = sendGet(baseUrl + getMetadata(id), getAuthToken());
        checkResponseAndJsonScheme(response, getJsonSchemaPath("metadataSuccessesScheme"));
        Assert.compareParameters(
                status, getValueFromResponse(response, STATUS_ID.getPath() + ".name"), "статус");
    }

    @Step("Получение и проверка метадаты изображения id = {id}")
    public Response checkMetadata(String id) {
        Response response = sendGet(baseUrl + getMetadata(id), getAuthToken());
        checkResponseAndJsonScheme(response, getJsonSchemaPath("metadataSuccessesScheme"));
        return response;
    }

    @Step("Неуспешное получение и проверка метадаты изображения id = {id}")
    public Response unsuccessfulCheckMetadata(String id) {
        Response response = sendGet(baseUrl + getMetadata(id), getAuthToken());
        checkResponseAndJsonScheme(response, getJsonSchemaPath("unsuccessfulForbiddenMetadataScheme"), SC_FORBIDDEN);
        return response;
    }

    @Step("Проверка метадаты с несуществующем id = {id}")
    public void checkNotFoundMetadataImage(String id) {
        Response response = sendGet(baseUrl + getMetadata(id), getAuthToken());
        checkResponseAndJsonScheme(response, getJsonSchemaPath("notFoundMetadataScheme"), SC_NOT_FOUND);
    }

    @Step("Проверка метадаты с некорректным id = {id}")
    public void checkBadRequestMetadataImage(String id) {
        Response response = sendGet(baseUrl + getMetadata(id), getAuthToken());
        checkResponse(response, SC_BAD_REQUEST);
    }

    @Step("Успешная выгрузка изображений {idList} в PIMS, удаление: {fullReplacement}")
    public void successPostProductMediaExport(List<String> idList, Boolean fullReplacement) {
        Response response = postJsonToUrlWithoutHeaders(
                Collections.singletonMap("data", idList),
                baseUrl + METADATA_PRODUCT_MEDIA_EXPORT,
                Collections.singletonMap("full_replacement", fullReplacement),
                getAuthToken());
        checkResponse(response, SC_ACCEPTED);
    }

    @Step("Неуспешная выгрузка изображений {idList} в PIMS, удаление: {fullReplacement}")
    public List<InvalidParamsItem> unsuccessfulPostProductMediaExport(List<String> idList, Object fullReplacement) {
        Response response = postJsonToUrlWithoutHeaders(
                Collections.singletonMap("data", idList),
                baseUrl + METADATA_PRODUCT_MEDIA_EXPORT,
                fullReplacement == null ? null : Collections.singletonMap("full_replacement", fullReplacement),
                getAuthToken());
        checkResponseAndJsonScheme(response, getJsonSchemaPath("unsuccessfulEditMetadataScheme"), SC_BAD_REQUEST);
        return jsonParse(response, ErrorResponse.class).error().invalidParams();
    }

    @Step("Получение сообщения выгрузки изображений с SKU: {sku}")
    public ProductMediaExport.UpdateProductMedia pollProductMediaExport(String sku) {
        KafkaConsumerClient consumer = new KafkaConsumerClient();

        AtomicReference<ProductMediaExport.UpdateProductMedia> updateProductMediaAtomic = new AtomicReference<>();
        consumer.createConsumer(CONTENT_PRODUCT_MEDIA);
        Awaitility.given().pollDelay(300, TimeUnit.MILLISECONDS)
                .pollInterval(1, TimeUnit.SECONDS)
                .and().timeout(20, TimeUnit.SECONDS)
                .await("Сообщение не найдено SKU: " + sku)
                .until(() -> {
                    List<ProductMediaExport.UpdateProductMedia> updateProductMediaList =
                            consumer.poll(ProductMediaExport.UpdateProductMedia.class, 5000);
                    ProductMediaExport.UpdateProductMedia updateProductMedia = updateProductMediaList.stream().
                            filter(media -> media.getSku().equals(sku)).
                            findFirst().orElse(null);
                    if (updateProductMedia != null) {
                        updateProductMediaAtomic.set(updateProductMedia);
                        return true;
                    } else {
                        return false;
                    }
                });
        consumer.closeConsumer();

        return updateProductMediaAtomic.get();
    }
}
