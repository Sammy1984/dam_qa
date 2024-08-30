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
import ru.spice.at.api.dto.response.metadata.DictionariesItem;
import ru.spice.at.api.dto.response.metadata.InvalidParamsItem;
import ru.spice.at.api.dto.response.metadata.UsersItem;
import ru.spice.at.api.metadata_service.MetadataSettings;
import ru.spice.at.api.metadata_service.MetadataStepDef;
import ru.spice.at.api.retailer_media_import_service.RetailerMediaImportSettings;
import ru.spice.at.api.retailer_media_import_service.RetailerMediaImportStepDef;
import ru.spice.at.common.base_test.BaseApiTest;
import ru.spice.at.common.dto.dam.ImageData;
import ru.spice.at.common.emuns.ApiServices;
import ru.spice.at.common.emuns.dam.ImageFormat;
import ru.spice.at.common.utils.Assert;

import java.util.*;

import static ru.spice.at.common.constants.TestConstants.*;
import static ru.spice.at.common.emuns.dam.ImageFormat.JPG;
import static ru.spice.at.common.emuns.dam.ImageParameters.*;
import static ru.spice.at.common.emuns.dam.Quality.BAD;
import static ru.spice.at.common.emuns.dam.SearchParameters.SEARCH;
import static ru.spice.at.common.emuns.dam.Status.IN_PROGRESS;
import static ru.spice.at.common.utils.Assert.assertAll;
import static ru.spice.at.common.utils.Assert.compareParameters;

@Feature("Metadata Service")
@Story("POST metadata selection search data")
public class MetadataPostSelectionSearchTests extends BaseApiTest<MetadataSettings> {
    private final static int COUNT_METADATA = 4;
    private final MetadataStepDef metadataStepDef;
    private final RetailerMediaImportStepDef retailerMediaImportStepDef;
    private final List<String> idList = new ArrayList<>();
    private final List<ImageData> imageList = new ArrayList<>();

    protected MetadataPostSelectionSearchTests() {
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

    @Test(description = "Выбор результатов поиска по несуществующему значению", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"263247"})
    public void successSelectionWithSearchByNotExistValueTest() {
        metadataStepDef.successMetadataSelectionSearch(RandomStringUtils.randomAlphabetic(20), true);
    }

    @Test(description = "Выбор результатов поиска - неверный тип", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"263267"})
    public void unsuccessfulSelectionWithSearchByInvalidTypeValueTest() {
        List<InvalidParamsItem> invalidParams = metadataStepDef.unsuccessfulMetadataSelection(
                Collections.singletonMap(SEARCH.getName(), Collections.singletonList(UUID.randomUUID().toString())));
        getData().invalidParams().get(2).name(SEARCH.getName());
        assertAll(
                () -> compareParameters(1, invalidParams.size(), "invalid_params.size"),
                () -> compareParameters(getData().invalidParams().get(2), invalidParams.get(0), "invalid_params")
        );
    }

    @Test(description = "Выбор результатов поиска по filename - по одному значению", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"263250"})
    public void successSelectionWithSearchByFilenameTest() {
        List<String> actualIds = metadataStepDef.successMetadataSelectionSearch(imageList.get(0).getFilename().split("\\.", 2)[0]);
        Assert.compareParameters(Collections.singletonList(idList.get(0)), actualIds, "ids");
    }

    @Test(description = "Выбор результатов поиска по filename - по нескольким значениям", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"263250"})
    public void successSelectionWithSearchByArrayFilenameTest() {
        StringBuilder searchValue = new StringBuilder(EMPTY_VALUE);
        idList.remove(idList.size() - 1);
        imageList.remove(imageList.size() - 1);
        for (ImageData image : imageList) {
            String filename = image.getFilename().split("\\.", 2)[0];
            searchValue.append(filename).append(SPACE_VALUE);
        }

        List<String> actualIds = metadataStepDef.successMetadataSelectionSearch(searchValue.toString());
        Assert.compareParameters(idList, actualIds, "ids");
    }

    @Test(description = "Выбор результатов поиска по format - по одному значению", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"263251"})
    public void successSelectionWithSearchByFormatTest() {
        metadataStepDef.createMetadataImage(new ImageData(ImageFormat.PNG));

        List<String> actualIds = metadataStepDef.successMetadataSelectionSearch(ImageFormat.JPEG.getFormatName());
        Assert.compareParameters(idList, actualIds, "ids");
    }

    @Test(description = "Выбор результатов поиска по origin_name - по одному значению", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"263252"})
    public void successSelectionWithSearchByOriginNameTest() {
        idList.forEach(id ->
                metadataStepDef.successEditMetadata(id, FILENAME.getName(), RandomStringUtils.randomAlphabetic(8) + DOT_VALUE + ImageFormat.JPEG));

        List<String> actualIds = metadataStepDef.successMetadataSelectionSearch(imageList.get(0).getFilename().split("\\.", 2)[0]);
        Assert.compareParameters(Collections.singletonList(idList.get(0)), actualIds, "ids");
    }

    @Test(description = "Выбор результатов поиска по description - по одному значению", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"263253"})
    public void successSelectionWithSearchByDescriptionTest() {
        String searchValue = RandomStringUtils.randomAlphabetic(8);
        String expId = idList.get(idList.size() - 1);
        metadataStepDef.successEditMetadata(expId, DESCRIPTION.getName(), searchValue);

        idList.remove(idList.size() - 1);
        idList.forEach(id -> metadataStepDef.successEditMetadata(id, DESCRIPTION.getName(), RandomStringUtils.randomAlphabetic(10)));

        List<String> actualIds = metadataStepDef.successMetadataSelectionSearch(searchValue);
        Assert.compareParameters(Collections.singletonList(expId), actualIds, "ids");
    }

    @Test(description = "Выбор результатов поиска по keywords - по одному значению", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"263254"})
    public void successSelectionWithSearchByKeywordsTest() {
        String searchValue = RandomStringUtils.randomAlphabetic(8);

        RequestMultiEditData requestMultiEditData = RequestMultiEditData.builder()
                .ids(idList).keywords(Collections.singletonList(searchValue)).build();
        metadataStepDef.successMultiEditMetadata(requestMultiEditData);
        metadataStepDef.successEditMetadata(
                idList.get(idList.size() - 1), KEYWORDS.getName(), Collections.singletonList(RandomStringUtils.randomAlphabetic(10)));
        idList.remove(idList.size() - 1);

        List<String> actualIds = metadataStepDef.successMetadataSelectionSearch(searchValue);
        Assert.compareParameters(idList, actualIds, "ids");
    }

    @Test(description = "Выбор результатов поиска по received (получено от) - по одному значению", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"263255"})
    public void successSelectionWithSearchByReceivedTest() {
        String searchValue = RandomStringUtils.randomAlphabetic(8);

        RequestMultiEditData requestMultiEditData = RequestMultiEditData.builder()
                .ids(idList).received(searchValue).build();
        metadataStepDef.successMultiEditMetadata(requestMultiEditData);
        metadataStepDef.successEditMetadata(
                idList.get(idList.size() - 1), RECEIVED.getName(), RandomStringUtils.randomAlphabetic(10));
        idList.remove(idList.size() - 1);

        List<String> actualIds = metadataStepDef.successMetadataSelectionSearch(searchValue);
        Assert.compareParameters(idList, actualIds, "ids");
    }

    @Test(description = "Выбор результатов поиска по assignee = по одному значению", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"263256"})
    public void successSelectionWithSearchByAssigneeTest() {
        List<UsersItem> usersItems = metadataStepDef.getListUsersMetadata();

        String expAssigneeId = usersItems.get(0).id();
        String expAssigneeName = usersItems.get(0).fullName();

        idList.remove(idList.size() - 1);
        RequestMultiEditData requestMultiEditData = RequestMultiEditData.builder()
                .ids(idList).assigneeId(expAssigneeId).build();
        metadataStepDef.successMultiEditMetadata(requestMultiEditData);

        List<String> actualIds = metadataStepDef.successMetadataSelectionSearch(expAssigneeName);
        Assert.compareParameters(idList, actualIds, "ids");
    }

    @Test(description = "Выбор результатов поиска по assignee = по части значения (только по имени/фамилии)", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"263256"})
    public void successSelectionWithSearchByPartValueAssigneeTest() {
        List<UsersItem> usersItems = metadataStepDef.getListUsersMetadata();

        String expAssigneeId = usersItems.get(0).id();
        String expAssigneeName = usersItems.get(0).fullName().split(SPACE_VALUE)[0];

        idList.remove(idList.size() - 1);
        RequestMultiEditData requestMultiEditData = RequestMultiEditData.builder()
                .ids(idList).assigneeId(expAssigneeId).build();
        metadataStepDef.successMultiEditMetadata(requestMultiEditData);

        List<String> actualIds = metadataStepDef.successMetadataSelectionSearch(expAssigneeName);
        Assert.compareParameters(idList, actualIds, "ids");
    }

    @Test(description = "Выбор результатов поиска по status_id - по одному значению", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"263257"})
    public void successSelectionWithSearchByStatusTest() {
        DictionariesItem statusItem = metadataStepDef.getListStatusesMetadata().
                stream().filter(s -> s.name().equals(IN_PROGRESS.getName())).findFirst().orElseThrow(() -> new RuntimeException("Не удалось найти статус"));

        idList.remove(idList.size() - 1);
        RequestMultiEditData requestMultiEditData = RequestMultiEditData.builder()
                .ids(idList).statusId(statusItem.id()).build();
        metadataStepDef.successMultiEditMetadata(requestMultiEditData);

        List<String> actualIds = metadataStepDef.successMetadataSelectionSearch(statusItem.name());
        Assert.compareParameters(idList, actualIds, "ids");
    }

    @Test(description = "Выбор результатов поиска по sku - по одному значению", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"263258"})
    public void successSelectionWithSearchBySkuTest() {
        String searchValue = RandomStringUtils.randomAlphabetic(6);
        metadataStepDef.successEditMetadata(idList.get(0), SKU.getName(), searchValue);

        List<String> actualIds = metadataStepDef.successMetadataSelectionSearch(searchValue);
        Assert.compareParameters(Collections.singletonList(idList.get(0)), actualIds, "ids");
    }

    @Test(description = "Выбор результатов поиска по sku - по нескольким значениям", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"263258"})
    public void successSelectionWithSearchByArraySkuTest() {
        String firstSearchValue = RandomStringUtils.randomAlphabetic(10);
        metadataStepDef.successEditMetadata(idList.get(0), SKU.getName(), firstSearchValue);

        String secondSearchValue = RandomStringUtils.randomAlphabetic(8);
        metadataStepDef.successEditMetadata(idList.get(1), SKU.getName(), secondSearchValue);

        List<String> actualIds = metadataStepDef.successMetadataSelectionSearch(firstSearchValue + SPACE_VALUE + secondSearchValue);
        Assert.compareParameters(Arrays.asList(idList.get(0), idList.get(1)), actualIds, "ids");
    }

    @Test(description = "Выбор результатов поиска по source_id - по одному значению", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"263259"})
    public void successSelectionWithSearchBySourceTest() {
        List<DictionariesItem> sourceItems = metadataStepDef.getListSourcesMetadata();
        String expSourceId = sourceItems.get(0).id();
        String expSourceName = sourceItems.get(0).name();

        idList.remove(idList.size() - 1);
        RequestMultiEditData requestMultiEditData = RequestMultiEditData.builder()
                .ids(idList).sourceId(expSourceId).build();
        metadataStepDef.successMultiEditMetadata(requestMultiEditData);

        List<String> actualIds = metadataStepDef.successMetadataSelectionSearch(expSourceName);
        Assert.compareParameters(idList, actualIds, "ids");
    }

    @Test(description = "Выбор результатов поиска по quality_id - по одному значению", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"263260"})
    public void successSelectionWithSearchByQualityTest() {
        DictionariesItem qualityItem = metadataStepDef.getListQualitiesMetadata().stream().
                filter(q -> q.name().equals(BAD.getName())).findFirst().orElseThrow(() -> new RuntimeException("Качество не найдено"));

        idList.remove(idList.size() - 1);
        RequestMultiEditData requestMultiEditData = RequestMultiEditData.builder()
                .ids(idList).qualityId(qualityItem.id()).build();
        metadataStepDef.successMultiEditMetadata(requestMultiEditData);

        List<String> actualIds = metadataStepDef.successMetadataSelectionSearch(qualityItem.name());
        Assert.compareParameters(idList, actualIds, "ids");
    }

    @Test(description = "Выбор результатов поиска по category_id - по одному значению", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"263261"})
    public void successSelectionWithSearchByCategoryTest() {
        List<DictionariesItem> categoryItems = metadataStepDef.getListCategoriesMetadata();
        String expCategoryId = categoryItems.get(0).id();
        String expCategoryName = categoryItems.get(0).name();

        idList.remove(idList.size() - 1);
        RequestMultiEditData requestMultiEditData = RequestMultiEditData.builder()
                .ids(idList).masterCategoryId(expCategoryId).build();
        metadataStepDef.successMultiEditMetadata(requestMultiEditData);

        List<String> actualIds = metadataStepDef.successMetadataSelectionSearch(expCategoryName);
        Assert.compareParameters(idList, actualIds, "ids");
    }

    @Test(description = "Выбор результатов поиска по priority - по одному значению", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"263262"})
    public void successSelectionWithSearchByPriorityTest() {
        int priority = new Random().nextInt(FOUR_HUNDRED_NINETY_NINE) + 1;

        idList.remove(idList.size() - 1);
        for (String id : idList) {
            metadataStepDef.successEditMetadata(id, PRIORITY.getName(), priority);
        }

        List<String> actualIds = metadataStepDef.successMetadataSelectionSearch(String.valueOf(priority));
        Assert.compareParameters(idList, actualIds, "ids");
    }

    @Test(description = "Выбор результатов поиска по external_task_id - по одному значению", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"263263"})
    public void successSelectionWithSearchByExternalTaskIdTest() {
        String searchValue = String.valueOf(new Random().nextInt(ONE_THOUSAND));

        idList.remove(idList.size() - 1);
        for (String id : idList) {
            metadataStepDef.successEditMetadata(id, EXTERNAL_TASK_ID.getName(), searchValue);
        }

        List<String> actualIds = metadataStepDef.successMetadataSelectionSearch(searchValue);
        Assert.compareParameters(idList, actualIds, "ids");
    }

    @Test(description = "Выбор результатов поиска по external_offer_id", timeOut = 600000, groups = {"regress", "kafka"})
    @WorkItemIds({"263264"})
    public void successSelectionWithSearchByExternalOfferIdTest() {
        Integer externalOfferId = new Random().nextInt(1000000) + 1000000;
        Integer masterSellerId = new Random().nextInt(100) + 1;

        RetailerMediaImportSettings.ImportParameters importParameters = getData().importParameters().clone().
                mediaName(RandomStringUtils.randomAlphabetic(10) + DOT_VALUE + JPG.getFormatName()).
                masterSellerId(masterSellerId).externalOfferId(externalOfferId.toString());
        retailerMediaImportStepDef.sendMediaFile(retailerMediaImportStepDef.buildImportOneMediaRequest(importParameters));

        List<String> actualIds = metadataStepDef.successMetadataSelectionSearch(externalOfferId);
        Assert.compareParameters(2, actualIds.size(), "ids");
    }

    @Test(description = "Выбор результатов поиска нескольких файлов по разным параметрам", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"263265"})
    public void successSelectionWithSearchBySomeParametersTest() {
        String expFilename = imageList.get(0).getFilename().split("\\.", 2)[0];

        String expDescription = RandomStringUtils.randomAlphabetic(8);
        metadataStepDef.successEditMetadata(idList.get(1), DESCRIPTION.getName(), expDescription);

        List<DictionariesItem> sourceItems = metadataStepDef.getListSourcesMetadata();
        metadataStepDef.successEditMetadata(idList.get(2), SOURCE_ID.getName(), sourceItems.get(0).id());
        String expSourceName = sourceItems.get(0).name();

        idList.remove(idList.size() - 1);

        List<String> actualIds = metadataStepDef.successMetadataSelectionSearch(expFilename + SPACE_VALUE + expDescription + SPACE_VALUE + expSourceName);
        Assert.compareParameters(idList, actualIds, "ids");
    }
}
