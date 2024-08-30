package ru.spice.at.ui.pages.spice.dam.blocks;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import ru.spice.at.ui.annotations.Name;
import ru.spice.at.ui.controls.Button;
import ru.spice.at.ui.pages.BasePage;

/**
 * Блок контекстного меню (правая кнопка мыши)
 */
@Name("Блок контекстного меню")
public class MediaContextMenuBlock extends BasePage {
    public MediaContextMenuBlock(WebDriver webDriver) {
        super(webDriver);
    }

    @FindBy(xpath = "//div[contains(@class,'MuiMenu-paper')]/ul//span[text()='Скачать']/ancestor::li")
    private WebElement download;

    @FindBy(xpath = "//div[contains(@class,'MuiMenu-paper')]/ul//span[text()='Экспортировать']/ancestor::li")
    private WebElement export;

    @FindBy(xpath = "//div[contains(@class,'MuiMenu-paper')]/ul//span[text()='Просмотр']/ancestor::li")
    private WebElement review;

    @FindBy(xpath = "//div[contains(@class,'MuiMenu-paper')]/ul//span[text()='Редактировать метаданные']/ancestor::li")
    private WebElement edit;

    @FindBy(xpath = "//div[contains(@class,'MuiMenu-paper')]/ul//span[contains(text(),'Скопировать ссылку')]/ancestor::li")
    private WebElement copyLink;

    @FindBy(xpath = "//div[contains(@class,'MuiPaper-root')]/ul//li//span[contains(text(), 'В существующем')]/ancestor::li")
    private WebElement currentFormat;

    @FindBy(xpath = "//div[contains(@class,'MuiPaper-root')]/ul//li//span[contains(text(), 'PNG')]/ancestor::li")
    private WebElement pngFormat;

    @FindBy(xpath = "//div[contains(@class,'MuiPaper-root')]/ul//li//span[contains(text(), 'JPEG')]/ancestor::li")
    private WebElement jpegFormat;

    public Button getDownloadButton() {
        return new Button(download, "Скачать", getWebDriver());
    }

    public Button getExportButton() {
        return new Button(export, "Экспортировать", getWebDriver());
    }

    public Button getReviewButton() {
        return new Button(review, "Просмотр", getWebDriver());
    }

    public Button getEditButton() {
        return new Button(edit, "Редактировать метаданные", getWebDriver());
    }

    public Button getCopyLinkButton() {
        return new Button(copyLink, "Скопировать ссылку", getWebDriver());
    }

    public Button getCurrentFormatButton() {
        return new Button(currentFormat, "В существующем формате", getWebDriver());
    }

    public Button getPngFormatButton() {
        return new Button(pngFormat, "PNG", getWebDriver());
    }

    public Button getJpegFormatButton() {
        return new Button(jpegFormat, "JPEG", getWebDriver());
    }
}