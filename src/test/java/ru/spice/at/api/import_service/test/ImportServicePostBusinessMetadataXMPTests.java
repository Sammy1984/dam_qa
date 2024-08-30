package ru.spice.at.api.import_service.test;

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
import ru.spice.at.api.dto.response.metadata.DictionariesItem;
import ru.spice.at.api.dto.response.metadata.UsersItem;
import ru.spice.at.api.import_service.ImportServiceSettings;
import ru.spice.at.api.import_service.ImportServiceStepDef;
import ru.spice.at.api.metadata_service.MetadataStepDef;
import ru.spice.at.common.base_test.BaseApiTest;
import ru.spice.at.common.dto.dam.ImageData;
import ru.spice.at.common.emuns.ApiServices;
import ru.spice.at.common.emuns.dam.ImageFormat;
import ru.spice.at.common.emuns.dam.Source;

import java.util.*;

import static ru.spice.at.common.constants.TestConstants.CAPITAL_FALSE;
import static ru.spice.at.common.constants.TestConstants.CAPITAL_TRUE;
import static ru.spice.at.common.emuns.dam.ImageParameters.*;
import static ru.spice.at.common.utils.Assert.*;
import static ru.spice.at.common.utils.ImageHelper.getRandomByteImage;
import static ru.spice.at.common.utils.JsonHelper.getValueFromResponse;

@Deprecated
@Feature("Import Service")
@Story("POST business-metadata with metadata XMP")
public class ImportServicePostBusinessMetadataXMPTests extends BaseApiTest<ImportServiceSettings> {
    private ImportServiceStepDef importServiceStepDef;
    private MetadataStepDef metadataStepDef;
    private byte[] bytes;
    private ImageData image;

    protected ImportServicePostBusinessMetadataXMPTests() {
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

    @Test(description = "Успешный импорт медиафайла c XMP и бизнес метадатой с category_id", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"243249"})
    public void successImportXmpImageWithCategoryIdTest() {
        List<DictionariesItem> category = metadataStepDef.getListCategoriesMetadata();

        RequestXmpMetadata xmpMetadata = getData().xmpMetadata().clone();
        xmpMetadata.masterCategoryId(category.get(0).id());
        xmpMetadata.masterCategoryName(category.get(0).name());

        bytes = importServiceStepDef.writeXMPMeta(bytes, image, importServiceStepDef.buildRequestXmpMetadata(xmpMetadata), downloadPath);

        String metadataId = importServiceStepDef.successImportBusinessMetadata(MASTER_CATEGORY_ID.getName(), category.get(1).id());

        String id = importServiceStepDef.importRandomImageWithBusinessMetadata(image.getFilename(), bytes, metadataId);
        Response response = metadataStepDef.checkMetadata(id);
        assertAll(
                () -> compareParameters(category.get(1).id(), getValueFromResponse(response, MASTER_CATEGORY_ID.getPath() + ".id"), "category_id"),
                () -> compareParameters(category.get(1).name(), getValueFromResponse(response, MASTER_CATEGORY_ID.getPath() + ".name"), "category_name")
        );
    }

    @Test(description = "Успешный импорт медиафайла c XMP и бизнес метадатой с source_id", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"243251"})
    public void successImportXmpImageWithSourceIdTest() {
        List<DictionariesItem> dictionaries = metadataStepDef.getListSourcesMetadata();
        String brandSourceId = dictionaries.stream().filter(item -> item.name().equals(Source.BRAND.getName())).
                map(DictionariesItem::id).findFirst().orElse(null);
        String sellerSourceId = dictionaries.stream().filter(item -> item.name().equals(Source.RESTAURANT.getName())).
                map(DictionariesItem::id).findFirst().orElse(null);

        RequestXmpMetadata xmpMetadata = getData().xmpMetadata().clone();
        xmpMetadata.sourceId(brandSourceId);
        xmpMetadata.sourceName(Source.BRAND.toString().toLowerCase());
        xmpMetadata.sourceCode("0");

        bytes = importServiceStepDef.writeXMPMeta(bytes, image, importServiceStepDef.buildRequestXmpMetadata(xmpMetadata), downloadPath);

        String metadataId = importServiceStepDef.successImportBusinessMetadata(SOURCE_ID.getName(), sellerSourceId);

        String id = importServiceStepDef.importRandomImageWithBusinessMetadata(image.getFilename(), bytes, metadataId);
        Response response = metadataStepDef.checkMetadata(id);
        assertAll(
                () -> compareParameters(sellerSourceId, getValueFromResponse(response, SOURCE_ID.getPath() + ".id"), "source_id"),
                () -> compareParameters(Source.RESTAURANT.getName(), getValueFromResponse(response, SOURCE_ID.getPath() + ".name"), "source_name")
        );
    }

    @Test(description = "Успешный импорт медиафайла c XMP и бизнес метадатой с own_trademark", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"243252"})
    public void successImportXmpImageWithOwnTrademarkTest() {
        RequestXmpMetadata xmpMetadata = getData().xmpMetadata().clone();
        xmpMetadata.ownTrademark(CAPITAL_TRUE);

        bytes = importServiceStepDef.writeXMPMeta(bytes, image, importServiceStepDef.buildRequestXmpMetadata(xmpMetadata), downloadPath);

        String metadataId = importServiceStepDef.successImportBusinessMetadata(OWN_TRADEMARK.getName(), false);

        String id = importServiceStepDef.importRandomImageWithBusinessMetadata(image.getFilename(), bytes, metadataId);
        Response response = metadataStepDef.checkMetadata(id);
        equalsFalseParameter(getValueFromResponse(response, IS_OWN_TRADEMARK.getPath()), "own_trademark");
    }

    @Test(description = "Успешный импорт медиафайла c XMP и бизнес метадатой с copyright", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"243253"})
    public void successImportXmpImageWithOwnCopyrightTest() {
        RequestXmpMetadata xmpMetadata = getData().xmpMetadata().clone();
        xmpMetadata.copyright(CAPITAL_TRUE);

        bytes = importServiceStepDef.writeXMPMeta(bytes, image, importServiceStepDef.buildRequestXmpMetadata(xmpMetadata), downloadPath);

        String metadataId = importServiceStepDef.successImportBusinessMetadata(COPYRIGHT.getName(), false);

        String id = importServiceStepDef.importRandomImageWithBusinessMetadata(image.getFilename(), bytes, metadataId);
        Response response = metadataStepDef.checkMetadata(id);
        equalsFalseParameter(getValueFromResponse(response, IS_COPYRIGHT.getPath()), "copyright");
    }

    //todo тест устарел
    //@Test(description = "Успешный импорт медиафайла c XMP и бизнес метадатой с raw_image", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"243254"})
    public void successImportXmpImageWithRawImageTest() {
        RequestXmpMetadata xmpMetadata = getData().xmpMetadata().clone();
        xmpMetadata.rawImage(CAPITAL_FALSE);

        bytes = importServiceStepDef.writeXMPMeta(bytes, image, importServiceStepDef.buildRequestXmpMetadata(xmpMetadata), downloadPath);

        String metadataId = importServiceStepDef.successImportBusinessMetadata(RAW_IMAGE.getName(), true);

        String id = importServiceStepDef.importRandomImageWithBusinessMetadata(image.getFilename(), bytes, metadataId);
        Response response = metadataStepDef.checkMetadata(id);
        equalsTrueParameter(getValueFromResponse(response, IS_RAW_IMAGE.getPath()), "raw_image");
    }

    @Test(description = "Успешный импорт медиафайла c XMP и бизнес метадатой с keywords", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"243255"})
    public void successImportXmpImageWithKeywordsTest() {
        RequestXmpMetadata xmpMetadata = getData().xmpMetadata().clone();
        xmpMetadata.keywords(Arrays.asList(RandomStringUtils.randomAlphabetic(5), RandomStringUtils.randomAlphabetic(8)));

        bytes = importServiceStepDef.writeXMPMeta(bytes, image, importServiceStepDef.buildRequestXmpMetadata(xmpMetadata), downloadPath);

        List<String> businessKeywords = Arrays.asList(
                RandomStringUtils.randomAlphabetic(7),
                RandomStringUtils.randomAlphabetic(12),
                RandomStringUtils.randomAlphabetic(10)
        );
        String metadataId = importServiceStepDef.successImportBusinessMetadata(KEYWORDS.getName(), businessKeywords);

        String id = importServiceStepDef.importRandomImageWithBusinessMetadata(image.getFilename(), bytes, metadataId);
        Response response = metadataStepDef.checkMetadata(id);
        compareParameters(businessKeywords, getValueFromResponse(response, KEYWORDS.getPath()), "keywords");
    }

    //todo тест устарел
    //@Test(description = "Успешный импорт медиафайла c XMP и бизнес метадатой с received", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"243257"})
    public void successImportXmpImageWithReceivedTest() {
        RequestXmpMetadata xmpMetadata = getData().xmpMetadata().clone();
        xmpMetadata.received(RandomStringUtils.randomAlphabetic(10));

        bytes = importServiceStepDef.writeXMPMeta(bytes, image, importServiceStepDef.buildRequestXmpMetadata(xmpMetadata), downloadPath);

        String received = RandomStringUtils.randomAlphabetic(10);
        String metadataId = importServiceStepDef.successImportBusinessMetadata(RECEIVED.getName(), received);

        String id = importServiceStepDef.importRandomImageWithBusinessMetadata(image.getFilename(), bytes, metadataId);
        Response response = metadataStepDef.checkMetadata(id);
        compareParameters(received, getValueFromResponse(response, RECEIVED.getPath()), "received");
    }

    @Test(description = "Успешный импорт медиафайла c XMP и бизнес метадатой с assignee_id", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"243258"})
    public void successImportXmpImageWithAssigneeIdTest() {
        List<UsersItem> assignees = metadataStepDef.getListUsersMetadata();

        RequestXmpMetadata xmpMetadata = getData().xmpMetadata().clone();
        xmpMetadata.assigneeId(assignees.get(0).id());

        bytes = importServiceStepDef.writeXMPMeta(bytes, image, importServiceStepDef.buildRequestXmpMetadata(xmpMetadata), downloadPath);

        String metadataId = importServiceStepDef.successImportBusinessMetadata(ASSIGNEE_ID.getName(), assignees.get(1).id());

        String id = importServiceStepDef.importRandomImageWithBusinessMetadata(image.getFilename(), bytes, metadataId);
        Response response = metadataStepDef.checkMetadata(id);
        assertAll(
                () -> compareParameters(assignees.get(1).id(), getValueFromResponse(response, ASSIGNEE_ID.getPath() + ".id"), "assignee_id"),
                () -> compareParameters(assignees.get(1).fullName(), getValueFromResponse(response, ASSIGNEE_ID.getPath() + ".full_name"), "assignee_name")
        );
    }

    @Test(description = "Успешный импорт медиафайла c XMP и бизнес метадатой с external_task_id", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"243259"})
    public void successImportXmpImageWithExternalTaskIdTest() {
        RequestXmpMetadata xmpMetadata = getData().xmpMetadata().clone();
        xmpMetadata.externalTaskId(String.valueOf(new Random().nextInt(1000) + 1));

        bytes = importServiceStepDef.writeXMPMeta(bytes, image, importServiceStepDef.buildRequestXmpMetadata(xmpMetadata), downloadPath);

        String externalTaskId = String.valueOf(new Random().nextInt(1000) + 1);
        String metadataId = importServiceStepDef.successImportBusinessMetadata(EXTERNAL_TASK_ID.getName(), externalTaskId);

        String id = importServiceStepDef.importRandomImageWithBusinessMetadata(image.getFilename(), bytes, metadataId);
        Response response = metadataStepDef.checkMetadata(id);
        compareParameters(externalTaskId, getValueFromResponse(response, EXTERNAL_TASK_ID.getPath()), "external_task_id");
    }

    @Test(description = "Успешный импорт медиафайла c XMP и бизнес метадатой - все параметры", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"243260"})
    public void successImportXmpImageWithAllParametersTest() {
        List<DictionariesItem> category = metadataStepDef.getListCategoriesMetadata();
        List<DictionariesItem> sources = metadataStepDef.getListSourcesMetadata();
        List<UsersItem> assignees = metadataStepDef.getListUsersMetadata();

        RequestXmpMetadata xmpMetadata = getData().xmpMetadata().clone();
        xmpMetadata.masterCategoryId(category.get(0).id());
        xmpMetadata.masterCategoryName(category.get(0).name());
        xmpMetadata.sourceId(sources.get(0).id());
        xmpMetadata.sourceName(sources.get(0).name());
        xmpMetadata.ownTrademark(CAPITAL_TRUE);
        xmpMetadata.copyright(CAPITAL_TRUE);
        xmpMetadata.rawImage(CAPITAL_FALSE);
        xmpMetadata.keywords(Arrays.asList(RandomStringUtils.randomAlphabetic(4), RandomStringUtils.randomAlphabetic(9)));
        xmpMetadata.received(RandomStringUtils.randomAlphabetic(15));
        xmpMetadata.assigneeId(assignees.get(0).id());
        xmpMetadata.externalTaskId(String.valueOf(new Random().nextInt(1000) + 1));

        bytes = importServiceStepDef.writeXMPMeta(bytes, image, importServiceStepDef.buildRequestXmpMetadata(xmpMetadata), downloadPath);

        List<String> keywords = Arrays.asList(RandomStringUtils.randomAlphabetic(5), RandomStringUtils.randomAlphabetic(8));
        String externalTaskId = String.valueOf(new Random().nextInt(1000) + 1);

        Map<String, Object> metadata = new HashMap<String, Object>() {{
            put(MASTER_CATEGORY_ID.getName(), category.get(1).id());
            put(SOURCE_ID.getName(), sources.get(1).id());
            put(KEYWORDS.getName(), keywords);
            put(OWN_TRADEMARK.getName(), false);
            put(COPYRIGHT.getName(), false);
            put(ASSIGNEE_ID.getName(), assignees.get(1).id());
            put(EXTERNAL_TASK_ID.getName(), externalTaskId);
        }};

        String metadataId = importServiceStepDef.successImportBusinessMetadata(Collections.singletonMap("data", metadata));

        String id = importServiceStepDef.importRandomImageWithBusinessMetadata(image.getFilename(), bytes, metadataId);
        Response response = metadataStepDef.checkMetadata(id);
        assertAll(
                () -> compareParameters(category.get(1).id(), getValueFromResponse(response, MASTER_CATEGORY_ID.getPath() + ".id"), "category_id"),
                () -> compareParameters(category.get(1).name(), getValueFromResponse(response, MASTER_CATEGORY_ID.getPath() + ".name"), "category_name"),
                () -> compareParameters(sources.get(1).id(), getValueFromResponse(response, SOURCE_ID.getPath() + ".id"), "source_id"),
                () -> compareParameters(sources.get(1).name(), getValueFromResponse(response, SOURCE_ID.getPath() + ".name"), "source_name"),
                () -> equalsFalseParameter(getValueFromResponse(response, IS_OWN_TRADEMARK.getPath()), "own_trademark"),
                () -> equalsFalseParameter(getValueFromResponse(response, IS_COPYRIGHT.getPath()), "copyright"),
                () -> compareParameters(keywords, getValueFromResponse(response, KEYWORDS.getPath()), "keywords"),
                () -> compareParameters(assignees.get(1).id(), getValueFromResponse(response, ASSIGNEE_ID.getPath() + ".id"), "assignee_id"),
                () -> compareParameters(assignees.get(1).fullName(), getValueFromResponse(response, ASSIGNEE_ID.getPath() + ".full_name"), "assignee_name"),
                () -> compareParameters(externalTaskId, getValueFromResponse(response, EXTERNAL_TASK_ID.getPath()), "external_task_id")
        );
    }

    //@Test(description = "Успешный импорт медиафайла c частью метадаты из XMP и частью из бизнес метадаты", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"243261"})
    public void successImportXmpImageWithSomeXMPSomeBusinessParametersTest() {
        List<DictionariesItem> category = metadataStepDef.getListCategoriesMetadata();
        List<DictionariesItem> sources = metadataStepDef.getListSourcesMetadata();
        List<UsersItem> assignees = metadataStepDef.getListUsersMetadata();

        List<String> keywords = Arrays.asList(RandomStringUtils.randomAlphabetic(5), RandomStringUtils.randomAlphabetic(8));
        String received = RandomStringUtils.randomAlphabetic(10);
        String externalTaskId = String.valueOf(new Random().nextInt(1000) + 1);

        RequestXmpMetadata xmpMetadata = getData().xmpMetadata().clone();
        xmpMetadata.copyright(CAPITAL_TRUE);
        xmpMetadata.rawImage(CAPITAL_FALSE);
        xmpMetadata.keywords(keywords);
        xmpMetadata.received(received);
        xmpMetadata.assigneeId(assignees.get(0).id());
        xmpMetadata.externalTaskId(externalTaskId);

        bytes = importServiceStepDef.writeXMPMeta(bytes, image, importServiceStepDef.buildRequestXmpMetadata(xmpMetadata), downloadPath);

        Map<String, Object> metadata = new HashMap<String, Object>() {{
            put(MASTER_CATEGORY_ID.getName(), category.get(1).id());
            put(SOURCE_ID.getName(), sources.get(1).id());
            put(OWN_TRADEMARK.getName(), false);
        }};

        String metadataId = importServiceStepDef.successImportBusinessMetadata(Collections.singletonMap("data", metadata));

        String id = importServiceStepDef.importRandomImageWithBusinessMetadata(image.getFilename(), bytes, metadataId);
        Response response = metadataStepDef.checkMetadata(id);
        assertAll(
                () -> compareParameters(category.get(1).id(), getValueFromResponse(response, MASTER_CATEGORY_ID.getPath() + ".id"), "category_id"),
                () -> compareParameters(category.get(1).name(), getValueFromResponse(response, MASTER_CATEGORY_ID.getPath() + ".name"), "category_name"),
                () -> compareParameters(sources.get(1).id(), getValueFromResponse(response, SOURCE_ID.getPath() + ".id"), "source_id"),
                () -> compareParameters(sources.get(1).name(), getValueFromResponse(response, SOURCE_ID.getPath() + ".name"), "source_name"),
                () -> equalsFalseParameter(getValueFromResponse(response, IS_OWN_TRADEMARK.getPath()), "own_trademark"),
                () -> equalsTrueParameter(getValueFromResponse(response, IS_COPYRIGHT.getPath()), "copyright"),
                () -> equalsFalseParameter(getValueFromResponse(response, IS_RAW_IMAGE.getPath()), "raw_image"),
                () -> compareParameters(keywords, getValueFromResponse(response, KEYWORDS.getPath()), "keywords"),
                () -> compareParameters(received, getValueFromResponse(response, RECEIVED.getPath()), "received"),
                () -> compareParameters(assignees.get(0).id(), getValueFromResponse(response, ASSIGNEE_ID.getPath() + ".id"), "assignee_id"),
                () -> compareParameters(assignees.get(0).fullName(), getValueFromResponse(response, ASSIGNEE_ID.getPath() + ".full_name"), "assignee_name"),
                () -> compareParameters(externalTaskId, getValueFromResponse(response, EXTERNAL_TASK_ID.getPath()), "external_task_id")
        );
    }
}