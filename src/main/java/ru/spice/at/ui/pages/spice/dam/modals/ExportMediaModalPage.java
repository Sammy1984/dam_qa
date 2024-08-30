package ru.spice.at.ui.pages.spice.dam.modals;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import ru.spice.at.ui.annotations.Mandatory;
import ru.spice.at.ui.annotations.Name;
import ru.spice.at.ui.controls.Button;
import ru.spice.at.ui.controls.Field;
import ru.spice.at.ui.controls.Label;
import ru.spice.at.ui.controls.Table;
import ru.spice.at.ui.pages.BasePage;

/**
 * Выбрано для экспорта: модальное окно
 */
@Name("Выбрано для экспорта")
public class ExportMediaModalPage extends BasePage {

    public ExportMediaModalPage(WebDriver webDriver) {
        super(webDriver);
    }

    @Mandatory
    @FindBy(xpath = "//div[contains(@class, 'modal-content')]//div[contains(@class, 'title')]")
    private WebElement title;

    @FindBy(xpath = "//div[contains(@class, 'modal-content')]//tbody/..")
    private WebElement exportTable;

    @FindBy(xpath = "//div[contains(@class, 'modal-content')]//div[contains(@class, 'DownloadFiles')]")
    private WebElement exportDescription;

    @FindBy(xpath = "//div[contains(@class, 'modal-content')]//div[contains(@class, 'DownloadFiles')]//input")
    private WebElement exportField;

    @FindBy(xpath = "//div[contains(@class, 'modal-content')]//span[text()='Отмена']/parent::button")
    private WebElement cancelButton;

    @FindBy(xpath = "//div[contains(@class, 'modal-content')]//span[text()='Скачать']/parent::button")
    private WebElement exportButton;

    public Label getTitleLabel() {
        return new Label(title, "Заголовок", getWebDriver());
    }

    public Table getExportTable() {
        return new Table(exportTable, "Таблица экспорта", getWebDriver());
    }

    public Label getExportDescriptionLabel() {
        return new Label(exportDescription, "Описание экспорта", getWebDriver());
    }

    public Field getExportField() {
        return new Field(exportField, "Поле - название архива для экспорта", getWebDriver());
    }

    public Button getCancelButton() {
        return new Button(cancelButton, "Отмена", getWebDriver());
    }

    public Button getExportButton() {
        return new Button(exportButton, "Скачать", getWebDriver());
    }
}
