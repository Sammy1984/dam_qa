package ru.spice.at.api.end_to_end.test;

import io.qameta.allure.Feature;
import io.qameta.allure.Issue;
import io.qameta.allure.Story;
import ru.testit.annotations.WorkItemIds;
import io.restassured.response.Response;
import org.apache.commons.lang3.RandomStringUtils;
import org.awaitility.Awaitility;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.spice.at.api.dto.request.import_service.ExternalImportData;
import ru.spice.at.api.dto.request.import_service.RequestExternalImport;
import ru.spice.at.api.dto.response.metadata.filtration.DataItem;
import ru.spice.at.api.end_to_end.EndToEndSettings;
import ru.spice.at.api.import_service.ImportServiceStepDef;
import ru.spice.at.api.metadata_service.MetadataStepDef;
import ru.spice.at.api.retailer_media_import_service.RetailerMediaImportSettings;
import ru.spice.at.api.retailer_media_import_service.RetailerMediaImportStepDef;
import ru.spice.at.common.base_test.BaseApiTest;
import ru.spice.at.common.dto.dam.ImageData;
import ru.spice.at.common.emuns.ApiServices;
import ru.spice.at.common.emuns.dam.Status;
import ru.spice.at.common.utils.Assert;
import ru.spice.at.common.utils.CipherHelper;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static ru.spice.at.common.constants.TestConstants.*;
import static ru.spice.at.common.emuns.dam.ImageFormat.*;
import static ru.spice.at.common.emuns.dam.ImageParameters.*;
import static ru.spice.at.common.emuns.dam.Status.ARCHIVE;
import static ru.spice.at.common.emuns.dam.Status.NEW;
import static ru.spice.at.common.utils.Assert.assertAll;
import static ru.spice.at.common.utils.Assert.compareParameters;
import static ru.spice.at.common.utils.ImageHelper.getRandomByteImage;
import static ru.spice.at.common.utils.JsonHelper.getValueFromResponse;

@Feature("End To End")
@Story("Re-import")
public class ReImportEndToEndTests extends BaseApiTest<EndToEndSettings> {
    private final RetailerMediaImportStepDef retailerMediaImportStepDef;
    private final MetadataStepDef metadataStepDef;
    private final ImportServiceStepDef importServiceStepDef;

    private String externalToken;

    protected ReImportEndToEndTests() {
        super(ApiServices.END_TO_END);
        retailerMediaImportStepDef = new RetailerMediaImportStepDef();
        metadataStepDef = new MetadataStepDef();
        importServiceStepDef = new ImportServiceStepDef();
    }

    @BeforeClass(alwaysRun = true)
    public void beforeClass() {
        externalToken = CipherHelper.decrypt(getData().externalToken());
    }

    @AfterClass(alwaysRun = true)
    public void afterClass() {
        metadataStepDef.deleteMetadata();
    }

    @Deprecated
    @Test(description = "Переход файла c ретушью в 'Архивный' статус при повторной загрузке - JPG", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"264272"})
    public void successReImportCheckArchiveJPGTest() {
        ImageData image = new ImageData(JPG);
        byte[] bytes = getRandomByteImage(image.getWidth(), image.getHeight(), image.getFormat().getFormatName());
        List<String> metadataIds = importServiceStepDef.importImages(Collections.singletonMap(image.getFilename(), bytes), true, false);

        AtomicReference<Response> atomicResponse = waitStatus(metadataIds.get(0), ARCHIVE);

        String derivedMetadataId = getValueFromResponse(atomicResponse.get(), DERIVED_METADATA_ID.getPath());
        Response response = metadataStepDef.checkMetadata(derivedMetadataId);
        Assert.compareParameters(NEW.getName(), getValueFromResponse(response, STATUS_NAME.getPath()), "статус");

        List<String> newMetadataIds = importServiceStepDef.importImages(Collections.singletonMap(image.getFilename(), bytes));

        AtomicReference<Response> atomicReResponse = waitStatus(derivedMetadataId, ARCHIVE);

        assertAll(
                () -> compareParameters(ARCHIVE.getName(),
                        getValueFromResponse(metadataStepDef.checkMetadata(metadataIds.get(0)), STATUS_NAME.getPath()), "статус - первичное изображение"),
                () -> compareParameters(ARCHIVE.getName(),
                        getValueFromResponse(atomicReResponse.get(), STATUS_NAME.getPath()), "статус - обработанное изображение"),
                () -> compareParameters(NEW.getName(),
                        getValueFromResponse(metadataStepDef.checkMetadata(newMetadataIds.get(0)), STATUS_NAME.getPath()), "статус - новое первичное изображение")
        );
    }

    @Deprecated
    @Issue("SPC-2824")
    @Test(description = "Переход файла c ретушью в 'Архивный' статус при повторной загрузке - PNG", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"264272"})
    public void successReImportCheckArchivePNGTest() {
        ImageData image = new ImageData(PNG);
        byte[] bytes = getRandomByteImage(image.getWidth(), image.getHeight(), image.getFormat().getFormatName());
        List<String> metadataIds = importServiceStepDef.importImages(Collections.singletonMap(image.getFilename(), bytes), true, false);

        AtomicReference<Response> atomicResponse = waitStatus(metadataIds.get(0), ARCHIVE);

        String derivedMetadataId = getValueFromResponse(atomicResponse.get(), DERIVED_METADATA_ID.getPath());
        Response response = metadataStepDef.checkMetadata(derivedMetadataId);
        Assert.compareParameters(NEW.getName(), getValueFromResponse(response, STATUS_NAME.getPath()), "статус");

        List<String> newMetadataIds = importServiceStepDef.importImages(Collections.singletonMap(image.getFilename(), bytes), true, false);

        AtomicReference<Response> atomicReResponse = waitStatus(newMetadataIds.get(0), ARCHIVE);

        String newDerivedMetadataId = getValueFromResponse(atomicResponse.get(), DERIVED_METADATA_ID.getPath());

        assertAll(
                () -> compareParameters(ARCHIVE.getName(),
                        getValueFromResponse(metadataStepDef.checkMetadata(metadataIds.get(0)), STATUS_NAME.getPath()), "статус - первичное изображение"),
                () -> compareParameters(ARCHIVE.getName(),
                        getValueFromResponse(metadataStepDef.checkMetadata(derivedMetadataId), STATUS_NAME.getPath()), "статус - обработанное изображение"),
                () -> compareParameters(ARCHIVE.getName(),
                        getValueFromResponse(atomicReResponse.get(), STATUS_NAME.getPath()), "статус - новое первичное изображение"),
                () -> compareParameters(NEW.getName(),
                        getValueFromResponse(metadataStepDef.checkMetadata(newDerivedMetadataId), STATUS_NAME.getPath()), "статус - новое обработанное изображение")

        );
    }

    @Test(description = "Повторный ручной импорт файла, загруженного через топик кафки", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"264273"})
    public void successKafkaManualReImportTest() {
        Integer externalOfferId = new Random().nextInt(1000000) + 1000000;

        RetailerMediaImportSettings.ImportParameters importParameters = getData().importParameters().clone().
                mediaName(RandomStringUtils.randomAlphabetic(10) + DOT_VALUE + JPG.getFormatName()).masterSellerId(new Random().nextInt(100) + 1).externalOfferId(externalOfferId.toString());
        retailerMediaImportStepDef.sendMediaFile(retailerMediaImportStepDef.buildImportOneMediaRequest(importParameters));

        List<DataItem> dataItems = metadataStepDef.successMetadataSearch(externalOfferId, 2);

        ImageData image = new ImageData(JPG).setFilename(dataItems.get(0).getFilename());
        byte[] bytes = getRandomByteImage(image.getWidth(), image.getHeight(), image.getFormat().getFormatName());
        List<String> newMetadataIds = importServiceStepDef.importImages(Collections.singletonMap(image.getFilename(), bytes));

        AtomicReference<Response> atomicResponse = waitStatus(newMetadataIds.get(0), NEW);
        assertAll(
                () -> compareParameters(ARCHIVE.getName(),
                        getValueFromResponse(metadataStepDef.checkMetadata(dataItems.get(0).getId()), STATUS_NAME.getPath()), "статус - первичное изображение"),
                () -> compareParameters(ARCHIVE.getName(),
                        getValueFromResponse(metadataStepDef.checkMetadata(dataItems.get(1).getId()), STATUS_NAME.getPath()), "статус - обработанное изображение"),
                () -> compareParameters(NEW.getName(),
                        getValueFromResponse(atomicResponse.get(), STATUS_NAME.getPath()), "статус - новое первичное изображение")

        );
    }

    @Test(description = "Повторный ручной импорт файла, загруженного через внешний импорт", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"264274"})
    public void successExternalManualReImportTest() {
        ImageData image = new ImageData(JPG);
        byte[] bytes = getRandomByteImage(NINETY_NINE, NINETY_NINE, image.getFormat().getFormatName());
        String fileBase64 = new String(Base64.getEncoder().encode(bytes));
        RequestExternalImport requestExternalImport = importServiceStepDef.buildEditRequest(fileBase64, image.getFilename());

        importServiceStepDef.externalImportImage(requestExternalImport, getData().baseExternalUrl(), externalToken);

        ExternalImportData externalImportData = requestExternalImport.externalImportData();
        List<DataItem> dataItems = metadataStepDef.successMetadataSearch(externalImportData.filename().split("\\.", 2)[0], 2);

        List<String> newMetadataIds = importServiceStepDef.importImages(Collections.singletonMap(image.getFilename(), bytes));

        AtomicReference<Response> atomicResponse = waitStatus(newMetadataIds.get(0), NEW);
        assertAll(
                () -> compareParameters(ARCHIVE.getName(),
                        getValueFromResponse(metadataStepDef.checkMetadata(dataItems.get(0).getId()), STATUS_NAME.getPath()), "статус - первичное изображение"),
                () -> compareParameters(ARCHIVE.getName(),
                        getValueFromResponse(metadataStepDef.checkMetadata(dataItems.get(1).getId()), STATUS_NAME.getPath()), "статус - обработанное изображение"),
                () -> compareParameters(NEW.getName(),
                        getValueFromResponse(atomicResponse.get(), STATUS_NAME.getPath()), "статус - новое первичное изображение")
        );
    }

    @Test(description = "Добавление тегов при повторной загрузке", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"264286"})
    public void successReImportKeywordsTest() {
        ImageData image = new ImageData(JPG);
        byte[] bytes = getRandomByteImage(image.getWidth(), image.getHeight(), image.getFormat().getFormatName());
        List<String> metadataIds = importServiceStepDef.importImages(Collections.singletonMap(image.getFilename(), bytes));

        List<String> keywords = new ArrayList<String>() {{
            add(RandomStringUtils.randomAlphabetic(6));
            add(RandomStringUtils.randomAlphabetic(15));
            add(RandomStringUtils.randomAlphabetic(3));
            add(CYRILLIC_VALUE);
        }};
        Response response = metadataStepDef.successEditMetadata(metadataIds.get(0), KEYWORDS.getName(), keywords);
        Assert.compareParameters(keywords, getValueFromResponse(response, KEYWORDS.getPath()), "keywords");

        List<String> newKeywords = new ArrayList<String>() {{
            add(RandomStringUtils.randomAlphabetic(7));
            add(RandomStringUtils.randomAlphabetic(2));
            add(RandomStringUtils.randomAlphabetic(10));
            add(CYRILLIC_VALUE);
        }};

        String businessMetadataId = importServiceStepDef.successImportBusinessMetadata(KEYWORDS.getName(), newKeywords);
        String newMetadataId = importServiceStepDef.importRandomImageWithBusinessMetadata(image.getFilename(), bytes, businessMetadataId);

        response = metadataStepDef.checkMetadata(newMetadataId);
        keywords.addAll(newKeywords);
        Assert.compareParameters(keywords, getValueFromResponse(response, KEYWORDS.getPath()), "keywords");
    }

    public AtomicReference<Response> waitStatus(String metadataId, Status status) {
        AtomicReference<Response> atomicResponse = new AtomicReference<>();
        Awaitility.await(String.format("Статус не перешел в '%s'", status.getName())).
                atMost(20, TimeUnit.SECONDS).pollInterval(1, TimeUnit.SECONDS).until(() ->
                {
                    atomicResponse.set(metadataStepDef.checkMetadata(metadataId));
                    String actualStatus = getValueFromResponse(atomicResponse.get(), STATUS_NAME.getPath());
                    return actualStatus.equals(status.getName());
                });

        return atomicResponse;
    }
}