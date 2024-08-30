package ru.spice.at.api.export_service;

import com.adobe.internal.xmp.XMPException;
import com.adobe.internal.xmp.XMPMeta;
import io.qameta.allure.Step;
import io.restassured.response.Response;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.awaitility.Awaitility;
import ru.spice.at.api.dto.response.export_service.ExportsDataItem;
import ru.spice.at.api.dto.response.export_service.ExportsResponse;
import ru.spice.at.api.dto.response.metadata.ErrorResponse;
import ru.spice.at.api.dto.response.metadata.InvalidParamsItem;
import ru.spice.at.common.base_test.AbstractApiStepDef;
import ru.spice.at.common.emuns.ApiServices;
import ru.spice.at.common.emuns.dam.ExportsStatus;
import ru.spice.at.common.utils.Assert;

import java.io.File;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static com.adobe.internal.xmp.XMPConst.NS_JPEG;
import static org.apache.http.HttpStatus.*;
import static ru.spice.at.api.urls.ExportServiceUrls.EXPORT;
import static ru.spice.at.api.urls.ExportServiceUrls.EXPORTS;
import static ru.spice.at.api.utils.ApiUtils.*;
import static ru.spice.at.common.constants.TestConstants.*;
import static ru.spice.at.common.constants.TestConstants.CAPITAL_NULL;
import static ru.spice.at.common.emuns.dam.ImageParameters.*;
import static ru.spice.at.common.emuns.dam.ImageParameters.UPDATED_BY;
import static ru.spice.at.common.emuns.dam.Quality.GOOD;
import static ru.spice.at.common.emuns.dam.Status.NEW;
import static ru.spice.at.common.utils.Assert.compareParameters;
import static ru.spice.at.common.utils.Assert.notNullOrEmptyParameter;
import static ru.spice.at.common.utils.FileHelper.*;
import static ru.spice.at.common.utils.JsonHelper.getValueFromResponse;
import static ru.spice.at.common.utils.JsonHelper.jsonParse;

@Log4j2
public class ExportServiceStepDef extends AbstractApiStepDef {
    private static final String IMAGES_EXPORT_PATH = "src/test/resources/images/export/";

    public ExportServiceStepDef() {
        super(ApiServices.EXPORT_SERVICE);
    }

    public ExportServiceStepDef(String token) {
        super(ApiServices.EXPORT_SERVICE, token);
    }

    @SneakyThrows
    @Step("Получаем байты из файла для экспорта '{filename}'")
    public File getExportFile(String filename) {
        return new File(IMAGES_EXPORT_PATH + filename);
    }

    public byte[] exportImage(String id) {
        return exportImage(Collections.singletonList(id), null);
    }

    public byte[] exportImage(List<String> idList) {
        return exportImage(idList, null);
    }

    @Step("Выгружаем из файлы с key: {idList}")
    public byte[] exportImage(List<String> idList, String archiveFilename) {
        Response response = postJsonToUrlWithoutHeaders(Collections.singletonMap("data", idList), baseUrl + EXPORT,
                archiveFilename != null ? Collections.singletonMap("filename", archiveFilename) : null, getAuthToken());
        checkResponse(response, idList.size() == 1 ? SC_OK : SC_ACCEPTED);
        return response.asByteArray();
    }

    @Step("Выгружаем из файлы с ошибкой: {idList}")
    public List<InvalidParamsItem> unsuccessfulExportImage(List<String> idList) {
        Response response = postJsonToUrl(Collections.singletonMap("data", idList), baseUrl + EXPORT, getAuthToken());
        checkResponseAndJsonScheme(response, getJsonSchemaPath("unsuccessfulExportScheme"), SC_BAD_REQUEST);
        return jsonParse(response, ErrorResponse.class).error().invalidParams();
    }

    @Step("Выгружаем из файлы с ошибкой с названием архива 'archiveFilename': {idList}")
    public List<InvalidParamsItem> unsuccessfulExportImage(List<String> idList, String archiveFilename) {
        Response response = postJsonToUrlWithoutHeaders(Collections.singletonMap("data", idList), baseUrl + EXPORT,
                Collections.singletonMap("filename", archiveFilename), getAuthToken());
        checkResponseAndJsonScheme(response, getJsonSchemaPath("unsuccessfulExportScheme"), SC_BAD_REQUEST);
        return jsonParse(response, ErrorResponse.class).error().invalidParams();
    }

    @Step("Получаем архив со из внешней ссылки {storageUrl}")
    public byte[] getExportStorageArchiveImage(String storageUrl) {
        Response response = sendGet(storageUrl.split("\\?")[0]);
        checkResponse(response);
        return response.asByteArray();
    }

    @Step("Ожидаем экспорт (верхний в списке) с параметрами: название - {archiveName}, " +
            "статус - {statusName}, количество изображений - {count}")
    public String successWaitExportsWithParameters(String archiveName, ExportsStatus statusName, int count) {
        AtomicReference<List<ExportsDataItem>> exportsDataAtomic = new AtomicReference<>();
        Awaitility.await().atMost(10, TimeUnit.SECONDS).pollInterval(1, TimeUnit.SECONDS).until(() ->
        {
            exportsDataAtomic.set(successGetExports());
            ExportsDataItem exportsDataItem = exportsDataAtomic.get().get(0);
            boolean checkStatus = statusName == null || exportsDataItem.status().name().equals(statusName.getName()) && exportsDataItem.url() != null;
            return checkStatus && exportsDataItem.filename().equals(archiveName) && exportsDataItem.planMetadataIdsCount() == count;
        });
        return statusName != null ? exportsDataAtomic.get().get(0).url() : null;
    }

    public List<ExportsDataItem> successGetExports() {
        return successGetExports(null);
    }

    @Step("Получаем список экспортов")
    public List<ExportsDataItem> successGetExports(String exportData) {
        Response response = sendGet( baseUrl + EXPORTS, exportData != null ? Collections.singletonMap("export_data", exportData) : null, getAuthToken());
        checkResponse(response);
        return jsonParse(response, ExportsResponse.class).data();
    }

    @Step("Неуспешно получаем список экспортов")
    public List<InvalidParamsItem> unsuccessfulGetExports(String exportData) {
        Response response = sendGet( baseUrl + EXPORTS, exportData != null ? Collections.singletonMap("export_data", exportData) : null, getAuthToken());
        checkResponse(response, 400);
        return jsonParse(response, ErrorResponse.class).error().invalidParams();
    }

    @Step("Сравниваем полученные файлы из архива с загруженными")
    public void checkFiles(byte[] bytesZip, Map<String, byte[]> images, String path) {
        String name = createFileFromBytesArray(bytesZip, path, "zip");
        String extractFolder = extractZip(name, path);

        images.forEach((k, v) -> {
            byte[] resultBytes = covertFileToBytes(k, extractFolder);
            Assert.notNullOrEmptyParameter(resultBytes.length, "байты");
        });
    }

    @Step("Получаем байты файлов из байтов zip архива")
    public List<byte[]> getFilesBytesFromZip(byte[] bytesZip, String path, List<String> names) {
        String name = createFileFromBytesArray(bytesZip, path, "zip");
        String extractFolder = extractZip(name, path);
        return names.stream().map(n -> covertFileToBytes(n, extractFolder)).collect(Collectors.toList());
    }

    public void checkXMP(XMPMeta metadataXmp, Response metadataResponse, boolean allFields) {
        checkXMP(metadataXmp, metadataResponse, NS_JPEG, allFields);
    }

    @Step("Проверка полученного XMP")
    public void checkXMP(XMPMeta metadataXmp, Response metadataResponse, String xmpConst, boolean allFields) {
        try {
            compareParameters(
                    allFields ? getValueFromResponse(metadataResponse, ASSIGNEE_ID.getPath() + ".id") : CAPITAL_NULL,
                    metadataXmp.getProperty(xmpConst, ASSIGNEE_ID.getName()).getValue(), "assignee_id");
            compareParameters(CAPITAL_FALSE, metadataXmp.getProperty(xmpConst, COPYRIGHT.getName()).getValue(), "copyright");
            compareParameters(
                    getValueFromResponse(metadataResponse, CREATED_AT.getPath()),
                    metadataXmp.getProperty(xmpConst, CREATED_AT.getName()).getValue(), "created_at");
            compareParameters(
                    getValueFromResponse(metadataResponse, CREATED_BY.getPath() + ".id"),
                    metadataXmp.getProperty(xmpConst, CREATED_BY.getName()).getValue(), "created_by");
            compareParameters(
                    allFields ? getValueFromResponse(metadataResponse, DESCRIPTION.getPath()) : CAPITAL_NULL,
                    metadataXmp.getProperty(xmpConst, DESCRIPTION.getName()).getValue(), "description");
            compareParameters(CAPITAL_NULL, metadataXmp.getProperty(xmpConst, EXTERNAL_DRAFT_DONE.getName()).getValue(), "external_draft_done");
            compareParameters(CAPITAL_NULL, metadataXmp.getProperty(xmpConst, EXTERNAL_OFFER.getName() + "_name").getValue(), "external_offer");
            compareParameters(
                    allFields ? getValueFromResponse(metadataResponse, EXTERNAL_TASK_ID.getPath()) : CAPITAL_NULL,
                    metadataXmp.getProperty(xmpConst, EXTERNAL_TASK_ID.getName()).getValue(), "external_task_id");
            compareParameters(
                    getValueFromResponse(metadataResponse, FILENAME.getPath()),
                    metadataXmp.getProperty(xmpConst, FILENAME.getName()).getValue(), "filename");
            compareParameters("0", metadataXmp.getProperty(xmpConst, IMPORT_TYPE.getName() + "_code").getValue(), "import_type_code");
            compareParameters(
                    getValueFromResponse(metadataResponse, IMPORT_TYPE.getPath() + ".id"),
                    metadataXmp.getProperty(xmpConst, IMPORT_TYPE.getName() + "_id").getValue(), "import_type_id");
            compareParameters("manual", metadataXmp.getProperty(xmpConst, IMPORT_TYPE.getName() + "_name").getValue(), "import_type_name");
            compareParameters(
                    getValueFromResponse(metadataResponse, KEY.getPath()),
                    metadataXmp.getProperty(xmpConst, KEY.getName()).getValue(), "key");
            compareParameters(
                    allFields ? ((List<?>)getValueFromResponse(metadataResponse, KEYWORDS.getPath())).size() : CAPITAL_NULL,
                    allFields ? metadataXmp.countArrayItems(xmpConst, KEYWORDS.getName()) : metadataXmp.getProperty(xmpConst, KEYWORDS.getName()).getValue(), "keywords");
            if (allFields) {
                compareParameters(
                        getValueFromResponse(metadataResponse, MASTER_CATEGORY_ID.getPath() + ".id"),
                        metadataXmp.getProperty(xmpConst, MASTER_CATEGORY_ID.getName()).getValue(), "master_category_id");
                compareParameters(
                        getValueFromResponse(metadataResponse, MASTER_CATEGORY_ID.getPath() + ".name"),
                        metadataXmp.getProperty(xmpConst, MASTER_CATEGORY_NAME.getName()).getValue(), "master_category_name");
            } else {
                compareParameters(CAPITAL_NULL, metadataXmp.getProperty(xmpConst, MASTER_CATEGORY.getName()).getValue(), "master_category");
            }
            compareParameters(CAPITAL_NULL, metadataXmp.getProperty(xmpConst, MASTER_SELLER_ID.getName()).getValue(), "master_seller_id");
            compareParameters(
                    getValueFromResponse(metadataResponse, METADATA_ID.getPath()),
                    metadataXmp.getProperty(xmpConst, METADATA_ID.getName()).getValue(), "metadata_id");
            compareParameters(
                    getValueFromResponse(metadataResponse, ORIGIN_FILENAME.getPath()),
                    metadataXmp.getProperty(xmpConst, ORIGIN_FILENAME.getName()).getValue(), "origin_filename");
            compareParameters(CAPITAL_FALSE, metadataXmp.getProperty(xmpConst, OWN_TRADEMARK.getName()).getValue(), "own_trademark");
            compareParameters(
                    allFields ? getValueFromResponse(metadataResponse, PRIORITY.getPath()).toString() : CAPITAL_NULL,
                    metadataXmp.getProperty(xmpConst, PRIORITY.getName()).getValue(), "priority");
            compareParameters("1", metadataXmp.getProperty(xmpConst, QUALITY_CODE.getName()).getValue(), "quality_code");
            compareParameters(
                    getValueFromResponse(metadataResponse, QUALITY_ID.getPath() + ".id"),
                    metadataXmp.getProperty(xmpConst, QUALITY_ID.getName()).getValue(), "quality_id");
            compareParameters(GOOD.toString().toLowerCase(), metadataXmp.getProperty(xmpConst, QUALITY_NAME.getName()).getValue(), "quality_code");
            compareParameters(CAPITAL_TRUE, metadataXmp.getProperty(xmpConst, RAW_IMAGE.getName()).getValue(), "raw_image");
            compareParameters(
                    allFields ? getValueFromResponse(metadataResponse, RECEIVED.getPath()) : CAPITAL_NULL,
                    metadataXmp.getProperty(xmpConst, RECEIVED.getName()).getValue(), "received");
            compareParameters(
                    allFields ? getValueFromResponse(metadataResponse, SKU.getPath()) : CAPITAL_NULL,
                    metadataXmp.getProperty(xmpConst, SKU.getName()).getValue(), "sku");
            if (allFields) {
                compareParameters(
                        getValueFromResponse(metadataResponse, SOURCE_ID.getPath() + ".id"),
                        metadataXmp.getProperty(xmpConst, SOURCE_ID.getName()).getValue(), "source_id");
                notNullOrEmptyParameter(metadataXmp.getProperty(xmpConst, SOURCE_NAME.getName()).getValue(), "source_name");
            } else {
                compareParameters(CAPITAL_NULL, metadataXmp.getProperty(xmpConst, SOURCE.getName()).getValue(), "source");
            }
            compareParameters("0", metadataXmp.getProperty(xmpConst, STATUS_CODE.getName()).getValue(), "status_code");
            compareParameters(
                    getValueFromResponse(metadataResponse, STATUS_ID.getPath() + ".id"),
                    metadataXmp.getProperty(xmpConst, STATUS_ID.getName()).getValue(), "status_id");
            compareParameters(NEW.toString().toLowerCase(), metadataXmp.getProperty(xmpConst, STATUS_NAME.getName()).getValue(), "status_name");
            compareParameters(
                    getValueFromResponse(metadataResponse, UPDATED_AT.getPath()),
                    metadataXmp.getProperty(xmpConst, UPDATED_AT.getName()).getValue(), "updated_at");
            compareParameters(
                    getValueFromResponse(metadataResponse, UPDATED_BY.getPath() + ".id"),
                    metadataXmp.getProperty(xmpConst, UPDATED_BY.getName()).getValue(), "updated_by");
        } catch (XMPException e) {
            log.error("Ошибка, в XMP не найден параметр");
            throw new RuntimeException("Ошибка, в XMP не найден параметр", e);
        }
    }
}
