package ru.spice.at.api.import_service.test;

import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import ru.testit.annotations.WorkItemIds;
import io.restassured.response.Response;
import org.apache.commons.lang3.RandomStringUtils;
import org.testng.annotations.*;
import ru.spice.at.api.dto.request.import_service.ExternalImportData;
import ru.spice.at.api.dto.request.import_service.RequestExternalImport;
import ru.spice.at.api.dto.response.import_service.InvalidParamsItem;
import ru.spice.at.api.dto.response.metadata.filtration.DataItem;
import ru.spice.at.api.import_service.ImportServiceSettings;
import ru.spice.at.api.import_service.ImportServiceStepDef;
import ru.spice.at.api.metadata_service.MetadataStepDef;
import ru.spice.at.common.base_test.BaseApiTest;
import ru.spice.at.common.dto.dam.ImageData;
import ru.spice.at.common.emuns.ApiServices;
import ru.spice.at.common.emuns.dam.ImageFormat;
import ru.spice.at.common.utils.Assert;
import ru.spice.at.common.utils.CipherHelper;

import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static java.time.format.DateTimeFormatter.ISO_INSTANT;
import static ru.spice.at.common.constants.TestConstants.*;
import static ru.spice.at.common.emuns.dam.ImageParameters.*;
import static ru.spice.at.common.utils.Assert.*;
import static ru.spice.at.common.utils.ImageHelper.getRandomByteImage;
import static ru.spice.at.common.utils.JsonHelper.getValueFromResponse;

@Feature("Import Service")
@Story("POST external import")
public class ImportServicePostExternalImportMediaTests extends BaseApiTest<ImportServiceSettings> {
    private ImportServiceStepDef importServiceStepDef;
    private MetadataStepDef metadataStepDef;
    private RequestExternalImport requestExternalImport;
    private String externalToken;

    protected ImportServicePostExternalImportMediaTests() {
        super(ApiServices.IMPORT_SERVICE);
    }

    @BeforeClass(alwaysRun = true)
    public void beforeClass() {
        importServiceStepDef = new ImportServiceStepDef();
        metadataStepDef = new MetadataStepDef(importServiceStepDef.getAuthToken());
        externalToken = CipherHelper.decrypt(getData().externalToken());
    }

    @BeforeMethod(alwaysRun = true)
    public void beforeMethod() {
        ImageData image = new ImageData(ImageFormat.JPEG);
        byte[] bytes = getRandomByteImage(NINETY_NINE, NINETY_NINE, image.getFormat().getFormatName());
        String fileBase64 = new String(Base64.getEncoder().encode(bytes));
        requestExternalImport = importServiceStepDef.buildEditRequest(
                fileBase64, image.getFilename(), OffsetDateTime.now().minusDays(1).format(ISO_INSTANT));
    }

    //todo выключено для использования параллельного сьюта
    //@AfterClass(alwaysRun = true)
    public void afterClass() {
        importServiceStepDef.deleteMedia();
        metadataStepDef.deleteMetadata();
    }

    @Test(description = "Успешный внешний импорт файла", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"237038"})
    public void successExternalImportTest() {
        importServiceStepDef.externalImportImage(requestExternalImport, getData().baseExternalUrl(), externalToken);

        ExternalImportData externalImportData = requestExternalImport.externalImportData();
        List<DataItem> dataItems = metadataStepDef.successMetadataSearch(externalImportData.filename().split("\\.", 2)[0]);
        List<String> actualIdList = dataItems.stream().map(DataItem::getId).collect(Collectors.toList());

        actualIdList.forEach(id -> {
            Response response = metadataStepDef.checkMetadata(id);
            assertAll(
                    () -> compareParameters(externalImportData.externalOfferId(), getValueFromResponse(response, EXTERNAL_OFFER.getPath() + ".id"), "external_offer.id"),
                    () -> compareParameters(externalImportData.externalOfferName(), getValueFromResponse(response, EXTERNAL_OFFER.getPath() + ".name"), "external_offer.name"),
                    () -> contains(externalImportData.filename().split("\\.", 2)[0], getValueFromResponse(response, FILENAME.getPath()), "filename"),
                    () -> compareParameters(externalImportData.filename(), getValueFromResponse(response, ORIGIN_FILENAME.getPath()), "origin_filename"),
                    () -> compareParameters(externalImportData.priority(), getValueFromResponse(response, PRIORITY.getPath()), "priority"),
                    () -> compareParameters(externalImportData.sku(), getValueFromResponse(response, SKU.getPath()), "sku"),
                    () -> compareParameters(externalImportData.ownTrademark(), getValueFromResponse(response, IS_OWN_TRADEMARK.getPath()), "is_own_trademark"),
                    () -> compareParameters(externalImportData.externalTaskId(), getValueFromResponse(response, EXTERNAL_TASK_ID.getPath()), "external_task_id"),
                    () -> compareParameters(externalImportData.masterSellerId(), getValueFromResponse(response, MASTER_SELLER.getPath() + ".ext_id"), "master_seller"),
                    () -> compareParameters(
                            externalImportData.readyForRetouchAt().substring(0, 19),
                            getValueFromResponse(response, READY_FOR_RETOUCH_AT.getPath()).toString().substring(0, 19),
                            "ready_for_retouch_at")
            );
        });
    }

    @Test(description = "Успешный внешний импорт файла (длинное название и высокий приоритет)", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"237038"})
    public void successExternalImportLongNameHighPriorityTest() {
        String filename = RandomStringUtils.randomAlphabetic(MAX_JPG_FILENAME_SYMBOLS) + DOT_VALUE + ImageFormat.JPEG.getFormatName();
        requestExternalImport.externalImportData().priority(500).filename(filename);
        importServiceStepDef.externalImportImage(requestExternalImport, getData().baseExternalUrl(), externalToken);

        ExternalImportData externalImportData = requestExternalImport.externalImportData();
        List<DataItem> dataItems = metadataStepDef.successMetadataSearch(filename);
        List<String> actualIdList = dataItems.stream().map(DataItem::getId).collect(Collectors.toList());

        actualIdList.forEach(id -> {
            Response response = metadataStepDef.checkMetadata(id);
            assertAll(
                    () -> compareParameters(externalImportData.externalOfferId(), getValueFromResponse(response, EXTERNAL_OFFER.getPath() + ".id"), "external_offer.id"),
                    () -> compareParameters(externalImportData.externalOfferName(), getValueFromResponse(response, EXTERNAL_OFFER.getPath() + ".name"), "external_offer.name"),
                    () -> contains(externalImportData.filename().split("\\.", 2)[0], getValueFromResponse(response, FILENAME.getPath()), "filename"),
                    () -> compareParameters(externalImportData.filename(), getValueFromResponse(response, ORIGIN_FILENAME.getPath()), "origin_filename"),
                    () -> compareParameters(externalImportData.priority(), getValueFromResponse(response, PRIORITY.getPath()), "priority"),
                    () -> compareParameters(externalImportData.sku(), getValueFromResponse(response, SKU.getPath()), "sku"),
                    () -> compareParameters(externalImportData.ownTrademark(), getValueFromResponse(response, IS_OWN_TRADEMARK.getPath()), "is_own_trademark"),
                    () -> compareParameters(externalImportData.externalTaskId(), getValueFromResponse(response, EXTERNAL_TASK_ID.getPath()), "external_task_id"),
                    () -> compareParameters(externalImportData.masterSellerId(), getValueFromResponse(response, MASTER_SELLER.getPath() + ".ext_id"), "master_seller"),
                    () -> compareParameters(
                            externalImportData.readyForRetouchAt().substring(0, 19),
                            getValueFromResponse(response, READY_FOR_RETOUCH_AT.getPath()).toString().substring(0, 19),
                            "ready_for_retouch_at")
            );
        });
    }

    @Test(description = "Успешный внешний импорт файла priority = 1", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"237227"})
    public void successExternalImportPriorityOneTest() {
        requestExternalImport.externalImportData().priority(1);
        importServiceStepDef.externalImportImage(requestExternalImport, getData().baseExternalUrl(), externalToken);

        ExternalImportData externalImportData = requestExternalImport.externalImportData();
        List<DataItem> dataItems = metadataStepDef.successMetadataSearch(externalImportData.filename().split("\\.", 2)[0]);
        List<String> actualIdList = dataItems.stream().map(DataItem::getId).collect(Collectors.toList());

        actualIdList.forEach(id -> {
            Response response = metadataStepDef.checkMetadata(id);
            assertAll(
                    () -> compareParameters(externalImportData.externalOfferId(), getValueFromResponse(response, EXTERNAL_OFFER.getPath() + ".id"), "external_offer.id"),
                    () -> compareParameters(externalImportData.externalOfferName(), getValueFromResponse(response, EXTERNAL_OFFER.getPath() + ".name"), "external_offer.name"),
                    () -> contains(externalImportData.filename().split("\\.", 2)[0], getValueFromResponse(response, FILENAME.getPath()), "filename"),
                    () -> compareParameters(externalImportData.filename(), getValueFromResponse(response, ORIGIN_FILENAME.getPath()), "origin_filename"),
                    () -> compareParameters(externalImportData.priority(), getValueFromResponse(response, PRIORITY.getPath()), "priority"),
                    () -> compareParameters(externalImportData.sku(), getValueFromResponse(response, SKU.getPath()), "sku"),
                    () -> compareParameters(externalImportData.ownTrademark(), getValueFromResponse(response, IS_OWN_TRADEMARK.getPath()), "is_own_trademark"),
                    () -> compareParameters(externalImportData.externalTaskId(), getValueFromResponse(response, EXTERNAL_TASK_ID.getPath()), "external_task_id"),
                    () -> compareParameters(externalImportData.masterSellerId(), getValueFromResponse(response, MASTER_SELLER.getPath() + ".ext_id"), "master_seller"),
                    () -> compareParameters(
                            externalImportData.readyForRetouchAt().substring(0, 19),
                            getValueFromResponse(response, READY_FOR_RETOUCH_AT.getPath()).toString().substring(0, 19),
                            "ready_for_retouch_at")
            );
        });
    }

    @Test(description = "Неуспешный внешний импорт файла - priority > 500", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"237237"})
    public void unsuccessfulExternalImportPriorityMore100Test() {
        requestExternalImport.externalImportData().priority(501 + new Random().nextInt(100));
        List<InvalidParamsItem> invalidParamsItems = importServiceStepDef.unsuccessfulExternalImportImage(
                requestExternalImport, getData().baseExternalUrl(), externalToken);

        List<InvalidParamsItem> invalidParamsItemsExp = Collections.singletonList(
                new InvalidParamsItem().
                        name("priority").
                        type(getData().errorTypes().get(5).type()).
                        reason(getData().errorTypes().get(5).description())
        );

        Assert.compareParameters(invalidParamsItemsExp, invalidParamsItems, "сообщение об ошибке");
    }

    @Test(description = "Неуспешный внешний импорт файла - битый файл", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"237231"})
    public void unsuccessfulExternalImportBrokenImageTest() {
        ImageData image = new ImageData(ImageFormat.JPEG);
        byte[] bytes = getRandomByteImage(NINETY_NINE, NINETY_NINE, image.getFormat().getFormatName());
        int brokenCount = 30;
        Assert.equalsTrueParameter(bytes.length > brokenCount, "количество байтов");
        for (int i = 0; i < brokenCount; i++) {
            bytes[i] = 0;
        }

        String fileBase64 = new String(Base64.getEncoder().encode(bytes));

        requestExternalImport.externalImportData().file(fileBase64);
        List<InvalidParamsItem> invalidParamsItems = importServiceStepDef.unsuccessfulExternalImportImage(
                requestExternalImport, getData().baseExternalUrl(), externalToken);

        List<InvalidParamsItem> invalidParamsItemsExp = Arrays.asList(
                new InvalidParamsItem().
                        name(requestExternalImport.externalImportData().filename()).
                        type(getData().errorTypes().get(1).type()).
                        reason(getData().errorTypes().get(1).description()),
                new InvalidParamsItem().
                        name(requestExternalImport.externalImportData().filename()).
                        type(getData().errorTypes().get(7).type()).
                        reason(getData().errorTypes().get(7).description())
        );

        Assert.compareParameters(new LinkedList<>(invalidParamsItemsExp), new LinkedList<>(invalidParamsItems), "сообщение об ошибке");
    }

    @Test(description = "Неуспешный внешний импорт файла - пустой файл", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"237233"})
    public void unsuccessfulExternalImportEmptyImageTest() {
        requestExternalImport.externalImportData().file(EMPTY_VALUE);
        List<InvalidParamsItem> invalidParamsItems = importServiceStepDef.unsuccessfulExternalImportImage(requestExternalImport, getData().baseExternalUrl(), externalToken);

        List<InvalidParamsItem> invalidParamsItemsExp = Arrays.asList(
                new InvalidParamsItem().
                        name(requestExternalImport.externalImportData().filename()).
                        type(getData().errorTypes().get(1).type()).
                        reason(getData().errorTypes().get(1).description()),
                new InvalidParamsItem().
                        name(requestExternalImport.externalImportData().filename()).
                        type(getData().errorTypes().get(7).type()).
                        reason(getData().errorTypes().get(7).description())
        );

        Assert.compareParameters(new LinkedList<>(invalidParamsItemsExp), new LinkedList<>(invalidParamsItems), "сообщение об ошибке");
    }

    @Test(description = "Неуспешный внешний импорт файла - невалидная строка файла", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"239763"})
    public void unsuccessfulExternalImportBrokenStringImageTest() {
        requestExternalImport.externalImportData().file(RandomStringUtils.randomAlphabetic(100));
        List<InvalidParamsItem> invalidParamsItems = importServiceStepDef.unsuccessfulExternalImportImage(requestExternalImport, getData().baseExternalUrl(), externalToken);

        List<InvalidParamsItem> invalidParamsItemsExp = Arrays.asList(
                new InvalidParamsItem().
                        name(requestExternalImport.externalImportData().filename()).
                        type(getData().errorTypes().get(1).type()).
                        reason(getData().errorTypes().get(1).description()),
                new InvalidParamsItem().
                        name(requestExternalImport.externalImportData().filename()).
                        type(getData().errorTypes().get(7).type()).
                        reason(getData().errorTypes().get(7).description())
        );

        Assert.compareParameters(new LinkedList<>(invalidParamsItemsExp), new LinkedList<>(invalidParamsItems), "сообщение об ошибке");
    }

    @Test(description = "Неуспешный внешний импорт файла - невалидное расширение в названии", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"237234"})
    public void unsuccessfulExternalImportInvalidFormatTest() {
        requestExternalImport.externalImportData().filename(RandomStringUtils.randomAlphabetic(6) + DOT_VALUE + ImageFormat.INVALID.getFormatName());
        List<InvalidParamsItem> invalidParamsItems = importServiceStepDef.unsuccessfulExternalImportImage(requestExternalImport, getData().baseExternalUrl(), externalToken);

        List<InvalidParamsItem> invalidParamsItemsExp = Collections.singletonList(
                new InvalidParamsItem().
                        name(requestExternalImport.externalImportData().filename()).
                        type(getData().errorTypes().get(4).type()).
                        reason(getData().errorTypes().get(4).description())
        );

        Assert.compareParameters(invalidParamsItemsExp, invalidParamsItems, "сообщение об ошибке");
    }

    @Test(description = "Неуспешный внешний импорт файла - длинное название", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"237235"})
    public void unsuccessfulExternalImportLongFilenameTest() {
        requestExternalImport.externalImportData().filename(RandomStringUtils.randomAlphabetic(MAX_JPG_FILENAME_SYMBOLS + 1) + DOT_VALUE + ImageFormat.JPEG.getFormatName());
        List<InvalidParamsItem> invalidParamsItems = importServiceStepDef.unsuccessfulExternalImportImage(requestExternalImport, getData().baseExternalUrl(), externalToken);

        List<InvalidParamsItem> invalidParamsItemsExp = Collections.singletonList(
                new InvalidParamsItem().
                        name("filename").
                        type(getData().errorTypes().get(3).type()).
                        reason(getData().errorTypes().get(3).description())
        );

        Assert.compareParameters(invalidParamsItemsExp, invalidParamsItems, "сообщение об ошибке");
    }

    @Test(description = "Неуспешный внешний импорт файла - пустое название", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"237236"})
    public void unsuccessfulExternalImportEmptyFilenameTest() {
        requestExternalImport.externalImportData().filename(EMPTY_VALUE);
        List<InvalidParamsItem> invalidParamsItems = importServiceStepDef.unsuccessfulExternalImportImage(requestExternalImport, getData().baseExternalUrl(), externalToken);

        List<InvalidParamsItem> invalidParamsItemsExp = Collections.singletonList(
                new InvalidParamsItem().
                        name(requestExternalImport.externalImportData().filename()).
                        type(getData().errorTypes().get(4).type()).
                        reason(getData().errorTypes().get(4).description())
        );

        Assert.compareParameters(invalidParamsItemsExp, invalidParamsItems, "сообщение об ошибке");
    }

    @Test(description = "Неуспешный внешний импорт файла external_task_id = null", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"237224"})
    public void unsuccessfulExternalImportExternalTaskIdNullTest() {
        requestExternalImport.externalImportData().externalTaskId(null);
        List<InvalidParamsItem> invalidParamsItems = importServiceStepDef.unsuccessfulExternalImportImage(
                requestExternalImport, getData().baseExternalUrl(), externalToken);

        List<InvalidParamsItem> invalidParamsItemsExp = Collections.singletonList(
                new InvalidParamsItem().
                        name(EXTERNAL_TASK_ID.getName()).
                        type(getData().errorTypes().get(0).type()).
                        reason(getData().errorTypes().get(0).description())
        );

        Assert.compareParameters(invalidParamsItemsExp, invalidParamsItems, "сообщение об ошибке");
    }

    @Test(description = "Неуспешный внешний импорт файла sku = null", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"237225"})
    public void unsuccessfulExternalImportSkuNullTest() {
        requestExternalImport.externalImportData().sku(null);
        List<InvalidParamsItem> invalidParamsItems = importServiceStepDef.unsuccessfulExternalImportImage(
                requestExternalImport, getData().baseExternalUrl(), externalToken);

        List<InvalidParamsItem> invalidParamsItemsExp = Collections.singletonList(
                new InvalidParamsItem().
                        name(SKU.getName()).
                        type(getData().errorTypes().get(0).type()).
                        reason(getData().errorTypes().get(0).description())
        );

        Assert.compareParameters(invalidParamsItemsExp, invalidParamsItems, "сообщение об ошибке");
    }

    @Test(description = "Неуспешный внешний импорт файла external_offer_name = null", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"237228"})
    public void unsuccessfulExternalImportExternalOfferNameNullTest() {
        requestExternalImport.externalImportData().externalOfferName(null);
        List<InvalidParamsItem> invalidParamsItems = importServiceStepDef.unsuccessfulExternalImportImage(requestExternalImport, getData().baseExternalUrl(), externalToken);

        List<InvalidParamsItem> invalidParamsItemsExp = Collections.singletonList(
                new InvalidParamsItem().
                        name(EXTERNAL_OFFER.getName() + "_name").
                        type(getData().errorTypes().get(0).type()).
                        reason(getData().errorTypes().get(0).description())
        );

        Assert.compareParameters(invalidParamsItemsExp, invalidParamsItems, "сообщение об ошибке");
    }

    @Test(description = "Неуспешный внешний импорт файла master_seller_id = null", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"237230"})
    public void unsuccessfulExternalImportMasterSellerIdNullTest() {
        requestExternalImport.externalImportData().masterSellerId(null);
        List<InvalidParamsItem> invalidParamsItems = importServiceStepDef.unsuccessfulExternalImportImage(
                requestExternalImport, getData().baseExternalUrl(), externalToken);

        List<InvalidParamsItem> invalidParamsItemsExp = Collections.singletonList(
                new InvalidParamsItem().
                        name(MASTER_SELLER_ID.getName()).
                        type(getData().errorTypes().get(0).type()).
                        reason(getData().errorTypes().get(0).description())
        );

        Assert.compareParameters(invalidParamsItemsExp, invalidParamsItems, "сообщение об ошибке");
    }

    @Test(description = "Неуспешный внешний импорт файла file = null", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"239782"})
    public void unsuccessfulExternalImportFileTest() {
        requestExternalImport.externalImportData().file(null);
        List<InvalidParamsItem> invalidParamsItems = importServiceStepDef.unsuccessfulExternalImportImage(
                requestExternalImport, getData().baseExternalUrl(), externalToken);

        List<InvalidParamsItem> invalidParamsItemsExp = Collections.singletonList(
                new InvalidParamsItem().
                        name("file").
                        type(getData().errorTypes().get(0).type()).
                        reason(getData().errorTypes().get(0).description())
        );

        Assert.compareParameters(invalidParamsItemsExp, invalidParamsItems, "сообщение об ошибке");
    }

    @Test(description = "Неуспешный внешний импорт файла filename = null", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"239783"})
    public void unsuccessfulExternalImportFilenameTest() {
        requestExternalImport.externalImportData().filename(null);
        List<InvalidParamsItem> invalidParamsItems = importServiceStepDef.unsuccessfulExternalImportImage(
                requestExternalImport, getData().baseExternalUrl(), externalToken);

        List<InvalidParamsItem> invalidParamsItemsExp = Collections.singletonList(
                new InvalidParamsItem().
                        name(FILENAME.getName()).
                        type(getData().errorTypes().get(0).type()).
                        reason(getData().errorTypes().get(0).description())
        );

        Assert.compareParameters(invalidParamsItemsExp, invalidParamsItems, "сообщение об ошибке");
    }

    @Test(description = "Неуспешный внешний импорт файла external_offer_id = null", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"239784"})
    public void unsuccessfulExternalImportExternalOfferIdFilenameTest() {
        requestExternalImport.externalImportData().externalOfferId(null);
        List<InvalidParamsItem> invalidParamsItems = importServiceStepDef.unsuccessfulExternalImportImage(
                requestExternalImport, getData().baseExternalUrl(), externalToken);

        List<InvalidParamsItem> invalidParamsItemsExp = Collections.singletonList(
                new InvalidParamsItem().
                        name(EXTERNAL_OFFER.getName() + "_id").
                        type(getData().errorTypes().get(0).type()).
                        reason(getData().errorTypes().get(0).description())
        );

        Assert.compareParameters(invalidParamsItemsExp, invalidParamsItems, "сообщение об ошибке");
    }

    @Test(description = "Неуспешный внешний импорт файла is_own_trademark = null", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"239785"})
    public void unsuccessfulExternalImportIsOwnTrademarkTest() {
        requestExternalImport.externalImportData().ownTrademark(null);
        List<InvalidParamsItem> invalidParamsItems = importServiceStepDef.unsuccessfulExternalImportImage(
                requestExternalImport, getData().baseExternalUrl(), externalToken);

        List<InvalidParamsItem> invalidParamsItemsExp = Collections.singletonList(
                new InvalidParamsItem().
                        name(OWN_TRADEMARK.getName()).
                        type(getData().errorTypes().get(0).type()).
                        reason(getData().errorTypes().get(0).description())
        );

        Assert.compareParameters(invalidParamsItemsExp, invalidParamsItems, "сообщение об ошибке");
    }
}
