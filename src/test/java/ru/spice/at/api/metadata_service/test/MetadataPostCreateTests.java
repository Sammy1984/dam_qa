package ru.spice.at.api.metadata_service.test;

import io.qameta.allure.Feature;
import io.qameta.allure.Issue;
import io.qameta.allure.Story;
import ru.testit.annotations.WorkItemIds;
import org.apache.commons.lang3.RandomStringUtils;
import org.testng.annotations.Test;
import ru.spice.at.api.dto.response.metadata.InvalidParamsItem;
import ru.spice.at.api.metadata_service.MetadataSettings;
import ru.spice.at.api.metadata_service.MetadataStepDef;
import ru.spice.at.common.base_test.BaseApiTest;
import ru.spice.at.common.dto.dam.ImageData;
import ru.spice.at.common.emuns.ApiServices;
import ru.spice.at.common.utils.Assert;

import java.util.LinkedList;
import java.util.List;

import static ru.spice.at.common.constants.TestConstants.EMPTY_BODY;
import static ru.spice.at.common.constants.TestConstants.EMPTY_VALUE;
import static ru.spice.at.common.utils.Assert.assertAll;
import static ru.spice.at.common.utils.Assert.compareParameters;

@Feature("Metadata Service")
@Story("POST create metadata")
public class MetadataPostCreateTests extends BaseApiTest<MetadataSettings> {
    private final MetadataStepDef metadataStepDef;

    protected MetadataPostCreateTests() {
        super(ApiServices.METADATA_SERVICE);
        metadataStepDef = new MetadataStepDef();
    }

    //todo выключено для использования параллельного сьюта
    //@AfterClass(alwaysRun = true)
    public void afterClass() {
        metadataStepDef.deleteMetadata();
    }

    @Test(description = "Успешное создание объекта в метадата", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"191798"})
    public void successCreateImageTest() {
        metadataStepDef.createMetadataImage(getData().images().get(0));
    }

    @Test(description = "Успешное создание объекта с не уникальным полем filename", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"191799"})
    public void successCreateArchiveImageTest() {
        ImageData image = getData().images().get(1);
        String id = metadataStepDef.createMetadataImage(image);
        image.setKey(null).setUrl(null);
        String newId = metadataStepDef.createMetadataImage(image);
        metadataStepDef.checkMetadataStatusImage(id, getData().newStatus());
        metadataStepDef.checkMetadataStatusImage(newId, getData().newStatus());
    }

    @Test(description = "Успешное создание объекта в метадата - длинное значение key (граница)", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"191798"})
    public void successCreateImageLongKeyTest() {
        ImageData imageData = getData().images().get(0).clone();
        imageData.setKey(RandomStringUtils.randomAlphanumeric(100));
        metadataStepDef.createMetadataImage(imageData);
    }

    @Issue("SPC-1269")
    @Test(description = "Неуспешное создание объекта в метадата - длинное значение key", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"233172"})
    public void unsuccessfulCreateImageLongKeyTest() {
        ImageData imageData = getData().images().get(0).clone();
        imageData.setKey(RandomStringUtils.randomAlphanumeric(101));
        List<InvalidParamsItem> invalidParams = metadataStepDef.unsuccessfulCreateMetadataImage(imageData);
        imageData.setKey(null);
        getData().invalidParams().get(7).name("key");
        assertAll(
                () -> compareParameters(1, invalidParams.size(), "invalid_params.size"),
                () -> compareParameters(getData().invalidParams().get(7), invalidParams.get(0), "invalid_params")
        );
    }

    @Test(description = "Успешное создание объекта в метадата - длинное значение url (граница)", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"191798"})
    public void successCreateImageLongUrlTest() {
        ImageData imageData = getData().images().get(0).clone();
        imageData.setUrl(String.format("https://%s/%s/%s",
                RandomStringUtils.randomAlphanumeric(60),
                RandomStringUtils.randomAlphanumeric(60),
                RandomStringUtils.randomAlphanumeric(70)));
        metadataStepDef.createMetadataImage(imageData);
    }

    @Test(description = "Неуспешное создание объекта в метадата - длинное значение url", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"233173"})
    public void unsuccessfulCreateImageLongUrlTest() {
        ImageData imageData = getData().images().get(0).clone();
        imageData.setUrl(String.format("https://%s/%s/%s",
                RandomStringUtils.randomAlphanumeric(60),
                RandomStringUtils.randomAlphanumeric(60),
                RandomStringUtils.randomAlphanumeric(71)));
        List<InvalidParamsItem> invalidParams = metadataStepDef.unsuccessfulCreateMetadataImage(imageData);
        imageData.setUrl(null);
        getData().invalidParams().get(7).name("url");
        assertAll(
                () -> compareParameters(1, invalidParams.size(), "invalid_params.size"),
                () -> compareParameters(getData().invalidParams().get(7), invalidParams.get(0), "invalid_params")
        );
    }

    @Test(description = "Неуспешное создание объекта в метадата с пустым телом", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"226174"})
    public void unsuccessfulCreateEmptyBodyImageTest() {
        List<InvalidParamsItem> invalidParamsItems = metadataStepDef.unsuccessfulCreateMetadataImage(EMPTY_BODY);
        Assert.compareParameters(new LinkedList<>(getData().invalidBodyParams()), new LinkedList<>(invalidParamsItems), "список ошибок");
    }

    @Test(description = "Неуспешное создание объекта в метадата со строкой", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"230145"})
    public void unsuccessfulCreateStringBodyImageTest() {
        List<InvalidParamsItem> invalidParamsItems = metadataStepDef.unsuccessfulCreateMetadataImage(RandomStringUtils.randomAlphabetic(7));
        assertAll(
                () -> compareParameters(1, invalidParamsItems.size(), "size"),
                () -> compareParameters(getData().invalidBodyStringParam(), invalidParamsItems.get(0), "ошибка")
        );
    }

    @Test(description = "Неуспешное создание объекта в метадата с пустой строкой", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"230146"})
    public void unsuccessfulCreateEmptyStringBodyImageTest() {
        List<InvalidParamsItem> invalidParamsItems = metadataStepDef.unsuccessfulCreateMetadataImage(EMPTY_VALUE);
        assertAll(
                () -> compareParameters(1, invalidParamsItems.size(), "size"),
                () -> compareParameters(getData().invalidEmptyBodyStringParam(), invalidParamsItems.get(0), "ошибка")
        );
    }
}
