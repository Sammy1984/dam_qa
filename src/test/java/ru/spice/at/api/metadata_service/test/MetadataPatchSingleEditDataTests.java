package ru.spice.at.api.metadata_service.test;

import io.qameta.allure.Feature;
import io.qameta.allure.Issue;
import io.qameta.allure.Story;
import lombok.extern.log4j.Log4j2;
import org.awaitility.Awaitility;
import ru.spice.at.api.import_service.ImportServiceStepDef;
import ru.testit.annotations.WorkItemIds;
import io.restassured.response.Response;
import org.apache.commons.lang3.RandomStringUtils;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.spice.at.api.dto.response.metadata.*;
import ru.spice.at.api.metadata_service.MetadataSettings;
import ru.spice.at.api.metadata_service.MetadataStepDef;
import ru.spice.at.common.base_test.BaseApiTest;
import ru.spice.at.common.dto.dam.ImageData;
import ru.spice.at.common.emuns.ApiServices;
import ru.spice.at.common.emuns.dam.*;
import ru.spice.at.common.utils.Assert;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static ru.spice.at.common.constants.TestConstants.*;
import static ru.spice.at.common.emuns.dam.ImageFormat.*;
import static ru.spice.at.common.emuns.dam.ImageParameters.*;
import static ru.spice.at.common.emuns.dam.ProcessingTypeEnum.NORMALIZATION;
import static ru.spice.at.common.emuns.dam.Status.ARCHIVE;
import static ru.spice.at.common.emuns.dam.Status.NEW;
import static ru.spice.at.common.utils.Assert.*;
import static ru.spice.at.common.utils.ImageHelper.getRandomByteImage;
import static ru.spice.at.common.utils.JsonHelper.getValueFromResponse;

@Log4j2
@Feature("Metadata Service")
@Story("PATCH single edit data")
public class MetadataPatchSingleEditDataTests extends BaseApiTest<MetadataSettings> {
    private final MetadataStepDef metadataStepDef;
    private final ImportServiceStepDef importServiceStepDef;
    private String id;

    protected MetadataPatchSingleEditDataTests() {
        super(ApiServices.METADATA_SERVICE);
        metadataStepDef = new MetadataStepDef();
        importServiceStepDef = new ImportServiceStepDef();
    }

    @BeforeClass(alwaysRun = true)
    public void beforeClass() {
        id = metadataStepDef.createMetadataImage(new ImageData(ImageFormat.JPEG));
    }

    //todo выключено для использования параллельного сьюта
    //@AfterClass(alwaysRun = true)
    public void afterClass() {
        metadataStepDef.deleteMetadata();
    }

    @Test(description = "Успешное редактирование поля filename", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"191816"})
    public void successEditFilenameTest() {
        String newFilename = new ImageData(ImageFormat.JPEG).getFilename();
        Response response = metadataStepDef.successEditMetadata(id, FILENAME.getName(), newFilename);
        Assert.compareParameters(newFilename, getValueFromResponse(response, FILENAME.getPath()), "filename");
    }

    @Test(description = "Успешное редактирование поля filename (длинное название)", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"191816"})
    public void successEditFilenameLongNameTest() {
        String newFilename = RandomStringUtils.randomAlphabetic(MAX_JPG_FILENAME_SYMBOLS) + DOT_VALUE + ImageFormat.JPG.getFormatName();
        Response response = metadataStepDef.successEditMetadata(id, FILENAME.getName(), newFilename);
        Assert.compareParameters(newFilename, getValueFromResponse(response, FILENAME.getPath()), "filename");
    }

    @Test(description = "Некорректное редактирование поля filename - формат", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"191817"})
    public void unsuccessfulEditFilenameFormatTest() {
        getData().invalidFormatFilenames().forEach(invalidFilename -> {
            List<InvalidParamsItem> invalidParams = metadataStepDef.unsuccessfulEditMetadata(id, FILENAME.getName(), invalidFilename);
            assertAll(
                    () -> compareParameters(1, invalidParams.size(), "invalid_params.size"),
                    () -> compareParameters(getData().invalidParams().get(8), invalidParams.get(0), "invalid_params")
            );
        });
    }

    @Test(description = "Некорректное редактирование поля filename - длинное название", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"191817"})
    public void unsuccessfulEditFilenameLongNameTest() {
        metadataStepDef.unsuccessfulServerErrorEditMetadata(id,
                Collections.singletonMap(FILENAME.getName(), RandomStringUtils.randomAlphabetic(MAX_JPG_FILENAME_SYMBOLS + 1) + DOT_VALUE + ImageFormat.JPEG.getFormatName()));
    }

    @Test(description = "Некорректное редактирование поля origin_filename", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"191818"})
    public void successEditHeaderTest() {
        String newHeader = RandomStringUtils.randomAlphabetic(6);
        Response response = metadataStepDef.successEditMetadata(id, ORIGIN_FILENAME.getName(), newHeader);
        Assert.equalsFalseParameter(newHeader.equals(getValueFromResponse(response, ORIGIN_FILENAME.getPath())), "origin_filename");
    }

    @Test(description = "Некорректное редактирование поля origin_filename на пустое значение", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"191819"})
    public void successEditEmptyHeaderTest() {
        Response response = metadataStepDef.successEditMetadata(id, ORIGIN_FILENAME.getName(), EMPTY_VALUE);
        Assert.notNullOrEmptyParameter(getValueFromResponse(response, ORIGIN_FILENAME.getPath()), "origin_filename");
    }

    @Test(description = "Успешное редактирование поля description", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"191820"})
    public void successEditDescriptionTest() {
        String newDescription = RandomStringUtils.randomAlphabetic(6);
        Response response = metadataStepDef.successEditMetadata(id, DESCRIPTION.getName(), newDescription);
        Assert.compareParameters(newDescription, getValueFromResponse(response, DESCRIPTION.getPath()), "description");
    }

    @Test(description = "Успешное редактирование поля description - длинное значение (граница)", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"191820"})
    public void successEditDescriptionLongTest() {
        String newDescription = RandomStringUtils.randomAlphabetic(500);
        Response response = metadataStepDef.successEditMetadata(id, DESCRIPTION.getName(), newDescription);
        Assert.compareParameters(newDescription, getValueFromResponse(response, DESCRIPTION.getPath()), "description");
    }

    @Test(description = "Неуспешное редактирование поля description - длинное значение", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"233176"})
    public void unsuccessfulEditDescriptionLongTest() {
        List<InvalidParamsItem> invalidParams = metadataStepDef.unsuccessfulEditMetadata(id,
                DESCRIPTION.getName(), RandomStringUtils.randomAlphabetic(501));
        getData().invalidParams().get(7).name(DESCRIPTION.getName());
        assertAll(
                () -> compareParameters(1, invalidParams.size(), "invalid_params.size"),
                () -> compareParameters(getData().invalidParams().get(7), invalidParams.get(0), "invalid_params")
        );
    }

    @Test(description = "Успешное редактирование поля description на пустое значение", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"191821"})
    public void successEditEmptyDescriptionTest() {
        metadataStepDef.successEditMetadata(id, DESCRIPTION.getName(), RandomStringUtils.randomAlphabetic(6));
        Response response = metadataStepDef.successEditMetadata(id, DESCRIPTION.getName(), EMPTY_VALUE);
        Assert.compareParameters(EMPTY_VALUE, getValueFromResponse(response, DESCRIPTION.getPath()), "description");
    }

    @Test(description = "Успешное редактирование поля status_id", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"191822"})
    public void successEditStatusIdTest() {
        List<DictionariesItem> dictionaries = metadataStepDef.getListStatusesMetadata();
        String deleteStatusId = dictionaries.stream().filter(item -> item.name().equals(Status.DELETE.getName())).
                map(DictionariesItem::id).findFirst().orElse(null);
        Response response = metadataStepDef.successEditMetadata(id, STATUS_ID.getName(), deleteStatusId);
        assertAll(
                () -> compareParameters(deleteStatusId, getValueFromResponse(response, STATUS_ID.getPath() + ".id"), "status_id"),
                () -> compareParameters(Status.DELETE.getName(), getValueFromResponse(response, STATUS_ID.getPath() + ".name"), "status_name")
        );
    }

    @Test(description = "Некорректное редактирование поля status_id - NotFound", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"191823"})
    public void unsuccessfulEditStatusIdTest() {
        List<InvalidParamsItem> invalidParams = metadataStepDef.unsuccessfulEditMetadata(id, STATUS_ID.getName(), UUID.randomUUID().toString());
        getData().invalidParams().get(1).name(STATUS_ID.getName());
        assertAll(
                () -> compareParameters(1, invalidParams.size(), "invalid_params.size"),
                () -> compareParameters(getData().invalidParams().get(1), invalidParams.get(0), "invalid_params")
        );
    }

    @Test(description = "Некорректное редактирование поля status_id - InvalidType", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"191823"})
    public void unsuccessfulEditStatusIdInvalidUuidTest() {
        List<InvalidParamsItem> invalidParams = metadataStepDef.unsuccessfulEditMetadata(id, STATUS_ID.getName(), UUID.randomUUID() + "test");
        getData().invalidParams().get(2).name(STATUS_ID.getName());
        assertAll(
                () -> compareParameters(1, invalidParams.size(), "invalid_params.size"),
                () -> compareParameters(getData().invalidParams().get(2), invalidParams.get(0), "invalid_params")
        );
    }

    @Test(description = "Успешное редактирование поля quality_id", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"191824"})
    public void successEditQualityIdTest() {
        List<DictionariesItem> dictionaries = metadataStepDef.getListQualitiesMetadata();
        String goodQualityId = dictionaries.stream().filter(item -> item.name().equals(Quality.GOOD.getName())).
                map(DictionariesItem::id).findFirst().orElse(null);
        Response response = metadataStepDef.successEditMetadata(id, QUALITY_ID.getName(), goodQualityId);
        assertAll(
                () -> compareParameters(goodQualityId, getValueFromResponse(response, QUALITY_ID.getPath() + ".id"), "quality_id"),
                () -> compareParameters(Quality.GOOD.getName(), getValueFromResponse(response, QUALITY_ID.getPath() + ".name"), "quality_name")
        );
    }

    @Test(description = "Некорректное редактирование поля quality_id - NotFound", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"191825"})
    public void unsuccessfulEditQualityIdTest() {
        List<InvalidParamsItem> invalidParams = metadataStepDef.unsuccessfulEditMetadata(id, QUALITY_ID.getName(), UUID.randomUUID().toString());
        getData().invalidParams().get(1).name(QUALITY_ID.getName());
        assertAll(
                () -> compareParameters(1, invalidParams.size(), "invalid_params.size"),
                () -> compareParameters(getData().invalidParams().get(1), invalidParams.get(0), "invalid_params")
        );
    }

    @Test(description = "Некорректное редактирование поля quality_id - InvalidType", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"191825"})
    public void unsuccessfulEditQualityIdInvalidUuidTest() {
        List<InvalidParamsItem> invalidParams = metadataStepDef.unsuccessfulEditMetadata(id, QUALITY_ID.getName(), UUID.randomUUID() + "test");
        getData().invalidParams().get(2).name(QUALITY_ID.getName());
        assertAll(
                () -> compareParameters(1, invalidParams.size(), "invalid_params.size"),
                () -> compareParameters(getData().invalidParams().get(2), invalidParams.get(0), "invalid_params")
        );
    }

    @Test(description = "Успешное редактирование master_seller", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"246928"})
    public void successEditMasterSellerTest() {
        RetailersResponse retailersResponse = metadataStepDef.getListRetailersMetadata();

        RetailersItem retailer = retailersResponse.data().stream().findFirst().orElse(null);
        notNullOrEmptyParameter(retailer, "retailer");

        Response response = metadataStepDef.successEditMetadata(id, MASTER_SELLER_ID.getName(), retailer.id());
        assertAll(
                () -> compareParameters(retailer.id(), getValueFromResponse(response, MASTER_SELLER.getPath() + ".id"), "master_seller.id"),
                () -> compareParameters(retailer.name(), getValueFromResponse(response, MASTER_SELLER.getPath() + ".name"), "master_seller.name")
        );
    }

    @Test(description = "Успешное редактирование master_seller - null", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"251289"})
    public void successEditMasterSellerNullTest() {
        successEditMasterSellerTest();
        Response response = metadataStepDef.successEditMetadata(id, MASTER_SELLER_ID.getName(), null);
        Assert.mustBeNullParameter(getValueFromResponse(response, MASTER_SELLER.getPath()), "master_seller");
    }

    @Test(description = "Некорректное редактирование master_seller - NotFound", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"246936"})
    public void unsuccessfulMasterSellerNotFoundTest() {
        List<InvalidParamsItem> invalidParams = metadataStepDef.unsuccessfulEditMetadata(id, MASTER_SELLER_ID.getName(), UUID.randomUUID().toString());
        getData().invalidParams().get(1).name(MASTER_SELLER_ID.getName());
        assertAll(
                () -> compareParameters(1, invalidParams.size(), "invalid_params.size"),
                () -> compareParameters(getData().invalidParams().get(1), invalidParams.get(0), "invalid_params")
        );
    }

    @Test(description = "Некорректное редактирование master_seller - InvalidType", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"246936"})
    public void unsuccessfulMasterSellerInvalidTypeTest() {
        List<InvalidParamsItem> invalidParams = metadataStepDef.unsuccessfulEditMetadata(id, MASTER_SELLER_ID.getName(), UUID.randomUUID() + "test");
        getData().invalidParams().get(2).name(MASTER_SELLER_ID.getName());
        assertAll(
                () -> compareParameters(1, invalidParams.size(), "invalid_params.size"),
                () -> compareParameters(getData().invalidParams().get(2), invalidParams.get(0), "invalid_params")
        );
    }

    @Test(description = "Успешное редактирование поля source_id", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"191826"})
    public void successEditSourceIdTest() {
        List<DictionariesItem> dictionaries = metadataStepDef.getListSourcesMetadata();
        String brandSourceId = dictionaries.stream().filter(item -> item.name().equals(Source.BRAND.getName())).
                map(DictionariesItem::id).findFirst().orElse(null);
        Response response = metadataStepDef.successEditMetadata(id, SOURCE_ID.getName(), brandSourceId);
        assertAll(
                () -> compareParameters(brandSourceId, getValueFromResponse(response, SOURCE_ID.getPath() + ".id"), "source_id"),
                () -> compareParameters(Source.BRAND.getName(), getValueFromResponse(response, SOURCE_ID.getPath() + ".name"), "source_name")
        );
    }

    @Test(description = "Успешное редактирование поля source_id - null", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"251297"})
    public void successEditSourceIdNullTest() {
        successEditSourceIdTest();
        Response response = metadataStepDef.successEditMetadata(id, SOURCE_ID.getName(), null);
        Assert.mustBeNullParameter(getValueFromResponse(response, SOURCE_ID.getPath()), "source");
    }

    @Test(description = "Некорректное редактирование поля source_id - NotFound", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"191827"})
    public void unsuccessfulEditSourceIdTest() {
        List<InvalidParamsItem> invalidParams = metadataStepDef.unsuccessfulEditMetadata(id, SOURCE_ID.getName(), UUID.randomUUID().toString());
        getData().invalidParams().get(1).name(SOURCE_ID.getName());
        assertAll(
                () -> compareParameters(1, invalidParams.size(), "invalid_params.size"),
                () -> compareParameters(getData().invalidParams().get(1), invalidParams.get(0), "invalid_params")
        );
    }

    @Test(description = "Некорректное редактирование поля source_id - InvalidType", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"191827"})
    public void unsuccessfulEditSourceIdInvalidUuidTest() {
        List<InvalidParamsItem> invalidParams = metadataStepDef.unsuccessfulEditMetadata(id, SOURCE_ID.getName(), UUID.randomUUID() + "test");
        getData().invalidParams().get(2).name(SOURCE_ID.getName());
        assertAll(
                () -> compareParameters(1, invalidParams.size(), "invalid_params.size"),
                () -> compareParameters(getData().invalidParams().get(2), invalidParams.get(0), "invalid_params")
        );
    }

    @Test(description = "Успешное редактирование поля assignee_id", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"191828"})
    public void successEditAssigneeIdTest() {
        List<UsersItem> dictionaries = metadataStepDef.getListUsersMetadata();
        String assigneeId = dictionaries.stream().filter(item -> item.fullName().equals(getData().assigneeName())).
                map(UsersItem::id).findFirst().orElse(null);
        Response response = metadataStepDef.successEditMetadata(id, ASSIGNEE_ID.getName(), assigneeId);
        assertAll(
                () -> compareParameters(assigneeId, getValueFromResponse(response, ASSIGNEE_ID.getPath() + ".id"), "assignee_id"),
                () -> compareParameters(getData().assigneeName(), getValueFromResponse(response, ASSIGNEE_ID.getPath() + ".full_name"), "assignee_name")
        );
    }

    @Test(description = "Некорректное редактирование поля assignee_id - NotFound", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"191829"})
    public void unsuccessfulEditAssigneeIdTest() {
        List<InvalidParamsItem> invalidParams = metadataStepDef.unsuccessfulEditMetadata(id, ASSIGNEE_ID.getName(), UUID.randomUUID().toString());
        getData().invalidParams().get(1).name(ASSIGNEE_ID.getName());
        assertAll(
                () -> compareParameters(1, invalidParams.size(), "invalid_params.size"),
                () -> compareParameters(getData().invalidParams().get(1), invalidParams.get(0), "invalid_params")
        );
    }

    @Test(description = "Некорректное редактирование поля assignee_id - InvalidType", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"191829"})
    public void unsuccessfulEditAssigneeIdInvalidUuidTest() {
        List<InvalidParamsItem> invalidParams = metadataStepDef.unsuccessfulEditMetadata(id, ASSIGNEE_ID.getName(), UUID.randomUUID() + "test");
        getData().invalidParams().get(2).name(ASSIGNEE_ID.getName());
        assertAll(
                () -> compareParameters(1, invalidParams.size(), "invalid_params.size"),
                () -> compareParameters(getData().invalidParams().get(2), invalidParams.get(0), "invalid_params")
        );
    }

    @Test(description = "Успешное редактирование поля category_id", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"191830"})
    public void successEditCategoryIdTest() {
        List<DictionariesItem> dictionaries = metadataStepDef.getListCategoriesMetadata();
        String categoryId = dictionaries.stream().filter(item -> item.name().equals(getData().categoryName())).
                map(DictionariesItem::id).findFirst().orElse(null);
        Response response = metadataStepDef.successEditMetadata(id, MASTER_CATEGORY_ID.getName(), categoryId);
        assertAll(
                () -> compareParameters(categoryId, getValueFromResponse(response, MASTER_CATEGORY_ID.getPath() + ".id"), "category_id"),
                () -> compareParameters(getData().categoryName(), getValueFromResponse(response, MASTER_CATEGORY_ID.getPath() + ".name"), "category_name")
        );
    }

    @Test(description = "Успешное редактирование поля master_category_id - null", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"251298"})
    public void successEditCategoryIdNullTest() {
        successEditCategoryIdTest();
        Response response = metadataStepDef.successEditMetadata(id, MASTER_CATEGORY_ID.getName(), null);
        Assert.mustBeNullParameter(getValueFromResponse(response, MASTER_CATEGORY_ID.getPath()), "master_category");
    }

    @Test(description = "Некорректное редактирование поля category_id - NotFound", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"191831"})
    public void unsuccessfulEditCategoryIdTest() {
        List<InvalidParamsItem> invalidParams = metadataStepDef.unsuccessfulEditMetadata(id, MASTER_CATEGORY_ID.getName(), UUID.randomUUID().toString());
        getData().invalidParams().get(1).name(MASTER_CATEGORY_ID.getName());
        assertAll(
                () -> compareParameters(1, invalidParams.size(), "invalid_params.size"),
                () -> compareParameters(getData().invalidParams().get(1), invalidParams.get(0), "invalid_params")
        );
    }

    @Test(description = "Некорректное редактирование поля category_id - InvalidType", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"191831"})
    public void unsuccessfulEditCategoryIdInvalidUuidTest() {
        List<InvalidParamsItem> invalidParams = metadataStepDef.unsuccessfulEditMetadata(id, MASTER_CATEGORY_ID.getName(), UUID.randomUUID() + "test");
        getData().invalidParams().get(2).name(MASTER_CATEGORY_ID.getName());
        assertAll(
                () -> compareParameters(1, invalidParams.size(), "invalid_params.size"),
                () -> compareParameters(getData().invalidParams().get(2), invalidParams.get(0), "invalid_params")
        );
    }

    @Test(description = "Успешное редактирование поля is_own_trademark", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"191832"})
    public void successEditIsOwnTrademarkTest() {
        Response response = metadataStepDef.successEditMetadata(id, IS_OWN_TRADEMARK.getName(), true);
        Assert.equalsTrueParameter(getValueFromResponse(response, IS_OWN_TRADEMARK.getPath()), "is_own_trademark");
    }

    @Test(description = "Успешное редактирование поля is_own_trademark - null", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"251300"})
    public void successEditIsOwnTrademarkNullTest() {
        Response response = metadataStepDef.successEditMetadata(id, IS_OWN_TRADEMARK.getName(), null);
        Assert.mustBeNullParameter(getValueFromResponse(response, IS_OWN_TRADEMARK.getPath()), "is_own_trademark");
    }

    @Test(description = "Некорректное редактирование поля is_own_trademark - InvalidType", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"191833"})
    public void unsuccessfulEditIsOwnTrademarkTest() {
        List<InvalidParamsItem> invalidParams = metadataStepDef.unsuccessfulEditMetadata(id, IS_OWN_TRADEMARK.getName(), RandomStringUtils.randomAlphabetic(6));
        getData().invalidParams().get(2).name(IS_OWN_TRADEMARK.getName());
        assertAll(
                () -> compareParameters(1, invalidParams.size(), "invalid_params.size"),
                () -> compareParameters(getData().invalidParams().get(2), invalidParams.get(0), "invalid_params")
        );
    }

    @Deprecated
    //@Test(description = "Успешное редактирование поля is_main_image", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"191834"})
    public void successEditIsMainImageTest() {
        Response response = metadataStepDef.successEditMetadata(id, IS_MAIN_IMAGE.getName(), true);
        Assert.equalsTrueParameter(getValueFromResponse(response, IS_MAIN_IMAGE.getPath()), "is_main_image");
    }

    @Deprecated
    //@Test(description = "Некорректное редактирование поля is_main_image - InvalidType", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"191835"})
    public void unsuccessfulEditIsMainImageTest() {
        List<InvalidParamsItem> invalidParams = metadataStepDef.unsuccessfulEditMetadata(id, IS_MAIN_IMAGE.getName(), RandomStringUtils.randomAlphabetic(6));
        getData().invalidParams().get(2).name(IS_MAIN_IMAGE.getName());
        assertAll(
                () -> compareParameters(1, invalidParams.size(), "invalid_params.size"),
                () -> compareParameters(getData().invalidParams().get(2), invalidParams.get(0), "invalid_params")
        );
    }

    @Test(description = "Успешное редактирование поля is_copyright", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"191836"})
    public void successEditIsCopyrightTest() {
        Response response = metadataStepDef.successEditMetadata(id, IS_COPYRIGHT.getName(), true);
        Assert.equalsTrueParameter(getValueFromResponse(response, IS_COPYRIGHT.getPath()), "is_copyright");
    }

    @Test(description = "Успешное редактирование поля is_copyright - null", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"251301"})
    public void successEditIsCopyrightNullTest() {
        Response response = metadataStepDef.successEditMetadata(id, IS_COPYRIGHT.getName(), null);
        Assert.mustBeNullParameter(getValueFromResponse(response, IS_COPYRIGHT.getPath()), "is_copyright");
    }

    @Test(description = "Некорректное редактирование поля is_copyright - InvalidType", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"191837"})
    public void unsuccessfulEditIsCopyrightTest() {
        List<InvalidParamsItem> invalidParams = metadataStepDef.unsuccessfulEditMetadata(id, IS_COPYRIGHT.getName(), RandomStringUtils.randomAlphabetic(6));
        getData().invalidParams().get(2).name(IS_COPYRIGHT.getName());
        assertAll(
                () -> compareParameters(1, invalidParams.size(), "invalid_params.size"),
                () -> compareParameters(getData().invalidParams().get(2), invalidParams.get(0), "invalid_params")
        );
    }

    @Test(description = "Успешное редактирование поля is_raw_image", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"191838"})
    public void successEditIsRawImageTest() {
        Response response = metadataStepDef.successEditMetadata(id, IS_RAW_IMAGE.getName(), true);
        Assert.equalsTrueParameter(getValueFromResponse(response, IS_RAW_IMAGE.getPath()), "is_raw_image");
    }

    @Test(description = "Успешное редактирование поля is_raw_image - null", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"251299"})
    public void successEditIsRawImageNullTest() {
        Response response = metadataStepDef.successEditMetadata(id, IS_RAW_IMAGE.getName(), null);
        Assert.mustBeNullParameter(getValueFromResponse(response, IS_RAW_IMAGE.getPath()), "is_raw_image");
    }

    @Test(description = "Некорректное редактирование поля is_raw_image - InvalidType", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"191839"})
    public void unsuccessfulEditIsRawImageTest() {
        List<InvalidParamsItem> invalidParams = metadataStepDef.unsuccessfulEditMetadata(id, IS_RAW_IMAGE.getName(), RandomStringUtils.randomAlphabetic(6));
        getData().invalidParams().get(2).name(IS_RAW_IMAGE.getName());
        assertAll(
                () -> compareParameters(1, invalidParams.size(), "invalid_params.size"),
                () -> compareParameters(getData().invalidParams().get(2), invalidParams.get(0), "invalid_params")
        );
    }

    @Test(description = "Успешное редактирование поля keywords", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"191840"})
    public void successEditKeywordTest() {
        List<String> keywords = Collections.singletonList(RandomStringUtils.randomAlphabetic(6));
        Response response = metadataStepDef.successEditMetadata(id, KEYWORDS.getName(), keywords);
        Assert.compareParameters(keywords, getValueFromResponse(response, KEYWORDS.getPath()), "keywords");
    }

    @Test(description = "Успешное редактирование поля keywords - длинное значение (граница)", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"191840"})
    public void successEditKeywordLongTest() {
        List<String> keywords = Collections.singletonList(RandomStringUtils.randomAlphabetic(30));
        Response response = metadataStepDef.successEditMetadata(id, KEYWORDS.getName(), keywords);
        Assert.compareParameters(keywords, getValueFromResponse(response, KEYWORDS.getPath()), "keywords");
    }

    @Issue("SPC-1269")
    @Test(description = "Неуспешное редактирование поля keywords - длинное значение", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"233177"})
    public void unsuccessfulEditKeywordLongTest() {
        List<InvalidParamsItem> invalidParams = metadataStepDef.unsuccessfulEditMetadata(id,
                KEYWORDS.getName(), Collections.singletonList(RandomStringUtils.randomAlphabetic(31)));
        getData().invalidParams().get(7).name(KEYWORDS.getName());
        assertAll(
                () -> compareParameters(1, invalidParams.size(), "invalid_params.size"),
                () -> compareParameters(getData().invalidParams().get(7), invalidParams.get(0), "invalid_params")
        );
    }

    @Test(description = "Успешное редактирование поля keywords - несколько тегов", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"191840"})
    public void successEditKeywordsTest() {
        List<String> keywords = new ArrayList<String>() {{
            add(RandomStringUtils.randomAlphabetic(6));
            add(RandomStringUtils.randomAlphabetic(15));
            add(RandomStringUtils.randomAlphabetic(3));
        }};
        Response response = metadataStepDef.successEditMetadata(id, KEYWORDS.getName(), keywords);
        Assert.compareParameters(keywords, getValueFromResponse(response, KEYWORDS.getPath()), "keywords");
    }

    @Test(description = "Успешное редактирование поля keywords - несколько тегов (граница)", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"191840"})
    public void successEditMoreKeywordsTest() {
        List<String> keywords = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            keywords.add(RandomStringUtils.randomAlphabetic(3));
        }
        Response response = metadataStepDef.successEditMetadata(id, KEYWORDS.getName(), keywords);
        Assert.compareParameters(keywords, getValueFromResponse(response, KEYWORDS.getPath()), "keywords");
    }

    @Test(description = "Неуспешное редактирование поля keywords - много тегов", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"233180"})
    public void unsuccessfulEditMoreKeywordsTest() {
        List<String> keywords = new ArrayList<>();
        for (int i = 0; i < 101; i++) {
            keywords.add(RandomStringUtils.randomAlphabetic(3));
        }
        List<InvalidParamsItem> invalidParams = metadataStepDef.unsuccessfulEditMetadata(id, KEYWORDS.getName(), keywords);
        getData().invalidParams().get(9).name(KEYWORDS.getName());
        assertAll(
                () -> compareParameters(1, invalidParams.size(), "invalid_params.size"),
                () -> compareParameters(getData().invalidParams().get(9), invalidParams.get(0), "invalid_params")
        );
    }

    @Test(description = "Успешное редактирование поля keywords на пустое значение", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"191841"})
    public void successEditEmptyKeywordTest() {
        metadataStepDef.successEditMetadata(id, KEYWORDS.getName(),
                Collections.singletonList(RandomStringUtils.randomAlphabetic(6)));

        List<String> keywords = Collections.singletonList(EMPTY_VALUE);
        Response response = metadataStepDef.successEditMetadata(id, KEYWORDS.getName(), keywords);
        Assert.compareParameters(keywords, getValueFromResponse(response, KEYWORDS.getPath()), "keywords");
    }

    @Test(description = "Успешное редактирование поля received", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"191844"})
    public void successEditReceivedTest() {
        String received = RandomStringUtils.randomAlphabetic(6);
        Response response = metadataStepDef.successEditMetadata(id, RECEIVED.getName(), received);
        Assert.compareParameters(received, getValueFromResponse(response, RECEIVED.getPath()), "received");
    }

    @Test(description = "Успешное редактирование поля received - длинное значение (граница)", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"191844"})
    public void successEditReceivedLongTest() {
        String received = RandomStringUtils.randomAlphabetic(150);
        Response response = metadataStepDef.successEditMetadata(id, RECEIVED.getName(), received);
        Assert.compareParameters(received, getValueFromResponse(response, RECEIVED.getPath()), "received");
    }

    @Test(description = "Неуспешное редактирование поля received - длинное значение", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"233181"})
    public void unsuccessfulEditReceivedLongTest() {
        List<InvalidParamsItem> invalidParams = metadataStepDef.unsuccessfulEditMetadata(id,
                RECEIVED.getName(), RandomStringUtils.randomAlphabetic(151));
        getData().invalidParams().get(7).name(RECEIVED.getName());
        assertAll(
                () -> compareParameters(1, invalidParams.size(), "invalid_params.size"),
                () -> compareParameters(getData().invalidParams().get(7), invalidParams.get(0), "invalid_params")
        );
    }

    @Test(description = "Успешное редактирование поля received на пустое значение", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"191845"})
    public void successEditEmptyReceivedTest() {
        metadataStepDef.successEditMetadata(id, RECEIVED.getName(), RandomStringUtils.randomAlphabetic(6));

        Response response = metadataStepDef.successEditMetadata(id, RECEIVED.getName(), EMPTY_VALUE);
        Assert.compareParameters(EMPTY_VALUE, getValueFromResponse(response, RECEIVED.getPath()), "received");
    }

    @Test(description = "Некорректное редактирование поля external_offer (не редактируемый параметр)", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"231281"})
    public void unsuccessfulEditExternalOfferTest() {
        Response response = metadataStepDef.successEditMetadata(id, EXTERNAL_OFFER.getName(), RandomStringUtils.randomAlphabetic(6));
        Assert.mustBeNullParameter(getValueFromResponse(response, EXTERNAL_OFFER.getPath() + ".id"), "external_offer.id");
    }

    @Test(description = "Успешное редактирование поля external_offer (не редактируемый параметр)", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"239591"})
    public void successEditExternalTaskIdTest() {
        String externalTaskId = String.valueOf(new Random().nextInt(10000) + 1);
        Response response = metadataStepDef.successEditMetadata(id, EXTERNAL_TASK_ID.getName(), externalTaskId);
        Assert.compareParameters(externalTaskId, getValueFromResponse(response, EXTERNAL_TASK_ID.getPath()), "external_task_id");
    }

    @Test(description = "Некорректное редактирование поля external_task_id - InvalidType", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"231282"})
    public void unsuccessfulEditExternalTaskIdTest() {
        List<InvalidParamsItem> invalidParams = metadataStepDef.unsuccessfulEditMetadata(id, EXTERNAL_TASK_ID.getName(), RandomStringUtils.randomAlphabetic(8));
        getData().invalidParams().get(2).name(EXTERNAL_TASK_ID.getName());
        assertAll(
                () -> compareParameters(1, invalidParams.size(), "invalid_params.size"),
                () -> compareParameters(getData().invalidParams().get(2), invalidParams.get(0), "invalid_params")
        );
    }

    @Test(description = "Некорректное редактирование поля external_draft_done (не редактируемый параметр)", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"231285"})
    public void unsuccessfulEditExternalDraftDoneTest() {
        Response response = metadataStepDef.successEditMetadata(id, EXTERNAL_DRAFT_DONE.getName(), RandomStringUtils.randomAlphabetic(6));
        Assert.mustBeNullParameter(getValueFromResponse(response, EXTERNAL_DRAFT_DONE.getPath()), "external_draft_done");
    }

    @Test(description = "Успешное редактирование поля sku", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"191848"})
    public void successEditSkuTest() {
        String sku = RandomStringUtils.randomAlphabetic(6);
        Response response = metadataStepDef.successEditMetadata(id, SKU.getName(), sku);
        Assert.compareParameters(sku, getValueFromResponse(response, SKU.getPath()), "sku");
    }

    @Test(description = "Успешное редактирование поля sku - длинное значение (граница)", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"191848"})
    public void successEditLongSkuTest() {
        String sku = RandomStringUtils.randomAlphabetic(100);
        Response response = metadataStepDef.successEditMetadata(id, SKU.getName(), sku);
        Assert.compareParameters(sku, getValueFromResponse(response, SKU.getPath()), "sku");
    }

    @Test(description = "Некорректное редактирование поля sku - длинное значение", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"191849"})
    public void unsuccessfulEditLongSkuTest() {
        List<InvalidParamsItem> invalidParams = metadataStepDef.unsuccessfulEditMetadata(id,
                SKU.getName(), RandomStringUtils.randomAlphabetic(101));
        getData().invalidParams().get(7).name(SKU.getName());
        assertAll(
                () -> compareParameters(1, invalidParams.size(), "invalid_params.size"),
                () -> compareParameters(getData().invalidParams().get(7), invalidParams.get(0), "invalid_params")
        );
    }

    @Test(description = "Успешное редактирование поля priority", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"191846"})
    public void successEditPriorityTest() {
        int priority = new Random().nextInt(FOUR_HUNDRED_NINETY_NINE) + 1;
        Response response = metadataStepDef.successEditMetadata(id, PRIORITY.getName(), priority);
        Assert.compareParameters(priority, getValueFromResponse(response, PRIORITY.getPath()), PRIORITY.getName());
    }

    @Test(description = "Успешное редактирование поля priority на пустое значение", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"191846"})
    public void successEditByEmptyValuePriorityTest() {
        int priority = new Random().nextInt(FOUR_HUNDRED_NINETY_NINE) + 1;
        metadataStepDef.successEditMetadata(id, ImageParameters.PRIORITY.getName(), priority);
        List<InvalidParamsItem> invalidParams = metadataStepDef.unsuccessfulEditMetadata(id, PRIORITY.getName(), EMPTY_VALUE);
        getData().invalidParams().get(2).name(PRIORITY.getName());
        assertAll(
                () -> compareParameters(1, invalidParams.size(), "invalid_params.size"),
                () -> compareParameters(getData().invalidParams().get(2), invalidParams.get(0), "invalid_params")
        );
    }

    @Test(description = "Успешное редактирование поля priority - null", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"251302"})
    public void successEditPriorityNullTest() {
        successEditPriorityTest();
        Response response = metadataStepDef.successEditMetadata(id, PRIORITY.getName(), null);
        Assert.mustBeNullParameter(getValueFromResponse(response, PRIORITY.getPath()), PRIORITY.getName());
    }

    @Test(description = "Некорректное редактирование поля priority < 0", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"191847"})
    public void unsuccessfulEditByZeroPriorityTest() {
        int priority = -1;
        List<InvalidParamsItem> invalidParams = metadataStepDef.unsuccessfulEditMetadata(id, PRIORITY.getName(), priority);
        getData().invalidParams().get(5).name(PRIORITY.getName());
        assertAll(
                () -> compareParameters(1, invalidParams.size(), "invalid_params.size"),
                () -> compareParameters(getData().invalidParams().get(5), invalidParams.get(0), "invalid_params")
        );
    }

    @Test(description = "Некорректное редактирование поля priority > 500", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"191847"})
    public void unsuccessfulEditByMoreThanOneThousandPriorityTest() {
        int priority = 501;
        List<InvalidParamsItem> invalidParams = metadataStepDef.unsuccessfulEditMetadata(id, PRIORITY.getName(), priority);
        getData().invalidParams().get(5).name(PRIORITY.getName());
        assertAll(
                () -> compareParameters(1, invalidParams.size(), "invalid_params.size"),
                () -> compareParameters(getData().invalidParams().get(5), invalidParams.get(0), "invalid_params")
        );
    }

    @Test(description = "Некорректное редактирование поля priority = строка", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"191847"})
    public void unsuccessfulEditByStringValuePriorityTest() {
        String priority = RandomStringUtils.randomAlphabetic(3);
        List<InvalidParamsItem> invalidParams = metadataStepDef.unsuccessfulEditMetadata(id, PRIORITY.getName(), priority);
        getData().invalidParams().get(6).name(PRIORITY.getName());
        assertAll(
                () -> compareParameters(1, invalidParams.size(), "invalid_params.size"),
                () -> compareParameters(getData().invalidParams().get(6), invalidParams.get(0), "invalid_params")
        );
    }

    @Test(description = "Успешное редактирование всех полей", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"191850"})
    public void successEditAllTest() {
        ImageData imageData = new ImageData(ImageFormat.JPEG);
        String statusId = metadataStepDef.getListStatusesMetadata().stream().filter(item -> item.name().equals(Status.NEW.getName())).
                map(DictionariesItem::id).findFirst().orElse(null);
        String categoryId = metadataStepDef.getListCategoriesMetadata().get(0).id();
        String qualityId = metadataStepDef.getListQualitiesMetadata().get(0).id();
        String sourceId = metadataStepDef.getListSourcesMetadata().get(0).id();
        String assigneeId = metadataStepDef.getListUsersMetadata().get(0).id();
        Integer priority = new Random().nextInt(NINETY_NINE) + 1;
        String externalTaskId = String.valueOf(new Random().nextInt(NINETY_NINE) + 1);
        String masterSellerId = metadataStepDef.getListRetailersMetadata().data().get(0).id();

        String editRequest = metadataStepDef.buildEditRequest(imageData, statusId, categoryId, qualityId,
                sourceId, assigneeId, priority, externalTaskId, masterSellerId);
        Response response = metadataStepDef.successEditAllMetadata(id, editRequest);

        assertAll(
                () -> compareParameters(imageData.getFilename(), getValueFromResponse(response, FILENAME.getPath()), "filename"),
                () -> compareParameters(imageData.getSku(), getValueFromResponse(response, SKU.getPath()), "sku"),
                () -> compareParameters(statusId, getValueFromResponse(response, STATUS_ID.getPath() + ".id"), "status_id"),
                () -> compareParameters(categoryId, getValueFromResponse(response, MASTER_CATEGORY_ID.getPath() + ".id"), "category_id"),
                () -> compareParameters(qualityId, getValueFromResponse(response, QUALITY_ID.getPath() + ".id"), "quality_id"),
                () -> compareParameters(sourceId, getValueFromResponse(response, SOURCE_ID.getPath() + ".id"), "source_id"),
                () -> compareParameters(assigneeId, getValueFromResponse(response, ASSIGNEE_ID.getPath() + ".id"), "assignee_id"),
                () -> compareParameters(priority, getValueFromResponse(response, PRIORITY.getPath()), "priority"),
                () -> compareParameters(externalTaskId, getValueFromResponse(response, EXTERNAL_TASK_ID.getPath()), "external_task_id"),
                () -> compareParameters(masterSellerId, getValueFromResponse(response, MASTER_SELLER.getPath()+ ".id"), "master_seller_id")
        );
    }

    @Test(description = "Некорректное редактирование нескольких полей", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"191851"})
    public void unsuccessfulEditAllTest() {
        ImageData imageData = new ImageData(ImageFormat.JPEG);

        String statusId = UUID.randomUUID().toString();
        String categoryId = UUID.randomUUID().toString();
        Integer priority = new Random().nextInt(NINETY_NINE) + 1;

        String qualityId = metadataStepDef.getListQualitiesMetadata().get(0).id();
        String sourceId = metadataStepDef.getListSourcesMetadata().get(0).id();
        String assigneeId = metadataStepDef.getListUsersMetadata().get(0).id();
        String externalTaskId = String.valueOf(new Random().nextInt(NINETY_NINE) + 1);
        String masterSellerId = metadataStepDef.getListRetailersMetadata().data().get(0).id();

        String editRequest = metadataStepDef.buildEditRequest(imageData, statusId, categoryId, qualityId,
                sourceId, assigneeId, priority, externalTaskId, masterSellerId);
        List<InvalidParamsItem> invalidParams = metadataStepDef.unsuccessfulEditMetadata(id, editRequest);

        List<InvalidParamsItem> expectedInvalidParams = new ArrayList<InvalidParamsItem>() {{
            getData().invalidParams().get(1).name(STATUS_ID.getName());
            add(new InvalidParamsItem(getData().invalidParams().get(1)));

            getData().invalidParams().get(1).name(MASTER_CATEGORY_ID.getName());
            add(new InvalidParamsItem(getData().invalidParams().get(1)));
        }};
        Assert.compareParameters(new LinkedList<>(expectedInvalidParams), new LinkedList<>(invalidParams), "invalid_params");
    }

    @Test(description = "Успешное редактирование - оригинальный файл, есть производная - в ответе есть derived_metadata_id", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"295039"})
    public void successEditIsOwnTrademarkOriginalFileTest() {
        List<String> metadataId = importServiceStepDef.importImages(new ImageData(ImageFormat.JPEG), Collections.singletonList(ProcessingTypeEnum.SQUARE));

        log.info("Ожидание создания производного");
        AtomicReference<String> derivedMetadataIdAtomic = new AtomicReference<>();
        Awaitility.await().atMost(20, TimeUnit.SECONDS).pollInterval(1, TimeUnit.SECONDS).until(() ->
        {
            Response response = metadataStepDef.checkMetadata(metadataId.get(0));
            String id = getValueFromResponse(response, DERIVED_METADATA_ID.getPath());
            derivedMetadataIdAtomic.set(id);
            return id != null;
        });

        Response response = metadataStepDef.successEditMetadata(metadataId.get(0), IS_OWN_TRADEMARK.getName(), true);
        Assert.compareParameters(derivedMetadataIdAtomic.toString(), getValueFromResponse(response, DERIVED_METADATA_ID.getPath()), "derived_metadata_id");
    }

    @Test(description = "Успешное редактирование - производное изображение - в ответе есть original_metadata_id", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"295040"})
    public void successEditIsOwnTrademarkDerivedFileTest() {
        List<String> metadataId = importServiceStepDef.importImages(new ImageData(ImageFormat.JPEG), Collections.singletonList(ProcessingTypeEnum.SQUARE));

        log.info("Ожидание создания производного");
        AtomicReference<String> derivedMetadataIdAtomic = new AtomicReference<>();
        Awaitility.await().atMost(20, TimeUnit.SECONDS).pollInterval(1, TimeUnit.SECONDS).until(() ->
        {
            Response response = metadataStepDef.checkMetadata(metadataId.get(0));
            String id = getValueFromResponse(response, DERIVED_METADATA_ID.getPath());
            derivedMetadataIdAtomic.set(id);
            return id != null;
        });

        Response response = metadataStepDef.successEditMetadata(derivedMetadataIdAtomic.toString(), IS_OWN_TRADEMARK.getName(), true);
        Assert.compareParameters(metadataId.get(0), getValueFromResponse(response, ORIGINAL_METADATA_ID.getPath()), "original_metadata_id");
    }
}
