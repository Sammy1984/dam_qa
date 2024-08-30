package ru.spice.at.api.metadata_service.test;

import io.qameta.allure.Feature;
import io.qameta.allure.Issue;
import io.qameta.allure.Story;
import ru.testit.annotations.WorkItemIds;
import org.apache.commons.lang3.RandomStringUtils;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import ru.spice.at.api.dto.request.metadata.RequestMultiEditData;
import ru.spice.at.api.dto.response.metadata.DictionariesItem;
import ru.spice.at.api.dto.response.metadata.RetailersItem;
import ru.spice.at.api.dto.response.metadata.RetailersResponse;
import ru.spice.at.api.dto.response.metadata.UsersItem;
import ru.spice.at.api.dto.response.metadata.filtration.DataItem;
import ru.spice.at.api.dto.response.metadata.filtration.FiltrationResponse;
import ru.spice.at.api.metadata_service.MetadataSettings;
import ru.spice.at.api.metadata_service.MetadataStepDef;
import ru.spice.at.common.base_test.BaseApiTest;
import ru.spice.at.common.dto.dam.ImageData;
import ru.spice.at.common.emuns.*;
import ru.spice.at.common.emuns.dam.*;
import ru.spice.at.common.utils.Assert;

import java.util.*;
import java.util.stream.Collectors;

import static ru.spice.at.common.constants.TestConstants.*;
import static ru.spice.at.common.emuns.dam.ImageParameters.*;
import static ru.spice.at.common.emuns.dam.SearchParameters.*;
import static ru.spice.at.common.emuns.dam.Sort.*;
import static ru.spice.at.common.utils.Assert.notNullOrEmptyParameter;

@Feature("Metadata Service")
@Story("GET filtration data")
@Deprecated
public class MetadataGetFiltrationTests extends BaseApiTest<MetadataSettings> {
    private final static int COUNT_METADATA = 4;
    private final MetadataStepDef metadataStepDef;
    private final List<String> idList = new ArrayList<>();

    protected MetadataGetFiltrationTests() {
        super(ApiServices.METADATA_SERVICE);
        metadataStepDef = new MetadataStepDef();
    }

    @BeforeMethod(alwaysRun = true)
    public void beforeMethod() {
        for (int i = 0; i < COUNT_METADATA; i++) {
            idList.add(metadataStepDef.createMetadataImage(new ImageData(ImageFormat.JPEG)));
        }
    }

    @AfterMethod(alwaysRun = true)
    public void afterMethod() {
        idList.clear();
        metadataStepDef.deleteMetadata();
    }

    //@Test(description = "Фильтрация по массиву skus по одному значению", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"191880"})
    public void successFiltrationBySkuTest() {
        String expQueryStr = metadataStepDef.checkMetadata(idList.get(0)).then().extract().path(SKU.getPath());

        Map<String, Object> expQueryParam = new HashMap<>();

        expQueryParam.put(ImageParameters.SKUS.getName(), expQueryStr);

        FiltrationResponse filtrationResponse = metadataStepDef.successFiltrationMetadata(expQueryParam, false).extract().as(FiltrationResponse.class);

        List<String> actualListSku = new ArrayList<>();
        for (DataItem dataItem : filtrationResponse.getData()) {
            actualListSku.add(dataItem.getSku());
        }

        Assert.compareParameters(expQueryStr, actualListSku.get(0), "skus");
    }

    //@Test(description = "Фильтрация по массиву skus по нескольким значениям", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"191880"})
    public void successFiltrationBySkuSTest() {
        List<String> skuList = new ArrayList<>();
        idList.remove(idList.size() - 1);
        for (String id : idList) {
            String sku = metadataStepDef.checkMetadata(id).then().extract().path(SKU.getPath());
            skuList.add(sku);
        }

        FiltrationResponse filtrationResponse =
                metadataStepDef.successFiltrationMetadata(Collections.singletonMap(ImageParameters.SKUS.getName(), skuList), false).
                        extract().as(FiltrationResponse.class);

        List<String> actualListSku = new ArrayList<>();
        for (DataItem dataItem : filtrationResponse.getData()) {
            actualListSku.add(dataItem.getSku());
        }

        Assert.compareParameters(skuList, actualListSku, "skus");
    }

    //@Test(description = "Фильтрация по массиву skus - пустое значение", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"191880"})
    public void unsuccessfulFiltrationByEmptySkuTest() {
        Map<String, Object> expQueryParam = new HashMap<>();

        expQueryParam.put(ImageParameters.SKUS.getName(), EMPTY_VALUE);

        FiltrationResponse filtrationResponse = metadataStepDef.successFiltrationMetadata(expQueryParam, false).extract().as(FiltrationResponse.class);

        List<String> actualListSku = new ArrayList<>();
        for (DataItem dataItem : filtrationResponse.getData()) {
            actualListSku.add(dataItem.getSku());
        }

        Assert.equalsFalseParameter(actualListSku.isEmpty(), "skus");
    }

    //@Test(description = "Фильтрация по массиву skus - несуществующее значение", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"191880"})
    public void unsuccessfulFiltrationBySkuTest() {
        String notExist = RandomStringUtils.randomAlphabetic(10);

        Map<String, Object> expQueryParam = new HashMap<>();
        expQueryParam.put(ImageParameters.SKUS.getName(), notExist);

        FiltrationResponse filtrationResponse = metadataStepDef.successFiltrationMetadata(expQueryParam, true).extract().as(FiltrationResponse.class);

        Assert.equalsTrueParameter(filtrationResponse.getData().isEmpty(), "skus");
    }

    //@Test(description = "Фильтрация по полю source_ids по одному значению", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"230067"})
    public void successFiltrationBySourceIdTest() {
        String expSourceId = metadataStepDef.getListSourcesMetadata().stream().filter(item -> item.name().equals(Source.BRAND.getName())).map(DictionariesItem::id).findFirst().orElse(null);

        idList.remove(idList.size() - 1);
        RequestMultiEditData requestMultiEditData = RequestMultiEditData.builder().ids(idList).sourceId(expSourceId).build();

        metadataStepDef.successMultiEditMetadata(requestMultiEditData);

        Map<String, Object> expQueryParam = new HashMap<>();
        expQueryParam.put(ImageParameters.SOURCE_IDS.getName(), expSourceId);

        FiltrationResponse filtrationResponse = metadataStepDef.successFiltrationMetadata(expQueryParam, false).extract().as(FiltrationResponse.class);

        List<String> actualIdList = new ArrayList<>();
        for (DataItem dataItem : filtrationResponse.getData()) {
            actualIdList.add(dataItem.getId());
        }

        Assert.compareParameters(idList, actualIdList, "id");
    }

    //@Test(description = "Фильтрация по полю source_ids по нескольким значениям", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"230067"})
    public void successFiltrationBySourceIdsTest() {
        List<String> sourceIds = metadataStepDef.getListSourcesMetadata().stream().map(DictionariesItem::id).collect(Collectors.toList());

        idList.remove(idList.size() - 1);

        List<String> expSourceIds = new ArrayList<>();
        for (int i = 0; i < idList.size(); i++) {
            String expSourceId = sourceIds.get(i);
            expSourceIds.add(expSourceId);
            metadataStepDef.successMultiEditMetadata(
                    RequestMultiEditData.builder().ids(Collections.singletonList(idList.get(i))).sourceId(expSourceId).build());
        }

        Map<String, Object> expQueryParam = new HashMap<>();
        expQueryParam.put(ImageParameters.SOURCE_IDS.getName(), expSourceIds);

        FiltrationResponse filtrationResponse = metadataStepDef.successFiltrationMetadata(expQueryParam, false).extract().as(FiltrationResponse.class);

        List<String> actualIdList = new ArrayList<>();
        for (DataItem dataItem : filtrationResponse.getData()) {
            actualIdList.add(dataItem.getId());
        }

        Assert.compareParameters(idList, actualIdList, "id");
    }

    //@Test(description = "Фильтрация по полю source_ids = null", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"230067"})
    public void successFiltrationByNullSourceIdTest() {
        Map<String, Object> expQueryParam = new HashMap<>();
        expQueryParam.put(ImageParameters.SOURCE_IDS.getName(), null);

        FiltrationResponse filtrationResponse = metadataStepDef.successFiltrationMetadata(expQueryParam, false).extract().as(FiltrationResponse.class);

        List<String> actualIdList = new ArrayList<>();
        for (DataItem dataItem : filtrationResponse.getData()) {
            actualIdList.add(dataItem.getId());
        }

        Assert.compareParameters(idList, actualIdList, "id");
    }

    //@Test(description = "Фильтрация по полю source_ids = несуществующий id", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"230067"})
    public void unsuccessfulFiltrationBySourceIdTest() {
        Map<String, Object> expQueryParam = new HashMap<>();
        expQueryParam.put(ImageParameters.SOURCE_IDS.getName(), UUID.randomUUID().toString());

        FiltrationResponse filtrationResponse = metadataStepDef.successFiltrationMetadata(expQueryParam, true).extract().as(FiltrationResponse.class);

        List<String> actualIdList = new ArrayList<>();
        for (DataItem dataItem : filtrationResponse.getData()) {
            actualIdList.add(dataItem.getId());
        }

        Assert.equalsTrueParameter(actualIdList.isEmpty(), "id");
    }

    //@Test(description = "Фильтрация по полю quality_ids по одному значению", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"230068"})
    public void successFiltrationByQualityIdTest() {
        String expQualitiesId = metadataStepDef.getListQualitiesMetadata().stream().filter(item -> item.name().equals(Quality.TO_REVISION.getName())).map(DictionariesItem::id).findFirst().orElse(null);

        idList.remove(idList.size() - 1);
        RequestMultiEditData requestMultiEditData = RequestMultiEditData.builder().ids(idList).qualityId(expQualitiesId).build();

        metadataStepDef.successMultiEditMetadata(requestMultiEditData);

        Map<String, Object> expQueryParam = new HashMap<>();
        expQueryParam.put(ImageParameters.QUALITY_IDS.getName(), expQualitiesId);

        FiltrationResponse filtrationResponse = metadataStepDef.successFiltrationMetadata(expQueryParam, false).extract().as(FiltrationResponse.class);

        List<String> actualIdList = new ArrayList<>();
        for (DataItem dataItem : filtrationResponse.getData()) {
            actualIdList.add(dataItem.getId());
        }

        Assert.compareParameters(idList, actualIdList, "id");
    }

    //@Test(description = "Фильтрация по полю quality_ids по нескольким значением", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"230068"})
    public void successFiltrationByQualityIdsTest() {
        List<String> expQualitiesIds = metadataStepDef.getListQualitiesMetadata().stream().
                filter(item -> item.name().equals(Quality.TO_REVISION.getName()) || item.name().equals(Quality.BAD.getName())).map(DictionariesItem::id).collect(Collectors.toList());

        idList.remove(idList.size() - 1);

        for (int i = 0; i < idList.size(); i++) {
            metadataStepDef.successMultiEditMetadata(
                    RequestMultiEditData.builder().ids(Collections.singletonList(idList.get(i))).qualityId(i % 2 == 0 ? expQualitiesIds.get(0) : expQualitiesIds.get(1)).build());
        }

        Map<String, Object> expQueryParam = new HashMap<>();
        expQueryParam.put(ImageParameters.QUALITY_IDS.getName(), expQualitiesIds);

        FiltrationResponse filtrationResponse = metadataStepDef.successFiltrationMetadata(expQueryParam, false).extract().as(FiltrationResponse.class);

        List<String> actualIdList = new ArrayList<>();
        for (DataItem dataItem : filtrationResponse.getData()) {
            actualIdList.add(dataItem.getId());
        }

        Assert.compareParameters(idList, actualIdList, "id");
    }

    //@Test(description = "Фильтрация по полю quality_ids = null", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"230068"})
    public void successFiltrationByNullQualityIdTest() {
        Map<String, Object> expQueryParam = new HashMap<>();
        expQueryParam.put(ImageParameters.QUALITY_IDS.getName(), null);

        FiltrationResponse filtrationResponse = metadataStepDef.successFiltrationMetadata(expQueryParam, false).extract().as(FiltrationResponse.class);

        List<String> actualIdList = new ArrayList<>();
        for (DataItem dataItem : filtrationResponse.getData()) {
            actualIdList.add(dataItem.getId());
        }

        Assert.compareParameters(idList, actualIdList, "id");
    }

    //@Test(description = "Фильтрация по полю quality_ids = несуществующий id", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"230068"})
    public void unsuccessfulFiltrationByQualityIdTest() {
        Map<String, Object> expQueryParam = new HashMap<>();
        expQueryParam.put(ImageParameters.QUALITY_IDS.getName(), UUID.randomUUID().toString());

        FiltrationResponse filtrationResponse = metadataStepDef.successFiltrationMetadata(expQueryParam, true).extract().as(FiltrationResponse.class);

        List<String> actualIdList = new ArrayList<>();
        for (DataItem dataItem : filtrationResponse.getData()) {
            actualIdList.add(dataItem.getId());
        }

        Assert.equalsTrueParameter(actualIdList.isEmpty(), "id");
    }

    //@Test(description = "Фильтрация по полю master_seller_ids по одному значению", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"246939"})
    public void successFiltrationByMasterSellerIdTest() {
        RetailersResponse retailersResponse = metadataStepDef.getListRetailersMetadata();

        String retailerId = retailersResponse.data().stream().map(RetailersItem::id).findFirst().orElse(null);
        notNullOrEmptyParameter(retailerId, "retailerId");

        idList.remove(idList.size() - 1);
        RequestMultiEditData requestMultiEditData = RequestMultiEditData.builder().ids(idList).masterSellerId(retailerId).build();

        metadataStepDef.successMultiEditMetadata(requestMultiEditData);

        Map<String, Object> expQueryParam = new HashMap<>();
        expQueryParam.put(MASTER_SELLER_IDS.getName(), retailerId);

        FiltrationResponse filtrationResponse = metadataStepDef.successFiltrationMetadata(expQueryParam, false).extract().as(FiltrationResponse.class);

        List<String> actualIdList = new ArrayList<>();
        for (DataItem dataItem : filtrationResponse.getData()) {
            actualIdList.add(dataItem.getId());
        }

        Assert.compareParameters(idList, actualIdList, "id");
    }

    //@Test(description = "Фильтрация по полю master_seller_ids по нескольким значением", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"246939"})
    public void successFiltrationByMasterSellerIdsTest() {
        RetailersResponse retailersResponse = metadataStepDef.getListRetailersMetadata();

        List<String> retailerIds = Arrays.asList(retailersResponse.data().get(0).id(), retailersResponse.data().get(1).id());

        idList.remove(idList.size() - 1);

        for (int i = 0; i < idList.size(); i++) {
            metadataStepDef.successMultiEditMetadata(
                    RequestMultiEditData.builder().ids(Collections.singletonList(idList.get(i))).masterSellerId(i % 2 == 0 ? retailerIds.get(0) : retailerIds.get(1)).build());
        }

        Map<String, Object> expQueryParam = new HashMap<>();
        expQueryParam.put(MASTER_SELLER_IDS.getName(), retailerIds);

        FiltrationResponse filtrationResponse = metadataStepDef.successFiltrationMetadata(expQueryParam, false).extract().as(FiltrationResponse.class);

        List<String> actualIdList = new ArrayList<>();
        for (DataItem dataItem : filtrationResponse.getData()) {
            actualIdList.add(dataItem.getId());
        }

        Assert.compareParameters(idList, actualIdList, "id");
    }

    //@Test(description = "Фильтрация по полю master_seller_ids = null", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"246939"})
    public void successFiltrationByNullMasterSellerIdTest() {
        Map<String, Object> expQueryParam = new HashMap<>();
        expQueryParam.put(MASTER_SELLER_IDS.getName(), null);

        FiltrationResponse filtrationResponse = metadataStepDef.successFiltrationMetadata(expQueryParam, false).extract().as(FiltrationResponse.class);

        List<String> actualIdList = new ArrayList<>();
        for (DataItem dataItem : filtrationResponse.getData()) {
            actualIdList.add(dataItem.getId());
        }

        Assert.compareParameters(idList, actualIdList, "id");
    }

    //@Test(description = "Фильтрация по полю master_seller_ids = несуществующий id", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"246939"})
    public void unsuccessfulFiltrationByNullMasterSellerIdTest() {
        Map<String, Object> expQueryParam = new HashMap<>();
        expQueryParam.put(MASTER_SELLER_IDS.getName(), UUID.randomUUID().toString());

        FiltrationResponse filtrationResponse = metadataStepDef.successFiltrationMetadata(expQueryParam, true).extract().as(FiltrationResponse.class);

        List<String> actualIdList = new ArrayList<>();
        for (DataItem dataItem : filtrationResponse.getData()) {
            actualIdList.add(dataItem.getId());
        }

        Assert.equalsTrueParameter(actualIdList.isEmpty(), "id");
    }

    //@Test(description = "Фильтрация по полю assignee_ids по одному значению", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"230069"})
    public void successFiltrationByAssigneeIdTest() {
        String expUsersId = metadataStepDef.getListUsersMetadata().get(0).id();

        idList.remove(idList.size() - 1);
        RequestMultiEditData requestMultiEditData = RequestMultiEditData.builder().ids(idList).assigneeId(expUsersId).build();

        metadataStepDef.successMultiEditMetadata(requestMultiEditData);

        Map<String, Object> expQueryParam = new HashMap<>();
        expQueryParam.put(ImageParameters.ASSIGNEE_IDS.getName(), expUsersId);

        FiltrationResponse filtrationResponse = metadataStepDef.successFiltrationMetadata(expQueryParam, false).extract().as(FiltrationResponse.class);

        List<String> actualIdList = new ArrayList<>();
        for (DataItem dataItem : filtrationResponse.getData()) {
            actualIdList.add(dataItem.getId());
        }

        Assert.compareParameters(idList, actualIdList, "id");
    }

    //@Test(description = "Фильтрация по полю assignee_ids по нескольким значениям", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"230069"})
    public void successFiltrationByAssigneeIdsTest() {
        List<String> userIds = metadataStepDef.getListUsersMetadata().stream().map(UsersItem::id).collect(Collectors.toList());

        idList.remove(idList.size() - 1);

        List<String> expUserIds = new ArrayList<>();
        for (int i = 0; i < idList.size(); i++) {
            String expUserId = userIds.get(i);
            expUserIds.add(expUserId);
            metadataStepDef.successMultiEditMetadata(
                    RequestMultiEditData.builder().ids(Collections.singletonList(idList.get(i))).assigneeId(expUserId).build());
        }

        Map<String, Object> expQueryParam = new HashMap<>();
        expQueryParam.put(ImageParameters.ASSIGNEE_IDS.getName(), expUserIds);

        FiltrationResponse filtrationResponse = metadataStepDef.successFiltrationMetadata(expQueryParam, false).extract().as(FiltrationResponse.class);

        List<String> actualIdList = new ArrayList<>();
        for (DataItem dataItem : filtrationResponse.getData()) {
            actualIdList.add(dataItem.getId());
        }

        Assert.compareParameters(idList, actualIdList, "id");
    }

    //@Test(description = "Фильтрация по полю assignee_ids = null", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"230069"})
    public void successFiltrationByNullAssigneeIdTest() {
        Map<String, Object> expQueryParam = new HashMap<>();
        expQueryParam.put(ImageParameters.ASSIGNEE_IDS.getName(), null);

        FiltrationResponse filtrationResponse = metadataStepDef.successFiltrationMetadata(expQueryParam, false).extract().as(FiltrationResponse.class);

        List<String> actualIdList = new ArrayList<>();
        for (DataItem dataItem : filtrationResponse.getData()) {
            actualIdList.add(dataItem.getId());
        }

        Assert.compareParameters(idList, actualIdList, "id");
    }

    //@Test(description = "Фильтрация по полю assignee_ids = несуществующий id", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"230069"})
    public void unsuccessfulFiltrationByAssigneeIdTest() {
        Map<String, Object> expQueryParam = new HashMap<>();
        expQueryParam.put(ImageParameters.ASSIGNEE_IDS.getName(), UUID.randomUUID().toString());

        FiltrationResponse filtrationResponse = metadataStepDef.successFiltrationMetadata(expQueryParam, true).extract().as(FiltrationResponse.class);

        List<String> actualIdList = new ArrayList<>();
        for (DataItem dataItem : filtrationResponse.getData()) {
            actualIdList.add(dataItem.getId());
        }

        Assert.equalsTrueParameter(actualIdList.isEmpty(), "id");
    }

    //@Test(description = "Фильтрация по полю created_by", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"230070"})
    public void successFiltrationByCreatedByTest() {
        String expCreatedById = metadataStepDef.getListUsersMetadata().stream().
                filter(user -> user.fullName().equals(getData().createdName())).map(UsersItem::id).findFirst().orElseThrow(RuntimeException::new);

        Map<String, Object> expQueryParam = new HashMap<>();
        expQueryParam.put(ImageParameters.CREATED_BY.getName(), expCreatedById);

        FiltrationResponse filtrationResponse = metadataStepDef.successFiltrationMetadata(expQueryParam, false).extract().as(FiltrationResponse.class);

        List<String> actualIdList = new ArrayList<>();
        for (DataItem dataItem : filtrationResponse.getData()) {
            actualIdList.add(dataItem.getId());
        }

        Assert.compareParameters(idList, actualIdList, "id");
    }

    //@Test(description = "Фильтрация по полю created_by = null", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"230070"})
    public void successFiltrationByNullByCreatedByTest() {
        Map<String, Object> expQueryParam = new HashMap<>();
        expQueryParam.put(ImageParameters.CREATED_BY.getName(), null);

        FiltrationResponse filtrationResponse = metadataStepDef.successFiltrationMetadata(expQueryParam, false).extract().as(FiltrationResponse.class);

        List<String> actualIdList = new ArrayList<>();
        for (DataItem dataItem : filtrationResponse.getData()) {
            actualIdList.add(dataItem.getId());
        }

        Assert.compareParameters(idList, actualIdList, "id");
    }

    //@Test(description = "Фильтрация по полю created_by = несуществующий id", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"230070"})
    public void unsuccessfulFiltrationByCreatedByTest() {
        Map<String, Object> expQueryParam = new HashMap<>();
        expQueryParam.put(ImageParameters.CREATED_BY.getName(), UUID.randomUUID().toString());

        FiltrationResponse filtrationResponse = metadataStepDef.successFiltrationMetadata(expQueryParam, true).extract().as(FiltrationResponse.class);

        List<String> actualIdList = new ArrayList<>();
        for (DataItem dataItem : filtrationResponse.getData()) {
            actualIdList.add(dataItem.getId());
        }

        Assert.equalsTrueParameter(actualIdList.isEmpty(), "id");
    }

    //@Test(description = "Фильтрация по полю category_ids по одному значению", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"230071"})
    public void successFiltrationByCategoryIdTest() {
        List<DictionariesItem> categories = metadataStepDef.getListCategoriesMetadata();

        String expCategoryId = categories.stream().
                filter(c -> !c.name().equalsIgnoreCase(getData().unknownCategoryName())).
                map(DictionariesItem::id).findFirst().orElse(null);

        idList.remove(idList.size() - 1);
        RequestMultiEditData requestMultiEditData = RequestMultiEditData.builder().ids(idList).masterCategoryId(expCategoryId).build();

        metadataStepDef.successMultiEditMetadata(requestMultiEditData);

        Map<String, Object> expQueryParam = new HashMap<>();
        expQueryParam.put(ImageParameters.MASTER_CATEGORY_IDS.getName(), expCategoryId);

        FiltrationResponse filtrationResponse = metadataStepDef.successFiltrationMetadata(expQueryParam, false).extract().as(FiltrationResponse.class);

        List<String> actualIdList = new ArrayList<>();
        for (DataItem dataItem : filtrationResponse.getData()) {
            actualIdList.add(dataItem.getId());
        }

        Assert.compareParameters(idList, actualIdList, "id");
    }

    @Issue("SPC-2466")
    //@Test(description = "Фильтрация по полю category_ids по одному значению - неизвестная категория", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"230071"})
    public void successFiltrationByUnknownCategoryIdTest() {
        List<DictionariesItem> categories = metadataStepDef.getListCategoriesMetadata();

        String expCategoryId = categories.stream().
                filter(c -> c.name().equalsIgnoreCase(getData().unknownCategoryName())).
                map(DictionariesItem::id).findFirst().orElse(null);

        idList.remove(idList.size() - 1);
        RequestMultiEditData requestMultiEditData = RequestMultiEditData.builder().ids(idList).masterCategoryId(expCategoryId).build();

        metadataStepDef.successMultiEditMetadata(requestMultiEditData);

        Map<String, Object> expQueryParam = new HashMap<>();
        expQueryParam.put(ImageParameters.MASTER_CATEGORY_IDS.getName(), expCategoryId);

        FiltrationResponse filtrationResponse = metadataStepDef.successFiltrationMetadata(expQueryParam, false).extract().as(FiltrationResponse.class);

        List<String> actualIdList = new ArrayList<>();
        for (DataItem dataItem : filtrationResponse.getData()) {
            actualIdList.add(dataItem.getId());
        }

        Assert.compareParameters(idList, actualIdList, "id");
    }

    //@Test(description = "Фильтрация по полю category_ids по нескольким значениям", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"230071"})
    public void successFiltrationByCategoryIdsTest() {
        List<DictionariesItem> categories = metadataStepDef.getListCategoriesMetadata().
                stream().filter(c -> !c.name().equalsIgnoreCase(getData().unknownCategoryName())).collect(Collectors.toList());

        String expCategoryIdFirst = categories.get(0).id();
        String expCategoryIdSecond = categories.get(1).id();
        idList.remove(idList.size() - 1);

        for (int i = 0; i < idList.size(); i++) {
            RequestMultiEditData requestMultiEditData = RequestMultiEditData.builder().ids(Collections.singletonList(idList.get(i))).masterCategoryId(i % 2 == 0 ? expCategoryIdFirst : expCategoryIdSecond).build();
            metadataStepDef.successMultiEditMetadata(requestMultiEditData);
        }

        Map<String, Object> expQueryParam = new HashMap<>();
        expQueryParam.put(ImageParameters.MASTER_CATEGORY_IDS.getName(), Arrays.asList(expCategoryIdFirst, expCategoryIdSecond));

        FiltrationResponse filtrationResponse = metadataStepDef.successFiltrationMetadata(expQueryParam, false).extract().as(FiltrationResponse.class);

        List<String> actualIdList = new ArrayList<>();
        for (DataItem dataItem : filtrationResponse.getData()) {
            actualIdList.add(dataItem.getId());
        }

        Assert.compareParameters(idList, actualIdList, "id");
    }

    //@Test(description = "Фильтрация по полю category_ids = null", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"230071"})
    public void successFiltrationByNullCategoryIdTest() {
        Map<String, Object> expQueryParam = new HashMap<>();
        expQueryParam.put(ImageParameters.MASTER_CATEGORY_IDS.getName(), null);

        FiltrationResponse filtrationResponse = metadataStepDef.successFiltrationMetadata(expQueryParam, false).extract().as(FiltrationResponse.class);

        List<String> actualIdList = new ArrayList<>();
        for (DataItem dataItem : filtrationResponse.getData()) {
            actualIdList.add(dataItem.getId());
        }

        Assert.compareParameters(idList, actualIdList, "id");
    }

    //@Test(description = "Фильтрация по полю category_ids = несуществующий id", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"230071"})
    public void unsuccessfulFiltrationByCategoryIdTest() {
        Map<String, Object> expQueryParam = new HashMap<>();
        expQueryParam.put(ImageParameters.MASTER_CATEGORY_IDS.getName(), UUID.randomUUID().toString());

        FiltrationResponse filtrationResponse = metadataStepDef.successFiltrationMetadata(expQueryParam, true).extract().as(FiltrationResponse.class);

        List<String> actualIdList = new ArrayList<>();
        for (DataItem dataItem : filtrationResponse.getData()) {
            actualIdList.add(dataItem.getId());
        }

        Assert.equalsTrueParameter(actualIdList.isEmpty(), "id");
    }

    //@Test(description = "Фильтрация по полю is_own_trademark = true", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"230073"})
    public void successFiltrationByIsOwnTrademarkTest() {
        RequestMultiEditData requestMultiEditData = RequestMultiEditData.builder().ids(idList).isOwnTrademark(true).build();
        metadataStepDef.successMultiEditMetadata(requestMultiEditData);

        Map<String, Object> expQueryParam = new HashMap<>();
        expQueryParam.put(ImageParameters.IS_OWN_TRADEMARK.getName(), true);

        FiltrationResponse filtrationResponse = metadataStepDef.successFiltrationMetadata(expQueryParam, false).extract().as(FiltrationResponse.class);

        List<String> actualIdList = new ArrayList<>();
        for (DataItem dataItem : filtrationResponse.getData()) {
            actualIdList.add(dataItem.getId());
        }

        Assert.compareParameters(idList, actualIdList, "id");
    }

    //@Test(description = "Фильтрация по полю is_own_trademark = null", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"230073"})
    public void successFiltrationByNullIsOwnTrademarkTest() {
        Map<String, Object> expQueryParam = new HashMap<>();
        expQueryParam.put(ImageParameters.IS_OWN_TRADEMARK.getName(), null);

        FiltrationResponse filtrationResponse = metadataStepDef.successFiltrationMetadata(expQueryParam, false).extract().as(FiltrationResponse.class);

        List<String> actualIdList = new ArrayList<>();
        for (DataItem dataItem : filtrationResponse.getData()) {
            actualIdList.add(dataItem.getId());
        }

        Assert.compareParameters(idList, actualIdList, "id");
    }

    //@Test(description = "Фильтрация по полю is_copyright = true", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"230074"})
    public void successFiltrationByIsCopyrightTest() {
        RequestMultiEditData requestMultiEditData = RequestMultiEditData.builder().ids(idList).isCopyright(true).build();
        metadataStepDef.successMultiEditMetadata(requestMultiEditData);

        Map<String, Object> expQueryParam = new HashMap<>();
        expQueryParam.put(ImageParameters.IS_COPYRIGHT.getName(), true);

        FiltrationResponse filtrationResponse = metadataStepDef.successFiltrationMetadata(expQueryParam, false).extract().as(FiltrationResponse.class);

        List<String> actualIdList = new ArrayList<>();
        for (DataItem dataItem : filtrationResponse.getData()) {
            actualIdList.add(dataItem.getId());
        }

        Assert.compareParameters(idList, actualIdList, "id");
    }

    //@Test(description = "Фильтрация по полю is_copyright = null", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"230074"})
    public void successFiltrationByNullIsCopyrightTest() {
        Map<String, Object> expQueryParam = new HashMap<>();
        expQueryParam.put(ImageParameters.IS_COPYRIGHT.getName(), null);

        FiltrationResponse filtrationResponse = metadataStepDef.successFiltrationMetadata(expQueryParam, false).extract().as(FiltrationResponse.class);

        List<String> actualIdList = new ArrayList<>();
        for (DataItem dataItem : filtrationResponse.getData()) {
            actualIdList.add(dataItem.getId());
        }

        Assert.compareParameters(idList, actualIdList, "id");
    }

    //@Test(description = "Фильтрация по полю is_raw_image = true", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"230075"})
    public void successFiltrationByIsRawImageTest() {
        for (String id : idList) {
            metadataStepDef.successEditMetadata(id, ImageParameters.IS_RAW_IMAGE.getName(), true);
        }

        Map<String, Object> expQueryParam = new HashMap<>();
        expQueryParam.put(ImageParameters.IS_RAW_IMAGE.getName(), true);

        FiltrationResponse filtrationResponse = metadataStepDef.successFiltrationMetadata(expQueryParam, false).extract().as(FiltrationResponse.class);

        List<String> actualIdList = new ArrayList<>();
        for (DataItem dataItem : filtrationResponse.getData()) {
            actualIdList.add(dataItem.getId());
        }

        Assert.compareParameters(idList, actualIdList, "id");
    }

    //@Test(description = "Фильтрация по полю is_raw_image = null", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"230075"})
    public void successFiltrationByNullIsRawImageTest() {
        Map<String, Object> expQueryParam = new HashMap<>();
        expQueryParam.put(ImageParameters.IS_RAW_IMAGE.getName(), null);

        FiltrationResponse filtrationResponse = metadataStepDef.successFiltrationMetadata(expQueryParam, false).extract().as(FiltrationResponse.class);

        List<String> actualIdList = new ArrayList<>();
        for (DataItem dataItem : filtrationResponse.getData()) {
            actualIdList.add(dataItem.getId());
        }

        Assert.compareParameters(idList, actualIdList, "id");
    }

    //@Test(description = "Фильтрация по полю sort = created_asc", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"230076"})
    public void successFiltrationByCreatedAscSortTest() {
        Map<String, Object> expQueryParam = new HashMap<>();
        expQueryParam.put(SORT.getName(), CREATED_ASC.getName());

        FiltrationResponse filtrationResponse = metadataStepDef.successFiltrationMetadata(expQueryParam, false).extract().as(FiltrationResponse.class);

        List<String> actualIdList = new ArrayList<>();
        for (DataItem dataItem : filtrationResponse.getData()) {
            actualIdList.add(dataItem.getId());
        }

        Assert.compareParameters(idList, actualIdList, "id");
    }

    //@Test(description = "Фильтрация по полю sort = created_desc", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"230077"})
    public void successFiltrationByCreatedDescSortTest() {
        Map<String, Object> expQueryParam = new HashMap<>();
        expQueryParam.put(SORT.getName(), CREATED_DESC.getName());

        FiltrationResponse filtrationResponse = metadataStepDef.successFiltrationMetadata(expQueryParam, false).extract().as(FiltrationResponse.class);

        List<String> actualIdList = new ArrayList<>();
        for (DataItem dataItem : filtrationResponse.getData()) {
            actualIdList.add(dataItem.getId());
        }

        Collections.reverse(idList);

        Assert.compareParameters(idList, actualIdList, "id");
    }

    //@Test(description = "Фильтрация по полю sort = updated_asc", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"230078"})
    public void successFiltrationByUpdatedAscTest() {
        Map<String, Object> expQueryParam = new HashMap<>();
        expQueryParam.put(SORT.getName(), UPDATED_ASC.getName());

        FiltrationResponse filtrationResponse = metadataStepDef.successFiltrationMetadata(expQueryParam, false).extract().as(FiltrationResponse.class);

        List<String> actualIdList = new ArrayList<>();
        for (DataItem dataItem : filtrationResponse.getData()) {
            actualIdList.add(dataItem.getId());
        }
        Assert.compareParameters(idList.get(0), actualIdList.get(0), "id");
    }

    //@Test(description = "Фильтрация по полю sort = updated_desc", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"230079"})
    public void successFiltrationByUpdatedDescSortTest() {
        metadataStepDef.successEditMetadata(idList.get(1), ImageParameters.IS_RAW_IMAGE.getName(), false);

        Map<String, Object> expQueryParam = new HashMap<>();
        expQueryParam.put(SORT.getName(), UPDATED_DESC.getName());

        FiltrationResponse filtrationResponse = metadataStepDef.successFiltrationMetadata(expQueryParam, false).extract().as(FiltrationResponse.class);

        List<String> actualIdList = new ArrayList<>();
        for (DataItem dataItem : filtrationResponse.getData()) {
            actualIdList.add(dataItem.getId());
        }
        Assert.compareParameters(idList.get(1), actualIdList.get(0), "id");
    }

    //@Test(description = "Фильтрация по полю limit = 1", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"230080"})
    public void successFiltrationByLimitTest() {
        Map<String, Object> expQueryParam = new HashMap<>();
        expQueryParam.put(LIMIT.getName(), 1);

        FiltrationResponse filtrationResponse = metadataStepDef.successFiltrationMetadata(expQueryParam, false).extract().as(FiltrationResponse.class);

        List<String> actualIdList = new ArrayList<>();
        for (DataItem dataItem : filtrationResponse.getData()) {
            actualIdList.add(dataItem.getId());
        }
        Assert.compareParameters(1, actualIdList.size(), "id");
    }

    //@Test(description = "Фильтрация по полю offset = 1", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"230081"})
    public void successFiltrationByOffsetTest() {
        Map<String, Object> expQueryParam = new HashMap<>();
        expQueryParam.put(OFFSET.getName(), 1);

        FiltrationResponse filtrationResponse = metadataStepDef.successFiltrationMetadata(expQueryParam, false).extract().as(FiltrationResponse.class);

        List<String> actualIdList = new ArrayList<>();
        for (DataItem dataItem : filtrationResponse.getData()) {
            actualIdList.add(dataItem.getId());
        }
        Assert.compareParameters(idList.get(1), actualIdList.get(0), "id");
    }

    //@Test(description = "Фильтрация по полю priorities = существующее значение", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"230082"})
    public void successFiltrationByPriorityTest() {
        int priority = new Random().nextInt(NINETY_NINE) + 1;
        idList.remove(idList.size() - 1);
        for (String id : idList) {
            metadataStepDef.successEditMetadata(id, PRIORITY.getName(), priority);
        }

        Map<String, Object> expQueryParam = new HashMap<>();
        expQueryParam.put(PRIORITIES.getName(), priority);

        FiltrationResponse filtrationResponse = metadataStepDef.successFiltrationMetadata(expQueryParam, false).extract().as(FiltrationResponse.class);

        List<String> actualIdList = new ArrayList<>();
        for (DataItem dataItem : filtrationResponse.getData()) {
            actualIdList.add(dataItem.getId());
        }
        Assert.compareParameters(idList, actualIdList, "id");
    }

    //@Test(description = "Фильтрация по полю priorities = несколько значений", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"230082"})
    public void successFiltrationBySomePriorityTest() {
        int firstPriority = new Random().nextInt(NINETY_NINE) + 1;
        int secondPriority = new Random().nextInt(NINETY_NINE) + 1;
        idList.remove(idList.size() - 1);
        for (int i = 0; i < idList.size(); i++) {
            metadataStepDef.successEditMetadata(idList.get(i), PRIORITY.getName(), i % 2 == 0 ? firstPriority : secondPriority);
        }

        Map<String, Object> expQueryParam = new HashMap<>();
        expQueryParam.put(PRIORITIES.getName(), Arrays.asList(firstPriority, secondPriority));

        FiltrationResponse filtrationResponse = metadataStepDef.successFiltrationMetadata(expQueryParam, false).extract().as(FiltrationResponse.class);

        List<String> actualIdList = new ArrayList<>();
        for (DataItem dataItem : filtrationResponse.getData()) {
            actualIdList.add(dataItem.getId());
        }
        Assert.compareParameters(idList, actualIdList, "id");
    }

    //@Test(description = "Фильтрация по полю priorities = null", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"230082"})
    public void successFiltrationByPriorityEmptyValueTest() {
        Map<String, Object> expQueryParam = new HashMap<>();
        expQueryParam.put(PRIORITIES.getName(), null);

        FiltrationResponse filtrationResponse = metadataStepDef.successFiltrationMetadata(expQueryParam, false).extract().as(FiltrationResponse.class);

        List<String> actualIdList = new ArrayList<>();
        for (DataItem dataItem : filtrationResponse.getData()) {
            actualIdList.add(dataItem.getId());
        }
        Assert.compareParameters(idList, actualIdList, "id");
    }

    //@Test(description = "Фильтрация по полю priorities = not exist", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"230082"})
    public void successFiltrationByPriorityNotExistValueTest() {
        int priority = new Random().nextInt(ONE_THOUSAND) + 1;

        Map<String, Object> expQueryParam = new HashMap<>();
        expQueryParam.put(PRIORITIES.getName(), priority);

        FiltrationResponse filtrationResponse = metadataStepDef.successFiltrationMetadata(expQueryParam, true).extract().as(FiltrationResponse.class);

        List<String> actualIdList = new ArrayList<>();
        for (DataItem dataItem : filtrationResponse.getData()) {
            actualIdList.add(dataItem.getId());
        }

        Assert.equalsTrueParameter(actualIdList.isEmpty(), "id");
    }

    //@Test(description = "Фильтрация по всем полям", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"230083"})
    public void successFiltrationByAllFieldsTest() {
        int priority = new Random().nextInt(NINETY_NINE) + 1;

        String expStatusId = metadataStepDef.getListStatusesMetadata()
                .stream().filter(item -> item.name().equals(Status.DELETE.getName()))
                .map(DictionariesItem::id).findFirst().orElse(null);
        String expQualityId = metadataStepDef.getListQualitiesMetadata()
                .stream().filter(item -> item.name().equals(Quality.GOOD.getName())).
                map(DictionariesItem::id).findFirst().orElse(null);
        String expSourceId = metadataStepDef.getListSourcesMetadata()
                .stream().filter(item -> item.name().equals(Source.BRAND.getName())).
                map(DictionariesItem::id).findFirst().orElse(null);
        String expAssigneeId = metadataStepDef.getListUsersMetadata().get(0).id();
        String expCategoryId = metadataStepDef.getListCategoriesMetadata().stream().
                filter(c -> !c.name().equalsIgnoreCase(getData().unknownCategoryName())).
                map(DictionariesItem::id).findFirst().orElse(null);;

        idList.remove(idList.size() - 1);
        RequestMultiEditData requestMultiEditData = RequestMultiEditData.builder()
                .ids(idList).statusId(expStatusId)
                .qualityId(expQualityId).sourceId(expSourceId)
                .assigneeId(expAssigneeId).masterCategoryId(expCategoryId)
                .isOwnTrademark(true).isCopyright(true)
                .build();

        metadataStepDef.successMultiEditMetadata(requestMultiEditData);

        List<String> expSkuList = new ArrayList<>();
        for (String id : idList) {
            String sku = metadataStepDef.checkMetadata(id).then().extract().path(SKU.getPath());
            expSkuList.add(sku);
        }

        for (String id : idList) {
            metadataStepDef.successEditMetadata(id, IS_RAW_IMAGE.getName(), true);
            metadataStepDef.successEditMetadata(id, PRIORITY.getName(), priority);
        }

        Map<String, Object> expQueryParam = new HashMap<>();
        expQueryParam.put(SKUS.getName(), expSkuList);
        expQueryParam.put(SOURCE_ID.getName(), expSourceId);
        expQueryParam.put(STATUS_ID.getName(), expStatusId);
        expQueryParam.put(QUALITY_ID.getName(), expQualityId);
        expQueryParam.put(ASSIGNEE_IDS.getName(), expAssigneeId);
        expQueryParam.put(MASTER_CATEGORY_IDS.getName(), expCategoryId);
        expQueryParam.put(IS_OWN_TRADEMARK.getName(), true);
        expQueryParam.put(IS_COPYRIGHT.getName(), true);
        expQueryParam.put(IS_RAW_IMAGE.getName(), true);
        expQueryParam.put(SORT.getName(), UPDATED_DESC.getName());
        expQueryParam.put(PRIORITY.getName(), priority);

        FiltrationResponse filtrationResponse = metadataStepDef.successFiltrationMetadata(expQueryParam, false).extract().as(FiltrationResponse.class);

        List<String> actualIdList = new ArrayList<>();
        for (DataItem dataItem : filtrationResponse.getData()) {
            actualIdList.add(dataItem.getId());
        }

        Assert.compareParameters(idList, actualIdList, "id");
    }
}
