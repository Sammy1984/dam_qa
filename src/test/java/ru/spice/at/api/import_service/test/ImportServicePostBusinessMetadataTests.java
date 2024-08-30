package ru.spice.at.api.import_service.test;

import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import ru.testit.annotations.WorkItemIds;
import io.restassured.response.Response;
import org.apache.commons.lang3.RandomStringUtils;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.spice.at.api.dto.response.import_service.InvalidParamsItem;
import ru.spice.at.api.dto.response.metadata.DictionariesItem;
import ru.spice.at.api.import_service.ImportServiceSettings;
import ru.spice.at.api.import_service.ImportServiceStepDef;
import ru.spice.at.api.metadata_service.MetadataStepDef;
import ru.spice.at.common.base_test.BaseApiTest;
import ru.spice.at.common.dto.dam.ImageData;
import ru.spice.at.common.emuns.ApiServices;
import ru.spice.at.common.emuns.dam.ImageFormat;

import java.util.*;

import static ru.spice.at.common.constants.TestConstants.*;
import static ru.spice.at.common.emuns.dam.ImageParameters.*;
import static ru.spice.at.common.utils.Assert.*;
import static ru.spice.at.common.utils.Assert.compareParameters;
import static ru.spice.at.common.utils.JsonHelper.getValueFromResponse;

@Feature("Import Service")
@Story("POST business-metadata")
public class ImportServicePostBusinessMetadataTests extends BaseApiTest<ImportServiceSettings> {
    private ImportServiceStepDef importServiceStepDef;
    private MetadataStepDef metadataStepDef;

    protected ImportServicePostBusinessMetadataTests() {
        super(ApiServices.IMPORT_SERVICE);
    }

    @BeforeClass(alwaysRun = true)
    public void beforeClass() {
        importServiceStepDef = new ImportServiceStepDef();
        metadataStepDef = new MetadataStepDef(importServiceStepDef.getAuthToken());
    }

    //todo выключено для использования параллельного сьюта
    //@AfterClass(alwaysRun = true)
    public void afterClass() {
        importServiceStepDef.deleteMedia();
        metadataStepDef.deleteMetadata();
    }

    @Test(description = "Успешный импорт медиафайла с category_id", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"225850"})
    public void successImportImageWithCategoryIdTest() {
        DictionariesItem category = metadataStepDef.getListCategoriesMetadata().get(0);
        String metadataId = importServiceStepDef.successImportBusinessMetadata(MASTER_CATEGORY_ID.getName(), category.id());
        String imageId = importServiceStepDef.importRandomImageWithBusinessMetadata(new ImageData(ImageFormat.JPEG), metadataId);
        Response response = metadataStepDef.checkMetadata(imageId);
        assertAll(
                () -> compareParameters(category.id(), getValueFromResponse(response, MASTER_CATEGORY_ID.getPath() + ".id"), "category.id"),
                () -> compareParameters(category.name(), getValueFromResponse(response, MASTER_CATEGORY_ID.getPath() + ".name"), "category.name")
        );
    }

    @Test(description = "Успешный импорт медиафайла с category_id - Not Found", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"225854"})
    public void successImportImageWithCategoryIdNotFoundTest() {
        String metadataId = importServiceStepDef.successImportBusinessMetadata(MASTER_CATEGORY_ID.getName(), UUID.randomUUID().toString());
        List<InvalidParamsItem> invalidParamsItems = importServiceStepDef.invalidImportRandomImageWithBusinessMetadata(new ImageData(ImageFormat.JPEG), metadataId);
        assertAll(
                () -> compareParameters(1, invalidParamsItems.size(), "size"),
                () -> compareParameters(MASTER_CATEGORY_ID.getName(), invalidParamsItems.get(0).name(), "name"),
                () -> compareParameters(getData().errorNotFound(), invalidParamsItems.get(0).type(), "type"),
                () -> compareParameters(getData().errorNotFoundReason(), invalidParamsItems.get(0).reason(), "reason")
        );
    }

    @Test(description = "Неуспешный импорт медиафайла с category_id - Invalid Type", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"225862"})
    public void unsuccessfulImportImageWithCategoryIdTest() {
        List<InvalidParamsItem> invalidParamsItems = importServiceStepDef.unsuccessfulImportBusinessMetadata(MASTER_CATEGORY_ID.getName(), UUID.randomUUID().toString().substring(5));
        assertAll(
                () -> compareParameters(1, invalidParamsItems.size(), "size"),
                () -> compareParameters(MASTER_CATEGORY_ID.getName(), invalidParamsItems.get(0).name(), "name"),
                () -> compareParameters(getData().errorInvalidType(), invalidParamsItems.get(0).type(), "type"),
                () -> compareParameters(getData().errorInvalidTypeReason(), invalidParamsItems.get(0).reason(), "reason")
        );
    }


    @Test(description = "Успешный импорт медиафайла с source_id", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"225844"})
    public void successImportImageWithSourceIdTest() {
        DictionariesItem source = metadataStepDef.getListSourcesMetadata().get(0);
        String metadataId = importServiceStepDef.successImportBusinessMetadata(SOURCE_ID.getName(), source.id());
        String imageId = importServiceStepDef.importRandomImageWithBusinessMetadata(new ImageData(ImageFormat.JPEG), metadataId);
        Response response = metadataStepDef.checkMetadata(imageId);
        assertAll(
                () -> compareParameters(source.id(), getValueFromResponse(response, SOURCE_ID.getPath() + ".id"), "source.id"),
                () -> compareParameters(source.name(), getValueFromResponse(response, SOURCE_ID.getPath() + ".name"), "source.name")
        );
    }

    @Test(description = "Успешный импорт медиафайла с source_id - Not Found", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"225848"})
    public void successImportImageWithSourceIdNotFoundTest() {
        String metadataId = importServiceStepDef.successImportBusinessMetadata(SOURCE_ID.getName(), UUID.randomUUID().toString());
        List<InvalidParamsItem> invalidParamsItems = importServiceStepDef.invalidImportRandomImageWithBusinessMetadata(new ImageData(ImageFormat.JPEG), metadataId);
        assertAll(
                () -> compareParameters(1, invalidParamsItems.size(), "size"),
                () -> compareParameters(SOURCE_ID.getName(), invalidParamsItems.get(0).name(), "name"),
                () -> compareParameters(getData().errorNotFound(), invalidParamsItems.get(0).type(), "type"),
                () -> compareParameters(getData().errorNotFoundReason(), invalidParamsItems.get(0).reason(), "reason")
        );
    }

    @Test(description = "Неуспешный импорт медиафайла с source_id - Invalid Type", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"225863"})
    public void unsuccessfulImportImageWithSourceIdTest() {
        List<InvalidParamsItem> invalidParamsItems = importServiceStepDef.unsuccessfulImportBusinessMetadata(SOURCE_ID.getName(), RandomStringUtils.randomAlphanumeric(5));
        assertAll(
                () -> compareParameters(1, invalidParamsItems.size(), "size"),
                () -> compareParameters(SOURCE_ID.getName(), invalidParamsItems.get(0).name(), "name"),
                () -> compareParameters(getData().errorInvalidType(), invalidParamsItems.get(0).type(), "type"),
                () -> compareParameters(getData().errorInvalidTypeReason(), invalidParamsItems.get(0).reason(), "reason")
        );
    }

    //todo тест устарел
    //@Test(description = "Успешный импорт медиафайла с received", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"225855"})
    public void successImportImageWithReceivedTest() {
        String received = RandomStringUtils.randomAlphabetic(7);
        String metadataId = importServiceStepDef.successImportBusinessMetadata(RECEIVED.getName(), received);
        String imageId = importServiceStepDef.importRandomImageWithBusinessMetadata(new ImageData(ImageFormat.JPEG), metadataId);
        compareParameters(received, getValueFromResponse(metadataStepDef.checkMetadata(imageId), RECEIVED.getPath()), "received");
    }

    //todo тест устарел
    //@Test(description = "Успешный импорт медиафайла с received - пустое значение", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"225846"})
    public void successImportImageWithEmptyReceivedTest() {
        String metadataId = importServiceStepDef.successImportBusinessMetadata(RECEIVED.getName(), EMPTY_VALUE);
        String imageId = importServiceStepDef.importRandomImageWithBusinessMetadata(new ImageData(ImageFormat.JPEG), metadataId);
        mustBeNullParameter(getValueFromResponse(metadataStepDef.checkMetadata(imageId), RECEIVED.getPath()), "received");
    }

    @Test(description = "Успешный импорт медиафайла с keywords - одно значение", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"225853"})
    public void successImportImageWithKeywordTest() {
        List<String> keywords = Collections.singletonList(RandomStringUtils.randomAlphabetic(7));
        String metadataId = importServiceStepDef.successImportBusinessMetadata(KEYWORDS.getName(), keywords);
        String imageId = importServiceStepDef.importRandomImageWithBusinessMetadata(new ImageData(ImageFormat.JPEG), metadataId);
        compareParameters(keywords, getValueFromResponse(metadataStepDef.checkMetadata(imageId), KEYWORDS.getPath()), "keywords");
    }

    @Test(description = "Успешный импорт медиафайла с keywords - несколько значение", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"225849"})
    public void successImportImageWithKeywordsTest() {
        List<String> keywords = Arrays.asList(
                RandomStringUtils.randomAlphabetic(7),
                RandomStringUtils.randomAlphabetic(12),
                RandomStringUtils.randomAlphabetic(10)
        );
        String metadataId = importServiceStepDef.successImportBusinessMetadata(KEYWORDS.getName(), keywords);
        String imageId = importServiceStepDef.importRandomImageWithBusinessMetadata(new ImageData(ImageFormat.JPEG), metadataId);
        compareParameters(keywords, getValueFromResponse(metadataStepDef.checkMetadata(imageId), KEYWORDS.getPath()), "keywords");
    }

    @Test(description = "Успешный импорт медиафайла с keywords - пустое значение", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"225842"})
    public void successImportImageWithEmptyKeywordsTest() {
        String metadataId = importServiceStepDef.successImportBusinessMetadata(KEYWORDS.getName(), Collections.emptyList());
        String imageId = importServiceStepDef.importRandomImageWithBusinessMetadata(new ImageData(ImageFormat.JPEG), metadataId);
        mustBeNullParameter(getValueFromResponse(metadataStepDef.checkMetadata(imageId), KEYWORDS.getPath()), "keywords");
    }

    @Test(description = "Успешный импорт медиафайла с own_trademark", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"225843"})
    public void successImportImageWithIsOwnTrademarkTest() {
        String metadataId = importServiceStepDef.successImportBusinessMetadata(OWN_TRADEMARK.getName(), true);
        String imageId = importServiceStepDef.importRandomImageWithBusinessMetadata(new ImageData(ImageFormat.JPEG), metadataId);
        equalsTrueParameter(getValueFromResponse(metadataStepDef.checkMetadata(imageId), IS_OWN_TRADEMARK.getPath()), "is_own_trademark");
    }

    @Test(description = "Неуспешный импорт медиафайла с own_trademark - Invalid Type", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"225859"})
    public void unsuccessfulImportImageWithIsOwnTrademarkTest() {
        List<InvalidParamsItem> invalidParamsItems = importServiceStepDef.unsuccessfulImportBusinessMetadata(OWN_TRADEMARK.getName(), RandomStringUtils.randomAlphanumeric(5));
        assertAll(
                () -> compareParameters(1, invalidParamsItems.size(), "size"),
                () -> compareParameters(OWN_TRADEMARK.getName(), invalidParamsItems.get(0).name(), "name"),
                () -> compareParameters(getData().errorInvalidType(), invalidParamsItems.get(0).type(), "type"),
                () -> compareParameters(getData().errorInvalidTypeReason(), invalidParamsItems.get(0).reason(), "reason")
        );
    }

    //todo тест устарел
    //@Test(description = "Успешный импорт медиафайла с raw_image", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"225852"})
    public void successImportImageWithIsRawImageTest() {
        String metadataId = importServiceStepDef.successImportBusinessMetadata(RAW_IMAGE.getName(), false);
        String imageId = importServiceStepDef.importRandomImageWithBusinessMetadata(new ImageData(ImageFormat.JPEG), metadataId);
        equalsFalseParameter(getValueFromResponse(metadataStepDef.checkMetadata(imageId), IS_RAW_IMAGE.getPath()), "is_raw_image");
    }

    //todo тест устарел
    //@Test(description = "Неуспешный импорт медиафайла с is_raw_image - Invalid Type", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"225857"})
    public void unsuccessfulImportImageWithIsRawImageTest() {
        List<InvalidParamsItem> invalidParamsItems = importServiceStepDef.unsuccessfulImportBusinessMetadata(RAW_IMAGE.getName(), RandomStringUtils.randomAlphabetic(4));
        assertAll(
                () -> compareParameters(1, invalidParamsItems.size(), "size"),
                () -> compareParameters(RAW_IMAGE.getName(), invalidParamsItems.get(0).name(), "name"),
                () -> compareParameters(getData().errorInvalidType(), invalidParamsItems.get(0).type(), "type"),
                () -> compareParameters(getData().errorInvalidTypeReason(), invalidParamsItems.get(0).reason(), "reason")
        );
    }

    @Test(description = "Успешный импорт медиафайла с is_copyright", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"225847"})
    public void successImportImageWithIsCopyrightTest() {
        String metadataId = importServiceStepDef.successImportBusinessMetadata(COPYRIGHT.getName(), true);
        String imageId = importServiceStepDef.importRandomImageWithBusinessMetadata(new ImageData(ImageFormat.JPEG), metadataId);
        equalsTrueParameter(getValueFromResponse(metadataStepDef.checkMetadata(imageId), IS_COPYRIGHT.getPath()), "is_copyright");
    }

    @Test(description = "Неуспешный импорт медиафайла с is_copyright - Invalid Type", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"225858"})
    public void unsuccessfulImportImageWithIsCopyrightTest() {
        List<InvalidParamsItem> invalidParamsItems =
                importServiceStepDef.unsuccessfulImportBusinessMetadata(COPYRIGHT.getName(), Integer.parseInt(RandomStringUtils.randomNumeric(4)));
        assertAll(
                () -> compareParameters(1, invalidParamsItems.size(), "size"),
                () -> compareParameters(COPYRIGHT.getName(), invalidParamsItems.get(0).name(), "name"),
                () -> compareParameters(getData().errorInvalidType(), invalidParamsItems.get(0).type(), "type"),
                () -> compareParameters(getData().errorInvalidTypeReason(), invalidParamsItems.get(0).reason(), "reason")
        );
    }

    @Test(description = "Успешный импорт медиафайла с external_task_id", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"283040"})
    public void successImportImageWithExternalTaskIdTest() {
        String externalTaskId = 1 + RandomStringUtils.randomNumeric(4);
        String metadataId = importServiceStepDef.successImportBusinessMetadata(EXTERNAL_TASK_ID.getName(), externalTaskId);
        String imageId = importServiceStepDef.importRandomImageWithBusinessMetadata(new ImageData(ImageFormat.JPEG), metadataId);
        compareParameters(externalTaskId, getValueFromResponse(metadataStepDef.checkMetadata(imageId), EXTERNAL_TASK_ID.getPath()), "external_task_id");
    }

    @Test(description = "Не успешный импорт медиафайла с external_task_id (строка)", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"283040"})
    public void unsuccessfulImportImageWithExternalTaskIdTest() {
        List<InvalidParamsItem> invalidParamsItems = importServiceStepDef.unsuccessfulImportBusinessMetadata(EXTERNAL_TASK_ID.getName(), RandomStringUtils.randomAlphabetic(5));
        assertAll(
                () -> compareParameters(1, invalidParamsItems.size(), "size"),
                () -> compareParameters(EXTERNAL_TASK_ID.getName(), invalidParamsItems.get(0).name(), "name"),
                () -> compareParameters(getData().errorInvalidType(), invalidParamsItems.get(0).type(), "type"),
                () -> compareParameters(getData().errorInvalidTypeReason(), invalidParamsItems.get(0).reason(), "reason")
        );
    }

    @Test(description = "Успешный импорт медиафайла со всеми параметрами", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"225851"})
    public void successImportImageAllMetadataTest() {
        DictionariesItem category = metadataStepDef.getListCategoriesMetadata().get(0);
        DictionariesItem source = metadataStepDef.getListSourcesMetadata().get(0);
        List<String> keywords = Arrays.asList(
                RandomStringUtils.randomAlphabetic(17),
                RandomStringUtils.randomNumeric(8),
                RandomStringUtils.randomAlphabetic(9)
        );
        String externalTaskId = 1 + RandomStringUtils.randomNumeric(3);

        Map<String, Object> metadata = new HashMap<>() {{
            put(MASTER_CATEGORY_ID.getName(), category.id());
            put(SOURCE_ID.getName(), source.id());
            put(KEYWORDS.getName(), keywords);
            put(OWN_TRADEMARK.getName(), true);
            put(COPYRIGHT.getName(), true);
            put(EXTERNAL_TASK_ID.getName(), externalTaskId);
        }};


        String metadataId = importServiceStepDef.successImportBusinessMetadata(Collections.singletonMap("data", metadata));
        String imageId = importServiceStepDef.importRandomImageWithBusinessMetadata(new ImageData(ImageFormat.JPEG), metadataId);
        Response response = metadataStepDef.checkMetadata(imageId);
        assertAll(
                () -> compareParameters(category.id(), getValueFromResponse(response, MASTER_CATEGORY_ID.getPath() + ".id"), "category.id"),
                () -> compareParameters(category.name(), getValueFromResponse(response, MASTER_CATEGORY_ID.getPath() + ".name"), "category.name"),
                () -> compareParameters(source.id(), getValueFromResponse(response, SOURCE_ID.getPath() + ".id"), "source.id"),
                () -> compareParameters(source.name(), getValueFromResponse(response, SOURCE_ID.getPath() + ".name"), "source.name"),
                () -> compareParameters(keywords, getValueFromResponse(response, KEYWORDS.getPath()), "keywords"),
                () -> compareParameters(externalTaskId, getValueFromResponse(response, EXTERNAL_TASK_ID.getPath()), "external_task_id"),
                () -> equalsTrueParameter(getValueFromResponse(response, IS_OWN_TRADEMARK.getPath()), "is_own_trademark"),
                () -> equalsTrueParameter(getValueFromResponse(response, IS_COPYRIGHT.getPath()), "is_copyright")

        );
    }

    @Test(description = "Неуспешный импорт медиафайла с несколькими параметрами", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"225861"})
    public void unsuccessfulImportImageMetadataTest() {
        DictionariesItem source = metadataStepDef.getListSourcesMetadata().get(0);

        Map<String, Object> metadata = new HashMap<>() {{
            put(SOURCE_ID.getName(), source.id() + RandomStringUtils.randomAscii(3));
            put(OWN_TRADEMARK.getName(), RandomStringUtils.randomAscii(10));
            put(COPYRIGHT.getName(), true);
        }};

        List<InvalidParamsItem> expInvalidParamsItems = Arrays.asList(
                new InvalidParamsItem().name(SOURCE_ID.getName()).type(getData().errorInvalidType()).reason(getData().errorInvalidTypeReason()),
                new InvalidParamsItem().name(OWN_TRADEMARK.getName()).type(getData().errorInvalidType()).reason(getData().errorInvalidTypeReason())
        );

        List<InvalidParamsItem> actualInvalidParamsItems = importServiceStepDef.unsuccessfulImportBusinessMetadata(Collections.singletonMap("data", metadata));

        assertAll(
                () -> compareParameters(2, actualInvalidParamsItems.size(), "size"),
                () -> compareParameters(new LinkedList<>(expInvalidParamsItems), new LinkedList<>(actualInvalidParamsItems), "name")
        );
    }

    @Test(description = "Успешное создание бизнес метадаты с пустым телом", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"230061"})
    public void successImportMetadataEmptyBodyTest() {
        String metadataId = importServiceStepDef.successImportBusinessMetadata(Collections.singletonMap("data", Collections.emptyMap()));
        importServiceStepDef.importRandomImageWithBusinessMetadata(new ImageData(ImageFormat.JPEG), metadataId);
    }

    @Test(description = "Неуспешное создание бизнес метадаты со строкой", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"230062"})
    public void unsuccessfulImportMetadataStringTest() {
        List<InvalidParamsItem> actualInvalidParamsItems = importServiceStepDef.unsuccessfulImportBusinessMetadata(RandomStringUtils.randomAlphabetic(7));
        assertAll(
                () -> compareParameters(1, actualInvalidParamsItems.size(), "size"),
                () -> compareParameters(getData().invalidBodyStringParam(), actualInvalidParamsItems.get(0), "ошибка")
        );
    }

    @Test(description = "Неуспешное создание бизнес метадаты с пустой строкой", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"230063"})
    public void unsuccessfulImportMetadataEmptyStringTest() {
        List<InvalidParamsItem> actualInvalidParamsItems = importServiceStepDef.unsuccessfulImportBusinessMetadata(EMPTY_VALUE);
        assertAll(
                () -> compareParameters(1, actualInvalidParamsItems.size(), "size"),
                () -> compareParameters(getData().invalidEmptyBodyStringParam(), actualInvalidParamsItems.get(0), "ошибка")
        );
    }
}
