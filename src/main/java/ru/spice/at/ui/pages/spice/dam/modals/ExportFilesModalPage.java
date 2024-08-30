package ru.spice.at.ui.pages.spice.dam.modals;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import ru.spice.at.ui.annotations.Mandatory;
import ru.spice.at.ui.annotations.Name;
import ru.spice.at.ui.controls.*;
import ru.spice.at.ui.pages.BasePage;

import java.util.List;

/**
 * Экспорт файлов: модальное окно
 */
@Name("Экспорт файлов")
public class ExportFilesModalPage extends BasePage {
    public ExportFilesModalPage(WebDriver webDriver) {
        super(webDriver);
    }

    @Mandatory
    @FindBy(xpath = "//div[contains(@class, 'MuiPaper') and @role='dialog']//h2/p")
    private WebElement title;

    @Mandatory
    @FindBy(xpath = "//div[contains(@class, 'MuiPaper') and @role='dialog']//button[@aria-label='close']")
    private WebElement close;

    @FindBy(xpath = "//div[@id='mui-component-select-format_id']")
    private WebElement formatField;

    @FindBy(xpath = "//div[contains(@class, 'MuiPaper-root')]//li[@role='option']")
    private List<WebElement> formatList;

    @FindBy(xpath = "//div[contains(@class, 'MuiPaper') and @role='dialog']//input[@name='filename']")
    private WebElement archiveNameField;

    @FindBy(xpath = "//div[contains(@class, 'MuiPaper') and @role='dialog']//button[@type='submit']")
    private WebElement export;

    public Label getTitleLabel() {
        return new Label(title, "Заголовок", getWebDriver());
    }

    public Button getCloseButton() {
        return new Button(close, "Закрыть", getWebDriver());
    }

    public ComboBox getFormatComboBox() {
        return new ComboBox(formatField,  formatList, "Формат", getWebDriver());
    }

    public Field getArchiveNameField() {
        return new Field(archiveNameField, "Название архива", getWebDriver());
    }

    public Button getExportButton() {
        return new Button(export, "Экспортировать", getWebDriver());
    }
}