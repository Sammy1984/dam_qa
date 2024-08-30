package ru.spice.at.api.metadata_service.test;

import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import org.testng.annotations.BeforeClass;
import ru.testit.annotations.WorkItemIds;
import org.apache.commons.lang3.RandomStringUtils;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import ru.spice.at.api.dto.request.metadata.RequestMultiEditData;
import ru.spice.at.api.dto.response.metadata.*;
import ru.spice.at.api.metadata_service.MetadataSettings;
import ru.spice.at.api.metadata_service.MetadataStepDef;
import ru.spice.at.api.retailer_media_import_service.RetailerMediaImportSettings;
import ru.spice.at.api.retailer_media_import_service.RetailerMediaImportStepDef;
import ru.spice.at.common.base_test.BaseApiTest;
import ru.spice.at.common.dto.dam.ImageData;
import ru.spice.at.common.emuns.ApiServices;
import ru.spice.at.common.emuns.dam.*;
import ru.spice.at.common.utils.Assert;

import java.util.*;
import java.util.stream.Collectors;

import static ru.spice.at.common.constants.TestConstants.*;
import static ru.spice.at.common.emuns.Role.*;
import static ru.spice.at.common.emuns.dam.ImageFormat.JPG;
import static ru.spice.at.common.emuns.dam.ImageParameters.*;
import static ru.spice.at.common.utils.Assert.*;

@Feature("Metadata Service")
@Story("POST metadata selection filtration data")
public class MetadataPostSelectionFiltrationTests extends BaseApiTest<MetadataSettings> {
    private final static int COUNT_METADATA = 4;
    private final MetadataStepDef metadataStepDef;
    private final RetailerMediaImportStepDef retailerMediaImportStepDef;
    private final List<String> idList = new ArrayList<>();
    private final List<ImageData> imageList = new ArrayList<>();

    protected MetadataPostSelectionFiltrationTests() {
        super(ApiServices.METADATA_SERVICE);
        metadataStepDef = new MetadataStepDef();
        retailerMediaImportStepDef = new RetailerMediaImportStepDef();
    }

    @BeforeClass(alwaysRun = true)
    public void beforeClass() {
        metadataStepDef.deleteMetadata();
    }

    @BeforeMethod(alwaysRun = true)
    public void beforeMethod() {
        for (int i = 0; i < COUNT_METADATA; i++) {
            ImageData image = new ImageData(ImageFormat.JPEG);
            idList.add(metadataStepDef.createMetadataImage(image));
            imageList.add(image);
        }
    }

    @AfterMethod(alwaysRun = true)
    public void afterMethod() {
        idList.clear();
        imageList.clear();
        metadataStepDef.deleteMetadata();
    }

    @Test(description = "Выбор результатов фильтрации по массиву skus по одному значению", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"260879"})
    public void successSelectionBySkuTest() {
        String sku = RandomStringUtils.randomAlphabetic(10);
        idList.remove(idList.size() - 1);
        idList.forEach(id -> metadataStepDef.successEditMetadata(id, SKU.getName(), sku));

        List<String> actualIds = metadataStepDef.successMetadataSelection(SKUS.getName(), Collections.singletonList(sku));
        Assert.compareParameters(idList, actualIds, "ids");
    }

    @Test(description = "Выбор результатов фильтрации по массиву skus по нескольким значениям", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"260879"})
    public void successSelectionBySkusTest() {
        List<String> expectedSkus = Arrays.asList(imageList.get(0).getSku(), imageList.get(1).getSku());
        List<String> actualIds = metadataStepDef.successMetadataSelection(SKUS.getName(), expectedSkus);
        Assert.compareParameters(Arrays.asList(idList.get(0), idList.get(1)), actualIds, "ids");
    }

    @Test(description = "Выбор результатов фильтрации по массиву skus - пустое значение", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"260879"})
    public void successSelectionByEmptySkuTest() {
        metadataStepDef.successMetadataSelection(SKUS.getName(), Collections.singletonList(EMPTY_VALUE), true);
    }

    @Test(description = "Выбор результатов фильтрации по массиву skus - несуществующее значение", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"260879"})
    public void successSelectionRandomSkuTest() {
        metadataStepDef.successMetadataSelection(SKUS.getName(), Collections.singletonList(RandomStringUtils.randomAlphabetic(10)), true);
    }

    @Test(description = "Выбор результатов фильтрации по массиву skus - неверный тип", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"262485"})
    public void unsuccessfulSelectionInvalidTypeSkuTest() {
        List<InvalidParamsItem> invalidParams = metadataStepDef.unsuccessfulMetadataSelection(SKUS.getName(), UUID.randomUUID().toString());
        getData().invalidParams().get(2).name(SKUS.getName());
        assertAll(
                () -> compareParameters(1, invalidParams.size(), "invalid_params.size"),
                () -> compareParameters(getData().invalidParams().get(2), invalidParams.get(0), "invalid_params")
        );
    }

    @Test(description = "Выбор результатов фильтрации по полю source_ids по одному значению", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"260880"})
    public void successSelectionBySourceIdTest() {
        String expSourceId = metadataStepDef.getListSourcesMetadata().stream().filter(item -> item.name().equals(Source.BRAND.getName())).map(DictionariesItem::id).findFirst().orElse(null);
        idList.remove(idList.size() - 1);
        RequestMultiEditData requestMultiEditData = RequestMultiEditData.builder().ids(idList).sourceId(expSourceId).build();
        metadataStepDef.successMultiEditMetadata(requestMultiEditData);

        List<String> actualIds = metadataStepDef.successMetadataSelection(SOURCE_IDS.getName(), Collections.singletonList(expSourceId));
        Assert.compareParameters(idList, actualIds, "ids");
    }

    @Test(description = "Выбор результатов фильтрации по полю source_ids - null", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"260880"})
    public void successSelectionByNullSourceIdTest() {
        List<String> actualIds = metadataStepDef.successMetadataSelection(SOURCE_IDS.getName(), null);
        Assert.compareParameters(idList, actualIds, "ids");
    }

    @Test(description = "Выбор результатов фильтрации по полю quality_ids по одному значению", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"260881"})
    public void successSelectionByQualityIdTest() {
        String expQualitiesId = metadataStepDef.getListQualitiesMetadata().stream().filter(item -> item.name().equals(Quality.TO_REVISION.getName())).map(DictionariesItem::id).findFirst().orElse(null);
        idList.remove(idList.size() - 1);
        RequestMultiEditData requestMultiEditData = RequestMultiEditData.builder().ids(idList).qualityId(expQualitiesId).build();
        metadataStepDef.successMultiEditMetadata(requestMultiEditData);

        List<String> actualIds = metadataStepDef.successMetadataSelection(QUALITY_IDS.getName(), Collections.singletonList(expQualitiesId));
        Assert.compareParameters(idList, actualIds, "ids");
    }

    @Test(description = "Выбор результатов фильтрации по полю quality_ids по нескольким значением", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"260881"})
    public void successSelectionByQualityIdsTest() {
        List<String> expQualitiesIds = metadataStepDef.getListQualitiesMetadata().stream().
                filter(item -> item.name().equals(Quality.TO_REVISION.getName()) || item.name().equals(Quality.BAD.getName())).map(DictionariesItem::id).collect(Collectors.toList());

        idList.remove(idList.size() - 1);

        for (int i = 0; i < idList.size(); i++) {
            metadataStepDef.successMultiEditMetadata(
                    RequestMultiEditData.builder().ids(Collections.singletonList(idList.get(i))).qualityId(i % 2 == 0 ? expQualitiesIds.get(0) : expQualitiesIds.get(1)).build());
        }

        List<String> actualIds = metadataStepDef.successMetadataSelection(QUALITY_IDS.getName(), expQualitiesIds);
        Assert.compareParameters(idList, actualIds, "ids");
    }

    @Test(description = "Выбор результатов фильтрации по полю quality_ids - null", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"260881"})
    public void successSelectionByNullQualityIdTest() {
        metadataStepDef.successMetadataSelection(QUALITY_IDS.getName(), null, true);
    }

    @Test(description = "Выбор результатов фильтрации по полю quality_ids - несуществующий id", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"260881"})
    public void successSelectionByRandomQualityIdTest() {
        metadataStepDef.successMetadataSelection(QUALITY_IDS.getName(), Collections.singletonList(UUID.randomUUID().toString()), true);
    }

    @Test(description = "Выбор результатов фильтрации по массиву quality_ids - неверный тип", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"262491"})
    public void unsuccessfulSelectionInvalidTypeQualityIdTest() {
        List<InvalidParamsItem> invalidParams = metadataStepDef.unsuccessfulMetadataSelection(QUALITY_IDS.getName(), UUID.randomUUID().toString());
        getData().invalidParams().get(2).name(QUALITY_IDS.getName());
        assertAll(
                () -> compareParameters(1, invalidParams.size(), "invalid_params.size"),
                () -> compareParameters(getData().invalidParams().get(2), invalidParams.get(0), "invalid_params")
        );
    }

    @Test(description = "Выбор результатов фильтрации по полю master_seller_ids по одному значению", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"260891"})
    public void successSelectionByMasterSellerIdTest() {
        RetailersResponse retailersResponse = metadataStepDef.getListRetailersMetadata();

        String retailerId = retailersResponse.data().stream().map(RetailersItem::id).findFirst().orElse(null);
        notNullOrEmptyParameter(retailerId, "retailerId");

        idList.remove(idList.size() - 1);
        RequestMultiEditData requestMultiEditData = RequestMultiEditData.builder().ids(idList).masterSellerId(retailerId).build();

        metadataStepDef.successMultiEditMetadata(requestMultiEditData);

        List<String> actualIds = metadataStepDef.successMetadataSelection(MASTER_SELLER_IDS.getName(), Collections.singletonList(retailerId));
        Assert.compareParameters(idList, actualIds, "ids");
    }

    @Test(description = "Выбор результатов фильтрации по полю assignee_ids по одному значению", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"260882"})
    public void successSelectionByAssigneeIdTest() {
        String expUsersId = metadataStepDef.getListUsersMetadata().get(0).id();

        idList.remove(idList.size() - 1);
        RequestMultiEditData requestMultiEditData = RequestMultiEditData.builder().ids(idList).assigneeId(expUsersId).build();

        metadataStepDef.successMultiEditMetadata(requestMultiEditData);

        List<String> actualIds = metadataStepDef.successMetadataSelection(ASSIGNEE_IDS.getName(), Collections.singletonList(expUsersId));
        Assert.compareParameters(idList, actualIds, "ids");
    }

    @Test(description = "Выбор результатов фильтрации по полю assignee_ids - null", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"260882"})
    public void successSelectionByNullAssigneeIdTest() {
        List<String> actualIds = metadataStepDef.successMetadataSelection(ASSIGNEE_IDS.getName(), null);
        Assert.compareParameters(idList, actualIds, "ids");
    }

    @Test(description = "Выбор результатов фильтрации по массиву created_by по одному значению", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"260883"})
    public void successSelectionByCreatedByTest() {
        metadataStepDef.setRole(PHOTOPRODUCTION);
        metadataStepDef.createMetadataImage(new ImageData(ImageFormat.JPEG));

        metadataStepDef.setRole(ADMINISTRATOR);
        String expCreatedById = metadataStepDef.getListUsersMetadata().stream().
                filter(user -> user.fullName().equals(ADMINISTRATOR.getFullName())).map(UsersItem::id).findFirst().orElseThrow(RuntimeException::new);

        List<String> actualIds = metadataStepDef.successMetadataSelection(CREATED_BY.getName(), Collections.singletonList(expCreatedById));
        Assert.compareParameters(idList, actualIds, "ids");
    }

    @Test(description = "Выбор результатов фильтрации по полю category_ids по одному значению", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"260884"})
    public void successSelectionByCategoryIdTest() {
        List<DictionariesItem> categories = metadataStepDef.getListCategoriesMetadata();

        String expCategoryId = categories.stream().
                filter(c -> !c.name().equalsIgnoreCase(getData().unknownCategoryName())).
                map(DictionariesItem::id).findFirst().orElse(null);

        idList.remove(idList.size() - 1);
        RequestMultiEditData requestMultiEditData = RequestMultiEditData.builder().ids(idList).masterCategoryId(expCategoryId).build();

        metadataStepDef.successMultiEditMetadata(requestMultiEditData);

        List<String> actualIds = metadataStepDef.successMetadataSelection(MASTER_CATEGORY_IDS.getName(), Collections.singletonList(expCategoryId));
        Assert.compareParameters(idList, actualIds, "ids");
    }

    @Test(description = "Выбор результатов фильтрации по полю category_ids - null", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"260884"})
    public void successSelectionByNullCategoryIdTest() {
        List<String> actualIds = metadataStepDef.successMetadataSelection(MASTER_CATEGORY_IDS.getName(), null);
        Assert.compareParameters(idList, actualIds, "ids");
    }

    @Test(description = "Выбор результатов фильтрации по полю is_own_trademark - true", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"260886"})
    public void successSelectionByIsOwnTrademarkTest() {
        idList.remove(idList.size() - 1);
        RequestMultiEditData requestMultiEditData = RequestMultiEditData.builder().ids(idList).isOwnTrademark(true).build();
        metadataStepDef.successMultiEditMetadata(requestMultiEditData);

        List<String> actualIds = metadataStepDef.successMetadataSelection(IS_OWN_TRADEMARK.getName(), true);
        Assert.compareParameters(idList, actualIds, "ids");
    }

    @Test(description = "Выбор результатов фильтрации по полю is_copyright - true", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"260887"})
    public void successSelectionByIsCopyrightTest() {
        idList.remove(idList.size() - 1);
        RequestMultiEditData requestMultiEditData = RequestMultiEditData.builder().ids(idList).isCopyright(true).build();
        metadataStepDef.successMultiEditMetadata(requestMultiEditData);

        List<String> actualIds = metadataStepDef.successMetadataSelection(IS_COPYRIGHT.getName(), true);
        Assert.compareParameters(idList, actualIds, "ids");
    }

    @Test(description = "Выбор результатов фильтрации по полю is_raw_image - true", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"260888"})
    public void successSelectionByIsRawImageTest() {
        idList.remove(idList.size() - 1);
        idList.forEach(id -> metadataStepDef.successEditMetadata(id, ImageParameters.IS_RAW_IMAGE.getName(), true));

        List<String> actualIds = metadataStepDef.successMetadataSelection(IS_RAW_IMAGE.getName(), true);
        Assert.compareParameters(idList, actualIds, "ids");
    }

    @Test(description = "Выбор результатов фильтрации по массиву priorities по одному значению", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"260889"})
    public void successSelectionByPriorityTest() {
        Integer priority = new Random().nextInt(FOUR_HUNDRED_NINETY_NINE) + 1;
        idList.remove(idList.size() - 1);
        idList.forEach(id -> metadataStepDef.successEditMetadata(id, PRIORITY.getName(), priority));

        List<String> actualIds = metadataStepDef.successMetadataSelection(PRIORITIES.getName(), Collections.singletonList(priority.toString()));
        Assert.compareParameters(idList, actualIds, "ids");
    }

    @Test(description = "Выбор результатов фильтрации по массиву priorities - null", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"260889"})
    public void successSelectionByPriorityEmptyValueTest() {
        String id = metadataStepDef.createMetadataImage(new ImageData(ImageFormat.JPEG));
        metadataStepDef.successEditMetadata(id, PRIORITY.getName(), new Random().nextInt(FOUR_HUNDRED_NINETY_NINE) + 1);

        List<String> actualIds = metadataStepDef.successMetadataSelection(PRIORITIES.getName(), null);
        Assert.compareParameters(idList, actualIds, "ids");
    }

    @Test(description = "Выбор результатов фильтрации по массиву external_task_ids по одному значению", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"260892"})
    public void successSelectionByExternalTaskIdsTest() {
        int externalTaskId = new Random().nextInt(ONE_THOUSAND) + 1;
        idList.remove(idList.size() - 1);
        RequestMultiEditData requestMultiEditData = RequestMultiEditData.builder().ids(idList).externalTaskId(Integer.toString(externalTaskId)).build();
        metadataStepDef.successMultiEditMetadata(requestMultiEditData);

        List<String> actualIds = metadataStepDef.successMetadataSelection(
                EXTERNAL_TASK_IDS.getName(), Collections.singletonList(Integer.toString(externalTaskId)));
        Assert.compareParameters(idList, actualIds, "ids");
    }

    @Test(description = "Выбор результатов фильтрации по массиву external_task_ids - null", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"260892"})
    public void successSelectionByNullExternalTaskIdsTest() {
        List<String> actualIds = metadataStepDef.successMetadataSelection(EXTERNAL_TASK_IDS.getName(), null);
        Assert.compareParameters(idList, actualIds, "ids");
    }

    @Test(description = "Выбор результатов фильтрации по массиву status_ids по одному значению", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"260893"})
    public void successSelectionByStatusIdsTest() {
        String expStatusId = metadataStepDef.getListStatusesMetadata()
                .stream().filter(item -> item.name().equals(Status.DELETE.getName()))
                .map(DictionariesItem::id).findFirst().orElse(null);

        idList.remove(idList.size() - 1);
        RequestMultiEditData requestMultiEditData = RequestMultiEditData.builder().ids(idList).statusId(expStatusId).build();
        metadataStepDef.successMultiEditMetadata(requestMultiEditData);

        List<String> actualIds = metadataStepDef.successMetadataSelection(STATUS_IDS.getName(), Collections.singletonList(expStatusId));
        Assert.compareParameters(idList, actualIds, "ids");
    }

    @Test(description = "Выбор результатов фильтрации по массиву external_offer_ids по одному значению", timeOut = 600000, groups = {"regress", "kafka"})
    @WorkItemIds({"260894"})
    public void successSelectionByExternalOfferIdsTest() {
        String externalOfferId = Integer.toString(new Random().nextInt(1000000) + 1000000);

        RetailerMediaImportSettings.ImportParameters importParameters = getData().importParameters().clone().
                mediaName(RandomStringUtils.randomAlphabetic(10) + DOT_VALUE + JPG.getFormatName()).
                masterSellerId(new Random().nextInt(100) + 1).
                externalOfferId(externalOfferId);
        retailerMediaImportStepDef.sendMediaFile(retailerMediaImportStepDef.buildImportOneMediaRequest(importParameters));

        List<String> actualIds = metadataStepDef.successMetadataSelection(EXTERNAL_OFFER_IDS.getName(), Collections.singletonList(externalOfferId));
        Assert.compareParameters(1, actualIds.size(), "ids");
    }

    @Test(description = "Выбор результатов фильтрации по массиву external_offer_ids - null", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"260894"})
    public void successSelectionByNullExternalOfferIdsTest() {
        List<String> actualIds = metadataStepDef.successMetadataSelection(EXTERNAL_OFFER_IDS.getName(), null);
        Assert.compareParameters(idList, actualIds, "ids");
    }

    @Test(description = "Выбор результатов фильтрации по нескольким полям", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"260890"})
    public void successSelectionBySomeFieldsTest() {
        String expSourceId = metadataStepDef.getListSourcesMetadata().stream().
                filter(item -> item.name().equals(Source.BRAND.getName())).
                map(DictionariesItem::id).findFirst().orElse(null);
        String expCategoryId = metadataStepDef.getListCategoriesMetadata().stream().
                filter(c -> !c.name().equalsIgnoreCase(getData().unknownCategoryName())).
                map(DictionariesItem::id).findFirst().orElse(null);
        String expQualitiesId = metadataStepDef.getListQualitiesMetadata().stream().
                filter(item -> item.name().equals(Quality.TO_REVISION.getName())).
                map(DictionariesItem::id).findFirst().orElse(null);

        idList.remove(idList.size() - 1);
        RequestMultiEditData requestMultiEditData = RequestMultiEditData.builder().
                ids(idList).
                qualityId(expQualitiesId).
                sourceId(expSourceId).
                masterCategoryId(expCategoryId).
                isOwnTrademark(true).
                build();
        metadataStepDef.successMultiEditMetadata(requestMultiEditData);

        Map<String, Object> expQueryParam = new HashMap<>();
        expQueryParam.put(SOURCE_ID.getName(), Collections.singletonList(expSourceId));
        expQueryParam.put(MASTER_CATEGORY_IDS.getName(), Collections.singletonList(expCategoryId));
        expQueryParam.put(QUALITY_IDS.getName(), Collections.singletonList(expQualitiesId));
        expQueryParam.put(IS_OWN_TRADEMARK.getName(), true);

        List<String> actualIds = metadataStepDef.successMetadataSelection(expQueryParam, false);
        Assert.compareParameters(idList, actualIds, "ids");
    }
}
