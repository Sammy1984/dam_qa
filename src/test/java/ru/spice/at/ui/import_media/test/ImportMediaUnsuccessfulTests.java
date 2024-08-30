package ru.spice.at.ui.import_media.test;

import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import ru.testit.annotations.WorkItemIds;
import org.apache.commons.lang3.RandomStringUtils;
import org.testng.annotations.*;
import ru.spice.at.api.import_service.ImportServiceStepDef;
import ru.spice.at.api.metadata_service.MetadataStepDef;
import ru.spice.at.common.base_test.BaseUiTest;
import ru.spice.at.common.emuns.dam.ImageFormat;
import ru.spice.at.common.emuns.UiCategories;
import ru.spice.at.common.listeners.TestAllureListener;
import ru.spice.at.common.utils.Assert;
import ru.spice.at.ui.import_media.ImportMediaSettings;
import ru.spice.at.ui.import_media.ImportMediaStepDef;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static ru.spice.at.common.emuns.dam.ImageParameters.KEYWORDS;
import static ru.spice.at.common.utils.FileHelper.createFileFromBytesArray;
import static ru.spice.at.common.utils.ImageHelper.getRandomByteImage;

@Deprecated
@Feature("Import media")
@Story("Unsuccessful import validation")
@Listeners({TestAllureListener.class})
public class ImportMediaUnsuccessfulTests extends BaseUiTest<ImportMediaSettings> {
    private ImportMediaStepDef importMediaStepDef;
    private byte[] bytes;

    protected ImportMediaUnsuccessfulTests() {
        super(UiCategories.IMPORT_MEDIA);
    }

    @BeforeClass(alwaysRun = true)
    public void beforeClass() {
        importMediaStepDef = new ImportMediaStepDef(getWebDriver());
        bytes = getRandomByteImage(10, 10, ImageFormat.JPEG.getFormatName());
    }

    @BeforeMethod(alwaysRun = true)
    public void beforeMethod() {
        importMediaStepDef.goToBaseUrlWithLogin(getWebDriver());
        createFileDirection();
    }

    @AfterMethod(alwaysRun = true)
    public void afterMethod() {
        new ImportServiceStepDef(importMediaStepDef.getAuthToken()).deleteValidations();
        deleteFileDirection();
    }

    @AfterClass(alwaysRun = true)
    public void afterClass() {
        new MetadataStepDef(importMediaStepDef.getAuthToken()).deleteMetadata();
    }

    @Test(description = "Ошибка импорта 'Неверный. формат файла'", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"225875"})
    public void unsuccessfulImportInvalidFormatImageTest() {
        byte[] bytes = getRandomByteImage(10, 10, ImageFormat.INVALID.getFormatName());
        String fileName = createFileFromBytesArray(bytes, downloadPath, ImageFormat.INVALID.getFormatName()) + "." + ImageFormat.INVALID.getFormatName();

        importMediaStepDef.importFiles(Collections.singletonList(downloadPath + fileName));
        importMediaStepDef.checkUnsuccessfulImportFiles(Collections.singletonList(fileName));

        importMediaStepDef.checkImportFilesValidation(Collections.singletonList(new ImportMediaStepDef.FileValidation(fileName, getData().typeError())));
    }

    @Test(description = "Ошибка импорта 'Битый файл'", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"225872"})
    public void unsuccessfulImportBrokenImageTest() {
        byte[] bytes = getRandomByteImage(100, 100, ImageFormat.JPEG.getFormatName());
        int brokenCount = 30;
        Assert.equalsTrueParameter(bytes.length > brokenCount, "количество байтов");
        for (int i = 0; i < brokenCount; i++) {
            bytes[i] = 0;
        }

        String fileName = createFileFromBytesArray(bytes, downloadPath, ImageFormat.JPEG.getFormatName()) + "." + ImageFormat.JPEG.getFormatName();

        importMediaStepDef.importFiles(Collections.singletonList(downloadPath + fileName));
        importMediaStepDef.checkUnsuccessfulImportFiles(Collections.singletonList(fileName));

        importMediaStepDef.checkImportFilesValidation(Collections.singletonList(
                new ImportMediaStepDef.FileValidation(fileName, Arrays.asList(getData().fileError(), getData().formatError()))));
    }

    @Test(description = "Ошибка импорта 'Пустой файл'", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"225870"})
    public void unsuccessfulImportEmptyImageTest() {
        String fileName = createFileFromBytesArray(new byte[0], downloadPath, ImageFormat.JPEG.getFormatName()) + "." + ImageFormat.JPEG.getFormatName();

        importMediaStepDef.importFiles(Collections.singletonList(downloadPath + fileName));
        importMediaStepDef.checkUnsuccessfulImportFiles(Collections.singletonList(fileName));

        importMediaStepDef.checkImportFilesValidation(Collections.singletonList(
                new ImportMediaStepDef.FileValidation(fileName, Arrays.asList(getData().fileError(), getData().formatError()))));
    }

    @Test(description = "Ошибка импорта 'Длина наименования'", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"225876"})
    public void unsuccessfulImportLongNameImageTest() {
        String longName = RandomStringUtils.randomAlphabetic(200);
        String fileName = createFileFromBytesArray(bytes, longName, downloadPath, ImageFormat.JPEG.getFormatName()) + "." + ImageFormat.JPEG.getFormatName();

        importMediaStepDef.importFiles(Collections.singletonList(downloadPath + fileName));
        importMediaStepDef.checkUnsuccessfulImportFiles(Collections.singletonList(fileName));

        importMediaStepDef.checkImportFilesValidation(Collections.singletonList(new ImportMediaStepDef.FileValidation(fileName, getData().longNameError())));
    }

    @Test(description = "Ошибка импорта нескольких файлов", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"225874"})
    public void unsuccessfulImportInvalidSomeImagesTest() {
        String longName = RandomStringUtils.randomAlphabetic(200);
        String fileFailedName = createFileFromBytesArray(bytes, longName, downloadPath, ImageFormat.JPEG.getFormatName()) + "." + ImageFormat.JPEG.getFormatName();
        String fileFailedFormatName = createFileFromBytesArray(bytes, downloadPath, ImageFormat.INVALID.getFormatName()) + "." + ImageFormat.INVALID.getFormatName();
        List<String> fileNames = Arrays.asList(fileFailedName, fileFailedFormatName);

        importMediaStepDef.importFiles(fileNames.stream().map(name -> downloadPath + name).collect(Collectors.toList()));
        importMediaStepDef.checkUnsuccessfulImportFiles(fileNames);

        List<ImportMediaStepDef.FileValidation> fileValidations = Arrays.asList(
                new ImportMediaStepDef.FileValidation(fileFailedName, getData().longNameError()),
                new ImportMediaStepDef.FileValidation(fileFailedFormatName, getData().typeError())
        );
        importMediaStepDef.checkImportFilesValidation(fileValidations);
    }

    @Test(description = "Загрузка валидных и не валидных файлов", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"225877"})
    public void unsuccessfulImportValidAndInvalidImageTest() {
        byte[] bytes = getRandomByteImage(10, 10, ImageFormat.INVALID.getFormatName());
        String invalidFileName = createFileFromBytesArray(bytes, downloadPath, ImageFormat.INVALID.getFormatName()) +
                "." + ImageFormat.INVALID.getFormatName();

        bytes = getRandomByteImage(10, 10, ImageFormat.JPEG.getFormatName());
        String validFileName = createFileFromBytesArray(bytes, downloadPath, ImageFormat.JPEG.getFormatName()) + "." + ImageFormat.JPEG.getFormatName();

        importMediaStepDef.importFiles(Arrays.asList(downloadPath + invalidFileName, downloadPath + validFileName));

        importMediaStepDef.checkSuccessImportFiles(Collections.singletonList(validFileName));
        importMediaStepDef.checkUnsuccessfulImportFiles(Collections.singletonList(invalidFileName));

        importMediaStepDef.checkImportFilesValidation(
                Collections.singletonList(new ImportMediaStepDef.FileValidation(invalidFileName, getData().typeError())));
    }

    @Test(description = "Несколько ошибок импорта одного файла", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"225873"})
    public void unsuccessfulImportLongInvalidNameAndFormatImageTest() {
        String fileFailedName = createFileFromBytesArray(bytes, RandomStringUtils.randomAlphabetic(200), downloadPath, ImageFormat.INVALID.getFormatName()) +
                "." + ImageFormat.INVALID.getFormatName();

        importMediaStepDef.importFiles(Collections.singletonList(downloadPath + fileFailedName));
        importMediaStepDef.checkUnsuccessfulImportFiles(Collections.singletonList(fileFailedName));

        ImportMediaStepDef.FileValidation fileValidation =
                new ImportMediaStepDef.FileValidation(fileFailedName, Arrays.asList(getData().longNameError(), getData().typeError()));
        importMediaStepDef.checkImportFilesValidation(Collections.singletonList(fileValidation));
    }

    @Test(description = "Заполнение поля Ключевые слова - длина поля", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"227409"})
    public void unsuccessfulImportLongKeywordsTest() {
        bytes = getRandomByteImage(10, 10, ImageFormat.JPEG.getFormatName());
        String validFileName = createFileFromBytesArray(bytes, downloadPath, ImageFormat.JPEG.getFormatName()) + "." + ImageFormat.JPEG.getFormatName();

        importMediaStepDef.importFiles(Collections.singletonList(downloadPath + validFileName));
        importMediaStepDef.confirmImportWithMetadata(Collections.singletonMap(KEYWORDS, RandomStringUtils.randomAlphabetic(31)), false);
        importMediaStepDef.checkUnsuccessfulImportMetadata(Collections.singletonMap(KEYWORDS, getData().tagsError()));

        importMediaStepDef.checkUnsuccessfulImportFiles(Collections.singletonList(validFileName));
    }
}
