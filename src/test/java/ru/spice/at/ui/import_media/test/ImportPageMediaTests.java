package ru.spice.at.ui.import_media.test;

import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import ru.testit.annotations.WorkItemIds;
import org.testng.annotations.*;
import ru.spice.at.api.import_service.ImportServiceStepDef;
import ru.spice.at.api.metadata_service.MetadataStepDef;
import ru.spice.at.common.base_test.BaseUiTest;
import ru.spice.at.common.dto.dam.ImageData;
import ru.spice.at.common.emuns.UiCategories;
import ru.spice.at.common.emuns.dam.ImageFormat;
import ru.spice.at.common.listeners.TestAllureListener;
import ru.spice.at.ui.import_media.ImportMediaSettings;
import ru.spice.at.ui.import_media.ImportMediaStepDef;

import java.util.*;

import static ru.spice.at.common.constants.TestConstants.COMMA_SPICE_VALUE;
import static ru.spice.at.common.constants.TestConstants.EMPTY_VALUE;

@Feature("Import media")
@Story("Import page")
@Listeners({TestAllureListener.class})
public class ImportPageMediaTests extends BaseUiTest<ImportMediaSettings> {
    private ImportMediaStepDef importMediaStepDef;
    private MetadataStepDef metadataStepDef;
    private ImportServiceStepDef importServiceStepDef;

    protected ImportPageMediaTests() {
        super(UiCategories.IMPORT_MEDIA);
    }

    @BeforeClass(alwaysRun = true)
    public void beforeClass() {
        importMediaStepDef = new ImportMediaStepDef(getWebDriver());
        metadataStepDef = new MetadataStepDef(importMediaStepDef.getAuthToken());
        importServiceStepDef = new ImportServiceStepDef(importMediaStepDef.getAuthToken());

        importServiceStepDef.deleteImports();

        importServiceStepDef.importRandomImages(new ImageData(ImageFormat.JPEG));

        List<ImageData> imageDataList = Arrays.asList(new ImageData(ImageFormat.JPEG), new ImageData(ImageFormat.INVALID));
        String importId = importServiceStepDef.successImportOpen(imageDataList.size());
        importServiceStepDef.importRandomImages(imageDataList.get(0), importId);
        importServiceStepDef.importInvalidRandomImages(imageDataList.get(1), importId);

        importServiceStepDef.importRandomImages(new ImageData(ImageFormat.JPEG));

        importServiceStepDef.importInvalidRandomImages(new ImageData(ImageFormat.INVALID));
    }

    @BeforeMethod(alwaysRun = true)
    public void beforeMethod() {
        importMediaStepDef.goToBaseUrlWithLogin(getWebDriver());
        createFileDirection();
    }

    @AfterClass(alwaysRun = true)
    public void afterClass() {
        metadataStepDef.deleteMetadata();
        importServiceStepDef.deleteImports();
    }

    @Test(description = "Вкладка 'Все загрузки' на странице загрузок - импорт без ошибок", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"263564"})
    public void successAllWithoutErrorImportsTest() {
        importMediaStepDef.checkImports(
                ImportMediaStepDef.ImportValidationType.ALL,
                Arrays.asList("1", "1", "2", "1"),
                Arrays.asList("1", EMPTY_VALUE, "1", EMPTY_VALUE));

        Map<ImportMediaStepDef.ImportValidationType, List<String>> fileStatusMap =
                new HashMap<>() {{
            put(ImportMediaStepDef.ImportValidationType.ALL, Collections.singletonList(getData().successImport()));
            put(ImportMediaStepDef.ImportValidationType.WITHOUT_ERRORS, Collections.singletonList(getData().successImport()));
        }};

        importMediaStepDef.checkModalImports(fileStatusMap, 1);
    }

    @Test(description = "Вкладка 'Все загрузки' на странице загрузок - импорт с ошибками", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"263564"})
    public void successAllWithErrorImportsTest() {
        importMediaStepDef.checkImports(
                ImportMediaStepDef.ImportValidationType.ALL,
                Arrays.asList("1", "1", "2", "1"),
                Arrays.asList("1", EMPTY_VALUE, "1", EMPTY_VALUE));

        Map<ImportMediaStepDef.ImportValidationType, List<String>> fileStatusMap =
                new HashMap<>() {{
                    put(ImportMediaStepDef.ImportValidationType.ALL,
                            Arrays.asList(getData().fileError() + COMMA_SPICE_VALUE + getData().formatError(), getData().successImport()));
                    put(ImportMediaStepDef.ImportValidationType.WITHOUT_ERRORS, Collections.singletonList(getData().successImport()));
                    put(ImportMediaStepDef.ImportValidationType.WITH_ERRORS, Collections.singletonList(getData().fileError() + COMMA_SPICE_VALUE + getData().formatError()));
                }};

        importMediaStepDef.checkModalImports(fileStatusMap, 2);
    }

    @Test(description = "Вкладка 'Без ошибок' на странице загрузок", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"263565"})
    public void successWithoutErrorImportsTest() {
        importMediaStepDef.checkImports(
                ImportMediaStepDef.ImportValidationType.WITHOUT_ERRORS,
                Arrays.asList("1", "1"),
                Arrays.asList(EMPTY_VALUE, EMPTY_VALUE));

        Map<ImportMediaStepDef.ImportValidationType, List<String>> fileStatusMap =
                new HashMap<>() {{
                    put(ImportMediaStepDef.ImportValidationType.ALL, Collections.singletonList(getData().successImport()));
                    put(ImportMediaStepDef.ImportValidationType.WITHOUT_ERRORS, Collections.singletonList(getData().successImport()));
                }};

        importMediaStepDef.checkModalImports(fileStatusMap, 0);
    }

    @Test(description = "Вкладка 'С ошибками' на странице загрузок - импорт с ошибками", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"263566"})
    public void successWithErrorImportsTest() {
        importMediaStepDef.checkImports(
                ImportMediaStepDef.ImportValidationType.WITH_ERRORS,
                Arrays.asList("1", "2"),
                Arrays.asList("1", "1"));

        Map<ImportMediaStepDef.ImportValidationType, List<String>> fileStatusMap =
                new HashMap<>() {{
                    put(ImportMediaStepDef.ImportValidationType.ALL, Collections.singletonList(getData().fileError() + COMMA_SPICE_VALUE + getData().formatError()));
                    put(ImportMediaStepDef.ImportValidationType.WITH_ERRORS, Collections.singletonList(getData().fileError() + COMMA_SPICE_VALUE + getData().formatError()));
                }};

        importMediaStepDef.checkModalImports(fileStatusMap, 0);
    }

    @Test(description = "Вкладка 'С ошибками' на странице загрузок - импорт с ошибками и без", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"263566"})
    public void successWithAndWithoutErrorImportsTest() {
        importMediaStepDef.checkImports(
                ImportMediaStepDef.ImportValidationType.WITH_ERRORS,
                Arrays.asList("1", "2"),
                Arrays.asList("1", "1"));

        Map<ImportMediaStepDef.ImportValidationType, List<String>> fileStatusMap =
                new HashMap<>() {{
                    put(ImportMediaStepDef.ImportValidationType.ALL,
                            Arrays.asList(getData().fileError() + COMMA_SPICE_VALUE + getData().formatError(), getData().successImport()));
                    put(ImportMediaStepDef.ImportValidationType.WITHOUT_ERRORS, Collections.singletonList(getData().successImport()));
                    put(ImportMediaStepDef.ImportValidationType.WITH_ERRORS, Collections.singletonList(getData().fileError() + COMMA_SPICE_VALUE + getData().formatError()));
                }};

        importMediaStepDef.checkModalImports(fileStatusMap, 1);
    }
}
