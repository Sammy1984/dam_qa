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
import static ru.spice.at.common.emuns.dam.ProcessingStatusEnum.*;
import static ru.spice.at.common.emuns.dam.ProcessingTypeEnum.*;
import static ru.spice.at.common.utils.Assert.assertAll;
import static ru.spice.at.common.utils.Assert.compareParameters;
import static ru.spice.at.common.utils.JsonHelper.getValueFromResponse;

@Log4j2
@Feature("Metadata Service")
@Story("GET processings")
public class MetadataGetProcessingsTests extends BaseApiTest<MetadataSettings> {
    private final MetadataStepDef metadataStepDef;
    private final ImportServiceStepDef importServiceStepDef;

    protected MetadataGetProcessingsTests() {
        super(ApiServices.METADATA_SERVICE);
        metadataStepDef = new MetadataStepDef();
        importServiceStepDef = new ImportServiceStepDef();
    }

    //todo выключено для использования параллельного сьюта
    //@AfterClass
    public void afterClass(){
        metadataStepDef.deleteMetadata();
    }

    @DataProvider(name = "processingType")
    public Object[] getProcessingType() {
        return new List [] {
                Collections.singletonList(NORMALIZATION),
                Arrays.asList(NORMALIZATION, FIELDS),
                Arrays.asList(NORMALIZATION, CLIPPING),
                Arrays.asList(NORMALIZATION, FIELDS, CLIPPING),
                Collections.singletonList(WATERMARK)
        };
    }

    @Test(description = "Успешная проверка статусов обработки изображения", timeOut = 600000, groups = {"regress"}, dataProvider = "processingType")
    @WorkItemIds({"273354", "273357", "273358", "273359"})
    public void successProcessingTest(List<ProcessingTypeEnum> processingTypes) {
        List<String> metadataIds = importServiceStepDef.importImages(new ImageData(ImageFormat.JPEG), processingTypes);

        log.info("Ожидание создания производного");
        AtomicReference<String> derivedMetadataIdAtomic = new AtomicReference<>();
        Awaitility.await().atMost(20, TimeUnit.SECONDS).pollInterval(1, TimeUnit.SECONDS).until(() ->
        {
            Response response = metadataStepDef.checkMetadata(metadataIds.get(0));
            String id = getValueFromResponse(response, DERIVED_METADATA_ID.getPath());
            derivedMetadataIdAtomic.set(id);
            return id != null;
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

        log.info("Проверка параметров обработки");
        processingTypes.forEach(type -> {
            ProcessingItem processingItem = processingItems.get().stream().filter(item -> item.processingType().id().equals(type.getId())).
                    findFirst().orElseThrow(() -> new RuntimeException("Тип обработки не найден " + type));

            assertAll(
                    () -> compareParameters(processingItem.processingType().name(), type.getName(), "processing type name"),
                    () -> compareParameters(processingItem.processingType().id(), type.getId(), "processing type id"),
                    () -> compareParameters(processingItem.processingStatus().name(), SUCCESS.getName(), "processing status name")
            );
        });
    }

    @Test(description = "Успешная проверка статусов обработки изображения - создание производного", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"273761"})
    public void successProcessingCreatingTest() {
        List<String> metadataIds = importServiceStepDef.importImages(new ImageData(ImageFormat.JPEG), Collections.singletonList(NORMALIZATION));

        log.info("Ожидание создания производного");
        Awaitility.await().atMost(20, TimeUnit.SECONDS).pollInterval(1, TimeUnit.SECONDS).until(() ->
        {
            Response response = metadataStepDef.checkMetadata(metadataIds.get(0));
            return getValueFromResponse(response, DERIVED_METADATA_ID.getPath()) != null;
        });

        List<ProcessingItem> processingItems = metadataStepDef.successGetProcessings(metadataIds.get(0));
        Assert.compareParameters(1, processingItems.size(), "size");
        assertAll(
                () -> compareParameters(processingItems.get(0).processingType().name(), CREATING_DERIVATIVE.getName(), "processing type name"),
                () -> compareParameters(processingItems.get(0).processingType().id(), CREATING_DERIVATIVE.getId(), "processing type id"),
                () -> compareParameters(processingItems.get(0).processingStatus().name(), SUCCESS.getName(), "processing status name")
        );
    }

    @Test(description = "Успешная проверка статусов обработки изображения - без обработки", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"273762"})
    public void successNotProcessingTest() {
        String metadataId = importServiceStepDef.importRandomImages(new ImageData(ImageFormat.JPEG));

        List<ProcessingItem> processingItems = metadataStepDef.successGetProcessings(metadataId);
        Assert.mustBeEmptyList(processingItems, "size");
    }

    @Test(description = "Неуспешная проверка статусов обработки изображения - NotFound", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"273360"})
    public void unsuccessfulProcessingNotFoundTest() {
        metadataStepDef.unsuccessfulGetProcessingsNotFound(UUID.randomUUID().toString());
    }

    @Test(description = "Неуспешная проверка статусов обработки изображения - InvalidType", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"273361"})
    public void unsuccessfulProcessingInvalidTypeTest() {
        List<InvalidParamsItem> invalidParamsItems = metadataStepDef.unsuccessfulGetProcessings(RandomStringUtils.randomAlphabetic(10));

        getData().invalidBodyStringParam().name("id");
        Assert.compareParameters(Collections.singletonList(getData().invalidBodyStringParam()), invalidParamsItems, "ошибки");
    }
}
