package ru.spice.at.ui.import_media;

import io.qameta.allure.Step;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.awaitility.Awaitility;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import ru.spice.at.common.base_test.AbstractUiStepDef;
import ru.spice.at.common.emuns.dam.ImageParameters;
import ru.spice.at.common.utils.Assert;
import ru.spice.at.ui.controls.Button;
import ru.spice.at.ui.controls.Label;
import ru.spice.at.ui.pages.spice.dam.*;
import ru.spice.at.ui.pages.spice.dam.modals.DownloadInfoModalPage;
import ru.spice.at.ui.pages.spice.dam.modals.ImportInfoModalPage;
import ru.spice.at.ui.pages.spice.dam.modals.ImportMediaModalPage;
import ru.spice.at.ui.pages.spice.dam.modals.ImportMetadataModalPage;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static ru.spice.at.common.constants.TestConstants.*;
import static ru.spice.at.common.emuns.Role.ADMINISTRATOR;
import static ru.spice.at.common.emuns.dam.Status.ARCHIVE;
import static ru.spice.at.common.emuns.dam.Status.NEW;
import static ru.spice.at.common.utils.Assert.*;

public class ImportMediaStepDef extends AbstractUiStepDef {
    private static final String DATE_REMOTE_PATTERN = "M/d/yyyy";
    private static final String DATE_PATTERN = "dd.MM.yyyy";

    private final MediaFilesPage mediaFilesPage;
    private final ImportMediaModalPage importMediaPage;
    private final ValidationErrorsPage validationErrorsPage;
    @Deprecated
    private final ImportMetadataModalPage importMetadataModalPage;
    private final DownloadInfoModalPage downloadInfoModalPage;
    private final ImportsFilesPage importsFilesPage;
    private final ImportInfoModalPage importInfoModalPage;

    public ImportMediaStepDef(WebDriver webDriver) {
        mediaFilesPage = new MediaFilesPage(webDriver);
        importMediaPage = new ImportMediaModalPage(webDriver);
        validationErrorsPage = new ValidationErrorsPage(webDriver);
        importMetadataModalPage = new ImportMetadataModalPage(webDriver);
        downloadInfoModalPage = new DownloadInfoModalPage(webDriver);
        importsFilesPage = new ImportsFilesPage(webDriver);
        importInfoModalPage = new ImportInfoModalPage(webDriver);
    }

    public void importFiles(List<String> filePaths) {
        importFiles(filePaths, false, false, false, false);
    }

    public void importFiles(List<String> filePaths, boolean clickImport) {
        importFiles(filePaths, Collections.emptyMap(), false, false, false, false, clickImport);
    }

    public void importFiles(String filePath, boolean normalize, boolean center, boolean clipping, boolean takeSku) {
        importFiles(Collections.singletonList(filePath), Collections.emptyMap(), normalize, center, clipping, takeSku, true);
    }

    public void importFiles(List<String> filePaths, boolean normalize, boolean center, boolean clipping, boolean takeSku) {
        importFiles(filePaths, Collections.emptyMap(), normalize, center, clipping, takeSku, true);
    }

    public void importFiles(String filePath, ImageParameters imageParameter, String value) {
        importFiles(Collections.singletonList(filePath), Collections.singletonMap(imageParameter, value));
    }

    public void importFiles(List<String> filePaths, Map<ImageParameters, String> imageParameters) {
        importFiles(filePaths, imageParameters, false, false, false, false, true);
    }

    @Step("Загружаем файлы")
    public void importFiles(List<String> filePaths,
                            Map<ImageParameters, String> imageParameters,
                            boolean normalize,
                            boolean center,
                            boolean clipping,
                            boolean takeSku,
                            boolean clickImport) {
        mediaFilesPage.checkLoadPage();
        mediaFilesPage.getViewModuleButton().click();
        mediaFilesPage.getImportFileButton().click();
        importMediaPage.checkLoadPage();
        filePaths.forEach(filePath -> importMediaPage.getImportFileInput().setHiddenText(filePath));

        int size = importMediaPage.getMediaFilesCollection().waitForElements().getSize();
        Assert.compareParameters(filePaths.size(), size, "files size");

        if (normalize) {
            importMediaPage.getNormalizeCheckBox().check("class", "checked");
        }
        if (center) {
            importMediaPage.getCenterCheckBox().check("class", "checked");
        }
        if (clipping) {
            importMediaPage.getClippingCheckBox().check("class", "checked");
        }
        if (takeSku) {
            importMediaPage.getTakeSkuCheckBox().check("class", "checked");
        }
        if (!imageParameters.isEmpty()) {
            importWithMetadata(imageParameters);
        }
        if (clickImport) {
            importMediaPage.getImportMediaButton().click();
        }
    }

    @Step("Добавляем метадату в импорт '{imageParameters}'")
    private void importWithMetadata(Map<ImageParameters, String> imageParameters) {
        imageParameters.forEach((k, v) -> {
            switch (k) {
                case MASTER_CATEGORY_ID: {
                    importMediaPage.getCategoryComboBox().clickSelect(v);
                    break;
                }
                case SOURCE_ID: {
                    importMediaPage.getSourceComboBox().clickSelect(v);
                    break;
                }
                case KEYWORDS: {
                    importMediaPage.getTagsField().setText(v).sendKeys(Keys.ENTER);
                    break;
                }
                case IS_OWN_TRADEMARK: {
                    importMediaPage.getIsOwnTrademarkComboBox().clickSelect(v);
                    break;
                }
                case EXTERNAL_TASK_ID: {
                    importMediaPage.getExternalTaskIdField().setText(v);
                    break;
                }
                case QUALITY_ID: {
                    importMediaPage.getQualityComboBox().clickSelect(v);
                    break;
                }
                default:
                    throw new RuntimeException("Нужный параметр не поддерживается");
            }
        });
    }

    @Step("Отменяем загрузку файла ")
    public void cancelImport() {
        importMediaPage.<ImportMediaModalPage>checkLoadPage().getMediaFilesCollection().
                getElement(Button.class, 0).hover();
        importMediaPage.<ImportMediaModalPage>checkLoadPage().getMediaFilesCollection().
                getElement(Button.class, 0, By.xpath(".//..//button")).click();
        importMediaPage.getMediaFilesCollection().waitForEmpty();

        importMediaPage.getCancelImportButton().click();
    }

    @Deprecated
    public void confirmImportWithMetadata(ImageParameters imageParameter, String value) {
        confirmImportWithMetadata(Collections.singletonMap(imageParameter, value), true);
    }

    @Deprecated
    @Step("Подтверждаем загрузку c метадатой '{imageParameters}'")
    public void confirmImportWithMetadata(Map<ImageParameters, String> imageParameters, boolean confirm) {
        importMetadataModalPage.checkLoadPage();
        imageParameters.forEach((k, v) -> {
            switch (k) {
                case MASTER_CATEGORY_ID: {
                    importMetadataModalPage.getCategoryComboBox().clickSelect(v);
                    break;
                }
                case SOURCE_ID: {
                    importMetadataModalPage.getSourceComboBox().clickSelect(v);
                    break;
                }
                case KEYWORDS: {
                    importMetadataModalPage.getTagsField().setText(v).sendKeys(Keys.ENTER);
                    break;
                }
                case IS_OWN_TRADEMARK: {
                    importMetadataModalPage.getIsOwnTrademarkComboBox().clickSelect(v);
                    break;
                }
                case IS_COPYRIGHT: {
                    importMetadataModalPage.getIsCopyrightComboBox().clickSelect(v);
                    break;
                }
                case IS_RAW_IMAGE: {
                    importMetadataModalPage.getIsRawImageComboBox().clickSelect(v);
                    break;
                }
                case EXTERNAL_TASK_ID: {
                    importMetadataModalPage.getExternalTaskIdField().setText(v);
                    break;
                }
                default:
                    throw new RuntimeException("Нужный параметр не поддерживается");
            }
        });

        if (confirm) {
            importMetadataModalPage.getSaveButton().click();
            //todo функционал устарел, убрать после прогона
//            confirmModalPage.checkLoadPage();
//            confirmModalPage.getCloseButton().click();
        }
    }

    public void checkSuccessImportFiles(List<String> fileNames) {
        checkSuccessImportFiles(fileNames, 1, false);
    }

    @Step("Проверяем успешную загрузку для файлов {fileNames}")
    public void checkSuccessImportFiles(List<String> fileNames, int count, boolean cancelFilter) {
        mediaFilesPage.checkLoadPage();
        fileNames.forEach(fileName -> {
            Awaitility.given().ignoreExceptions().pollDelay(2, TimeUnit.SECONDS)
                    .pollInterval(1, TimeUnit.SECONDS)
                    .and().timeout(30, TimeUnit.SECONDS)
                    .await("Изображения не найдены")
                    .until(() -> {
                        mediaFilesPage.checkLoadPage();
                        if (cancelFilter) {
                            mediaFilesPage.getCancelFiltersButton().click();
                        }
                        mediaFilesPage.getSearchField().setText(fileName.split("\\.", 2)[0]).sendKeys(Keys.ENTER);
                        mediaFilesPage.getMediaFilesModuleCollection().waitForElements();
                        if (mediaFilesPage.getMediaFilesModuleCollection().getSize() == count) {
                            return true;
                        }
                        else {
                            mediaFilesPage.refresh();
                            return false;
                        }
                    });
            String name = mediaFilesPage.getMediaFilesModuleCollection().getElement(Label.class, 0).getText();
            Assert.compareParameters(
                    fileName.split("\\.", 2)[0], name.split("\\.", 2)[0], "fileName");
            mediaFilesPage.getSearchField().hover();
            mediaFilesPage.getCancelSearchButton().click();
        });
    }

    @Step("Проверяем отсутствие загруженных файлов {fileNames}")
    public void checkUnsuccessfulImportFiles(List<String> fileNames) {
        mediaFilesPage.checkLoadPage();
        fileNames.forEach(fileName -> {
            mediaFilesPage.getSearchField().highlightDeleteText().setText(fileName.split("\\.", 2)[0]).sendKeys(Keys.ENTER);
            mediaFilesPage.getMediaFilesModuleCollection().waitForEmpty();
        });
    }

    @Step("Проверяем сообщения при вводе метаданных {parametersMessage}")
    public void checkUnsuccessfulImportMetadata(Map<ImageParameters, String> parametersMessage) {
        importMetadataModalPage.checkLoadPage();
        parametersMessage.forEach((k, v) -> {
            String value;
            switch (k) {
                case KEYWORDS: {
                    value = importMetadataModalPage.getTagsMessageLabel().getText();
                    break;
                }
                default:
                    throw new RuntimeException("Нужный параметр не поддерживается");
            }
            compareParameters(v, value, "текст сообщения");
        });

        importMetadataModalPage.getSaveButton().click();
        importMediaPage.<ImportMediaModalPage>checkLoadPage().getCancelImportButton().click();
    }

    @Step("Проверяем архивный статус задублированных файлов")
    public void checkArchiveStatusDoubleFile(String filename) {
        mediaFilesPage.checkLoadPage();
        mediaFilesPage.getSearchField().setText(filename.split("\\.", 2)[0]).sendKeys(Keys.ENTER);
        List<String> actualStatuses = new LinkedList<>();
        mediaFilesPage.getMediaFilesModuleCollection().waitForElementsSize(2).getElementList(Button.class).forEach(
                element -> {
                    element.click();
                    String status = mediaFilesPage.getMetadataBlock().getStatusComboBox().getTextValue();
                    actualStatuses.add(status);
                }
        );
        Assert.compareParameters(new LinkedList<>(Arrays.asList(ARCHIVE.getName(), NEW.getName())), actualStatuses, "statuses");
    }

    public void checkFileWithMetadata(String filename, ImageParameters imageParameter, String value) {
        checkFileWithMetadata(filename, Collections.singletonMap(imageParameter, value));
    }

    @Step("Проверяем успешную загрузку для файла '{filename}' с метадатой {imageParameters}")
    public void checkFileWithMetadata(String filename, Map<ImageParameters, String> imageParameters) {
        mediaFilesPage.checkLoadPage();
        mediaFilesPage.getSearchField().setText(filename.split("\\.", 2)[0]).sendKeys(Keys.ENTER);
        mediaFilesPage.getMediaFilesModuleCollection().waitForElementsSize(1).clickTo(0);

        imageParameters.forEach((k, v) -> {
            String actualValue;
            switch (k) {
                case MASTER_CATEGORY_ID: {
                    actualValue = mediaFilesPage.getMetadataBlock().getCategoryComboBox().getTextValue();
                    break;
                }
                case SOURCE_ID: {
                    actualValue = mediaFilesPage.getMetadataBlock().getSourceComboBox().getTextValue();
                    break;
                }
                case KEYWORDS: {
                    actualValue = mediaFilesPage.getMetadataBlock().getTagsCollection().waitForElements().getText(0);
                    break;
                }
                case IS_OWN_TRADEMARK: {
                    actualValue = mediaFilesPage.getMetadataBlock().getIsOwnTrademarkComboBox().getTextValue();
                    break;
                }
                case EXTERNAL_TASK_ID: {
                    actualValue = mediaFilesPage.getMetadataBlock().getExternalTaskIdField().getTextValue();
                    break;
                }
                case QUALITY_ID: {
                    actualValue = mediaFilesPage.getMetadataBlock().getQualityComboBox().getTextValue();
                    break;
                }
                case SKU: {
                    actualValue = mediaFilesPage.getMetadataBlock().getSkuField().getTextValue();
                    break;
                }
                case PRIORITY: {
                    actualValue = mediaFilesPage.getMetadataBlock().getPriorityField().getTextValue();
                    break;
                }
                default:
                    throw new RuntimeException("Нужный параметр не поддерживается");
            }
            Assert.compareParameters(v, actualValue, k.getName());
        });
    }

    @Step("Проверяем валидацию незагруженных файлов")
    public void checkImportFilesValidation(List<FileValidation> filesValidation) {
        mediaFilesPage.<MediaFilesPage>checkLoadPage().getImportsButton().click();

        validationErrorsPage.checkLoadPage();
        validationErrorsPage.getValidationTable().getRowsCollection().waitForElements(20);

        int expFileCount = filesValidation.size();
        AtomicInteger expAtomicErrorCount = new AtomicInteger();
        filesValidation.forEach(v -> expAtomicErrorCount.set(expAtomicErrorCount.get() + v.errors.size()));
        int expErrorCount = expAtomicErrorCount.get();

        String fileCount = validationErrorsPage.getValidationTable().getCellValue(0, 2);
        String errorCount = validationErrorsPage.getValidationTable().getCellValue(0, 3);

        assertAll(
                () -> compareParameters(String.valueOf(expFileCount), fileCount, "fileCount"),
                () -> compareParameters(String.valueOf(expErrorCount), errorCount, "errorCount")
        );

        validationErrorsPage.getValidationTable().getRowsCollection().clickTo(0);

        downloadInfoModalPage.checkLoadPage();
        List<String> expErrorsRows = filesValidation.stream().map(FileValidation::getErrorsRow).collect(Collectors.toList());
        List<String> errorsRows = downloadInfoModalPage.getValidationInfoCollection().waitForElements().getElementList(Label.class).
                stream().map(Label::getText).collect(Collectors.toList());

        compareParameters(new LinkedList<>(expErrorsRows), new LinkedList<>(errorsRows), "строки с ошибками");
    }

    public void checkImports(ImportValidationType type, List<String> importCounts) {
        checkImports(type, importCounts, null);
    }

    @Step("Проверка страницы импорта на вкладке {type}")
    public void checkImports(ImportValidationType type, List<String> importCounts, List<String> importErrors) {
        mediaFilesPage.<MediaFilesPage>checkLoadPage().getImportsButton().click();
        importsFilesPage.checkLoadPage();

        switch (type) {
            case ALL: {
                importsFilesPage.getAllImportsButton().click();
                break;
            }
            case WITH_ERRORS: {
                importsFilesPage.getImportsWithErrorsButton().click();
                break;
            }
            case WITHOUT_ERRORS: {
                importsFilesPage.getImportsWithoutErrorsButton().click();
                break;
            }
        }

        importsFilesPage.getImportsTable().getRowsCollection().waitForElements();
        for (int i = 0; i < importCounts.size(); i++) {
            compareParameters(importErrors == null ? EMPTY_VALUE : importErrors.get(i), importsFilesPage.getImportsTable().getCellValue(i, 3), "количество файлов с ошибками");
            compareParameters(importCounts.get(i), importsFilesPage.getImportsTable().getCellValue(i, 2), "количество импортов");
            compareParameters(ADMINISTRATOR.getFullName(), importsFilesPage.getImportsTable().getCellValue(i, 1), "загрузчик");
            contains(
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern(standProperties.getSettings().remote() ? DATE_REMOTE_PATTERN : DATE_PATTERN)),
                    importsFilesPage.getImportsTable().getCellValue(i, 0), "дата");
        }
    }

    @Step("Проверка модального окна для импортов")
    public void checkModalImports(Map<ImportValidationType, List<String>> fileStatusMap, int importNumber) {
        importsFilesPage.checkLoadPage();
        importsFilesPage.getImportsTable().getRowsCollection().waitForElements().
                getElement(Button.class, importNumber, By.xpath("./th")).click();

        importInfoModalPage.checkLoadPage();
        importInfoModalPage.getFilesListCollection().waitForElements();

        assertAll(
                () -> contains(LocalDateTime.now().format(
                        DateTimeFormatter.ofPattern(standProperties.getSettings().remote() ? DATE_REMOTE_PATTERN : DATE_PATTERN)),
                        importInfoModalPage.getImportDateLabel().getText(), "дата"),
                () -> compareParameters(ADMINISTRATOR.getFullName(), importInfoModalPage.getImportUserLabel().getText(), "загрузчик")
        );

        fileStatusMap.forEach((k, v) -> {
            switch (k) {
                case ALL: {
                    importInfoModalPage.getAllFilesButton().click();
                    break;
                }
                case WITHOUT_ERRORS: {
                    importInfoModalPage.getFilesWithoutErrorsButton().click();
                    break;
                }
                case WITH_ERRORS: {
                    importInfoModalPage.getFilesWithErrorsButton().click();
                    break;
                }
            }

            List<String> collectionLabelsText = importInfoModalPage.getFilesListCollection().getElementList(Label.class).
                    stream().map(Label::getText).collect(Collectors.toList());
            List<String> actualStatusList = collectionLabelsText.
                    stream().map(s -> s.split("\n")[1]).collect(Collectors.toList());
            List<String> actualNamesList = collectionLabelsText.
                    stream().map(s -> s.split("\n")[0]).collect(Collectors.toList());

            compareParameters(v, actualStatusList, "статус");
            actualNamesList.forEach(n -> notNullOrEmptyParameter(n, "имя"));
        });
    }

    @AllArgsConstructor
    public static class FileValidation {
        private String filename;
        @Getter
        private List<String> errors;

        public FileValidation(String filename, String error) {
            this.filename = filename;
            this.errors = Collections.singletonList(error);
        }

        public String getErrorsRow() {
            String errorsString = EMPTY_VALUE;
            for (String error : errors) {
                errorsString += error + COMMA_SPICE_VALUE;
            }
            errorsString = errorsString.substring(0, errorsString.length() - 2);
            return filename + ENTER_VALUE + errorsString;
        }
    }

    @AllArgsConstructor
    @Getter
    public enum ImportValidationType {
        ALL("Все загрузки"),
        WITH_ERRORS("С ошибками"),
        WITHOUT_ERRORS("Без ошибок");

        private final String formatName;
    }
}
