package ru.spice.at.api.metadata_service.test;

import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import io.restassured.response.Response;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.RandomStringUtils;
import org.awaitility.Awaitility;
import org.testng.annotations.AfterClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import ru.spice.at.api.dto.response.metadata.InvalidParamsItem;
import ru.spice.at.api.dto.response.metadata.ProcessingItem;
import ru.spice.at.api.import_service.ImportServiceStepDef;
import ru.spice.at.api.metadata_service.MetadataSettings;
import ru.spice.at.api.metadata_service.MetadataStepDef;
import ru.spice.at.common.base_test.BaseApiTest;
import ru.spice.at.common.dto.dam.ImageData;
import ru.spice.at.common.emuns.ApiServices;
import ru.spice.at.common.emuns.dam.ImageFormat;
import ru.spice.at.common.emuns.dam.ProcessingTypeEnum;
import ru.spice.at.common.utils.Assert;
import ru.testit.annotations.WorkItemIds;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static ru.spice.at.common.emuns.dam.ImageParameters.DERIVED_METADATA_ID;
import static ru.spice.at.common.emuns.dam.ProcessingStatusEnum.IN_PROCESS;
import static ru.spice.at.common.emuns.dam.ProcessingTypeEnum.*;
import static ru.spice.at.common.emuns.dam.ProcessingTypeEnum.CLIPPING;
import static ru.spice.at.common.utils.Assert.assertAll;
import static ru.spice.at.common.utils.Assert.compareParameters;
import static ru.spice.at.common.utils.JsonHelper.getValueFromResponse;

@Log4j2
@Feature("Metadata Service")
@Story("PUT processings")
public class MetadataPutProcessingsTests extends BaseApiTest<MetadataSettings> {
    private final MetadataStepDef metadataStepDef;
    private final ImportServiceStepDef importServiceStepDef;

    protected MetadataPutProcessingsTests() {
        super(ApiServices.METADATA_SERVICE);
        metadataStepDef = new MetadataStepDef();
        importServiceStepDef = new ImportServiceStepDef();
    }

    //todo выключено для использования параллельного сьюта
    //@AfterClass
    public void afterClass(){
        metadataStepDef.deleteMetadata();
    }

    @DataProvider(name = "successProcessingType")
    public Object[] getSuccessProcessingType() {
        return new List [] {
                Collections.singletonList(NORMALIZATION),
                Arrays.asList(NORMALIZATION, FIELDS),
                Arrays.asList(NORMALIZATION, CLIPPING),
                Arrays.asList(NORMALIZATION, FIELDS, CLIPPING),
                Collections.singletonList(WATERMARK)
        };
    }

    @DataProvider(name = "unsuccessfulProcessingType")
    public Object[] getUnsuccessfulProcessingType() {
        return new List [] {
                Collections.singletonList(FIELDS),
                Collections.singletonList(CLIPPING),
                Arrays.asList(FIELDS, CLIPPING)
        };
    }

    @Test(description = "Успешная проверка обновления обработки изображения для необработанного изображения",
            timeOut = 600000, groups = {"regress"}, dataProvider = "successProcessingType")
    @WorkItemIds({"274165", "274167", "274168", "274174"})
    public void successUpdateEmptyProcessingTest(List<ProcessingTypeEnum> processingTypes) {
        String metadataId = importServiceStepDef.importRandomImages(new ImageData(ImageFormat.JPEG));

        log.info("Обновляем обработку изображения");
        List<ProcessingItem> putProcessingItems = metadataStepDef.successPutProcessings(metadataId, processingTypes);
        Assert.compareParameters(1, putProcessingItems.size(), "size");
        assertAll(
                () -> compareParameters(putProcessingItems.get(0).processingType().name(), CREATING_DERIVATIVE.getName(), "processing type name"),
                () -> compareParameters(putProcessingItems.get(0).processingType().id(), CREATING_DERIVATIVE.getId(), "processing type id"),
                () -> compareParameters(putProcessingItems.get(0).processingStatus().name(), IN_PROCESS.getName(), "processing status name")
        );

        log.info("Ожидание создания производного");
        AtomicReference<String> derivedMetadataIdAtomic = new AtomicReference<>();
        Awaitility.await().atMost(20, TimeUnit.SECONDS).pollInterval(1, TimeUnit.SECONDS).until(() ->
        {
            Response response = metadataStepDef.checkMetadata(metadataId);
            String id = getValueFromResponse(response, DERIVED_METADATA_ID.getPath());
            derivedMetadataIdAtomic.set(id);
            return id != null;
        });

        log.info("Ожидание статусов обработки");
        AtomicReference<List<ProcessingItem>> processingItems = new AtomicReference<>();
        Awaitility.await("Есть обработки в статусе 'В процессе'").atMost(10, TimeUnit.SECONDS).pollInterval(1, TimeUnit.SECONDS).until(() ->
        {
            processingItems.set(metadataStepDef.successGetProcessings(derivedMetadataIdAtomic.get()));
            Assert.compareParameters(processingTypes.size(), processingItems.get().size(), "size");

            List<String> statuses = processingItems.get().stream().map(item -> item.processingStatus().name()).collect(Collectors.toList());
            return !statuses.contains(IN_PROCESS.getName());
        });
    }

    @Test(description = "Неуспешная проверка обновления обработки изображения для необработанного изображения без нормализации",
            timeOut = 600000, groups = {"regress"}, dataProvider = "unsuccessfulProcessingType")
    @WorkItemIds({"274196"})
    public void unsuccessfulUpdateEmptyProcessingTest(List<ProcessingTypeEnum> processingTypes) {
        String metadataId = importServiceStepDef.importRandomImages(new ImageData(ImageFormat.JPEG));

        List<InvalidParamsItem> invalidParamsItems = metadataStepDef.unsuccessfulPutProcessings(
                metadataId, processingTypes.stream().map(ProcessingTypeEnum::getId).collect(Collectors.toList()));

        getData().invalidProcessingParam().name("processing_type");
        Assert.compareParameters(Collections.singletonList(getData().invalidProcessingParam()), invalidParamsItems, "ошибки");
    }

    @Test(description = "Успешная проверка обновления обработки изображения для обработанного изображения",
            timeOut = 600000, groups = {"regress"}, dataProvider = "successProcessingType")
    @WorkItemIds({"274426", "274427", "274428", "274429"})
    public void successUpdateProcessingTest(List<ProcessingTypeEnum> processingTypes) {
        List<String> metadataIds = importServiceStepDef.importImages(new ImageData(ImageFormat.JPEG), Collections.singletonList(NORMALIZATION));

        log.info("Ожидание создания производного");
        AtomicReference<String> derivedMetadataIdAtomic = new AtomicReference<>();
        Awaitility.await().atMost(20, TimeUnit.SECONDS).pollInterval(1, TimeUnit.SECONDS).until(() ->
        {
            Response response = metadataStepDef.checkMetadata(metadataIds.get(0));
            String id = getValueFromResponse(response, DERIVED_METADATA_ID.getPath());
            derivedMetadataIdAtomic.set(id);
            return id != null;
        });

        log.info("Обновляем обработку изображения");
        List<ProcessingItem> putProcessingItems = metadataStepDef.successPutProcessings(derivedMetadataIdAtomic.get(), processingTypes);
        processingTypes.forEach(type -> {
            ProcessingItem processingItem = putProcessingItems.stream().filter(item -> item.processingType().id().equals(type.getId())).
                    findFirst().orElseThrow(() -> new RuntimeException("Тип обработки не найден " + type));

            assertAll(
                    () -> compareParameters(processingItem.processingType().name(), type.getName(), "processing type name"),
                    () -> compareParameters(processingItem.processingType().id(), type.getId(), "processing type id"),
                    () -> compareParameters(processingItem.processingStatus().name(), IN_PROCESS.getName(), "processing status name")
            );
        });

        log.info("Ожидание статусов обработки");
        AtomicReference<List<ProcessingItem>> processingItems = new AtomicReference<>();
        Awaitility.await("Есть обработки в статусе 'В процессе'").atMost(10, TimeUnit.SECONDS).pollInterval(1, TimeUnit.SECONDS).until(() ->
        {
            processingItems.set(metadataStepDef.successGetProcessings(derivedMetadataIdAtomic.get()));
            Assert.compareParameters(processingTypes.size() + 1, processingItems.get().size(), "size");

            List<String> statuses = processingItems.get().stream().map(item -> item.processingStatus().name()).collect(Collectors.toList());
            return !statuses.contains(IN_PROCESS.getName());
        });
    }

    @Test(description = "Неуспешная проверка обновления обработки изображения для обработанного изображения без нормализации",
            timeOut = 600000, groups = {"regress"}, dataProvider = "unsuccessfulProcessingType")
    @WorkItemIds({"274430"})
    public void unsuccessfulUpdateProcessingTest(List<ProcessingTypeEnum> processingTypes) {
        List<String> metadataIds = importServiceStepDef.importImages(new ImageData(ImageFormat.JPEG), Collections.singletonList(NORMALIZATION));

        List<InvalidParamsItem> invalidParamsItems = metadataStepDef.unsuccessfulPutProcessings(
                metadataIds.get(0), processingTypes.stream().map(ProcessingTypeEnum::getId).collect(Collectors.toList()));

        getData().invalidProcessingParam().name("processing_type");
        Assert.compareParameters(Collections.singletonList(getData().invalidProcessingParam()), invalidParamsItems, "ошибки");
    }

    @Test(description = "Неуспешная проверка обновления обработки изображения для обработанного изображения с пустым списком обработок",
            timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"274430"})
    public void unsuccessfulUpdateEmptyListProcessingTest() {
        List<String> metadataIds = importServiceStepDef.importImages(new ImageData(ImageFormat.JPEG), Collections.singletonList(NORMALIZATION));

        List<InvalidParamsItem> invalidParamsItems = metadataStepDef.unsuccessfulPutProcessings(
                metadataIds.get(0), Collections.emptyList());

        getData().invalidParams().get(9).name("data");
        assertAll(
                () -> compareParameters(1, invalidParamsItems.size(), "invalid_params.size"),
                () -> compareParameters(getData().invalidParams().get(9), invalidParamsItems.get(0), "invalid_params")
        );
    }

    @Test(description = "Неуспешная проверка обновления обработки изображения - NotFound (неизвестный id файла)", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"274431"})
    public void unsuccessfulUpdateProcessingNotFoundIdTest() {
        metadataStepDef.unsuccessfulPutProcessingsNotFound(UUID.randomUUID().toString(), Collections.singletonList(NORMALIZATION));
    }

    @Test(description = "Неуспешная проверка обновления обработки изображения - NotFound (неизвестный id обработки)", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"274431"})
    public void unsuccessfulUpdateProcessingNotFoundDataTest() {
        String metadataId = importServiceStepDef.importRandomImages(new ImageData(ImageFormat.JPEG));

        List<InvalidParamsItem> invalidParamsItems = metadataStepDef.unsuccessfulPutProcessings(
                metadataId, Collections.singletonList(UUID.randomUUID().toString()));

        getData().invalidBodyStringParam().name("processing_type");
        Assert.compareParameters(Collections.singletonList(getData().invalidNotFoundParam()), invalidParamsItems, "ошибки");
    }

    @Test(description = "Неуспешная проверка обновления обработки изображения - InvalidType (невалидный id файла)", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"274432"})
    public void unsuccessfulUpdateProcessingInvalidTypeIdTest() {
        List<InvalidParamsItem> invalidParamsItems = metadataStepDef.unsuccessfulPutProcessings(
                RandomStringUtils.randomAlphabetic(10), Collections.singletonList(NORMALIZATION.getId()));

        getData().invalidBodyStringParam().name("id");
        Assert.compareParameters(Collections.singletonList(getData().invalidBodyStringParam()), invalidParamsItems, "ошибки");
    }

    @Test(description = "Неуспешная проверка обновления обработки изображения - InvalidType (невалидный id обработки)", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"274432"})
    public void unsuccessfulUpdateProcessingInvalidTypeDataTest() {
        String metadataId = importServiceStepDef.importRandomImages(new ImageData(ImageFormat.JPEG));

        List<InvalidParamsItem> invalidParamsItems = metadataStepDef.unsuccessfulPutProcessings(
                metadataId, Collections.singletonList(RandomStringUtils.randomAlphabetic(10)));

        getData().invalidBodyStringParam().name("data");
        Assert.compareParameters(Collections.singletonList(getData().invalidBodyStringParam()), invalidParamsItems, "ошибки");
    }
}
