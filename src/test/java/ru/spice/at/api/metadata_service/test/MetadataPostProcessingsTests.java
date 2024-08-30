package ru.spice.at.api.metadata_service.test;

import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import io.restassured.response.Response;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.RandomStringUtils;
import org.awaitility.Awaitility;
import org.testng.annotations.*;
import ru.spice.at.api.dto.response.metadata.DictionariesItem;
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
import ru.spice.at.common.emuns.dam.Status;
import ru.spice.at.common.utils.Assert;
import ru.testit.annotations.WorkItemIds;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static ru.spice.at.common.constants.TestConstants.NINETY_NINE;
import static ru.spice.at.common.emuns.dam.ImageParameters.*;
import static ru.spice.at.common.emuns.dam.ProcessingStatusEnum.ERROR;
import static ru.spice.at.common.emuns.dam.ProcessingStatusEnum.IN_PROCESS;
import static ru.spice.at.common.emuns.dam.ProcessingTypeEnum.*;
import static ru.spice.at.common.emuns.dam.ProcessingTypeEnum.CLIPPING;
import static ru.spice.at.common.utils.Assert.*;
import static ru.spice.at.common.utils.Assert.equalsTrueParameter;
import static ru.spice.at.common.utils.JsonHelper.getValueFromResponse;

@Log4j2
@Feature("Metadata Service")
@Story("POST processings")
public class MetadataPostProcessingsTests extends BaseApiTest<MetadataSettings> {
    private final MetadataStepDef metadataStepDef;
    private final ImportServiceStepDef importServiceStepDef;
    private final List<String> idList = new ArrayList<>();
    private final List<String> derivativeList = new ArrayList<>();
    private String actualStatusId;

    protected MetadataPostProcessingsTests() {
        super(ApiServices.METADATA_SERVICE);
        metadataStepDef = new MetadataStepDef();
        importServiceStepDef = new ImportServiceStepDef();
    }

    @BeforeMethod(alwaysRun = true)
    public void beforeMethod() {
        for (int i = 0; i < 2; i++) {
            idList.add(importServiceStepDef.importRandomImages(new ImageData(ImageFormat.JPEG)));
        }
    }

    @AfterMethod(alwaysRun = true)
    public void afterMethod() {
        idList.clear();
        derivativeList.clear();
    }

    @BeforeClass(alwaysRun = true)
    public void beforeClass() {
        actualStatusId = metadataStepDef.getListStatusesMetadata().stream().filter(item -> item.name().equals(Status.ACTUAL.getName())).
                map(DictionariesItem::id).findFirst().orElse(null);
    }

    //todo выключено для использования параллельного сьюта
    //@AfterClass
    public void afterClass() {
        metadataStepDef.deleteMetadata();
    }

    //todo включить Обтравку и Удаление вотермарок после стабилизации ее работы на стейдже
    @DataProvider(name = "successProcessingType")
    public Object[] getSuccessProcessingType() {
        return new List[]{
                Collections.singletonList(NORMALIZATION),
                Arrays.asList(NORMALIZATION, FIELDS),
                //Arrays.asList(NORMALIZATION, CLIPPING),
                //Arrays.asList(NORMALIZATION, FIELDS, CLIPPING),
                //Collections.singletonList(WATERMARK)
        };
    }

    @DataProvider(name = "unsuccessfulProcessingType")
    public Object[] getUnsuccessfulProcessingType() {
        return new List[]{
                Collections.singletonList(FIELDS),
                Collections.singletonList(CLIPPING),
                Arrays.asList(FIELDS, CLIPPING)
        };
    }

    @Test(description = "Успешная обработка - несколько изображений - оригиналы (нет производных)",
            timeOut = 600000, groups = {"regress"}, dataProvider = "successProcessingType")
    @WorkItemIds({"283371", "283404", "283447", "285847"})
    public void successUpdateEmptyOriginalProcessingTest(List<ProcessingTypeEnum> processingTypes) {
        metadataStepDef.successPostProcessings(idList, processingTypes);

        for (String metadataId : idList
        ) {
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
            Awaitility.await("Есть обработки в статусе 'В процессе'").atMost(10, TimeUnit.SECONDS).pollInterval(1, TimeUnit.SECONDS).until(() ->
            {
                List<ProcessingItem> processingItems = metadataStepDef.successGetProcessings(derivedMetadataIdAtomic.get());
                List<String> statuses = processingItems.stream().map(item -> item.processingStatus().name()).collect(Collectors.toList());
                return !statuses.contains(IN_PROCESS.getName()) && !statuses.contains(ERROR.getName());
            });
        }
    }

    @Test(description = "Успешная обработка - несколько изображений - оригиналы (есть производные)",
            timeOut = 600000, groups = {"regress"}, dataProvider = "successProcessingType")
    @WorkItemIds({"283462", "283463", "283464", "285849"})
    public void successUpdateOriginalProcessingTest(List<ProcessingTypeEnum> processingTypes) {
        metadataStepDef.successPostProcessings(idList, List.of(NORMALIZATION));

        for (String metadataIdFirst : idList
        ) {
            log.info("Ожидание создания производного");
            Awaitility.await().atMost(20, TimeUnit.SECONDS).pollInterval(1, TimeUnit.SECONDS).until(() ->
            {
                Response response = metadataStepDef.checkMetadata(metadataIdFirst);
                String id = getValueFromResponse(response, DERIVED_METADATA_ID.getPath());
                return id != null;
            });
        }

        log.info("Обновляем обработку изображения");
        metadataStepDef.successPostProcessings(idList, processingTypes);

        for (String metadataId : idList
        ) {
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
            Awaitility.await("Есть обработки в статусе 'В процессе'").atMost(10, TimeUnit.SECONDS).pollInterval(1, TimeUnit.SECONDS).until(() ->
            {
                List<ProcessingItem> processingItems = metadataStepDef.successGetProcessings(derivedMetadataIdAtomic.get());
                List<String> statuses = processingItems.stream().map(item -> item.processingStatus().name()).collect(Collectors.toList());
                return !statuses.contains(IN_PROCESS.getName()) && !statuses.contains(ERROR.getName());
            });
        }
    }

    @Test(description = "Успешная обработка - несколько изображений - производные",
            timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"283460"})
    public void successUpdateDerivativeProcessingTest() {
        metadataStepDef.successPostProcessings(idList, List.of(NORMALIZATION));

        for (String metadataOriginalId : idList
        ) {
            log.info("Ожидание создания производного");
            AtomicReference<String> derivedMetadataIdAtomic = new AtomicReference<>();
            Awaitility.await().atMost(20, TimeUnit.SECONDS).pollInterval(1, TimeUnit.SECONDS).until(() ->
            {
                Response response = metadataStepDef.checkMetadata(metadataOriginalId);
                String id = getValueFromResponse(response, DERIVED_METADATA_ID.getPath());
                derivedMetadataIdAtomic.set(id);
                return id != null;
            });
            derivativeList.add(derivedMetadataIdAtomic.toString());
        }

        log.info("Отправляем на обработку производные");
        metadataStepDef.successPostProcessings(derivativeList, Arrays.asList(NORMALIZATION, FIELDS));

        for (String metadataDerivativeId : derivativeList
        ) {
            log.info("Ожидание статусов обработки");
            Awaitility.await("Есть обработки в статусе 'В процессе'").atMost(15, TimeUnit.SECONDS).pollInterval(1, TimeUnit.SECONDS).until(() ->
            {
                List<ProcessingItem> processingItems = metadataStepDef.successGetProcessings(metadataDerivativeId);
                List<String> statuses = processingItems.stream().map(item -> item.processingStatus().name()).collect(Collectors.toList());
                return !statuses.contains(IN_PROCESS.getName()) && !statuses.contains(ERROR.getName());
            });
        }
    }

    @Test(description = "Успешная обработка - несколько изображений - оригиналы и производные",
            timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"283466"})
    public void successUpdateOriginalAndDerivativeProcessingTest() {
        metadataStepDef.successPostProcessings(idList, List.of(NORMALIZATION));

        for (String metadataOriginalId : idList
        ) {
            log.info("Ожидание создания производного");
            AtomicReference<String> derivedMetadataIdAtomic = new AtomicReference<>();
            Awaitility.await().atMost(20, TimeUnit.SECONDS).pollInterval(1, TimeUnit.SECONDS).until(() ->
            {
                Response response = metadataStepDef.checkMetadata(metadataOriginalId);
                String id = getValueFromResponse(response, DERIVED_METADATA_ID.getPath());
                derivedMetadataIdAtomic.set(id);
                return id != null;
            });
            derivativeList.add(derivedMetadataIdAtomic.toString());
        }

        log.info("Отправляем на обработку оригиналы и производные");
        List<String> fullIdList = new ArrayList<>(idList);
        fullIdList.addAll(derivativeList);
        metadataStepDef.successPostProcessings(fullIdList, Arrays.asList(NORMALIZATION, FIELDS));

        for (String metadataDerivativeId : fullIdList
        ) {
            log.info("Ожидание статусов обработки");
            Awaitility.await("Есть обработки в статусе 'В процессе'").atMost(15, TimeUnit.SECONDS).pollInterval(1, TimeUnit.SECONDS).until(() ->
            {
                List<ProcessingItem> processingItems = metadataStepDef.successGetProcessings(metadataDerivativeId);
                List<String> statuses = processingItems.stream().map(item -> item.processingStatus().name()).collect(Collectors.toList());
                return !statuses.contains(IN_PROCESS.getName()) && !statuses.contains(ERROR.getName());
            });
        }
    }

    @Test(description = "Не успешная обработка - несколько изображений - оригиналы в статусе Актуальный (нет производных) - не создались производные",
            timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"283582"})
    public void unsuccessfulUpdateEmptyOriginalActualStatusProcessingTest() {
        for (String metadataIdUpdate : idList
        ) {
            Map<String, Object> editValues = new HashMap<>() {{
                put(SKU.getName(), RandomStringUtils.randomNumeric(3));
                put(PRIORITY.getName(), new Random().nextInt(NINETY_NINE) + 1);
                put(STATUS_ID.getName(), actualStatusId);
            }};
            metadataStepDef.successEditMetadata(metadataIdUpdate, editValues);
        }

        metadataStepDef.successPostProcessings(idList, List.of(NORMALIZATION));

        for (String metadataId : idList
        ) {
            log.info("Проверка создания производного");
            Response response = metadataStepDef.checkMetadata(metadataId);
            Assert.mustBeNullParameter(getValueFromResponse(response, DERIVED_METADATA_ID.getPath()), DERIVED_METADATA_ID.getName());
        }
    }

    @Test(description = "Не успешная обработка - несколько изображений - оригиналы в статусе Актуальный (есть производные) - не обновились производные",
            timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"283602"})
    public void unsuccessfulUpdateOriginalActualStatusProcessingTest() {
        metadataStepDef.successPostProcessings(idList, List.of(NORMALIZATION));

        for (String metadataId : idList
        ) {
            log.info("Ожидание создания производного");
            AtomicReference<String> derivedMetadataIdAtomic = new AtomicReference<>();
            Awaitility.await().atMost(20, TimeUnit.SECONDS).pollInterval(1, TimeUnit.SECONDS).until(() ->
            {
                Response response = metadataStepDef.checkMetadata(metadataId);
                String id = getValueFromResponse(response, DERIVED_METADATA_ID.getPath());
                derivedMetadataIdAtomic.set(id);
                return id != null;
            });
            derivativeList.add(derivedMetadataIdAtomic.toString());
        }

        for (String metadataIdUpdate : idList
        ) {
            Map<String, Object> editValues = new HashMap<>() {{
                put(SKU.getName(), RandomStringUtils.randomNumeric(3));
                put(PRIORITY.getName(), new Random().nextInt(NINETY_NINE) + 1);
                put(STATUS_ID.getName(), actualStatusId);
            }};
            metadataStepDef.successEditMetadata(metadataIdUpdate, editValues);
        }

        metadataStepDef.successPostProcessings(idList, Arrays.asList(NORMALIZATION, FIELDS));

        for (String metadataDerivativeId : derivativeList
        ) {
            log.info("Ожидание статусов обработки");
            List<ProcessingItem> processingItems = metadataStepDef.successGetProcessings(metadataDerivativeId);
            List<String> statuses = processingItems.stream().map(item -> item.processingStatus().name()).collect(Collectors.toList());

            assertAll(
                    () -> equalsTrueParameter(!statuses.contains(FIELDS.getName()), "5% поля"),
                    () -> equalsTrueParameter(!statuses.contains(IN_PROCESS.getName()), "обработка в процессе"),
                    () -> equalsTrueParameter(!statuses.contains(ERROR.getName()), "ошибки")
            );
        }
    }

    @Test(description = "Не успешная обработка - несколько изображений - оригиналы (производные в статусе Актуальный) - не обновились производные",
            timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"283603"})
    public void unsuccessfulUpdateOriginalAndDerivativeActualStatusProcessingTest() {
        metadataStepDef.successPostProcessings(idList, List.of(NORMALIZATION));

        for (String metadataId : idList
        ) {
            log.info("Ожидание создания производного");
            AtomicReference<String> derivedMetadataIdAtomic = new AtomicReference<>();
            Awaitility.await().atMost(20, TimeUnit.SECONDS).pollInterval(1, TimeUnit.SECONDS).until(() ->
            {
                Response response = metadataStepDef.checkMetadata(metadataId);
                String id = getValueFromResponse(response, DERIVED_METADATA_ID.getPath());
                derivedMetadataIdAtomic.set(id);
                return id != null;
            });
            derivativeList.add(derivedMetadataIdAtomic.toString());
        }

        for (String metadataIdUpdate : derivativeList
        ) {
            Map<String, Object> editValues = new HashMap<>() {{
                put(SKU.getName(), RandomStringUtils.randomNumeric(3));
                put(PRIORITY.getName(), new Random().nextInt(NINETY_NINE) + 1);
                put(STATUS_ID.getName(), actualStatusId);
            }};
            metadataStepDef.successEditMetadata(metadataIdUpdate, editValues);
        }

        metadataStepDef.successPostProcessings(idList, Arrays.asList(NORMALIZATION, FIELDS));

        for (String metadataDerivativeId : derivativeList
        ) {
            log.info("Ожидание статусов обработки");
            List<ProcessingItem> processingItems = metadataStepDef.successGetProcessings(metadataDerivativeId);
            List<String> statuses = processingItems.stream().map(item -> item.processingStatus().name()).collect(Collectors.toList());

            assertAll(
                    () -> equalsTrueParameter(!statuses.contains(FIELDS.getName()), "5% поля"),
                    () -> equalsTrueParameter(!statuses.contains(IN_PROCESS.getName()), "обработка в процессе"),
                    () -> equalsTrueParameter(!statuses.contains(ERROR.getName()), "ошибки")
            );
        }
    }

    @Test(description = "Не успешная обработка - несколько изображений - производные в статусе Актуальный - не обновились производные",
            timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"283606"})
    public void unsuccessfulUpdateDerivativeActualStatusProcessingTest() {
        metadataStepDef.successPostProcessings(idList, List.of(NORMALIZATION));

        for (String metadataId : idList
        ) {
            log.info("Ожидание создания производного");
            AtomicReference<String> derivedMetadataIdAtomic = new AtomicReference<>();
            Awaitility.await().atMost(20, TimeUnit.SECONDS).pollInterval(1, TimeUnit.SECONDS).until(() ->
            {
                Response response = metadataStepDef.checkMetadata(metadataId);
                String id = getValueFromResponse(response, DERIVED_METADATA_ID.getPath());
                derivedMetadataIdAtomic.set(id);
                return id != null;
            });
            derivativeList.add(derivedMetadataIdAtomic.toString());
        }

        for (String metadataIdUpdate : derivativeList
        ) {
            Map<String, Object> editValues = new HashMap<>() {{
                put(SKU.getName(), RandomStringUtils.randomNumeric(3));
                put(PRIORITY.getName(), new Random().nextInt(NINETY_NINE) + 1);
                put(STATUS_ID.getName(), actualStatusId);
            }};
            metadataStepDef.successEditMetadata(metadataIdUpdate, editValues);
        }

        metadataStepDef.successPostProcessings(derivativeList, Arrays.asList(NORMALIZATION, FIELDS));

        for (String metadataDerivativeId : derivativeList
        ) {
            log.info("Ожидание статусов обработки");
            List<ProcessingItem> processingItems = metadataStepDef.successGetProcessings(metadataDerivativeId);
            List<String> statuses = processingItems.stream().map(item -> item.processingStatus().name()).collect(Collectors.toList());

            assertAll(
                    () -> equalsTrueParameter(!statuses.contains(FIELDS.getName()), "5% поля"),
                    () -> equalsTrueParameter(!statuses.contains(IN_PROCESS.getName()), "обработка в процессе"),
                    () -> equalsTrueParameter(!statuses.contains(ERROR.getName()), "ошибки")
            );
        }
    }

    @Test(description = "Неуспешная проверка обработки изображений - NotFound (неизвестный id файла)", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"283613"})
    public void unsuccessfulUpdateMassProcessingNotFoundIdTest() {
        List<String> notFoundMetadataIds = Arrays.asList(UUID.randomUUID().toString(),
                UUID.randomUUID().toString());
        List<InvalidParamsItem> invalidParamsItems = metadataStepDef.unsuccessfulPostProcessings(notFoundMetadataIds, Collections.singletonList(NORMALIZATION.getId()));

        getData().invalidParams().get(1).name("metadata_ids");
        Assert.compareParameters(Collections.singletonList(getData().invalidParams().get(1)), invalidParamsItems, "ошибки");
    }

    @Test(description = "Неуспешная проверка обработки изображений - NotFound (неизвестный id обработки)", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"283620"})
    public void unsuccessfulUpdateMassProcessingNotFoundDataTest() {
        List<String> notFoundProcessingIds = Arrays.asList(UUID.randomUUID().toString(),
                UUID.randomUUID().toString());
        List<InvalidParamsItem> invalidParamsItems = metadataStepDef.unsuccessfulPostProcessings(
                idList, notFoundProcessingIds);

        getData().invalidBodyStringParam().name("processing_type");
        Assert.compareParameters(Collections.singletonList(getData().invalidNotFoundParam()), invalidParamsItems, "ошибки");
    }

    @Test(description = "Неуспешная проверка обработки изображений - InvalidType (невалидный id файла)", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"283612"})
    public void unsuccessfulUpdateMassProcessingInvalidTypeIdTest() {
        List<String> invalidMetadataIds = Arrays.asList(RandomStringUtils.randomAlphabetic(10),
                RandomStringUtils.randomAlphabetic(12));
        List<InvalidParamsItem> invalidParamsItems = metadataStepDef.unsuccessfulPostProcessings(
                invalidMetadataIds, Collections.singletonList(NORMALIZATION.getId()));

        getData().invalidBodyStringParam().name("metadata_ids");
        Assert.compareParameters(Collections.singletonList(getData().invalidBodyStringParam()), invalidParamsItems, "ошибки");
    }

    @Test(description = "Неуспешная проверка обработки изображений - InvalidType (невалидный id обработки)", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"283619"})
    public void unsuccessfulUpdateMassProcessingInvalidTypeDataTest() {
        List<InvalidParamsItem> invalidParamsItems = metadataStepDef.unsuccessfulPostProcessings(
                idList, Collections.singletonList(RandomStringUtils.randomAlphabetic(10)));

        getData().invalidBodyStringParam().name("processing_ids");
        Assert.compareParameters(Collections.singletonList(getData().invalidBodyStringParam()), invalidParamsItems, "ошибки");
    }

    @Test(description = "Неуспешная проверка обработки изображения для необработанного изображения без нормализации",
            timeOut = 600000, groups = {"regress"}, dataProvider = "unsuccessfulProcessingType")
    @WorkItemIds({"285995", "285997", "285999"})
    public void unsuccessfulUpdateMassEmptyProcessingTest(List<ProcessingTypeEnum> processingTypes) {
        List<InvalidParamsItem> invalidParamsItems = metadataStepDef.unsuccessfulPostProcessings(
                idList, processingTypes.stream().map(ProcessingTypeEnum::getId).collect(Collectors.toList()));

        getData().invalidProcessingParam().name("processing_type");
        Assert.compareParameters(Collections.singletonList(getData().invalidProcessingParam()), invalidParamsItems, "ошибки");
    }

    @Test(description = "Неуспешная проверка обработки изображения для обработанного изображения без нормализации",
            timeOut = 600000, groups = {"regress"}, dataProvider = "unsuccessfulProcessingType")
    @WorkItemIds({"286003", "286004", "286005"})
    public void unsuccessfulUpdateMassProcessingTest(List<ProcessingTypeEnum> processingTypes) {
        metadataStepDef.successPostProcessings(idList, List.of(NORMALIZATION));

        for (String metadataId : idList
        ) {
            log.info("Ожидание создания производного");
            Awaitility.await().atMost(20, TimeUnit.SECONDS).pollInterval(1, TimeUnit.SECONDS).until(() ->
            {
                Response response = metadataStepDef.checkMetadata(metadataId);
                String id = getValueFromResponse(response, DERIVED_METADATA_ID.getPath());
                return id != null;
            });
        }

        List<InvalidParamsItem> invalidParamsItems = metadataStepDef.unsuccessfulPostProcessings(
                idList, processingTypes.stream().map(ProcessingTypeEnum::getId).collect(Collectors.toList()));

        getData().invalidProcessingParam().name("processing_type");
        Assert.compareParameters(Collections.singletonList(getData().invalidProcessingParam()), invalidParamsItems, "ошибки");
    }

    @Test(description = "Неуспешная проверка обработки оригинального изображения (нет производных) для пустого списка обработок",
            timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"285971"})
    public void unsuccessfulUpdateMassProcessingEmptyTest() {
        List<InvalidParamsItem> invalidParamsItems = metadataStepDef.unsuccessfulPostProcessings(
                idList, Collections.emptyList());

        getData().invalidParams().get(9).name("processing_ids");
        assertAll(
                () -> compareParameters(1, invalidParamsItems.size(), "invalid_params.size"),
                () -> compareParameters(getData().invalidParams().get(9), invalidParamsItems.get(0), "invalid_params")
        );
    }

    @Test(description = "Неуспешная проверка обработки оригинального изображения (есть производные) для пустого списка обработок - не обновились производные",
            timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"286002"})
    public void unsuccessfulUpdateMassOriginalProcessingEmptyTest() {
        metadataStepDef.successPostProcessings(idList, List.of(NORMALIZATION));

        for (String metadataId : idList
        ) {
            log.info("Ожидание создания производного");
            AtomicReference<String> derivedMetadataIdAtomic = new AtomicReference<>();
            Awaitility.await().atMost(20, TimeUnit.SECONDS).pollInterval(1, TimeUnit.SECONDS).until(() ->
            {
                Response response = metadataStepDef.checkMetadata(metadataId);
                String id = getValueFromResponse(response, DERIVED_METADATA_ID.getPath());
                derivedMetadataIdAtomic.set(id);
                return id != null;
            });
            derivativeList.add(derivedMetadataIdAtomic.toString());
        }

        List<InvalidParamsItem> invalidParamsItems = metadataStepDef.unsuccessfulPostProcessings(
                idList, Collections.emptyList());

        getData().invalidParams().get(9).name("processing_ids");
        assertAll(
                () -> compareParameters(1, invalidParamsItems.size(), "invalid_params.size"),
                () -> compareParameters(getData().invalidParams().get(9), invalidParamsItems.get(0), "invalid_params")
        );

        for (String metadataDerivativeId : derivativeList
        ) {
            log.info("Ожидание статусов обработки");
            List<ProcessingItem> processingItems = metadataStepDef.successGetProcessings(metadataDerivativeId);
            List<String> statuses = processingItems.stream().map(item -> item.processingStatus().name()).collect(Collectors.toList());

            assertAll(
                    () -> equalsTrueParameter(!statuses.contains(FIELDS.getName()), "5% поля"),
                    () -> equalsTrueParameter(!statuses.contains(CLIPPING.getName()), "обтравка"),
                    () -> equalsTrueParameter(!statuses.contains(IN_PROCESS.getName()), "обработка в процессе"),
                    () -> equalsTrueParameter(!statuses.contains(ERROR.getName()), "ошибки")
            );
        }
    }
}
