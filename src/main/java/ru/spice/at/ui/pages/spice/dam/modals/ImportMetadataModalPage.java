package ru.spice.at.ui.pages.spice.dam.modals;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import ru.spice.at.ui.annotations.Mandatory;
import ru.spice.at.ui.annotations.Name;
import ru.spice.at.ui.controls.Button;
import ru.spice.at.ui.controls.ComboBox;
import ru.spice.at.ui.controls.Field;
import ru.spice.at.ui.controls.Label;
import ru.spice.at.ui.pages.BasePage;

import java.util.List;

/**
 * Сохранение файлов (добавление метаданных для импорта): модальное окно
 */
@Deprecated
@Name("Сохранение файлов")
public class ImportMetadataModalPage extends BasePage {

    public ImportMetadataModalPage(WebDriver webDriver) {
        super(webDriver);
    }

    @Mandatory
    @FindBy(xpath = "//form//h2/h2")
    private WebElement title;

    @FindBy(xpath = "//form//input[@name='external_task_id']")
    private WebElement externalTaskId;

    @FindBy(xpath = "//form//label[text()='Категория']/..//div[contains(@class, 'MuiInputBase-root')]")
    private WebElement category;

    @FindBy(xpath = "//div[contains(@class, 'MuiBox')]//ul/li")
    private List<WebElement> categoriesItems;

    @FindBy(xpath = "//form//label[text()='Источник']/..//div[contains(@class, 'MuiInputBase-root')]")
    private WebElement source;

    @FindBy(xpath = "//form//label[text()='Теги']/..//div[contains(@class, 'MuiInputBase-root')]/input")
    private WebElement tags;

    @FindBy(xpath = "//form//label[text()='Теги']/..//p")
    private WebElement tagsMessage;

    @FindBy(xpath = "//form//label[text()='СТМ']/..//div[contains(@class, 'MuiInputBase-root')]")
    private WebElement isOwnTrademark;

    @FindBy(xpath = "//form//label[text()='Исх.изобр.']/..//div[contains(@class, 'MuiInputBase-root')]")
    private WebElement isRawImage;

    @FindBy(xpath = "//form//label[text()='Автор. права']/..//div[contains(@class, 'MuiInputBase-root')]")
    private WebElement isCopyright;

    @FindBy(xpath = "//div[contains(@class, 'MuiPaper')]//ul/li")
    private List<WebElement> parametersItems;

    @FindBy(xpath = "//form//button[text()='Пропустить']")
    private WebElement skip;

    @FindBy(xpath = "//form//button[text()='Сохранить']")
    private WebElement save;

    @FindBy(xpath = "//form//button[@type='reset']")
    private WebElement close;

    public Label getTitleLabel() {
        return new Label(title, "Заголовок", getWebDriver());
    }

    public ComboBox getCategoryComboBox() {
        return new ComboBox(category, categoriesItems, "Категория", getWebDriver());
    }

    public ComboBox getSourceComboBox() {
        return new ComboBox(source, parametersItems, "Источник", getWebDriver());
    }

    public Field getExternalTaskIdField() {
        return new Field(externalTaskId, "Задача PIMS", getWebDriver());
    }

    public Field getTagsField() {
        return new Field(tags, "Теги", getWebDriver());
    }

    public Label getTagsMessageLabel() {
        return new Label(tagsMessage, "Теги - сообщение", getWebDriver());
    }

    public ComboBox getIsOwnTrademarkComboBox() {
        return new ComboBox(isOwnTrademark, parametersItems, "СТМ", getWebDriver());
    }

    public ComboBox getIsRawImageComboBox() {
        return new ComboBox(isRawImage, parametersItems, "Исходное изображение", getWebDriver());
    }

    public ComboBox getIsCopyrightComboBox() {
        return new ComboBox(isCopyright, parametersItems, "Авторские права", getWebDriver());
    }

    public Button getSkipButton() {
        return new Button(skip, "Пропустить", getWebDriver());
    }

    public Button getSaveButton() {
        return new Button(save, "Сохранить", getWebDriver());
    }

    public Button getCloseButton() {
        return new Button(close, "Закрыть", getWebDriver());
    }
}
