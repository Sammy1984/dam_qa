package ru.spice.at.api.metadata_service.test;

import io.qameta.allure.Feature;
import io.qameta.allure.Issue;
import io.qameta.allure.Story;
import ru.testit.annotations.WorkItemIds;
import org.apache.commons.lang3.RandomStringUtils;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import ru.spice.at.api.dto.request.metadata.RequestMultiEditData;
import ru.spice.at.api.dto.response.metadata.DictionariesItem;
import ru.spice.at.api.dto.response.metadata.UsersItem;
import ru.spice.at.api.dto.response.metadata.filtration.DataItem;
import ru.spice.at.api.dto.response.metadata.filtration.FiltrationResponse;
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
import java.util.stream.Collectors;

import static ru.spice.at.common.constants.TestConstants.*;
import static ru.spice.at.common.emuns.dam.ImageFormat.JPG;
import static ru.spice.at.common.emuns.dam.ImageParameters.*;
import static ru.spice.at.common.emuns.dam.SearchParameters.SEARCH;
import static ru.spice.at.common.utils.Assert.*;

@Deprecated
@Feature("Metadata Service")
@Story("GET search data")
public class MetadataGetSearchTests extends BaseApiTest<MetadataSettings> {
    private final static int COUNT_METADATA = 4;
    private final MetadataStepDef metadataStepDef;
    private final RetailerMediaImportStepDef retailerMediaImportStepDef;
    private final List<String> idList = new ArrayList<>();

    protected MetadataGetSearchTests() {
        super(ApiServices.METADATA_SERVICE);
        metadataStepDef = new MetadataStepDef();
        retailerMediaImportStepDef = new RetailerMediaImportStepDef();
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

    //@Test(description = "Поиск по несуществующему значению", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"230091"})
    public void successSearchByNotExistValueTest() {
        String expQueryStr = RandomStringUtils.randomAlphabetic(5);

        Map<String, Object> expQueryParam = new HashMap<>();

        expQueryParam.put(SEARCH.getName(), expQueryStr);

        FiltrationResponse filtrationResponse = metadataStepDef.successFiltrationMetadata(expQueryParam, true).extract().as(FiltrationResponse.class);

        List<String> actualListId = new ArrayList<>();
        for (DataItem dataItem : filtrationResponse.getData()) {
            actualListId.add(dataItem.getId());
        }

        Assert.equalsTrueParameter(actualListId.isEmpty(), "id");
    }

    //@Test(description = "Поиск по null", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"230092"})
    public void successSearchByNullValueTest() {
        Map<String, Object> expQueryParam = new HashMap<>();

        expQueryParam.put(SEARCH.getName(), null);

        FiltrationResponse filtrationResponse = metadataStepDef.successFiltrationMetadata(expQueryParam, false).extract().as(FiltrationResponse.class);

        List<String> actualListId = new ArrayList<>();
        for (DataItem dataItem : filtrationResponse.getData()) {
            actualListId.add(dataItem.getId());
        }

        Assert.equalsFalseParameter(actualListId.isEmpty(), "id");
    }

    //@Test(description = "Поиск по пробелу", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"230093"})
    public void successSearchBySpaceValueTest() {
        Map<String, Object> expQueryParam = new HashMap<>();

        expQueryParam.put(SEARCH.getName(), SPACE_VALUE);

        FiltrationResponse filtrationResponse = metadataStepDef.successFiltrationMetadata(expQueryParam, true).extract().as(FiltrationResponse.class);

        List<String> actualListId = new ArrayList<>();
        for (DataItem dataItem : filtrationResponse.getData()) {
            actualListId.add(dataItem.getId());
        }

        Assert.equalsTrueParameter(actualListId.isEmpty(), "id");
    }

    //@Test(description = "Поиск по filename = одному значению", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"230094"})
    public void successSearchByFilenameTest() {
        String expQueryStr = metadataStepDef.checkMetadata(idList.get(0)).then().extract().path(FILENAME.getPath());

        Map<String, Object> expQueryParam = new HashMap<>();

        expQueryParam.put(SEARCH.getName(), expQueryStr.split("\\.", 2)[0]);

        FiltrationResponse filtrationResponse = metadataStepDef.successFiltrationMetadata(expQueryParam, false).extract().as(FiltrationResponse.class);
        Assert.compareParameters(1, filtrationResponse.getData().size(), "количество файлов");

        String actualFilename = filtrationResponse.getData().get(0).getFilename();

        Assert.compareParameters(expQueryStr, actualFilename, "filename");
    }

    //@Test(description = "Поиск по filename = нескольким значениям", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"230094"})
    public void successSearchByArrayFilenameTest() {
        String expQueryStr = EMPTY_VALUE;
        List<String> filenameList = new ArrayList<>();
        idList.remove(idList.size() - 1);
        for (String id : idList) {
            String filename = metadataStepDef.checkMetadata(id).then().extract().path(FILENAME.getPath()).toString().split("\\.", 2)[0];
            filenameList.add(filename);
            expQueryStr = expQueryStr.isEmpty() ? filename : expQueryStr + " " + filename;
        }
        Map<String, Object> expQueryParam = new HashMap<>();

        expQueryParam.put(SEARCH.getName(), expQueryStr);

        FiltrationResponse filtrationResponse = metadataStepDef.successFiltrationMetadata(expQueryParam, false).extract().as(FiltrationResponse.class);

        List<String> actualListFilename = new ArrayList<>();
        for (DataItem dataItem : filtrationResponse.getData()) {
            actualListFilename.add(dataItem.getFilename().split("\\.", 2)[0]);
        }

        Assert.compareParameters(filenameList, actualListFilename, "filename");
    }

    //@Test(description = "Поиск по format = одному значению", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"230095"})
    public void successSearchByFormatTest() {
        String expQueryStr = ImageFormat.JPEG.getFormatName();

        Map<String, Object> expQueryParam = new HashMap<>();

        expQueryParam.put(SEARCH.getName(), expQueryStr);

        FiltrationResponse filtrationResponse = metadataStepDef.successFiltrationMetadata(expQueryParam, false).extract().as(FiltrationResponse.class);

        List<String> actualListId = new ArrayList<>();
        for (DataItem dataItem : filtrationResponse.getData()) {
            actualListId.add(dataItem.getId());
        }

        Assert.compareParameters(idList, actualListId, "id");
    }

    //@Test(description = "Поиск по format = нескольким значениям", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"230095"})
    public void successSearchByArrayFormatTest() {
        String expQueryStr = ImageFormat.PNG.getFormatName() + SPACE_VALUE + ImageFormat.JPEG.getFormatName();

        idList.add(metadataStepDef.createMetadataImage(new ImageData(ImageFormat.PNG)));

        Map<String, Object> expQueryParam = new HashMap<>();

        expQueryParam.put(SEARCH.getName(), expQueryStr);

        FiltrationResponse filtrationResponse = metadataStepDef.successFiltrationMetadata(expQueryParam, false).extract().as(FiltrationResponse.class);

        List<String> actualListId = new ArrayList<>();
        for (DataItem dataItem : filtrationResponse.getData()) {
            actualListId.add(dataItem.getId());
        }

        Assert.compareParameters(idList, actualListId, "id");
    }

    //@Test(description = "Поиск по origin_name = одному значению", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"230096"})
    public void successSearchByOriginNameTest() {
        String expQueryStr = metadataStepDef.checkMetadata(idList.get(0)).then().extract().path(FILENAME.getPath());

        for (String id : idList) {
            metadataStepDef.successEditMetadata(id, FILENAME.getName(), RandomStringUtils.randomAlphabetic(8) + "." + ImageFormat.JPEG);
        }

        Map<String, Object> expQueryParam = new HashMap<>();

        expQueryParam.put(SEARCH.getName(), expQueryStr.split("\\.", 2)[0]);

        FiltrationResponse filtrationResponse = metadataStepDef.successFiltrationMetadata(expQueryParam, false).extract().as(FiltrationResponse.class);

        List<String> actualListId = new ArrayList<>();
        for (DataItem dataItem : filtrationResponse.getData()) {
            actualListId.add(dataItem.getId());
        }
        assertAll(
                () -> compareParameters(1, actualListId.size(), "actualListId.size"),
                () -> compareParameters(idList.get(0), actualListId.get(0), "id")
        );
    }

    //@Test(description = "Поиск по origin_name = несколько значений", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"230096"})
    public void successSearchByArrayOriginNameTest() {
        List<String> expIds = new LinkedList<>();
        StringBuilder expQueryStr = new StringBuilder(EMPTY_VALUE);
        for (int i = 0; i < 2; i++) {
            String id = idList.get(i);
            expIds.add(id);
            String originalFilename = metadataStepDef.checkMetadata(id).then().extract().path(FILENAME.getPath());
            expQueryStr.append(originalFilename.split("\\.", 2)[0]).append(SPACE_VALUE);
        }

        for (String id : idList) {
            metadataStepDef.successEditMetadata(id, FILENAME.getName(), RandomStringUtils.randomAlphabetic(8) + "." + ImageFormat.JPEG);
        }

        Map<String, Object> expQueryParam = new HashMap<>();

        expQueryParam.put(SEARCH.getName(), expQueryStr);

        FiltrationResponse filtrationResponse = metadataStepDef.successFiltrationMetadata(expQueryParam, false).extract().as(FiltrationResponse.class);

        List<String> actualListId = new LinkedList<>();
        for (DataItem dataItem : filtrationResponse.getData()) {
            actualListId.add(dataItem.getId());
        }

        Assert.compareParameters(expIds, actualListId, "id");
    }

    //@Test(description = "Поиск по description = одному значению", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"230097"})
    public void successSearchByDescriptionTest() {
        String expQueryStr = RandomStringUtils.randomAlphabetic(8);

        for (String id : idList) {
            metadataStepDef.successEditMetadata(id, DESCRIPTION.getName(), expQueryStr);
        }

        Map<String, Object> expQueryParam = new HashMap<>();

        expQueryParam.put(SEARCH.getName(), expQueryStr);

        FiltrationResponse filtrationResponse = metadataStepDef.successFiltrationMetadata(expQueryParam, false).extract().as(FiltrationResponse.class);

        List<String> actualListId = new ArrayList<>();
        for (DataItem dataItem : filtrationResponse.getData()) {
            actualListId.add(dataItem.getId());
        }

        Assert.compareParameters(idList, actualListId, "id");
    }

    //@Test(description = "Поиск по description = нескольким значениям", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"230097"})
    public void successSearchByArrayDescriptionTest() {
        String expQueryStr = RandomStringUtils.randomAlphabetic(8) + SPACE_VALUE + RandomStringUtils.randomAlphabetic(8);

        for (String id : idList) {
            metadataStepDef.successEditMetadata(id, DESCRIPTION.getName(), expQueryStr);
        }

        Map<String, Object> expQueryParam = new HashMap<>();

        expQueryParam.put(SEARCH.getName(), expQueryStr);

        FiltrationResponse filtrationResponse = metadataStepDef.successFiltrationMetadata(expQueryParam, false).extract().as(FiltrationResponse.class);

        List<String> actualListId = new ArrayList<>();
        for (DataItem dataItem : filtrationResponse.getData()) {
            actualListId.add(dataItem.getId());
        }

        Assert.compareParameters(idList, actualListId, "id");
    }

    //@Test(description = "Поиск по description = одному значению из нескольких", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"230097"})
    public void successSearchByPartValueDescriptionTest() {
        String expQueryStr = RandomStringUtils.randomAlphabetic(8);

        for (String id : idList) {
            metadataStepDef.successEditMetadata(id, DESCRIPTION.getName(), expQueryStr + SPACE_VALUE + RandomStringUtils.randomAlphabetic(8));
        }

        Map<String, Object> expQueryParam = new HashMap<>();

        expQueryParam.put(SEARCH.getName(), expQueryStr);

        FiltrationResponse filtrationResponse = metadataStepDef.successFiltrationMetadata(expQueryParam, false).extract().as(FiltrationResponse.class);

        List<String> actualListId = new ArrayList<>();
        for (DataItem dataItem : filtrationResponse.getData()) {
            actualListId.add(dataItem.getId());
        }

        Assert.compareParameters(idList, actualListId, "id");
    }

    //@Test(description = "Поиск по keywords = одному значению", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"230098"})
    public void successSearchByKeywordsTest() {
        String expQueryStr = RandomStringUtils.randomAlphabetic(8);

        RequestMultiEditData requestMultiEditData = RequestMultiEditData.builder()
                .ids(idList).keywords(Collections.singletonList(expQueryStr)).build();
        metadataStepDef.successMultiEditMetadata(requestMultiEditData);

        Map<String, Object> expQueryParam = new HashMap<>();

        expQueryParam.put(SEARCH.getName(), expQueryStr);

        FiltrationResponse filtrationResponse = metadataStepDef.successFiltrationMetadata(expQueryParam, false).extract().as(FiltrationResponse.class);

        List<String> actualListId = new ArrayList<>();
        for (DataItem dataItem : filtrationResponse.getData()) {
            actualListId.add(dataItem.getId());
        }

        Assert.compareParameters(idList, actualListId, "id");
    }

    //@Test(description = "Поиск по keywords = нескольким значениям", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"230098"})
    public void successSearchByListKeywordsTest() {
        List<String> keywordsList = Arrays.asList(RandomStringUtils.randomAlphabetic(8), RandomStringUtils.randomAlphabetic(8));

        String expQueryStr = EMPTY_VALUE;

        for (String keyword : keywordsList) {
            expQueryStr = expQueryStr.isEmpty() ? keyword : expQueryStr + " " + keyword;
        }

        RequestMultiEditData requestMultiEditData = RequestMultiEditData.builder()
                .ids(idList).keywords(keywordsList).build();
        metadataStepDef.successMultiEditMetadata(requestMultiEditData);

        Map<String, Object> expQueryParam = new HashMap<>();

        expQueryParam.put(SEARCH.getName(), expQueryStr);

        FiltrationResponse filtrationResponse = metadataStepDef.successFiltrationMetadata(expQueryParam, false).extract().as(FiltrationResponse.class);

        List<String> actualListId = new ArrayList<>();
        for (DataItem dataItem : filtrationResponse.getData()) {
            actualListId.add(dataItem.getId());
        }

        Assert.compareParameters(idList, actualListId, "id");
    }

    //@Test(description = "Поиск по keywords = одному значению из нескольких", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"230098"})
    public void successSearchByPartValueKeywordsTest() {
        String expQueryStr = RandomStringUtils.randomAlphabetic(8);
        List<String> keywordsList = Arrays.asList(expQueryStr, RandomStringUtils.randomAlphabetic(8), RandomStringUtils.randomAlphabetic(8));

        RequestMultiEditData requestMultiEditData = RequestMultiEditData.builder()
                .ids(idList).keywords(keywordsList).build();
        metadataStepDef.successMultiEditMetadata(requestMultiEditData);

        Map<String, Object> expQueryParam = new HashMap<>();

        expQueryParam.put(SEARCH.getName(), expQueryStr);

        FiltrationResponse filtrationResponse = metadataStepDef.successFiltrationMetadata(expQueryParam, false).extract().as(FiltrationResponse.class);

        List<String> actualListId = new ArrayList<>();
        for (DataItem dataItem : filtrationResponse.getData()) {
            actualListId.add(dataItem.getId());
        }

        Assert.compareParameters(idList, actualListId, "id");
    }

    //@Test(description = "Поиск по received = одному значению", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"230099"})
    public void successSearchByReceivedTest() {
        String expQueryStr = RandomStringUtils.randomAlphabetic(8);

        RequestMultiEditData requestMultiEditData = RequestMultiEditData.builder()
                .ids(idList).received(expQueryStr).build();
        metadataStepDef.successMultiEditMetadata(requestMultiEditData);

        Map<String, Object> expQueryParam = new HashMap<>();

        expQueryParam.put(SEARCH.getName(), expQueryStr);

        FiltrationResponse filtrationResponse = metadataStepDef.successFiltrationMetadata(expQueryParam, false).extract().as(FiltrationResponse.class);

        List<String> actualListId = new ArrayList<>();
        for (DataItem dataItem : filtrationResponse.getData()) {
            actualListId.add(dataItem.getId());
        }

        Assert.compareParameters(idList, actualListId, "id");
    }

    //@Test(description = "Поиск по received = нескольким значениям", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"230099"})
    public void successSearchByArrayReceivedTest() {
        String expQueryStr = RandomStringUtils.randomAlphabetic(8) + SPACE_VALUE +
                RandomStringUtils.randomAlphabetic(8) + SPACE_VALUE + RandomStringUtils.randomAlphabetic(8);

        RequestMultiEditData requestMultiEditData = RequestMultiEditData.builder()
                .ids(idList).received(expQueryStr).build();
        metadataStepDef.successMultiEditMetadata(requestMultiEditData);

        Map<String, Object> expQueryParam = new HashMap<>();

        expQueryParam.put(SEARCH.getName(), expQueryStr);

        FiltrationResponse filtrationResponse = metadataStepDef.successFiltrationMetadata(expQueryParam, false).extract().as(FiltrationResponse.class);

        List<String> actualListId = new ArrayList<>();
        for (DataItem dataItem : filtrationResponse.getData()) {
            actualListId.add(dataItem.getId());
        }

        Assert.compareParameters(idList, actualListId, "id");
    }

    //@Test(description = "Поиск по received = одному значению из нескольких", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"230099"})
    public void successSearchByPartValueReceivedTest() {
        String expQueryStr = RandomStringUtils.randomAlphabetic(8);

        RequestMultiEditData requestMultiEditData = RequestMultiEditData.builder()
                .ids(idList).received(expQueryStr + SPACE_VALUE + RandomStringUtils.randomAlphabetic(8) +
                        SPACE_VALUE + RandomStringUtils.randomAlphabetic(8))
                .build();
        metadataStepDef.successMultiEditMetadata(requestMultiEditData);

        Map<String, Object> expQueryParam = new HashMap<>();

        expQueryParam.put(SEARCH.getName(), expQueryStr);

        FiltrationResponse filtrationResponse = metadataStepDef.successFiltrationMetadata(expQueryParam, false).extract().as(FiltrationResponse.class);

        List<String> actualListId = new ArrayList<>();
        for (DataItem dataItem : filtrationResponse.getData()) {
            actualListId.add(dataItem.getId());
        }

        Assert.compareParameters(idList, actualListId, "id");
    }

    @Issue("SPC-1236")
    //@Test(description = "Поиск по assignee = одному значению", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"230100"})
    public void successSearchByAssigneeTest() {
        List<UsersItem> usersItems = metadataStepDef.getListUsersMetadata();

        String expAssigneeId = usersItems.get(0).id();
        String expAssigneeName = usersItems.get(0).fullName();

        RequestMultiEditData requestMultiEditData = RequestMultiEditData.builder()
                .ids(idList).assigneeId(expAssigneeId).build();

        metadataStepDef.successMultiEditMetadata(requestMultiEditData);

        Map<String, Object> expQueryParam = new HashMap<>();

        expQueryParam.put(SEARCH.getName(), expAssigneeName);

        FiltrationResponse filtrationResponse = metadataStepDef.successFiltrationMetadata(expQueryParam, false).extract().as(FiltrationResponse.class);

        List<String> actualListId = new ArrayList<>();
        for (DataItem dataItem : filtrationResponse.getData()) {
            actualListId.add(dataItem.getId());
        }

        Assert.compareParameters(idList, actualListId, "id");
    }

    @Issue("SPC-1236")
    //@Test(description = "Поиск по assignee = по части значения (только по имени)", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"230100"})
    public void successSearchByPartValueAssigneeTest() {
        List<UsersItem> usersItems = metadataStepDef.getListUsersMetadata();

        String expAssigneeId = usersItems.get(0).id();
        String expAssigneeName = usersItems.get(0).fullName();

        String expQueryStr = expAssigneeName.substring(expAssigneeName.indexOf(' ') + 1);

        RequestMultiEditData requestMultiEditData = RequestMultiEditData.builder()
                .ids(idList).assigneeId(expAssigneeId).build();

        metadataStepDef.successMultiEditMetadata(requestMultiEditData);

        Map<String, Object> expQueryParam = new HashMap<>();

        expQueryParam.put(SEARCH.getName(), expQueryStr);

        FiltrationResponse filtrationResponse = metadataStepDef.successFiltrationMetadata(expQueryParam, false).extract().as(FiltrationResponse.class);

        List<String> actualListId = new ArrayList<>();
        for (DataItem dataItem : filtrationResponse.getData()) {
            actualListId.add(dataItem.getId());
        }

        Assert.compareParameters(idList, actualListId, "id");
    }

    //@Test(description = "Поиск по status_id = одному значению", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"230101"})
    public void successSearchByStatusTest() {
        List<DictionariesItem> statusItems = metadataStepDef.getListStatusesMetadata();

        String expStatusId = statusItems.get(0).id();
        String expStatusName = statusItems.get(0).name();

        RequestMultiEditData requestMultiEditData = RequestMultiEditData.builder()
                .ids(idList).statusId(expStatusId).build();

        metadataStepDef.successMultiEditMetadata(requestMultiEditData);

        Map<String, Object> expQueryParam = new HashMap<>();

        expQueryParam.put(SEARCH.getName(), expStatusName);

        FiltrationResponse filtrationResponse = metadataStepDef.successFiltrationMetadata(expQueryParam, false).extract().as(FiltrationResponse.class);

        List<String> actualListId = new ArrayList<>();
        for (DataItem dataItem : filtrationResponse.getData()) {
            actualListId.add(dataItem.getId());
        }

        Assert.compareParameters(idList, actualListId, "id");
    }

    //@Test(description = "Поиск по sku = одному значению", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"230102"})
    public void successSearchBySkuTest() {
        String expQueryStr = metadataStepDef.checkMetadata(idList.get(0)).then().extract().path(SKU.getPath());

        Map<String, Object> expQueryParam = new HashMap<>();

        expQueryParam.put(SEARCH.getName(), expQueryStr);

        FiltrationResponse filtrationResponse = metadataStepDef.successFiltrationMetadata(expQueryParam, false).extract().as(FiltrationResponse.class);

        Assert.compareParameters(1, filtrationResponse.getData().size(), "количество файлов");
        String actualFilename = filtrationResponse.getData().get(0).getSku();

        Assert.compareParameters(expQueryStr, actualFilename, "sku");
    }

    //@Test(description = "Поиск по sku = нескольким значениям", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"230102"})
    public void successSearchByArraySkuTest() {
        String expQueryStr = EMPTY_VALUE;
        List<String> skuList = new ArrayList<>();
        for (String id : idList) {
            String sku = metadataStepDef.checkMetadata(id).then().extract().path(SKU.getPath());
            skuList.add(sku);
            expQueryStr = expQueryStr.isEmpty() ? sku : expQueryStr + SPACE_VALUE + sku;
        }
        Map<String, Object> expQueryParam = new HashMap<>();

        expQueryParam.put(SEARCH.getName(), expQueryStr);

        FiltrationResponse filtrationResponse = metadataStepDef.successFiltrationMetadata(expQueryParam, false).extract().as(FiltrationResponse.class);

        List<String> actualListFilename = new ArrayList<>();
        for (DataItem dataItem : filtrationResponse.getData()) {
            actualListFilename.add(dataItem.getSku());
        }

        Assert.compareParameters(skuList, actualListFilename, "sku");
    }

    //@Test(description = "Поиск по source_id = одному значению", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"230103"})
    public void successSearchBySourceTest() {
        List<DictionariesItem> sourceItems = metadataStepDef.getListSourcesMetadata();

        String expSourceId = sourceItems.get(0).id();
        String expSourceName = sourceItems.get(0).name();

        RequestMultiEditData requestMultiEditData = RequestMultiEditData.builder()
                .ids(idList).sourceId(expSourceId).build();

        metadataStepDef.successMultiEditMetadata(requestMultiEditData);

        Map<String, Object> expQueryParam = new HashMap<>();

        expQueryParam.put(SEARCH.getName(), expSourceName);

        FiltrationResponse filtrationResponse = metadataStepDef.successFiltrationMetadata(expQueryParam, false).extract().as(FiltrationResponse.class);

        List<String> actualListId = new ArrayList<>();
        for (DataItem dataItem : filtrationResponse.getData()) {
            actualListId.add(dataItem.getId());
        }

        Assert.compareParameters(idList, actualListId, "id");
    }

    //@Test(description = "Поиск по quality_id = одному значению", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"230104"})
    public void successSearchByQualityTest() {
        List<DictionariesItem> qualityItems = metadataStepDef.getListQualitiesMetadata();

        String expQualityId = qualityItems.get(0).id();
        String expQualityName = qualityItems.get(0).name();

        RequestMultiEditData requestMultiEditData = RequestMultiEditData.builder()
                .ids(idList).qualityId(expQualityId).build();

        metadataStepDef.successMultiEditMetadata(requestMultiEditData);

        Map<String, Object> expQueryParam = new HashMap<>();

        expQueryParam.put(SEARCH.getName(), expQualityName);

        FiltrationResponse filtrationResponse = metadataStepDef.successFiltrationMetadata(expQueryParam, false).extract().as(FiltrationResponse.class);

        List<String> actualListId = new ArrayList<>();
        for (DataItem dataItem : filtrationResponse.getData()) {
            actualListId.add(dataItem.getId());
        }

        Assert.compareParameters(idList, actualListId, "id");
    }

    //@Test(description = "Поиск по category_id = одному значению", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"230105"})
    public void successSearchByCategoryTest() {
        List<DictionariesItem> categoryItems = metadataStepDef.getListCategoriesMetadata();

        String expCategoryId = categoryItems.get(0).id();
        String expCategoryName = categoryItems.get(0).name();

        RequestMultiEditData requestMultiEditData = RequestMultiEditData.builder()
                .ids(idList).masterCategoryId(expCategoryId).build();

        metadataStepDef.successMultiEditMetadata(requestMultiEditData);

        Map<String, Object> expQueryParam = new HashMap<>();

        expQueryParam.put(SEARCH.getName(), expCategoryName);

        FiltrationResponse filtrationResponse = metadataStepDef.successFiltrationMetadata(expQueryParam, false).extract().as(FiltrationResponse.class);

        List<String> actualListId = new ArrayList<>();
        for (DataItem dataItem : filtrationResponse.getData()) {
            actualListId.add(dataItem.getId());
        }

        Assert.compareParameters(idList, actualListId, "id");
    }

    //@Test(description = "Поиск по priority = одному значению", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"230106"})
    public void successSearchByPriorityTest() {
        int priority = new Random().nextInt(NINETY_NINE) + 1;

        for (String id : idList) {
            metadataStepDef.successEditMetadata(id, PRIORITY.getName(), priority);
        }

        Map<String, Object> expQueryParam = new HashMap<>();

        expQueryParam.put(SEARCH.getName(), priority);

        FiltrationResponse filtrationResponse = metadataStepDef.successFiltrationMetadata(expQueryParam, false).extract().as(FiltrationResponse.class);

        List<String> actualListId = new ArrayList<>();
        for (DataItem dataItem : filtrationResponse.getData()) {
            actualListId.add(dataItem.getId());
        }

        Assert.compareParameters(idList, actualListId, "id");
    }

    //@Test(description = "Поиск по external_task_id", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"231280"})
    public void successSearchByExternalTaskIdTest() {
        String mediaName = RandomStringUtils.randomAlphabetic(10) + DOT_VALUE + JPG.getFormatName();
        Integer externalOfferId = new Random().nextInt(1000000) + 1000000;
        Integer masterSellerId = new Random().nextInt(100) + 1;

        RetailerMediaImportSettings.ImportParameters importParameters = getData().importParameters().clone().
                mediaName(mediaName).masterSellerId(masterSellerId).externalOfferId(externalOfferId.toString());
        retailerMediaImportStepDef.sendMediaFile(retailerMediaImportStepDef.buildImportOneMediaRequest(importParameters));

        FiltrationResponse filtrationNameResponse = metadataStepDef.successFiltrationMetadata(Collections.singletonMap(SEARCH.getName(), externalOfferId), 2).
                extract().as(FiltrationResponse.class);
        Assert.notNullOrEmptyParameter(filtrationNameResponse.getData().size(), "список файлов");

        getData().importMetadata().taskIdForDam(new Random().nextInt(100) + 1);
        String metadataRequest = retailerMediaImportStepDef.buildImportMetadataRequest(
                externalOfferId, masterSellerId, getData().importMetadata());
        retailerMediaImportStepDef.sendMetadata(metadataRequest);

        FiltrationResponse filtrationExternalTaskIdResponse = metadataStepDef.
                successFiltrationMetadata(Collections.singletonMap(SEARCH.getName(), getData().importMetadata().taskIdForDam()), 2).
                extract().as(FiltrationResponse.class);

        compareParameters(
                new LinkedList<>(filtrationNameResponse.getData().stream().map(DataItem::getId).collect(Collectors.toList())),
                new LinkedList<>(filtrationExternalTaskIdResponse.getData().stream().map(DataItem::getId).collect(Collectors.toList())),
                "ids");
    }

    //@Test(description = "Поиск по external_offer_id", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"252680"})
    public void successSearchByExternalOfferIdTest() {
        String mediaName = RandomStringUtils.randomAlphabetic(10) + "." + JPG.getFormatName();
        Integer externalOfferId = new Random().nextInt(1000000) + 1000000;
        Integer masterSellerId = new Random().nextInt(100) + 1;

        RetailerMediaImportSettings.ImportParameters importParameters = getData().importParameters().clone().
                mediaName(mediaName).masterSellerId(masterSellerId).externalOfferId(externalOfferId.toString());
        retailerMediaImportStepDef.sendMediaFile(retailerMediaImportStepDef.buildImportOneMediaRequest(importParameters));

        FiltrationResponse filtrationNameResponse =
                metadataStepDef.successFiltrationMetadata(
                        Collections.singletonMap(SEARCH.getName(), String.format(RMIS_FILENAME_MASK, masterSellerId, externalOfferId, 1, JPG.getFormatName())), 2).
                extract().as(FiltrationResponse.class);
        Assert.notNullOrEmptyParameter(filtrationNameResponse.getData().size(), "список файлов");

        String metadataRequest = retailerMediaImportStepDef.buildImportMetadataRequest(
                externalOfferId, masterSellerId, getData().importMetadata());
        retailerMediaImportStepDef.sendMetadata(metadataRequest);

        FiltrationResponse filtrationExternalOfferIdResponse = metadataStepDef.
                successFiltrationMetadata(Collections.singletonMap(SEARCH.getName(), externalOfferId), 2).
                extract().as(FiltrationResponse.class);

        compareParameters(
                new LinkedList<>(filtrationNameResponse.getData().stream().map(DataItem::getId).collect(Collectors.toList())),
                new LinkedList<>(filtrationExternalOfferIdResponse.getData().stream().map(DataItem::getId).collect(Collectors.toList())),
                "ids");
    }

    //@Test(description = "Поиск нескольких файлов по разным параметрам", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"252686"})
    public void successSearchBySomeParametersTest() {
        String expFilename = metadataStepDef.checkMetadata(idList.get(0)).then().extract().path(FILENAME.getPath()).toString().
                split("\\.", 2)[0];

        String expDescription = RandomStringUtils.randomAlphabetic(8);
        metadataStepDef.successEditMetadata(idList.get(1), DESCRIPTION.getName(), expDescription);

        List<DictionariesItem> sourceItems = metadataStepDef.getListSourcesMetadata();
        metadataStepDef.successEditMetadata(idList.get(2), SOURCE_ID.getName(), sourceItems.get(0).id());
        String expSourceName = sourceItems.get(0).name();

        String expQueryStr = expFilename + SPACE_VALUE + expDescription + SPACE_VALUE + expSourceName;

        FiltrationResponse filtrationResponse =
                metadataStepDef.successFiltrationMetadata(Collections.singletonMap(SEARCH.getName(), expQueryStr), false).extract().as(FiltrationResponse.class);
        Assert.compareParameters(3, filtrationResponse.getData().size(), "количество файлов");
    }
}
