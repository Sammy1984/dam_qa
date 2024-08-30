package ru.spice.at.api.metadata_service.test;

import io.qameta.allure.Feature;
import io.qameta.allure.Issue;
import io.qameta.allure.Step;
import io.qameta.allure.Story;
import io.restassured.response.Response;
import org.awaitility.Awaitility;
import org.testng.annotations.BeforeClass;
import ru.testit.annotations.WorkItemIds;
import org.apache.commons.lang3.RandomStringUtils;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import ru.spice.at.api.dto.request.metadata.RequestMultiEditData;
import ru.spice.at.api.dto.response.metadata.*;
import ru.spice.at.api.dto.response.metadata.filtration.DataItem;
import ru.spice.at.api.metadata_service.MetadataSettings;
import ru.spice.at.api.metadata_service.MetadataStepDef;
import ru.spice.at.api.retailer_media_import_service.RetailerMediaImportSettings;
import ru.spice.at.api.retailer_media_import_service.RetailerMediaImportStepDef;
import ru.spice.at.common.base_test.BaseApiTest;
import ru.spice.at.common.dto.dam.ImageData;
import ru.spice.at.common.emuns.ApiServices;
import ru.spice.at.common.emuns.dam.*;
import ru.spice.at.common.utils.Assert;

import java.time.OffsetDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static java.time.format.DateTimeFormatter.*;
import static ru.spice.at.common.constants.TestConstants.*;
import static ru.spice.at.common.emuns.Role.*;
import static ru.spice.at.common.emuns.dam.ImageFormat.JPG;
import static ru.spice.at.common.emuns.dam.ImageParameters.*;
import static ru.spice.at.common.utils.Assert.*;
import static ru.spice.at.common.utils.JsonHelper.getValueFromResponse;

@Feature("Metadata Service")
@Story("POST metadata searching filtration data")
public class MetadataPostSearchingFiltrationTests extends BaseApiTest<MetadataSettings> {
    private final static int COUNT_METADATA = 4;
    private final MetadataStepDef metadataStepDef;
    private final RetailerMediaImportStepDef retailerMediaImportStepDef;
    private final List<String> idList = new ArrayList<>();
    private final List<ImageData> imageList = new ArrayList<>();

    protected MetadataPostSearchingFiltrationTests() {
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

    @Test(description = "Фильтрация по массиву skus по одному значению", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"191880"})
    public void successFiltrationBySkuTest() {
        List<String> expectedSkus = Collections.singletonList(imageList.get(0).getSku());

        List<DataItem> dataItems = metadataStepDef.successMetadataSearching(SKUS.getName(), expectedSkus);
        List<String> actualSkus = dataItems.stream().map(DataItem::getSku).collect(Collectors.toList());

        Assert.compareParameters(expectedSkus, actualSkus, "skus");
    }

    @Test(description = "Фильтрация по массиву skus по нескольким значениям", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"191880"})
    public void successFiltrationBySkusTest() {
        List<String> expectedSkus = Arrays.asList(imageList.get(0).getSku(), imageList.get(1).getSku());

        List<DataItem> dataItems = metadataStepDef.successMetadataSearching(SKUS.getName(), expectedSkus);
        List<String> actualSkus = dataItems.stream().map(DataItem::getSku).collect(Collectors.toList());

        Assert.compareParameters(expectedSkus, actualSkus, "skus");
    }

    @Test(description = "Фильтрация по массиву skus - пустое значение", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"191880"})
    public void successFiltrationByEmptySkuTest() {
        metadataStepDef.successMetadataSearching(SKUS.getName(), Collections.singletonList(EMPTY_VALUE), true);
    }

    @Test(description = "Фильтрация по массиву skus - несуществующее значение", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"191880"})
    public void successFiltrationRandomSkuTest() {
        metadataStepDef.successMetadataSearching(SKUS.getName(), Collections.singletonList(RandomStringUtils.randomAlphabetic(10)), true);
    }

    @Test(description = "Фильтрация по массиву skus - неверный тип", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"191880"})
    public void unsuccessfulFiltrationInvalidTypeSkuTest() {
        List<InvalidParamsItem> invalidParams = metadataStepDef.unsuccessfulMetadataSearching(SKUS.getName(), UUID.randomUUID().toString());
        getData().invalidParams().get(2).name(SKUS.getName());
        assertAll(
                () -> compareParameters(1, invalidParams.size(), "invalid_params.size"),
                () -> compareParameters(getData().invalidParams().get(2), invalidParams.get(0), "invalid_params")
        );
    }

    @Test(description = "Фильтрация по полю source_ids по одному значению", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"230067"})
    public void successFiltrationBySourceIdTest() {
        String expSourceId = metadataStepDef.getListSourcesMetadata().stream().filter(item -> item.name().equals(Source.BRAND.getName())).map(DictionariesItem::id).findFirst().orElse(null);

        idList.remove(idList.size() - 1);
        RequestMultiEditData requestMultiEditData = RequestMultiEditData.builder().ids(idList).sourceId(expSourceId).build();
        metadataStepDef.successMultiEditMetadata(requestMultiEditData);

        List<DataItem> dataItems = metadataStepDef.successMetadataSearching(SOURCE_IDS.getName(), Collections.singletonList(expSourceId));
        List<String> actualIdList = dataItems.stream().map(DataItem::getId).collect(Collectors.toList());

        Assert.compareParameters(idList, actualIdList, "ids");
    }

    @Test(description = "Фильтрация по полю source_ids по нескольким значениям", timeOut = 600000, groups = {"regress"})
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

        List<DataItem> dataItems = metadataStepDef.successMetadataSearching(SOURCE_IDS.getName(), expSourceIds);
        List<String> actualIdList = dataItems.stream().map(DataItem::getId).collect(Collectors.toList());

        Assert.compareParameters(idList, actualIdList, "ids");
    }

    @Test(description = "Фильтрация по полю source_ids - пустой список", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"230067"})
    public void successFiltrationByEmptyListSourceIdTest() {
        List<DataItem> dataItems = metadataStepDef.successMetadataSearching(SOURCE_IDS.getName(), Collections.emptyList());
        List<String> actualIdList = dataItems.stream().map(DataItem::getId).collect(Collectors.toList());

        Assert.compareParameters(idList, actualIdList, "ids");
    }

    @Test(description = "Фильтрация по полю source_ids - несуществующий id", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"230067"})
    public void successFiltrationRandomBySourceIdTest() {
        metadataStepDef.successMetadataSearching(SOURCE_IDS.getName(), Collections.singletonList(UUID.randomUUID().toString()), true);
    }

    @Test(description = "Фильтрация по массиву source_ids - неверный тип", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"230067"})
    public void unsuccessfulFiltrationInvalidTypeSourceIdsTest() {
        List<InvalidParamsItem> invalidParams = metadataStepDef.unsuccessfulMetadataSearching(SOURCE_IDS.getName(), UUID.randomUUID().toString());
        getData().invalidParams().get(2).name(SOURCE_IDS.getName());
        assertAll(
                () -> compareParameters(1, invalidParams.size(), "invalid_params.size"),
                () -> compareParameters(getData().invalidParams().get(2), invalidParams.get(0), "invalid_params")
        );
    }

    @Test(description = "Фильтрация по полю quality_ids по одному значению", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"230068"})
    public void successFiltrationByQualityIdTest() {
        String expQualitiesId = metadataStepDef.getListQualitiesMetadata().stream().filter(item -> item.name().equals(Quality.TO_REVISION.getName())).map(DictionariesItem::id).findFirst().orElse(null);

        idList.remove(idList.size() - 1);
        RequestMultiEditData requestMultiEditData = RequestMultiEditData.builder().ids(idList).qualityId(expQualitiesId).build();
        metadataStepDef.successMultiEditMetadata(requestMultiEditData);

        List<DataItem> dataItems = metadataStepDef.successMetadataSearching(QUALITY_IDS.getName(), Collections.singletonList(expQualitiesId));
        List<String> actualIdList = dataItems.stream().map(DataItem::getId).collect(Collectors.toList());

        Assert.compareParameters(idList, actualIdList, "ids");
    }

    @Test(description = "Фильтрация по полю quality_ids по нескольким значением", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"230068"})
    public void successFiltrationByQualityIdsTest() {
        List<String> expQualitiesIds = metadataStepDef.getListQualitiesMetadata().stream().
                filter(item -> item.name().equals(Quality.TO_REVISION.getName()) || item.name().equals(Quality.BAD.getName())).map(DictionariesItem::id).collect(Collectors.toList());

        idList.remove(idList.size() - 1);

        for (int i = 0; i < idList.size(); i++) {
            metadataStepDef.successMultiEditMetadata(
                    RequestMultiEditData.builder().ids(Collections.singletonList(idList.get(i))).qualityId(i % 2 == 0 ? expQualitiesIds.get(0) : expQualitiesIds.get(1)).build());
        }

        List<DataItem> dataItems = metadataStepDef.successMetadataSearching(QUALITY_IDS.getName(), expQualitiesIds);
        List<String> actualIdList = dataItems.stream().map(DataItem::getId).collect(Collectors.toList());

        Assert.compareParameters(idList, actualIdList, "ids");
    }

    @Test(description = "Фильтрация по полю quality_ids - null", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"230068"})
    public void successFiltrationByNullQualityIdTest() {
        metadataStepDef.successMetadataSearching(QUALITY_IDS.getName(), null, true);
    }

    @Test(description = "Фильтрация по полю quality_ids - несуществующий id", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"230068"})
    public void successFiltrationByRandomQualityIdTest() {
        metadataStepDef.successMetadataSearching(QUALITY_IDS.getName(), Collections.singletonList(UUID.randomUUID().toString()), true);
    }

    @Test(description = "Фильтрация по массиву quality_ids - неверный тип", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"230068"})
    public void unsuccessfulFiltrationInvalidTypeQualityIdTest() {
        List<InvalidParamsItem> invalidParams = metadataStepDef.unsuccessfulMetadataSearching(QUALITY_IDS.getName(), UUID.randomUUID().toString());
        getData().invalidParams().get(2).name(QUALITY_IDS.getName());
        assertAll(
                () -> compareParameters(1, invalidParams.size(), "invalid_params.size"),
                () -> compareParameters(getData().invalidParams().get(2), invalidParams.get(0), "invalid_params")
        );
    }

    @Test(description = "Фильтрация по полю master_seller_ids по одному значению", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"246939"})
    public void successFiltrationByMasterSellerIdTest() {
        RetailersResponse retailersResponse = metadataStepDef.getListRetailersMetadata();

        String retailerId = retailersResponse.data().stream().map(RetailersItem::id).findFirst().orElse(null);
        notNullOrEmptyParameter(retailerId, "retailerId");

        idList.remove(idList.size() - 1);
        RequestMultiEditData requestMultiEditData = RequestMultiEditData.builder().ids(idList).masterSellerId(retailerId).build();

        metadataStepDef.successMultiEditMetadata(requestMultiEditData);

        List<DataItem> dataItems = metadataStepDef.successMetadataSearching(MASTER_SELLER_IDS.getName(), Collections.singletonList(retailerId));
        List<String> actualIdList = dataItems.stream().map(DataItem::getId).collect(Collectors.toList());

        Assert.compareParameters(idList, actualIdList, "ids");
    }

    @Test(description = "Фильтрация по полю master_seller_ids по нескольким значением", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"246939"})
    public void successFiltrationByMasterSellerIdsTest() {
        RetailersResponse retailersResponse = metadataStepDef.getListRetailersMetadata();

        List<String> retailerIds = Arrays.asList(retailersResponse.data().get(0).id(), retailersResponse.data().get(1).id());

        idList.remove(idList.size() - 1);

        for (int i = 0; i < idList.size(); i++) {
            metadataStepDef.successMultiEditMetadata(
                    RequestMultiEditData.builder().ids(Collections.singletonList(idList.get(i))).masterSellerId(i % 2 == 0 ? retailerIds.get(0) : retailerIds.get(1)).build());
        }

        List<DataItem> dataItems = metadataStepDef.successMetadataSearching(MASTER_SELLER_IDS.getName(), retailerIds);
        List<String> actualIdList = dataItems.stream().map(DataItem::getId).collect(Collectors.toList());

        Assert.compareParameters(idList, actualIdList, "ids");
    }

    @Test(description = "Фильтрация по полю master_seller_ids - пустой список", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"246939"})
    public void successFiltrationByEmptyListMasterSellerIdTest() {
        List<DataItem> dataItems = metadataStepDef.successMetadataSearching(MASTER_SELLER_IDS.getName(), Collections.emptyList());
        List<String> actualIdList = dataItems.stream().map(DataItem::getId).collect(Collectors.toList());

        Assert.compareParameters(idList, actualIdList, "ids");
    }

    @Test(description = "Фильтрация по полю master_seller_ids - несуществующий id", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"246939"})
    public void successFiltrationByRandomMasterSellerIdTest() {
        metadataStepDef.successMetadataSearching(MASTER_SELLER_IDS.getName(), Collections.singletonList(UUID.randomUUID().toString()), true);
    }

    @Test(description = "Фильтрация по массиву master_seller_ids - неверный тип", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"246939"})
    public void unsuccessfulFiltrationInvalidTypeMasterSellerIdsTest() {
        List<InvalidParamsItem> invalidParams = metadataStepDef.unsuccessfulMetadataSearching(MASTER_SELLER_IDS.getName(), UUID.randomUUID().toString());
        getData().invalidParams().get(2).name(MASTER_SELLER_IDS.getName());
        assertAll(
                () -> compareParameters(1, invalidParams.size(), "invalid_params.size"),
                () -> compareParameters(getData().invalidParams().get(2), invalidParams.get(0), "invalid_params")
        );
    }

    @Test(description = "Фильтрация по массиву master_seller_ids - неверный тип значения", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"246939"})
    public void unsuccessfulFiltrationInvalidValueTypeMasterSellerIdsTest() {
        List<InvalidParamsItem> invalidParams = metadataStepDef.unsuccessfulMetadataSearching(MASTER_SELLER_IDS.getName(),
                Collections.singletonList(RandomStringUtils.randomAlphabetic(12)));
        getData().invalidParams().get(2).name(MASTER_SELLER_IDS.getName());
        assertAll(
                () -> compareParameters(1, invalidParams.size(), "invalid_params.size"),
                () -> compareParameters(getData().invalidParams().get(2), invalidParams.get(0), "invalid_params")
        );
    }

    @Test(description = "Фильтрация по полю assignee_ids по одному значению", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"230069"})
    public void successFiltrationByAssigneeIdTest() {
        String expUsersId = metadataStepDef.getListUsersMetadata().get(0).id();

        idList.remove(idList.size() - 1);
        RequestMultiEditData requestMultiEditData = RequestMultiEditData.builder().ids(idList).assigneeId(expUsersId).build();

        metadataStepDef.successMultiEditMetadata(requestMultiEditData);

        List<DataItem> dataItems = metadataStepDef.successMetadataSearching(ASSIGNEE_IDS.getName(), Collections.singletonList(expUsersId));
        List<String> actualIdList = dataItems.stream().map(DataItem::getId).collect(Collectors.toList());

        Assert.compareParameters(idList, actualIdList, "ids");
    }

    @Test(description = "Фильтрация по полю assignee_ids по нескольким значениям", timeOut = 600000, groups = {"regress"})
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

        List<DataItem> dataItems = metadataStepDef.successMetadataSearching(ASSIGNEE_IDS.getName(), expUserIds);
        List<String> actualIdList = dataItems.stream().map(DataItem::getId).collect(Collectors.toList());

        Assert.compareParameters(idList, actualIdList, "ids");
    }

    @Test(description = "Фильтрация по полю assignee_ids - null", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"230069"})
    public void successFiltrationByNullAssigneeIdTest() {
        List<DataItem> dataItems = metadataStepDef.successMetadataSearching(ASSIGNEE_IDS.getName(), null);
        List<String> actualIdList = dataItems.stream().map(DataItem::getId).collect(Collectors.toList());

        Assert.compareParameters(idList, actualIdList, "ids");
    }

    @Test(description = "Фильтрация по полю assignee_ids = несуществующий id", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"230069"})
    public void successFiltrationByRandomAssigneeIdTest() {
        metadataStepDef.successMetadataSearching(ASSIGNEE_IDS.getName(), Collections.singletonList(UUID.randomUUID().toString()), true);
    }

    @Test(description = "Фильтрация по массиву assignee_ids - неверный тип", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"246939"})
    public void unsuccessfulFiltrationInvalidTypeAssigneeIdsTest() {
        List<InvalidParamsItem> invalidParams = metadataStepDef.unsuccessfulMetadataSearching(ASSIGNEE_IDS.getName(), UUID.randomUUID().toString());
        getData().invalidParams().get(2).name(ASSIGNEE_IDS.getName());
        assertAll(
                () -> compareParameters(1, invalidParams.size(), "invalid_params.size"),
                () -> compareParameters(getData().invalidParams().get(2), invalidParams.get(0), "invalid_params")
        );
    }

    @Test(description = "Фильтрация по массиву assignee_ids - неверный тип значения", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"246939"})
    public void unsuccessfulFiltrationInvalidValueTypeAssigneeIdsTest() {
        List<InvalidParamsItem> invalidParams = metadataStepDef.unsuccessfulMetadataSearching(ASSIGNEE_IDS.getName(),
                Collections.singletonList(RandomStringUtils.randomAlphabetic(12)));
        getData().invalidParams().get(2).name(ASSIGNEE_IDS.getName());
        assertAll(
                () -> compareParameters(1, invalidParams.size(), "invalid_params.size"),
                () -> compareParameters(getData().invalidParams().get(2), invalidParams.get(0), "invalid_params")
        );
    }

    @Test(description = "Фильтрация по массиву created_by по одному значению", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"230070"})
    public void successFiltrationByCreatedByTest() {
        metadataStepDef.setRole(PHOTOPRODUCTION);
        metadataStepDef.createMetadataImage(new ImageData(ImageFormat.JPEG));

        metadataStepDef.setRole(ADMINISTRATOR);
        String expCreatedById = metadataStepDef.getListUsersMetadata().stream().
                filter(user -> user.fullName().equals(ADMINISTRATOR.getFullName())).map(UsersItem::id).findFirst().orElseThrow(RuntimeException::new);

        List<DataItem> dataItems = metadataStepDef.successMetadataSearching(CREATED_BY.getName(), Collections.singletonList(expCreatedById));
        List<String> actualIdList = dataItems.stream().map(DataItem::getId).collect(Collectors.toList());

        Assert.compareParameters(idList, actualIdList, "ids");
    }

    @Deprecated
    //todo доработать при доработки словарей с поиском
    @Test(description = "Фильтрация по массиву created_by по нескольким значениям", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"230070"})
    public void successFiltrationBySomeCreatedByTest() {
        metadataStepDef.setRole(PHOTOPRODUCTION);
        idList.add(metadataStepDef.createMetadataImage(new ImageData(ImageFormat.JPEG)));

        metadataStepDef.setRole(CONTENT_PRODUCTION);
        metadataStepDef.createMetadataImage(new ImageData(ImageFormat.JPEG));

        metadataStepDef.setRole(ADMINISTRATOR);
        String expCreatedByAdministratorId = metadataStepDef.getListUsersMetadata(ADMINISTRATOR.getFullName()).get(0).id();
        String expCreatedByPhotoproductionId = metadataStepDef.getListUsersMetadata(PHOTOPRODUCTION.getFullName()).get(0).id();
        Assert.notNullOrEmptyParameter(expCreatedByAdministratorId, "id Администратора");
        Assert.notNullOrEmptyParameter(expCreatedByPhotoproductionId, "id Фотопродакшена");

        List<DataItem> dataItems = metadataStepDef.successMetadataSearching(CREATED_BY.getName(), Arrays.asList(expCreatedByAdministratorId, expCreatedByPhotoproductionId));
        List<String> actualIdList = dataItems.stream().map(DataItem::getId).collect(Collectors.toList());

        Assert.compareParameters(idList, actualIdList, "ids");
    }

    @Test(description = "Фильтрация по массиву created_by - null", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"230070"})
    public void successFiltrationByNullByCreatedByTest() {
        metadataStepDef.successMetadataSearching(CREATED_BY.getName(), null, true);
    }

    @Test(description = "Фильтрация по массиву created_by - несуществующий id", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"230070"})
    public void successFiltrationByRandomCreatedByTest() {
        metadataStepDef.successMetadataSearching(CREATED_BY.getName(), Collections.singletonList(UUID.randomUUID().toString()), true);
    }

    @Test(description = "Фильтрация по массиву created_by - неверный тип", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"230070"})
    public void unsuccessfulFiltrationInvalidTypeCreatedByTest() {
        List<InvalidParamsItem> invalidParams = metadataStepDef.unsuccessfulMetadataSearching(CREATED_BY.getName(), UUID.randomUUID().toString());
        getData().invalidParams().get(2).name(CREATED_BY.getName());
        assertAll(
                () -> compareParameters(1, invalidParams.size(), "invalid_params.size"),
                () -> compareParameters(getData().invalidParams().get(2), invalidParams.get(0), "invalid_params")
        );
    }

    @Test(description = "Фильтрация по полю category_ids по одному значению", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"230071"})
    public void successFiltrationByCategoryIdTest() {
        List<DictionariesItem> categories = metadataStepDef.getListCategoriesMetadata();

        String expCategoryId = categories.stream().
                filter(c -> !c.name().equalsIgnoreCase(getData().unknownCategoryName())).
                map(DictionariesItem::id).findFirst().orElse(null);

        idList.remove(idList.size() - 1);
        RequestMultiEditData requestMultiEditData = RequestMultiEditData.builder().ids(idList).masterCategoryId(expCategoryId).build();

        metadataStepDef.successMultiEditMetadata(requestMultiEditData);

        List<DataItem> dataItems = metadataStepDef.successMetadataSearching(MASTER_CATEGORY_IDS.getName(), Collections.singletonList(expCategoryId));
        List<String> actualIdList = dataItems.stream().map(DataItem::getId).collect(Collectors.toList());

        Assert.compareParameters(idList, actualIdList, "ids");
    }

    @Issue("SPC-2466")
    @Test(description = "Фильтрация по полю category_ids по одному значению - неизвестная категория", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"230071"})
    public void successFiltrationByUnknownCategoryIdTest() {
        List<DictionariesItem> categories = metadataStepDef.getListCategoriesMetadata();

        String expCategoryId = categories.stream().
                filter(c -> c.name().equalsIgnoreCase(getData().unknownCategoryName())).
                map(DictionariesItem::id).findFirst().orElse(null);

        idList.remove(idList.size() - 1);
        RequestMultiEditData requestMultiEditData = RequestMultiEditData.builder().ids(idList).masterCategoryId(expCategoryId).build();

        metadataStepDef.successMultiEditMetadata(requestMultiEditData);

        List<DataItem> dataItems = metadataStepDef.successMetadataSearching(MASTER_CATEGORY_IDS.getName(), Collections.singletonList(expCategoryId));
        List<String> actualIdList = dataItems.stream().map(DataItem::getId).collect(Collectors.toList());

        Assert.compareParameters(idList, actualIdList, "ids");
    }

    @Test(description = "Фильтрация по полю category_ids по нескольким значениям", timeOut = 600000, groups = {"regress"})
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

        List<DataItem> dataItems = metadataStepDef.successMetadataSearching(MASTER_CATEGORY_IDS.getName(), Arrays.asList(expCategoryIdFirst, expCategoryIdSecond));
        List<String> actualIdList = dataItems.stream().map(DataItem::getId).collect(Collectors.toList());

        Assert.compareParameters(idList, actualIdList, "ids");
    }

    @Test(description = "Фильтрация по полю category_ids - null", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"230071"})
    public void successFiltrationByNullCategoryIdTest() {
        List<DataItem> dataItems = metadataStepDef.successMetadataSearching(MASTER_CATEGORY_IDS.getName(), null);
        List<String> actualIdList = dataItems.stream().map(DataItem::getId).collect(Collectors.toList());

        Assert.compareParameters(idList, actualIdList, "ids");
    }

    @Test(description = "Фильтрация по полю category_ids - несуществующий id", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"230071"})
    public void successFiltrationByRandomCategoryIdTest() {
        metadataStepDef.successMetadataSearching(MASTER_CATEGORY_IDS.getName(), Collections.singletonList(UUID.randomUUID().toString()), true);
    }

    @Test(description = "Фильтрация по массиву category_ids - неверный тип", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"230071"})
    public void unsuccessfulFiltrationInvalidTypeCategoryIdTest() {
        List<InvalidParamsItem> invalidParams = metadataStepDef.unsuccessfulMetadataSearching(MASTER_CATEGORY_IDS.getName(), UUID.randomUUID().toString());
        getData().invalidParams().get(2).name(MASTER_CATEGORY_IDS.getName());
        assertAll(
                () -> compareParameters(1, invalidParams.size(), "invalid_params.size"),
                () -> compareParameters(getData().invalidParams().get(2), invalidParams.get(0), "invalid_params")
        );
    }

    @Test(description = "Фильтрация по полю is_own_trademark - true", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"230073"})
    public void successFiltrationByIsOwnTrademarkTest() {
        idList.remove(idList.size() - 1);
        RequestMultiEditData requestMultiEditData = RequestMultiEditData.builder().ids(idList).isOwnTrademark(true).build();
        metadataStepDef.successMultiEditMetadata(requestMultiEditData);

        List<DataItem> dataItems = metadataStepDef.successMetadataSearching(IS_OWN_TRADEMARK.getName(), true);
        List<String> actualIdList = dataItems.stream().map(DataItem::getId).collect(Collectors.toList());

        Assert.compareParameters(idList, actualIdList, "ids");
    }

    @Test(description = "Фильтрация по полю is_own_trademark = null", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"230073"})
    public void successFiltrationByNullIsOwnTrademarkTest() {
        List<DataItem> dataItems = metadataStepDef.successMetadataSearching(IS_OWN_TRADEMARK.getName(), null);
        List<String> actualIdList = dataItems.stream().map(DataItem::getId).collect(Collectors.toList());

        Assert.compareParameters(idList, actualIdList, "ids");
    }

    @Test(description = "Фильтрация по полю is_own_trademark - неверный тип", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"230073"})
    public void unsuccessfulFiltrationInvalidTypeIsOwnTrademarkTest() {
        List<InvalidParamsItem> invalidParams = metadataStepDef.unsuccessfulMetadataSearching(IS_OWN_TRADEMARK.getName(),
                Collections.singletonList(UUID.randomUUID().toString()));
        getData().invalidParams().get(2).name(IS_OWN_TRADEMARK.getName());
        assertAll(
                () -> compareParameters(1, invalidParams.size(), "invalid_params.size"),
                () -> compareParameters(getData().invalidParams().get(2), invalidParams.get(0), "invalid_params")
        );
    }

    @Test(description = "Фильтрация по полю is_copyright - true", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"230074"})
    public void successFiltrationByIsCopyrightTest() {
        idList.remove(idList.size() - 1);
        RequestMultiEditData requestMultiEditData = RequestMultiEditData.builder().ids(idList).isCopyright(true).build();
        metadataStepDef.successMultiEditMetadata(requestMultiEditData);

        List<DataItem> dataItems = metadataStepDef.successMetadataSearching(IS_COPYRIGHT.getName(), true);
        List<String> actualIdList = dataItems.stream().map(DataItem::getId).collect(Collectors.toList());

        Assert.compareParameters(idList, actualIdList, "ids");
    }

    @Test(description = "Фильтрация по полю is_copyright - null", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"230074"})
    public void successFiltrationByNullIsCopyrightTest() {
        List<DataItem> dataItems = metadataStepDef.successMetadataSearching(IS_COPYRIGHT.getName(), null);
        List<String> actualIdList = dataItems.stream().map(DataItem::getId).collect(Collectors.toList());

        Assert.compareParameters(idList, actualIdList, "ids");
    }

    @Test(description = "Фильтрация по полю is_copyright - неверный тип", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"230074"})
    public void unsuccessfulFiltrationInvalidTypeIsCopyrightTest() {
        List<InvalidParamsItem> invalidParams = metadataStepDef.unsuccessfulMetadataSearching(IS_COPYRIGHT.getName(), UUID.randomUUID().toString());
        getData().invalidParams().get(2).name(IS_COPYRIGHT.getName());
        assertAll(
                () -> compareParameters(1, invalidParams.size(), "invalid_params.size"),
                () -> compareParameters(getData().invalidParams().get(2), invalidParams.get(0), "invalid_params")
        );
    }

    @Test(description = "Фильтрация по полю is_raw_image - true", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"230075"})
    public void successFiltrationByIsRawImageTest() {
        idList.remove(idList.size() - 1);
        idList.forEach(id -> metadataStepDef.successEditMetadata(id, ImageParameters.IS_RAW_IMAGE.getName(), true));

        List<DataItem> dataItems = metadataStepDef.successMetadataSearching(IS_RAW_IMAGE.getName(), true);
        List<String> actualIdList = dataItems.stream().map(DataItem::getId).collect(Collectors.toList());

        Assert.compareParameters(idList, actualIdList, "ids");
    }

    @Test(description = "Фильтрация по полю is_raw_image - null", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"230075"})
    public void successFiltrationByNullIsRawImageTest() {
        metadataStepDef.successMetadataSearching(IS_RAW_IMAGE.getName(), null, true);
    }

    @Test(description = "Фильтрация по полю is_raw_image - неверный тип", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"230075"})
    public void unsuccessfulFiltrationInvalidTypeIsRawImageTest() {
        List<InvalidParamsItem> invalidParams = metadataStepDef.unsuccessfulMetadataSearching(IS_RAW_IMAGE.getName(), 2 + new Random().nextInt(8));
        getData().invalidParams().get(2).name(IS_RAW_IMAGE.getName());
        assertAll(
                () -> compareParameters(1, invalidParams.size(), "invalid_params.size"),
                () -> compareParameters(getData().invalidParams().get(2), invalidParams.get(0), "invalid_params")
        );
    }

    @Test(description = "Фильтрация по массиву priorities по одному значению", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"230082"})
    public void successFiltrationByPriorityTest() {
        Integer priority = new Random().nextInt(FOUR_HUNDRED_NINETY_NINE) + 1;
        idList.remove(idList.size() - 1);
        idList.forEach(id -> metadataStepDef.successEditMetadata(id, PRIORITY.getName(), priority));

        List<DataItem> dataItems = metadataStepDef.successMetadataSearching(PRIORITIES.getName(), Collections.singletonList(priority.toString()));
        List<String> actualIdList = dataItems.stream().map(DataItem::getId).collect(Collectors.toList());

        Assert.compareParameters(idList, actualIdList, "ids");
    }

    @Test(description = "Фильтрация по массиву priorities по несколько значений", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"230082"})
    public void successFiltrationBySomePriorityTest() {
        Integer firstPriority = new Random().nextInt(FOUR_HUNDRED_NINETY_NINE) + 1;
        Integer secondPriority = new Random().nextInt(FOUR_HUNDRED_NINETY_NINE) + 1;
        idList.remove(idList.size() - 1);
        for (int i = 0; i < idList.size(); i++) {
            metadataStepDef.successEditMetadata(idList.get(i), PRIORITY.getName(), i % 2 == 0 ? firstPriority : secondPriority);
        }

        List<DataItem> dataItems = metadataStepDef.successMetadataSearching(PRIORITIES.getName(), Arrays.asList(firstPriority, secondPriority));
        List<String> actualIdList = dataItems.stream().map(DataItem::getId).collect(Collectors.toList());

        Assert.compareParameters(idList, actualIdList, "ids");
    }

    @Test(description = "Фильтрация по массиву priorities - null", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"230082"})
    public void successFiltrationByPriorityEmptyValueTest() {
        String id = metadataStepDef.createMetadataImage(new ImageData(ImageFormat.JPEG));
        metadataStepDef.successEditMetadata(id, PRIORITY.getName(), new Random().nextInt(FOUR_HUNDRED_NINETY_NINE) + 1);

        List<DataItem> dataItems = metadataStepDef.successMetadataSearching(PRIORITIES.getName(), null);
        List<String> actualIdList = dataItems.stream().map(DataItem::getId).collect(Collectors.toList());

        Assert.compareParameters(idList, actualIdList, "ids");
    }

    @Test(description = "Фильтрация по массиву priorities - not exist", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"230082"})
    public void successFiltrationByPriorityNotExistValueTest() {
        int priority = new Random().nextInt(FOUR_HUNDRED_NINETY_NINE) + FOUR_HUNDRED_NINETY_NINE + 1;
        metadataStepDef.successMetadataSearching(PRIORITIES.getName(), Collections.singletonList(Integer.toString(priority)), true);
    }

    @Test(description = "Фильтрация по массиву priorities - неверный тип", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"230082"})
    public void unsuccessfulFiltrationInvalidTypePrioritiesTest() {
        List<InvalidParamsItem> invalidParams = metadataStepDef.unsuccessfulMetadataSearching(
                PRIORITIES.getName(), Collections.singletonList(UUID.randomUUID()));
        getData().invalidParams().get(2).name(PRIORITIES.getName());
        assertAll(
                () -> compareParameters(1, invalidParams.size(), "invalid_params.size"),
                () -> compareParameters(getData().invalidParams().get(2), invalidParams.get(0), "invalid_params")
        );
    }

    @Test(description = "Фильтрация по массиву external_task_ids по одному значению", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"254809"})
    public void successFiltrationByExternalTaskIdsTest() {
        int externalTaskId = new Random().nextInt(ONE_THOUSAND) + 1;
        idList.remove(idList.size() - 1);
        RequestMultiEditData requestMultiEditData = RequestMultiEditData.builder().ids(idList).externalTaskId(Integer.toString(externalTaskId)).build();
        metadataStepDef.successMultiEditMetadata(requestMultiEditData);

        List<DataItem> dataItems = metadataStepDef.successMetadataSearching(
                EXTERNAL_TASK_IDS.getName(), Collections.singletonList(Integer.toString(externalTaskId)));
        List<String> actualIdList = dataItems.stream().map(DataItem::getId).collect(Collectors.toList());

        Assert.compareParameters(idList, actualIdList, "ids");
    }

    @Test(description = "Фильтрация по массиву external_task_ids по несколько значений", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"254809"})
    public void successFiltrationBySomeExternalTaskIdsTest() {
        String firstExternalTaskId = Integer.toString(new Random().nextInt(ONE_THOUSAND) + 1);
        String secondExternalTaskId = Integer.toString(new Random().nextInt(ONE_THOUSAND) + 1);
        idList.remove(idList.size() - 1);
        RequestMultiEditData requestMultiEditData = RequestMultiEditData.builder().ids(idList).externalTaskId(firstExternalTaskId).build();
        metadataStepDef.successMultiEditMetadata(requestMultiEditData);

        String id = metadataStepDef.createMetadataImage(new ImageData(ImageFormat.JPEG));
        idList.add(id);
        metadataStepDef.successEditMetadata(id, EXTERNAL_TASK_ID.getName(), secondExternalTaskId);

        List<DataItem> dataItems = metadataStepDef.successMetadataSearching(
                EXTERNAL_TASK_IDS.getName(), Arrays.asList(firstExternalTaskId, secondExternalTaskId));
        List<String> actualIdList = dataItems.stream().map(DataItem::getId).collect(Collectors.toList());

        Assert.compareParameters(idList, actualIdList, "ids");
    }

    @Test(description = "Фильтрация по массиву external_task_ids - null", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"254809"})
    public void successFiltrationByNullExternalTaskIdsTest() {
        List<DataItem> dataItems = metadataStepDef.successMetadataSearching(
                EXTERNAL_TASK_IDS.getName(), null);
        List<String> actualIdList = dataItems.stream().map(DataItem::getId).collect(Collectors.toList());

        Assert.compareParameters(idList, actualIdList, "ids");
    }

    @Test(description = "Фильтрация по массиву external_task_ids - неверный тип", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"254809"})
    public void unsuccessfulFiltrationInvalidTypeExternalTaskIdsTest() {
        List<InvalidParamsItem> invalidParams = metadataStepDef.unsuccessfulMetadataSearching(
                EXTERNAL_TASK_IDS.getName(), Collections.singletonList(RandomStringUtils.randomAlphabetic(10)));
        getData().invalidParams().get(2).name(EXTERNAL_TASK_IDS.getName());
        assertAll(
                () -> compareParameters(1, invalidParams.size(), "invalid_params.size"),
                () -> compareParameters(getData().invalidParams().get(2), invalidParams.get(0), "invalid_params")
        );
    }

    @Test(description = "Фильтрация по массиву status_ids по одному значению", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"254808"})
    public void successFiltrationByStatusIdsTest() {
        String expStatusId = metadataStepDef.getListStatusesMetadata()
                .stream().filter(item -> item.name().equals(Status.DELETE.getName()))
                .map(DictionariesItem::id).findFirst().orElse(null);

        idList.remove(idList.size() - 1);
        RequestMultiEditData requestMultiEditData = RequestMultiEditData.builder().ids(idList).statusId(expStatusId).build();
        metadataStepDef.successMultiEditMetadata(requestMultiEditData);

        List<DataItem> dataItems = metadataStepDef.successMetadataSearching(STATUS_IDS.getName(), Collections.singletonList(expStatusId));
        List<String> actualIdList = dataItems.stream().map(DataItem::getId).collect(Collectors.toList());

        Assert.compareParameters(idList, actualIdList, "ids");
    }

    @Test(description = "Фильтрация по массиву status_ids по несколько значений", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"254808"})
    public void successFiltrationBySomeStatusIdsTest() {
        List<String> expStatusIds = metadataStepDef.getListStatusesMetadata()
                .stream().filter(item -> item.name().equals(Status.DELETE.getName()) || item.name().equals(Status.ARCHIVE.getName()))
                .map(DictionariesItem::id).collect(Collectors.toList());

        idList.remove(idList.size() - 1);
        RequestMultiEditData requestMultiEditData = RequestMultiEditData.builder().ids(idList).statusId(expStatusIds.get(0)).build();
        metadataStepDef.successMultiEditMetadata(requestMultiEditData);

        String id = metadataStepDef.createMetadataImage(new ImageData(ImageFormat.JPEG));
        idList.add(id);
        metadataStepDef.successEditMetadata(id, STATUS_ID.getName(), expStatusIds.get(1));

        List<DataItem> dataItems = metadataStepDef.successMetadataSearching(STATUS_IDS.getName(), expStatusIds);
        List<String> actualIdList = dataItems.stream().map(DataItem::getId).collect(Collectors.toList());

        Assert.compareParameters(idList, actualIdList, "ids");
    }

    @Test(description = "Фильтрация по массиву status_ids - null", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"254808"})
    public void successFiltrationByNullStatusIdsTest() {
        metadataStepDef.successMetadataSearching(STATUS_IDS.getName(), null, true);
    }

    @Test(description = "Фильтрация по массиву status_ids - неверный тип", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"254808"})
    public void unsuccessfulFiltrationInvalidTypeStatusIdsTest() {
        List<InvalidParamsItem> invalidParams = metadataStepDef.unsuccessfulMetadataSearching(
                STATUS_IDS.getName(), RandomStringUtils.randomAlphabetic(10));
        getData().invalidParams().get(2).name(STATUS_IDS.getName());
        assertAll(
                () -> compareParameters(1, invalidParams.size(), "invalid_params.size"),
                () -> compareParameters(getData().invalidParams().get(2), invalidParams.get(0), "invalid_params")
        );
    }

    @Test(description = "Фильтрация по массиву external_offer_ids по одному значению", timeOut = 600000, groups = {"regress", "kafka"})
    @WorkItemIds({"254806"})
    public void successFiltrationByExternalOfferIdsTest() {
        String externalOfferId = Integer.toString(new Random().nextInt(1000000) + 1000000);

        RetailerMediaImportSettings.ImportParameters importParameters = getData().importParameters().clone().
                mediaName(RandomStringUtils.randomAlphabetic(10) + DOT_VALUE + JPG.getFormatName()).
                masterSellerId(new Random().nextInt(100) + 1).
                externalOfferId(externalOfferId);
        retailerMediaImportStepDef.sendMediaFile(retailerMediaImportStepDef.buildImportOneMediaRequest(importParameters));

        List<DataItem> dataItems = metadataStepDef.successMetadataSearching(EXTERNAL_OFFER_IDS.getName(), Collections.singletonList(externalOfferId));
        List<String> actualIdList = dataItems.stream().map(DataItem::getId).collect(Collectors.toList());

        Assert.compareParameters(2, actualIdList.size(), "ids");
    }

    @Test(description = "Фильтрация по массиву external_offer_ids по нескольким значениям", timeOut = 600000, groups = {"regress", "kafka"})
    @WorkItemIds({"254806"})
    public void successFiltrationBySomeExternalOfferIdsTest() {
        String firstExternalOfferId = Integer.toString(new Random().nextInt(1000000) + 1000000);

        RetailerMediaImportSettings.ImportParameters importParameters = getData().importParameters().clone().
                mediaName(RandomStringUtils.randomAlphabetic(10) + DOT_VALUE + JPG.getFormatName()).
                masterSellerId(new Random().nextInt(100) + 1).
                externalOfferId(firstExternalOfferId);
        retailerMediaImportStepDef.sendMediaFile(retailerMediaImportStepDef.buildImportOneMediaRequest(importParameters));

        String secondExternalOfferId = Integer.toString(new Random().nextInt(1000000) + 1000000);

        importParameters = getData().importParameters().clone().
                mediaName(RandomStringUtils.randomAlphabetic(10) + DOT_VALUE + JPG.getFormatName()).
                masterSellerId(new Random().nextInt(100) + 1).
                externalOfferId(secondExternalOfferId);
        retailerMediaImportStepDef.sendMediaFile(retailerMediaImportStepDef.buildImportOneMediaRequest(importParameters));

        metadataStepDef.successMetadataSearching(
                Collections.singletonMap(EXTERNAL_OFFER_IDS.getName(), Arrays.asList(firstExternalOfferId, secondExternalOfferId)), 2);
    }

    @Test(description = "Фильтрация по массиву external_offer_ids - null", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"254806"})
    public void successFiltrationByNullExternalOfferIdsTest() {
        List<DataItem> dataItems = metadataStepDef.successMetadataSearching(EXTERNAL_OFFER_IDS.getName(), null);
        List<String> actualIdList = dataItems.stream().map(DataItem::getId).collect(Collectors.toList());

        Assert.compareParameters(idList, actualIdList, "ids");
    }

    @Test(description = "Фильтрация по массиву external_offer_ids - неверный тип", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"254806"})
    public void unsuccessfulFiltrationInvalidTypeExternalOfferIdsTest() {
        List<InvalidParamsItem> invalidParams = metadataStepDef.unsuccessfulMetadataSearching(
                EXTERNAL_OFFER_IDS.getName(), RandomStringUtils.randomAlphabetic(10));
        getData().invalidParams().get(2).name(EXTERNAL_OFFER_IDS.getName());
        assertAll(
                () -> compareParameters(1, invalidParams.size(), "invalid_params.size"),
                () -> compareParameters(getData().invalidParams().get(2), invalidParams.get(0), "invalid_params")
        );
    }

    @Test(description = "Фильтрация по полю created_at_to", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"262559"})
    public void successFiltrationByCreatedAtToTest() {
        String createdAtToDate = OffsetDateTime.now().format(ISO_INSTANT);
        metadataStepDef.createMetadataImage(new ImageData(ImageFormat.JPEG));

        List<DataItem> dataItems = metadataStepDef.successMetadataSearching(CREATED_AT_TO.getName(), createdAtToDate);
        List<String> actualIdList = dataItems.stream().map(DataItem::getId).collect(Collectors.toList());

        Assert.compareParameters(idList, actualIdList, "ids");
    }

    @Test(description = "Фильтрация по полю created_at_to - невалидный тип", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"262559"})
    public void unsuccessfulFiltrationByCreatedAtToInvalidTypeTest() {
        List<InvalidParamsItem> invalidParams = metadataStepDef.unsuccessfulMetadataSearching(CREATED_AT_TO.getName(), new Random().nextInt(100));
        getData().invalidParams().get(2).name(CREATED_AT_TO.getName());
        assertAll(
                () -> compareParameters(1, invalidParams.size(), "invalid_params.size"),
                () -> compareParameters(getData().invalidParams().get(2), invalidParams.get(0), "invalid_params")
        );
    }

    @Test(description = "Фильтрация по полю created_at_to - невалидный тип (неверный формат даты)", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"262559"})
    public void unsuccessfulFiltrationByCreatedAtToInvalidFormatTest() {
        String createdAtToDate = OffsetDateTime.now().format(ISO_LOCAL_DATE);
        List<InvalidParamsItem> invalidParams = metadataStepDef.unsuccessfulMetadataSearching(CREATED_AT_TO.getName(), createdAtToDate);
        getData().invalidParams().get(2).name(CREATED_AT_TO.getName());
        assertAll(
                () -> compareParameters(1, invalidParams.size(), "invalid_params.size"),
                () -> compareParameters(getData().invalidParams().get(2), invalidParams.get(0), "invalid_params")
        );
    }

    @Test(description = "Фильтрация по полю created_at_from", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"262560"})
    public void successFiltrationByCreatedAtFromTest() {
        String createdAtFromDate = OffsetDateTime.now().format(ISO_INSTANT);
        List<String> idList = Collections.singletonList(metadataStepDef.createMetadataImage(new ImageData(ImageFormat.JPEG)));

        List<DataItem> dataItems = metadataStepDef.successMetadataSearching(CREATED_AT_FROM.getName(), createdAtFromDate);
        List<String> actualIdList = dataItems.stream().map(DataItem::getId).collect(Collectors.toList());

        Assert.compareParameters(idList, actualIdList, "ids");
    }

    @Test(description = "Фильтрация по полю created_at_from - невалидный тип", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"262560"})
    public void unsuccessfulFiltrationByCreatedAtFromInvalidTypeTest() {
        List<InvalidParamsItem> invalidParams = metadataStepDef.unsuccessfulMetadataSearching(CREATED_AT_FROM.getName(), new Random().nextInt(100));
        getData().invalidParams().get(2).name(CREATED_AT_FROM.getName());
        assertAll(
                () -> compareParameters(1, invalidParams.size(), "invalid_params.size"),
                () -> compareParameters(getData().invalidParams().get(2), invalidParams.get(0), "invalid_params")
        );
    }

    @Test(description = "Фильтрация по полю created_at_from - невалидный тип (неверный формат даты)", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"262560"})
    public void unsuccessfulFiltrationByCreatedAtFromInvalidFormatTest() {
        String createdAtToDate = OffsetDateTime.now().format(ISO_LOCAL_DATE);
        List<InvalidParamsItem> invalidParams = metadataStepDef.unsuccessfulMetadataSearching(CREATED_AT_FROM.getName(), createdAtToDate);
        getData().invalidParams().get(2).name(CREATED_AT_FROM.getName());
        assertAll(
                () -> compareParameters(1, invalidParams.size(), "invalid_params.size"),
                () -> compareParameters(getData().invalidParams().get(2), invalidParams.get(0), "invalid_params")
        );
    }

    @Test(description = "Фильтрация по полю created_at_from и created_at_to", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"262561"})
    public void successFiltrationByCreatedAtFromToTest() {
        String createdAtFromDate = OffsetDateTime.now().format(ISO_INSTANT);
        List<String> idList = Collections.singletonList(metadataStepDef.createMetadataImage(new ImageData(ImageFormat.JPEG)));
        String createdAtToDate = OffsetDateTime.now().format(ISO_INSTANT);
        metadataStepDef.createMetadataImage(new ImageData(ImageFormat.JPEG));

        Map<String, Object> expQueryParam = new HashMap<>();
        expQueryParam.put(CREATED_AT_FROM.getName(), createdAtFromDate);
        expQueryParam.put(CREATED_AT_TO.getName(), createdAtToDate);

        List<DataItem> dataItems = metadataStepDef.successMetadataSearching(expQueryParam, false);
        List<String> actualIdList = dataItems.stream().map(DataItem::getId).collect(Collectors.toList());

        Assert.compareParameters(idList, actualIdList, "ids");
    }

    @Test(description = "Фильтрация по полю updated_at_to", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"262571"})
    public void successFiltrationByUpdatedAtToTest() {
        metadataStepDef.successEditMetadata(idList.get(1), PRIORITY.getName(), new Random().nextInt(FOUR_HUNDRED_NINETY_NINE) + 1);

        String updatedAtToDate = OffsetDateTime.now().format(ISO_INSTANT);
        metadataStepDef.createMetadataImage(new ImageData(ImageFormat.JPEG));

        List<DataItem> dataItems = metadataStepDef.successMetadataSearching(UPDATED_AT_TO.getName(), updatedAtToDate);
        List<String> actualIdList = dataItems.stream().map(DataItem::getId).collect(Collectors.toList());

        Assert.compareParameters(idList, actualIdList, "ids");
    }

    @Test(description = "Фильтрация по полю updated_at_to - невалидный тип", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"262571"})
    public void unsuccessfulFiltrationByUpdatedAtToInvalidTypeTest() {
        List<InvalidParamsItem> invalidParams = metadataStepDef.unsuccessfulMetadataSearching(UPDATED_AT_TO.getName(), new Random().nextInt(100));
        getData().invalidParams().get(2).name(UPDATED_AT_TO.getName());
        assertAll(
                () -> compareParameters(1, invalidParams.size(), "invalid_params.size"),
                () -> compareParameters(getData().invalidParams().get(2), invalidParams.get(0), "invalid_params")
        );
    }

    @Test(description = "Фильтрация по полю updated_at_to - невалидный тип (неверный формат даты)", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"262571"})
    public void unsuccessfulFiltrationByUpdatedAtToInvalidFormatTest() {
        String createdAtToDate = OffsetDateTime.now().format(ISO_LOCAL_DATE);
        List<InvalidParamsItem> invalidParams = metadataStepDef.unsuccessfulMetadataSearching(UPDATED_AT_TO.getName(), createdAtToDate);
        getData().invalidParams().get(2).name(UPDATED_AT_TO.getName());
        assertAll(
                () -> compareParameters(1, invalidParams.size(), "invalid_params.size"),
                () -> compareParameters(getData().invalidParams().get(2), invalidParams.get(0), "invalid_params")
        );
    }

    @Test(description = "Фильтрация по полю updated_at_from", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"262572"})
    public void successFiltrationByUpdatedAtFromTest() {
        String updatedAtFromDate = OffsetDateTime.now().format(ISO_INSTANT);
        metadataStepDef.successEditMetadata(idList.get(1), PRIORITY.getName(), new Random().nextInt(FOUR_HUNDRED_NINETY_NINE) + 1);

        String addImageId = metadataStepDef.createMetadataImage(new ImageData(ImageFormat.JPEG));

        List<DataItem> dataItems = metadataStepDef.successMetadataSearching(UPDATED_AT_FROM.getName(), updatedAtFromDate);
        List<String> actualIdList = dataItems.stream().map(DataItem::getId).collect(Collectors.toList());

        Assert.compareParameters(Arrays.asList(idList.get(1), addImageId), actualIdList, "ids");
    }

    @Test(description = "Фильтрация по полю updated_at_from - невалидный тип", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"262572"})
    public void unsuccessfulFiltrationByUpdatedAtFromInvalidTypeTest() {
        List<InvalidParamsItem> invalidParams = metadataStepDef.unsuccessfulMetadataSearching(UPDATED_AT_FROM.getName(), new Random().nextInt(100));
        getData().invalidParams().get(2).name(UPDATED_AT_FROM.getName());
        assertAll(
                () -> compareParameters(1, invalidParams.size(), "invalid_params.size"),
                () -> compareParameters(getData().invalidParams().get(2), invalidParams.get(0), "invalid_params")
        );
    }

    @Test(description = "Фильтрация по полю updated_at_from - невалидный тип (неверный формат даты)", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"262572"})
    public void unsuccessfulFiltrationByUpdatedAtFromInvalidFormatTest() {
        String createdAtToDate = OffsetDateTime.now().format(ISO_LOCAL_DATE);
        List<InvalidParamsItem> invalidParams = metadataStepDef.unsuccessfulMetadataSearching(UPDATED_AT_FROM.getName(), createdAtToDate);
        getData().invalidParams().get(2).name(UPDATED_AT_FROM.getName());
        assertAll(
                () -> compareParameters(1, invalidParams.size(), "invalid_params.size"),
                () -> compareParameters(getData().invalidParams().get(2), invalidParams.get(0), "invalid_params")
        );
    }

    @Test(description = "Фильтрация по полям updated_at_from и updated_at_to", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"262574"})
    public void successFiltrationByUpdatedAtFromToTest() {
        String updatedAtFromDate = OffsetDateTime.now().format(ISO_INSTANT);

        metadataStepDef.successEditMetadata(idList.get(1), PRIORITY.getName(), new Random().nextInt(FOUR_HUNDRED_NINETY_NINE) + 1);
        String addImageId = metadataStepDef.createMetadataImage(new ImageData(ImageFormat.JPEG));

        String updatedAtToDate = OffsetDateTime.now().format(ISO_INSTANT);

        metadataStepDef.createMetadataImage(new ImageData(ImageFormat.JPEG));

        Map<String, Object> expQueryParam = new HashMap<>();
        expQueryParam.put(UPDATED_AT_FROM.getName(), updatedAtFromDate);
        expQueryParam.put(UPDATED_AT_TO.getName(), updatedAtToDate);

        List<DataItem> dataItems = metadataStepDef.successMetadataSearching(expQueryParam, false);
        List<String> actualIdList = dataItems.stream().map(DataItem::getId).collect(Collectors.toList());

        Assert.compareParameters(Arrays.asList(idList.get(1), addImageId), actualIdList, "ids");
    }

    @Test(description = "Фильтрация по полю ready_for_retouch_at_to", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"290027"})
    public void successFiltrationByReadyForRetouchAtToTest() {
        List<DataItem> expDataItems = createFilesWithNowReadyForRetouchAt();
        List<String> expIdList = expDataItems.stream().map(DataItem::getId).collect(Collectors.toList());

        List<DataItem> actualDataItems = metadataStepDef.successMetadataSearching(READY_FOR_RETOUCH_AT_TO.getName(), OffsetDateTime.now().plusDays(2).format(ISO_INSTANT));
        List<String> actualIdList = actualDataItems.stream().map(DataItem::getId).collect(Collectors.toList());

        Assert.compareParameters(expIdList, actualIdList, "ids");
    }

    @Test(description = "Фильтрация по полю ready_for_retouch_at_to - не найдены", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"290027"})
    public void successFiltrationNoByReadyForRetouchAtToTest() {
        createFilesWithNowReadyForRetouchAt();
        metadataStepDef.successMetadataSearching(READY_FOR_RETOUCH_AT_TO.getName(), OffsetDateTime.now().minusDays(2).format(ISO_INSTANT), true);
    }

    @Test(description = "Фильтрация по полю ready_for_retouch_at_to - невалидный тип (неверный формат даты)", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"290027"})
    public void unsuccessfulFiltrationByReadyForRetouchAtToTest() {
        String createdAtToDate = OffsetDateTime.now().format(ISO_LOCAL_DATE);
        List<InvalidParamsItem> invalidParams = metadataStepDef.unsuccessfulMetadataSearching(READY_FOR_RETOUCH_AT_TO.getName(), createdAtToDate);
        getData().invalidParams().get(2).name(READY_FOR_RETOUCH_AT_TO.getName());
        assertAll(
                () -> compareParameters(1, invalidParams.size(), "invalid_params.size"),
                () -> compareParameters(getData().invalidParams().get(2), invalidParams.get(0), "invalid_params")
        );
    }

    @Test(description = "Фильтрация по полю ready_for_retouch_at_from", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"290028"})
    public void successFiltrationByReadyForRetouchAtFromTest() {
        List<DataItem> expDataItems = createFilesWithNowReadyForRetouchAt();
        List<String> expIdList = expDataItems.stream().map(DataItem::getId).collect(Collectors.toList());

        List<DataItem> actualDataItems = metadataStepDef.successMetadataSearching(READY_FOR_RETOUCH_AT_FROM.getName(), OffsetDateTime.now().minusDays(2).format(ISO_INSTANT));
        List<String> actualIdList = actualDataItems.stream().map(DataItem::getId).collect(Collectors.toList());

        Assert.compareParameters(expIdList, actualIdList, "ids");
    }

    @Test(description = "Фильтрация по полю ready_for_retouch_at_from - не найдены", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"290028"})
    public void successFiltrationNoByReadyForRetouchAtFromTest() {
        createFilesWithNowReadyForRetouchAt();
        metadataStepDef.successMetadataSearching(READY_FOR_RETOUCH_AT_FROM.getName(), OffsetDateTime.now().plusDays(2).format(ISO_INSTANT), true);
    }

    @Test(description = "Фильтрация по полю ready_for_retouch_at_from - невалидный тип", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"290028"})
    public void unsuccessfulFiltrationByReadyForRetouchAtFromInvalidTypeTest() {
        List<InvalidParamsItem> invalidParams = metadataStepDef.unsuccessfulMetadataSearching(READY_FOR_RETOUCH_AT_FROM.getName(), new Random().nextInt(100));
        getData().invalidParams().get(2).name(READY_FOR_RETOUCH_AT_FROM.getName());
        assertAll(
                () -> compareParameters(1, invalidParams.size(), "invalid_params.size"),
                () -> compareParameters(getData().invalidParams().get(2), invalidParams.get(0), "invalid_params")
        );
    }

    @Test(description = "Фильтрация по полю ready_for_retouch_at_from и ready_for_retouch_at_to", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"290029"})
    public void successFiltrationByReadyForRetouchAtFromAndToTest() {
        List<DataItem> expDataItems = createFilesWithNowReadyForRetouchAt();
        List<String> expIdList = expDataItems.stream().map(DataItem::getId).collect(Collectors.toList());

        Map<String, Object> expQueryParam = new HashMap<>();
        expQueryParam.put(READY_FOR_RETOUCH_AT_TO.getName(), OffsetDateTime.now().plusDays(2).format(ISO_INSTANT));
        expQueryParam.put(READY_FOR_RETOUCH_AT_FROM.getName(), OffsetDateTime.now().minusDays(2).format(ISO_INSTANT));

        List<DataItem> actualDataItems = metadataStepDef.successMetadataSearching(expQueryParam, false);
        List<String> actualIdList = actualDataItems.stream().map(DataItem::getId).collect(Collectors.toList());

        Assert.compareParameters(expIdList, actualIdList, "ids");
    }

    @Step("Создаем файлы с заполненным ready_for_retouch_at = сейчас")
    private List<DataItem> createFilesWithNowReadyForRetouchAt() {
        RetailerMediaImportSettings.ImportParameters importParameters = getData().importParameters().clone().
                mediaName(RandomStringUtils.randomAlphabetic(10) + DOT_VALUE + JPG.getFormatName()).
                masterSellerId(new Random().nextInt(100) + 1).externalOfferId(String.valueOf(new Random().nextInt(1000000) + 1000000));
        retailerMediaImportStepDef.sendMediaFile(retailerMediaImportStepDef.buildImportOneMediaRequest(importParameters));

        AtomicReference<List<DataItem>> dataItemsAtomic = new AtomicReference<>();
        Awaitility.await().atMost(10, TimeUnit.SECONDS).pollInterval(1, TimeUnit.SECONDS).until(() ->
        {
            List<DataItem> dataItems = metadataStepDef.successMetadataSearch(importParameters.externalOfferId());
            dataItemsAtomic.set(dataItems);
            return dataItems.size() == 2;
        });

        String metadataRequest = retailerMediaImportStepDef.buildImportModerationMetadataRequest(
                Integer.parseInt(importParameters.externalOfferId()), importParameters.masterSellerId(),
                OfferProcessTypeEnum.FINALYZE_TASK, OffsetDateTime.now().format(ISO_INSTANT), getData().importMetadata());
        retailerMediaImportStepDef.sendMetadata(metadataRequest);

        dataItemsAtomic.get().stream().map(DataItem::getId).forEach(id ->
                Awaitility.await().atMost(10, TimeUnit.SECONDS).pollInterval(1, TimeUnit.SECONDS).until(() ->
                {
                    Response response = metadataStepDef.checkMetadata(id);
                    Object readyForRetouchAt = getValueFromResponse(response, READY_FOR_RETOUCH_AT.getPath());
                    return readyForRetouchAt != null;
                }));
        return dataItemsAtomic.get();
    }

    @Test(description = "Фильтрация по нескольким полям", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"230083"})
    public void successFiltrationBySomeFieldsTest() {
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

        List<DataItem> dataItems = metadataStepDef.successMetadataSearching(expQueryParam, false);
        List<String> actualIdList = dataItems.stream().map(DataItem::getId).collect(Collectors.toList());

        Assert.compareParameters(idList, actualIdList, "ids");
    }

    @Test(description = "Фильтрация по нескольким полям с ошибкой", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"230083"})
    public void unsuccessfulFiltrationBySomeFieldsTest() {
        Map<String, Object> expQueryParam = new HashMap<>();
        expQueryParam.put(SOURCE_ID.getName(), Collections.singletonList(UUID.randomUUID()));
        expQueryParam.put(MASTER_CATEGORY_IDS.getName(), Collections.singletonList(UUID.randomUUID()));
        expQueryParam.put(QUALITY_IDS.getName(), Collections.singletonList(UUID.randomUUID()));
        expQueryParam.put(IS_OWN_TRADEMARK.getName(), UUID.randomUUID());

        List<InvalidParamsItem> invalidParams = metadataStepDef.unsuccessfulMetadataSearching(expQueryParam);
        getData().invalidParams().get(2).name(IS_OWN_TRADEMARK.getName());
        assertAll(
                () -> compareParameters(1, invalidParams.size(), "invalid_params.size"),
                () -> compareParameters(getData().invalidParams().get(2), invalidParams.get(0), "invalid_params")
        );
    }
}
