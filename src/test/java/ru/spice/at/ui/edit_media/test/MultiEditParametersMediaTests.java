package ru.spice.at.ui.edit_media.test;

import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import ru.spice.at.api.dto.response.metadata.DictionariesItem;
import ru.spice.at.api.dto.response.metadata.RetailersItem;
import ru.spice.at.common.emuns.dam.Source;
import ru.spice.at.common.utils.WaitHelper;
import ru.testit.annotations.WorkItemIds;
import io.restassured.response.Response;
import org.apache.commons.lang3.RandomStringUtils;
import org.testng.annotations.*;
import ru.spice.at.api.metadata_service.MetadataStepDef;
import ru.spice.at.common.base_test.BaseUiTest;
import ru.spice.at.common.dto.dam.ImageData;
import ru.spice.at.common.emuns.UiCategories;
import ru.spice.at.common.emuns.dam.ImageFormat;
import ru.spice.at.common.emuns.dam.ImageParameters;
import ru.spice.at.common.listeners.TestAllureListener;
import ru.spice.at.ui.edit_media.EditMediaSettings;
import ru.spice.at.ui.edit_media.EditMediaStepDef;

import java.util.*;
import java.util.stream.Collectors;

import static ru.spice.at.common.constants.TestConstants.FOUR_HUNDRED_NINETY_NINE;
import static ru.spice.at.common.constants.TestConstants.YES;
import static ru.spice.at.common.emuns.dam.ImageParameters.*;
import static ru.spice.at.common.emuns.dam.Quality.BAD;
import static ru.spice.at.common.emuns.dam.Source.BRAND;
import static ru.spice.at.common.emuns.dam.Status.ARCHIVE;
import static ru.spice.at.common.emuns.dam.Status.IN_PROGRESS;
import static ru.spice.at.common.utils.Assert.*;
import static ru.spice.at.common.utils.JsonHelper.getValueFromResponse;

@Feature("Edit media")
@Story("Success parameters multi edit")
@Listeners({TestAllureListener.class})
public class MultiEditParametersMediaTests extends BaseUiTest<EditMediaSettings> {
    private final static int COUNT_METADATA = 3;
    private List<EditMediaStepDef.Image> images;
    private EditMediaStepDef editMediaStepDef;
    private MetadataStepDef metadataStepDef;

    protected MultiEditParametersMediaTests() {
        super(UiCategories.EDIT_MEDIA);
    }

    @BeforeClass(alwaysRun = true)
    public void beforeClass() {
        editMediaStepDef = new EditMediaStepDef(getWebDriver());
        metadataStepDef = new MetadataStepDef(editMediaStepDef.getAuthToken());
    }

    @BeforeMethod(alwaysRun = true)
    public void beforeMethod() {
        images = new ArrayList<>();
        for (int i = 0; i < COUNT_METADATA; i++) {
            ImageData imageData = new ImageData(ImageFormat.JPEG);
            String id = metadataStepDef.createMetadataImage(imageData);
            images.add(new EditMediaStepDef.Image(imageData.getFilename(), id));
        }

        editMediaStepDef.goToBaseUrlWithLogin(getWebDriver());
    }

    @AfterMethod(alwaysRun = true)
    public void afterMethod() {
        metadataStepDef.deleteMetadata();
    }

    @Test(description = "Успешное массовое редактирование поля 'Статус'", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"248161"})
    public void successMultiEditStatusTest() {
        images.remove(0);
        editMediaStepDef.goToMultiEditLink(images, false);

        String status = ARCHIVE.getName();
        editMediaStepDef.multiEditImageParameters(STATUS_NAME, status);

        images.forEach(image ->
                WaitHelper.withRetriesAsserted(
                        () -> {
                            Response response = metadataStepDef.checkMetadata(image.id());
                            compareParameters(status, getValueFromResponse(response, STATUS_NAME.getPath()), "status_name");
                        })
        );
    }

    @Test(description = "Успешное массовое редактирование поля 'Качество'", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"248162"})
    public void successMultiEditQualityTest() {
        images.remove(0);
        editMediaStepDef.goToMultiEditLink(images, true);

        String quality = BAD.getName();
        editMediaStepDef.multiEditImageParameters(QUALITY_NAME, quality);

        images.forEach(image ->
                WaitHelper.withRetriesAsserted(
                        () -> {
                            Response response = metadataStepDef.checkMetadata(image.id());
                            compareParameters(quality, getValueFromResponse(response, QUALITY_NAME.getPath()), "quality_name");
                        })
        );
    }

    @Test(description = "Успешное массовое редактирование поля 'Исполнитель'", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"248163"})
    public void successMultiEditAssigneeTest() {
        images.remove(0);
        editMediaStepDef.goToMultiEditLink(images, true);

        editMediaStepDef.multiEditImageParameters(ASSIGNEE_ID, getData().assigneeName());

        images.forEach(image ->
                WaitHelper.withRetriesAsserted(
                        () -> {
                            Response response = metadataStepDef.checkMetadata(image.id());
                            compareParameters(
                                    getData().assigneeName(),
                                    getValueFromResponse(response, ASSIGNEE_ID.getPath() + ".full_name"), "assignee_id");
                        })
        );
    }

    @Test(description = "Успешное массовое редактирование поля 'Исполнитель' = null", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"248163"})
    public void successMultiEditAssigneeNullTest() {
        images.remove(0);
        editMediaStepDef.goToMultiEditLink(images, true);

        String expAssigneeId = metadataStepDef.getListUsersMetadata().get(0).id();
        metadataStepDef.successMultiEditMetadata(
                images.stream().map(EditMediaStepDef.Image::id).collect(Collectors.toList()), ASSIGNEE_ID.getName(), expAssigneeId);

        editMediaStepDef.multiEditImageParameters(ASSIGNEE_ID, null);

        images.forEach(image ->
                WaitHelper.withRetriesAsserted(
                        () -> {
                            Response response = metadataStepDef.checkMetadata(image.id());
                            mustBeNullParameter(getValueFromResponse(response, ASSIGNEE_ID.getPath()), "assignee_id");
                        })
        );
    }

    @Test(description = "Успешное массовое редактирование поля 'SKU'", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"248159"})
    public void successMultiEditSkuTest() {
        images.remove(0);
        editMediaStepDef.goToMultiEditLink(images, false);

        String sku = RandomStringUtils.randomAlphabetic(5);
        editMediaStepDef.multiEditImageParameters(SKU, sku);

        images.forEach(image ->
                WaitHelper.withRetriesAsserted(
                        () -> {
                            Response response = metadataStepDef.checkMetadata(image.id());
                            compareParameters(sku, getValueFromResponse(response, SKU.getPath()), "sku");
                        })
        );
    }

    @Test(description = "Успешное массовое редактирование поля 'SKU' = null", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"248159"})
    public void successMultiEditSkuNullTest() {
        images.remove(0);
        editMediaStepDef.goToMultiEditLink(images, false);

        String sku = RandomStringUtils.randomAlphabetic(5);
        metadataStepDef.successMultiEditMetadata(
                images.stream().map(EditMediaStepDef.Image::id).collect(Collectors.toList()), SKU.getName(), sku);

        editMediaStepDef.multiEditImageParameters(SKU, null);

        images.forEach(image ->
                WaitHelper.withRetriesAsserted(
                        () -> {
                            Response response = metadataStepDef.checkMetadata(image.id());
                            mustBeNullParameter(getValueFromResponse(response, SKU.getPath()), "sku");
                        })
        );
    }

    @Test(description = "Успешное массовое редактирование поля 'Приоритет'", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"293217"})
    public void successMultiEditPriorityTest() {
        images.remove(0);
        editMediaStepDef.goToMultiEditLink(images, false);

        int priority = new Random().nextInt(FOUR_HUNDRED_NINETY_NINE) + 1;
        editMediaStepDef.multiEditImageParameters(PRIORITY, String.valueOf(priority));

        images.forEach(image ->
                WaitHelper.withRetriesAsserted(
                        () -> {
                            Response response = metadataStepDef.checkMetadata(image.id());
                            compareParameters(priority, getValueFromResponse(response, PRIORITY.getPath()), "priority");
                        })
        );
    }

    @Test(description = "Успешное массовое редактирование поля 'Приоритет' = null", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"293217"})
    public void successMultiEditPriorityNullTest() {
        images.remove(0);
        editMediaStepDef.goToMultiEditLink(images, false);

        int priority = new Random().nextInt(FOUR_HUNDRED_NINETY_NINE) + 1;
        metadataStepDef.successMultiEditMetadata(
                images.stream().map(EditMediaStepDef.Image::id).collect(Collectors.toList()), PRIORITY.getName(), String.valueOf(priority));

        editMediaStepDef.multiEditImageParameters(PRIORITY, null);

        images.forEach(image ->
                WaitHelper.withRetriesAsserted(
                        () -> {
                            Response response = metadataStepDef.checkMetadata(image.id());
                            mustBeNullParameter(getValueFromResponse(response, PRIORITY.getPath()), "priority");
                        })
        );
    }

    @Test(description = "Успешное массовое редактирование поля 'Теги'", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"248165"})
    public void successMultiEditKeywordsTest() {
        images.remove(0);
        editMediaStepDef.goToMultiEditLink(images, true);

        String keyword = RandomStringUtils.randomAlphabetic(8);
        editMediaStepDef.multiEditImageParameters(KEYWORDS, keyword);

        images.forEach(image ->
                WaitHelper.withRetriesAsserted(
                        () -> {
                            Response response = metadataStepDef.checkMetadata(image.id());
                            compareParameters(Collections.singletonList(keyword), getValueFromResponse(response, KEYWORDS.getPath()), "keyword");
                        })
        );
    }

    @Test(description = "Успешное массовое редактирование поля 'Теги' = null", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"248165"})
    public void successMultiEditKeywordsNullTest() {
        images.remove(0);
        editMediaStepDef.goToMultiEditLink(images, true);

        metadataStepDef.successMultiEditMetadata(
                images.stream().map(EditMediaStepDef.Image::id).collect(Collectors.toList()),
                KEYWORDS.getName(), Collections.singletonList(RandomStringUtils.randomAlphabetic(7)));

        editMediaStepDef.multiEditImageParameters(KEYWORDS, null);

        images.forEach(image ->
                WaitHelper.withRetriesAsserted(
                        () -> {
                            Response response = metadataStepDef.checkMetadata(image.id());
                            mustBeNullParameter(getValueFromResponse(response, KEYWORDS.getPath()), "keyword");
                        })
        );
    }

    @Test(description = "Успешное массовое редактирование поля 'Источник'", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"248160"})
    public void successMultiEditSourceTest() {
        images.remove(0);
        editMediaStepDef.goToMultiEditLink(images, false);

        editMediaStepDef.multiEditImageParameters(SOURCE, BRAND.getName());

        images.forEach(image ->
                WaitHelper.withRetriesAsserted(
                        () -> {
                            Response response = metadataStepDef.checkMetadata(image.id());
                            compareParameters(BRAND.getName(), getValueFromResponse(response, SOURCE_NAME.getPath()), "source_name");
                        })
        );
    }

    @Test(description = "Успешное массовое редактирование поля 'Источник' = null", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"248160"})
    public void successMultiEditSourceNullTest() {
        images.remove(0);
        editMediaStepDef.goToMultiEditLink(images, false);

        String expSourceId = metadataStepDef.getListSourcesMetadata().stream().filter(item -> item.name().equals(Source.BRAND.getName())).
                map(DictionariesItem::id).findFirst().orElse(null);

        metadataStepDef.successMultiEditMetadata(
                images.stream().map(EditMediaStepDef.Image::id).collect(Collectors.toList()), SOURCE_ID.getName(), expSourceId);

        editMediaStepDef.multiEditImageParameters(SOURCE, null);

        images.forEach(image ->
                WaitHelper.withRetriesAsserted(
                        () -> {
                            Response response = metadataStepDef.checkMetadata(image.id());
                            mustBeNullParameter(getValueFromResponse(response, SOURCE.getPath()), "source");
                        })
        );
    }

    @Test(description = "Успешное массовое редактирование поля 'Задача PIMS'", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"248157"})
    public void successMultiEditExternalTaskIdTest() {
        images.remove(0);
        editMediaStepDef.goToMultiEditLink(images, false);

        String externalTaskId = String.valueOf(1000 + Integer.parseInt(RandomStringUtils.randomNumeric(3)));
        editMediaStepDef.multiEditImageParameters(EXTERNAL_TASK_ID, externalTaskId);

        images.forEach(image ->
                WaitHelper.withRetriesAsserted(
                        () -> {
                            Response response = metadataStepDef.checkMetadata(image.id());
                            compareParameters(externalTaskId, getValueFromResponse(response, EXTERNAL_TASK_ID.getPath()), "external_task_id");
                        })
        );
    }

    @Test(description = "Успешное массовое редактирование поля 'Задача PIMS' = null", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"248157"})
    public void successMultiEditExternalTaskIdNullTest() {
        images.remove(0);
        editMediaStepDef.goToMultiEditLink(images, false);

        String externalTaskId = String.valueOf(1000 + Integer.parseInt(RandomStringUtils.randomNumeric(3)));
        metadataStepDef.successMultiEditMetadata(
                images.stream().map(EditMediaStepDef.Image::id).collect(Collectors.toList()), EXTERNAL_TASK_ID.getName(), externalTaskId);

        editMediaStepDef.multiEditImageParameters(EXTERNAL_TASK_ID, null);

        images.forEach(image ->
                WaitHelper.withRetriesAsserted(
                        () -> {
                            Response response = metadataStepDef.checkMetadata(image.id());
                            mustBeNullParameter(getValueFromResponse(response, EXTERNAL_TASK_ID.getPath()), "external_task_id");
                        })
        );
    }

    @Test(description = "Успешное массовое редактирование поля 'Ритейлер'", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"248158"})
    public void successMultiEditMasterSellerTest() {
        images.remove(0);
        editMediaStepDef.goToMultiEditLink(images, false);

        editMediaStepDef.multiEditImageParameters(MASTER_SELLER, getData().masterSellerName());

        images.forEach(image ->
                WaitHelper.withRetriesAsserted(
                        () -> {
                            Response response = metadataStepDef.checkMetadata(image.id());
                            compareParameters(
                                    getData().masterSellerName(),
                                    getValueFromResponse(response, MASTER_SELLER.getPath() + ".name"), "master_seller");
                        })
        );
    }

    @Test(description = "Успешное массовое редактирование поля 'Ритейлер' = null", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"248158"})
    public void successMultiEditMasterSellerNullTest() {
        images.remove(0);
        editMediaStepDef.goToMultiEditLink(images, false);

        RetailersItem retailer = metadataStepDef.getListRetailersMetadata().data().stream().findFirst().orElse(null);
        metadataStepDef.successMultiEditMetadata(
                images.stream().map(EditMediaStepDef.Image::id).collect(Collectors.toList()), MASTER_SELLER_ID.getName(), retailer.id());

        editMediaStepDef.multiEditImageParameters(MASTER_SELLER, null);

        images.forEach(image ->
                WaitHelper.withRetriesAsserted(
                        () -> {
                            Response response = metadataStepDef.checkMetadata(image.id());
                            mustBeNullParameter(getValueFromResponse(response, MASTER_SELLER.getPath()), "master_seller");
                        })
        );
    }

    @Test(description = "Успешное массовое редактирование поля 'Категория'", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"248164"})
    public void successMultiEditMasterCategoryTest() {
        images.remove(0);
        editMediaStepDef.goToMultiEditLink(images, true);

        editMediaStepDef.multiEditImageParameters(MASTER_CATEGORY, getData().categoryName());

        images.forEach(image ->
                WaitHelper.withRetriesAsserted(
                        () -> {
                            Response response = metadataStepDef.checkMetadata(image.id());
                            compareParameters(
                                    getData().categoryName(),
                                    getValueFromResponse(response, MASTER_CATEGORY.getPath() + ".name"), "master_seller");
                        })
        );
    }

    @Test(description = "Успешное массовое редактирование поля 'Категория' = null", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"248164"})
    public void successMultiEditMasterCategoryNullTest() {
        images.remove(0);
        editMediaStepDef.goToMultiEditLink(images, true);

        String expCategoryId = metadataStepDef.getListCategoriesMetadata().get(0).id();
        metadataStepDef.successMultiEditMetadata(
                images.stream().map(EditMediaStepDef.Image::id).collect(Collectors.toList()), MASTER_CATEGORY_ID.getName(), expCategoryId);

        editMediaStepDef.multiEditImageParameters(MASTER_CATEGORY, null);

        images.forEach(image ->
                WaitHelper.withRetriesAsserted(
                        () -> {
                            Response response = metadataStepDef.checkMetadata(image.id());
                            mustBeNullParameter(
                                    getValueFromResponse(response, MASTER_CATEGORY.getPath()), "master_category");
                        })
        );
    }

    @Test(description = "Успешное массовое редактирование поля 'СТМ'", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"248169"})
    public void successMultiEditIsOwnTrademarkTest() {
        images.remove(0);
        editMediaStepDef.goToMultiEditLink(images, true);

        editMediaStepDef.multiEditImageParameters(IS_OWN_TRADEMARK, YES);

        images.forEach(image ->
                WaitHelper.withRetriesAsserted(
                        () -> {
                            Response response = metadataStepDef.checkMetadata(image.id());
                            equalsTrueParameter(
                                    getValueFromResponse(response, IS_OWN_TRADEMARK.getPath()), "is_own_trademark");
                        })
        );
    }

    @Test(description = "Успешное массовое редактирование поля 'СТМ' = null", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"248169"})
    public void successMultiEditIsOwnTrademarkNullTest() {
        images.remove(0);
        editMediaStepDef.goToMultiEditLink(images, true);

        metadataStepDef.successMultiEditMetadata(
                images.stream().map(EditMediaStepDef.Image::id).collect(Collectors.toList()), IS_OWN_TRADEMARK.getName(), true);

        editMediaStepDef.multiEditImageParameters(IS_OWN_TRADEMARK, null);

        images.forEach(image ->
                WaitHelper.withRetriesAsserted(
                        () -> {
                            Response response = metadataStepDef.checkMetadata(image.id());
                            mustBeNullParameter(
                                    getValueFromResponse(response, IS_OWN_TRADEMARK.getPath()), "is_own_trademark");
                        })
        );
    }

    @Test(description = "Успешное массовое редактирование поля 'Автор. права'", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"248170"})
    public void successMultiEditIsCopyrightTest() {
        images.remove(0);
        editMediaStepDef.goToMultiEditLink(images, true);

        editMediaStepDef.multiEditImageParameters(IS_COPYRIGHT, YES);

        images.forEach(image ->
                WaitHelper.withRetriesAsserted(
                        () -> {
                            Response response = metadataStepDef.checkMetadata(image.id());
                            equalsTrueParameter(
                                    getValueFromResponse(response, IS_COPYRIGHT.getPath()), "is_copyright");
                        })
        );
    }

    @Test(description = "Успешное массовое редактирование поля 'Автор. права' = null", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"248170"})
    public void successMultiEditIsCopyrightNullTest() {
        images.remove(0);
        editMediaStepDef.goToMultiEditLink(images, true);

        metadataStepDef.successMultiEditMetadata(
                images.stream().map(EditMediaStepDef.Image::id).collect(Collectors.toList()), IS_COPYRIGHT.getName(), true);

        editMediaStepDef.multiEditImageParameters(IS_COPYRIGHT, null);

        images.forEach(image ->
                WaitHelper.withRetriesAsserted(
                        () -> {
                            Response response = metadataStepDef.checkMetadata(image.id());
                            mustBeNullParameter(
                                    getValueFromResponse(response, IS_COPYRIGHT.getPath()), "is_copyright");
                        })
        );
    }

    @Test(description = "Успешное массовое редактирование - несколько полей", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"248171"})
    public void successMultiEditSomeParametersTest() {
        images.remove(0);
        editMediaStepDef.goToMultiEditLink(images, false);

        String externalTaskId = RandomStringUtils.randomNumeric(7);
        String sku = RandomStringUtils.randomAlphanumeric(3);
        Map<ImageParameters, String> metadata = new TreeMap<>() {{
            put(EXTERNAL_TASK_ID, externalTaskId);
            put(SKU, sku);
            put(STATUS_NAME, IN_PROGRESS.getName());
            put(MASTER_CATEGORY, getData().categoryName());
        }};

        editMediaStepDef.multiEditImageParameters(metadata);

        images.forEach(image ->
                WaitHelper.withRetriesAsserted(
                        () -> {
                            Response response = metadataStepDef.checkMetadata(image.id());
                            compareParameters(externalTaskId, getValueFromResponse(response, EXTERNAL_TASK_ID.getPath()), "external_task_id");
                        })
        );

        images.forEach(image -> {
                    Response response = metadataStepDef.checkMetadata(image.id());
                    assertAll(
                            () -> compareParameters(externalTaskId, getValueFromResponse(response, EXTERNAL_TASK_ID.getPath()), "external_task_id"),
                            () -> compareParameters(sku, getValueFromResponse(response, SKU.getPath()), "sku"),
                            () -> compareParameters(IN_PROGRESS.getName(), getValueFromResponse(response, STATUS_NAME.getPath()), "status"),
                            () -> compareParameters(getData().categoryName(), getValueFromResponse(response, MASTER_CATEGORY.getPath() + ".name"), "master_category")
                    );
                }
        );
    }
}
