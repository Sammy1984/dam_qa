package ru.spice.at.ui.export_media.test;

import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import ru.testit.annotations.WorkItemIds;
import org.apache.commons.lang3.RandomStringUtils;
import org.testng.annotations.*;
import ru.spice.at.api.import_service.ImportServiceStepDef;
import ru.spice.at.api.metadata_service.MetadataStepDef;
import ru.spice.at.common.base_test.BaseUiTest;
import ru.spice.at.common.dto.dam.ImageData;
import ru.spice.at.common.emuns.UiCategories;
import ru.spice.at.common.emuns.dam.ImageFormat;
import ru.spice.at.common.listeners.TestAllureListener;
import ru.spice.at.ui.export_media.ExportMediaSettings;
import ru.spice.at.ui.export_media.ExportMediaStepDef;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import static ru.spice.at.common.utils.ImageHelper.getRandomByteImage;

@Feature("Export media")
@Story("Export media files")
@Listeners({TestAllureListener.class})
public class ExportMediaFilesTests extends BaseUiTest<ExportMediaSettings> {
    private ExportMediaStepDef exportMediaStepDef;
    private MetadataStepDef metadataStepDef;
    List<ExportMediaStepDef.Image> exportsImages;

    protected ExportMediaFilesTests() {
        super(UiCategories.EXPORT_MEDIA);
    }

    @BeforeClass(alwaysRun = true)
    public void beforeClass() {
        exportMediaStepDef = new ExportMediaStepDef(getWebDriver());
        metadataStepDef = new MetadataStepDef(exportMediaStepDef.getAuthToken());
        metadataStepDef.deleteMetadata();

        ImportServiceStepDef importServiceStepDef = new ImportServiceStepDef(exportMediaStepDef.getAuthToken());
        Map<String, byte[]> imageBytes = getData().images().stream().collect(Collectors.toMap(ImageData::getFilename,
                image -> getRandomByteImage(image.getWidth(), image.getHeight(), image.getFormat().getFormatName())));
        importServiceStepDef.importImages(imageBytes);
        List<ExportMediaStepDef.Image> images = new ArrayList<>();
        imageBytes.forEach((k, v) -> images.add(new ExportMediaStepDef.Image(k, v)));

        exportsImages = Arrays.asList(images.get(0), images.get(1));
    }

    @AfterClass(alwaysRun = true)
    public void afterClass()  {
        metadataStepDef.deleteMetadata();
    }

    @BeforeMethod(alwaysRun = true)
    public void beforeMethod() {
        exportMediaStepDef.goToBaseUrlWithLogin(getWebDriver());
        createFileDirection();
    }

    @AfterMethod(alwaysRun = true)
    public void afterMethod() {
        deleteFileDirection();
    }

    @Test(description = "Экспорт одного медиафайла", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"225879"})
    public void successExportFileTest() {
        exportMediaStepDef.exportImage(Collections.singletonList(exportsImages.get(0)), ImageFormat.CURRENT, false);
    }

    @Test(description = "Экспорт одного медиафайла - правая кнопка мыши", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"246154"})
    public void successExportFileRightTest() {
        exportMediaStepDef.exportImage(Collections.singletonList(exportsImages.get(0)), ImageFormat.PNG,  true);
    }

    @Test(description = "Экспорт нескольких медиафайлов", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"225880"})
    public void successExportFilesTest() {
        exportMediaStepDef.exportImage(exportsImages, ImageFormat.CURRENT, false);
        exportMediaStepDef.checkExports(exportsImages.size());
    }

    @Test(description = "Экспорт нескольких медиафайлов - правая кнопка мыши", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"246155"})
    public void successContextExportFileTest() {
        exportMediaStepDef.exportImage(exportsImages, ImageFormat.PNG, true);
        exportMediaStepDef.checkExports(exportsImages.size());
    }

    @Test(description = "Экспорт нескольких медиафайлов - поиск загрузки", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"259747"})
    public void successSearchExportsFileTest() {
        String archiveName = RandomStringUtils.randomAlphabetic(10);
        exportMediaStepDef.exportImage(exportsImages, ImageFormat.JPEG, archiveName, false);
        exportMediaStepDef.checkExports(
                getData().archive() + archiveName, LocalDate.now(), exportsImages.size());
    }

    @Test(description = "Экспорт нескольких медиафайлов - отмена поиска загрузки", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"259750"})
    public void successCancelSearchExportsFileTest() {
        String archiveName = RandomStringUtils.randomAlphabetic(10);
        exportMediaStepDef.exportImage(exportsImages, ImageFormat.CURRENT, archiveName, false);
        exportMediaStepDef.checkExports(getData().archive() + archiveName, LocalDate.now(), exportsImages.size());
        exportMediaStepDef.cancelSearchExports(getData().archive() + archiveName);
    }

    @Test(description = "Отмена экспорта медиафайла", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"225878"})
    public void successCancelExportsTest() {
        exportMediaStepDef.cancelExportImage(exportsImages);
    }
}
