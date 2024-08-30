package ru.spice.at.ui.dam_pims_integration.test;

import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import ru.testit.annotations.WorkItemIds;
import io.restassured.response.Response;
import org.apache.commons.lang3.RandomStringUtils;
import org.awaitility.Awaitility;
import org.testng.annotations.*;
import ru.spice.at.api.dto.response.metadata.filtration.FiltrationResponse;
import ru.spice.at.api.metadata_service.MetadataStepDef;
import ru.spice.at.api.retailer_media_import_service.RetailerMediaImportSettings;
import ru.spice.at.api.retailer_media_import_service.RetailerMediaImportStepDef;
import ru.spice.at.common.base_test.BaseUiTest;
import ru.spice.at.common.emuns.UiCategories;
import ru.spice.at.common.listeners.TestAllureListener;
import ru.spice.at.common.utils.Assert;
import ru.spice.at.ui.dam_pims_integration.DamPimsIntegrationSettings;
import ru.spice.at.ui.dam_pims_integration.DamPimsIntegrationStepDef;

import java.util.Collections;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static ru.spice.at.common.constants.TestConstants.DOT_VALUE;
import static ru.spice.at.common.emuns.dam.ImageFormat.JPG;
import static ru.spice.at.common.emuns.dam.ImageParameters.*;
import static ru.spice.at.common.emuns.dam.SearchParameters.SEARCH;
import static ru.spice.at.common.utils.Assert.*;
import static ru.spice.at.common.utils.JsonHelper.getValueFromResponse;

@Feature("DAM PIMS integration")
@Story("PIMS integration")
@Listeners({TestAllureListener.class})
public class DamPimsIntegrationTests extends BaseUiTest<DamPimsIntegrationSettings> {
    private DamPimsIntegrationStepDef damPimsIntegrationStepDef;
    private RetailerMediaImportStepDef retailerMediaImportStepDef;
    private MetadataStepDef metadataStepDef;

    protected DamPimsIntegrationTests() {
        super(UiCategories.DAM_PIMS_INTEGRATION);
    }

    @BeforeClass(alwaysRun = true)
    public void beforeClass() {
        damPimsIntegrationStepDef = new DamPimsIntegrationStepDef(getWebDriver());
        retailerMediaImportStepDef = new RetailerMediaImportStepDef();
        metadataStepDef = new MetadataStepDef();
    }

    @BeforeMethod(alwaysRun = true)
    public void beforeMethod() {
        String url = getData().pimsUrl() + String.format(getData().taskEndpoint(), getData().taskId());
        damPimsIntegrationStepDef.urlAuthorization(url, getData().pimsLogin(), getData().pimsEncryptPassword());
    }

    @AfterMethod(alwaysRun = true)
    public void afterMethod() {
        damPimsIntegrationStepDef.deleteOffers();
        metadataStepDef.deleteMetadata();
        metadataStepDef.setAuthToken(null);
    }

    @Test(description = "Успешное добавление оффера", timeOut = 6000000, groups = {"regress"})
    @WorkItemIds({"239092"})
    public void successIntegrationAddTest() {
        String mediaName = RandomStringUtils.randomAlphabetic(10) + DOT_VALUE + JPG.getFormatName();
        DamPimsIntegrationSettings.Offer offer = getData().offers().get(0);

        RetailerMediaImportSettings.ImportParameters importParameters = getData().importParameters().clone().
                mediaName(mediaName).
                externalOfferId(offer.externalOfferId()).
                masterSellerId(Integer.parseInt(offer.masterSellerId())).
                mediaUrl(getData().mediaUrls().get(0));
        retailerMediaImportStepDef.sendMediaFile(retailerMediaImportStepDef.buildImportOneMediaRequest(importParameters));

        FiltrationResponse filtrationResponse = metadataStepDef.successFiltrationMetadata(Collections.singletonMap(SEARCH.getName(), mediaName), false).
                extract().as(FiltrationResponse.class);
        Assert.notNullOrEmptyParameter(filtrationResponse.getData().size(), "список файлов");

        damPimsIntegrationStepDef.addOffers(getData().taskId(), offer.offerId());

        AtomicReference<Response> response = new AtomicReference<>();
        Awaitility.await().atMost(10, TimeUnit.SECONDS).pollInterval(1, TimeUnit.SECONDS).until(() ->
        {
            response.set(metadataStepDef.checkMetadata(filtrationResponse.getData().get(0).getId()));
            return getValueFromResponse(response.get(), EXTERNAL_TASK_ID.getPath()) != null;
        });

        assertAll(
                () -> compareParameters(offer.externalOfferId(), getValueFromResponse(response.get(), EXTERNAL_OFFER.getPath() + ".id").toString(), "external_offer.id"),
                () -> compareParameters(offer.masterSellerId(), getValueFromResponse(response.get(), MASTER_SELLER_ID.getPath()).toString(), "master_seller_id"),
                () -> compareParameters(getData().taskId(), getValueFromResponse(response.get(), EXTERNAL_TASK_ID.getPath()).toString(), "external_task_id"),
                () -> compareParameters(offer.sku(), getValueFromResponse(response.get(), SKU.getPath()), "sku"),
                () -> compareParameters(offer.isOwnTrademark(), getValueFromResponse(response.get(), IS_OWN_TRADEMARK.getPath()), "is_own_trademark"),
                () -> compareParameters(offer.draftDone(), getValueFromResponse(response.get(), EXTERNAL_DRAFT_DONE.getPath()), "external_draft_done")
        );
    }

    @Test(description = "Успешное добавление оффера с sku = null", timeOut = 6000000, groups = {"regress"})
    @WorkItemIds({"239094"})
    public void successIntegrationAddSkuNullTest() {
        String mediaName = RandomStringUtils.randomAlphabetic(10) + DOT_VALUE + JPG.getFormatName();
        DamPimsIntegrationSettings.Offer offer = getData().offers().get(1);

        RetailerMediaImportSettings.ImportParameters importParameters = getData().importParameters().clone().
                mediaName(mediaName).
                externalOfferId(offer.externalOfferId()).
                masterSellerId(Integer.parseInt(offer.masterSellerId())).
                mediaUrl(getData().mediaUrls().get(1));
        retailerMediaImportStepDef.sendMediaFile(retailerMediaImportStepDef.buildImportOneMediaRequest(importParameters));

        FiltrationResponse filtrationResponse = metadataStepDef.successFiltrationMetadata(Collections.singletonMap(SEARCH.getName(), mediaName), false).
                extract().as(FiltrationResponse.class);
        Assert.notNullOrEmptyParameter(filtrationResponse.getData().size(), "список файлов");

        damPimsIntegrationStepDef.addOffers(getData().taskId(), offer.offerId());

        AtomicReference<Response> response = new AtomicReference<>();
        Awaitility.await().atMost(10, TimeUnit.SECONDS).pollInterval(1, TimeUnit.SECONDS).until(() ->
        {
            response.set(metadataStepDef.checkMetadata(filtrationResponse.getData().get(0).getId()));
            return getValueFromResponse(response.get(), EXTERNAL_TASK_ID.getPath()) != null;
        });

        assertAll(
                () -> compareParameters(offer.externalOfferId(), getValueFromResponse(response.get(), EXTERNAL_OFFER.getPath() + ".id").toString(), "external_offer.id"),
                () -> compareParameters(offer.masterSellerId(), getValueFromResponse(response.get(), MASTER_SELLER_ID.getPath()).toString(), "master_seller_id"),
                () -> compareParameters(getData().taskId(), getValueFromResponse(response.get(), EXTERNAL_TASK_ID.getPath()).toString(), "external_task_id"),
                () -> mustBeNullParameter(getValueFromResponse(response.get(), SKU.getPath()), "sku"),
                () -> compareParameters(offer.isOwnTrademark(), getValueFromResponse(response.get(), IS_OWN_TRADEMARK.getPath()), "is_own_trademark"),
                () -> compareParameters(offer.draftDone(), getValueFromResponse(response.get(), EXTERNAL_DRAFT_DONE.getPath()), "external_draft_done")
        );
    }

    @Test(description = "Успешное удаление оффера", timeOut = 6000000, groups = {"regress"})
    @WorkItemIds({"239095"})
    public void successIntegrationDeleteTest() {
        String mediaName = RandomStringUtils.randomAlphabetic(10) + DOT_VALUE + JPG.getFormatName();
        DamPimsIntegrationSettings.Offer offer = getData().offers().get(0);

        RetailerMediaImportSettings.ImportParameters importParameters = getData().importParameters().clone().
                mediaName(mediaName).
                externalOfferId(offer.externalOfferId()).
                masterSellerId(Integer.parseInt(offer.masterSellerId())).
                mediaUrl(getData().mediaUrls().get(2));
        retailerMediaImportStepDef.sendMediaFile(retailerMediaImportStepDef.buildImportOneMediaRequest(importParameters));

        FiltrationResponse filtrationResponse = metadataStepDef.successFiltrationMetadata(Collections.singletonMap(SEARCH.getName(), mediaName), false).
                extract().as(FiltrationResponse.class);
        Assert.notNullOrEmptyParameter(filtrationResponse.getData().size(), "список файлов");

        damPimsIntegrationStepDef.addOffers(getData().taskId(), offer.offerId());

        AtomicReference<Response> response = new AtomicReference<>();
        Awaitility.await().atMost(10, TimeUnit.SECONDS).pollInterval(1, TimeUnit.SECONDS).until(() ->
        {
            response.set(metadataStepDef.checkMetadata(filtrationResponse.getData().get(0).getId()));
            return getValueFromResponse(response.get(), EXTERNAL_TASK_ID.getPath()) != null;
        });

        assertAll(
                () -> compareParameters(offer.externalOfferId(), getValueFromResponse(response.get(), EXTERNAL_OFFER.getPath() + ".id").toString(), "external_offer.id"),
                () -> compareParameters(offer.masterSellerId(), getValueFromResponse(response.get(), MASTER_SELLER_ID.getPath()).toString(), "master_seller_id"),
                () -> compareParameters(getData().taskId(), getValueFromResponse(response.get(), EXTERNAL_TASK_ID.getPath()).toString(), "external_task_id"),
                () -> compareParameters(offer.sku(), getValueFromResponse(response.get(), SKU.getPath()), "sku"),
                () -> compareParameters(offer.isOwnTrademark(), getValueFromResponse(response.get(), IS_OWN_TRADEMARK.getPath()), "is_own_trademark"),
                () -> compareParameters(offer.draftDone(), getValueFromResponse(response.get(), EXTERNAL_DRAFT_DONE.getPath()), "external_draft_done")
        );

        damPimsIntegrationStepDef.deleteOffers();

        Awaitility.await().atMost(10, TimeUnit.SECONDS).pollInterval(1, TimeUnit.SECONDS).until(() ->
        {
            response.set(metadataStepDef.checkMetadata(filtrationResponse.getData().get(0).getId()));
            return getValueFromResponse(response.get(), EXTERNAL_TASK_ID.getPath()) == null;
        });
    }
}