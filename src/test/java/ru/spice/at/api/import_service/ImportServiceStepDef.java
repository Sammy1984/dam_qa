package ru.spice.at.api.import_service;

import com.adobe.internal.xmp.XMPMeta;
import com.adobe.internal.xmp.impl.XMPMetaImpl;
import com.adobe.internal.xmp.options.PropertyOptions;
import io.qameta.allure.Step;
import io.restassured.http.ContentType;
import io.restassured.http.Cookies;
import io.restassured.response.Response;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.RandomStringUtils;
import ru.spice.at.api.dto.request.import_service.*;
import ru.spice.at.api.dto.response.import_service.*;
import ru.spice.at.api.dto.response.import_service.imports.ImportsListResponse;
import ru.spice.at.api.dto.response.import_service.imports.item.ImportsItemResponse;
import ru.spice.at.api.urls.ImportServiceUrls;
import ru.spice.at.common.base_test.AbstractApiStepDef;
import ru.spice.at.common.dto.dam.ImageData;
import ru.spice.at.common.emuns.ApiServices;
import ru.spice.at.common.emuns.dam.ProcessingTypeEnum;
import ru.spice.at.common.utils.XmpUtil;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.adobe.internal.xmp.XMPConst.*;
import static com.adobe.internal.xmp.options.PropertyOptions.ARRAY;
import static java.time.format.DateTimeFormatter.ISO_INSTANT;
import static org.apache.http.HttpStatus.*;
import static ru.spice.at.api.urls.ImportServiceUrls.*;
import static ru.spice.at.api.utils.ApiUtils.*;
import static ru.spice.at.common.constants.TestConstants.CAPITAL_NULL;
import static ru.spice.at.common.constants.TestConstants.DOT_VALUE;
import static ru.spice.at.common.emuns.dam.ImageParameters.*;
import static ru.spice.at.common.utils.Assert.*;
import static ru.spice.at.common.utils.FileHelper.createFileFromBytesArray;
import static ru.spice.at.common.utils.ImageHelper.getRandomByteImage;
import static ru.spice.at.common.utils.JsonHelper.getValueFromResponse;
import static ru.spice.at.common.utils.JsonHelper.jsonParse;

@Log4j2
public class ImportServiceStepDef extends AbstractApiStepDef {
    private static final String IMAGES_IMPORT_PATH = "src/test/resources/images/import/";

    public ImportServiceStepDef() {
        super(ApiServices.IMPORT_SERVICE);
    }

    public ImportServiceStepDef(String token) {
        super(ApiServices.IMPORT_SERVICE, token);
    }

    public RequestExternalImport buildEditRequest(String fileBase64, String filename) {
        return buildEditRequest(fileBase64, filename, OffsetDateTime.now().format(ISO_INSTANT));
    }

    @Step("Получаем объект для редактирования всех полей")
    public RequestExternalImport buildEditRequest(String fileBase64, String filename, String readyForRetouchAt) {
        return new RequestExternalImport().externalImportData(
                new ExternalImportData().
                        file(fileBase64).
                        filename(filename).
                        ownTrademark(true).
                        readyForRetouchAt(readyForRetouchAt).
                        externalOfferName(RandomStringUtils.randomAlphabetic(10) + 1).
                        externalTaskId(String.valueOf(new Random().nextInt(100) + 1)).
                        externalOfferId(new Random().nextInt(100) + 1).
                        externalMasterCategoryId(new Random().nextInt(100) + 1).
                        sku(RandomStringUtils.randomAlphabetic(10) + 1).
                        priority(new Random().nextInt(97) + 2).
                        masterSellerId(new Random().nextInt(100) + 1));
    }

    @SneakyThrows
    @Step("Получаем объект для XMP metadata")
    public XMPMeta buildRequestXmpMetadata(RequestXmpMetadata metadata) {
        //вариативность заполнения - keywords, masterCategoryId, sourceId
        XMPMeta metadataXmp = new XMPMetaImpl();
        metadataXmp.setProperty(NS_JPEG, ASSIGNEE_ID.getName(), metadata.assigneeId());
        metadataXmp.setProperty(NS_JPEG, COPYRIGHT.getName(), metadata.copyright());
        metadataXmp.setProperty(NS_JPEG, CREATED_AT.getName(), metadata.createdAt());
        metadataXmp.setProperty(NS_JPEG, CREATED_BY.getName(), metadata.createdBy());
        metadataXmp.setProperty(NS_JPEG, DESCRIPTION.getName(), metadata.description());
        metadataXmp.setProperty(NS_JPEG, EXTERNAL_DRAFT_DONE.getName(), metadata.externalDraftDone());
        metadataXmp.setProperty(NS_JPEG, EXTERNAL_OFFER.getName() + "_id", metadata.externalOfferId());
        metadataXmp.setProperty(NS_JPEG, EXTERNAL_OFFER.getName() + "_name", metadata.externalOfferName());
        metadataXmp.setProperty(NS_JPEG, EXTERNAL_TASK_ID.getName(), metadata.externalTaskId());
        metadataXmp.setProperty(NS_JPEG, FILENAME.getName(), metadata.filename());
        metadataXmp.setProperty(NS_JPEG, IMPORT_TYPE.getName() + "_code", metadata.importTypeCode());
        metadataXmp.setProperty(NS_JPEG, IMPORT_TYPE.getName() + "_id", metadata.importTypeId());
        metadataXmp.setProperty(NS_JPEG, IMPORT_TYPE.getName() + "_name", metadata.importTypeName());
        metadataXmp.setProperty(NS_JPEG, KEY.getName(), metadata.key());

        if (metadata.keywords().isEmpty()) {
            metadataXmp.setProperty(NS_JPEG, KEYWORDS.getName(), CAPITAL_NULL);
        }
        else {
            metadataXmp.setProperty(NS_JPEG, KEYWORDS.getName(), null, new PropertyOptions(ARRAY));
            for (String keyword : metadata.keywords()) {
                metadataXmp.appendArrayItem(NS_JPEG, KEYWORDS.getName(), keyword);
            }
        }

        if (metadata.masterCategoryId().equals(CAPITAL_NULL)) {
            metadataXmp.setProperty(NS_JPEG, MASTER_CATEGORY.getName(), metadata.masterCategoryId());
        }
        else {
            metadataXmp.setProperty(NS_JPEG, MASTER_CATEGORY_ID.getName(), metadata.masterCategoryId());
            metadataXmp.setProperty(NS_JPEG, MASTER_CATEGORY_NAME.getName(), metadata.masterCategoryName());
        }

        metadataXmp.setProperty(NS_JPEG, MASTER_SELLER_ID.getName(), metadata.masterSellerId());
        metadataXmp.setProperty(NS_JPEG, METADATA_ID.getName(), metadata.metadataId());
        metadataXmp.setProperty(NS_JPEG, ORIGIN_FILENAME.getName(), metadata.originFilename());
        metadataXmp.setProperty(NS_JPEG, OWN_TRADEMARK.getName(), metadata.ownTrademark());
        metadataXmp.setProperty(NS_JPEG, PRIORITY.getName(), metadata.priority());
        metadataXmp.setProperty(NS_JPEG, QUALITY_CODE.getName(), metadata.qualityCode());
        metadataXmp.setProperty(NS_JPEG, QUALITY_ID.getName(), metadata.qualityId());
        metadataXmp.setProperty(NS_JPEG, QUALITY_NAME.getName(), metadata.qualityName());
        metadataXmp.setProperty(NS_JPEG, RAW_IMAGE.getName(), metadata.rawImage());
        metadataXmp.setProperty(NS_JPEG, RECEIVED.getName(), metadata.received());
        metadataXmp.setProperty(NS_JPEG, SKU.getName(), metadata.sku());

        if (metadata.sourceId().equals(CAPITAL_NULL)) {
            metadataXmp.setProperty(NS_JPEG, SOURCE.getName(), metadata.sourceId());
        }
        else {
            metadataXmp.setProperty(NS_JPEG, SOURCE.getName() + "_code", metadata.sourceCode());
            metadataXmp.setProperty(NS_JPEG, SOURCE_ID.getName(), metadata.sourceId());
            metadataXmp.setProperty(NS_JPEG, SOURCE_NAME.getName(), metadata.sourceName());
        }

        metadataXmp.setProperty(NS_JPEG, STATUS_CODE.getName(), metadata.statusCode());
        metadataXmp.setProperty(NS_JPEG, STATUS_ID.getName(), metadata.statusId());
        metadataXmp.setProperty(NS_JPEG, STATUS_NAME.getName(), metadata.statusName());
        metadataXmp.setProperty(NS_JPEG, UPDATED_AT.getName(), metadata.updatedAt());
        metadataXmp.setProperty(NS_JPEG, UPDATED_BY.getName(), metadata.updatedBy());

        return metadataXmp;
    }

    @SneakyThrows
    @Step("Записываем XMP метадату в файл")
    public byte[] writeXMPMeta(byte[] bytes, ImageData image, XMPMeta xmpMeta, String downloadPath) {
        String fileName = createFileFromBytesArray(bytes, downloadPath, image.getFormat().getFormatName()) + DOT_VALUE + image.getFormat().getFormatName();
        XmpUtil.writeXMPMeta(downloadPath + fileName, xmpMeta);
        return Files.readAllBytes(Paths.get(downloadPath + fileName));
    }

    @SneakyThrows
    @Step("Получаем байты из файла для импорта '{filename}'")
    public File getImportFile(String filename) {
        return new File(IMAGES_IMPORT_PATH + filename);
    }

    @Step("Удаляем объект в БД")
    public void deleteValidations() {
        //todo Вернуть проверку на удачное удаление
        //ProcessingResponse response =
        sendDelete(baseUrl + VALIDATIONS, getAuthToken());
        //checkResponse(response, SC_NO_CONTENT);
    }

    @Step("Удаляем результаты загрузок")
    public void deleteImports() {
        //todo Вернуть проверку на удачное удаление
        //ProcessingResponse response =
        sendDelete(baseUrl + IMPORTS, getAuthToken());
        //checkResponse(response, SC_NO_CONTENT);
    }

    @Step("Удаляем медиафайлы из s3")
    public void deleteMedia() {
        //todo Вернуть проверку на удачное удаление
        //ProcessingResponse response =
        sendDelete(baseUrl + IMPORT, getAuthToken());
        //checkResponse(response, SC_NO_CONTENT);
    }

    public String importRandomImages(ImageData image) {
        return importRandomImages(image, null);
    }

    @Step("Загружаем сгенерированное изображение")
    public String importRandomImages(ImageData image, String importId) {
        //todo параметры устарели
        Map<String, Object> queryParams = new HashMap<String, Object>() {{
            put("handle", false);
            put("frame", false);
        }};

        if (importId != null) {
            queryParams.put("import_id", importId);
        }
        Response response = postImagesToUrl(baseUrl + IMPORT, queryParams, Collections.singletonList(image), getAuthToken());

        checkResponseAndJsonScheme(response, getJsonSchemaPath("successImportScheme"));

        compareParameters(image.getFilename(), getValueFromResponse(response, "data.filename"), "Название файла");
        return getValueFromResponse(response, "data.id");
    }

    @Step("Загружаем изображение с внешнего импорта {baseExternalUrl}")
    public Response externalImportImage(Object body, String baseExternalUrl, String externalToken) {
        Response response = postToUrl(body,
                baseExternalUrl + EXTERNAL_IMPORT,
                new Cookies(),
                Collections.singletonMap("Authorization", externalToken),
                null,
                ContentType.JSON);
        checkResponseAndJsonScheme(response, getJsonSchemaPath("successExternalImportScheme"));
        return response;
    }

    @Step("Загружаем изображение с ошибкой внешнего импорта {baseExternalUrl}")
    public List<InvalidParamsItem> unsuccessfulExternalImportImage(Object body, String baseExternalUrl, String externalToken) {
        Response response = postToUrl(body,
                baseExternalUrl + EXTERNAL_IMPORT,
                new Cookies(),
                Collections.singletonMap("Authorization", externalToken),
                null,
                ContentType.JSON);

        checkResponse(response, 400);
        return jsonParse(response, ErrorResponse.class).error().invalidParams();
    }

    public List<InvalidParamsItem> importInvalidRandomImages(ImageData image) {
        return importInvalidRandomImages(image, null);
    }

    @Step("Загружаем сгенерированное невалидное изображение")
    public List<InvalidParamsItem> importInvalidRandomImages(ImageData image, String importId) {
        Response response;
        if (importId == null) {
            response = postImagesToUrl(baseUrl + IMPORT, Collections.singletonList(image), getAuthToken());
        } else {
            response = postImagesToUrl(
                    baseUrl + IMPORT, Collections.singletonMap("import_id", importId), Collections.singletonList(image), getAuthToken());
        }

        checkResponse(response, 400);
        return jsonParse(response, ErrorResponse.class).error().invalidParams();
    }

    @Step("Загружаем сгенерированное невалидное изображение {filename}")
    public List<InvalidParamsItem> importInvalidRandomImages(String filename, byte[] image) {
        Response response = postImagesToUrl(baseUrl + IMPORT, Collections.singletonMap(filename, image), getAuthToken());
        checkResponse(response, 400);
        return jsonParse(response, ErrorResponse.class).error().invalidParams();
    }

    @Step("Загружаем изображения {filename}")
    public String importImages(String filename, byte[] image) {
        Response response = postImagesToUrl(baseUrl + IMPORT, Collections.singletonMap(filename, image), getAuthToken());
        checkResponse(response);
        return getValueFromResponse(response, "data.id");
    }

    @Deprecated
    public List<String> importImages(List<File> files, boolean handle, boolean frame) {
        Map<String, Boolean> queryParams = new HashMap<>() {{
            put("handle", handle);
            put("frame", frame);
        }};
        Map<String, byte[]> images = files.stream().collect(Collectors.toMap(File::getName, file -> {
            try {
                return Files.readAllBytes(file.toPath());
            } catch (IOException e) {
                log.error("Не удалось преобразовать изображение в байты");
                throw new RuntimeException("Не удалось преобразовать изображение в байты", e);
            }
        }));
        return importImages(images, queryParams);
    }

    @Deprecated
    public List<String> importImages(Map<String, byte[]> images, boolean handle, boolean frame) {
        Map<String, Boolean> queryParams = new HashMap<>() {{
            put("handle", handle);
            put("frame", frame);
        }};
        return importImages(images, queryParams);
    }

    public List<String> importFiles(List<File> files, List<ProcessingTypeEnum> processingTypeEnums) {
        Map<String, byte[]> images = files.stream().collect(Collectors.toMap(File::getName, file -> {
            try {
                return Files.readAllBytes(file.toPath());
            } catch (IOException e) {
                log.error("Не удалось преобразовать изображение в байты");
                throw new RuntimeException("Не удалось преобразовать изображение в байты", e);
            }
        }));
        return importImages(images, processingTypeEnums);
    }

    public List<String> importImages(ImageData image, List<ProcessingTypeEnum> processingTypeEnums) {
        return importImages(Collections.singletonList(image), processingTypeEnums);
    }

    public List<String> importImages(List<ImageData> imagesList, List<ProcessingTypeEnum> processingTypeEnums) {
        Map<String, byte[]> imagesMap = new HashMap<>();
        imagesList.forEach(image -> {
            byte[] bytes = getRandomByteImage(image.getWidth(), image.getHeight(), image.getFormat().getFormatName());
            imagesMap.put(image.getFilename(), bytes);
        });
        return importImages(imagesMap, processingTypeEnums);
    }

    public List<String> importImages(Map<String, byte[]> images) {
        return importImages(images, Collections.emptyMap());
    }

    public List<String> importImages(Map<String, byte[]> images, List<ProcessingTypeEnum> processingTypeEnums) {
        return importImages(images,
                Collections.singletonMap("processing_ids", processingTypeEnums.stream().map(ProcessingTypeEnum::getId).collect(Collectors.toList())));
    }

    @Step("Загружаем изображения")
    public List<String> importImages(Map<String, byte[]> images, Map<String, ?> queryParams) {
        List<String> ids = new ArrayList<>();
        images.forEach((k, v) -> {
                    Response response = postImagesToUrl(baseUrl + IMPORT, queryParams, Collections.singletonMap(k, v), getAuthToken());
                    checkResponse(response);
                    ids.add(getValueFromResponse(response, "data.id"));
                }
        );
        return ids;
    }

    @Step("Загружаем изображение с метадатой")
    public String importRandomImageWithBusinessMetadata(ImageData image, String metadataId) {
        Response response = postImagesToUrl(baseUrl + IMPORT,
                Collections.singletonMap("business_metadata_id", metadataId),
                Collections.singletonList(image), getAuthToken());
        checkResponse(response);
        return getValueFromResponse(response, "data.id");
    }

    @Step("Загружаем изображение с метадатой")
    public String importRandomImageWithBusinessMetadata(String filename, byte[] image, String metadataId) {
        Response response = postImagesToUrl(baseUrl + IMPORT,
                Collections.singletonMap("business_metadata_id", metadataId),
                Collections.singletonMap(filename, image), getAuthToken());
        checkResponse(response);
        return getValueFromResponse(response, "data.id");
    }

    @Step("Загружаем изображение с метадатой с ошибкой")
    public List<InvalidParamsItem> invalidImportRandomImageWithBusinessMetadata(ImageData image, String metadataId) {
        Response response = postImagesToUrl(baseUrl + IMPORT,
                Collections.singletonMap("business_metadata_id", metadataId),
                Collections.singletonList(image), getAuthToken());
        checkResponseAndJsonScheme(response, getJsonSchemaPath("unsuccessfulImportMetadataScheme"), SC_BAD_REQUEST);
        return jsonParse(response, ErrorResponse.class).error().invalidParams();
    }

    public String successImportBusinessMetadata(String parameter, Object value) {
        return successImportBusinessMetadata(Collections.singletonMap("data", Collections.singletonMap(parameter, value)));
    }

    @Step("Загружаем метадату изображения {metadata}")
    public String successImportBusinessMetadata(Object metadata) {
        Response response = postJsonToUrl(metadata, baseUrl + BUSINESS_METADATA, getAuthToken());
        checkResponse(response);
        return getValueFromResponse(response, "data.business_metadata_id");
    }

    public List<InvalidParamsItem> unsuccessfulImportBusinessMetadata(String parameter, Object value) {
        return unsuccessfulImportBusinessMetadata(Collections.singletonMap("data", Collections.singletonMap(parameter, value)));
    }

    @Step("Загружаем невалидную метадату изображения {metadata}")
    public List<InvalidParamsItem> unsuccessfulImportBusinessMetadata(Object metadata) {
        Response response = postJsonToUrl(metadata, baseUrl + BUSINESS_METADATA, getAuthToken());
        checkResponseAndJsonScheme(response, getJsonSchemaPath("unsuccessfulImportMetadataScheme"), SC_BAD_REQUEST);
        return jsonParse(response, ErrorResponse.class).error().invalidParams();
    }

    public List<DataItem> getValidationsDataItems() {
        return getValidationsDataItems(50);
    }

    @Step("Получаем данные валидации")
    public List<DataItem> getValidationsDataItems(Integer limit) {
        Response response = sendGet(baseUrl + VALIDATIONS,
                limit == null ? null : Collections.singletonMap("limit", limit), getAuthToken());
        checkResponse(response, SC_OK);
        return jsonParse(response, ValidationsResponse.class).data();
    }

    @Step("Скачиваем данные о валидации файлов")
    public byte[] downloadValidation(List<String> idList) {
        Response response = sendGet(baseUrl + VALIDATIONS_DOWNLOAD, Collections.singletonMap("ids", idList), getAuthToken());
        checkResponse(response, SC_OK);
        return response.asByteArray();
    }

    public ImportsListResponse getImports() {
        return getImports(Collections.emptyMap());
    }

    @Step("Получаем данные о загрузках")
    public ImportsListResponse getImports(Map<String, String> params) {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("max_page_size", "25");
        queryParams.putAll(params);
        Response response = sendGet(baseUrl + IMPORTS, queryParams, getAuthToken());
        checkResponse(response, SC_OK);
        return jsonParse(response, ImportsListResponse.class);
    }

    @Step("Получаем данные о неуспешной загрузках")
    public List<InvalidParamsItem> unsuccessfulGetImports(Map<String, String> params) {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("max_page_size", "25");
        queryParams.putAll(params);
        Response response = sendGet(baseUrl + IMPORTS, queryParams, getAuthToken());
        checkResponse(response, SC_BAD_REQUEST);
        return jsonParse(response, ErrorResponse.class).error().invalidParams();
    }

    public ImportsItemResponse getImportsItem(String importId) {
        return getImportsItem(importId, Collections.emptyMap());
    }

    @Step("Получаем данные о загрузке по {importId}")
    public ImportsItemResponse getImportsItem(String importId, Map<String, String> params) {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("max_page_size", "25");
        queryParams.putAll(params);
        Response response = sendGet(baseUrl + ImportServiceUrls.getImportsItem(importId), queryParams, getAuthToken());
        checkResponse(response, SC_OK);
        return jsonParse(response, ImportsItemResponse.class);
    }

    @Step("Получаем данные о загрузке по {importId}")
    public List<InvalidParamsItem> unsuccessfulGetImportsItem(String importId, Map<String, String> params) {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("max_page_size", "25");
        queryParams.putAll(params);
        Response response = sendGet(baseUrl + ImportServiceUrls.getImportsItem(importId), queryParams, getAuthToken());
        checkResponse(response, SC_BAD_REQUEST);
        return jsonParse(response, ErrorResponse.class).error().invalidParams();
    }

    @Step("Получаем id для импорта {totalPlan} изображений")
    public String successImportOpen(Object totalPlan) {
        RequestImportOpening requestImportOpening = new RequestImportOpening();
        requestImportOpening.totalPlanData(new TotalPlanData().totalPlan(totalPlan));

        Response response = postJsonToUrl(requestImportOpening, baseUrl + IMPORT_OPENING, getAuthToken());
        checkResponseAndJsonScheme(response, getJsonSchemaPath("successImportOpenScheme"));
        return getValueFromResponse(response, "data.import_id");
    }

    @Step("Ошибка получения id для импорта {totalPlan} изображений")
    public List<InvalidParamsItem> unsuccessfulImportOpen(Object totalPlan) {
        RequestImportOpening requestImportOpening = new RequestImportOpening();
        requestImportOpening.totalPlanData(new TotalPlanData().totalPlan(totalPlan));

        Response response = postJsonToUrl(requestImportOpening, baseUrl + IMPORT_OPENING, getAuthToken());
        checkResponseAndJsonScheme(response, getJsonSchemaPath("unsuccessfulImportMetadataScheme"), SC_BAD_REQUEST);
        return jsonParse(response, ErrorResponse.class).error().invalidParams();
    }

    @Step("Загружаем изображение с парсингом названия (приоритет и sku)")
    public String importRandomImageWithParseFilename(ImageData image, boolean parseFilename) {
        Response response = postImagesToUrl(baseUrl + IMPORT,
                Collections.singletonMap("parse_filename", parseFilename),
                Collections.singletonList(image), getAuthToken());
        checkResponse(response);
        return getValueFromResponse(response, "data.id");
    }

    @Step("Неуспешный импорт с парсингом (приоритет и sku)")
    public List<InvalidParamsItem> unsuccessfulImportRandomImageWithParseFilename(ImageData image, boolean parseFilename) {
        Response response = postImagesToUrl(baseUrl + IMPORT,
                Collections.singletonMap("parse_filename", parseFilename),
                Collections.singletonList(image), getAuthToken());
        checkResponseAndJsonScheme(response, getJsonSchemaPath("unsuccessfulImportMetadataScheme"), SC_BAD_REQUEST);
        return jsonParse(response, ErrorResponse.class).error().invalidParams();
    }

    @Step("Загружаем изображение с копированием метаданных")
    public String importImageWithCopyMetadata(ImageData image, boolean copyMetadata) {
        Response response = postImagesToUrl(baseUrl + IMPORT,
                Collections.singletonMap("copy_metadata", copyMetadata),
                Collections.singletonList(image), getAuthToken());
        checkResponse(response);
        return getValueFromResponse(response, "data.id");
    }

    @Step("Загружаем изображение копированием метаданных и бизнес-метаданными")
    public String importImageWithCopyMetadataAndBusinessMetadata(ImageData image, boolean copyMetadata, boolean parseFilename, String metadataId) {
        Map<String, Object> queryParams = new HashMap<String, Object>() {{
            put("copy_metadata", copyMetadata);
            put("parse_filename", parseFilename);
            if (metadataId != null)
                put("business_metadata_id", metadataId);
        }};

        Response response = postImagesToUrl(baseUrl + IMPORT, queryParams, Collections.singletonList(image), getAuthToken());
        checkResponse(response);
        return getValueFromResponse(response, "data.id");
    }
}

