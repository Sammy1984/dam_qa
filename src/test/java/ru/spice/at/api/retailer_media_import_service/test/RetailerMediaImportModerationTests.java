package ru.spice.at.api.retailer_media_import_service.test;

import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import io.restassured.response.Response;
import org.apache.commons.lang3.RandomStringUtils;
import org.awaitility.Awaitility;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import ru.spice.at.api.dto.response.metadata.DictionariesItem;
import ru.spice.at.api.dto.response.metadata.filtration.DataItem;
import ru.spice.at.api.metadata_service.MetadataStepDef;
import ru.spice.at.api.retailer_media_import_service.RetailerMediaImportSettings;
import ru.spice.at.api.retailer_media_import_service.RetailerMediaImportStepDef;
import ru.spice.at.common.base_test.BaseApiTest;
import ru.spice.at.common.emuns.ApiServices;
import ru.spice.at.common.emuns.dam.OfferProcessStatusEnum;
import ru.spice.at.common.emuns.dam.OfferProcessTypeEnum;
import ru.spice.at.common.emuns.dam.Status;
import ru.testit.annotations.WorkItemIds;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static ru.spice.at.common.constants.TestConstants.*;
import static ru.spice.at.common.emuns.dam.ImageFormat.JPG;
import static ru.spice.at.common.emuns.dam.ImageParameters.*;
import static ru.spice.at.common.utils.Assert.*;
import static ru.spice.at.common.utils.JsonHelper.getValueFromResponse;

/**
 * В соответствии с SPC-3307 https://wiki.sbmt.io/pages/viewpage.action?pageId=3193659514
 */
@Feature("Retailer Media Import Service (RMIS)")
@Story("PIMS - Import metadata moderation products")
public class RetailerMediaImportModerationTests extends BaseApiTest<RetailerMediaImportSettings> {
    private final RetailerMediaImportStepDef retailerMediaImportStepDef;
    private final MetadataStepDef metadataStepDef;

    private String actualStatusId;
    private RetailerMediaImportSettings.ImportParameters importParameters;
    private List<DataItem> dataItems;

    protected RetailerMediaImportModerationTests() {
        super(ApiServices.RETAILER_MEDIA_IMPORT_SERVICE);
        retailerMediaImportStepDef = new RetailerMediaImportStepDef();
        metadataStepDef = new MetadataStepDef();
    }

    @BeforeClass(alwaysRun = true)
    public void beforeClass() {
        actualStatusId = metadataStepDef.getListStatusesMetadata().stream().filter(item -> item.name().equals(Status.ACTUAL.getName())).
                map(DictionariesItem::id).findFirst().orElse(null);
    }

    @BeforeMethod(alwaysRun = true)
    public void beforeMethod() {
        String mediaName = RandomStringUtils.randomAlphabetic(10) + DOT_VALUE + JPG.getFormatName();
        Integer externalOfferId = new Random().nextInt(1000000) + 1000000;
        Integer masterSellerId = new Random().nextInt(100) + 1;

        importParameters = getData().importParameters().clone().
                mediaName(mediaName).masterSellerId(masterSellerId).externalOfferId(externalOfferId.toString());
        retailerMediaImportStepDef.sendMediaFile(retailerMediaImportStepDef.buildImportOneMediaRequest(importParameters));

        AtomicReference<List<DataItem>> dataItemsAtomic = new AtomicReference<>();
        Awaitility.await().atMost(10, TimeUnit.SECONDS).pollInterval(1, TimeUnit.SECONDS).until(() ->
        {
            List<DataItem> dataItems = metadataStepDef.successMetadataSearch(externalOfferId);
            dataItemsAtomic.set(dataItems);
            return dataItems.size() == 2;
        });

        dataItems = dataItemsAtomic.get();
    }

    //todo выключено для использования параллельного сьюта
    //@AfterClass(alwaysRun = true)
    public void afterClass() {
        metadataStepDef.deleteMetadata();
    }

    //todo на удаление, изменилась логика
    //@Test(description = "Успешный импорт метадаты по процессу CREATE_TASK", timeOut = 600000, groups = {"regress", "kafka"})
    @WorkItemIds({"288867"})
    public void successImportMetadataCreateTaskTest() {
        RetailerMediaImportSettings.ImportMetadata importMetadata = getData().importMetadata().clone().sku(null);
        String metadataRequest = retailerMediaImportStepDef.buildImportModerationMetadataRequest(
                Integer.parseInt(importParameters.externalOfferId()), importParameters.masterSellerId(), OfferProcessTypeEnum.CREATE_TASK, importMetadata);
        retailerMediaImportStepDef.sendMetadata(metadataRequest);

        dataItems.stream().map(DataItem::getId).forEach(id -> {
            AtomicReference<Response> response = new AtomicReference<>();
            Awaitility.await().atMost(10, TimeUnit.SECONDS).pollInterval(1, TimeUnit.SECONDS).until(() ->
            {
                response.set(metadataStepDef.checkMetadata(id));
                Object masterCategory = getValueFromResponse(response.get(), MASTER_CATEGORY.getPath());
                return masterCategory != null;
            });

            assertAll(
                    () -> compareParameters(Integer.parseInt(importParameters.externalOfferId()), getValueFromResponse(response.get(), EXTERNAL_OFFER.getPath() + ".id"), "external_offer.id"),
                    () -> compareParameters(importParameters.masterSellerId(), getValueFromResponse(response.get(), MASTER_SELLER.getPath() + ".ext_id"), "master_seller_id"),
                    () -> compareParameters(getData().unknownCategory(), getValueFromResponse(response.get(), MASTER_CATEGORY.getPath() + ".name"), "master_category"),
                    () -> compareParameters(importMetadata.taskIdForDam().toString(), getValueFromResponse(response.get(), EXTERNAL_TASK_ID.getPath()), "external_task_id"),
                    () -> mustBeNullParameter(getValueFromResponse(response.get(), SKU.getPath()), "sku"),
                    () -> notEquals(getValueFromResponse(response.get(), CREATED_AT.getPath()), getValueFromResponse(response.get(), UPDATED_AT.getPath()), "updated_at"),
                    () -> compareParameters(importMetadata.isOwnTrademark(), getValueFromResponse(response.get(), IS_OWN_TRADEMARK.getPath()), "is_own_trademark")
            );
        });
    }

    //todo на удаление, изменилась логика
    //@Test(description = "Успешный импорт метадаты по процессу CREATE_TASK (sku не null)", timeOut = 600000, groups = {"regress", "kafka"})
    @WorkItemIds({"288867"})
    public void successImportMetadataCreateTaskNotNullSkuTest() {
        RetailerMediaImportSettings.ImportMetadata importMetadata = getData().importMetadata().clone();
        String metadataRequest = retailerMediaImportStepDef.buildImportModerationMetadataRequest(
                Integer.parseInt(importParameters.externalOfferId()), importParameters.masterSellerId(), OfferProcessTypeEnum.CREATE_TASK, importMetadata);
        retailerMediaImportStepDef.sendMetadata(metadataRequest);

        dataItems.stream().map(DataItem::getId).forEach(id -> {
            AtomicReference<Response> response = new AtomicReference<>();
            Awaitility.await().atMost(10, TimeUnit.SECONDS).pollInterval(1, TimeUnit.SECONDS).until(() ->
            {
                response.set(metadataStepDef.checkMetadata(id));
                Object masterCategory = getValueFromResponse(response.get(), MASTER_CATEGORY.getPath());
                return masterCategory != null;
            });

            assertAll(
                    () -> compareParameters(Integer.parseInt(importParameters.externalOfferId()), getValueFromResponse(response.get(), EXTERNAL_OFFER.getPath() + ".id"), "external_offer.id"),
                    () -> compareParameters(importParameters.masterSellerId(), getValueFromResponse(response.get(), MASTER_SELLER.getPath() + ".ext_id"), "master_seller_id"),
                    () -> compareParameters(getData().unknownCategory(), getValueFromResponse(response.get(), MASTER_CATEGORY.getPath() + ".name"), "master_category"),
                    () -> compareParameters(importMetadata.taskIdForDam().toString(), getValueFromResponse(response.get(), EXTERNAL_TASK_ID.getPath()), "external_task_id"),
                    () -> mustBeNullParameter(getValueFromResponse(response.get(), SKU.getPath()), "sku"),
                    () -> notEquals(getValueFromResponse(response.get(), CREATED_AT.getPath()), getValueFromResponse(response.get(), UPDATED_AT.getPath()), "updated_at"),
                    () -> compareParameters(importMetadata.isOwnTrademark(), getValueFromResponse(response.get(), IS_OWN_TRADEMARK.getPath()), "is_own_trademark")
            );
        });
    }

    //todo на удаление, изменилась логика
    //@Test(description = "Успешный импорт метадаты по процессу CREATE_TASK (незаполненные параметры)", timeOut = 600000, groups = {"regress", "kafka"})
    @WorkItemIds({"288867"})
    public void successImportMetadataCreateTaskNoParametersTest() {
        RetailerMediaImportSettings.ImportMetadata importMetadata = getData().importMetadata().clone().
                sku(null).taskIdForDam(null);
        String metadataRequest = retailerMediaImportStepDef.buildImportModerationMetadataRequest(
                Integer.parseInt(importParameters.externalOfferId()), importParameters.masterSellerId(), OfferProcessTypeEnum.CREATE_TASK, importMetadata);
        retailerMediaImportStepDef.sendMetadata(metadataRequest);

        dataItems.stream().map(DataItem::getId).forEach(id -> {
            AtomicReference<Response> response = new AtomicReference<>();
            Awaitility.await().atMost(10, TimeUnit.SECONDS).pollInterval(1, TimeUnit.SECONDS).until(() ->
            {
                response.set(metadataStepDef.checkMetadata(id));
                Boolean isOwnTrademark = getValueFromResponse(response.get(), IS_OWN_TRADEMARK.getPath());
                return isOwnTrademark != null && isOwnTrademark.equals(importMetadata.isOwnTrademark());
            });

            assertAll(
                    () -> compareParameters(Integer.parseInt(importParameters.externalOfferId()), getValueFromResponse(response.get(), EXTERNAL_OFFER.getPath() + ".id"), "external_offer.id"),
                    () -> compareParameters(importParameters.masterSellerId(), getValueFromResponse(response.get(), MASTER_SELLER.getPath() + ".ext_id"), "master_seller_id"),
                    () -> mustBeNullParameter(getValueFromResponse(response.get(), EXTERNAL_TASK_ID.getPath()), "external_task_id"),
                    () -> mustBeNullParameter(getValueFromResponse(response.get(), SKU.getPath()), "sku"),
                    () -> notEquals(getValueFromResponse(response.get(), CREATED_AT.getPath()), getValueFromResponse(response.get(), UPDATED_AT.getPath()), "updated_at"),
                    () -> compareParameters(importMetadata.isOwnTrademark(), getValueFromResponse(response.get(), IS_OWN_TRADEMARK.getPath()), "is_own_trademark")
            );
        });
    }

    //todo на удаление, изменилась логика
    //@Test(description = "Успешный импорт метадаты по процессу CREATE_TASK - Актуальный (оригинал и производное)", timeOut = 600000, groups = {"regress", "kafka"})
    @WorkItemIds({"288868"})
    public void successImportMetadataCreateTaskActualTest() {
        dataItems.forEach(dataItem -> {
            Map<String, Object> editValues = new HashMap<>() {{
                put(SKU.getName(), RandomStringUtils.randomNumeric(3));
                put(PRIORITY.getName(), new Random().nextInt(NINETY_NINE) + 1);
                put(STATUS_ID.getName(), actualStatusId);
            }};
            metadataStepDef.successEditMetadata(dataItem.getId(), editValues);
        });

        RetailerMediaImportSettings.ImportMetadata importMetadata = getData().importMetadata().clone().sku(null);
        String metadataRequest = retailerMediaImportStepDef.buildImportModerationMetadataRequest(
                Integer.parseInt(importParameters.externalOfferId()), importParameters.masterSellerId(), OfferProcessTypeEnum.CREATE_TASK, importMetadata);
        retailerMediaImportStepDef.sendMetadata(metadataRequest);

        dataItems.stream().map(DataItem::getId).forEach(id -> {
            Response response = metadataStepDef.checkMetadata(id);

            assertAll(
                    () -> compareParameters(Integer.parseInt(importParameters.externalOfferId()), getValueFromResponse(response, EXTERNAL_OFFER.getPath() + ".id"), "external_offer.id"),
                    () -> compareParameters(importParameters.masterSellerId(), getValueFromResponse(response, MASTER_SELLER.getPath() + ".ext_id"), "master_seller_id"),
                    () -> mustBeNullParameter(getValueFromResponse(response, MASTER_CATEGORY.getPath()), "master_category"),
                    () -> mustBeNullParameter(getValueFromResponse(response, EXTERNAL_TASK_ID.getPath()), "external_task_id"),
                    () -> compareParameters(!importMetadata.isOwnTrademark(), getValueFromResponse(response, IS_OWN_TRADEMARK.getPath()), "is_own_trademark")
            );
        });
    }

    //todo на удаление, изменилась логика
    //@Test(description = "Успешный импорт метадаты по процессу CREATE_TASK - Актуальный (производное)", timeOut = 600000, groups = {"regress", "kafka"})
    @WorkItemIds({"288868"})
    public void successImportMetadataCreateTaskActualRelatedTest() {
        Map<String, Object> editValues = new HashMap<>() {{
            put(SKU.getName(), RandomStringUtils.randomNumeric(3));
            put(PRIORITY.getName(), new Random().nextInt(NINETY_NINE) + 1);
            put(STATUS_ID.getName(), actualStatusId);
        }};

        metadataStepDef.successEditMetadata(dataItems.get(0).getId(), editValues);

        RetailerMediaImportSettings.ImportMetadata importMetadata = getData().importMetadata().clone().sku(null);
        String metadataRequest = retailerMediaImportStepDef.buildImportModerationMetadataRequest(
                Integer.parseInt(importParameters.externalOfferId()), importParameters.masterSellerId(), OfferProcessTypeEnum.CREATE_TASK, importMetadata);
        retailerMediaImportStepDef.sendMetadata(metadataRequest);

        dataItems.stream().map(DataItem::getId).forEach(id -> {
            Response response = metadataStepDef.checkMetadata(id);

            assertAll(
                    () -> compareParameters(Integer.parseInt(importParameters.externalOfferId()), getValueFromResponse(response, EXTERNAL_OFFER.getPath() + ".id"), "external_offer.id"),
                    () -> compareParameters(importParameters.masterSellerId(), getValueFromResponse(response, MASTER_SELLER.getPath() + ".ext_id"), "master_seller_id"),
                    () -> mustBeNullParameter(getValueFromResponse(response, MASTER_CATEGORY.getPath()), "master_category"),
                    () -> mustBeNullParameter(getValueFromResponse(response, EXTERNAL_TASK_ID.getPath()), "external_task_id"),
                    () -> compareParameters(!importMetadata.isOwnTrademark(), getValueFromResponse(response, IS_OWN_TRADEMARK.getPath()), "is_own_trademark")
            );
        });
    }

    @Test(description = "Успешный импорт метадаты по процессу FINALYZE_TASK статус BINDED", timeOut = 600000, groups = {"regress", "kafka"})
    @WorkItemIds({"288870"})
    public void successImportMetadataFinalyzeTaskBindedTest() {
        RetailerMediaImportSettings.ImportMetadata importMetadata = getData().importMetadata().clone().
                sku(1 + new Random().nextInt(100));
        String metadataRequest = retailerMediaImportStepDef.buildImportModerationMetadataRequest(
                Integer.parseInt(importParameters.externalOfferId()), importParameters.masterSellerId(), OfferProcessTypeEnum.FINALYZE_TASK, importMetadata);
        retailerMediaImportStepDef.sendMetadata(metadataRequest);

        dataItems.stream().map(DataItem::getId).forEach(id -> {
            AtomicReference<Response> response = new AtomicReference<>();
            Awaitility.await().atMost(10, TimeUnit.SECONDS).pollInterval(1, TimeUnit.SECONDS).until(() ->
            {
                response.set(metadataStepDef.checkMetadata(id));
                Object masterCategory = getValueFromResponse(response.get(), MASTER_CATEGORY.getPath());
                return masterCategory != null;
            });

            assertAll(
                    () -> compareParameters(Integer.parseInt(importParameters.externalOfferId()), getValueFromResponse(response.get(), EXTERNAL_OFFER.getPath() + ".id"), "external_offer.id"),
                    () -> compareParameters(importParameters.masterSellerId(), getValueFromResponse(response.get(), MASTER_SELLER.getPath() + ".ext_id"), "master_seller_id"),
                    () -> compareParameters(getData().unknownCategory(), getValueFromResponse(response.get(), MASTER_CATEGORY.getPath() + ".name"), "master_category"),
                    () -> compareParameters(importMetadata.taskIdForDam().toString(), getValueFromResponse(response.get(), EXTERNAL_TASK_ID.getPath()), "external_task_id"),
                    () -> compareParameters(
                            String.format(FILENAME_MASK, importMetadata.sku(), 1, JPG.getFormatName()),
                            getValueFromResponse(response.get(), FILENAME.getPath()),
                            "filename"),
                    () -> compareParameters(1, getValueFromResponse(response.get(), PRIORITY.getPath()), "priority"),
                    () -> compareParameters(importMetadata.sku().toString(), getValueFromResponse(response.get(), SKU.getPath()), "sku"),
                    () -> notEquals(getValueFromResponse(response.get(), CREATED_AT.getPath()), getValueFromResponse(response.get(), UPDATED_AT.getPath()), "updated_at"),
                    () -> compareParameters(getData().readyForRetouchAt(), getValueFromResponse(response.get(), READY_FOR_RETOUCH_AT.getPath()), "ready_for_retouch_at"),
                    () -> compareParameters(importMetadata.isOwnTrademark(), getValueFromResponse(response.get(), IS_OWN_TRADEMARK.getPath()), "is_own_trademark")
            );
        });
    }

    @Test(description = "Успешный импорт метадаты по процессу FINALYZE_TASK статус BINDED (нет Sku)", timeOut = 600000, groups = {"regress", "kafka"})
    @WorkItemIds({"288871"})
    public void successImportMetadataFinalyzeTaskBindedNoSkuTest() {
        RetailerMediaImportSettings.ImportMetadata importMetadata = getData().importMetadata().clone().
                sku(null);
        String metadataRequest = retailerMediaImportStepDef.buildImportModerationMetadataRequest(
                Integer.parseInt(importParameters.externalOfferId()), importParameters.masterSellerId(), OfferProcessTypeEnum.FINALYZE_TASK, importMetadata);
        retailerMediaImportStepDef.sendMetadata(metadataRequest);

        dataItems.forEach(dataItem -> {
            String id = dataItem.getId();
            AtomicReference<Response> response = new AtomicReference<>();
            Awaitility.await().atMost(10, TimeUnit.SECONDS).pollInterval(1, TimeUnit.SECONDS).until(() ->
            {
                response.set(metadataStepDef.checkMetadata(id));
                Object masterCategory = getValueFromResponse(response.get(), MASTER_CATEGORY.getPath());
                return masterCategory != null;
            });

            assertAll(
                    () -> compareParameters(Integer.parseInt(importParameters.externalOfferId()), getValueFromResponse(response.get(), EXTERNAL_OFFER.getPath() + ".id"), "external_offer.id"),
                    () -> compareParameters(importParameters.masterSellerId(), getValueFromResponse(response.get(), MASTER_SELLER.getPath() + ".ext_id"), "master_seller_id"),
                    () -> compareParameters(getData().unknownCategory(), getValueFromResponse(response.get(), MASTER_CATEGORY.getPath() + ".name"), "master_category"),
                    () -> compareParameters(importMetadata.taskIdForDam().toString(), getValueFromResponse(response.get(), EXTERNAL_TASK_ID.getPath()), "external_task_id"),
                    () -> compareParameters(
                            dataItem.getFilename(),
                            getValueFromResponse(response.get(), FILENAME.getPath()),
                            "filename"),
                    () -> compareParameters(1, getValueFromResponse(response.get(), PRIORITY.getPath()), "priority"),
                    () -> mustBeNullParameter(getValueFromResponse(response.get(), SKU.getPath()), "sku"),
                    () -> notEquals(getValueFromResponse(response.get(), CREATED_AT.getPath()), getValueFromResponse(response.get(), UPDATED_AT.getPath()), "updated_at"),
                    () -> compareParameters(getData().readyForRetouchAt(), getValueFromResponse(response.get(), READY_FOR_RETOUCH_AT.getPath()), "ready_for_retouch_at"),
                    () -> compareParameters(importMetadata.isOwnTrademark(), getValueFromResponse(response.get(), IS_OWN_TRADEMARK.getPath()), "is_own_trademark")
            );
        });
    }

    @Test(description = "Успешный импорт метадаты по процессу FINALYZE_TASK статус НЕ BINDED", timeOut = 600000, groups = {"regress", "kafka"})
    @WorkItemIds({"288872"})
    public void successImportMetadataFinalyzeTaskNotBindedTest() {
        RetailerMediaImportSettings.ImportMetadata importMetadata = getData().importMetadata().clone().
                sku(1 + new Random().nextInt(100));
        String metadataRequest = retailerMediaImportStepDef.buildImportModerationMetadataRequest(
                Integer.parseInt(importParameters.externalOfferId()), importParameters.masterSellerId(), OfferProcessTypeEnum.FINALYZE_TASK, OfferProcessStatusEnum.NOT_TAKEN, null, importMetadata);
        retailerMediaImportStepDef.sendMetadata(metadataRequest);

        dataItems.forEach(dataItem -> {
            String id = dataItem.getId();
            AtomicReference<Response> response = new AtomicReference<>();
            Awaitility.await().atMost(10, TimeUnit.SECONDS).pollInterval(1, TimeUnit.SECONDS).until(() ->
            {
                response.set(metadataStepDef.checkMetadata(id));
                Object masterCategory = getValueFromResponse(response.get(), MASTER_CATEGORY.getPath());
                return masterCategory != null;
            });

            assertAll(
                    () -> compareParameters(Integer.parseInt(importParameters.externalOfferId()), getValueFromResponse(response.get(), EXTERNAL_OFFER.getPath() + ".id"), "external_offer.id"),
                    () -> compareParameters(importParameters.masterSellerId(), getValueFromResponse(response.get(), MASTER_SELLER.getPath() + ".ext_id"), "master_seller_id"),
                    () -> compareParameters(getData().unknownCategory(), getValueFromResponse(response.get(), MASTER_CATEGORY.getPath() + ".name"), "master_category"),
                    () -> compareParameters(importMetadata.taskIdForDam().toString(), getValueFromResponse(response.get(), EXTERNAL_TASK_ID.getPath()), "external_task_id"),
                    () -> compareParameters(
                            dataItem.getFilename(),
                            getValueFromResponse(response.get(), FILENAME.getPath()),
                            "filename"),
                    () -> compareParameters(importParameters.mediaPriority() + 1, getValueFromResponse(response.get(), PRIORITY.getPath()), "priority"),
                    () -> compareParameters(importMetadata.sku().toString(), getValueFromResponse(response.get(), SKU.getPath()), "sku"),
                    () -> notEquals(getValueFromResponse(response.get(), CREATED_AT.getPath()), getValueFromResponse(response.get(), UPDATED_AT.getPath()), "updated_at"),
                    () -> mustBeNullParameter(getValueFromResponse(response.get(), READY_FOR_RETOUCH_AT.getPath()), "ready_for_retouch_at"),
                    () -> compareParameters(importMetadata.isOwnTrademark(), getValueFromResponse(response.get(), IS_OWN_TRADEMARK.getPath()), "is_own_trademark")
            );
        });
    }

    @Test(description = "Успешный импорт метадаты по процессу FINALYZE_TASK - Актуальный", timeOut = 600000, groups = {"regress", "kafka"})
    @WorkItemIds({"288873"})
    public void successImportMetadataFinalyzeTaskActualTest() {
        dataItems.forEach(dataItem -> {
            Map<String, Object> editValues = new HashMap<>() {{
                put(SKU.getName(), RandomStringUtils.randomNumeric(3));
                put(PRIORITY.getName(), new Random().nextInt(NINETY_NINE) + 1);
                put(STATUS_ID.getName(), actualStatusId);
            }};
            metadataStepDef.successEditMetadata(dataItem.getId(), editValues);
        });

        RetailerMediaImportSettings.ImportMetadata importMetadata = getData().importMetadata().clone().
                sku(1 + new Random().nextInt(100)).taskIdForDam(null);
        String metadataRequest = retailerMediaImportStepDef.buildImportModerationMetadataRequest(
                Integer.parseInt(importParameters.externalOfferId()), importParameters.masterSellerId(), OfferProcessTypeEnum.FINALYZE_TASK, importMetadata);
        retailerMediaImportStepDef.sendMetadata(metadataRequest);

        dataItems.stream().map(DataItem::getId).forEach(id -> {
            Response response = metadataStepDef.checkMetadata(id);

            assertAll(
                    () -> compareParameters(Integer.parseInt(importParameters.externalOfferId()), getValueFromResponse(response, EXTERNAL_OFFER.getPath() + ".id"), "external_offer.id"),
                    () -> compareParameters(importParameters.masterSellerId(), getValueFromResponse(response, MASTER_SELLER.getPath() + ".ext_id"), "master_seller_id"),
                    () -> mustBeNullParameter(getValueFromResponse(response, MASTER_CATEGORY.getPath()), "master_category"),
                    () -> mustBeNullParameter(getValueFromResponse(response, EXTERNAL_TASK_ID.getPath()), "external_task_id"),
                    () -> compareParameters(!importMetadata.isOwnTrademark(), getValueFromResponse(response, IS_OWN_TRADEMARK.getPath()), "is_own_trademark")
            );
        });
    }

    //todo на удаление, изменилась логика
    //@Test(description = "Успешный импорт метадаты по процессу CANCEL_TASK", timeOut = 600000, groups = {"regress", "kafka"})
    @WorkItemIds({"288874"})
    public void successImportMetadataCancelTaskTest() {
        RetailerMediaImportSettings.ImportMetadata importMetadata = getData().importMetadata().clone().sku(null).taskIdForDam(null);
        String metadataRequest = retailerMediaImportStepDef.buildImportModerationMetadataRequest(
                Integer.parseInt(importParameters.externalOfferId()), importParameters.masterSellerId(), OfferProcessTypeEnum.CANCEL_TASK, importMetadata);
        retailerMediaImportStepDef.sendMetadata(metadataRequest);

        dataItems.stream().map(DataItem::getId).forEach(id -> {
            AtomicReference<Response> response = new AtomicReference<>();
            Awaitility.await().atMost(10, TimeUnit.SECONDS).pollInterval(1, TimeUnit.SECONDS).until(() ->
            {
                response.set(metadataStepDef.checkMetadata(id));
                Object masterCategory = getValueFromResponse(response.get(), MASTER_CATEGORY.getPath());
                return masterCategory != null;
            });

            assertAll(
                    () -> compareParameters(Integer.parseInt(importParameters.externalOfferId()), getValueFromResponse(response.get(), EXTERNAL_OFFER.getPath() + ".id"), "external_offer.id"),
                    () -> compareParameters(importParameters.masterSellerId(), getValueFromResponse(response.get(), MASTER_SELLER.getPath() + ".ext_id"), "master_seller_id"),
                    () -> compareParameters(getData().unknownCategory(), getValueFromResponse(response.get(), MASTER_CATEGORY.getPath() + ".name"), "master_category"),
                    () -> mustBeNullParameter(getValueFromResponse(response.get(), EXTERNAL_TASK_ID.getPath()), "external_task_id"),
                    () -> mustBeNullParameter(getValueFromResponse(response.get(), SKU.getPath()), "sku"),
                    () -> notEquals(getValueFromResponse(response.get(), CREATED_AT.getPath()), getValueFromResponse(response.get(), UPDATED_AT.getPath()), "updated_at"),
                    () -> compareParameters(importMetadata.isOwnTrademark(), getValueFromResponse(response.get(), IS_OWN_TRADEMARK.getPath()), "is_own_trademark")
            );
        });
    }

    //todo на удаление, изменилась логика
    //@Test(description = "Успешный импорт метадаты по процессу CANCEL_TASK - Актуальный", timeOut = 600000, groups = {"regress", "kafka"})
    @WorkItemIds({"288875"})
    public void successImportMetadataCancelTaskActualTest() {
        dataItems.forEach(dataItem -> {
            Map<String, Object> editValues = new HashMap<>() {{
                put(SKU.getName(), RandomStringUtils.randomNumeric(3));
                put(PRIORITY.getName(), new Random().nextInt(NINETY_NINE) + 1);
                put(STATUS_ID.getName(), actualStatusId);
            }};
            metadataStepDef.successEditMetadata(dataItem.getId(), editValues);
        });

        RetailerMediaImportSettings.ImportMetadata importMetadata = getData().importMetadata().clone().sku(null).taskIdForDam(null);
        String metadataRequest = retailerMediaImportStepDef.buildImportModerationMetadataRequest(
                Integer.parseInt(importParameters.externalOfferId()), importParameters.masterSellerId(), OfferProcessTypeEnum.CANCEL_TASK, importMetadata);
        retailerMediaImportStepDef.sendMetadata(metadataRequest);

        dataItems.stream().map(DataItem::getId).forEach(id -> {
            Response response = metadataStepDef.checkMetadata(id);

            assertAll(
                    () -> compareParameters(Integer.parseInt(importParameters.externalOfferId()), getValueFromResponse(response, EXTERNAL_OFFER.getPath() + ".id"), "external_offer.id"),
                    () -> compareParameters(importParameters.masterSellerId(), getValueFromResponse(response, MASTER_SELLER.getPath() + ".ext_id"), "master_seller_id"),
                    () -> mustBeNullParameter(getValueFromResponse(response, MASTER_CATEGORY.getPath()), "master_category"),
                    () -> mustBeNullParameter(getValueFromResponse(response, EXTERNAL_TASK_ID.getPath()), "external_task_id"),
                    () -> compareParameters(!importMetadata.isOwnTrademark(), getValueFromResponse(response, IS_OWN_TRADEMARK.getPath()), "is_own_trademark")
            );
        });
    }

    @Test(description = "Неуспешный импорт метадаты - нет externalOfferId", timeOut = 600000, groups = {"regress", "kafka"})
    @WorkItemIds({"288876"})
    public void unsuccessfulImportMetadataCancelTaskNotExternalOfferIdTest() {
        RetailerMediaImportSettings.ImportMetadata importMetadata = getData().importMetadata().clone().sku(null).taskIdForDam(null);
        String metadataRequest = retailerMediaImportStepDef.buildImportModerationMetadataRequest(
                null, importParameters.masterSellerId(), OfferProcessTypeEnum.CANCEL_TASK, importMetadata);
        retailerMediaImportStepDef.sendMetadata(metadataRequest);

        dataItems.stream().map(DataItem::getId).forEach(id -> {
            Response response = metadataStepDef.checkMetadata(id);

            assertAll(
                    () -> compareParameters(Integer.parseInt(importParameters.externalOfferId()), getValueFromResponse(response, EXTERNAL_OFFER.getPath() + ".id"), "external_offer.id"),
                    () -> compareParameters(importParameters.masterSellerId(), getValueFromResponse(response, MASTER_SELLER.getPath() + ".ext_id"), "master_seller_id"),
                    () -> mustBeNullParameter(getValueFromResponse(response, MASTER_CATEGORY.getPath()), "master_category"),
                    () -> mustBeNullParameter(getValueFromResponse(response, EXTERNAL_TASK_ID.getPath()), "external_task_id"),
                    () -> compareParameters(!importMetadata.isOwnTrademark(), getValueFromResponse(response, IS_OWN_TRADEMARK.getPath()), "is_own_trademark")
            );
        });
    }

    @Test(description = "Неуспешный импорт метадаты - нет masterSellerId", timeOut = 600000, groups = {"regress", "kafka"})
    @WorkItemIds({"288877"})
    public void unsuccessfulImportMetadataCancelTaskNotMasterSellerIdTest() {
        RetailerMediaImportSettings.ImportMetadata importMetadata = getData().importMetadata().clone().sku(null).taskIdForDam(null);
        String metadataRequest = retailerMediaImportStepDef.buildImportModerationMetadataRequest(
                Integer.parseInt(importParameters.externalOfferId()), null, OfferProcessTypeEnum.CANCEL_TASK, importMetadata);
        retailerMediaImportStepDef.sendMetadata(metadataRequest);

        dataItems.stream().map(DataItem::getId).forEach(id -> {
            Response response = metadataStepDef.checkMetadata(id);

            assertAll(
                    () -> compareParameters(Integer.parseInt(importParameters.externalOfferId()), getValueFromResponse(response, EXTERNAL_OFFER.getPath() + ".id"), "external_offer.id"),
                    () -> compareParameters(importParameters.masterSellerId(), getValueFromResponse(response, MASTER_SELLER.getPath() + ".ext_id"), "master_seller_id"),
                    () -> mustBeNullParameter(getValueFromResponse(response, MASTER_CATEGORY.getPath()), "master_category"),
                    () -> mustBeNullParameter(getValueFromResponse(response, EXTERNAL_TASK_ID.getPath()), "external_task_id"),
                    () -> compareParameters(!importMetadata.isOwnTrademark(), getValueFromResponse(response, IS_OWN_TRADEMARK.getPath()), "is_own_trademark")
            );
        });
    }

    @Test(description = "Успешный импорт метадаты c пустым task_type_for_dam по процессу CONTENT_AUTOGENERATION", timeOut = 600000, groups = {"regress", "kafka"})
    @WorkItemIds({"289324"})
    public void successImportMetadataContentAutogenerationTest() {
        RetailerMediaImportSettings.ImportMetadata importMetadata = getData().importMetadata().clone().
                sku(1 + new Random().nextInt(100)).taskIdForDam(null);
        String metadataRequest = retailerMediaImportStepDef.buildImportModerationNullExternalTaskMetadataRequest(
                Integer.parseInt(importParameters.externalOfferId()), importParameters.masterSellerId(), OfferProcessTypeEnum.CONTENT_AUTOGENERATION, importMetadata);
        retailerMediaImportStepDef.sendMetadata(metadataRequest);

        dataItems.stream().map(DataItem::getId).forEach(id -> {
            AtomicReference<Response> response = new AtomicReference<>();
            Awaitility.await().atMost(10, TimeUnit.SECONDS).pollInterval(1, TimeUnit.SECONDS).until(() ->
            {
                response.set(metadataStepDef.checkMetadata(id));
                Object masterCategory = getValueFromResponse(response.get(), MASTER_CATEGORY.getPath());
                return masterCategory != null;
            });

            assertAll(
                    () -> compareParameters(Integer.parseInt(importParameters.externalOfferId()), getValueFromResponse(response.get(), EXTERNAL_OFFER.getPath() + ".id"), "external_offer.id"),
                    () -> compareParameters(importParameters.masterSellerId(), getValueFromResponse(response.get(), MASTER_SELLER.getPath() + ".ext_id"), "master_seller_id"),
                    () -> compareParameters(getData().unknownCategory(), getValueFromResponse(response.get(), MASTER_CATEGORY.getPath() + ".name"), "master_category"),
                    () -> mustBeNullParameter(getValueFromResponse(response.get(), EXTERNAL_TASK_ID.getPath()), "external_task_id"),
                    () -> compareParameters(
                            String.format(FILENAME_MASK, importMetadata.sku(), 1, JPG.getFormatName()),
                            getValueFromResponse(response.get(), FILENAME.getPath()),
                            "filename"),
                    () -> compareParameters(1, getValueFromResponse(response.get(), PRIORITY.getPath()), "priority"),
                    () -> compareParameters(importMetadata.sku().toString(), getValueFromResponse(response.get(), SKU.getPath()), "sku"),
                    () -> notEquals(getValueFromResponse(response.get(), CREATED_AT.getPath()), getValueFromResponse(response.get(), UPDATED_AT.getPath()), "updated_at"),
                    () -> compareParameters(getData().readyForRetouchAt(), getValueFromResponse(response.get(), READY_FOR_RETOUCH_AT.getPath()), "ready_for_retouch_at"),
                    () -> compareParameters(importMetadata.isOwnTrademark(), getValueFromResponse(response.get(), IS_OWN_TRADEMARK.getPath()), "is_own_trademark")
            );
        });
    }

    @Test(description = "Успешный импорт метадаты c пустым task_type_for_dam по процессу CONTENT_AUTOGENERATION (нет Sku)",
            timeOut = 600000, groups = {"regress", "kafka"})
    @WorkItemIds({"289325"})
    public void successImportMetadataContentAutogenerationNoSkuTest() {
        RetailerMediaImportSettings.ImportMetadata importMetadata = getData().importMetadata().clone().
                sku(null).taskIdForDam(null);
        String metadataRequest = retailerMediaImportStepDef.buildImportModerationNullExternalTaskMetadataRequest(
                Integer.parseInt(importParameters.externalOfferId()), importParameters.masterSellerId(), OfferProcessTypeEnum.CONTENT_AUTOGENERATION, importMetadata);
        retailerMediaImportStepDef.sendMetadata(metadataRequest);

        dataItems.forEach(dataItem -> {
            String id = dataItem.getId();
            AtomicReference<Response> response = new AtomicReference<>();
            Awaitility.await().atMost(10, TimeUnit.SECONDS).pollInterval(1, TimeUnit.SECONDS).until(() ->
            {
                response.set(metadataStepDef.checkMetadata(id));
                Object masterCategory = getValueFromResponse(response.get(), MASTER_CATEGORY.getPath());
                return masterCategory != null;
            });

            assertAll(
                    () -> compareParameters(Integer.parseInt(importParameters.externalOfferId()), getValueFromResponse(response.get(), EXTERNAL_OFFER.getPath() + ".id"), "external_offer.id"),
                    () -> compareParameters(importParameters.masterSellerId(), getValueFromResponse(response.get(), MASTER_SELLER.getPath() + ".ext_id"), "master_seller_id"),
                    () -> compareParameters(getData().unknownCategory(), getValueFromResponse(response.get(), MASTER_CATEGORY.getPath() + ".name"), "master_category"),
                    () -> mustBeNullParameter(getValueFromResponse(response.get(), EXTERNAL_TASK_ID.getPath()), "external_task_id"),
                    () -> compareParameters(
                            dataItem.getFilename(),
                            getValueFromResponse(response.get(), FILENAME.getPath()),
                            "filename"),
                    () -> compareParameters(1, getValueFromResponse(response.get(), PRIORITY.getPath()), "priority"),
                    () -> mustBeNullParameter(getValueFromResponse(response.get(), SKU.getPath()), "sku"),
                    () -> notEquals(getValueFromResponse(response.get(), CREATED_AT.getPath()), getValueFromResponse(response.get(), UPDATED_AT.getPath()), "updated_at"),
                    () -> compareParameters(getData().readyForRetouchAt(), getValueFromResponse(response.get(), READY_FOR_RETOUCH_AT.getPath()), "ready_for_retouch_at"),
                    () -> compareParameters(importMetadata.isOwnTrademark(), getValueFromResponse(response.get(), IS_OWN_TRADEMARK.getPath()), "is_own_trademark")
            );
        });
    }

    @Test(description = "Успешный импорт метадаты c пустым task_type_for_dam по процессу CONTENT_AUTOGENERATION - Актуальный",
            timeOut = 600000, groups = {"regress", "kafka"})
    @WorkItemIds({"289326"})
    public void successImportMetadataContentAutogenerationActualTest() {
        dataItems.forEach(dataItem -> {
            Map<String, Object> editValues = new HashMap<>() {{
                put(SKU.getName(), RandomStringUtils.randomNumeric(3));
                put(PRIORITY.getName(), new Random().nextInt(NINETY_NINE) + 1);
                put(STATUS_ID.getName(), actualStatusId);
            }};
            metadataStepDef.successEditMetadata(dataItem.getId(), editValues);
        });

        RetailerMediaImportSettings.ImportMetadata importMetadata = getData().importMetadata().clone().
                sku(1 + new Random().nextInt(100)).taskIdForDam(null);
        String metadataRequest = retailerMediaImportStepDef.buildImportModerationNullExternalTaskMetadataRequest(
                Integer.parseInt(importParameters.externalOfferId()), importParameters.masterSellerId(), OfferProcessTypeEnum.CONTENT_AUTOGENERATION, importMetadata);
        retailerMediaImportStepDef.sendMetadata(metadataRequest);

        dataItems.stream().map(DataItem::getId).forEach(id -> {
            Response response = metadataStepDef.checkMetadata(id);

            assertAll(
                    () -> compareParameters(Integer.parseInt(importParameters.externalOfferId()), getValueFromResponse(response, EXTERNAL_OFFER.getPath() + ".id"), "external_offer.id"),
                    () -> compareParameters(importParameters.masterSellerId(), getValueFromResponse(response, MASTER_SELLER.getPath() + ".ext_id"), "master_seller_id"),
                    () -> mustBeNullParameter(getValueFromResponse(response, MASTER_CATEGORY.getPath()), "master_category"),
                    () -> mustBeNullParameter(getValueFromResponse(response, EXTERNAL_TASK_ID.getPath()), "external_task_id"),
                    () -> compareParameters(!importMetadata.isOwnTrademark(), getValueFromResponse(response, IS_OWN_TRADEMARK.getPath()), "is_own_trademark")
            );
        });
    }

    @Test(description = "Неуспешный импорт метадаты c пустым task_type_for_dam по процессу НЕ CONTENT_AUTOGENERATION",
            timeOut = 600000, groups = {"regress", "kafka"})
    @WorkItemIds({"289327"})
    public void unsuccessfulImportMetadataContentNoAutogenerationTest() {
        RetailerMediaImportSettings.ImportMetadata importMetadata = getData().importMetadata().clone().
                sku(1 + new Random().nextInt(100)).taskIdForDam(null);
        String metadataRequest = retailerMediaImportStepDef.buildImportModerationNullExternalTaskMetadataRequest(
                Integer.parseInt(importParameters.externalOfferId()), importParameters.masterSellerId(), OfferProcessTypeEnum.FINALYZE_TASK, importMetadata);
        retailerMediaImportStepDef.sendMetadata(metadataRequest);

        dataItems.stream().map(DataItem::getId).forEach(id -> {
            Response response = metadataStepDef.checkMetadata(id);

            assertAll(
                    () -> compareParameters(Integer.parseInt(importParameters.externalOfferId()), getValueFromResponse(response, EXTERNAL_OFFER.getPath() + ".id"), "external_offer.id"),
                    () -> compareParameters(importParameters.masterSellerId(), getValueFromResponse(response, MASTER_SELLER.getPath() + ".ext_id"), "master_seller_id"),
                    () -> mustBeNullParameter(getValueFromResponse(response, MASTER_CATEGORY.getPath()), "master_category"),
                    () -> mustBeNullParameter(getValueFromResponse(response, EXTERNAL_TASK_ID.getPath()), "external_task_id"),
                    () -> compareParameters(!importMetadata.isOwnTrademark(), getValueFromResponse(response, IS_OWN_TRADEMARK.getPath()), "is_own_trademark")
            );
        });
    }
}
