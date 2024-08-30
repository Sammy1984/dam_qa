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
import ru.spice.at.common.listeners.TestAllureListener;
import ru.spice.at.ui.edit_media.EditMediaSettings;
import ru.spice.at.ui.edit_media.EditMediaStepDef;

import java.util.Random;

import static ru.spice.at.common.constants.TestConstants.DOT_VALUE;
import static ru.spice.at.common.constants.TestConstants.MAX_JPG_FILENAME_SYMBOLS;
import static ru.spice.at.common.emuns.dam.ImageParameters.*;

@Feature("Edit media")
@Story("Unsuccessful parameters edit")
@Listeners({TestAllureListener.class})
public class EditParametersMediaUnsuccessfulTests extends BaseUiTest<EditMediaSettings> {
    private EditMediaStepDef editMediaStepDef;
    private MetadataStepDef metadataStepDef;

    protected EditParametersMediaUnsuccessfulTests() {
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

    @Test(description = "Неуспешное редактирование поля 'Название' - пустое название", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"231681"})
    public void unsuccessfulEditEmptyFilenameTest() {
        editMediaStepDef.unsuccessfulEditImageParameters(new EditMediaStepDef.EditImage(FILENAME, null, getData().formatFilenameMessage()));
    }

    @Test(description = "Неуспешное редактирование поля 'Название' - больше 250 символов", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"231682"})
    public void unsuccessfulEditLongFilenameTest() {
        editMediaStepDef.unsuccessfulEditImageParameters(
                new EditMediaStepDef.EditImage(
                        FILENAME,
                        RandomStringUtils.randomAlphabetic(MAX_JPG_FILENAME_SYMBOLS + 2) + DOT_VALUE + ImageFormat.JPG.getFormatName(),
                        getData().longFilenameMessage()
                )
        );
    }

    @Test(description = "Неуспешное редактирование поля 'Приоритет' - значение больше 500", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"231683"})
    public void unsuccessfulEditLongPriorityTest() {
        editMediaStepDef.unsuccessfulEditImageParameters(
                new EditMediaStepDef.EditImage(PRIORITY, String.valueOf(501 + new Random().nextInt(1000)), getData().longPriorityMessage()));
    }

    @Test(description = "Неуспешное редактирование поля 'Описание' - больше 500 символов", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"231684"})
    public void unsuccessfulEditLongDescriptionTest() {
        editMediaStepDef.unsuccessfulEditImageParameters(
                new EditMediaStepDef.EditImage(DESCRIPTION, RandomStringUtils.randomAlphabetic(501), getData().longDescriptionMessage()));
    }

    @Test(description = "Неуспешное редактирование поля 'Теги' - количество больше 100", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"231685"})
    public void unsuccessfulEditManyTagsTest() {
        editMediaStepDef.unsuccessfulEditImageParameters(
                new EditMediaStepDef.EditImage(KEYWORDS, String.valueOf(101 + new Random().nextInt(10)), getData().longTagsMessage()));
    }

    @Test(description = "Неуспешное редактирование поля 'SKU' - больше 100 символов", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"231686"})
    public void unsuccessfulEditLongSkuTest() {
        editMediaStepDef.unsuccessfulEditImageParameters(
                new EditMediaStepDef.EditImage(SKU, RandomStringUtils.randomAlphabetic(101), getData().longSkuMessage()));
    }

    @Test(description = "Неуспешное редактирование поля 'Получено от' - больше 150 символов", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"231687"})
    public void unsuccessfulEditLongReceivedTest() {
        editMediaStepDef.unsuccessfulEditImageParameters(
                new EditMediaStepDef.EditImage(RECEIVED, RandomStringUtils.randomAlphabetic(151), getData().longReceivedMessage()));
    }
}