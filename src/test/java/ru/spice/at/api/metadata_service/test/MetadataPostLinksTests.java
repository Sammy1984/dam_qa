package ru.spice.at.api.metadata_service.test;

import io.qameta.allure.Feature;
import io.qameta.allure.Issue;
import io.qameta.allure.Story;
import ru.testit.annotations.WorkItemIds;
import org.apache.commons.lang3.RandomStringUtils;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.spice.at.api.dto.response.metadata.InvalidParamsItem;
import ru.spice.at.api.dto.response.metadata.filtration.DataItem;
import ru.spice.at.api.metadata_service.MetadataSettings;
import ru.spice.at.api.metadata_service.MetadataStepDef;
import ru.spice.at.common.base_test.BaseApiTest;
import ru.spice.at.common.emuns.ApiServices;
import ru.spice.at.common.utils.Assert;

import java.util.*;
import java.util.stream.Collectors;

import static ru.spice.at.common.constants.TestConstants.EMPTY_VALUE;
import static ru.spice.at.common.emuns.dam.ImageParameters.MASTER_SELLER_IDS;
import static ru.spice.at.common.emuns.dam.ImageParameters.PRIORITIES;
import static ru.spice.at.common.emuns.dam.SearchParameters.Q_LINK;
import static ru.spice.at.common.utils.Assert.assertAll;
import static ru.spice.at.common.utils.Assert.compareParameters;

@Feature("Metadata Service")
@Story("POST metadata links")
public class MetadataPostLinksTests extends BaseApiTest<MetadataSettings> {
    private final MetadataStepDef metadataStepDef;
    private final List<String> ids = new ArrayList<>();

    protected MetadataPostLinksTests() {
        super(ApiServices.METADATA_SERVICE);
        metadataStepDef = new MetadataStepDef();
    }

    @BeforeClass(description = "Создаем файлы в методате", alwaysRun = true)
    public void beforeClass() {
        getData().images().forEach(image -> ids.add(metadataStepDef.createMetadataImage(image)));
    }

    //todo выключено для использования параллельного сьюта
    //@AfterClass(alwaysRun = true)
    public void afterClass() {
        metadataStepDef.deleteMetadata();
    }

    @Test(description = "Копирование ссылок для одного изображения (через links)", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"241032"})
    public void successCopyImageLinksTest() {
        List<String> expIds = Collections.singletonList(ids.get(0));

        String qLink = metadataStepDef.successGetQLink(expIds);
        List<DataItem> dataItems = metadataStepDef.successMetadataSearchingQLink(qLink);
        List<String> actualIdList = dataItems.stream().map(DataItem::getId).collect(Collectors.toList());

        Assert.compareParameters(expIds, actualIdList, "id файлов");
    }

    @Test(description = "Выбор результата по ссылке для одного изображения (через links)", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"241032"})
    public void successChooseImageLinksTest() {
        List<String> expIds = Collections.singletonList(ids.get(0));

        String qLink = metadataStepDef.successGetQLink(expIds);
        List<String> actualIds = metadataStepDef.successMetadataSelectionQLink(qLink);

        Assert.compareParameters(expIds, actualIds, "id файлов");
    }

    @Test(description = "Копирование ссылок для нескольких изображений (через links)", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"241033"})
    public void successCopyImagesLinksTest() {
        List<String> expIds = Arrays.asList(ids.get(0), ids.get(1));

        String qLink = metadataStepDef.successGetQLink(expIds);
        List<DataItem> dataItems = metadataStepDef.successMetadataSearchingQLink(qLink);
        List<String> actualIdList = dataItems.stream().map(DataItem::getId).collect(Collectors.toList());

        Assert.compareParameters(expIds, actualIdList, "id файлов");
    }

    @Test(description = "Выбор результатов по ссылке для нескольких изображений (через links)", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"241033"})
    public void successChooseImagesLinksTest() {
        List<String> expIds = Arrays.asList(ids.get(0), ids.get(1));

        String qLink = metadataStepDef.successGetQLink(expIds);
        List<String> actualIds = metadataStepDef.successMetadataSelectionQLink(qLink);

        Assert.compareParameters(new LinkedList<>(expIds), new LinkedList<>(actualIds), "id файлов");
    }

    @Issue("SPC-3530")
    @Test(description = "Копирование ссылок - пустой список", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"241035"})
    public void unsuccessfulCopyEmptyListImagesLinksTest() {
        List<InvalidParamsItem> invalidParamsItems = metadataStepDef.unsuccessfulGetQLink(Collections.emptyList());

        getData().invalidParams().get(1).name("ids");
        Assert.compareParameters(Collections.singletonList(getData().invalidParams().get(1)), invalidParamsItems, "ошибки");
    }

    @Test(description = "Копирование ссылок - пустое тело", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"241037"})
    public void unsuccessfulCopyEmptyBodyImagesLinksTest() {
        List<InvalidParamsItem> invalidParamsItems = metadataStepDef.unsuccessfulGetQLink(EMPTY_VALUE);

        Assert.compareParameters(Collections.singletonList(getData().invalidEmptyBodyStringParam()), invalidParamsItems, "ошибки");
    }

    @Test(description = "Копирование ссылок - невалидный uuid в списке", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"241036"})
    public void unsuccessfulCopyInvalidUuidImagesLinksTest() {
        List<String> expIds = Arrays.asList(ids.get(0), RandomStringUtils.randomNumeric(16), ids.get(1));
        List<InvalidParamsItem> invalidParamsItems = metadataStepDef.unsuccessfulGetQLink(expIds);

        getData().invalidBodyStringParam().name("body");
        Assert.compareParameters(Collections.singletonList(getData().invalidBodyStringParam()), invalidParamsItems, "ошибки");
    }

    @Test(description = "Копирование ссылок - несуществующий uuid в списке", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"241038"})
    public void unsuccessfulCopyNotExistUuidImagesLinksTest() {
        List<String> expIds = Arrays.asList(ids.get(0), UUID.randomUUID().toString(), ids.get(1));
        List<InvalidParamsItem> invalidParamsItems = metadataStepDef.unsuccessfulGetQLink(expIds);

        getData().invalidParams().get(1).name("ids");
        Assert.compareParameters(Collections.singletonList(getData().invalidParams().get(1)), invalidParamsItems, "ошибки");
    }

    @Test(description = "Копирование ссылок - невалидный qlink", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"241039"})
    public void unsuccessfulCopyInvalidQLinkImagesLinksTest() {
        List<InvalidParamsItem> invalidParamsItems = metadataStepDef.unsuccessfulMetadataSearching(Q_LINK.getName(), RandomStringUtils.randomNumeric(16));
        getData().invalidBodyStringParam().name(Q_LINK.getName());
        Assert.compareParameters(Collections.singletonList(getData().invalidBodyStringParam()), invalidParamsItems, "ошибки");
    }

    @Test(description = "Копирование ссылок - несуществующий qlink", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"241041"})
    public void unsuccessfulCopyNotExistQLinkImagesLinksTest() {
        List<InvalidParamsItem> invalidParamsItems = metadataStepDef.unsuccessfulMetadataSearching(Q_LINK.getName(), UUID.randomUUID().toString());
        getData().invalidParams().get(1).name(Q_LINK.getName());
        Assert.compareParameters(Collections.singletonList(getData().invalidParams().get(1)), invalidParamsItems, "ошибки");
    }

    @Test(description = "Выбрать результаты по ссылке - невалидный qlink", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"241039"})
    public void unsuccessfulChooseInvalidQLinkImagesLinksTest() {
        List<InvalidParamsItem> invalidParamsItems = metadataStepDef.unsuccessfulMetadataSelection(Q_LINK.getName(), RandomStringUtils.randomNumeric(16));
        getData().invalidBodyStringParam().name(Q_LINK.getName());
        Assert.compareParameters(Collections.singletonList(getData().invalidBodyStringParam()), invalidParamsItems, "ошибки");
    }

    @Test(description = "Выбрать результаты по ссылке - несуществующий qlink", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"241041"})
    public void unsuccessfulChooseNotExistQLinkImagesLinksTest() {
        List<InvalidParamsItem> invalidParamsItems = metadataStepDef.unsuccessfulMetadataSelection(Q_LINK.getName(), UUID.randomUUID().toString());
        getData().invalidParams().get(1).name(Q_LINK.getName());
        Assert.compareParameters(Collections.singletonList(getData().invalidParams().get(1)), invalidParamsItems, "ошибки");
    }

}