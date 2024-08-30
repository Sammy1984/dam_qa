package ru.spice.at.api.metadata_service.test;

import io.qameta.allure.*;
import io.restassured.response.ValidatableResponse;
import org.apache.commons.lang3.RandomStringUtils;
import org.testng.annotations.*;
import ru.spice.at.api.dto.request.metadata.RequestMultiEditData;
import ru.spice.at.api.dto.response.metadata.*;
import ru.spice.at.api.metadata_service.MetadataSettings;
import ru.spice.at.api.metadata_service.MetadataStepDef;
import ru.spice.at.common.base_test.BaseApiTest;
import ru.spice.at.common.dto.dam.ImageData;
import ru.spice.at.common.emuns.*;
import ru.spice.at.common.emuns.dam.ImageFormat;
import ru.spice.at.common.emuns.dam.Quality;
import ru.spice.at.common.emuns.dam.Source;
import ru.spice.at.common.emuns.dam.Status;
import ru.spice.at.common.utils.Assert;
import ru.spice.at.common.utils.WaitHelper;
import ru.testit.annotations.WorkItemIds;

import java.util.*;

import static ru.spice.at.common.constants.TestConstants.*;
import static ru.spice.at.common.emuns.dam.ImageParameters.*;
import static ru.spice.at.common.utils.Assert.*;

@Feature("Metadata Service")
@Story("PATCH multi edit data")
public class MetadataPatchMultiEditDataTests extends BaseApiTest<MetadataSettings> {
    private final MetadataStepDef metadataStepDef;
    private final List<String> ids = new ArrayList<>();

    private RetailersResponse retailersResponse;
    private List<DictionariesItem> categories;
    private List<UsersItem> usersItems;
    private List<DictionariesItem> sourcesItems;
    private List<DictionariesItem> qualitiesItems;
    private List<DictionariesItem> statusesItems;

    protected MetadataPatchMultiEditDataTests() {
        super(ApiServices.METADATA_SERVICE);
        metadataStepDef = new MetadataStepDef();
    }

    @BeforeClass(alwaysRun = true)
    public void beforeClass() {
        retailersResponse = metadataStepDef.getListRetailersMetadata();
        categories = metadataStepDef.getListCategoriesMetadata();
        usersItems = metadataStepDef.getListUsersMetadata();
        sourcesItems = metadataStepDef.getListSourcesMetadata();
        qualitiesItems = metadataStepDef.getListQualitiesMetadata();
        statusesItems = metadataStepDef.getListStatusesMetadata();
    }

    @BeforeMethod(alwaysRun = true)
    public void beforeMethod() {
        for (int i = 0; i < 2; i++) {
            ids.add(metadataStepDef.createMetadataImage(new ImageData(ImageFormat.JPEG)));
        }
    }


    @AfterMethod(alwaysRun = true)
    public void afterMethod() {
        ids.clear();
    }

    //todo выключено для использования параллельного сьюта
    //@AfterClass
    public void afterClass() {
        metadataStepDef.deleteMetadata();
    }

    @Test(description = "Успешное массовое редактирование поля status_id", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"191852"})
    public void successMultiEditStatusIdTest() {
        String expStatusId = statusesItems.stream().filter(item -> item.name().equals(Status.DELETE.getName())).
                map(DictionariesItem::id).findFirst().orElse(null);

        metadataStepDef.successMultiEditMetadata(ids, STATUS_ID.getName(), expStatusId);

        for (String id : ids) {
            WaitHelper.withRetriesAsserted(
                    () -> {
                        ValidatableResponse response = metadataStepDef.checkMetadata(id).then();
                        assertAll(
                                () -> compareParameters(Status.DELETE.getName(), response.extract().path(STATUS_ID.getPath() + ".name"), "status.name"),
                                () -> compareParameters(expStatusId, response.extract().path(STATUS_ID.getPath() + ".id"), "status.id")
                        );
                    });
        }
    }

    @Test(description = "Некорректное массовое редактирование поля status_id - NotFound", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"191853"})
    public void unsuccessfulMultiEditStatusIdTest() {
        List<InvalidParamsItem> invalidParams =
                metadataStepDef.unsuccessfulMultiEditMetadata(ids, STATUS_ID.getName(), UUID.randomUUID().toString());

        getData().invalidParams().get(1).name(STATUS_ID.getName());
        assertAll(
                () -> compareParameters(1, invalidParams.size(), "invalid_params.size"),
                () -> compareParameters(getData().invalidParams().get(1), invalidParams.get(0), "invalid_params")
        );
    }

    @Test(description = "Некорректное массовое редактирование поля status_id - InvalidType", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"191853"})
    public void unsuccessfulMultiEditStatusIdInvalidUuidTest() {
        List<InvalidParamsItem> invalidParams =
                metadataStepDef.unsuccessfulMultiEditMetadata(ids, STATUS_ID.getName(), RandomStringUtils.randomAlphabetic(5));

        getData().invalidParams().get(2).name(STATUS_ID.getName());
        assertAll(
                () -> compareParameters(1, invalidParams.size(), "invalid_params.size"),
                () -> compareParameters(getData().invalidParams().get(2), invalidParams.get(0), "invalid_params")
        );
    }

    @Test(description = "Успешное массовое редактирование поля quality_id", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"191854"})
    public void successMultiEditQualityIdTest() {
        String expQualityId = qualitiesItems.stream().filter(item -> item.name().equals(Quality.GOOD.getName())).
                map(DictionariesItem::id).findFirst().orElse(null);

        metadataStepDef.successMultiEditMetadata(ids, QUALITY_ID.getName(), expQualityId);

        for (String id : ids) {
            WaitHelper.withRetriesAsserted(
                    () -> {
                        ValidatableResponse response = metadataStepDef.checkMetadata(id).then();
                        assertAll(
                                () -> compareParameters(Quality.GOOD.getName(), response.extract().path(QUALITY_ID.getPath() + ".name"), "quality.name"),
                                () -> compareParameters(expQualityId, response.extract().path(QUALITY_ID.getPath() + ".id"), "quality.id")
                        );
                    });
        }
    }

    @Test(description = "Некорректное массовое редактирование поля quality_id - NotFound", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"191855"})
    public void unsuccessfulMultiEditQualityIdTest() {
        List<InvalidParamsItem> invalidParams =
                metadataStepDef.unsuccessfulMultiEditMetadata(ids, QUALITY_ID.getName(), UUID.randomUUID().toString());

        getData().invalidParams().get(1).name(QUALITY_ID.getName());
        assertAll(
                () -> compareParameters(1, invalidParams.size(), "invalid_params.size"),
                () -> compareParameters(getData().invalidParams().get(1), invalidParams.get(0), "invalid_params")
        );
    }

    @Test(description = "Некорректное массовое редактирование поля quality_id - InvalidType", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"191855"})
    public void unsuccessfulMultiEditQualityIdInvalidUuidTest() {
        List<InvalidParamsItem> invalidParams =
                metadataStepDef.unsuccessfulMultiEditMetadata(ids, QUALITY_ID.getName(), RandomStringUtils.randomAlphabetic(5));

        getData().invalidParams().get(2).name(QUALITY_ID.getName());
        assertAll(
                () -> compareParameters(1, invalidParams.size(), "invalid_params.size"),
                () -> compareParameters(getData().invalidParams().get(2), invalidParams.get(0), "invalid_params")
        );
    }

    @Test(description = "Успешное массовое редактирование поля assignee_id", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"191858"})
    public void successMultiEditaAssigneeIdTest() {
        String expAssigneeId = usersItems.get(0).id();
        String expAssigneeName = usersItems.get(0).fullName();

        metadataStepDef.successMultiEditMetadata(ids, ASSIGNEE_ID.getName(), expAssigneeId);

        for (String id : ids) {
            WaitHelper.withRetriesAsserted(
                    () -> {
                        ValidatableResponse response = metadataStepDef.checkMetadata(id).then();
                        assertAll(
                                () -> compareParameters(expAssigneeName, response.extract().path(ASSIGNEE_ID.getPath() + ".full_name"), "assignee.full_name"),
                                () -> compareParameters(expAssigneeId, response.extract().path(ASSIGNEE_ID.getPath() + ".id"), "assignee.id")
                        );
                    });
        }
    }

    @Test(description = "Успешное массовое редактирование поля assignee_id = null", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"191858"})
    public void successMultiEditaAssigneeIdNullTest() {
        successMultiEditaAssigneeIdTest();

        metadataStepDef.successMultiEditMetadata(ids, ASSIGNEE_ID.getName(), null);

        for (String id : ids) {
            WaitHelper.withRetriesAsserted(
                    () -> {
                        ValidatableResponse response = metadataStepDef.checkMetadata(id).then();
                        mustBeNullParameter(response.extract().path(ASSIGNEE_ID.getPath()), "assignee");
                    });
        }
    }

    @Test(description = "Некорректное массовое редактирование поля assignee_id - NotFound", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"191859"})
    public void unsuccessfulMultiEditAssigneeIdTest() {
        List<InvalidParamsItem> invalidParams =
                metadataStepDef.unsuccessfulMultiEditMetadata(ids, ASSIGNEE_ID.getName(), UUID.randomUUID().toString());

        getData().invalidParams().get(1).name(ASSIGNEE_ID.getName());
        assertAll(
                () -> compareParameters(1, invalidParams.size(), "invalid_params.size"),
                () -> compareParameters(getData().invalidParams().get(1), invalidParams.get(0), "invalid_params")
        );
    }

    @Test(description = "Некорректное массовое редактирование поля assignee_id - InvalidType", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"191859"})
    public void unsuccessfulMultiEditAssigneeIdInvalidUuidTest() {
        List<InvalidParamsItem> invalidParams =
                metadataStepDef.unsuccessfulMultiEditMetadata(ids, ASSIGNEE_ID.getName(), RandomStringUtils.randomAlphabetic(5));
        getData().invalidParams().get(2).name(ASSIGNEE_ID.getName());
        assertAll(
                () -> compareParameters(1, invalidParams.size(), "invalid_params.size"),
                () -> compareParameters(getData().invalidParams().get(2), invalidParams.get(0), "invalid_params")
        );
    }

    @Test(description = "Успешное массовое редактирование поля sku - латиница", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"191876"})
    public void successMultiEditLatinSkuTest() {
        String expSku = RandomStringUtils.randomAlphabetic(7);
        metadataStepDef.successMultiEditMetadata(ids, SKU.getName(), expSku);

        for (String id : ids) {
            WaitHelper.withRetriesAsserted(
                    () -> {
                        ValidatableResponse response = metadataStepDef.checkMetadata(id).then();
                        Assert.compareParameters(expSku, response.extract().path(SKU.getPath()), "sku");
                    });
        }
    }

    @Test(description = "Успешное массовое редактирование поля sku - цифры и латиница", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"191876"})
    public void successMultiEditNumberAndLatinSkuTest() {
        String expSku = RandomStringUtils.randomAlphanumeric(3) +
                RandomStringUtils.randomNumeric(3) + RandomStringUtils.randomAlphabetic(3);
        metadataStepDef.successMultiEditMetadata(ids, SKU.getName(), expSku);

        for (String id : ids) {
            WaitHelper.withRetriesAsserted(
                    () -> {
                        ValidatableResponse response = metadataStepDef.checkMetadata(id).then();
                        Assert.compareParameters(expSku, response.extract().path(SKU.getPath()), "sku");
                    });
        }
    }

    @Test(description = "Успешное массовое редактирование поля sku = null", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"191876"})
    public void successMultiEditLatinSkuNullTest() {
        successMultiEditLatinSkuTest();

        metadataStepDef.successMultiEditMetadata(ids, SKU.getName(), null);

        for (String id : ids) {
            WaitHelper.withRetriesAsserted(
                    () -> {
                        ValidatableResponse response = metadataStepDef.checkMetadata(id).then();
                        Assert.mustBeNullParameter(response.extract().path(SKU.getPath()), "sku");
                    });
        }
    }

    @Test(description = "Некорректное массовое редактирование поля sku - кириллица", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"191877"})
    public void unsuccessfulMultiEditCyrillicSkuTest() {
        List<InvalidParamsItem> invalidParams =
                metadataStepDef.unsuccessfulMultiEditMetadata(ids, SKU.getName(), CYRILLIC_VALUE);
        getData().invalidParams().get(0).name(SKU.getName());
        assertAll(
                () -> compareParameters(1, invalidParams.size(), "invalid_params.size"),
                () -> compareParameters(getData().invalidParams().get(0), invalidParams.get(0), "invalid_params")
        );
    }

    @Test(description = "Некорректное массовое редактирование поля sku - длинное значение", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"191877"})
    public void unsuccessfulMultiEditLongSkuTest() {
        List<InvalidParamsItem> invalidParams =
                metadataStepDef.unsuccessfulMultiEditMetadata(ids, SKU.getName(), RandomStringUtils.randomAlphanumeric(190));
        getData().invalidParams().get(7).name(SKU.getName());
        assertAll(
                () -> compareParameters(1, invalidParams.size(), "invalid_params.size"),
                () -> compareParameters(getData().invalidParams().get(7), invalidParams.get(0), "invalid_params")
        );
    }

    @Test(description = "Успешное массовое редактирование priority", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"292747"})
    public void successMultiEditPriorityTest() {
        int priority = new Random().nextInt(FOUR_HUNDRED_NINETY_NINE) + 1;
        metadataStepDef.successMultiEditMetadata(ids, PRIORITY.getName(), String.valueOf(priority));

        for (String id : ids) {
            WaitHelper.withRetriesAsserted(
                    () -> {
                        ValidatableResponse response = metadataStepDef.checkMetadata(id).then();
                        Assert.compareParameters(priority, response.extract().path(PRIORITY.getPath()), "priority");
                    });
        }
    }

    @Test(description = "Успешное массовое редактирование priority = null", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"292747"})
    public void successMultiEditPriorityNullTest() {
        successMultiEditPriorityTest();

        metadataStepDef.successMultiEditMetadata(ids, PRIORITY.getName(), null);

        for (String id : ids) {
            WaitHelper.withRetriesAsserted(
                    () -> {
                        ValidatableResponse response = metadataStepDef.checkMetadata(id).then();
                        Assert.mustBeNullParameter(response.extract().path(PRIORITY.getPath()), "priority");
                    });
        }
    }

    @Test(description = "Неуспешное массовое редактирование priority - выход за пределы диапазона", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"292747"})
    public void unsuccessfulMultiEditPriorityTest() {
        int priority = new Random().nextInt(FOUR_HUNDRED_NINETY_NINE) + 1 + FOUR_HUNDRED_NINETY_NINE;
        List<InvalidParamsItem> invalidParams =
                metadataStepDef.unsuccessfulMultiEditMetadata(ids, PRIORITY.getName(), String.valueOf(priority));

        getData().invalidParams().get(5).name(PRIORITY.getName());
        assertAll(
                () -> compareParameters(1, invalidParams.size(), "invalid_params.size"),
                () -> compareParameters(getData().invalidParams().get(5), invalidParams.get(0), "invalid_params")
        );
    }

    @Test(description = "Некорректное массовое редактирование priority - InvalidType", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"292749"})
    public void unsuccessfulMultiEditPriorityInvalidTypeTest() {
        List<InvalidParamsItem> invalidParams =
                metadataStepDef.unsuccessfulMultiEditMetadata(ids, PRIORITY.getName(), RandomStringUtils.randomAlphabetic(7));

        getData().invalidParams().get(2).name(PRIORITY.getName());
        assertAll(
                () -> compareParameters(1, invalidParams.size(), "invalid_params.size"),
                () -> compareParameters(getData().invalidParams().get(2), invalidParams.get(0), "invalid_params")
        );
    }

    @Test(description = "Успешное массовое редактирование поля keywords добавление 1 записи", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"191868"})
    public void successMultiEditKeywordTest() {
        List<String> expKeywordList = Collections.singletonList(RandomStringUtils.randomAlphabetic(7));
        metadataStepDef.successMultiEditMetadata(ids, KEYWORDS.getName(), expKeywordList);

        for (String id : ids) {
            WaitHelper.withRetriesAsserted(
                    () -> {
                        ValidatableResponse response = metadataStepDef.checkMetadata(id).then();
                        Assert.compareParameters(expKeywordList, response.extract().path(KEYWORDS.getPath()), "keywords");
                    });
        }
    }

    @Test(description = "Успешное массовое редактирование поля keywords добавление нескольких записей", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"191868"})
    public void successMultiEditKeywordsTest() {
        List<String> expKeywordList = Arrays.asList(RandomStringUtils.randomAlphabetic(7),
                RandomStringUtils.randomAlphabetic(7),
                RandomStringUtils.randomAlphabetic(7));

        metadataStepDef.successMultiEditMetadata(ids, KEYWORDS.getName(), expKeywordList);

        for (String id : ids) {
            WaitHelper.withRetriesAsserted(
                    () -> {
                        ValidatableResponse response = metadataStepDef.checkMetadata(id).then();
                        Assert.compareParameters(expKeywordList, response.extract().path(KEYWORDS.getPath()), "keywords");
                    });
        }
    }

    @Test(description = "Успешное массовое редактирование поля keywords добавление записи к существующим", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"191869"})
    public void successMultiEditEmptyKeywordsTest() {
        List<String> keywords = Arrays.asList(RandomStringUtils.randomAlphabetic(7),
                RandomStringUtils.randomAlphabetic(7),
                RandomStringUtils.randomAlphabetic(7));

        metadataStepDef.successMultiEditMetadata(ids, KEYWORDS.getName(), keywords);

        List<String> newKeyword = new ArrayList<>(Collections.singletonList(RandomStringUtils.randomAlphabetic(5)));

        metadataStepDef.successMultiEditMetadata(ids, KEYWORDS.getName(), newKeyword);

        newKeyword.addAll(keywords);
        for (String id : ids) {
            WaitHelper.withRetriesAsserted(
                    () -> {
                        ValidatableResponse response = metadataStepDef.checkMetadata(id).then();
                        Assert.compareParameters(new LinkedList<>(newKeyword), new LinkedList<>(response.extract().path(KEYWORDS.getPath())), "keywords");
                    });
        }
    }

    @Test(description = "Успешное массовое редактирование поля keywords = null", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"191868"})
    public void successMultiEditKeywordNullTest() {
        successMultiEditKeywordTest();

        metadataStepDef.successMultiEditMetadata(ids, KEYWORDS.getName(), null);

        for (String id : ids) {
            WaitHelper.withRetriesAsserted(
                    () -> {
                        ValidatableResponse response = metadataStepDef.checkMetadata(id).then();
                        Assert.mustBeNullParameter(response.extract().path(KEYWORDS.getPath()), "keywords");
                    });
        }
    }

    @Test(description = "Успешное массовое редактирование поля source_id", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"191856"})
    public void successMultiEditSourceIdTest() {
        String expSourceId = sourcesItems.stream().filter(item -> item.name().equals(Source.BRAND.getName())).
                map(DictionariesItem::id).findFirst().orElse(null);

        metadataStepDef.successMultiEditMetadata(ids, SOURCE_ID.getName(), expSourceId);

        for (String id : ids) {
            WaitHelper.withRetriesAsserted(
                    () -> {
                        ValidatableResponse response = metadataStepDef.checkMetadata(id).then();
                        assertAll(
                                () -> compareParameters(Source.BRAND.getName(), response.extract().path(SOURCE_ID.getPath() + ".name"), "source.name"),
                                () -> compareParameters(expSourceId, response.extract().path(SOURCE_ID.getPath() + ".id"), "source.id")
                        );
                    }
            );
        }
    }

    @Test(description = "Успешное массовое редактирование поля source_id = null", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"191856"})
    public void successMultiEditSourceIdNullTest() {
        successMultiEditSourceIdTest();

        metadataStepDef.successMultiEditMetadata(ids, SOURCE_ID.getName(), null);

        for (String id : ids) {
            WaitHelper.withRetriesAsserted(
                    () -> {
                        ValidatableResponse response = metadataStepDef.checkMetadata(id).then();
                        mustBeNullParameter(response.extract().path(SOURCE_ID.getPath()), "source");
                    });
        }
    }

    @Test(description = "Некорректное массовое редактирование поля source_id - NotFound", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"191857"})
    public void unsuccessfulMultiEditSourceIdTest() {
        List<InvalidParamsItem> invalidParams =
                metadataStepDef.unsuccessfulMultiEditMetadata(ids, SOURCE_ID.getName(), UUID.randomUUID().toString());

        getData().invalidParams().get(1).name(SOURCE_ID.getName());
        assertAll(
                () -> compareParameters(1, invalidParams.size(), "invalid_params.size"),
                () -> compareParameters(getData().invalidParams().get(1), invalidParams.get(0), "invalid_params")
        );
    }

    @Test(description = "Некорректное массовое редактирование поля source_id - InvalidType", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"191857"})
    public void unsuccessfulMultiEditSourceIdInvalidUuidTest() {
        List<InvalidParamsItem> invalidParams =
                metadataStepDef.unsuccessfulMultiEditMetadata(ids, SOURCE_ID.getName(), RandomStringUtils.randomAlphabetic(5));

        getData().invalidParams().get(2).name(SOURCE_ID.getName());
        assertAll(
                () -> compareParameters(1, invalidParams.size(), "invalid_params.size"),
                () -> compareParameters(getData().invalidParams().get(2), invalidParams.get(0), "invalid_params")
        );
    }

    @Test(description = "Успешное массовое редактирование поля external_task_id", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"292750"})
    public void successMultiEditExternalTaskIdTest() {
        String expExternalTaskId = String.valueOf(1000 + Integer.parseInt(RandomStringUtils.randomNumeric(3)));

        metadataStepDef.successMultiEditMetadata(ids, EXTERNAL_TASK_ID.getName(), expExternalTaskId);

        for (String id : ids) {
            WaitHelper.withRetriesAsserted(
                    () -> {
                        ValidatableResponse response = metadataStepDef.checkMetadata(id).then();
                        compareParameters(expExternalTaskId, response.extract().path(EXTERNAL_TASK_ID.getPath()), "external_task_id");
                    });
        }
    }

    @Test(description = "Успешное массовое редактирование поля external_task_id = null", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"292750"})
    public void successMultiEditExternalTaskIdNullTest() {
        successMultiEditExternalTaskIdTest();

        metadataStepDef.successMultiEditMetadata(ids, EXTERNAL_TASK_ID.getName(), null);

        for (String id : ids) {
            WaitHelper.withRetriesAsserted(
                    () -> {
                        ValidatableResponse response = metadataStepDef.checkMetadata(id).then();
                        mustBeNullParameter(response.extract().path(EXTERNAL_TASK_ID.getPath()), "external_task_id");
                    });
        }
    }

    @Test(description = "Некорректное массовое редактирование поля external_task_id - InvalidType", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"292751"})
    public void unsuccessfulMultiEditExternalTaskIdInvalidUuidTest() {
        List<InvalidParamsItem> invalidParams =
                metadataStepDef.unsuccessfulMultiEditMetadata(ids, EXTERNAL_TASK_ID.getName(), RandomStringUtils.randomAlphabetic(5));

        getData().invalidParams().get(2).name(EXTERNAL_TASK_ID.getName());
        assertAll(
                () -> compareParameters(1, invalidParams.size(), "invalid_params.size"),
                () -> compareParameters(getData().invalidParams().get(2), invalidParams.get(0), "invalid_params")
        );
    }

    @Test(description = "Успешное массовое редактирование master_seller_id", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"246937"})
    public void successMultiEditMasterSellerTest() {
        RetailersItem retailer = retailersResponse.data().stream().findFirst().orElse(null);
        notNullOrEmptyParameter(retailer, "retailer");

        metadataStepDef.successMultiEditMetadata(ids, MASTER_SELLER_ID.getName(), retailer.id());

        for (String id : ids) {
            WaitHelper.withRetriesAsserted(
                    () -> {
                        ValidatableResponse response = metadataStepDef.checkMetadata(id).then();
                        assertAll(
                                () -> compareParameters(retailer.id(), response.extract().path(MASTER_SELLER.getPath() + ".id"), "master_seller.id"),
                                () -> compareParameters(retailer.name(), response.extract().path(MASTER_SELLER.getPath() + ".name"), "master_seller.name")
                        );
                    });
        }
    }

    @Test(description = "Успешное массовое редактирование master_seller_id = null", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"246937"})
    public void successMultiEditMasterSellerNullTest() {
        successMultiEditMasterSellerTest();

        metadataStepDef.successMultiEditMetadata(ids, MASTER_SELLER_ID.getName(), null);

        for (String id : ids) {
            WaitHelper.withRetriesAsserted(
                    () -> {
                        ValidatableResponse response = metadataStepDef.checkMetadata(id).then();
                        mustBeNullParameter(response.extract().path(MASTER_SELLER.getPath()), "master_seller");
                    });
        }
    }

    @Test(description = "Некорректное массовое редактирование master_seller_id - NotFound", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"246938"})
    public void unsuccessfulMultiEditMasterSellerNotFoundTest() {
        List<InvalidParamsItem> invalidParams =
                metadataStepDef.unsuccessfulMultiEditMetadata(ids, MASTER_SELLER_ID.getName(), UUID.randomUUID().toString());

        getData().invalidParams().get(1).name(MASTER_SELLER_ID.getName());
        assertAll(
                () -> compareParameters(1, invalidParams.size(), "invalid_params.size"),
                () -> compareParameters(getData().invalidParams().get(1), invalidParams.get(0), "invalid_params")
        );
    }

    @Test(description = "Некорректное массовое редактирование master_seller_id - InvalidType", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"246938"})
    public void unsuccessfulMultiEditMasterSellerInvalidTypeTest() {
        List<InvalidParamsItem> invalidParams =
                metadataStepDef.unsuccessfulMultiEditMetadata(ids, MASTER_SELLER_ID.getName(), RandomStringUtils.randomAlphabetic(5));

        getData().invalidParams().get(2).name(MASTER_SELLER_ID.getName());
        assertAll(
                () -> compareParameters(1, invalidParams.size(), "invalid_params.size"),
                () -> compareParameters(getData().invalidParams().get(2), invalidParams.get(0), "invalid_params")
        );
    }

    @Test(description = "Успешное массовое редактирование поля category_id", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"191860"})
    public void successMultiEditaCategoryIdTest() {
        String expCategoryId = categories.get(0).id();
        String expCategoryName = categories.get(0).name();

        metadataStepDef.successMultiEditMetadata(ids, MASTER_CATEGORY_ID.getName(), expCategoryId);

        for (String id : ids) {
            WaitHelper.withRetriesAsserted(
                    () -> {
                        ValidatableResponse response = metadataStepDef.checkMetadata(id).then();
                        assertAll(
                                () -> compareParameters(expCategoryName, response.extract().path(MASTER_CATEGORY_ID.getPath() + ".name"), "category.name"),
                                () -> compareParameters(expCategoryId, response.extract().path(MASTER_CATEGORY_ID.getPath() + ".id"), "category.id")
                        );
                    });
        }
    }

    @Test(description = "Успешное массовое редактирование поля category_id = null", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"191860"})
    public void successMultiEditaCategoryIdNullTest() {
        successMultiEditaCategoryIdTest();

        metadataStepDef.successMultiEditMetadata(ids, MASTER_CATEGORY_ID.getName(), null);

        for (String id : ids) {
            WaitHelper.withRetriesAsserted(
                    () -> {
                        ValidatableResponse response = metadataStepDef.checkMetadata(id).then();
                        mustBeNullParameter(response.extract().path(MASTER_CATEGORY_ID.getPath()), "category");
                    });
        }
    }

    @Test(description = "Некорректное массовое редактирование поля category_id - NotFound", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"191861"})
    public void unsuccessfulMultiEditCategoryIdTest() {
        List<InvalidParamsItem> invalidParams =
                metadataStepDef.unsuccessfulMultiEditMetadata(ids, MASTER_CATEGORY_ID.getName(), UUID.randomUUID().toString());

        getData().invalidParams().get(1).name(MASTER_CATEGORY_ID.getName());
        assertAll(
                () -> compareParameters(1, invalidParams.size(), "invalid_params.size"),
                () -> compareParameters(getData().invalidParams().get(1), invalidParams.get(0), "invalid_params")
        );
    }

    @Test(description = "Некорректное массовое редактирование поля category_id - InvalidType", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"191861"})
    public void unsuccessfulMultiEditCategoryIdIsNotAValidUuidTest() {
        List<InvalidParamsItem> invalidParams =
                metadataStepDef.unsuccessfulMultiEditMetadata(
                        ids, MASTER_CATEGORY_ID.getName(), RandomStringUtils.randomAlphabetic(5));

        getData().invalidParams().get(2).name(MASTER_CATEGORY_ID.getName());
        assertAll(
                () -> compareParameters(1, invalidParams.size(), "invalid_params.size"),
                () -> compareParameters(getData().invalidParams().get(2), invalidParams.get(0), "invalid_params")
        );
    }

    @Test(description = "Успешное массовое редактирование поля is_own_trademark", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"191862"})
    public void successMultiEditIsOwnTrademarkTest() {
        metadataStepDef.successMultiEditMetadata(ids, IS_OWN_TRADEMARK.getName(), true);

        for (String id : ids) {
            WaitHelper.withRetriesAsserted(
                    () -> {
                        ValidatableResponse response = metadataStepDef.checkMetadata(id).then();
                        notNullOrEmptyParameter(response.extract().path(IS_OWN_TRADEMARK.getPath()), "is_own_trademark");
                        equalsTrueParameter(response.extract().path(IS_OWN_TRADEMARK.getPath()), "is_own_trademark");
                    });
        }
    }

    @Test(description = "Успешное массовое редактирование поля is_own_trademark = null", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"191862"})
    public void successMultiEditIsOwnTrademarkNullTest() {
        successMultiEditIsOwnTrademarkTest();

        metadataStepDef.successMultiEditMetadata(ids, IS_OWN_TRADEMARK.getName(), null);

        for (String id : ids) {
            WaitHelper.withRetriesAsserted(
                    () -> {
                        ValidatableResponse response = metadataStepDef.checkMetadata(id).then();
                        mustBeNullParameter(response.extract().path(IS_OWN_TRADEMARK.getPath()), "is_own_trademark");
                    });
        }
    }

    @Test(description = "Некорректное массовое редактирование поля is_own_trademark - InvalidType", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"191863"})
    public void unsuccessfulMultiEditIsOwnTrademarkIsNotAValidBooleanTest() {
        List<InvalidParamsItem> invalidParams =
                metadataStepDef.unsuccessfulMultiEditMetadata(
                        ids, IS_OWN_TRADEMARK.getName(), RandomStringUtils.randomAlphabetic(5));
        getData().invalidParams().get(2).name(IS_OWN_TRADEMARK.getName());
        assertAll(
                () -> compareParameters(1, invalidParams.size(), "invalid_params.size"),
                () -> compareParameters(getData().invalidParams().get(2), invalidParams.get(0), "invalid_params")
        );
    }


    @Test(description = "Успешное массовое редактирование поля is_copyright", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"191866"})
    public void successMultiEditIsCopyrightTest() {
        metadataStepDef.successMultiEditMetadata(ids, IS_COPYRIGHT.getName(), true);

        for (String id : ids) {
            WaitHelper.withRetriesAsserted(
                    () -> {
                        ValidatableResponse response = metadataStepDef.checkMetadata(id).then();
                        notNullOrEmptyParameter(response.extract().path(IS_COPYRIGHT.getPath()), "is_copyright");
                        equalsTrueParameter(response.extract().path(IS_COPYRIGHT.getPath()), "is_copyright");
                    });
        }
    }

    @Test(description = "Успешное массовое редактирование поля is_copyright = null", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"191866"})
    public void successMultiEditIsCopyrightNullTest() {
        successMultiEditIsCopyrightTest();

        metadataStepDef.successMultiEditMetadata(ids, IS_COPYRIGHT.getName(), null);

        for (String id : ids) {
            WaitHelper.withRetriesAsserted(
                    () -> {
                        ValidatableResponse response = metadataStepDef.checkMetadata(id).then();
                        mustBeNullParameter(response.extract().path(IS_COPYRIGHT.getPath()), "is_copyright");
                    });
        }
    }

    @Test(description = "Некорректное массовое редактирование поля is_copyright - InvalidType", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"191867"})
    public void unsuccessfulMultiEditIsCopyrightIsNotAValidBooleanTest() {
        List<InvalidParamsItem> invalidParams =
                metadataStepDef.unsuccessfulMultiEditMetadata(ids, IS_COPYRIGHT.getName(), RandomStringUtils.randomAlphabetic(5));
        getData().invalidParams().get(2).name(IS_COPYRIGHT.getName());
        assertAll(
                () -> compareParameters(1, invalidParams.size(), "invalid_params.size"),
                () -> compareParameters(getData().invalidParams().get(2), invalidParams.get(0), "invalid_params")
        );
    }

    @Deprecated
    //@Test(description = "Успешное массовое редактирование поля received", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"191872"})
    public void successMultiEditReceivedTest() {
        String expReceived = RandomStringUtils.randomAlphabetic(7);

        RequestMultiEditData requestMultiEditData = RequestMultiEditData.builder()
                .ids(ids).received(expReceived).build();

        metadataStepDef.successMultiEditMetadata(requestMultiEditData);

        for (String id : ids) {
            ValidatableResponse response = metadataStepDef.checkMetadata(id).then();
            Assert.compareParameters(expReceived, response.extract().path(RECEIVED.getPath()), "received");
        }
    }

    @Deprecated
    //@Test(description = "Успешное массовое редактирование поля received на пустое значение", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"191873"})
    public void successMultiEditEmptyReceivedTest() {
        String received = RandomStringUtils.randomAlphabetic(7);

        RequestMultiEditData requestMultiEditData = RequestMultiEditData.builder()
                .ids(ids).received(received).build();

        metadataStepDef.successMultiEditMetadata(requestMultiEditData);

        requestMultiEditData.setReceived(EMPTY_VALUE);

        metadataStepDef.successMultiEditMetadata(requestMultiEditData);

        for (String id : ids) {
            ValidatableResponse response = metadataStepDef.checkMetadata(id).then();
            Assert.compareParameters(EMPTY_VALUE, response.extract().path(RECEIVED.getPath()), "received");
        }
    }

    @Test(description = "Успешное массовое редактирование всех полей", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"191878"})
    public void successMultiEditAllFieldSkuTest() {
        String expStatusId = statusesItems.stream().filter(item -> item.name().equals(Status.DELETE.getName())).
                map(DictionariesItem::id).findFirst().orElse(null);
        String expQualityId = qualitiesItems.stream().filter(item -> item.name().equals(Quality.GOOD.getName())).
                map(DictionariesItem::id).findFirst().orElse(null);
        String expSourceId = sourcesItems.stream().filter(item -> item.name().equals(Source.BRAND.getName())).
                map(DictionariesItem::id).findFirst().orElse(null);
        String expAssigneeId = usersItems.get(0).id();
        String expCategoryId = categories.get(0).id();
        List<String> expKeywordList = Arrays.asList(RandomStringUtils.randomAlphabetic(7),
                RandomStringUtils.randomAlphabetic(7),
                RandomStringUtils.randomAlphabetic(7));
        String expSku = RandomStringUtils.randomAlphanumeric(3) + RandomStringUtils.randomNumeric(3) + RandomStringUtils.randomAlphabetic(3);
        int priority = new Random().nextInt(FOUR_HUNDRED_NINETY_NINE) + 1;
        String expExternalTaskId = String.valueOf(1000 + Integer.parseInt(RandomStringUtils.randomNumeric(3)));
        RetailersItem retailer = retailersResponse.data().stream().findFirst().orElse(null);

        Map<String, Object> newValues = new HashMap<>() {{
            put(STATUS_ID.getName(), expStatusId);
            put(QUALITY_ID.getName(), expQualityId);
            put(ASSIGNEE_ID.getName(), expAssigneeId);
            put(SKU.getName(), expSku);
            put(PRIORITY.getName(), String.valueOf(priority));
            put(KEYWORDS.getName(), expKeywordList);
            put(SOURCE_ID.getName(), expSourceId);
            put(EXTERNAL_TASK_ID.getName(), expExternalTaskId);
            put(MASTER_SELLER_ID.getName(), retailer.id());
            put(MASTER_CATEGORY_ID.getName(), expCategoryId);
            put(IS_OWN_TRADEMARK.getName(), true);
            put(IS_COPYRIGHT.getName(), true);
        }};

        metadataStepDef.successMultiEditMetadata(ids, newValues);

        for (String id : ids) {
            WaitHelper.withRetriesAsserted(
                    () -> {
                        ValidatableResponse response = metadataStepDef.checkMetadata(id).then();
                        notNullOrEmptyParameter(response.extract().path(IS_OWN_TRADEMARK.getPath()), "is_own_trademark");

                        assertAll(
                                () -> compareParameters(Status.DELETE.getName(), response.extract().path(STATUS_ID.getPath() + ".name"), "status.name"),
                                () -> compareParameters(Quality.GOOD.getName(), response.extract().path(QUALITY_ID.getPath() + ".name"), "quality.name"),
                                () -> compareParameters(expAssigneeId, response.extract().path(ASSIGNEE_ID.getPath() + ".id"), "assignee.id"),
                                () -> compareParameters(expSku, response.extract().path(SKU.getPath()), "sku"),
                                () -> compareParameters(priority, response.extract().path(PRIORITY.getPath()), "priority"),
                                () -> compareParameters(expKeywordList, response.extract().path(KEYWORDS.getPath()), "keywords"),
                                () -> compareParameters(Source.BRAND.getName(), response.extract().path(SOURCE_ID.getPath() + ".name"), "source.name"),
                                () -> compareParameters(expExternalTaskId, response.extract().path(EXTERNAL_TASK_ID.getPath()), "external_task_id"),
                                () -> compareParameters(retailer.name(), response.extract().path(MASTER_SELLER.getPath() + ".name"), "master_seller.name"),
                                () -> compareParameters(expCategoryId, response.extract().path(MASTER_CATEGORY_ID.getPath() + ".id"), "master_category.id"),
                                () -> equalsTrueParameter(response.extract().path(IS_COPYRIGHT.getPath()), "is_copyright"),
                                () -> equalsTrueParameter(response.extract().path(IS_OWN_TRADEMARK.getPath()), "is_own_trademark")
                        );
                    });
        }
    }

    @Test(description = "Некорректное массовое редактирование нескольких полей", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"191879"})
    public void unsuccessfulMultiEditAllFieldSkuTest() {
        Map<String, Object> newValues = new HashMap<>() {{
            put(STATUS_ID.getName(), UUID.randomUUID().toString());
            put(MASTER_CATEGORY_ID.getName(), UUID.randomUUID().toString());
            put(IS_OWN_TRADEMARK.getName(), true);
        }};

        List<InvalidParamsItem> invalidParams = metadataStepDef.unsuccessfulMultiEditMetadata(ids, newValues);

        List<InvalidParamsItem> expectedInvalidParams = new ArrayList<>() {{
            getData().invalidParams().get(1).name(STATUS_ID.getName());
            add(new InvalidParamsItem(getData().invalidParams().get(1)));

            getData().invalidParams().get(1).name(MASTER_CATEGORY_ID.getName());
            add(new InvalidParamsItem(getData().invalidParams().get(1)));
        }};

        Assert.compareParameters(expectedInvalidParams, invalidParams, "invalid_params");
    }
}
