package ru.spice.at.api.metadata_service.test;

import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import ru.testit.annotations.WorkItemIds;
import lombok.SneakyThrows;
import org.apache.commons.lang3.RandomStringUtils;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.spice.at.api.dto.response.metadata.*;
import ru.spice.at.api.dto.response.metadata.filtration.DataItem;
import ru.spice.at.api.metadata_service.MetadataSettings;
import ru.spice.at.api.metadata_service.MetadataStepDef;
import ru.spice.at.common.base_test.BaseApiTest;
import ru.spice.at.common.dto.dam.ImageData;
import ru.spice.at.common.emuns.ApiServices;
import ru.spice.at.common.emuns.dam.*;
import ru.spice.at.common.utils.Assert;

import java.util.*;
import java.util.stream.Collectors;

import static ru.spice.at.common.constants.TestConstants.*;
import static ru.spice.at.common.emuns.dam.ImageParameters.*;
import static ru.spice.at.common.emuns.dam.SearchParameters.SORT;
import static ru.spice.at.common.utils.Assert.*;

@Feature("Metadata Service")
@Story("POST metadata searching sorting data")
public class MetadataPostSearchingSortTests extends BaseApiTest<MetadataSettings> {
    private final static int COUNT_METADATA = 4;
    private final MetadataStepDef metadataStepDef;
    private final List<String> idList = new ArrayList<>();

    protected MetadataPostSearchingSortTests() {
        super(ApiServices.METADATA_SERVICE);
        metadataStepDef = new MetadataStepDef();
    }

    @BeforeClass(alwaysRun = true)
    @SneakyThrows
    public void beforeClass() {
        metadataStepDef.deleteMetadata();
        String actualStatusId = metadataStepDef.getListStatusesMetadata().stream().filter(item -> item.name().equals(Status.ACTUAL.getName())).
                map(DictionariesItem::id).findFirst().orElse(null);

        String sku = RandomStringUtils.randomNumeric(5);

        for (int i = 0; i < COUNT_METADATA; i++) {
            ImageData image = new ImageData(ImageFormat.JPEG);
            idList.add(metadataStepDef.createMetadataImage(image));

            int finalI = i;
            Map<String, Object> editValues = new HashMap<>() {{
                put(SKU.getName(), sku);
                put(PRIORITY.getName(), finalI + 1);
                put(STATUS_ID.getName(), actualStatusId);
            }};
            metadataStepDef.successEditMetadata(idList.get(finalI), editValues);
        }

        List<String> keywords = Collections.singletonList(RandomStringUtils.randomAlphabetic(6));
        metadataStepDef.successEditMetadata(idList.get(1), KEYWORDS.getName(), keywords);
        metadataStepDef.successEditMetadata(idList.get(2), KEYWORDS.getName(), keywords);
    }

    @AfterClass(alwaysRun = true)
    public void afterClass() {
        idList.clear();
        metadataStepDef.deleteMetadata();
    }

    @Test(description = "Сортировка по убыванию даты загрузки", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"259356"})
    public void successSortByCreatedDescTest() {
        List<DataItem> dataItems = metadataStepDef.successMetadataSearching(SORT.getName(), Sort.CREATED_DESC.getName());
        List<String> actualIdList = dataItems.stream().map(DataItem::getId).collect(Collectors.toList());
        Collections.reverse(actualIdList);
        for (int i = 0; i < COUNT_METADATA; i++) {
            Assert.compareParameters(idList.get(i), actualIdList.get(i), "ids");
        }
    }

    @Test(description = "Сортировка по возрастанию даты загрузки", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"259355"})
    public void successSortByCreatedAscTest() {
        List<DataItem> dataItems = metadataStepDef.successMetadataSearching(SORT.getName(), Sort.CREATED_ASC.getName());
        List<String> actualIdList = dataItems.stream().map(DataItem::getId).collect(Collectors.toList());
        for (int i = 0; i < COUNT_METADATA; i++) {
            Assert.compareParameters(idList.get(i), actualIdList.get(i), "ids");
        }
    }

    @Test(description = "Сортировка по убыванию даты обновления", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"259358"})
    public void successSortByUpdatedDescTest() {
        List<DataItem> dataItems = metadataStepDef.successMetadataSearching(SORT.getName(), Sort.UPDATED_DESC.getName());
        List<String> actualIdList = dataItems.stream().map(DataItem::getId).collect(Collectors.toList());
        List<String> expectedList = Arrays.asList(idList.get(2), idList.get(1), idList.get(3), idList.get(0));
        for (int i = 0; i < COUNT_METADATA; i++) {
            Assert.compareParameters(expectedList.get(i), actualIdList.get(i), "ids");
        }
    }

    @Test(description = "Сортировка по возрастанию даты обновления", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"259357"})
    public void successSortByUpdatedAscTest() {
        List<DataItem> dataItems = metadataStepDef.successMetadataSearching(SORT.getName(), Sort.UPDATED_ASC.getName());
        List<String> actualIdList = dataItems.stream().map(DataItem::getId).collect(Collectors.toList());
        List<String> expectedList = Arrays.asList(idList.get(0), idList.get(3), idList.get(1), idList.get(2));
        for (int i = 0; i < COUNT_METADATA; i++) {
            Assert.compareParameters(expectedList.get(i), actualIdList.get(i), "ids");
        }
    }

    @Test(description = "Сортировка по убыванию имени файла", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"290160"})
    public void successSortByFilenameDescTest() {
        List<DataItem> dataItems = metadataStepDef.successMetadataSearching(SORT.getName(), Sort.FILENAME_DESC.getName());
        List<String> actualIdList = dataItems.stream().map(DataItem::getId).collect(Collectors.toList());
        Collections.reverse(actualIdList);
        for (int i = 0; i < COUNT_METADATA; i++) {
            Assert.compareParameters(idList.get(i), actualIdList.get(i), "ids");
        }
    }

    @Test(description = "Сортировка по возрастанию имени файла", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"290159"})
    public void successSortByFilenameAscTest() {
        List<DataItem> dataItems = metadataStepDef.successMetadataSearching(SORT.getName(), Sort.FILENAME_ASC.getName());
        List<String> actualIdList = dataItems.stream().map(DataItem::getId).collect(Collectors.toList());
        for (int i = 0; i < COUNT_METADATA; i++) {
            Assert.compareParameters(idList.get(i), actualIdList.get(i), "ids");
        }
    }

    @Test(description = "Сортировка - неверное значение параметра sort", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"259359"})
    public void unsuccessfulSortInvalidValueTest() {
        List<InvalidParamsItem> invalidParams = metadataStepDef.unsuccessfulMetadataSearching(SORT.getName(), RandomStringUtils.randomAscii(10));
        assertAll(
                () -> compareParameters(1, invalidParams.size(), "size"),
                () -> compareParameters(SORT.getName(), invalidParams.get(0).name(), "name"),
                () -> compareParameters(getData().invalidParams().get(5).type(), invalidParams.get(0).type(), "type"),
                () -> compareParameters(getData().invalidParams().get(5).reason(), invalidParams.get(0).reason(), "reason")
        );
    }

    @Test(description = "Сортировка - неверный тип параметра sort (число)", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"259360"})
    public void unsuccessfulSortInvalidTypeTest() {
        List<InvalidParamsItem> invalidParams = metadataStepDef.unsuccessfulMetadataSearching(SORT.getName(), new Random().nextInt(FOUR_HUNDRED_NINETY_NINE));
        assertAll(
                () -> compareParameters(1, invalidParams.size(), "size"),
                () -> compareParameters(SORT.getName(), invalidParams.get(0).name(), "name"),
                () -> compareParameters(getData().invalidParams().get(5).type(), invalidParams.get(0).type(), "type"),
                () -> compareParameters(getData().invalidParams().get(5).reason(), invalidParams.get(0).reason(), "reason")
        );
    }
}