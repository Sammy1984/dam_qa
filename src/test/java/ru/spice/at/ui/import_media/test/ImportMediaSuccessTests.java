package ru.spice.at.ui.import_media.test;

import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import ru.testit.annotations.WorkItemIds;
import org.apache.commons.lang3.RandomStringUtils;
import org.testng.annotations.*;
import ru.spice.at.api.dto.response.metadata.DictionariesItem;
import ru.spice.at.api.import_service.ImportServiceStepDef;
import ru.spice.at.api.metadata_service.MetadataStepDef;
import ru.spice.at.common.base_test.BaseUiTest;
import ru.spice.at.common.emuns.UiCategories;
import ru.spice.at.common.emuns.dam.ImageFormat;
import ru.spice.at.common.emuns.dam.ImageParameters;
import ru.spice.at.common.listeners.TestAllureListener;
import ru.spice.at.ui.import_media.ImportMediaSettings;
import ru.spice.at.ui.import_media.ImportMediaStepDef;

import java.util.*;
import java.util.stream.Collectors;

import static ru.spice.at.common.constants.TestConstants.*;
import static ru.spice.at.common.emuns.dam.ImageParameters.*;
import static ru.spice.at.common.emuns.dam.Quality.BAD;
import static ru.spice.at.common.emuns.dam.Quality.TO_REVISION;
import static ru.spice.at.common.emuns.dam.Source.BRAND;
import static ru.spice.at.common.utils.FileHelper.createFileFromBytesArray;
import static ru.spice.at.common.utils.ImageHelper.getRandomByteImage;


@Feature("Import media")
@Story("Success import")
@Listeners({TestAllureListener.class})
public class ImportMediaSuccessTests extends BaseUiTest<ImportMediaSettings> {
    private ImportMediaStepDef importMediaStepDef;
    private MetadataStepDef metadataStepDef;
    private List<String> fileNames;
    private List<DictionariesItem> dictionaries;

    protected ImportMediaSuccessTests() {
        super(UiCategories.IMPORT_MEDIA);
    }

    @BeforeClass(alwaysRun = true)
    public void beforeClass() {
        importMediaStepDef = new ImportMediaStepDef(getWebDriver());
        metadataStepDef = new MetadataStepDef(importMediaStepDef.getAuthToken());
        dictionaries = metadataStepDef.getListCategoriesMetadata();
    }

    @BeforeMethod(alwaysRun = true)
    public void beforeMethod() {
        importMediaStepDef.goToBaseUrlWithLogin(getWebDriver());
        createFileDirection();
        fileNames = getData().images().stream().map(image -> {
            byte[] bytes = getRandomByteImage(image.getWidth(), image.getHeight(), image.getFormat().getFormatName());
            return createFileFromBytesArray(bytes, downloadPath, image.getFormat().getFormatName()) + DOT_VALUE + image.getFormat().getFormatName();
        }).collect(Collectors.toList());
    }

    @AfterMethod(alwaysRun = true)
    public void afterMethod() {
        metadataStepDef.deleteMetadata();
        new ImportServiceStepDef(importMediaStepDef.getAuthToken()).deleteImports();
        deleteFileDirection();
    }

    @Test(description = "Успешный импорт одного валидного файла", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"225869"})
    public void successImportFileTest() {
        importMediaStepDef.importFiles(Collections.singletonList(downloadPath + fileNames.get(0)));
        importMediaStepDef.checkSuccessImportFiles(Collections.singletonList(fileNames.get(0)));
    }

    @Test(description = "Успешный импорт нескольких валидных файлов", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"225867"})
    public void successImportFilesTest() {
        importMediaStepDef.importFiles(fileNames.stream().map(fileName -> downloadPath + fileName).collect(Collectors.toList()));
        importMediaStepDef.checkSuccessImportFiles(fileNames);
    }

    @Test(description = "Успешный импорт нескольких валидных файлов - страница загрузок", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"263558"})
    public void successImportFilesImportsPageTest() {
        importMediaStepDef.importFiles(fileNames.stream().map(fileName -> downloadPath + fileName).collect(Collectors.toList()));
        importMediaStepDef.checkImports(
                ImportMediaStepDef.ImportValidationType.ALL,
                Collections.singletonList(String.valueOf(fileNames.size())),
                Collections.singletonList(EMPTY_VALUE));
    }

    @Test(description = "Успешный импорт одного валидного файла - нормализовать", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"260538"})
    public void successImportFileNormalizeTest() {
        importMediaStepDef.importFiles(
                Collections.singletonList(downloadPath + fileNames.get(0)), true, false, false, false);
        importMediaStepDef.checkSuccessImportFiles(Collections.singletonList(fileNames.get(0)), 2, true);
    }

    @Test(description = "Успешный импорт одного валидного файла - нормализовать и центрировать (5% поля)", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"260541"})
    public void successImportFileNormalizeAndCenterTest() {
        importMediaStepDef.importFiles(
                Collections.singletonList(downloadPath + fileNames.get(0)), true, true, false, false);
        importMediaStepDef.checkSuccessImportFiles(Collections.singletonList(fileNames.get(0)), 2, true);
    }

    @Test(description = "Успешный импорт одного валидного файла - нормализовать, центрировать и обтравка", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"277719"})
    public void successImportFileNormalizeCenterClippingTest() {
        importMediaStepDef.importFiles(
                Collections.singletonList(downloadPath + fileNames.get(0)), true, true, true, false);
        importMediaStepDef.checkSuccessImportFiles(Collections.singletonList(fileNames.get(0)), 2, true);
    }

    @Test(description = "Успешный импорт одного валидного файла - взять SKU и приоритет из имени", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"260542"})
    public void successImportFileTakeSkuTest() {
        String sku = RandomStringUtils.randomAlphabetic(5);
        String priority = String.valueOf(new Random().nextInt(100) + 1);

        byte[] bytes = getRandomByteImage(100, 100, ImageFormat.JPEG.getFormatName());
        String fileName = createFileFromBytesArray(bytes, sku + "_" + priority, downloadPath, ImageFormat.JPEG.getFormatName()) + DOT_VALUE + ImageFormat.JPEG.getFormatName();

        importMediaStepDef.importFiles(
                Collections.singletonList(downloadPath + fileName), false, false, false, true);

        Map<ImageParameters, String> metadata = new HashMap<>() {{
            put(SKU, sku);
            put(PRIORITY, priority);
        }};
        importMediaStepDef.checkFileWithMetadata(fileName, metadata);
    }

    //todo изменилась логика перевода в архивный статус
    //@Test(description = "Загрузка файла уже существующего в системе", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"227168"})
    public void successRepeatedImportFileTest() {
        importMediaStepDef.importFiles(Collections.singletonList(downloadPath + fileNames.get(0)));
        importMediaStepDef.checkSuccessImportFiles(Collections.singletonList(fileNames.get(0)));

        importMediaStepDef.importFiles(Collections.singletonList(downloadPath + fileNames.get(0)));
        importMediaStepDef.checkSuccessImportFiles(Collections.singletonList(fileNames.get(0)), 2, true);

        importMediaStepDef.checkArchiveStatusDoubleFile(fileNames.get(0));
    }

    @Test(description = "Удаление файла из предзагрузки", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"227167"})
    public void successCancelImportFileTest() {
        importMediaStepDef.importFiles(Collections.singletonList(downloadPath + fileNames.get(0)), false);
        importMediaStepDef.cancelImport();
        importMediaStepDef.checkUnsuccessfulImportFiles(Collections.singletonList(fileNames.get(0)));
    }

    @Test(description = "Заполнение метаданных при загрузке - Категории", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"227361"})
    public void successImportFileWithCategoryTest() {
        String categoryName = dictionaries.stream().map(DictionariesItem::name).findFirst().orElse(null);

        importMediaStepDef.importFiles(downloadPath + fileNames.get(0), MASTER_CATEGORY_ID, categoryName);
        importMediaStepDef.checkSuccessImportFiles(Collections.singletonList(fileNames.get(0)));
        importMediaStepDef.checkFileWithMetadata(fileNames.get(0), MASTER_CATEGORY_ID, categoryName);
    }

    @Test(description = "Заполнение метаданных при загрузке - Качество", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"277722"})
    public void successImportFileWithQualityTest() {
        importMediaStepDef.importFiles(downloadPath + fileNames.get(0), QUALITY_ID, BAD.getName());

        importMediaStepDef.checkSuccessImportFiles(Collections.singletonList(fileNames.get(0)));
        importMediaStepDef.checkFileWithMetadata(fileNames.get(0), QUALITY_ID, BAD.getName());
    }

    @Test(description = "Заполнение метаданных при загрузке - Источник", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"227362"})
    public void successImportFileWithSourceTest() {
        importMediaStepDef.importFiles(downloadPath + fileNames.get(0), SOURCE_ID, BRAND.getName());

        importMediaStepDef.checkSuccessImportFiles(Collections.singletonList(fileNames.get(0)));
        importMediaStepDef.checkFileWithMetadata(fileNames.get(0), SOURCE_ID, BRAND.getName());
    }

    @Test(description = "Заполнение метаданных при загрузке - Теги (Ключевые слова)", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"227364"})
    public void successImportFileWithTagsTest() {
        String tags = RandomStringUtils.randomAlphabetic(7);
        importMediaStepDef.importFiles(downloadPath + fileNames.get(0), KEYWORDS, tags);

        importMediaStepDef.checkSuccessImportFiles(Collections.singletonList(fileNames.get(0)));
        importMediaStepDef.checkFileWithMetadata(fileNames.get(0), KEYWORDS, tags);
    }

    @Test(description = "Заполнение метаданных при загрузке - СТМ", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"227367"})
    public void successImportFileWithSTMTest() {
        importMediaStepDef.importFiles(downloadPath + fileNames.get(0), IS_OWN_TRADEMARK, YES);

        importMediaStepDef.checkSuccessImportFiles(Collections.singletonList(fileNames.get(0)));
        importMediaStepDef.checkFileWithMetadata(fileNames.get(0), IS_OWN_TRADEMARK, YES);
    }

    @Test(description = "Заполнение метаданных при загрузке - Задача PIMS", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"260544"})
    public void successImportFileWithExternalTaskIdTest() {
        String externalTaskId = String.valueOf(new Random().nextInt(1000) + 1);
        importMediaStepDef.importFiles(downloadPath + fileNames.get(0), EXTERNAL_TASK_ID, externalTaskId);

        importMediaStepDef.checkSuccessImportFiles(Collections.singletonList(fileNames.get(0)));
        importMediaStepDef.checkFileWithMetadata(fileNames.get(0), EXTERNAL_TASK_ID, externalTaskId);
    }

    @Test(description = "Заполнение метаданных при загрузке - несколько параметров", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"227370"})
    public void successImportFileWithSomeMetadataTest() {
        Map<ImageParameters, String> metadata = new HashMap<>() {{
            put(EXTERNAL_TASK_ID, String.valueOf(new Random().nextInt(1000) + 1));
            put(SOURCE_ID, BRAND.getName());
            put(IS_OWN_TRADEMARK, YES);
        }};

        importMediaStepDef.importFiles(Collections.singletonList(downloadPath + fileNames.get(0)), metadata);
        importMediaStepDef.checkSuccessImportFiles(Collections.singletonList(fileNames.get(0)));
        importMediaStepDef.checkFileWithMetadata(fileNames.get(0), metadata);
    }

    @Test(description = "Заполнение метаданных при загрузке - все параметры", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"227371"})
    public void successImportFileWithAllMetadataTest() {
        String categoryName = dictionaries.stream().map(DictionariesItem::name).findFirst().orElse(null);

        Map<ImageParameters, String> metadata = new HashMap<>() {{
            put(EXTERNAL_TASK_ID, String.valueOf(new Random().nextInt(1000) + 1));
            put(MASTER_CATEGORY_ID, categoryName);
            put(SOURCE_ID, BRAND.getName());
            put(KEYWORDS, RandomStringUtils.randomAlphabetic(7));
            put(QUALITY_ID, TO_REVISION.getName());
            put(IS_OWN_TRADEMARK, YES);
        }};

        importMediaStepDef.importFiles(Collections.singletonList(downloadPath + fileNames.get(0)), metadata);
        importMediaStepDef.checkSuccessImportFiles(Collections.singletonList(fileNames.get(0)));
        importMediaStepDef.checkFileWithMetadata(fileNames.get(0), metadata);
    }
}
