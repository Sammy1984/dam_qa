package ru.spice.at.api.metadata_service.test;

import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import org.awaitility.Awaitility;
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
import ru.spice.at.api.dto.response.metadata.filtration.DataItem;
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
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static ru.spice.at.common.constants.TestConstants.*;
import static ru.spice.at.common.emuns.dam.ImageFormat.JPG;
import static ru.spice.at.common.emuns.dam.ImageParameters.*;
import static ru.spice.at.common.emuns.dam.ImageParameters.FILENAME;
import static ru.spice.at.common.emuns.dam.Quality.BAD;
import static ru.spice.at.common.emuns.dam.Quality.TO_REVISION;
import static ru.spice.at.common.emuns.dam.SearchParameters.LIMIT;
import static ru.spice.at.common.emuns.dam.SearchParameters.SEARCH;
import static ru.spice.at.common.emuns.dam.Status.ARCHIVE;
import static ru.spice.at.common.emuns.dam.Status.IN_PROGRESS;
import static ru.spice.at.common.utils.Assert.assertAll;
import static ru.spice.at.common.utils.Assert.compareParameters;

@Feature("Metadata Service")
@Story("POST metadata searching search data")
public class MetadataPostSearchingSearchTests extends BaseApiTest<MetadataSettings> {
    private final static int COUNT_METADATA = 4;
    private final MetadataStepDef metadataStepDef;
    private final RetailerMediaImportStepDef retailerMediaImportStepDef;
    private final List<String> idList = new ArrayList<>();
    private final List<ImageData> imageList = new ArrayList<>();

    protected MetadataPostSearchingSearchTests() {
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

    @Test(description = "Поиск по несуществующему значению", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"230091"})
    public void successSearchByNotExistValueTest() {
        metadataStepDef.successMetadataSearch(RandomStringUtils.randomAlphabetic(20), true);
    }

    @Test(description = "Поиск по несуществующему значению со спецсимволами", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"230091"})
    public void successSearchByNotExistValueWithSpecialSymbolsTest() {
        metadataStepDef.successMetadataSearch(RandomStringUtils.randomAscii(20), true);
    }

    @Test(description = "Поиск по null", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"230092"})
    public void successSearchByNullValueTest() {
        List<DataItem> dataItems = metadataStepDef.successMetadataSearch(null);
        List<String> actualIdList = dataItems.stream().map(DataItem::getId).collect(Collectors.toList());

        Assert.compareParameters(idList, actualIdList, "ids");
    }

    @Test(description = "Поиск по пробелу", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"230093"})
    public void successSearchBySpaceValueTest() {
        metadataStepDef.successMetadataSearch(SPACE_VALUE, true);
    }

    @Test(description = "Поиск с ограничением limit", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"255205"})
    public void successSearchWithLimitTest() {
        String searchValue = RandomStringUtils.randomAlphabetic(8);

        RequestMultiEditData requestMultiEditData = RequestMultiEditData.builder()
                .ids(idList).keywords(Collections.singletonList(searchValue)).build();
        metadataStepDef.successMultiEditMetadata(requestMultiEditData);

        Map<String, Object> params = new HashMap<>();
        params.put(SEARCH.getName(), searchValue);
        params.put(LIMIT.getName(), idList.size() - 1);

        List<DataItem> dataItems = metadataStepDef.successMetadataSearching(params, false);
        List<String> actualIdList = dataItems.stream().map(DataItem::getId).collect(Collectors.toList());

        idList.remove(idList.size() - 1);
        Assert.compareParameters(idList, actualIdList, "ids");
    }

    @Test(description = "Поиск - неверный тип", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"255206"})
    public void unsuccessfulSearchByInvalidTypeValueTest() {
        List<InvalidParamsItem> invalidParams = metadataStepDef.unsuccessfulMetadataSearching(
                Collections.singletonMap(SEARCH.getName(), Collections.singletonList(UUID.randomUUID().toString())));
        getData().invalidParams().get(2).name(SEARCH.getName());
        assertAll(
                () -> compareParameters(1, invalidParams.size(), "invalid_params.size"),
                () -> compareParameters(getData().invalidParams().get(2), invalidParams.get(0), "invalid_params")
        );
    }

    @Test(description = "Поиск по filename - по одному значению", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"230094"})
    public void successSearchByFilenameTest() {
        List<DataItem> dataItems = metadataStepDef.successMetadataSearch(imageList.get(0).getFilename().split("\\.", 2)[0]);
        List<String> actualIdList = dataItems.stream().map(DataItem::getId).collect(Collectors.toList());

        Assert.compareParameters(Collections.singletonList(idList.get(0)), actualIdList, "ids");
    }

    @Test(description = "Поиск по filename - по нескольким значениям", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"230094"})
    public void successSearchByArrayFilenameTest() {
        StringBuilder searchValue = new StringBuilder(EMPTY_VALUE);
        idList.remove(idList.size() - 1);
        imageList.remove(imageList.size() - 1);
        for (ImageData image : imageList) {
            String filename = image.getFilename().split("\\.", 2)[0];
            searchValue.append(filename).append(SPACE_VALUE);
        }

        List<DataItem> dataItems = metadataStepDef.successMetadataSearch(searchValue.toString());
        List<String> actualIdList = dataItems.stream().map(DataItem::getId).collect(Collectors.toList());

        Assert.compareParameters(idList, actualIdList, "ids");
    }

    @Test(description = "Поиск по format - по одному значению", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"230095"})
    public void successSearchByFormatTest() {
        metadataStepDef.createMetadataImage(new ImageData(ImageFormat.PNG));

        List<DataItem> dataItems = metadataStepDef.successMetadataSearch(ImageFormat.JPEG.getFormatName());
        List<String> actualIdList = dataItems.stream().map(DataItem::getId).collect(Collectors.toList());

        Assert.compareParameters(idList, actualIdList, "ids");
    }

    @Test(description = "Поиск по format - по нескольким значениям", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"230095"})
    public void successSearchByArrayFormatTest() {
        idList.add(metadataStepDef.createMetadataImage(new ImageData(ImageFormat.PNG)));
        metadataStepDef.createMetadataImage(new ImageData(ImageFormat.JPG));

        List<DataItem> dataItems = metadataStepDef.successMetadataSearch(
                ImageFormat.PNG.getFormatName() + SPACE_VALUE + ImageFormat.JPEG.getFormatName());
        List<String> actualIdList = dataItems.stream().map(DataItem::getId).collect(Collectors.toList());

        Assert.compareParameters(idList, actualIdList, "ids");
    }

    @Test(description = "Поиск по origin_name - по одному значению", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"230096"})
    public void successSearchByOriginNameTest() {
        idList.forEach(id ->
                metadataStepDef.successEditMetadata(id, FILENAME.getName(), RandomStringUtils.randomAlphabetic(8) + DOT_VALUE + ImageFormat.JPEG));

        List<DataItem> dataItems = metadataStepDef.successMetadataSearch(imageList.get(0).getFilename().split("\\.", 2)[0]);
        List<String> actualIdList = dataItems.stream().map(DataItem::getId).collect(Collectors.toList());

        Assert.compareParameters(Collections.singletonList(idList.get(0)), actualIdList, "ids");
    }

    @Test(description = "Поиск по origin_name - по несколько значений", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"230096"})
    public void successSearchByArrayOriginNameTest() {
        idList.forEach(id ->
                metadataStepDef.successEditMetadata(id, FILENAME.getName(), RandomStringUtils.randomAlphabetic(8) + DOT_VALUE + ImageFormat.JPEG));

        String searchValue = imageList.get(0).getFilename().split("\\.", 2)[0] + SPACE_VALUE + imageList.get(1).getFilename().split("\\.", 2)[0];
        List<DataItem> dataItems = metadataStepDef.successMetadataSearch(searchValue);
        List<String> actualIdList = dataItems.stream().map(DataItem::getId).collect(Collectors.toList());

        Assert.compareParameters(Arrays.asList(idList.get(0), idList.get(1)), actualIdList, "ids");
    }

    @Test(description = "Поиск по description - по одному значению", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"230097"})
    public void successSearchByDescriptionTest() {
        String searchValue = RandomStringUtils.randomAlphabetic(8);
        String expId = idList.get(idList.size() - 1);
        metadataStepDef.successEditMetadata(expId, DESCRIPTION.getName(), searchValue);

        idList.remove(idList.size() - 1);
        idList.forEach(id -> metadataStepDef.successEditMetadata(id, DESCRIPTION.getName(), RandomStringUtils.randomAlphabetic(10)));

        List<DataItem> dataItems = metadataStepDef.successMetadataSearch(searchValue);
        List<String> actualIdList = dataItems.stream().map(DataItem::getId).collect(Collectors.toList());

        Assert.compareParameters(Collections.singletonList(expId), actualIdList, "ids");
    }

    @Test(description = "Поиск по description - по одному значению из нескольких в одном файле", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"230097"})
    public void successSearchByPartValueDescriptionTest() {
        String firstSearchValue = RandomStringUtils.randomAlphabetic(8);
        String secondSearchValue = RandomStringUtils.randomAlphabetic(10);
        String expId = idList.get(idList.size() - 1);
        metadataStepDef.successEditMetadata(expId, DESCRIPTION.getName(), firstSearchValue + SPACE_VALUE + secondSearchValue);

        idList.remove(idList.size() - 1);
        idList.forEach(id ->
                metadataStepDef.successEditMetadata(
                        id, DESCRIPTION.getName(), RandomStringUtils.randomAlphabetic(10) + SPACE_VALUE + RandomStringUtils.randomAlphabetic(8)));

        List<DataItem> dataItems = metadataStepDef.successMetadataSearch(secondSearchValue);
        List<String> actualIdList = dataItems.stream().map(DataItem::getId).collect(Collectors.toList());

        Assert.compareParameters(Collections.singletonList(expId), actualIdList, "ids");
    }

    @Test(description = "Поиск по description - по нескольким значениям", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"230097"})
    public void successSearchByArrayDescriptionTest() {
        String firstSearchValue = RandomStringUtils.randomAlphabetic(8);
        String firstExpId = idList.get(idList.size() - 2);
        metadataStepDef.successEditMetadata(firstExpId, DESCRIPTION.getName(), firstSearchValue);

        String secondSearchValue = RandomStringUtils.randomAlphabetic(8);
        String secondExpId = idList.get(idList.size() - 1);
        metadataStepDef.successEditMetadata(secondExpId, DESCRIPTION.getName(), secondSearchValue);

        idList.remove(idList.size() - 1);
        idList.remove(idList.size() - 1);
        idList.forEach(id -> metadataStepDef.successEditMetadata(id, DESCRIPTION.getName(), RandomStringUtils.randomAlphabetic(10)));

        List<DataItem> dataItems = metadataStepDef.successMetadataSearch(firstSearchValue + SPACE_VALUE + secondSearchValue);
        List<String> actualIdList = dataItems.stream().map(DataItem::getId).collect(Collectors.toList());

        Assert.compareParameters(Arrays.asList(firstExpId, secondExpId), actualIdList, "ids");
    }

    @Test(description = "Поиск по keywords - по одному значению", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"230098"})
    public void successSearchByKeywordsTest() {
        String searchValue = RandomStringUtils.randomAlphabetic(8);

        RequestMultiEditData requestMultiEditData = RequestMultiEditData.builder()
                .ids(idList).keywords(Collections.singletonList(searchValue)).build();
        metadataStepDef.successMultiEditMetadata(requestMultiEditData);
        metadataStepDef.successEditMetadata(
                idList.get(idList.size() - 1), KEYWORDS.getName(), Collections.singletonList(RandomStringUtils.randomAlphabetic(10)));
        idList.remove(idList.size() - 1);

        List<DataItem> dataItems = metadataStepDef.successMetadataSearch(searchValue);
        List<String> actualIdList = dataItems.stream().map(DataItem::getId).collect(Collectors.toList());

        Assert.compareParameters(idList, actualIdList, "ids");
    }

    @Test(description = "Поиск по keywords - по одному значению из нескольких в одном файле", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"230098"})
    public void successSearchByPartValueKeywordsTest() {
        String firstSearchValue = RandomStringUtils.randomAlphabetic(8);
        String secondSearchValue = RandomStringUtils.randomAlphabetic(8);

        RequestMultiEditData requestMultiEditData = RequestMultiEditData.builder()
                .ids(idList).keywords(Arrays.asList(RandomStringUtils.randomAlphabetic(10), RandomStringUtils.randomAlphabetic(15))).build();
        metadataStepDef.successMultiEditMetadata(requestMultiEditData);
        metadataStepDef.successEditMetadata(
                idList.get(idList.size() - 1), KEYWORDS.getName(), Arrays.asList(firstSearchValue, secondSearchValue));
        String expId = idList.remove(idList.size() - 1);

        List<DataItem> dataItems = metadataStepDef.successMetadataSearch(firstSearchValue);
        List<String> actualIdList = dataItems.stream().map(DataItem::getId).collect(Collectors.toList());

        Assert.compareParameters(Collections.singletonList(expId), actualIdList, "ids");
    }

    @Test(description = "Поиск по keywords - по нескольким значениям", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"230098"})
    public void successSearchByListKeywordsTest() {
        String someId = metadataStepDef.createMetadataImage(new ImageData(ImageFormat.JPEG));
        metadataStepDef.successEditMetadata(someId, KEYWORDS.getName(), Collections.singletonList(RandomStringUtils.randomAlphabetic(10)));

        String firstSearchValue = RandomStringUtils.randomAlphabetic(8);
        String secondSearchValue = RandomStringUtils.randomAlphabetic(8);

        RequestMultiEditData requestMultiEditData = RequestMultiEditData.builder()
                .ids(idList).keywords(Collections.singletonList(firstSearchValue)).build();
        metadataStepDef.successMultiEditMetadata(requestMultiEditData);
        metadataStepDef.successEditMetadata(idList.get(idList.size() - 1), KEYWORDS.getName(), Collections.singletonList(secondSearchValue));

        List<DataItem> dataItems = metadataStepDef.successMetadataSearch(firstSearchValue + SPACE_VALUE + secondSearchValue);
        List<String> actualIdList = dataItems.stream().map(DataItem::getId).collect(Collectors.toList());

        Assert.compareParameters(idList, actualIdList, "ids");
    }

    @Test(description = "Поиск по received (получено от) - по одному значению", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"230099"})
    public void successSearchByReceivedTest() {
        String searchValue = RandomStringUtils.randomAlphabetic(8);

        RequestMultiEditData requestMultiEditData = RequestMultiEditData.builder()
                .ids(idList).received(searchValue).build();
        metadataStepDef.successMultiEditMetadata(requestMultiEditData);
        metadataStepDef.successEditMetadata(
                idList.get(idList.size() - 1), RECEIVED.getName(), RandomStringUtils.randomAlphabetic(10));
        idList.remove(idList.size() - 1);

        List<DataItem> dataItems = metadataStepDef.successMetadataSearch(searchValue);
        List<String> actualIdList = dataItems.stream().map(DataItem::getId).collect(Collectors.toList());

        Assert.compareParameters(idList, actualIdList, "ids");
    }

    @Test(description = "Поиск по received - по одному значению из нескольких в одном файле", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"230099"})
    public void successSearchByPartValueReceivedTest() {
        String firstSearchValue = RandomStringUtils.randomAlphabetic(8);
        String secondSearchValue = RandomStringUtils.randomAlphabetic(8);

        RequestMultiEditData requestMultiEditData = RequestMultiEditData.builder()
                .ids(idList).received(RandomStringUtils.randomAlphabetic(10) + SPACE_VALUE + RandomStringUtils.randomAlphabetic(15)).build();
        metadataStepDef.successMultiEditMetadata(requestMultiEditData);
        metadataStepDef.successEditMetadata(
                idList.get(idList.size() - 1), RECEIVED.getName(), firstSearchValue + SPACE_VALUE + secondSearchValue);
        String expId = idList.remove(idList.size() - 1);

        List<DataItem> dataItems = metadataStepDef.successMetadataSearch(firstSearchValue);
        List<String> actualIdList = dataItems.stream().map(DataItem::getId).collect(Collectors.toList());

        Assert.compareParameters(Collections.singletonList(expId), actualIdList, "ids");
    }

    @Test(description = "Поиск по received - по нескольким значениям", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"230099"})
    public void successSearchByArrayReceivedTest() {
        String someId = metadataStepDef.createMetadataImage(new ImageData(ImageFormat.JPEG));
        metadataStepDef.successEditMetadata(someId, RECEIVED.getName(), RandomStringUtils.randomAlphabetic(10));

        String firstSearchValue = RandomStringUtils.randomAlphabetic(8);
        String secondSearchValue = RandomStringUtils.randomAlphabetic(8);

        RequestMultiEditData requestMultiEditData = RequestMultiEditData.builder()
                .ids(idList).received(firstSearchValue).build();
        metadataStepDef.successMultiEditMetadata(requestMultiEditData);
        metadataStepDef.successEditMetadata(idList.get(idList.size() - 1), RECEIVED.getName(), secondSearchValue);

        List<DataItem> dataItems = metadataStepDef.successMetadataSearch(firstSearchValue + SPACE_VALUE + secondSearchValue);
        List<String> actualIdList = dataItems.stream().map(DataItem::getId).collect(Collectors.toList());

        Assert.compareParameters(idList, actualIdList, "ids");
    }

    @Test(description = "Поиск по assignee = по одному значению", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"230100"})
    public void successSearchByAssigneeTest() {
        List<UsersItem> usersItems = metadataStepDef.getListUsersMetadata();

        String expAssigneeId = usersItems.get(0).id();
        String expAssigneeName = usersItems.get(0).fullName();

        idList.remove(idList.size() - 1);
        RequestMultiEditData requestMultiEditData = RequestMultiEditData.builder()
                .ids(idList).assigneeId(expAssigneeId).build();
        metadataStepDef.successMultiEditMetadata(requestMultiEditData);

        List<DataItem> dataItems = metadataStepDef.successMetadataSearch(expAssigneeName);
        List<String> actualIdList = dataItems.stream().map(DataItem::getId).collect(Collectors.toList());

        Assert.compareParameters(idList, actualIdList, "ids");
    }

    @Test(description = "Поиск по assignee = по части значения (только по имени/фамилии)", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"230100"})
    public void successSearchByPartValueAssigneeTest() {
        List<UsersItem> usersItems = metadataStepDef.getListUsersMetadata();

        String expAssigneeId = usersItems.get(0).id();
        String expAssigneeName = usersItems.get(0).fullName().split(SPACE_VALUE)[0];

        idList.remove(idList.size() - 1);
        RequestMultiEditData requestMultiEditData = RequestMultiEditData.builder()
                .ids(idList).assigneeId(expAssigneeId).build();
        metadataStepDef.successMultiEditMetadata(requestMultiEditData);

        List<DataItem> dataItems = metadataStepDef.successMetadataSearch(expAssigneeName);
        List<String> actualIdList = dataItems.stream().map(DataItem::getId).collect(Collectors.toList());

        Assert.compareParameters(idList, actualIdList, "ids");
    }

    @Test(description = "Поиск по status_id - по одному значению", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"230101"})
    public void successSearchByStatusTest() {
        DictionariesItem statusItem = metadataStepDef.getListStatusesMetadata().
                stream().filter(s -> s.name().equals(IN_PROGRESS.getName())).findFirst().orElseThrow(() -> new RuntimeException("Не удалось найти статус"));

        idList.remove(idList.size() - 1);
        RequestMultiEditData requestMultiEditData = RequestMultiEditData.builder()
                .ids(idList).statusId(statusItem.id()).build();
        metadataStepDef.successMultiEditMetadata(requestMultiEditData);

        List<DataItem> dataItems = metadataStepDef.successMetadataSearch(statusItem.name());
        List<String> actualIdList = dataItems.stream().map(DataItem::getId).collect(Collectors.toList());

        Assert.compareParameters(idList, actualIdList, "ids");
    }

    @Test(description = "Поиск по status_id - по нескольким значениям", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"230101"})
    public void successSearchByArrayStatusTest() {
        DictionariesItem inProgressStatusItem = metadataStepDef.getListStatusesMetadata().
                stream().filter(s -> s.name().equals(IN_PROGRESS.getName())).findFirst().orElseThrow(() -> new RuntimeException("Не удалось найти статус"));
        DictionariesItem archiveStatusItem = metadataStepDef.getListStatusesMetadata().
                stream().filter(s -> s.name().equals(ARCHIVE.getName())).findFirst().orElseThrow(() -> new RuntimeException("Не удалось найти статус"));

        idList.remove(idList.size() - 1);
        RequestMultiEditData requestMultiEditData = RequestMultiEditData.builder()
                .ids(idList).statusId(inProgressStatusItem.id()).build();
        metadataStepDef.successMultiEditMetadata(requestMultiEditData);
        metadataStepDef.successEditMetadata(idList.get(0), STATUS_ID.getName(), archiveStatusItem.id());

        List<DataItem> dataItems = metadataStepDef.successMetadataSearch(inProgressStatusItem.name() + SPACE_VALUE + archiveStatusItem.name());
        List<String> actualIdList = dataItems.stream().map(DataItem::getId).collect(Collectors.toList());

        Assert.compareParameters(idList, actualIdList, "ids");
    }

    @Test(description = "Поиск по sku - по одному значению", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"230102"})
    public void successSearchBySkuTest() {
        String searchValue = RandomStringUtils.randomAlphabetic(6);
        metadataStepDef.successEditMetadata(idList.get(0), SKU.getName(), searchValue);

        List<DataItem> dataItems = metadataStepDef.successMetadataSearch(searchValue);
        List<String> actualIdList = dataItems.stream().map(DataItem::getId).collect(Collectors.toList());

        Assert.compareParameters(Collections.singletonList(idList.get(0)), actualIdList, "ids");
    }

    @Test(description = "Поиск по sku - по нескольким значениям", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"230102"})
    public void successSearchByArraySkuTest() {
        String firstSearchValue = RandomStringUtils.randomAlphabetic(10);
        metadataStepDef.successEditMetadata(idList.get(0), SKU.getName(), firstSearchValue);

        String secondSearchValue = RandomStringUtils.randomAlphabetic(8);
        metadataStepDef.successEditMetadata(idList.get(1), SKU.getName(), secondSearchValue);

        List<DataItem> dataItems = metadataStepDef.successMetadataSearch(firstSearchValue + SPACE_VALUE + secondSearchValue);
        List<String> actualIdList = dataItems.stream().map(DataItem::getId).collect(Collectors.toList());

        Assert.compareParameters(Arrays.asList(idList.get(0), idList.get(1)), actualIdList, "ids");
    }

    @Test(description = "Поиск по source_id - по одному значению", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"230103"})
    public void successSearchBySourceTest() {
        List<DictionariesItem> sourceItems = metadataStepDef.getListSourcesMetadata();
        String expSourceId = sourceItems.get(0).id();
        String expSourceName = sourceItems.get(0).name();

        idList.remove(idList.size() - 1);
        RequestMultiEditData requestMultiEditData = RequestMultiEditData.builder()
                .ids(idList).sourceId(expSourceId).build();
        metadataStepDef.successMultiEditMetadata(requestMultiEditData);

        List<DataItem> dataItems = metadataStepDef.successMetadataSearch(expSourceName);
        List<String> actualIdList = dataItems.stream().map(DataItem::getId).collect(Collectors.toList());

        Assert.compareParameters(idList, actualIdList, "ids");
    }

    @Test(description = "Поиск по source_id - по нескольким значениям", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"230103"})
    public void successSearchByArraySourceTest() {
        List<DictionariesItem> sourceItems = metadataStepDef.getListSourcesMetadata();

        idList.remove(idList.size() - 1);
        RequestMultiEditData requestMultiEditData = RequestMultiEditData.builder()
                .ids(idList).sourceId(sourceItems.get(0).id()).build();
        metadataStepDef.successMultiEditMetadata(requestMultiEditData);

        idList.add(metadataStepDef.createMetadataImage(new ImageData(ImageFormat.JPEG)));
        metadataStepDef.successEditMetadata(idList.get(idList.size() - 1), SOURCE_ID.getName(), sourceItems.get(1).id());

        List<DataItem> dataItems = metadataStepDef.successMetadataSearch(sourceItems.get(0).name() + SPACE_VALUE + sourceItems.get(1).name());
        List<String> actualIdList = dataItems.stream().map(DataItem::getId).collect(Collectors.toList());

        Assert.compareParameters(idList, actualIdList, "ids");
    }

    @Test(description = "Поиск по quality_id - по одному значению", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"230104"})
    public void successSearchByQualityTest() {
        DictionariesItem qualityItem = metadataStepDef.getListQualitiesMetadata().stream().
                filter(q -> q.name().equals(BAD.getName())).findFirst().orElseThrow(() -> new RuntimeException("Качество не найдено"));

        idList.remove(idList.size() - 1);
        RequestMultiEditData requestMultiEditData = RequestMultiEditData.builder()
                .ids(idList).qualityId(qualityItem.id()).build();
        metadataStepDef.successMultiEditMetadata(requestMultiEditData);

        List<DataItem> dataItems = metadataStepDef.successMetadataSearch(qualityItem.name());
        List<String> actualIdList = dataItems.stream().map(DataItem::getId).collect(Collectors.toList());

        Assert.compareParameters(idList, actualIdList, "ids");
    }

    @Test(description = "Поиск по quality_id - по нескольким значениям", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"230104"})
    public void successSearchByArrayQualityTest() {
        List<DictionariesItem> qualityItem = metadataStepDef.getListQualitiesMetadata();
        DictionariesItem badQualityItem = qualityItem.stream().
                filter(q -> q.name().equals(BAD.getName())).findFirst().orElseThrow(() -> new RuntimeException("Качество не найдено"));
        DictionariesItem toRevisionQualityItem = qualityItem.stream().
                filter(q -> q.name().equals(TO_REVISION.getName())).findFirst().orElseThrow(() -> new RuntimeException("Качество не найдено"));

        idList.remove(idList.size() - 1);
        RequestMultiEditData requestMultiEditData = RequestMultiEditData.builder()
                .ids(idList).qualityId(badQualityItem.id()).build();
        metadataStepDef.successMultiEditMetadata(requestMultiEditData);

        idList.add(metadataStepDef.createMetadataImage(new ImageData(ImageFormat.JPEG)));
        metadataStepDef.successEditMetadata(idList.get(idList.size() - 1), QUALITY_ID.getName(), toRevisionQualityItem.id());

        List<DataItem> dataItems = metadataStepDef.successMetadataSearch(badQualityItem.name() + SPACE_VALUE + toRevisionQualityItem.name());
        List<String> actualIdList = dataItems.stream().map(DataItem::getId).collect(Collectors.toList());

        Assert.compareParameters(idList, actualIdList, "ids");
    }

    @Test(description = "Поиск по category_id - по одному значению", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"230105"})
    public void successSearchByCategoryTest() {
        List<DictionariesItem> categoryItems = metadataStepDef.getListCategoriesMetadata();
        String expCategoryId = categoryItems.get(0).id();
        String expCategoryName = categoryItems.get(0).name();

        idList.remove(idList.size() - 1);
        RequestMultiEditData requestMultiEditData = RequestMultiEditData.builder()
                .ids(idList).masterCategoryId(expCategoryId).build();
        metadataStepDef.successMultiEditMetadata(requestMultiEditData);

        List<DataItem> dataItems = metadataStepDef.successMetadataSearch(expCategoryName);
        List<String> actualIdList = dataItems.stream().map(DataItem::getId).collect(Collectors.toList());

        Assert.compareParameters(idList, actualIdList, "ids");
    }

    @Test(description = "Поиск по category_id - по нескольким значениям", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"230105"})
    public void successSearchByArrayCategoryTest() {
        List<DictionariesItem> categoryItems = metadataStepDef.getListCategoriesMetadata();

        idList.remove(idList.size() - 1);
        RequestMultiEditData requestMultiEditData = RequestMultiEditData.builder()
                .ids(idList).masterCategoryId(categoryItems.get(0).id()).build();
        metadataStepDef.successMultiEditMetadata(requestMultiEditData);

        metadataStepDef.successEditMetadata(idList.get(0), MASTER_CATEGORY_ID.getName(), categoryItems.get(1).id());

        List<DataItem> dataItems = metadataStepDef.successMetadataSearch(categoryItems.get(0).name() + SPACE_VALUE + categoryItems.get(1).name());
        List<String> actualIdList = dataItems.stream().map(DataItem::getId).collect(Collectors.toList());

        Assert.compareParameters(idList, actualIdList, "ids");
    }

    @Test(description = "Поиск по priority - по одному значению", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"230106"})
    public void successSearchByPriorityTest() {
        int priority = new Random().nextInt(FOUR_HUNDRED_NINETY_NINE) + 1;

        idList.remove(idList.size() - 1);
        for (String id : idList) {
            metadataStepDef.successEditMetadata(id, PRIORITY.getName(), priority);
        }

        List<DataItem> dataItems = metadataStepDef.successMetadataSearch(String.valueOf(priority));
        List<String> actualIdList = dataItems.stream().map(DataItem::getId).collect(Collectors.toList());

        Assert.compareParameters(idList, actualIdList, "ids");
    }

    @Test(description = "Поиск по priority - по нескольким значениям", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"230106"})
    public void successSearchByArrayPriorityTest() {
        int firstPriority = new Random().nextInt(FOUR_HUNDRED_NINETY_NINE) + 1;
        String firstId = idList.get(idList.size() - 2);
        metadataStepDef.successEditMetadata(firstId, PRIORITY.getName(), firstPriority);

        int secondPriority = new Random().nextInt(FOUR_HUNDRED_NINETY_NINE) + 1;
        String secondId = idList.get(idList.size() - 1);
        metadataStepDef.successEditMetadata(secondId, PRIORITY.getName(), secondPriority);

        idList.remove(idList.size() - 1);
        idList.remove(idList.size() - 1);
        for (String id : idList) {
            metadataStepDef.successEditMetadata(id, PRIORITY.getName(), new Random().nextInt(FOUR_HUNDRED_NINETY_NINE) + 1);
        }

        List<DataItem> dataItems = metadataStepDef.successMetadataSearch(firstPriority + SPACE_VALUE + secondPriority);
        List<String> actualIdList = dataItems.stream().map(DataItem::getId).collect(Collectors.toList());

        Assert.compareParameters(Arrays.asList(firstId, secondId), actualIdList, "ids");
    }

    @Test(description = "Поиск по external_task_id - по одному значению", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"231280"})
    public void successSearchByExternalTaskIdTest() {
        String searchValue = String.valueOf(new Random().nextInt(ONE_THOUSAND));

        idList.remove(idList.size() - 1);
        for (String id : idList) {
            metadataStepDef.successEditMetadata(id, EXTERNAL_TASK_ID.getName(), searchValue);
        }

        List<DataItem> dataItems = metadataStepDef.successMetadataSearch(searchValue);
        List<String> actualIdList = dataItems.stream().map(DataItem::getId).collect(Collectors.toList());

        Assert.compareParameters(idList, actualIdList, "ids");
    }

    @Test(description = "Поиск по external_task_id - по нескольким значениям", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"231280"})
    public void successSearchByArrayExternalTaskIdTest() {
        String firstSearchValue = String.valueOf(new Random().nextInt(ONE_THOUSAND));
        String secondSearchValue = String.valueOf(new Random().nextInt(ONE_THOUSAND));

        idList.remove(idList.size() - 1);
        for (String id : idList) {
            metadataStepDef.successEditMetadata(id, EXTERNAL_TASK_ID.getName(), firstSearchValue);
        }
        String id = metadataStepDef.createMetadataImage(new ImageData(ImageFormat.JPEG));
        idList.add(id);
        metadataStepDef.successEditMetadata(id, EXTERNAL_TASK_ID.getName(), secondSearchValue);

        List<DataItem> dataItems = metadataStepDef.successMetadataSearch(firstSearchValue + SPACE_VALUE + secondSearchValue);
        List<String> actualIdList = dataItems.stream().map(DataItem::getId).collect(Collectors.toList());

        Assert.compareParameters(idList, actualIdList, "ids");
    }

    @Test(description = "Поиск по external_offer_id", timeOut = 600000, groups = {"regress", "kafka"})
    @WorkItemIds({"252680"})
    public void successSearchByExternalOfferIdTest() {
        Integer externalOfferId = new Random().nextInt(1000000) + 1000000;
        Integer masterSellerId = new Random().nextInt(100) + 1;

        RetailerMediaImportSettings.ImportParameters importParameters = getData().importParameters().clone().
                mediaName(RandomStringUtils.randomAlphabetic(10) + DOT_VALUE + JPG.getFormatName()).
                masterSellerId(masterSellerId).externalOfferId(externalOfferId.toString());
        retailerMediaImportStepDef.sendMediaFile(retailerMediaImportStepDef.buildImportOneMediaRequest(importParameters));

        AtomicReference<List<DataItem>> dataItemsAtomic = new AtomicReference<>();
        Awaitility.await().atMost(10, TimeUnit.SECONDS).pollInterval(1, TimeUnit.SECONDS).until(() ->
        {
            List<DataItem> dataItems = metadataStepDef.successMetadataSearch(externalOfferId);
            dataItemsAtomic.set(dataItems);
            return dataItems.size() == 2;
        });

        List<String> actualFilenameList = dataItemsAtomic.get().stream().map(DataItem::getFilename).collect(Collectors.toList());

        String name = String.format(RMIS_FILENAME_MASK, masterSellerId, externalOfferId, 1, JPG.getFormatName());
        Assert.compareParameters(Arrays.asList(name, name), actualFilenameList, "filename");
    }

    @Test(description = "Поиск нескольких файлов по разным параметрам", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"252686"})
    public void successSearchBySomeParametersTest() {
        String expFilename = imageList.get(0).getFilename().split("\\.", 2)[0];

        String expDescription = RandomStringUtils.randomAlphabetic(8);
        metadataStepDef.successEditMetadata(idList.get(1), DESCRIPTION.getName(), expDescription);

        List<DictionariesItem> sourceItems = metadataStepDef.getListSourcesMetadata();
        metadataStepDef.successEditMetadata(idList.get(2), SOURCE_ID.getName(), sourceItems.get(0).id());
        String expSourceName = sourceItems.get(0).name();

        idList.remove(idList.size() - 1);

        List<DataItem> dataItems = metadataStepDef.successMetadataSearch(expFilename + SPACE_VALUE + expDescription + SPACE_VALUE + expSourceName);
        List<String> actualIdList = dataItems.stream().map(DataItem::getId).collect(Collectors.toList());

        Assert.compareParameters(idList, actualIdList, "ids");
    }
}
