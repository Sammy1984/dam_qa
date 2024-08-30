package ru.spice.at.api.retailer_media_import_service.test;

import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import io.restassured.response.Response;
import org.apache.commons.lang3.RandomStringUtils;
import org.awaitility.Awaitility;
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
@Story("PIMS - Import metadata create products")
public class RetailerMediaImportCreateTests extends BaseApiTest<RetailerMediaImportSettings> {
    private final RetailerMediaImportStepDef retailerMediaImportStepDef;
    private final MetadataStepDef metadataStepDef;

    private RetailerMediaImportSettings.ImportParameters importParameters;
    private List<DataItem> dataItems;

    protected RetailerMediaImportCreateTests() {
        super(ApiServices.RETAILER_MEDIA_IMPORT_SERVICE);
        retailerMediaImportStepDef = new RetailerMediaImportStepDef();
        metadataStepDef = new MetadataStepDef();
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
        Awaitility.await().atMost(10, TimeUnit.SECONDS).pollInterval(1, TimeUnit.SECONDS).ignoreExceptions().until(() ->
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
    //@Test(description = "Успешный импорт метадаты по процессу CREATE_TASK (производное)", timeOut = 600000, groups = {"regress", "kafka"})
    @WorkItemIds({"289329"})
    public void successImportMetadataCreateTaskTest() {
        RetailerMediaImportSettings.ImportMetadata importMetadata = getData().importMetadata().clone().sku(null);
        String metadataRequest = retailerMediaImportStepDef.buildImportCreateMetadataRequest(
                dataItems.get(0).getId(), OfferProcessTypeEnum.CREATE_TASK, importMetadata);
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
    //@Test(description = "Успешный импорт метадаты по процессу CREATE_TASK (оригинал)", timeOut = 600000, groups = {"regress", "kafka"})
    @WorkItemIds({"289329"})
    public void successImportMetadataCreateTaskOriginalTest() {
        RetailerMediaImportSettings.ImportMetadata importMetadata = getData().importMetadata().clone().sku(null);
        String metadataRequest = retailerMediaImportStepDef.buildImportCreateMetadataRequest(
                dataItems.get(1).getId(), OfferProcessTypeEnum.CREATE_TASK, importMetadata);
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
    //@Test(description = "Успешный импорт метадаты по процессу CREATE_TASK (несколько)", timeOut = 600000, groups = {"regress", "kafka"})
    @WorkItemIds({"289329"})
    public void successImportMetadataCreateTaskSomeMediaTest() {
        importParameters = getData().importParameters().clone().
                mediaName(RandomStringUtils.randomAlphabetic(10) + DOT_VALUE + JPG.getFormatName()).
                masterSellerId(new Random().nextInt(100) + 1).
                externalOfferId(String.valueOf(new Random().nextInt(1000000) + 1000000));
        retailerMediaImportStepDef.sendMediaFile(retailerMediaImportStepDef.buildImportOneMediaRequest(importParameters));

        AtomicReference<List<DataItem>> dataItemsAtomic = new AtomicReference<>();
        Awaitility.await().atMost(10, TimeUnit.SECONDS).pollInterval(1, TimeUnit.SECONDS).until(() ->
        {
            List<DataItem> dataItems = metadataStepDef.successMetadataSearch(Integer.parseInt(importParameters.externalOfferId()));
            dataItemsAtomic.set(dataItems);
            return dataItems.size() == 2;
        });


        RetailerMediaImportSettings.ImportMetadata importMetadata = getData().importMetadata().clone().sku(null);
        String metadataRequest = retailerMediaImportStepDef.buildImportCreateMetadataRequest(
                dataItems.get(0).getId(), dataItemsAtomic.get().get(0).getId(), OfferProcessTypeEnum.CREATE_TASK, importMetadata);
        retailerMediaImportStepDef.sendMetadata(metadataRequest);

        dataItems.addAll(dataItemsAtomic.get());
        dataItems.stream().map(DataItem::getId).forEach(id -> {
            AtomicReference<Response> response = new AtomicReference<>();
            Awaitility.await().atMost(10, TimeUnit.SECONDS).pollInterval(1, TimeUnit.SECONDS).until(() ->
            {
                response.set(metadataStepDef.checkMetadata(id));
                Object masterCategory = getValueFromResponse(response.get(), MASTER_CATEGORY.getPath());
                return masterCategory != null;
            });

            assertAll(
                    () -> compareParameters(getData().unknownCategory(), getValueFromResponse(response.get(), MASTER_CATEGORY.getPath() + ".name"), "master_category"),
                    () -> compareParameters(importMetadata.taskIdForDam().toString(), getValueFromResponse(response.get(), EXTERNAL_TASK_ID.getPath()), "external_task_id"),
                    () -> mustBeNullParameter(getValueFromResponse(response.get(), SKU.getPath()), "sku"),
                    () -> notEquals(getValueFromResponse(response.get(), CREATED_AT.getPath()), getValueFromResponse(response.get(), UPDATED_AT.getPath()), "updated_at"),
                    () -> compareParameters(importMetadata.isOwnTrademark(), getValueFromResponse(response.get(), IS_OWN_TRADEMARK.getPath()), "is_own_trademark")
            );
        });
    }

    //todo на удаление, изменилась логика
    //@Test(description = "Успешный импорт метадаты по процессу CREATE_TASK - заполнен SKU (оригинал и производное)", timeOut = 600000, groups = {"regress", "kafka"})
    @WorkItemIds({"289330"})
    public void successImportMetadataCreateTaskSkuTest() {
        dataItems.stream().map(DataItem::getId).
                forEach(id -> metadataStepDef.successEditMetadata(id, SKU.getName(), RandomStringUtils.randomNumeric(3)));

        RetailerMediaImportSettings.ImportMetadata importMetadata = getData().importMetadata().clone().sku(null);
        String metadataRequest = retailerMediaImportStepDef.buildImportCreateMetadataRequest(
                dataItems.get(0).getId(), dataItems.get(1).getId(), OfferProcessTypeEnum.CREATE_TASK, importMetadata);
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
    //@Test(description = "Успешный импорт метадаты по процессу CREATE_TASK - заполнен SKU (оригинал)", timeOut = 600000, groups = {"regress", "kafka"})
    @WorkItemIds({"289330"})
    public void successImportMetadataCreateTaskSkuOriginalTest() {
        metadataStepDef.successEditMetadata(dataItems.get(1).getId(), SKU.getName(), RandomStringUtils.randomNumeric(3));

        RetailerMediaImportSettings.ImportMetadata importMetadata = getData().importMetadata().clone().sku(null);
        String metadataRequest = retailerMediaImportStepDef.buildImportCreateMetadataRequest(
                dataItems.get(0).getId(), dataItems.get(1).getId(), OfferProcessTypeEnum.CREATE_TASK, importMetadata);
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
    //@Test(description = "Неуспешный импорт метадаты по процессу CREATE_TASK - не найдены media_id", timeOut = 600000, groups = {"regress", "kafka"})
    @WorkItemIds({"289331"})
    public void unsuccessfulImportMetadataCreateTaskTest() {
        RetailerMediaImportSettings.ImportMetadata importMetadata = getData().importMetadata().clone().sku(null);
        String metadataRequest = retailerMediaImportStepDef.buildImportCreateMetadataRequest(
                new RetailerMediaImportStepDef.Media().mediaId(UUID.randomUUID().toString()), new RetailerMediaImportStepDef.Media().mediaId(UUID.randomUUID().toString()),
                Integer.parseInt(importParameters.externalOfferId()), importParameters.masterSellerId(),
                OfferProcessTypeEnum.CREATE_TASK, OfferProcessStatusEnum.BINDED, importMetadata);
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
    //@Test(description = "Успешный импорт метадаты по процессу ADD_TO_TASK", timeOut = 600000, groups = {"regress", "kafka"})
    @WorkItemIds({"289332"})
    public void successImportMetadataAddToTaskTest() {
        RetailerMediaImportSettings.ImportMetadata importMetadata = getData().importMetadata().clone().sku(null);
        String metadataRequest = retailerMediaImportStepDef.buildImportCreateMetadataRequest(
                dataItems.get(0).getId(), OfferProcessTypeEnum.ADD_TO_TASK, importMetadata);
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
    //@Test(description = "Успешный импорт метадаты по процессу ADD_TO_TASK - заполнен SKU", timeOut = 600000, groups = {"regress", "kafka"})
    @WorkItemIds({"289333"})
    public void successImportMetadataAddToTaskSkuTest() {
        dataItems.stream().map(DataItem::getId).
                forEach(id -> metadataStepDef.successEditMetadata(id, SKU.getName(), RandomStringUtils.randomNumeric(3)));

        RetailerMediaImportSettings.ImportMetadata importMetadata = getData().importMetadata().clone().sku(null);
        String metadataRequest = retailerMediaImportStepDef.buildImportCreateMetadataRequest(
                dataItems.get(0).getId(), dataItems.get(1).getId(), OfferProcessTypeEnum.ADD_TO_TASK, importMetadata);
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
    //@Test(description = "Неуспешный импорт метадаты по процессу ADD_TO_TASK - не найдены media_id", timeOut = 600000, groups = {"regress", "kafka"})
    @WorkItemIds({"289334"})
    public void unsuccessfulImportMetadataAddToTaskTest() {
        RetailerMediaImportSettings.ImportMetadata importMetadata = getData().importMetadata().clone().sku(null);
        String metadataRequest = retailerMediaImportStepDef.buildImportCreateMetadataRequest(
                new RetailerMediaImportStepDef.Media().mediaId(UUID.randomUUID().toString()), new RetailerMediaImportStepDef.Media().mediaId(UUID.randomUUID().toString()),
                Integer.parseInt(importParameters.externalOfferId()), importParameters.masterSellerId(),
                OfferProcessTypeEnum.ADD_TO_TASK, OfferProcessStatusEnum.BINDED, importMetadata);
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

    @Test(description = "Успешный импорт метадаты по процессу FINALYZE_TASK статус BINDED, is_deleted = false (производное)",
            timeOut = 600000, groups = {"regress", "kafka"})
    @WorkItemIds({"289336"})
    public void successImportMetadataFinalyzeTaskBindedTest() {
        RetailerMediaImportSettings.ImportMetadata importMetadata = getData().importMetadata().clone().taskIdForDam(null);
        RetailerMediaImportStepDef.Media media = new RetailerMediaImportStepDef.Media().
                mediaId(dataItems.get(0).getId()).priority(new Random().nextInt(FOUR_HUNDRED_NINETY_NINE) + 1).isDeleted(false);

        String metadataRequest = retailerMediaImportStepDef.buildImportCreateMetadataRequest(media, OfferProcessTypeEnum.FINALYZE_TASK, importMetadata);
        retailerMediaImportStepDef.sendMetadata(metadataRequest);

        dataItems.stream().map(DataItem::getId).forEach(id -> {
            AtomicReference<Response> response = new AtomicReference<>();
            Awaitility.await().atMost(10, TimeUnit.SECONDS).pollInterval(1, TimeUnit.SECONDS).ignoreExceptions().until(() ->
            {
                response.set(metadataStepDef.checkMetadata(id));
                Object masterCategory = getValueFromResponse(response.get(), MASTER_CATEGORY.getPath());
                return masterCategory != null;
            });

            assertAll(
                    () -> compareParameters(getData().unknownCategory(), getValueFromResponse(response.get(), MASTER_CATEGORY.getPath() + ".name"), "master_category"),
                    () -> mustBeNullParameter(getValueFromResponse(response.get(), EXTERNAL_TASK_ID.getPath()), "external_task_id"),
                    () -> compareParameters(
                            String.format(FILENAME_MASK, importMetadata.sku(), media.priority(), JPG.getFormatName()),
                            getValueFromResponse(response.get(), FILENAME.getPath()),
                            "filename"),
                    () -> compareParameters(media.priority(), getValueFromResponse(response.get(), PRIORITY.getPath()), "priority"),
                    () -> compareParameters(importMetadata.sku().toString(), getValueFromResponse(response.get(), SKU.getPath()), "sku"),
                    () -> notEquals(getValueFromResponse(response.get(), CREATED_AT.getPath()), getValueFromResponse(response.get(), UPDATED_AT.getPath()), "updated_at"),
                    () -> compareParameters(getData().readyForRetouchAt(), getValueFromResponse(response.get(), READY_FOR_RETOUCH_AT.getPath()), "ready_for_retouch_at"),
                    () -> compareParameters(importMetadata.isOwnTrademark(), getValueFromResponse(response.get(), IS_OWN_TRADEMARK.getPath()), "is_own_trademark")
            );
        });
    }

    @Test(description = "Успешный импорт метадаты по процессу FINALYZE_TASK статус BINDED, is_deleted = false (оригинал)",
            timeOut = 600000, groups = {"regress", "kafka"})
    @WorkItemIds({"289336"})
    public void successImportMetadataFinalyzeTaskBindedOriginalTest() {
        RetailerMediaImportSettings.ImportMetadata importMetadata = getData().importMetadata().clone().taskIdForDam(null);
        RetailerMediaImportStepDef.Media media = new RetailerMediaImportStepDef.Media().
                mediaId(dataItems.get(1).getId()).priority(new Random().nextInt(FOUR_HUNDRED_NINETY_NINE) + 1).isDeleted(false);

        String metadataRequest = retailerMediaImportStepDef.buildImportCreateMetadataRequest(media, OfferProcessTypeEnum.FINALYZE_TASK, importMetadata);
        retailerMediaImportStepDef.sendMetadata(metadataRequest);

        dataItems.stream().map(DataItem::getId).forEach(id -> {
            AtomicReference<Response> response = new AtomicReference<>();
            Awaitility.await().atMost(10, TimeUnit.SECONDS).pollInterval(1, TimeUnit.SECONDS).ignoreExceptions().until(() ->
            {
                response.set(metadataStepDef.checkMetadata(id));
                Object masterCategory = getValueFromResponse(response.get(), MASTER_CATEGORY.getPath());
                return masterCategory != null;
            });

            assertAll(
                    () -> compareParameters(getData().unknownCategory(), getValueFromResponse(response.get(), MASTER_CATEGORY.getPath() + ".name"), "master_category"),
                    () -> mustBeNullParameter(getValueFromResponse(response.get(), EXTERNAL_TASK_ID.getPath()), "external_task_id"),
                    () -> compareParameters(
                            String.format(FILENAME_MASK, importMetadata.sku(), media.priority(), JPG.getFormatName()),
                            getValueFromResponse(response.get(), FILENAME.getPath()),
                            "filename"),
                    () -> compareParameters(media.priority(), getValueFromResponse(response.get(), PRIORITY.getPath()), "priority"),
                    () -> compareParameters(importMetadata.sku().toString(), getValueFromResponse(response.get(), SKU.getPath()), "sku"),
                    () -> notEquals(getValueFromResponse(response.get(), CREATED_AT.getPath()), getValueFromResponse(response.get(), UPDATED_AT.getPath()), "updated_at"),
                    () -> compareParameters(getData().readyForRetouchAt(), getValueFromResponse(response.get(), READY_FOR_RETOUCH_AT.getPath()), "ready_for_retouch_at"),
                    () -> compareParameters(importMetadata.isOwnTrademark(), getValueFromResponse(response.get(), IS_OWN_TRADEMARK.getPath()), "is_own_trademark")
            );
        });
    }

    @Test(description = "Успешный импорт метадаты по процессу FINALYZE_TASK статус BINDED, is_deleted = false (несколько)",
            timeOut = 600000, groups = {"regress", "kafka"})
    @WorkItemIds({"289336"})
    public void successImportMetadataFinalyzeTaskBindedSomeTest() {
        importParameters = getData().importParameters().clone().
                mediaName(RandomStringUtils.randomAlphabetic(10) + DOT_VALUE + JPG.getFormatName()).
                masterSellerId(new Random().nextInt(100) + 1).
                externalOfferId(String.valueOf(new Random().nextInt(1000000) + 1000000));
        retailerMediaImportStepDef.sendMediaFile(retailerMediaImportStepDef.buildImportOneMediaRequest(importParameters));

        AtomicReference<List<DataItem>> dataItemsAtomic = new AtomicReference<>();
        Awaitility.await().atMost(10, TimeUnit.SECONDS).pollInterval(1, TimeUnit.SECONDS).ignoreExceptions().until(() ->
        {
            List<DataItem> dataItems = metadataStepDef.successMetadataSearch(Integer.parseInt(importParameters.externalOfferId()));
            dataItemsAtomic.set(dataItems);
            return dataItems.size() == 2;
        });

        Integer priority = new Random().nextInt(FOUR_HUNDRED_NINETY_NINE) + 1;
        RetailerMediaImportSettings.ImportMetadata importMetadata = getData().importMetadata().clone().taskIdForDam(null);
        RetailerMediaImportStepDef.Media firstMedia = new RetailerMediaImportStepDef.Media().
                mediaId(dataItems.get(1).getId()).priority(priority).isDeleted(false);
        RetailerMediaImportStepDef.Media secondmedia = new RetailerMediaImportStepDef.Media().
                mediaId(dataItemsAtomic.get().get(0).getId()).priority(priority).isDeleted(false);

        String metadataRequest = retailerMediaImportStepDef.buildImportCreateMetadataRequest(firstMedia, secondmedia, OfferProcessTypeEnum.FINALYZE_TASK, importMetadata);
        retailerMediaImportStepDef.sendMetadata(metadataRequest);

        dataItems.addAll(dataItemsAtomic.get());
        dataItems.stream().map(DataItem::getId).forEach(id -> {
            AtomicReference<Response> response = new AtomicReference<>();
            Awaitility.await().atMost(10, TimeUnit.SECONDS).pollInterval(1, TimeUnit.SECONDS).ignoreExceptions().until(() ->
            {
                response.set(metadataStepDef.checkMetadata(id));
                Object masterCategory = getValueFromResponse(response.get(), MASTER_CATEGORY.getPath());
                return masterCategory != null;
            });

            assertAll(
                    () -> compareParameters(getData().unknownCategory(), getValueFromResponse(response.get(), MASTER_CATEGORY.getPath() + ".name"), "master_category"),
                    () -> mustBeNullParameter(getValueFromResponse(response.get(), EXTERNAL_TASK_ID.getPath()), "external_task_id"),
                    () -> compareParameters(
                            String.format(FILENAME_MASK, importMetadata.sku(), priority, JPG.getFormatName()),
                            getValueFromResponse(response.get(), FILENAME.getPath()),
                            "filename"),
                    () -> compareParameters(priority, getValueFromResponse(response.get(), PRIORITY.getPath()), "priority"),
                    () -> compareParameters(importMetadata.sku().toString(), getValueFromResponse(response.get(), SKU.getPath()), "sku"),
                    () -> notEquals(getValueFromResponse(response.get(), CREATED_AT.getPath()), getValueFromResponse(response.get(), UPDATED_AT.getPath()), "updated_at"),
                    () -> compareParameters(getData().readyForRetouchAt(), getValueFromResponse(response.get(), READY_FOR_RETOUCH_AT.getPath()), "ready_for_retouch_at"),
                    () -> compareParameters(importMetadata.isOwnTrademark(), getValueFromResponse(response.get(), IS_OWN_TRADEMARK.getPath()), "is_own_trademark")
            );
        });
    }

    @Test(description = "Успешный импорт метадаты по процессу FINALYZE_TASK статус BINDED, is_deleted = false (невалидный приоритет)",
            timeOut = 600000, groups = {"regress", "kafka"})
    @WorkItemIds({"289337"})
    public void successImportMetadataFinalyzeTaskBindedInvalidPriorityTest() {
        RetailerMediaImportSettings.ImportMetadata importMetadata = getData().importMetadata().clone().taskIdForDam(null);
        RetailerMediaImportStepDef.Media media = new RetailerMediaImportStepDef.Media().
                mediaId(dataItems.get(0).getId()).priority(new Random().nextInt(FOUR_HUNDRED_NINETY_NINE) + 501).isDeleted(false);

        String metadataRequest = retailerMediaImportStepDef.buildImportCreateMetadataRequest(media, OfferProcessTypeEnum.FINALYZE_TASK, importMetadata);
        retailerMediaImportStepDef.sendMetadata(metadataRequest);

        dataItems.stream().map(DataItem::getId).forEach(id -> {
            AtomicReference<Response> response = new AtomicReference<>();
            Awaitility.await().atMost(10, TimeUnit.SECONDS).pollInterval(1, TimeUnit.SECONDS).ignoreExceptions().until(() ->
            {
                response.set(metadataStepDef.checkMetadata(id));
                Object masterCategory = getValueFromResponse(response.get(), MASTER_CATEGORY.getPath());
                return masterCategory != null;
            });

            assertAll(
                    () -> compareParameters(getData().unknownCategory(), getValueFromResponse(response.get(), MASTER_CATEGORY.getPath() + ".name"), "master_category"),
                    () -> mustBeNullParameter(getValueFromResponse(response.get(), EXTERNAL_TASK_ID.getPath()), "external_task_id"),
                    () -> notEquals(
                            String.format(FILENAME_MASK, importMetadata.sku(), media.priority(), JPG.getFormatName()),
                            getValueFromResponse(response.get(), FILENAME.getPath()),
                            "filename"),
                    () -> mustBeNullParameter(getValueFromResponse(response.get(), PRIORITY.getPath()), "priority"),
                    () -> compareParameters(importMetadata.sku().toString(), getValueFromResponse(response.get(), SKU.getPath()), "sku"),
                    () -> notEquals(getValueFromResponse(response.get(), CREATED_AT.getPath()), getValueFromResponse(response.get(), UPDATED_AT.getPath()), "updated_at"),
                    () -> compareParameters(getData().readyForRetouchAt(), getValueFromResponse(response.get(), READY_FOR_RETOUCH_AT.getPath()), "ready_for_retouch_at"),
                    () -> compareParameters(importMetadata.isOwnTrademark(), getValueFromResponse(response.get(), IS_OWN_TRADEMARK.getPath()), "is_own_trademark")
            );
        });
    }

    @Test(description = "Успешный импорт метадаты по процессу FINALYZE_TASK статус BINDED, is_deleted = false (приоритет = 0)",
            timeOut = 600000, groups = {"regress", "kafka"})
    @WorkItemIds({"289337"})
    public void successImportMetadataFinalyzeTaskBindedNullPriorityTest() {
        RetailerMediaImportSettings.ImportMetadata importMetadata = getData().importMetadata().clone().taskIdForDam(null);
        RetailerMediaImportStepDef.Media media = new RetailerMediaImportStepDef.Media().
                mediaId(dataItems.get(0).getId()).priority(0).isDeleted(false);

        String metadataRequest = retailerMediaImportStepDef.buildImportCreateMetadataRequest(media, OfferProcessTypeEnum.FINALYZE_TASK, importMetadata);
        retailerMediaImportStepDef.sendMetadata(metadataRequest);

        dataItems.stream().map(DataItem::getId).forEach(id -> {
            AtomicReference<Response> response = new AtomicReference<>();
            Awaitility.await().atMost(10, TimeUnit.SECONDS).pollInterval(1, TimeUnit.SECONDS).ignoreExceptions().until(() ->
            {
                response.set(metadataStepDef.checkMetadata(id));
                Object masterCategory = getValueFromResponse(response.get(), MASTER_CATEGORY.getPath());
                return masterCategory != null;
            });

            assertAll(
                    () -> compareParameters(getData().unknownCategory(), getValueFromResponse(response.get(), MASTER_CATEGORY.getPath() + ".name"), "master_category"),
                    () -> mustBeNullParameter(getValueFromResponse(response.get(), EXTERNAL_TASK_ID.getPath()), "external_task_id"),
                    () -> notEquals(
                            String.format(FILENAME_MASK, importMetadata.sku(), media.priority(), JPG.getFormatName()),
                            getValueFromResponse(response.get(), FILENAME.getPath()),
                            "filename"),
                    () -> mustBeNullParameter(getValueFromResponse(response.get(), PRIORITY.getPath()), "priority"),
                    () -> compareParameters(importMetadata.sku().toString(), getValueFromResponse(response.get(), SKU.getPath()), "sku"),
                    () -> notEquals(getValueFromResponse(response.get(), CREATED_AT.getPath()), getValueFromResponse(response.get(), UPDATED_AT.getPath()), "updated_at"),
                    () -> compareParameters(getData().readyForRetouchAt(), getValueFromResponse(response.get(), READY_FOR_RETOUCH_AT.getPath()), "ready_for_retouch_at"),
                    () -> compareParameters(importMetadata.isOwnTrademark(), getValueFromResponse(response.get(), IS_OWN_TRADEMARK.getPath()), "is_own_trademark")
            );
        });
    }

    @Test(description = "Успешный импорт метадаты по процессу FINALYZE_TASK статус BINDED, is_deleted = true",
            timeOut = 600000, groups = {"regress", "kafka"})
    @WorkItemIds({"289338"})
    public void successImportMetadataFinalyzeTaskBindedIsDeletedTest() {
        RetailerMediaImportSettings.ImportMetadata importMetadata = getData().importMetadata().clone().taskIdForDam(null);
        RetailerMediaImportStepDef.Media media = new RetailerMediaImportStepDef.Media().
                mediaId(dataItems.get(0).getId()).priority(new Random().nextInt(FOUR_HUNDRED_NINETY_NINE) + 1).isDeleted(true);

        String metadataRequest = retailerMediaImportStepDef.buildImportCreateMetadataRequest(media, OfferProcessTypeEnum.FINALYZE_TASK, importMetadata);
        retailerMediaImportStepDef.sendMetadata(metadataRequest);

        dataItems.stream().map(DataItem::getId).forEach(id -> {
            AtomicReference<Response> response = new AtomicReference<>();
            Awaitility.await().atMost(10, TimeUnit.SECONDS).pollInterval(1, TimeUnit.SECONDS).ignoreExceptions().until(() ->
            {
                response.set(metadataStepDef.checkMetadata(id));
                Object masterCategory = getValueFromResponse(response.get(), MASTER_CATEGORY.getPath());
                return masterCategory != null;
            });

            assertAll(
                    () -> compareParameters(getData().unknownCategory(), getValueFromResponse(response.get(), MASTER_CATEGORY.getPath() + ".name"), "master_category"),
                    () -> mustBeNullParameter(getValueFromResponse(response.get(), EXTERNAL_TASK_ID.getPath()), "external_task_id"),
                    () -> notEquals(
                            String.format(FILENAME_MASK, importMetadata.sku(), media.priority(), JPG.getFormatName()),
                            getValueFromResponse(response.get(), FILENAME.getPath()),
                            "filename"),
                    () -> compareParameters(media.priority(), getValueFromResponse(response.get(), PRIORITY.getPath()), "priority"),
                    () -> compareParameters(Status.ARCHIVE.getName(), getValueFromResponse(response.get(), STATUS_ID.getPath() + ".name"), "status_name"),
                    () -> compareParameters(getData().keywordsIsDeleted(), getValueFromResponse(response.get(), KEYWORDS.getPath()), "keywords"),
                    () -> compareParameters(importMetadata.sku().toString(), getValueFromResponse(response.get(), SKU.getPath()), "sku"),
                    () -> notEquals(getValueFromResponse(response.get(), CREATED_AT.getPath()), getValueFromResponse(response.get(), UPDATED_AT.getPath()), "updated_at"),
                    () -> mustBeNullParameter(getValueFromResponse(response.get(), READY_FOR_RETOUCH_AT.getPath()), "ready_for_retouch_at"),
                    () -> compareParameters(importMetadata.isOwnTrademark(), getValueFromResponse(response.get(), IS_OWN_TRADEMARK.getPath()), "is_own_trademark")
            );
        });
    }

    @Test(description = "Успешный импорт метадаты по процессу FINALYZE_TASK статус НЕ BINDED", timeOut = 600000, groups = {"regress", "kafka"})
    @WorkItemIds({"289339"})
    public void successImportMetadataFinalyzeTaskNotBindedTest() {
        RetailerMediaImportSettings.ImportMetadata importMetadata = getData().importMetadata().clone().taskIdForDam(null);
        RetailerMediaImportStepDef.Media media = new RetailerMediaImportStepDef.Media().
                mediaId(dataItems.get(0).getId()).priority(new Random().nextInt(FOUR_HUNDRED_NINETY_NINE) + 1).isDeleted(true);

        String metadataRequest = retailerMediaImportStepDef.buildImportCreateMetadataRequest(
                media, new RetailerMediaImportStepDef.Media().mediaId(UUID.randomUUID().toString()),
                OfferProcessTypeEnum.FINALYZE_TASK, OfferProcessStatusEnum.NOT_TAKEN, importMetadata);
        retailerMediaImportStepDef.sendMetadata(metadataRequest);

        dataItems.stream().map(DataItem::getId).forEach(id -> {
            AtomicReference<Response> response = new AtomicReference<>();
            Awaitility.await().atMost(10, TimeUnit.SECONDS).pollInterval(1, TimeUnit.SECONDS).ignoreExceptions().until(() ->
            {
                response.set(metadataStepDef.checkMetadata(id));
                Object masterCategory = getValueFromResponse(response.get(), MASTER_CATEGORY.getPath());
                return masterCategory != null;
            });

            assertAll(
                    () -> compareParameters(getData().unknownCategory(), getValueFromResponse(response.get(), MASTER_CATEGORY.getPath() + ".name"), "master_category"),
                    () -> mustBeNullParameter(getValueFromResponse(response.get(), EXTERNAL_TASK_ID.getPath()), "external_task_id"),
                    () -> notEquals(
                            String.format(FILENAME_MASK, importMetadata.sku(), media.priority(), JPG.getFormatName()),
                            getValueFromResponse(response.get(), FILENAME.getPath()),
                            "filename"),
                    () -> notEquals(media.priority(), getValueFromResponse(response.get(), PRIORITY.getPath()), "priority"),
                    () -> mustBeNullParameter(getValueFromResponse(response.get(), KEYWORDS.getPath()), "keywords"),
                    () -> compareParameters(importMetadata.sku().toString(), getValueFromResponse(response.get(), SKU.getPath()), "sku"),
                    () -> notEquals(getValueFromResponse(response.get(), CREATED_AT.getPath()), getValueFromResponse(response.get(), UPDATED_AT.getPath()), "updated_at"),
                    () -> mustBeNullParameter(getValueFromResponse(response.get(), READY_FOR_RETOUCH_AT.getPath()), "ready_for_retouch_at"),
                    () -> compareParameters(importMetadata.isOwnTrademark(), getValueFromResponse(response.get(), IS_OWN_TRADEMARK.getPath()), "is_own_trademark")
            );
        });
    }

    @Test(description = "Успешный импорт метадаты по процессу FINALYZE_TASK - Актуальный", timeOut = 600000, groups = {"regress", "kafka"})
    @WorkItemIds({"289340"})
    public void successImportMetadataFinalyzeTaskSkuTest() {
        String actualStatusId = metadataStepDef.getListStatusesMetadata().stream().filter(item -> item.name().equals(Status.ACTUAL.getName())).
                map(DictionariesItem::id).findFirst().orElse(null);

        dataItems.forEach(dataItem -> {
            Map<String, Object> editValues = new HashMap<>() {{
                put(SKU.getName(), RandomStringUtils.randomNumeric(3));
                put(PRIORITY.getName(), new Random().nextInt(NINETY_NINE) + 1);
                put(STATUS_ID.getName(), actualStatusId);
            }};
            metadataStepDef.successEditMetadata(dataItem.getId(), editValues);
        });

        RetailerMediaImportSettings.ImportMetadata importMetadata = getData().importMetadata().clone().sku(null);
        String metadataRequest = retailerMediaImportStepDef.buildImportCreateMetadataRequest(
                dataItems.get(0).getId(), dataItems.get(1).getId(), OfferProcessTypeEnum.FINALYZE_TASK, importMetadata);
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

    @Test(description = "Неуспешный импорт метадаты по процессу FINALYZE_TASK - не найдены media_id", timeOut = 600000, groups = {"regress", "kafka"})
    @WorkItemIds({"289341"})
    public void unsuccessfulImportMetadataFinalyzeTaskTest() {
        RetailerMediaImportSettings.ImportMetadata importMetadata = getData().importMetadata().clone().sku(null);
        String metadataRequest = retailerMediaImportStepDef.buildImportCreateMetadataRequest(
                new RetailerMediaImportStepDef.Media().mediaId(UUID.randomUUID().toString()), new RetailerMediaImportStepDef.Media().mediaId(UUID.randomUUID().toString()),
                Integer.parseInt(importParameters.externalOfferId()), importParameters.masterSellerId(),
                OfferProcessTypeEnum.ADD_TO_TASK, OfferProcessStatusEnum.BINDED, importMetadata);
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

    @Test(description = "Успешный импорт метадаты по процессу НЕ FINALYZE_TASK", timeOut = 600000, groups = {"regress", "kafka"})
    @WorkItemIds({"289728"})
    public void unsuccessfulImportMetadataNotFinalyzeTaskSkuTest() {
        RetailerMediaImportSettings.ImportMetadata importMetadata = getData().importMetadata().clone().sku(null);
        String metadataRequest = retailerMediaImportStepDef.buildImportCreateMetadataRequest(
                dataItems.get(0).getId(), dataItems.get(1).getId(), OfferProcessTypeEnum.CREATE_TASK, importMetadata);
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
    @WorkItemIds({"289342"})
    public void successImportMetadataCancelTaskTest() {
        RetailerMediaImportSettings.ImportMetadata importMetadata = getData().importMetadata().clone().sku(null);
        String metadataRequest = retailerMediaImportStepDef.buildImportCreateMetadataRequest(
                dataItems.get(0).getId(), OfferProcessTypeEnum.CREATE_TASK, importMetadata);
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
                    () -> compareParameters(importMetadata.taskIdForDam().toString(), getValueFromResponse(response.get(), EXTERNAL_TASK_ID.getPath()), "external_task_id"),
                    () -> mustBeNullParameter(getValueFromResponse(response.get(), SKU.getPath()), "sku")
            );
        });

        metadataRequest = retailerMediaImportStepDef.buildImportCreateCancelMetadataRequest(
                Integer.parseInt(importParameters.externalOfferId()), importParameters.masterSellerId(), OfferProcessTypeEnum.CANCEL_TASK, importMetadata);
        retailerMediaImportStepDef.sendMetadata(metadataRequest);

        dataItems.stream().map(DataItem::getId).forEach(id -> {
            AtomicReference<Response> response = new AtomicReference<>();
            Awaitility.await().atMost(10, TimeUnit.SECONDS).pollInterval(1, TimeUnit.SECONDS).until(() ->
            {
                response.set(metadataStepDef.checkMetadata(id));
                Object externalTaskId = getValueFromResponse(response.get(), EXTERNAL_TASK_ID.getPath());
                return externalTaskId == null;
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
    //@Test(description = "Успешный импорт метадаты по процессу CANCEL_TASK - заполнен SKU", timeOut = 600000, groups = {"regress", "kafka"})
    @WorkItemIds({"289343"})
    public void successImportMetadataCancelTaskSkuTest() {
        RetailerMediaImportSettings.ImportMetadata importMetadata = getData().importMetadata().clone().sku(null);
        String metadataRequest = retailerMediaImportStepDef.buildImportCreateMetadataRequest(
                dataItems.get(0).getId(), OfferProcessTypeEnum.CREATE_TASK, importMetadata);
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
                    () -> compareParameters(importMetadata.taskIdForDam().toString(), getValueFromResponse(response.get(), EXTERNAL_TASK_ID.getPath()), "external_task_id"),
                    () -> mustBeNullParameter(getValueFromResponse(response.get(), SKU.getPath()), "sku")
            );
        });

        dataItems.stream().map(DataItem::getId).
                forEach(id -> metadataStepDef.successEditMetadata(id, SKU.getName(), RandomStringUtils.randomNumeric(3)));

        metadataRequest = retailerMediaImportStepDef.buildImportCreateCancelMetadataRequest(
                Integer.parseInt(importParameters.externalOfferId()), importParameters.masterSellerId(), OfferProcessTypeEnum.CANCEL_TASK, importMetadata);
        retailerMediaImportStepDef.sendMetadata(metadataRequest);

        dataItems.stream().map(DataItem::getId).forEach(id -> {
            Response response = metadataStepDef.checkMetadata(id);

            assertAll(
                    () -> compareParameters(Integer.parseInt(importParameters.externalOfferId()), getValueFromResponse(response, EXTERNAL_OFFER.getPath() + ".id"), "external_offer.id"),
                    () -> compareParameters(importParameters.masterSellerId(), getValueFromResponse(response, MASTER_SELLER.getPath() + ".ext_id"), "master_seller_id"),
                    () -> compareParameters(getData().unknownCategory(), getValueFromResponse(response, MASTER_CATEGORY.getPath() + ".name"), "master_category"),
                    () -> compareParameters(importMetadata.taskIdForDam().toString(), getValueFromResponse(response, EXTERNAL_TASK_ID.getPath()), "external_task_id"),
                    () -> notNullOrEmptyParameter(getValueFromResponse(response, SKU.getPath()), "sku"),
                    () -> notEquals(getValueFromResponse(response, CREATED_AT.getPath()), getValueFromResponse(response, UPDATED_AT.getPath()), "updated_at"),
                    () -> compareParameters(importMetadata.isOwnTrademark(), getValueFromResponse(response, IS_OWN_TRADEMARK.getPath()), "is_own_trademark")
            );
        });
    }

    //todo на удаление, изменилась логика
    //@Test(description = "Успешный импорт метадаты по процессу CANCEL_TASK - не заполнен external_task_id", timeOut = 600000, groups = {"regress", "kafka"})
    @WorkItemIds({"289344"})
    public void successImportMetadataCancelTaskNoExternalTaskIdTest() {
        RetailerMediaImportSettings.ImportMetadata importMetadata = getData().importMetadata().clone().sku(null);

        String metadataRequest = retailerMediaImportStepDef.buildImportCreateCancelMetadataRequest(
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

    //todo на удаление, изменилась логика
    //@Test(description = "Успешный импорт метадаты по процессу REMOVE_FROM_TASK", timeOut = 600000, groups = {"regress", "kafka"})
    @WorkItemIds({"289345"})
    public void successImportMetadataRemoveFromTaskTest() {
        RetailerMediaImportSettings.ImportMetadata importMetadata = getData().importMetadata().clone().sku(null);
        String metadataRequest = retailerMediaImportStepDef.buildImportCreateMetadataRequest(
                dataItems.get(0).getId(), OfferProcessTypeEnum.CREATE_TASK, importMetadata);
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
                    () -> compareParameters(importMetadata.taskIdForDam().toString(), getValueFromResponse(response.get(), EXTERNAL_TASK_ID.getPath()), "external_task_id"),
                    () -> mustBeNullParameter(getValueFromResponse(response.get(), SKU.getPath()), "sku")
            );
        });

        metadataRequest = retailerMediaImportStepDef.buildImportCreateCancelMetadataRequest(
                Integer.parseInt(importParameters.externalOfferId()), importParameters.masterSellerId(), OfferProcessTypeEnum.REMOVE_FROM_TASK, importMetadata);
        retailerMediaImportStepDef.sendMetadata(metadataRequest);

        dataItems.stream().map(DataItem::getId).forEach(id -> {
            AtomicReference<Response> response = new AtomicReference<>();
            Awaitility.await().atMost(10, TimeUnit.SECONDS).pollInterval(1, TimeUnit.SECONDS).until(() ->
            {
                response.set(metadataStepDef.checkMetadata(id));
                Object externalTaskId = getValueFromResponse(response.get(), EXTERNAL_TASK_ID.getPath());
                return externalTaskId == null;
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
    //@Test(description = "Успешный импорт метадаты по процессу REMOVE_FROM_TASK - заполнен SKU", timeOut = 600000, groups = {"regress", "kafka"})
    @WorkItemIds({"289346"})
    public void successImportMetadataCancelRemoveFromTaskSkuTest() {
        RetailerMediaImportSettings.ImportMetadata importMetadata = getData().importMetadata().clone().sku(null);
        String metadataRequest = retailerMediaImportStepDef.buildImportCreateMetadataRequest(
                dataItems.get(0).getId(), OfferProcessTypeEnum.CREATE_TASK, importMetadata);
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
                    () -> compareParameters(importMetadata.taskIdForDam().toString(), getValueFromResponse(response.get(), EXTERNAL_TASK_ID.getPath()), "external_task_id"),
                    () -> mustBeNullParameter(getValueFromResponse(response.get(), SKU.getPath()), "sku")
            );
        });

        dataItems.stream().map(DataItem::getId).
                forEach(id -> metadataStepDef.successEditMetadata(id, SKU.getName(), RandomStringUtils.randomNumeric(3)));

        metadataRequest = retailerMediaImportStepDef.buildImportCreateCancelMetadataRequest(
                Integer.parseInt(importParameters.externalOfferId()), importParameters.masterSellerId(), OfferProcessTypeEnum.REMOVE_FROM_TASK, importMetadata);
        retailerMediaImportStepDef.sendMetadata(metadataRequest);

        dataItems.stream().map(DataItem::getId).forEach(id -> {
            Response response = metadataStepDef.checkMetadata(id);

            assertAll(
                    () -> compareParameters(Integer.parseInt(importParameters.externalOfferId()), getValueFromResponse(response, EXTERNAL_OFFER.getPath() + ".id"), "external_offer.id"),
                    () -> compareParameters(importParameters.masterSellerId(), getValueFromResponse(response, MASTER_SELLER.getPath() + ".ext_id"), "master_seller_id"),
                    () -> compareParameters(getData().unknownCategory(), getValueFromResponse(response, MASTER_CATEGORY.getPath() + ".name"), "master_category"),
                    () -> compareParameters(importMetadata.taskIdForDam().toString(), getValueFromResponse(response, EXTERNAL_TASK_ID.getPath()), "external_task_id"),
                    () -> notNullOrEmptyParameter(getValueFromResponse(response, SKU.getPath()), "sku"),
                    () -> notEquals(getValueFromResponse(response, CREATED_AT.getPath()), getValueFromResponse(response, UPDATED_AT.getPath()), "updated_at"),
                    () -> compareParameters(importMetadata.isOwnTrademark(), getValueFromResponse(response, IS_OWN_TRADEMARK.getPath()), "is_own_trademark")
            );
        });
    }

    //todo на удаление, изменилась логика
    //@Test(description = "Успешный импорт метадаты по процессу REMOVE_FROM_TASK - не заполнен external_task_id", timeOut = 600000, groups = {"regress", "kafka"})
    @WorkItemIds({"289347"})
    public void successImportMetadataCancelTaskNoExternalRemoveFromTaskIdTest() {
        RetailerMediaImportSettings.ImportMetadata importMetadata = getData().importMetadata().clone().sku(null);

        String metadataRequest = retailerMediaImportStepDef.buildImportCreateCancelMetadataRequest(
                Integer.parseInt(importParameters.externalOfferId()), importParameters.masterSellerId(), OfferProcessTypeEnum.REMOVE_FROM_TASK, importMetadata);
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
