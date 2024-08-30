package ru.spice.at.ui.edit_media.test;

import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import ru.testit.annotations.WorkItemIds;
import org.apache.commons.lang3.RandomStringUtils;
import org.testng.annotations.*;
import ru.spice.at.api.metadata_service.MetadataStepDef;
import ru.spice.at.common.base_test.BaseUiTest;
import ru.spice.at.common.dto.dam.ImageData;
import ru.spice.at.common.emuns.UiCategories;
import ru.spice.at.common.emuns.dam.ImageFormat;
import ru.spice.at.common.emuns.dam.ImageParameters;
import ru.spice.at.common.emuns.dam.Quality;
import ru.spice.at.common.listeners.TestAllureListener;
import ru.spice.at.ui.edit_media.EditMediaSettings;
import ru.spice.at.ui.edit_media.EditMediaStepDef;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import static ru.spice.at.common.constants.TestConstants.NO;
import static ru.spice.at.common.constants.TestConstants.YES;
import static ru.spice.at.common.emuns.dam.ImageParameters.*;

@Feature("Edit media")
@Story("Success main parameters edit")
@Listeners({TestAllureListener.class})
public class EditMainParametersMediaSuccessTests extends BaseUiTest<EditMediaSettings> {
    private EditMediaStepDef editMediaStepDef;
    private MetadataStepDef metadataStepDef;

    protected EditMainParametersMediaSuccessTests() {
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
        metadataStepDef.createMetadataImage(imageData);
        editMediaStepDef.searchImage(imageData.getFilename());
    }

    @AfterMethod(alwaysRun = true)
    public void afterMethod() {
        new MetadataStepDef(editMediaStepDef.getAuthToken()).deleteMetadata();
    }

    @Test(description = "Успешное редактирование поля 'Название'", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"227071"})
    public void successEditFilenameTest() {
        editMediaStepDef.editImageParameters(FILENAME, RandomStringUtils.randomAlphanumeric(5) + "." + ImageFormat.JPEG.getFormatName());
    }

    @Test(description = "Успешное редактирование поля 'Приоритет'", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"231484"})
    public void successEditPriorityTest() {
        editMediaStepDef.editImageParameters(PRIORITY, String.valueOf(new Random().nextInt(100)));
    }

    @Test(description = "Успешное редактирование поля 'Качество'", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"231485"})
    public void successEditQualityTest() {
        editMediaStepDef.editImageParameters(QUALITY_ID, Quality.TO_REVISION.getName());
    }

    @Test(description = "Успешное редактирование поля 'Исходное изображение'", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"231486"})
    public void successEditIsRawImageTest() {
        editMediaStepDef.editImageParameters(IS_RAW_IMAGE, NO);
    }

    @Test(description = "Успешное редактирование поля 'СТМ'", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"231487"})
    public void successEditIsOwnImageTest() {
        editMediaStepDef.editImageParameters(IS_OWN_TRADEMARK, YES);
    }

    //todo удалить
    //@Test(description = "Успешное редактирование поля 'Главное изображение'", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"231488"})
    public void successEditIsMainImageTest() {
        editMediaStepDef.editImageParameters(IS_MAIN_IMAGE, YES);
    }

    @Test(description = "Успешное редактирование поля 'Описание'", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"231489"})
    public void successEditDescriptionTest() {
        editMediaStepDef.editImageParameters(DESCRIPTION, RandomStringUtils.randomAlphabetic(123));
    }

    @Test(description = "Успешное редактирование поля 'Теги'", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"231490"})
    public void successEditTagsTest() {
        editMediaStepDef.editImageParameters(KEYWORDS, RandomStringUtils.randomAlphabetic(5));
    }

    @Test(description = "Успешное редактирование нескольких полей - Основные", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"231492"})
    public void successEditSomeMainParametersTest() {
        Map<ImageParameters, String> metadata = new HashMap<ImageParameters, String>() {{
            put(PRIORITY, String.valueOf(new Random().nextInt(100)));
            put(QUALITY_ID, Quality.BAD.getName());
            put(DESCRIPTION, RandomStringUtils.randomAlphabetic(70));
        }};
        editMediaStepDef.editImageParameters(metadata);
    }
}