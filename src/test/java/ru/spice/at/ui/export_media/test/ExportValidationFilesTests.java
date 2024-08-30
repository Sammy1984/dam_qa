package ru.spice.at.ui.export_media.test;

import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import ru.testit.annotations.WorkItemIds;
import org.testng.annotations.*;
import ru.spice.at.api.import_service.ImportServiceStepDef;
import ru.spice.at.common.base_test.BaseUiTest;
import ru.spice.at.common.dto.dam.ImageData;
import ru.spice.at.common.emuns.dam.ImageFormat;
import ru.spice.at.common.emuns.UiCategories;
import ru.spice.at.common.listeners.TestAllureListener;
import ru.spice.at.ui.export_media.ExportMediaSettings;
import ru.spice.at.ui.export_media.ExportMediaStepDef;

@Deprecated
@Feature("Export media")
@Story("Export validation")
@Listeners({TestAllureListener.class})
public class ExportValidationFilesTests extends BaseUiTest<ExportMediaSettings> {
    private ExportMediaStepDef exportMediaStepDef;
    private ImportServiceStepDef mediaAdapterStepDef;

    protected ExportValidationFilesTests() {
        super(UiCategories.EXPORT_MEDIA);
    }

    @BeforeClass(description = "Добавляем файлы с ошибкой при загрузке", alwaysRun = true)
    public void beforeClass() {
        exportMediaStepDef = new ExportMediaStepDef(getWebDriver());
        mediaAdapterStepDef = new ImportServiceStepDef(exportMediaStepDef.getAuthToken());
        mediaAdapterStepDef.deleteValidations();

        for (int i = 0; i < 3; i++) {
            ImageData imageData = new ImageData(ImageFormat.INVALID);
            mediaAdapterStepDef.importInvalidRandomImages(imageData);
        }
    }

    @AfterClass(alwaysRun = true)
    public void afterClass() {
        mediaAdapterStepDef.deleteValidations();
    }

    @BeforeMethod(alwaysRun = true)
    public void beforeMethod() {
        exportMediaStepDef.goToBaseUrlWithLogin(getWebDriver());
    }

    //todo требуется настройка moon для скачивания файлов
    @Test(description = "Экспорт валидации для одного файла", timeOut = 60000, groups = {"regress"})
    @WorkItemIds({"191755"})
    public void successExportValidationTest() {
        exportMediaStepDef.exportValidationFile();
    }

    //todo требуется настройка moon для скачивания файлов
    @Test(description = "Экспорт валидации из таблицы для одного файла", timeOut = 60000, groups = {"regress"})
    @WorkItemIds({"191756"})
    public void successExportValidationInTableTest() {
        exportMediaStepDef.exportValidationInTableFile();
    }

    //todo требуется настройка moon для скачивания файлов
    @Test(description = "Экспорт валидации для нескольких файлов", timeOut = 60000, groups = {"regress"})
    @WorkItemIds({"191757"})
    public void successExportValidationsTest() {
        exportMediaStepDef.exportValidationFiles();
    }

    @Test(description = "Отмена экспорта валидации", timeOut = 60000, groups = {"regress"})
    @WorkItemIds({"191758"})
    public void cancelExportValidationTest() {
        exportMediaStepDef.cancelExportValidation();
    }
}
