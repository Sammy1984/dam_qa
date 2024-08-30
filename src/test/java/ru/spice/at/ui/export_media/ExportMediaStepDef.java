package ru.spice.at.ui.export_media;

import io.qameta.allure.Step;
import lombok.Data;
import lombok.experimental.Accessors;
import org.awaitility.Awaitility;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.interactions.Actions;
import ru.spice.at.common.base_test.AbstractUiStepDef;
import ru.spice.at.common.emuns.dam.ImageFormat;
import ru.spice.at.ui.controls.Button;
import ru.spice.at.ui.controls.Element;
import ru.spice.at.ui.controls.Label;
import ru.spice.at.ui.pages.spice.dam.ExportsFilesPage;
import ru.spice.at.ui.pages.spice.dam.blocks.MediaContextMenuBlock;
import ru.spice.at.ui.pages.spice.dam.blocks.MetadataBlock;
import ru.spice.at.ui.pages.spice.dam.modals.ExportFilesModalPage;
import ru.spice.at.ui.pages.spice.dam.MediaFilesPage;
import ru.spice.at.ui.pages.spice.dam.ValidationErrorsPage;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static ru.spice.at.common.utils.Assert.*;
import static ru.spice.at.common.utils.FileHelper.*;
import static ru.spice.at.ui.pages.spice.dam.ValidationErrorsPage.TABLE_DOWNLOAD_BUTTON;

public class ExportMediaStepDef extends AbstractUiStepDef {
    private final MediaFilesPage mediaFilesPage;
    private final ExportsFilesPage exportsFilesPage;
    private final ValidationErrorsPage validationErrorsPage;
    private final ExportFilesModalPage downloadFilesModalPage;
    private final Actions actions;

    public ExportMediaStepDef(WebDriver webDriver) {
        mediaFilesPage = new MediaFilesPage(webDriver);
        validationErrorsPage = new ValidationErrorsPage(webDriver);
        downloadFilesModalPage = new ExportFilesModalPage(webDriver);
        exportsFilesPage = new ExportsFilesPage(webDriver);
        actions = new Actions(webDriver);
    }

    public void exportImage(List<Image> images, ImageFormat format, boolean contextClick) {
        exportImage(images, format, null, contextClick);
    }

    @Step("Загрузка медиафайлов")
    public void exportImage(List<Image> images, ImageFormat format, String archiveName, boolean contextClick) {
        mediaFilesPage.checkLoadPage();
        mediaFilesPage.getViewModuleButton().click();
        mediaFilesPage.getMediaFilesModuleCollection().waitForElements().
                chooseElements(images.stream().map(Image::filename).collect(Collectors.toList()));

        if(contextClick && images.size() == 1) {
            actions.contextClick().build().perform();
            mediaFilesPage.getMediaContextMenuBlock().<MediaContextMenuBlock>checkLoadPage().getDownloadButton().hover();
        } else if (contextClick) {
            actions.contextClick().build().perform();
            mediaFilesPage.getMediaContextMenuBlock().<MediaContextMenuBlock>checkLoadPage().getExportButton().hover();
        }
        else if (images.size() == 1) {
            mediaFilesPage.getMetadataBlock().<MetadataBlock>checkLoadPage().getDownloadButton().click();
        } else {
            mediaFilesPage.getMetadataBlock().<MetadataBlock>checkLoadPage().getExportButton().click();
        }

        if (!contextClick) {
            switch (format) {
                case PNG:
                    mediaFilesPage.getMediaContextMenuBlock().<MediaContextMenuBlock>checkLoadPage().getPngFormatButton().click();
                    break;
                case JPEG:
                    mediaFilesPage.getMediaContextMenuBlock().<MediaContextMenuBlock>checkLoadPage().getJpegFormatButton().click();
                    break;
                default:
                    mediaFilesPage.getMediaContextMenuBlock().<MediaContextMenuBlock>checkLoadPage().getCurrentFormatButton().click();
            }
        } else {
            mediaFilesPage.getMediaContextMenuBlock().<MediaContextMenuBlock>checkLoadPage().getCurrentFormatButton().click();
        }

        if(images.size() == 1) {
            mediaFilesPage.getMediaFilesModuleCollection().waitForElements();
        } else {
            downloadFilesModalPage.checkLoadPage();
            if (format != ImageFormat.CURRENT) {
                downloadFilesModalPage.getFormatComboBox().clickSelect(format.toString());
            }

            if (archiveName != null) {
                downloadFilesModalPage.getArchiveNameField().setText(archiveName);
            }

            downloadFilesModalPage.getExportButton().click();
        }
    }

    @Step("Отмена загрузки медиафайлов")
    public void cancelExportImage(List<Image> images) {
        mediaFilesPage.checkLoadPage();
        mediaFilesPage.getViewModuleButton().click();
        mediaFilesPage.getMediaFilesModuleCollection().waitForElements().
                chooseElements(images.stream().map(Image::filename).collect(Collectors.toList()));

        mediaFilesPage.getMetadataBlock().<MetadataBlock>checkLoadPage().getExportButton().click();
        mediaFilesPage.getMediaContextMenuBlock().<MediaContextMenuBlock>checkLoadPage().getCurrentFormatButton().click();

        downloadFilesModalPage.checkLoadPage();
        downloadFilesModalPage.getCloseButton().click();

        mediaFilesPage.getMediaFilesModuleCollection().waitForElements();
    }

    public void checkExports(Integer countFiles) {
        checkExports("Архив", null, countFiles);
    }

    @Step("Проверяем экспорт архива медиафайлов")
    public void checkExports(String archiveName, LocalDate searchDate, Integer countFiles) {
        mediaFilesPage.getExportsButton().click();
        int size = exportsFilesPage.<ExportsFilesPage>checkLoadPage().getExportsCollection().waitForElements().getSize();

        Awaitility.given().pollDelay(300, TimeUnit.MILLISECONDS)
                .pollInterval(1, TimeUnit.SECONDS)
                .and().timeout(30, TimeUnit.SECONDS)
                .await("Архив не перешел в статус 'Скачать'")
                .until(() -> {
                    exportsFilesPage.refresh();
                    String status = exportsFilesPage.getExportsCollection().waitForElements().
                            getElement(Label.class, 0, By.xpath("./div/div/p")).getText();
                    return status.equals("Скачать");
                });

        if (searchDate != null) {
            exportsFilesPage.getDateField().click();
            exportsFilesPage.getDateField().setText(searchDate.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")));
            List<Label> rows =
                    exportsFilesPage.<ExportsFilesPage>checkLoadPage().getExportsCollection().getElementList(Label.class);
            equalsTrueParameter(rows.size() <= size, "количество загрузок");

            rows.forEach(row -> equalsTrueParameter(
                    row.getElement().
                            findElement(By.xpath("./div/p[1]")).getText().
                            contains(searchDate.format(DateTimeFormatter.ofPattern("dd.MM.yy"))),
                    "дата"));
        }

        String actualCount = exportsFilesPage.getExportsCollection().
                getElement(Label.class, 0, By.xpath("./div/p[2]")).getText();
        String actualName = exportsFilesPage.getExportsCollection().
                getElement(Label.class, 0, By.xpath("./div/p[3]")).getText();

        assertAll(
                () -> compareParameters(archiveName, actualName, "archive name"),
                () -> compareParameters(countFiles.toString(), actualCount, "count files")
        );

        exportsFilesPage.getExportsCollection().
                getElement(Label.class, 0, By.xpath("./div/div/p")).waitForClickableElement();
    }

    @Step("Проверяем отмену поиска экспорта")
    public void cancelSearchExports(String archiveName) {
        exportsFilesPage.checkLoadPage();
        exportsFilesPage.getDateField().click();
        exportsFilesPage.getDateClearButton().click();

        String actualName = exportsFilesPage.getExportsCollection().waitForElements().
                getElement(Label.class, 0, By.xpath("./div/p[3]")).getText();
        compareParameters(archiveName, actualName, "archive name");
    }

    //todo требуется настройка moon для скачивания файлов
    @Step("Проверяем скаченный медиафайл")
    public void checkImage(Image image, String path) {
        checkDownloadFile(path, image.format(), 10, false);
        byte[] resultBytes = covertFileToBytes(image.filename(), path);
        equalsParameters(image.bytes(), resultBytes, "массивы байтов изображений");
    }

    //todo требуется настройка moon для скачивания файлов
    @Step("Проверяем скаченные медиафайлы")
    public void checkImage(List<Image> images, String path) {
        checkDownloadFile(path, ".zip", 10, false);

        String extractFolder = extractZip("новый архив", path);
        images.forEach(image -> {
            byte[] resultBytes = covertFileToBytes(image.filename(), extractFolder);
            equalsParameters(image.bytes(), resultBytes, "массивы байтов изображений");
        });
    }

    @Step("Переходи на страницу валидации")
    public void goToExportValidation() {
        mediaFilesPage.checkLoadPage();
        //mediaFilesPage.getValidationErrorsButton().click();

        validationErrorsPage.checkLoadPage();
        //compareParameters("Ошибки валидации", validationErrorsPage.getTitleLabel().getText(), "заголовок");

        validationErrorsPage.getValidationTable().getRowsCollection().clickTo(1);
        //validationErrorsPage.getDownloadButton().waitForElement();
        //validationErrorsPage.getChooseAllButton().waitForElement();
        //validationErrorsPage.getCleanButton().waitForElement();
    }

    @Step("Скачиваем валидацию файла")
    public void exportValidationFile() {
        goToExportValidation();

        //validationErrorsPage.getDownloadButton().click();
    }

    @Step("Скачиваем валидацию файла в таблице")
    public void exportValidationInTableFile() {
        goToExportValidation();

        validationErrorsPage.getValidationTable().
                getElementInCell(Button.class, 1, 5, TABLE_DOWNLOAD_BUTTON).click();
    }

    @Step("Скачиваем валидацию файла")
    public void exportValidationFiles() {
        goToExportValidation();

        //validationErrorsPage.getChooseAllButton().click();
        List<Element> rows = validationErrorsPage.getValidationTable().getRowsCollection().getElementList(Element.class);
        rows.remove(0);
        rows.forEach(row -> compareRegexParameters(".+selected", row.getAttributeText("class"), "выбор строки"));
        //validationErrorsPage.getDownloadButton();
    }

    public void cancelExportValidation() {
        goToExportValidation();

//        validationErrorsPage.getCleanButton().click();
//        validationErrorsPage.getDownloadButton().waitForInvisibilityOfElement();
//        validationErrorsPage.getCleanButton().waitForInvisibilityOfElement();
//        validationErrorsPage.getChooseAllButton().waitForInvisibilityOfElement();
    }

    @Data
    @Accessors(chain = true, fluent = true)
    public static class Image {
        private String filename;
        private String format;
        private byte[] bytes;

        public Image(String filename, byte[] bytes) {
            this.filename = filename;
            this.bytes = bytes;
        }

        public String format() {
            return format == null ? format = filename.split("\\.", 2)[1] : format;
        }
    }
}
