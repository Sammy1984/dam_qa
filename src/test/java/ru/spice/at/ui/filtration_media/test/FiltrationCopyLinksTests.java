package ru.spice.at.ui.filtration_media.test;

import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import ru.testit.annotations.WorkItemIds;
import org.testng.annotations.*;
import ru.spice.at.api.metadata_service.MetadataStepDef;
import ru.spice.at.common.base_test.BaseUiTest;
import ru.spice.at.common.dto.dam.ImageData;
import ru.spice.at.common.emuns.UiCategories;
import ru.spice.at.common.emuns.dam.ImageFormat;
import ru.spice.at.common.listeners.TestAllureListener;
import ru.spice.at.ui.filtration_media.FiltrationMediaSettings;
import ru.spice.at.ui.filtration_media.FiltrationMediaStepDef;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Feature("Filtration media")
@Story("Filtration copy links")
@Listeners({TestAllureListener.class})
public class FiltrationCopyLinksTests extends BaseUiTest<FiltrationMediaSettings> {
    private final static int COUNT_METADATA = 5;
    private FiltrationMediaStepDef filtrationMediaStepDef;
    private MetadataStepDef metadataStepDef;
    private final List<FiltrationMediaStepDef.Image> images = new ArrayList<>();

    protected FiltrationCopyLinksTests() {
        super(UiCategories.FILTRATION_MEDIA);
    }

    @BeforeClass(alwaysRun = true)
    public void beforeClass() {
        filtrationMediaStepDef = new FiltrationMediaStepDef(getWebDriver());
        metadataStepDef = new MetadataStepDef(filtrationMediaStepDef.getAuthToken());
        metadataStepDef.deleteMetadata();

        for (int i = 0; i < COUNT_METADATA; i++) {
            ImageData imageData = new ImageData(ImageFormat.JPEG);
            String id = metadataStepDef.createMetadataImage(imageData);
            images.add(new FiltrationMediaStepDef.Image(imageData.getFilename(), id));
        }
    }

    @AfterClass(alwaysRun = true)
    public void afterClass()  {
        metadataStepDef.deleteMetadata();
    }

    @BeforeMethod(alwaysRun = true)
    public void beforeMethod() {
        filtrationMediaStepDef.goToBaseUrlWithLogin(getWebDriver());
    }

    @Test(description = "Копирование ссылки для одного файла", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"246252"})
    public void successCopyImageLinkTest() {
        String link = filtrationMediaStepDef.copyLink(Collections.singletonList(images.get(0)), true);
        getWebDriver().get(link);
        filtrationMediaStepDef.cancelCopyLink(Collections.singletonList(images.get(0)), true);
    }

    @Test(description = "Копирование ссылки для одного файла (боковое меню)", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"246256"})
    public void successCopyImageLinkSideMenuTest() {
        String link = filtrationMediaStepDef.copyLink(Collections.singletonList(images.get(0)), false);
        getWebDriver().get(link);
        filtrationMediaStepDef.cancelCopyLink(Collections.singletonList(images.get(0)), false);
    }

    @Test(description = "Копирование ссылки для нескольких файлов", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"246254"})
    public void successCopyImagesLinkTest() {
        String link = filtrationMediaStepDef.copyLink(Arrays.asList(images.get(0), images.get(3), images.get(1)), true);
        getWebDriver().get(link);
        filtrationMediaStepDef.cancelCopyLink(Arrays.asList(images.get(0), images.get(3), images.get(1)), true);
    }

    @Test(description = "Копирование ссылки для нескольких файлов (боковое меню)", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"246257"})
    public void successCopyImagesLinkSideMenuTest() {
        String link = filtrationMediaStepDef.copyLink(Arrays.asList(images.get(2), images.get(3), images.get(1)), false);
        getWebDriver().get(link);
        filtrationMediaStepDef.cancelCopyLink(Arrays.asList(images.get(2), images.get(3), images.get(1)), false);
    }
}