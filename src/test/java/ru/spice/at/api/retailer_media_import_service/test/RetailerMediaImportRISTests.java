package ru.spice.at.api.retailer_media_import_service.test;

import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import ru.testit.annotations.WorkItemIds;
import io.restassured.response.Response;
import org.apache.commons.lang3.RandomStringUtils;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import ru.spice.at.api.dto.response.metadata.filtration.DataItem;
import ru.spice.at.api.dto.response.metadata.filtration.FiltrationResponse;
import ru.spice.at.api.metadata_service.MetadataStepDef;
import ru.spice.at.api.retailer_media_import_service.RetailerMediaImportSettings;
import ru.spice.at.api.retailer_media_import_service.RetailerMediaImportStepDef;
import ru.spice.at.common.base_test.BaseApiTest;
import ru.spice.at.common.dto.dam.ImageData;
import ru.spice.at.common.emuns.ApiServices;
import ru.spice.at.common.emuns.dam.Status;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import static ru.spice.at.common.constants.TestConstants.*;
import static ru.spice.at.common.emuns.dam.ImageFormat.*;
import static ru.spice.at.common.emuns.dam.ImageParameters.*;
import static ru.spice.at.common.emuns.dam.SearchParameters.SEARCH;
import static ru.spice.at.common.utils.Assert.*;
import static ru.spice.at.common.utils.JsonHelper.getValueFromResponse;

@Feature("Retailer Media Import Service (RMIS)")
@Story("RIS - Import media")
public class RetailerMediaImportRISTests extends BaseApiTest<RetailerMediaImportSettings> {
    private final RetailerMediaImportStepDef retailerMediaImportStepDef;
    private final MetadataStepDef metadataStepDef;

    private String mediaName;
    private Integer externalOfferId;
    private Integer masterSellerId;

    protected RetailerMediaImportRISTests() {
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

    @Test(description = "Успешный импорт одного медиафайла", timeOut = 600000, groups = {"regress", "kafka"})
    @WorkItemIds({"230152"})
    public void successSingeImportTest() {
        RetailerMediaImportSettings.ImportParameters importParameters = getData().importParameters().clone().
                mediaName(mediaName).masterSellerId(masterSellerId).externalOfferId(externalOfferId.toString());
        retailerMediaImportStepDef.sendMediaFile(retailerMediaImportStepDef.buildImportOneMediaRequest(importParameters));

        List<DataItem> dataItems = metadataStepDef.successMetadataSearch(externalOfferId, false);

        Response response = metadataStepDef.checkMetadata(dataItems.get(0).getId());

        //todo починить взаимодействие с бд
        //List<MetadataDto> metadata = DbHelper.createDbo(MetadataDao.class).getMetadataByMasterSellerId(importParameters.masterSellerId());

        assertAll(
                //() -> compareParameters(id, metadata.get(0).id(), "id (бд)"),
                () -> compareParameters(importParameters.externalOfferId(), getValueFromResponse(response, EXTERNAL_OFFER.getPath() + ".id").toString(), "external_offer.id"),
                () -> compareParameters(importParameters.externalOfferName(), getValueFromResponse(response, EXTERNAL_OFFER.getPath() + ".name"), "external_offer.name"),
                () -> compareParameters(importParameters.mediaName(), getValueFromResponse(response, ORIGIN_FILENAME.getPath()), "origin_filename"),
                () -> compareParameters(String.format(RMIS_FILENAME_MASK, masterSellerId, externalOfferId, 1, JPG.getFormatName()), getValueFromResponse(response, FILENAME.getPath()), "filename"),
                () -> compareParameters(importParameters.mediaPriority() + 1, getValueFromResponse(response, PRIORITY.getPath()), "priority")
        );
    }

    @Test(description = "Успешный импорт одного медиафайла (длинное название)", timeOut = 600000, groups = {"regress", "kafka"})
    @WorkItemIds({"230152"})
    public void successSingeImportLongNameTest() {
        mediaName = RandomStringUtils.randomAlphabetic(MAX_JPG_FILENAME_SYMBOLS + 5) + DOT_VALUE + JPG.getFormatName();
        RetailerMediaImportSettings.ImportParameters importParameters = getData().importParameters().clone().
                mediaName(mediaName).masterSellerId(masterSellerId).externalOfferId(externalOfferId.toString());
        retailerMediaImportStepDef.sendMediaFile(retailerMediaImportStepDef.buildImportOneMediaRequest(importParameters));

        List<DataItem> dataItems = metadataStepDef.successMetadataSearch(externalOfferId, false);

        Response response = metadataStepDef.checkMetadata(dataItems.get(0).getId());

        assertAll(
                () -> compareParameters(importParameters.externalOfferId(), getValueFromResponse(response, EXTERNAL_OFFER.getPath() + ".id").toString(), "external_offer.id"),
                () -> compareParameters(importParameters.externalOfferName(), getValueFromResponse(response, EXTERNAL_OFFER.getPath() + ".name"), "external_offer.name"),
                () -> compareParameters(importParameters.mediaName(), getValueFromResponse(response, ORIGIN_FILENAME.getPath()), "origin_filename"),
                () -> compareParameters(String.format(RMIS_FILENAME_MASK, masterSellerId, externalOfferId, 1, JPG.getFormatName()), getValueFromResponse(response, FILENAME.getPath()), "filename"),
                () -> compareParameters(importParameters.mediaPriority() + 1, getValueFromResponse(response, PRIORITY.getPath()), "priority")
        );
    }

    @Test(description = "Успешный импорт одного медиафайла (проверка ретуши)", timeOut = 600000, groups = {"regress", "kafka"})
    @WorkItemIds({"230152"})
    public void successSingeImportRetouchTest() {
        RetailerMediaImportSettings.ImportParameters importParameters = getData().importParameters().clone().
                mediaName(mediaName).masterSellerId(masterSellerId).externalOfferId(externalOfferId.toString());
        retailerMediaImportStepDef.sendMediaFile(retailerMediaImportStepDef.buildImportOneMediaRequest(importParameters));

        List<DataItem> dataItems = metadataStepDef.successMetadataSearch(externalOfferId, 2);

        List<String> ids = dataItems.stream().map(DataItem::getId).collect(Collectors.toList());
        List<Response> metadataList = ids.stream().map(metadataStepDef::checkMetadata).collect(Collectors.toList());

        //todo добавить валидацию /processings
        equalsTrueParameter((getValueFromResponse(metadataList.get(0), STATUS_NAME.getPath()).equals(Status.NEW.getName()) && getValueFromResponse(metadataList.get(1), STATUS_NAME.getPath()).equals(Status.ARCHIVE.getName())) ||
                        (getValueFromResponse(metadataList.get(1), STATUS_NAME.getPath()).equals(Status.NEW.getName()) && getValueFromResponse(metadataList.get(0), STATUS_NAME.getPath()).equals(Status.ARCHIVE.getName())),
                "статусы"
        );
    }

    @Test(description = "Успешный импорт дубликата медиафайла (одинаковые ссылки)", timeOut = 600000, groups = {"regress", "kafka"})
    @WorkItemIds({"237139"})
    public void successDuplicateImportTest() {
        RetailerMediaImportSettings.ImportParameters importParameters = getData().importParameters().clone().
                mediaName(mediaName).masterSellerId(masterSellerId).externalOfferId(externalOfferId.toString());
        retailerMediaImportStepDef.sendMediaFile(retailerMediaImportStepDef.buildImportOneMediaRequest(importParameters));

        List<DataItem> dataItems = metadataStepDef.successMetadataSearch(externalOfferId, false);

        Response response = metadataStepDef.checkMetadata(dataItems.get(0).getId());
        assertAll(
                () -> compareParameters(importParameters.mediaName(), getValueFromResponse(response, ORIGIN_FILENAME.getPath()), "origin_filename"),
                () -> compareParameters(importParameters.mediaPriority() + 1, getValueFromResponse(response, PRIORITY.getPath()), "priority")
        );

        String newMediaName = RandomStringUtils.randomAlphabetic(10) + DOT_VALUE + JPG.getFormatName();
        importParameters.mediaName(newMediaName);
        retailerMediaImportStepDef.sendMediaFile(retailerMediaImportStepDef.buildImportOneMediaRequest(importParameters));

        metadataStepDef.successMetadataSearch(externalOfferId, 2);
    }

    @Test(description = "Успешный импорт дубликата медиафайла (разные ссылки - одинаковые masterSellerId и externalOfferId)",
            timeOut = 600000, groups = {"regress", "kafka"})
    @WorkItemIds({"237139"})
    public void successDuplicateS3ImportTest() {
        RetailerMediaImportSettings.ImportParameters importParameters = getData().importParameters().clone().
                mediaName(mediaName).masterSellerId(masterSellerId).externalOfferId(externalOfferId.toString());
        retailerMediaImportStepDef.sendMediaFile(retailerMediaImportStepDef.buildImportOneMediaRequest(importParameters));

        List<DataItem> dataItems = metadataStepDef.successMetadataSearch(externalOfferId, false);

        Response response = metadataStepDef.checkMetadata(dataItems.get(0).getId());
        assertAll(
                () -> compareParameters(importParameters.mediaName(), getValueFromResponse(response, ORIGIN_FILENAME.getPath()), "origin_filename"),
                () -> compareParameters(importParameters.mediaPriority() + 1, getValueFromResponse(response, PRIORITY.getPath()), "priority")
        );

        String newMediaName = RandomStringUtils.randomAlphabetic(10) + DOT_VALUE + JPG.getFormatName();
        importParameters.mediaName(newMediaName).mediaUrl(getData().anotherMediaUrl());
        retailerMediaImportStepDef.sendMediaFile(retailerMediaImportStepDef.buildImportOneMediaRequest(importParameters));

        metadataStepDef.successMetadataSearch(externalOfferId, 4);
    }

    @Test(description = "Успешный импорт одного медиафайла с ссылкой из s3", timeOut = 600000, groups = {"regress", "kafka"})
    @WorkItemIds({"235961"})
    public void successSingeS3ImportTest() {
        RetailerMediaImportSettings.ImportParameters importParameters = getData().importParameters().clone().
                mediaName(mediaName).mediaUrl(getData().s3MediaUrl()).masterSellerId(masterSellerId).externalOfferId(externalOfferId.toString());
        retailerMediaImportStepDef.sendMediaFile(retailerMediaImportStepDef.buildImportOneMediaRequest(importParameters));

        List<DataItem> dataItems = metadataStepDef.successMetadataSearch(externalOfferId, false);

        Response response = metadataStepDef.checkMetadata(dataItems.get(0).getId());

        assertAll(
                () -> compareParameters(importParameters.externalOfferId(), getValueFromResponse(response, EXTERNAL_OFFER.getPath() + ".id").toString(), "external_offer.id"),
                () -> compareParameters(importParameters.externalOfferName(), getValueFromResponse(response, EXTERNAL_OFFER.getPath() + ".name"), "external_offer.name"),
                () -> compareParameters(importParameters.mediaName(), getValueFromResponse(response, ORIGIN_FILENAME.getPath()), "origin_filename"),
                () -> compareParameters(String.format(RMIS_FILENAME_MASK, masterSellerId, externalOfferId, 1, JPG.getFormatName()), getValueFromResponse(response, FILENAME.getPath()), "filename"),
                () -> compareParameters(importParameters.mediaPriority() + 1, getValueFromResponse(response, PRIORITY.getPath()), "priority")
        );
    }

    @Test(description = "Успешный импорт нескольких медиафайлов", timeOut = 600000, groups = {"regress", "kafka"})
    @WorkItemIds({"230155"})
    public void successMultiImportTest() {
        List<String> namesList = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            String name = RandomStringUtils.randomAlphabetic(10) + DOT_VALUE + JPG.getFormatName();
            namesList.add(name);
        }

        RetailerMediaImportSettings.ImportParameters importParameters = getData().importParameters().clone().
                masterSellerId(masterSellerId).externalOfferId(externalOfferId.toString());
        retailerMediaImportStepDef.sendMediaFile(retailerMediaImportStepDef.buildImportSomeMediaWithParamsRequest(namesList, importParameters));
        metadataStepDef.successMetadataSearch(externalOfferId, 6);
    }

    @Test(description = "Успешный импорт нескольких медиафайлов c разными типами ссылок (s3)", timeOut = 600000, groups = {"regress", "kafka"})
    @WorkItemIds({"235962"})
    public void successMultiS3ImportTest() {
        List<String> namesList = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            String name = RandomStringUtils.randomAlphabetic(10) + DOT_VALUE + JPG.getFormatName();
            namesList.add(name);
        }

        RetailerMediaImportSettings.ImportParameters importParameters = getData().importParameters().clone().
                mediaUrl(getData().s3MediaUrl()).masterSellerId(masterSellerId).externalOfferId(externalOfferId.toString());
        retailerMediaImportStepDef.sendMediaFile(retailerMediaImportStepDef.buildImportSomeMediaWithS3Url(namesList, importParameters));
        metadataStepDef.successMetadataSearch(externalOfferId, 6);
    }

    @Test(description = "Успешный импорт медиафайла - невалидное название (невалидный формат)", timeOut = 600000, groups = {"regress", "kafka"})
    @WorkItemIds({"230159"})
    public void successInvalidFormatMediaImportTest() {
        String mediaName = RandomStringUtils.randomAlphabetic(10) + DOT_VALUE + INVALID.getFormatName();
        RetailerMediaImportSettings.ImportParameters importParameters = getData().importParameters().clone().
                masterSellerId(masterSellerId).externalOfferId(externalOfferId.toString()).mediaName(mediaName);
        retailerMediaImportStepDef.sendMediaFile(retailerMediaImportStepDef.buildImportOneMediaRequest(importParameters));

        List<DataItem> dataItems = metadataStepDef.successMetadataSearch(externalOfferId, false);

        Response response = metadataStepDef.checkMetadata(dataItems.get(0).getId());

        assertAll(
                () -> compareParameters(importParameters.externalOfferId(), getValueFromResponse(response, EXTERNAL_OFFER.getPath() + ".id").toString(), "external_offer.id"),
                () -> compareParameters(importParameters.externalOfferName(), getValueFromResponse(response, EXTERNAL_OFFER.getPath() + ".name"), "external_offer.name"),
                () -> compareParameters(importParameters.mediaName(), getValueFromResponse(response, ORIGIN_FILENAME.getPath()), "origin_filename"),
                () -> compareParameters(String.format(RMIS_FILENAME_MASK, masterSellerId, externalOfferId, 1, JPEG.getFormatName()), getValueFromResponse(response, FILENAME.getPath()), "filename"),
                () -> compareParameters(importParameters.mediaPriority() + 1, getValueFromResponse(response, PRIORITY.getPath()), "priority")
        );
    }

    @Test(description = "Успешный импорт медиафайла - невалидное название (нет формата)", timeOut = 600000, groups = {"regress", "kafka"})
    @WorkItemIds({"230159"})
    public void successNoFormatMediaImportTest() {
        RetailerMediaImportSettings.ImportParameters importParameters = getData().importParameters().clone().
                masterSellerId(masterSellerId).externalOfferId(externalOfferId.toString()).mediaName(RandomStringUtils.randomAlphabetic(10));
        retailerMediaImportStepDef.sendMediaFile(retailerMediaImportStepDef.buildImportOneMediaRequest(importParameters));

        List<DataItem> dataItems = metadataStepDef.successMetadataSearch(externalOfferId, false);

        Response response = metadataStepDef.checkMetadata(dataItems.get(0).getId());

        assertAll(
                () -> compareParameters(importParameters.externalOfferId(), getValueFromResponse(response, EXTERNAL_OFFER.getPath() + ".id").toString(), "external_offer.id"),
                () -> compareParameters(importParameters.externalOfferName(), getValueFromResponse(response, EXTERNAL_OFFER.getPath() + ".name"), "external_offer.name"),
                () -> compareParameters(importParameters.mediaName(), getValueFromResponse(response, ORIGIN_FILENAME.getPath()), "origin_filename"),
                () -> compareParameters(String.format(RMIS_FILENAME_MASK, masterSellerId, externalOfferId, 1, JPG.getFormatName()), getValueFromResponse(response, FILENAME.getPath()), "filename"),
                () -> compareParameters(importParameters.mediaPriority() + 1, getValueFromResponse(response, PRIORITY.getPath()), "priority")
        );
    }

    @Test(description = "Неуспешный импорт медиафайла - нет файлов", timeOut = 600000, groups = {"regress", "kafka"})
    @WorkItemIds({"230156"})
    public void unsuccessfulNoMediaImportTest() {
        metadataStepDef.deleteMetadata();
        retailerMediaImportStepDef.sendMediaFile(retailerMediaImportStepDef.buildImportNoMediaRequest());

        metadataStepDef.successMetadataSearch(externalOfferId, true);
    }

    @Test(description = "Неуспешный импорт медиафайла - невалидный файл", timeOut = 600000, groups = {"regress", "kafka"})
    @WorkItemIds({"230157"})
    public void unsuccessfulInvalidMediaImportTest() {
        String mediaName = RandomStringUtils.randomAlphabetic(10) + DOT_VALUE + JPG.getFormatName();
        RetailerMediaImportSettings.ImportParameters importParameters = getData().importParameters().clone().
                mediaName(mediaName).
                masterSellerId(masterSellerId).externalOfferId(externalOfferId.toString()).
                mediaUrl(getData().pdfMediaUrl());
        retailerMediaImportStepDef.sendMediaFile(retailerMediaImportStepDef.buildImportOneMediaRequest(importParameters));

        metadataStepDef.successMetadataSearch(externalOfferId, true);
    }

    @Test(description = "Неуспешный импорт медиафайла - недоступная ссылка", timeOut = 600000, groups = {"regress", "kafka"})
    @WorkItemIds({"230158"})
    public void unsuccessfulInvalidUrlImportTest() {
        String mediaName = RandomStringUtils.randomAlphabetic(10) + DOT_VALUE + JPG.getFormatName();
        RetailerMediaImportSettings.ImportParameters importParameters = getData().importParameters().clone().
                mediaName(mediaName).
                masterSellerId(masterSellerId).externalOfferId(externalOfferId.toString()).
                mediaUrl(new ImageData().getUrl());
        retailerMediaImportStepDef.sendMediaFile(retailerMediaImportStepDef.buildImportOneMediaRequest(importParameters));

        metadataStepDef.successMetadataSearch(externalOfferId, true);
    }

    @Test(description = "Неуспешный импорт медиафайла - недоступная ссылка из s3", timeOut = 600000, groups = {"regress", "kafka"})
    @WorkItemIds({"235963"})
    public void unsuccessfulInvalidUrlS3ImportTest() {
        String mediaName = RandomStringUtils.randomAlphabetic(10) + DOT_VALUE + JPG.getFormatName();
        String s3MediaUrl = getData().s3MediaUrl();
        RetailerMediaImportSettings.ImportParameters importParameters = getData().importParameters().clone().
                mediaName(mediaName).
                masterSellerId(masterSellerId).externalOfferId(externalOfferId.toString()).
                mediaUrl(s3MediaUrl.substring(0, s3MediaUrl.length() - 10) + RandomStringUtils.randomAlphabetic(10) + DOT_VALUE + JPG.getFormatName());
        retailerMediaImportStepDef.sendMediaFile(retailerMediaImportStepDef.buildImportOneMediaRequest(importParameters));

        metadataStepDef.successMetadataSearch(externalOfferId, true);
    }

    @Deprecated
    //@Test(description = "Неуспешный импорт медиафайла - невалидное название (длинна названия)", timeOut = 600000, groups = {"regress", "kafka"})
    @WorkItemIds({"230159"})
    public void unsuccessfulInvalidLongNameMediaImportTest() {
        String mediaName = RandomStringUtils.randomAlphabetic(250) + DOT_VALUE + JPG.getFormatName();
        RetailerMediaImportSettings.ImportParameters importParameters = getData().importParameters().clone().
                masterSellerId(masterSellerId).externalOfferId(externalOfferId.toString()).mediaName(mediaName);
        retailerMediaImportStepDef.sendMediaFile(retailerMediaImportStepDef.buildImportOneMediaRequest(importParameters));

        metadataStepDef.successFiltrationMetadata(Collections.singletonMap(SEARCH.getName(), externalOfferId), true).
                extract().as(FiltrationResponse.class);
    }

    //todo включить после доработки валидации на RMIS
    //@Test(description = "Неуспешный импорт медиафайла - невалидный name (external offer)", timeOut = 600000, groups = {"regress", "kafka"})
    //@WorkItemIds({"230160"})
    public void unsuccessfulInvalidExternalOfferNameMediaImportTest() {
        String mediaName = RandomStringUtils.randomAlphabetic(10) + DOT_VALUE + JPG.getFormatName();
        RetailerMediaImportSettings.ImportParameters importParameters = getData().importParameters().clone().
                mediaName(mediaName).
                externalOfferName(null);
        retailerMediaImportStepDef.sendMediaFile(retailerMediaImportStepDef.buildImportOneMediaRequest(importParameters));

        metadataStepDef.successMetadataSearch(externalOfferId, true);
    }

    //todo включить после доработки валидации на RMIS
    //@Test(description = "Неуспешный импорт медиафайла - невалидный externalOfferId", timeOut = 600000, groups = {"regress", "kafka"})
    //@WorkItemIds({"230161"})
    public void unsuccessfulInvalidExternalOfferIdMediaImportTest() {
        String mediaName = RandomStringUtils.randomAlphabetic(10) + DOT_VALUE + JPG.getFormatName();
        RetailerMediaImportSettings.ImportParameters importParameters = getData().importParameters().clone().
                mediaName(mediaName).
                externalOfferId(null);
        retailerMediaImportStepDef.sendMediaFile(retailerMediaImportStepDef.buildImportOneMediaRequest(importParameters));

        metadataStepDef.successMetadataSearch(externalOfferId, true);
    }

    //todo включить после доработки валидации на RMIS
    //@Test(description = "Неуспешный импорт медиафайла - невалидный masterSellerId", timeOut = 600000, groups = {"regress", "kafka"})
    //@WorkItemIds({"230162"})
    public void unsuccessfulInvalidMasterSellerIdMediaImportTest() {
        String mediaName = RandomStringUtils.randomAlphabetic(10) + DOT_VALUE + JPG.getFormatName();
        RetailerMediaImportSettings.ImportParameters importParameters = getData().importParameters().clone().
                mediaName(mediaName).
                masterSellerId(null);
        retailerMediaImportStepDef.sendMediaFile(retailerMediaImportStepDef.buildImportOneMediaRequest(importParameters));

        metadataStepDef.successMetadataSearch(externalOfferId, true);
    }

    //todo включить после доработки валидации на RMIS
    //@Test(description = "Неуспешный импорт медиафайла - невалидный priority", timeOut = 600000, groups = {"regress", "kafka"})
    //@WorkItemIds({"230163"})
    public void unsuccessfulInvalidPriorityMediaImportTest() {
        String mediaName = RandomStringUtils.randomAlphabetic(10) + DOT_VALUE + JPG.getFormatName();
        RetailerMediaImportSettings.ImportParameters importParameters = getData().importParameters().clone().
                mediaName(mediaName).
                mediaPriority(10000000);
        retailerMediaImportStepDef.sendMediaFile(retailerMediaImportStepDef.buildImportOneMediaRequest(importParameters));

        metadataStepDef.successMetadataSearch(externalOfferId, true);
    }
}