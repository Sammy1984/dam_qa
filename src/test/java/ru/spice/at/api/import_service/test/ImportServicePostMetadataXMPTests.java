package ru.spice.at.api.import_service.test;

import com.adobe.internal.xmp.XMPMeta;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import ru.testit.annotations.WorkItemIds;
import io.restassured.response.Response;
import org.apache.commons.lang3.RandomStringUtils;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import ru.spice.at.api.dto.request.import_service.RequestXmpMetadata;
import ru.spice.at.api.dto.response.import_service.InvalidParamsItem;
import ru.spice.at.api.dto.response.metadata.DictionariesItem;
import ru.spice.at.api.dto.response.metadata.UsersItem;
import ru.spice.at.api.import_service.ImportServiceSettings;
import ru.spice.at.api.import_service.ImportServiceStepDef;
import ru.spice.at.api.metadata_service.MetadataStepDef;
import ru.spice.at.common.base_test.BaseApiTest;
import ru.spice.at.common.dto.dam.ImageData;
import ru.spice.at.common.emuns.ApiServices;
import ru.spice.at.common.emuns.dam.ImageFormat;
import ru.spice.at.common.emuns.dam.Quality;
import ru.spice.at.common.emuns.dam.Source;
import ru.spice.at.common.emuns.dam.Status;

import java.util.*;

import static com.adobe.internal.xmp.XMPConst.NS_JPEG;
import static ru.spice.at.common.constants.TestConstants.CAPITAL_FALSE;
import static ru.spice.at.common.constants.TestConstants.CAPITAL_TRUE;
import static ru.spice.at.common.emuns.dam.ImageParameters.*;
import static ru.spice.at.common.utils.Assert.*;
import static ru.spice.at.common.utils.ImageHelper.getRandomByteImage;
import static ru.spice.at.common.utils.JsonHelper.getValueFromResponse;

@Deprecated
@Feature("Import Service")
@Story("POST import media with metadata XMP")
public class ImportServicePostMetadataXMPTests extends BaseApiTest<ImportServiceSettings> {
    private ImportServiceStepDef importServiceStepDef;
    private MetadataStepDef metadataStepDef;
    private byte[] bytes;
    private ImageData image;

    protected ImportServicePostMetadataXMPTests() {
        super(ApiServices.IMPORT_SERVICE);
    }

    @BeforeClass(alwaysRun = true)
    public void beforeClass() {
        createFileDirection();
        importServiceStepDef = new ImportServiceStepDef();
        metadataStepDef = new MetadataStepDef(importServiceStepDef.getAuthToken());
    }

    @BeforeMethod(alwaysRun = true)
    public void beforeMethod() {
        image = new ImageData(ImageFormat.JPEG);
        bytes = getRandomByteImage(image.getWidth(), image.getHeight(), image.getFormat().getFormatName());
    }

    @AfterClass(alwaysRun = true)
    public void afterClassDelete() {
        deleteFileDirection();
    }

    //todo выключено для использования параллельного сьюта
    //@AfterClass(alwaysRun = true)
    public void afterClass() {
        importServiceStepDef.deleteMedia();
        metadataStepDef.deleteMetadata();
    }

    //@Test(description = "Успешный импорт медиафайла c XMP с category_id", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"243218"})
    public void successImportXmpImageWithCategoryIdTest() {
        DictionariesItem category = metadataStepDef.getListCategoriesMetadata().get(0);

        RequestXmpMetadata xmpMetadata = getData().xmpMetadata().clone();
        xmpMetadata.masterCategoryId(category.id());
        xmpMetadata.masterCategoryName(category.name());

        bytes = importServiceStepDef.writeXMPMeta(bytes, image, importServiceStepDef.buildRequestXmpMetadata(xmpMetadata), downloadPath);

        List<String> idList = importServiceStepDef.importImages(Collections.singletonMap(image.getFilename(), bytes));
        Response response = metadataStepDef.checkMetadata(idList.get(0));
        assertAll(
                () -> compareParameters(category.id(), getValueFromResponse(response, MASTER_CATEGORY_ID.getPath() + ".id"), "category_id"),
                () -> compareParameters(category.name(), getValueFromResponse(response, MASTER_CATEGORY_ID.getPath() + ".name"), "category_name")
        );
    }

    //@Test(description = "Успешный импорт медиафайла c XMP с category_id - неверное название категории", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"243218"})
    public void successImportXmpImageWithCategoryIdInvalidNameTest() {
        DictionariesItem category = metadataStepDef.getListCategoriesMetadata().get(0);

        RequestXmpMetadata xmpMetadata = getData().xmpMetadata().clone();
        xmpMetadata.masterCategoryId(category.id());
        xmpMetadata.masterCategoryName(RandomStringUtils.randomAlphabetic(4));

        bytes = importServiceStepDef.writeXMPMeta(bytes, image, importServiceStepDef.buildRequestXmpMetadata(xmpMetadata), downloadPath);

        List<String> idList = importServiceStepDef.importImages(Collections.singletonMap(image.getFilename(), bytes));
        Response response = metadataStepDef.checkMetadata(idList.get(0));
        assertAll(
                () -> compareParameters(category.id(), getValueFromResponse(response, MASTER_CATEGORY_ID.getPath() + ".id"), "category_id"),
                () -> compareParameters(category.name(), getValueFromResponse(response, MASTER_CATEGORY_ID.getPath() + ".name"), "category_name")
        );
    }

    //@Test(description = "Неуспешный импорт медиафайла c XMP с category_id - Invalid Type", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"243239"})
    public void unsuccessfulImportXmpImageWithCategoryIdInvalidTypeTest() {
        RequestXmpMetadata xmpMetadata = getData().xmpMetadata().clone();
        xmpMetadata.masterCategoryId(RandomStringUtils.randomAlphabetic(4));
        xmpMetadata.masterCategoryName(RandomStringUtils.randomAlphabetic(4));

        bytes = importServiceStepDef.writeXMPMeta(bytes, image, importServiceStepDef.buildRequestXmpMetadata(xmpMetadata), downloadPath);

        List<InvalidParamsItem> invalidParamsItems = importServiceStepDef.importInvalidRandomImages(image.getFilename(), bytes);
        assertAll(
                () -> compareParameters(1, invalidParamsItems.size(), "size"),
                () -> compareParameters(MASTER_CATEGORY_ID.getName(), invalidParamsItems.get(0).name(), "name"),
                () -> compareParameters(getData().errorInvalidType(), invalidParamsItems.get(0).type(), "type"),
                () -> compareParameters(getData().errorInvalidTypeReason(), invalidParamsItems.get(0).reason(), "reason")
        );
    }

    //@Test(description = "Неуспешный импорт медиафайла c XMP с category_id - Not Found", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"243239"})
    public void unsuccessfulImportXmpImageWithCategoryIdNotFoundTest() {
        RequestXmpMetadata xmpMetadata = getData().xmpMetadata().clone();
        xmpMetadata.masterCategoryId(UUID.randomUUID().toString());
        xmpMetadata.masterCategoryName(RandomStringUtils.randomAlphabetic(4));

        bytes = importServiceStepDef.writeXMPMeta(bytes, image, importServiceStepDef.buildRequestXmpMetadata(xmpMetadata), downloadPath);

        List<InvalidParamsItem> invalidParamsItems = importServiceStepDef.importInvalidRandomImages(image.getFilename(), bytes);
        assertAll(
                () -> compareParameters(1, invalidParamsItems.size(), "size"),
                () -> compareParameters(MASTER_CATEGORY_ID.getName(), invalidParamsItems.get(0).name(), "name"),
                () -> compareParameters(getData().errorNotFound(), invalidParamsItems.get(0).type(), "type"),
                () -> compareParameters(getData().errorNotFoundReason(), invalidParamsItems.get(0).reason(), "reason")
        );
    }

    //@Test(description = "Успешный импорт медиафайла c XMP с source_id", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"243221"})
    public void successImportXmpImageWithSourceIdTest() {
        List<DictionariesItem> dictionaries = metadataStepDef.getListSourcesMetadata();
        String brandSourceId = dictionaries.stream().filter(item -> item.name().equals(Source.BRAND.getName())).
                map(DictionariesItem::id).findFirst().orElse(null);

        RequestXmpMetadata xmpMetadata = getData().xmpMetadata().clone();
        xmpMetadata.sourceId(brandSourceId);
        xmpMetadata.sourceName(Source.BRAND.toString().toLowerCase());
        xmpMetadata.sourceCode("0");

        bytes = importServiceStepDef.writeXMPMeta(bytes, image, importServiceStepDef.buildRequestXmpMetadata(xmpMetadata), downloadPath);

        List<String> idList = importServiceStepDef.importImages(Collections.singletonMap(image.getFilename(), bytes));
        Response response = metadataStepDef.checkMetadata(idList.get(0));
        assertAll(
                () -> compareParameters(brandSourceId, getValueFromResponse(response, SOURCE_ID.getPath() + ".id"), "source_id"),
                () -> compareParameters(Source.BRAND.getName(), getValueFromResponse(response, SOURCE_ID.getPath() + ".name"), "source_name")
        );
    }

    //@Test(description = "Неуспешный импорт медиафайла c XMP с source_id - Invalid Type", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"243240"})
    public void unsuccessfulImportXmpImageWithSourceIdInvalidTypeTest() {
        RequestXmpMetadata xmpMetadata = getData().xmpMetadata().clone();
        xmpMetadata.sourceId(RandomStringUtils.randomAlphabetic(6));

        bytes = importServiceStepDef.writeXMPMeta(bytes, image, importServiceStepDef.buildRequestXmpMetadata(xmpMetadata), downloadPath);

        List<InvalidParamsItem> invalidParamsItems = importServiceStepDef.importInvalidRandomImages(image.getFilename(), bytes);
        assertAll(
                () -> compareParameters(1, invalidParamsItems.size(), "size"),
                () -> compareParameters(SOURCE_ID.getName(), invalidParamsItems.get(0).name(), "name"),
                () -> compareParameters(getData().errorInvalidType(), invalidParamsItems.get(0).type(), "type"),
                () -> compareParameters(getData().errorInvalidTypeReason(), invalidParamsItems.get(0).reason(), "reason")
        );
    }

    //@Test(description = "Неуспешный импорт медиафайла c XMP с source_id - Not Found", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"243240"})
    public void unsuccessfulImportXmpImageWithSourceIdNotFoundTest() {
        RequestXmpMetadata xmpMetadata = getData().xmpMetadata().clone();
        xmpMetadata.sourceId(UUID.randomUUID().toString());

        bytes = importServiceStepDef.writeXMPMeta(bytes, image, importServiceStepDef.buildRequestXmpMetadata(xmpMetadata), downloadPath);

        List<InvalidParamsItem> invalidParamsItems = importServiceStepDef.importInvalidRandomImages(image.getFilename(), bytes);
        assertAll(
                () -> compareParameters(1, invalidParamsItems.size(), "size"),
                () -> compareParameters(SOURCE_ID.getName(), invalidParamsItems.get(0).name(), "name"),
                () -> compareParameters(getData().errorNotFound(), invalidParamsItems.get(0).type(), "type"),
                () -> compareParameters(getData().errorNotFoundReason(), invalidParamsItems.get(0).reason(), "reason")
        );
    }

    //@Test(description = "Успешный импорт медиафайла c XMP с own_trademark", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"243222"})
    public void successImportXmpImageWithOwnTrademarkTest() {
        RequestXmpMetadata xmpMetadata = getData().xmpMetadata().clone();
        xmpMetadata.ownTrademark(CAPITAL_TRUE);

        bytes = importServiceStepDef.writeXMPMeta(bytes, image, importServiceStepDef.buildRequestXmpMetadata(xmpMetadata), downloadPath);

        List<String> idList = importServiceStepDef.importImages(Collections.singletonMap(image.getFilename(), bytes));
        Response response = metadataStepDef.checkMetadata(idList.get(0));
        equalsTrueParameter(getValueFromResponse(response, IS_OWN_TRADEMARK.getPath()), "own_trademark");
    }

    //@Test(description = "Неуспешный импорт медиафайла c XMP с own_trademark", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"243241"})
    public void unsuccessfulImportXmpImageWithOwnTrademarkTest() {
        RequestXmpMetadata xmpMetadata = getData().xmpMetadata().clone();
        xmpMetadata.ownTrademark(RandomStringUtils.randomAlphabetic(5));

        bytes = importServiceStepDef.writeXMPMeta(bytes, image, importServiceStepDef.buildRequestXmpMetadata(xmpMetadata), downloadPath);

        List<InvalidParamsItem> invalidParamsItems = importServiceStepDef.importInvalidRandomImages(image.getFilename(), bytes);
        assertAll(
                () -> compareParameters(1, invalidParamsItems.size(), "size"),
                () -> compareParameters(OWN_TRADEMARK.getName(), invalidParamsItems.get(0).name(), "name"),
                () -> compareParameters(getData().errorInvalidType(), invalidParamsItems.get(0).type(), "type"),
                () -> compareParameters(getData().errorInvalidTypeReason(), invalidParamsItems.get(0).reason(), "reason")
        );
    }

    //@Test(description = "Успешный импорт медиафайла c XMP с copyright", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"243223"})
    public void successImportXmpImageWithOwnCopyrightTest() {
        RequestXmpMetadata xmpMetadata = getData().xmpMetadata().clone();
        xmpMetadata.copyright(CAPITAL_TRUE);

        bytes = importServiceStepDef.writeXMPMeta(bytes, image, importServiceStepDef.buildRequestXmpMetadata(xmpMetadata), downloadPath);

        List<String> idList = importServiceStepDef.importImages(Collections.singletonMap(image.getFilename(), bytes));
        Response response = metadataStepDef.checkMetadata(idList.get(0));
        equalsTrueParameter(getValueFromResponse(response, IS_COPYRIGHT.getPath()), "copyright");
    }

    //@Test(description = "Неуспешный импорт медиафайла c XMP с copyright", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"243242"})
    public void unsuccessfulImportXmpImageWithOwnCopyrightTest() {
        RequestXmpMetadata xmpMetadata = getData().xmpMetadata().clone();
        xmpMetadata.copyright(RandomStringUtils.randomAlphabetic(5));

        bytes = importServiceStepDef.writeXMPMeta(bytes, image, importServiceStepDef.buildRequestXmpMetadata(xmpMetadata), downloadPath);

        List<InvalidParamsItem> invalidParamsItems = importServiceStepDef.importInvalidRandomImages(image.getFilename(), bytes);
        assertAll(
                () -> compareParameters(1, invalidParamsItems.size(), "size"),
                () -> compareParameters(IS_COPYRIGHT.getName(), invalidParamsItems.get(0).name(), "name"),
                () -> compareParameters(getData().errorInvalidType(), invalidParamsItems.get(0).type(), "type"),
                () -> compareParameters(getData().errorInvalidTypeReason(), invalidParamsItems.get(0).reason(), "reason")
        );
    }

    //@Test(description = "Успешный импорт медиафайла c XMP с raw_image", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"243224"})
    public void successImportXmpImageWithRawImageTest() {
        RequestXmpMetadata xmpMetadata = getData().xmpMetadata().clone();
        xmpMetadata.rawImage(CAPITAL_FALSE);

        bytes = importServiceStepDef.writeXMPMeta(bytes, image, importServiceStepDef.buildRequestXmpMetadata(xmpMetadata), downloadPath);

        List<String> idList = importServiceStepDef.importImages(Collections.singletonMap(image.getFilename(), bytes));
        Response response = metadataStepDef.checkMetadata(idList.get(0));
        equalsFalseParameter(getValueFromResponse(response, IS_RAW_IMAGE.getPath()), "raw_image");
    }

    //@Test(description = "Неуспешный импорт медиафайла c XMP с raw_image", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"243243"})
    public void unsuccessfulImportXmpImageWithRawImageTest() {
        RequestXmpMetadata xmpMetadata = getData().xmpMetadata().clone();
        xmpMetadata.rawImage(RandomStringUtils.randomAlphabetic(5));

        bytes = importServiceStepDef.writeXMPMeta(bytes, image, importServiceStepDef.buildRequestXmpMetadata(xmpMetadata), downloadPath);

        List<InvalidParamsItem> invalidParamsItems = importServiceStepDef.importInvalidRandomImages(image.getFilename(), bytes);
        assertAll(
                () -> compareParameters(1, invalidParamsItems.size(), "size"),
                () -> compareParameters(IS_RAW_IMAGE.getName(), invalidParamsItems.get(0).name(), "name"),
                () -> compareParameters(getData().errorInvalidType(), invalidParamsItems.get(0).type(), "type"),
                () -> compareParameters(getData().errorInvalidTypeReason(), invalidParamsItems.get(0).reason(), "reason")
        );
    }

    //@Test(description = "Успешный импорт медиафайла c XMP с keywords", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"243225"})
    public void successImportXmpImageWithKeywordsTest() {
        List<String> keywords = Arrays.asList(RandomStringUtils.randomAlphabetic(5), RandomStringUtils.randomAlphabetic(8));

        RequestXmpMetadata xmpMetadata = getData().xmpMetadata().clone();
        xmpMetadata.keywords(keywords);

        bytes = importServiceStepDef.writeXMPMeta(bytes, image, importServiceStepDef.buildRequestXmpMetadata(xmpMetadata), downloadPath);

        List<String> idList = importServiceStepDef.importImages(Collections.singletonMap(image.getFilename(), bytes));
        Response response = metadataStepDef.checkMetadata(idList.get(0));
        compareParameters(keywords, getValueFromResponse(response, KEYWORDS.getPath()), "keywords");
    }

    //@Test(description = "Неуспешный импорт медиафайла c XMP с keywords - Invalid Length", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"243244"})
    public void unsuccessfulImportXmpImageWithKeywordsInvalidLengthTest() {
        RequestXmpMetadata xmpMetadata = getData().xmpMetadata().clone();
        xmpMetadata.keywords(Collections.singletonList(RandomStringUtils.randomAlphabetic(31)));

        bytes = importServiceStepDef.writeXMPMeta(bytes, image, importServiceStepDef.buildRequestXmpMetadata(xmpMetadata), downloadPath);

        List<InvalidParamsItem> invalidParamsItems = importServiceStepDef.importInvalidRandomImages(image.getFilename(), bytes);
        assertAll(
                () -> compareParameters(1, invalidParamsItems.size(), "size"),
                () -> compareParameters(KEYWORDS.getName(), invalidParamsItems.get(0).name(), "name"),
                () -> compareParameters(getData().errorTypes().get(3).type(), invalidParamsItems.get(0).type(), "type"),
                () -> compareParameters(getData().errorTypes().get(3).description(), invalidParamsItems.get(0).reason(), "reason")
        );
    }

    //@Test(description = "Неуспешный импорт медиафайла c XMP с keywords - Invalid Parameter Count", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"243244"})
    public void unsuccessfulImportXmpImageWithKeywordsInvalidParameterCountTest() {
        List<String> keywords = new ArrayList<>();
        for (int i = 0; i < 101; i++) {
            keywords.add(RandomStringUtils.randomAlphabetic(3));
        }

        RequestXmpMetadata xmpMetadata = getData().xmpMetadata().clone();
        xmpMetadata.keywords(keywords);

        bytes = importServiceStepDef.writeXMPMeta(bytes, image, importServiceStepDef.buildRequestXmpMetadata(xmpMetadata), downloadPath);

        List<InvalidParamsItem> invalidParamsItems = importServiceStepDef.importInvalidRandomImages(image.getFilename(), bytes);
        assertAll(
                () -> compareParameters(1, invalidParamsItems.size(), "size"),
                () -> compareParameters(KEYWORDS.getName(), invalidParamsItems.get(0).name(), "name"),
                () -> compareParameters(getData().errorTypes().get(6).type(), invalidParamsItems.get(0).type(), "type"),
                () -> compareParameters(getData().errorTypes().get(6).description(), invalidParamsItems.get(0).reason(), "reason")
        );
    }

    //@Test(description = "Успешный импорт медиафайла c XMP с received", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"243226"})
    public void successImportXmpImageWithReceivedTest() {
        String received = RandomStringUtils.randomAlphabetic(10);

        RequestXmpMetadata xmpMetadata = getData().xmpMetadata().clone();
        xmpMetadata.received(received);

        bytes = importServiceStepDef.writeXMPMeta(bytes, image, importServiceStepDef.buildRequestXmpMetadata(xmpMetadata), downloadPath);

        List<String> idList = importServiceStepDef.importImages(Collections.singletonMap(image.getFilename(), bytes));
        Response response = metadataStepDef.checkMetadata(idList.get(0));
        compareParameters(received, getValueFromResponse(response, RECEIVED.getPath()), "received");
    }

    //@Test(description = "Неуспешный импорт медиафайла c XMP с received", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"243245"})
    public void unsuccessfulImportXmpImageWithReceivedTest() {
        RequestXmpMetadata xmpMetadata = getData().xmpMetadata().clone();
        xmpMetadata.received(RandomStringUtils.randomAlphabetic(151));

        bytes = importServiceStepDef.writeXMPMeta(bytes, image, importServiceStepDef.buildRequestXmpMetadata(xmpMetadata), downloadPath);

        List<InvalidParamsItem> invalidParamsItems = importServiceStepDef.importInvalidRandomImages(image.getFilename(), bytes);
        assertAll(
                () -> compareParameters(1, invalidParamsItems.size(), "size"),
                () -> compareParameters(RECEIVED.getName(), invalidParamsItems.get(0).name(), "name"),
                () -> compareParameters(getData().errorTypes().get(3).type(), invalidParamsItems.get(0).type(), "type"),
                () -> compareParameters(getData().errorTypes().get(3).description(), invalidParamsItems.get(0).reason(), "reason")
        );
    }

    //@Test(description = "Успешный импорт медиафайла c XMP с assignee_id", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"243227"})
    public void successImportXmpImageWithAssigneeIdTest() {
        UsersItem assignees = metadataStepDef.getListUsersMetadata().get(0);

        RequestXmpMetadata xmpMetadata = getData().xmpMetadata().clone();
        xmpMetadata.assigneeId(assignees.id());

        bytes = importServiceStepDef.writeXMPMeta(bytes, image, importServiceStepDef.buildRequestXmpMetadata(xmpMetadata), downloadPath);

        List<String> idList = importServiceStepDef.importImages(Collections.singletonMap(image.getFilename(), bytes));
        Response response = metadataStepDef.checkMetadata(idList.get(0));
        assertAll(
                () -> compareParameters(assignees.id(), getValueFromResponse(response, ASSIGNEE_ID.getPath() + ".id"), "assignee_id"),
                () -> compareParameters(assignees.fullName(), getValueFromResponse(response, ASSIGNEE_ID.getPath() + ".full_name"), "assignee_name")
        );
    }

    //@Test(description = "Неуспешный импорт медиафайла c XMP с assignee_id - Invalid Type", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"243246"})
    public void unsuccessfulImportXmpImageWithAssigneeIdInvalidTypeTest() {
        RequestXmpMetadata xmpMetadata = getData().xmpMetadata().clone();
        xmpMetadata.assigneeId(RandomStringUtils.randomAlphabetic(6));

        bytes = importServiceStepDef.writeXMPMeta(bytes, image, importServiceStepDef.buildRequestXmpMetadata(xmpMetadata), downloadPath);

        List<InvalidParamsItem> invalidParamsItems = importServiceStepDef.importInvalidRandomImages(image.getFilename(), bytes);
        assertAll(
                () -> compareParameters(1, invalidParamsItems.size(), "size"),
                () -> compareParameters(ASSIGNEE_ID.getName(), invalidParamsItems.get(0).name(), "name"),
                () -> compareParameters(getData().errorInvalidType(), invalidParamsItems.get(0).type(), "type"),
                () -> compareParameters(getData().errorInvalidTypeReason(), invalidParamsItems.get(0).reason(), "reason")
        );
    }

    //@Test(description = "Неуспешный импорт медиафайла c XMP с assignee_id - Not Found", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"243246"})
    public void unsuccessfulImportXmpImageWithAssigneeIdTest() {
        RequestXmpMetadata xmpMetadata = getData().xmpMetadata().clone();
        xmpMetadata.assigneeId(UUID.randomUUID().toString());

        bytes = importServiceStepDef.writeXMPMeta(bytes, image, importServiceStepDef.buildRequestXmpMetadata(xmpMetadata), downloadPath);

        List<InvalidParamsItem> invalidParamsItems = importServiceStepDef.importInvalidRandomImages(image.getFilename(), bytes);
        assertAll(
                () -> compareParameters(1, invalidParamsItems.size(), "size"),
                () -> compareParameters(ASSIGNEE_ID.getName(), invalidParamsItems.get(0).name(), "name"),
                () -> compareParameters(getData().errorNotFound(), invalidParamsItems.get(0).type(), "type"),
                () -> compareParameters(getData().errorNotFoundReason(), invalidParamsItems.get(0).reason(), "reason")
        );
    }

    //@Test(description = "Успешный импорт медиафайла c XMP с external_task_id", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"243228"})
    public void successImportXmpImageWithExternalTaskIdTest() {
        String externalTaskId = String.valueOf(new Random().nextInt(1000) + 1);

        RequestXmpMetadata xmpMetadata = getData().xmpMetadata().clone();
        xmpMetadata.externalTaskId(externalTaskId);

        bytes = importServiceStepDef.writeXMPMeta(bytes, image, importServiceStepDef.buildRequestXmpMetadata(xmpMetadata), downloadPath);

        List<String> idList = importServiceStepDef.importImages(Collections.singletonMap(image.getFilename(), bytes));
        Response response = metadataStepDef.checkMetadata(idList.get(0));
        compareParameters(externalTaskId, getValueFromResponse(response, EXTERNAL_TASK_ID.getPath()), "external_task_id");
    }

    //@Test(description = "Неуспешный импорт медиафайла c XMP с external_task_id", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"243247"})
    public void unsuccessfulImportXmpImageWithExternalTaskIdTest() {
        RequestXmpMetadata xmpMetadata = getData().xmpMetadata().clone();
        xmpMetadata.externalTaskId(RandomStringUtils.randomAlphabetic(10));

        bytes = importServiceStepDef.writeXMPMeta(bytes, image, importServiceStepDef.buildRequestXmpMetadata(xmpMetadata), downloadPath);

        List<InvalidParamsItem> invalidParamsItems = importServiceStepDef.importInvalidRandomImages(image.getFilename(), bytes);
        assertAll(
                () -> compareParameters(1, invalidParamsItems.size(), "size"),
                () -> compareParameters(EXTERNAL_TASK_ID.getName(), invalidParamsItems.get(0).name(), "name"),
                () -> compareParameters(getData().errorInvalidType(), invalidParamsItems.get(0).type(), "type"),
                () -> compareParameters(getData().errorInvalidTypeReason(), invalidParamsItems.get(0).reason(), "reason")
        );
    }

    //@Test(description = "Успешный импорт медиафайла c XMP с sku", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"243229"})
    public void successImportXmpImageWithSkuTest() {
        String sku = RandomStringUtils.randomAlphabetic(5);

        RequestXmpMetadata xmpMetadata = getData().xmpMetadata().clone();
        xmpMetadata.sku(sku);

        bytes = importServiceStepDef.writeXMPMeta(bytes, image, importServiceStepDef.buildRequestXmpMetadata(xmpMetadata), downloadPath);

        List<String> idList = importServiceStepDef.importImages(Collections.singletonMap(image.getFilename(), bytes));
        Response response = metadataStepDef.checkMetadata(idList.get(0));
        compareParameters(sku, getValueFromResponse(response, SKU.getPath()), "sku");
    }

    //@Test(description = "Неуспешный импорт медиафайла c XMP с sku", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"243248"})
    public void unsuccessfulImportXmpImageWithSkuTest() {
        RequestXmpMetadata xmpMetadata = getData().xmpMetadata().clone();
        xmpMetadata.sku(RandomStringUtils.randomAlphabetic(101));

        bytes = importServiceStepDef.writeXMPMeta(bytes, image, importServiceStepDef.buildRequestXmpMetadata(xmpMetadata), downloadPath);

        List<InvalidParamsItem> invalidParamsItems = importServiceStepDef.importInvalidRandomImages(image.getFilename(), bytes);
        assertAll(
                () -> compareParameters(1, invalidParamsItems.size(), "size"),
                () -> compareParameters(SKU.getName(), invalidParamsItems.get(0).name(), "name"),
                () -> compareParameters(getData().errorTypes().get(3).type(), invalidParamsItems.get(0).type(), "type"),
                () -> compareParameters(getData().errorTypes().get(3).description(), invalidParamsItems.get(0).reason(), "reason")
        );
    }

    //@Test(description = "Успешный импорт медиафайла c XMP - все параметры", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"243230"})
    public void successImportXmpImageWithAllParametersTest() {
        DictionariesItem category = metadataStepDef.getListCategoriesMetadata().get(0);
        DictionariesItem sources = metadataStepDef.getListSourcesMetadata().get(0);
        List<String> keywords = Arrays.asList(RandomStringUtils.randomAlphabetic(5), RandomStringUtils.randomAlphabetic(8));
        String received = RandomStringUtils.randomAlphabetic(10);
        UsersItem assignees = metadataStepDef.getListUsersMetadata().get(0);
        String externalTaskId = String.valueOf(new Random().nextInt(1000) + 1);
        String sku = RandomStringUtils.randomAlphabetic(5);

        RequestXmpMetadata xmpMetadata = getData().xmpMetadata().clone();
        xmpMetadata.masterCategoryId(category.id());
        xmpMetadata.masterCategoryName(category.name());
        xmpMetadata.sourceId(sources.id());
        xmpMetadata.sourceName(sources.name());
        xmpMetadata.sourceCode("0");
        xmpMetadata.ownTrademark(CAPITAL_TRUE);
        xmpMetadata.copyright(CAPITAL_TRUE);
        xmpMetadata.rawImage(CAPITAL_FALSE);
        xmpMetadata.keywords(keywords);
        xmpMetadata.received(received);
        xmpMetadata.assigneeId(assignees.id());
        xmpMetadata.externalTaskId(externalTaskId);
        xmpMetadata.sku(sku);


        bytes = importServiceStepDef.writeXMPMeta(bytes, image, importServiceStepDef.buildRequestXmpMetadata(xmpMetadata), downloadPath);

        List<String> idList = importServiceStepDef.importImages(Collections.singletonMap(image.getFilename(), bytes));
        Response response = metadataStepDef.checkMetadata(idList.get(0));
        assertAll(
                () -> compareParameters(category.id(), getValueFromResponse(response, MASTER_CATEGORY_ID.getPath() + ".id"), "category_id"),
                () -> compareParameters(category.name(), getValueFromResponse(response, MASTER_CATEGORY_ID.getPath() + ".name"), "category_name"),
                () -> compareParameters(sources.id(), getValueFromResponse(response, SOURCE_ID.getPath() + ".id"), "source_id"),
                () -> compareParameters(sources.name(), getValueFromResponse(response, SOURCE_ID.getPath() + ".name"), "source_name"),
                () -> equalsTrueParameter(getValueFromResponse(response, IS_OWN_TRADEMARK.getPath()), "own_trademark"),
                () -> equalsTrueParameter(getValueFromResponse(response, IS_COPYRIGHT.getPath()), "copyright"),
                () -> equalsFalseParameter(getValueFromResponse(response, IS_RAW_IMAGE.getPath()), "raw_image"),
                () -> compareParameters(keywords, getValueFromResponse(response, KEYWORDS.getPath()), "keywords"),
                () -> compareParameters(received, getValueFromResponse(response, RECEIVED.getPath()), "received"),
                () -> compareParameters(assignees.id(), getValueFromResponse(response, ASSIGNEE_ID.getPath() + ".id"), "assignee_id"),
                () -> compareParameters(assignees.fullName(), getValueFromResponse(response, ASSIGNEE_ID.getPath() + ".full_name"), "assignee_name"),
                () -> compareParameters(externalTaskId, getValueFromResponse(response, EXTERNAL_TASK_ID.getPath()), "external_task_id"),
                () -> compareParameters(sku, getValueFromResponse(response, SKU.getPath()), "sku")
        );
    }

    @Test(description = "Успешный импорт медиафайла c XMP без нескольких полей", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"243232"})
    public void successImportXmpImageWithoutSomeParametersTest() {
        RequestXmpMetadata xmpMetadata = getData().xmpMetadata().clone();
        XMPMeta xmpMeta = importServiceStepDef.buildRequestXmpMetadata(xmpMetadata);
        xmpMeta.deleteProperty(NS_JPEG, DESCRIPTION.getName());
        xmpMeta.deleteProperty(NS_JPEG, MASTER_CATEGORY.getName());
        xmpMeta.deleteProperty(NS_JPEG, OWN_TRADEMARK.getName());
        xmpMeta.deleteProperty(NS_JPEG, KEYWORDS.getName());
        xmpMeta.deleteProperty(NS_JPEG, EXTERNAL_TASK_ID.getName());

        bytes = importServiceStepDef.writeXMPMeta(bytes, image, importServiceStepDef.buildRequestXmpMetadata(xmpMetadata), downloadPath);

        List<String> idList = importServiceStepDef.importImages(Collections.singletonMap(image.getFilename(), bytes));
        Response response = metadataStepDef.checkMetadata(idList.get(0));
        assertAll(
                () -> mustBeNullParameter(getValueFromResponse(response, DESCRIPTION.getPath()), "description"),
                () -> mustBeNullParameter(getValueFromResponse(response, MASTER_CATEGORY_ID.getPath()), "category"),
                () -> mustBeNullParameter(getValueFromResponse(response, IS_OWN_TRADEMARK.getPath()), "own_trademark"),
                () -> mustBeNullParameter(getValueFromResponse(response, KEYWORDS.getPath()), "keywords"),
                () -> mustBeNullParameter(getValueFromResponse(response, EXTERNAL_TASK_ID.getPath()), "external_task_id")
        );
    }

    @Test(description = "Успешный импорт медиафайла c XMP с filename - незаполняемое поле", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"243233"})
    public void successImportXmpImageWithFilenameTest() {
        String filename = RandomStringUtils.randomAlphabetic(5);

        RequestXmpMetadata xmpMetadata = getData().xmpMetadata().clone();
        xmpMetadata.filename(filename);

        bytes = importServiceStepDef.writeXMPMeta(bytes, image, importServiceStepDef.buildRequestXmpMetadata(xmpMetadata), downloadPath);

        List<String> idList = importServiceStepDef.importImages(Collections.singletonMap(image.getFilename(), bytes));
        Response response = metadataStepDef.checkMetadata(idList.get(0));
        notEquals(filename, getValueFromResponse(response, FILENAME.getPath()), "filename");
    }

    @Test(description = "Успешный импорт медиафайла c XMP с origin_filename - незаполняемое поле", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"243234"})
    public void successImportXmpImageWithOriginFilenameTest() {
        String originFilename = RandomStringUtils.randomAlphabetic(5);

        RequestXmpMetadata xmpMetadata = getData().xmpMetadata().clone();
        xmpMetadata.originFilename(originFilename);

        bytes = importServiceStepDef.writeXMPMeta(bytes, image, importServiceStepDef.buildRequestXmpMetadata(xmpMetadata), downloadPath);

        List<String> idList = importServiceStepDef.importImages(Collections.singletonMap(image.getFilename(), bytes));
        Response response = metadataStepDef.checkMetadata(idList.get(0));
        notEquals(originFilename, getValueFromResponse(response, ORIGIN_FILENAME.getPath()), "origin_filename");
    }

    @Test(description = "Успешный импорт медиафайла c XMP с priority - незаполняемое поле", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"243235"})
    public void successImportXmpImageWithPriorityTest() {
        String priority = String.valueOf(new Random().nextInt(100) + 1);

        RequestXmpMetadata xmpMetadata = getData().xmpMetadata().clone();
        xmpMetadata.originFilename(priority);

        bytes = importServiceStepDef.writeXMPMeta(bytes, image, importServiceStepDef.buildRequestXmpMetadata(xmpMetadata), downloadPath);

        List<String> idList = importServiceStepDef.importImages(Collections.singletonMap(image.getFilename(), bytes));
        Response response = metadataStepDef.checkMetadata(idList.get(0));
        mustBeNullParameter(getValueFromResponse(response, PRIORITY.getPath()), "priority");
    }

    @Test(description = "Успешный импорт медиафайла c XMP с quality - незаполняемое поле", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"243236"})
    public void successImportXmpImageWithQualityTest() {
        List<DictionariesItem> dictionaries = metadataStepDef.getListQualitiesMetadata();
        String toRevisionQualityId = dictionaries.stream().filter(item -> item.name().equals(Quality.TO_REVISION.getName())).
                map(DictionariesItem::id).findFirst().orElse(null);

        RequestXmpMetadata xmpMetadata = getData().xmpMetadata().clone();
        xmpMetadata.qualityId(toRevisionQualityId);
        xmpMetadata.qualityName(Quality.TO_REVISION.toString().toLowerCase());
        xmpMetadata.qualityCode("3");

        bytes = importServiceStepDef.writeXMPMeta(bytes, image, importServiceStepDef.buildRequestXmpMetadata(xmpMetadata), downloadPath);

        List<String> idList = importServiceStepDef.importImages(Collections.singletonMap(image.getFilename(), bytes));
        Response response = metadataStepDef.checkMetadata(idList.get(0));
        compareParameters(Quality.GOOD.getName(), getValueFromResponse(response, QUALITY_NAME.getPath()), "quality_name");
    }

    @Test(description = "Успешный импорт медиафайла c XMP с status - незаполняемое поле", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"243237"})
    public void successImportXmpImageWithStatusTest() {
        List<DictionariesItem> dictionaries = metadataStepDef.getListStatusesMetadata();
        String inProgressStatusId = dictionaries.stream().filter(item -> item.name().equals(Status.IN_PROGRESS.getName())).
                map(DictionariesItem::id).findFirst().orElse(null);

        RequestXmpMetadata xmpMetadata = getData().xmpMetadata().clone();
        xmpMetadata.statusId(inProgressStatusId);
        xmpMetadata.statusName(Status.IN_PROGRESS.toString().toLowerCase());
        xmpMetadata.statusCode("3");

        bytes = importServiceStepDef.writeXMPMeta(bytes, image, importServiceStepDef.buildRequestXmpMetadata(xmpMetadata), downloadPath);

        List<String> idList = importServiceStepDef.importImages(Collections.singletonMap(image.getFilename(), bytes));
        Response response = metadataStepDef.checkMetadata(idList.get(0));
        compareParameters(Status.NEW.getName(), getValueFromResponse(response, STATUS_NAME.getPath()), "status_name");
    }

    @Test(description = "Успешный импорт медиафайла c XMP с description - незаполняемое поле", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"243238"})
    public void successImportXmpImageWithDescriptionTest() {
        String description = RandomStringUtils.randomAlphabetic(19);

        RequestXmpMetadata xmpMetadata = getData().xmpMetadata().clone();
        xmpMetadata.description(description);

        bytes = importServiceStepDef.writeXMPMeta(bytes, image, importServiceStepDef.buildRequestXmpMetadata(xmpMetadata), downloadPath);

        List<String> idList = importServiceStepDef.importImages(Collections.singletonMap(image.getFilename(), bytes));
        Response response = metadataStepDef.checkMetadata(idList.get(0));
        mustBeNullParameter(getValueFromResponse(response, DESCRIPTION.getPath()), "description");
    }
}