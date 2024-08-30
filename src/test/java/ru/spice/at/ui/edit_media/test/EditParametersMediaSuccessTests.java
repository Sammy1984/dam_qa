package ru.spice.at.ui.edit_media.test;

import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import ru.testit.annotations.WorkItemIds;
import org.apache.commons.lang3.RandomStringUtils;
import org.testng.annotations.*;
import ru.spice.at.api.dto.response.metadata.DictionariesItem;
import ru.spice.at.api.metadata_service.MetadataStepDef;
import ru.spice.at.common.base_test.BaseUiTest;
import ru.spice.at.common.dto.dam.ImageData;
import ru.spice.at.common.emuns.UiCategories;
import ru.spice.at.common.emuns.dam.*;
import ru.spice.at.common.listeners.TestAllureListener;
import ru.spice.at.ui.edit_media.EditMediaSettings;
import ru.spice.at.ui.edit_media.EditMediaStepDef;

import java.util.*;

import static ru.spice.at.common.constants.TestConstants.EMPTY_VALUE;
import static ru.spice.at.common.constants.TestConstants.YES;
import static ru.spice.at.common.emuns.dam.ImageParameters.*;

@Feature("Edit media")
@Story("Success parameters edit")
@Listeners({TestAllureListener.class})
public class EditParametersMediaSuccessTests extends BaseUiTest<EditMediaSettings> {
    private EditMediaStepDef editMediaStepDef;
    private MetadataStepDef metadataStepDef;

    protected EditParametersMediaSuccessTests() {
        super(UiCategories.EDIT_MEDIA);
    }

    @BeforeClass(alwaysRun = true)
    public void beforeClass() {
        editMediaStepDef = new EditMediaStepDef(getWebDriver());
        metadataStepDef = new MetadataStepDef(editMediaStepDef.getAuthToken());
    }

    @BeforeMethod(alwaysRun = true)
    public void beforeMethod() {
        editMediaStepDef.goToBaseUrlWithLogin(getWebDriver());
        ImageData imageData = new ImageData(ImageFormat.JPEG);
        imageData.setSku(EMPTY_VALUE);
        metadataStepDef.createMetadataImage(imageData);
        editMediaStepDef.searchImage(imageData.getFilename());
    }

    @AfterMethod(alwaysRun = true)
    public void afterMethod() {
        new MetadataStepDef(editMediaStepDef.getAuthToken()).deleteMetadata();
    }

    @Test(description = "Успешное редактирование поля 'SKU'", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"231493"})
    public void successEditSkuTest() {
        editMediaStepDef.editImageParameters(SKU, RandomStringUtils.randomAlphanumeric(5));
    }

    @Test(description = "Успешное редактирование поля 'Источник'", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"231494"})
    public void successEditSourceTest() {
        editMediaStepDef.editImageParameters(SOURCE_ID, Source.BRAND.getName());
    }

    @Test(description = "Успешное редактирование поля 'Исполнитель'", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"231495"})
    public void successEditAssigneeTest() {
        editMediaStepDef.editImageParameters(ASSIGNEE_ID, getData().assigneeName());
    }

    @Test(description = "Успешное редактирование поля 'Категория'", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"231496"})
    public void successEditCategoryTest() {
        DictionariesItem category = metadataStepDef.getListCategoriesMetadata().get(0);
        editMediaStepDef.editImageParameters(MASTER_CATEGORY_ID, category.name());
    }

    @Test(description = "Успешное редактирование поля 'Статус'", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"231497"})
    public void successEditStatusTest() {
        editMediaStepDef.editImageParameters(STATUS_ID, Status.ARCHIVE.getName());
    }

    @Test(description = "Успешное редактирование поля 'Статус' - Актуальный", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"231498"})
    public void successEditActualStatusTest() {
        List<String> messages = new ArrayList<>() {{
            add(getData().warningPriorityMessage());
            add(getData().warningSkuMessage());
            add(getData().actualStatusMessage());
        }};
        editMediaStepDef.unsuccessfulEditImageParameters(new EditMediaStepDef.EditImage(STATUS_ID, Status.ACTUAL.getName(), messages));

        editMediaStepDef.editImageParameters(PRIORITY, String.valueOf(new Random().nextInt(100)));
        editMediaStepDef.editImageParameters(SKU, RandomStringUtils.randomAlphanumeric(5));
        editMediaStepDef.editImageParameters(STATUS_ID, Status.ACTUAL.getName());
    }

    @Test(description = "Успешное редактирование поля 'Получено от'", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"231499"})
    public void successEditReceivedTest() {
        editMediaStepDef.editImageParameters(RECEIVED, RandomStringUtils.randomAlphanumeric(5));
    }

    @Test(description = "Успешное редактирование поля 'Авторские права'", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"231500"})
    public void successEditCopyrightTest() {
        editMediaStepDef.editImageParameters(IS_COPYRIGHT, YES);
    }

    @Test(description = "Успешное редактирование нескольких полей - Параметры", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"231501"})
    public void successEditSomeParametersTest() {
        DictionariesItem category = metadataStepDef.getListCategoriesMetadata().get(1);
        Map<ImageParameters, String> metadata = new HashMap<>() {{
            put(SOURCE_ID, Source.BRAND.getName());
            put(MASTER_CATEGORY_ID, category.name());
            put(STATUS_ID, Status.IN_PROGRESS.getName());
            put(IS_COPYRIGHT, YES);
        }};
        editMediaStepDef.editImageParameters(metadata);
    }
}