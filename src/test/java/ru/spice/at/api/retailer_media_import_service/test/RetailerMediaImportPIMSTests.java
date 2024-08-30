package ru.spice.at.api.retailer_media_import_service.test;

import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import ru.testit.annotations.WorkItemIds;
import io.restassured.response.Response;
import org.apache.commons.lang3.RandomStringUtils;
import org.awaitility.Awaitility;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import ru.spice.at.api.dto.response.metadata.filtration.DataItem;
import ru.spice.at.api.metadata_service.MetadataStepDef;
import ru.spice.at.api.retailer_media_import_service.RetailerMediaImportSettings;
import ru.spice.at.api.retailer_media_import_service.RetailerMediaImportStepDef;
import ru.spice.at.common.base_test.BaseApiTest;
import ru.spice.at.common.emuns.ApiServices;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static ru.spice.at.common.constants.TestConstants.*;
import static ru.spice.at.common.emuns.dam.ImageFormat.JPG;
import static ru.spice.at.common.emuns.dam.ImageParameters.*;
import static ru.spice.at.common.utils.Assert.*;
import static ru.spice.at.common.utils.JsonHelper.getValueFromResponse;

@Deprecated
@Feature("Retailer Media Import Service (RMIS)")
@Story("PIMS - Import metadata")
public class RetailerMediaImportPIMSTests extends BaseApiTest<RetailerMediaImportSettings> {
    private final RetailerMediaImportStepDef retailerMediaImportStepDef;
    private final MetadataStepDef metadataStepDef;

    private String mediaName;
    private Integer externalOfferId;
    private Integer masterSellerId;

    protected RetailerMediaImportPIMSTests() {
        super(ApiServices.RETAILER_MEDIA_IMPORT_SERVICE);
        retailerMediaImportStepDef = new RetailerMediaImportStepDef();
        metadataStepDef = new MetadataStepDef();
    }

    @BeforeMethod(alwaysRun = true)
    public void beforeMethod() {
        mediaName = RandomStringUtils.randomAlphabetic(10) + DOT_VALUE + JPG.getFormatName();
        externalOfferId = new Random().nextInt(1000000) + 1000000;
        masterSellerId = new Random().nextInt(100) + 1;
    }

    //todo выключено для использования параллельного сьюта
    //@AfterClass(alwaysRun = true)
    public void afterClass() {
        metadataStepDef.deleteMetadata();
    }

    //todo тест устарел
    //@Test(description = "Успешный импорт метадаты - один файл", timeOut = 600000, groups = {"regress", "kafka"})
    @WorkItemIds({"230654"})
    public void successSingeImportMetadataTest() {
        RetailerMediaImportSettings.ImportParameters importParameters = getData().importParameters().clone().
                mediaName(mediaName).masterSellerId(masterSellerId).externalOfferId(externalOfferId.toString());
        retailerMediaImportStepDef.sendMediaFile(retailerMediaImportStepDef.buildImportOneMediaRequest(importParameters));

        List<DataItem> dataItems = metadataStepDef.successMetadataSearch(externalOfferId);

        String metadataRequest = retailerMediaImportStepDef.buildImportMetadataRequest(externalOfferId, masterSellerId, getData().importMetadata());
        retailerMediaImportStepDef.sendMetadata(metadataRequest);

        Awaitility.await().atMost(10, TimeUnit.SECONDS).pollInterval(1, TimeUnit.SECONDS).until(() ->
        {
            Response response = metadataStepDef.checkMetadata(dataItems.get(0).getId());
            String sku = getValueFromResponse(response, SKU.getPath());
            return sku != null && !sku.equals(EMPTY_VALUE);
        });

        dataItems.stream().map(DataItem::getId).forEach(id -> {
            AtomicReference<Response> response = new AtomicReference<>();
            Awaitility.await().atMost(10, TimeUnit.SECONDS).pollInterval(1, TimeUnit.SECONDS).until(() ->
            {
                response.set(metadataStepDef.checkMetadata(id));
                String sku = getValueFromResponse(response.get(), SKU.getPath());
                return sku != null && !sku.equals(EMPTY_VALUE);
            });

            assertAll(
                    () -> compareParameters(externalOfferId, getValueFromResponse(response.get(), EXTERNAL_OFFER.getPath() + ".id"), "external_offer.id"),
                    () -> compareParameters(masterSellerId, getValueFromResponse(response.get(), MASTER_SELLER.getPath() + ".ext_id"), "master_seller_id"),
                    () -> compareParameters(getData().importMetadata().sku().toString(), getValueFromResponse(response.get(), SKU.getPath()), "sku"),
                    () -> compareParameters(getData().importMetadata().isOwnTrademark(), getValueFromResponse(response.get(), IS_OWN_TRADEMARK.getPath()), "is_own_trademark"),
                    () -> compareParameters(getData().importMetadata().draftDone(), getValueFromResponse(response.get(), EXTERNAL_DRAFT_DONE.getPath()), "external_draft_done")
            );
        });
    }

    //todo тест устарел
    //@Test(description = "Успешный импорт метадаты - несколько файлов", timeOut = 600000, groups = {"regress", "kafka"})
    @WorkItemIds({"230658"})
    public void successMultiImportMetadataTest() {
        List<String> namesList = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            String name = RandomStringUtils.randomAlphabetic(10) + DOT_VALUE + JPG.getFormatName();
            namesList.add(name);
        }

        RetailerMediaImportSettings.ImportParameters importParameters = getData().importParameters().clone()
                .masterSellerId(masterSellerId).externalOfferId(externalOfferId.toString());
        retailerMediaImportStepDef.sendMediaFile(retailerMediaImportStepDef.buildImportSomeMediaWithParamsRequest(namesList, importParameters));

        List<DataItem> dataItems = metadataStepDef.successMetadataSearch(externalOfferId, 6);

        String metadataRequest = retailerMediaImportStepDef.buildImportMetadataRequest(externalOfferId, masterSellerId, getData().importMetadata());
        retailerMediaImportStepDef.sendMetadata(metadataRequest);

        dataItems.stream().map(DataItem::getId).forEach(id -> {
            AtomicReference<Response> response = new AtomicReference<>();
            Awaitility.await().atMost(10, TimeUnit.SECONDS).pollInterval(1, TimeUnit.SECONDS).until(() ->
            {
                response.set(metadataStepDef.checkMetadata(id));
                String sku = getValueFromResponse(response.get(), SKU.getPath());
                return sku != null && !sku.equals(EMPTY_VALUE);
            });

            assertAll(
                    () -> compareParameters(externalOfferId, getValueFromResponse(response.get(), EXTERNAL_OFFER.getPath() + ".id"), "external_offer.id"),
                    () -> compareParameters(masterSellerId, getValueFromResponse(response.get(), MASTER_SELLER.getPath() + ".ext_id"), "master_seller_id"),
                    () -> compareParameters(getData().importMetadata().sku().toString(), getValueFromResponse(response.get(), SKU.getPath()), "sku"),
                    () -> compareParameters(getData().importMetadata().isOwnTrademark(), getValueFromResponse(response.get(), IS_OWN_TRADEMARK.getPath()), "is_own_trademark"),
                    () -> compareParameters(getData().importMetadata().draftDone(), getValueFromResponse(response.get(), EXTERNAL_DRAFT_DONE.getPath()), "external_draft_done")
            );
        });
    }

    //todo тест устарел
    //@Test(description = "Успешный импорт метадаты sku = null", timeOut = 600000, groups = {"regress", "kafka"})
    @WorkItemIds({"230659"})
    public void successSingeImportMetadataSkuNullTest() {
        RetailerMediaImportSettings.ImportParameters importParameters = getData().importParameters().clone().
                mediaName(mediaName).masterSellerId(masterSellerId).externalOfferId(externalOfferId.toString());
        retailerMediaImportStepDef.sendMediaFile(retailerMediaImportStepDef.buildImportOneMediaRequest(importParameters));

        List<DataItem> dataItems = metadataStepDef.successMetadataSearch(externalOfferId);

        String metadataRequest = retailerMediaImportStepDef.buildImportMetadataRequest(
                externalOfferId, masterSellerId, getData().importMetadata().clone().sku(null));
        retailerMediaImportStepDef.sendMetadata(metadataRequest);

        dataItems.stream().map(DataItem::getId).forEach(id -> {
            AtomicReference<Response> response = new AtomicReference<>();
            Awaitility.await().atMost(10, TimeUnit.SECONDS).pollInterval(1, TimeUnit.SECONDS).until(() ->
            {
                response.set(metadataStepDef.checkMetadata(id));
                Boolean isOwnTrademark = getValueFromResponse(response.get(), IS_OWN_TRADEMARK.getPath());
                return isOwnTrademark != null && isOwnTrademark.equals(getData().importMetadata().isOwnTrademark());
            });

            assertAll(
                    () -> compareParameters(externalOfferId, getValueFromResponse(response.get(), EXTERNAL_OFFER.getPath() + ".id"), "external_offer.id"),
                    () -> compareParameters(masterSellerId, getValueFromResponse(response.get(), MASTER_SELLER.getPath() + ".ext_id"), "master_seller_id"),
                    () -> mustBeNullParameter(getValueFromResponse(response.get(), SKU.getPath()), "sku"),
                    () -> compareParameters(getData().importMetadata().isOwnTrademark(), getValueFromResponse(response.get(), IS_OWN_TRADEMARK.getPath()), "is_own_trademark"),
                    () -> compareParameters(getData().importMetadata().draftDone(), getValueFromResponse(response.get(), EXTERNAL_DRAFT_DONE.getPath()), "external_draft_done")
            );
        });
    }

    //todo тест устарел
    //@Test(description = "Неуспешный импорт метадаты - не совпадает externalOfferId", timeOut = 600000, groups = {"regress", "kafka"})
    @WorkItemIds({"230661"})
    public void unsuccessfulSingeImportMetadataExternalOfferIdTest() {
        RetailerMediaImportSettings.ImportParameters importParameters = getData().importParameters().clone().
                mediaName(mediaName).masterSellerId(masterSellerId).externalOfferId(externalOfferId.toString());
        retailerMediaImportStepDef.sendMediaFile(retailerMediaImportStepDef.buildImportOneMediaRequest(importParameters));

        List<DataItem> dataItems = metadataStepDef.successMetadataSearch(externalOfferId);

        String metadataRequest = retailerMediaImportStepDef.buildImportMetadataRequest(
                externalOfferId + 10, masterSellerId, getData().importMetadata());
        retailerMediaImportStepDef.sendMetadata(metadataRequest);

        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        dataItems.stream().map(DataItem::getId).forEach(id -> {
            Response response = metadataStepDef.checkMetadata(id);
            assertAll(
                    () -> compareParameters(externalOfferId, getValueFromResponse(response, EXTERNAL_OFFER.getPath() + ".id"), "external_offer.id"),
                    () -> compareParameters(masterSellerId, getValueFromResponse(response, MASTER_SELLER.getPath() + ".ext_id"), "master_seller_id"),
                    () -> mustBeNullParameter(getValueFromResponse(response, EXTERNAL_TASK_ID.getPath()), "external_task_id"),
                    () -> mustBeNullParameter(getValueFromResponse(response, SKU.getPath()), "sku"),
                    () -> mustBeNullParameter(getValueFromResponse(response, EXTERNAL_DRAFT_DONE.getPath()), "external_draft_done")
            );
        });
    }

    //todo тест устарел
    //@Test(description = "Неуспешный импорт метадаты - не совпадает masterSellerId", timeOut = 600000, groups = {"regress", "kafka"})
    @WorkItemIds({"230662"})
    public void unsuccessfulSingeImportMetadataMasterSellerIdTest() {
        RetailerMediaImportSettings.ImportParameters importParameters = getData().importParameters().clone().
                mediaName(mediaName).masterSellerId(masterSellerId).externalOfferId(externalOfferId.toString());
        retailerMediaImportStepDef.sendMediaFile(retailerMediaImportStepDef.buildImportOneMediaRequest(importParameters));

        List<DataItem> dataItems = metadataStepDef.successMetadataSearch(externalOfferId);

        String metadataRequest = retailerMediaImportStepDef.buildImportMetadataRequest(
                externalOfferId, masterSellerId + 10, getData().importMetadata());
        retailerMediaImportStepDef.sendMetadata(metadataRequest);

        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        dataItems.stream().map(DataItem::getId).forEach(id -> {
            Response response = metadataStepDef.checkMetadata(id);
            assertAll(
                    () -> compareParameters(externalOfferId, getValueFromResponse(response, EXTERNAL_OFFER.getPath() + ".id"), "external_offer.id"),
                    () -> compareParameters(masterSellerId, getValueFromResponse(response, MASTER_SELLER.getPath() + ".ext_id"), "master_seller_id"),
                    () -> mustBeNullParameter(getValueFromResponse(response, EXTERNAL_TASK_ID.getPath()), "external_task_id"),
                    () -> mustBeNullParameter(getValueFromResponse(response, SKU.getPath()), "sku"),
                    () -> mustBeNullParameter(getValueFromResponse(response, EXTERNAL_DRAFT_DONE.getPath()), "external_draft_done")
            );
        });
    }

    //todo тест устарел
    //@Test(description = "Неуспешный импорт метадаты - не совпадает externalOfferId и masterSellerId", timeOut = 600000, groups = {"regress", "kafka"})
    @WorkItemIds({"230663"})
    public void unsuccessfulSingeImportMetadataExternalOfferIdMasterSellerIdTest() {
        RetailerMediaImportSettings.ImportParameters importParameters = getData().importParameters().clone().
                mediaName(mediaName).masterSellerId(masterSellerId).externalOfferId(externalOfferId.toString());
        retailerMediaImportStepDef.sendMediaFile(retailerMediaImportStepDef.buildImportOneMediaRequest(importParameters));

        List<DataItem> dataItems = metadataStepDef.successMetadataSearch(externalOfferId);

        String metadataRequest = retailerMediaImportStepDef.buildImportMetadataRequest(
                externalOfferId + 10, masterSellerId + 10, getData().importMetadata());
        retailerMediaImportStepDef.sendMetadata(metadataRequest);

        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        dataItems.stream().map(DataItem::getId).forEach(id -> {
            Response response = metadataStepDef.checkMetadata(id);
            assertAll(
                    () -> compareParameters(externalOfferId, getValueFromResponse(response, EXTERNAL_OFFER.getPath() + ".id"), "external_offer.id"),
                    () -> compareParameters(masterSellerId, getValueFromResponse(response, MASTER_SELLER.getPath() + ".ext_id"), "master_seller_id"),
                    () -> mustBeNullParameter(getValueFromResponse(response, EXTERNAL_TASK_ID.getPath()), "external_task_id"),
                    () -> mustBeNullParameter(getValueFromResponse(response, SKU.getPath()), "sku"),
                    () -> mustBeNullParameter(getValueFromResponse(response, EXTERNAL_DRAFT_DONE.getPath()), "external_draft_done")
            );
        });
    }
}
