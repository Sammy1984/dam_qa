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
 * Загрузить медиа: модальное окно
 */
@Name("Загрузить медиа")
public class ImportMediaModalPage extends BasePage {

    public ImportMediaModalPage(WebDriver webDriver) {
        super(webDriver);
    }

    @Mandatory
    @FindBy(xpath = "//*[text()='Импорт изображений']")
    private WebElement title;

    @FindBy(xpath = "//div[@title='Добавление файлов']//input[@multiple]")
    private WebElement importFileInput;

    @FindBy(xpath = "//div[@title='Добавление файлов']//*[contains(@class, 'MuiCardMedia')]/../..")
    private List<WebElement> mediaFiles;

    @FindBy(xpath = "//*[contains(text(), 'Ещё')]/parent::button")
    private WebElement importMoreMedia;

    @FindBy(xpath = "//*[text()='Импорт изображений']/../button")
    private WebElement cancelImport;

    @FindBy(xpath = "//button[text()='Импортировать']")
    private WebElement importMedia;

    @Deprecated
    @FindBy(xpath = "//div[@title='Добавление файлов']//div[contains(@class, 'MuiAlert-message')]")
    private WebElement message;

    @FindBy(xpath = "//button[contains(text(), 'С совпадением имени')]")
    private WebElement repeatedMediaTab;

    @FindBy(xpath = "//button[contains(text(), 'Все файлы')]")
    private WebElement allMediaTab;

    @FindBy(xpath = "//*[contains(text(), 'Нормализация')]/../span[contains(@class, 'Button')]")
    private WebElement normalize;

    @FindBy(xpath = "//*[contains(text(), '5% поля')]/../span[contains(@class, 'Button')]")
    private WebElement center;

    @FindBy(xpath = "//*[contains(text(), 'Обтравка')]/../span[contains(@class, 'Button')]")
    private WebElement clipping;

    @FindBy(xpath = "//*[contains(text(), 'Заполнение SKU')]/../span[contains(@class, 'Button')]")
    private WebElement takeSku;



    @FindBy(xpath = "//form//input[@name='external_task_id']")
    private WebElement externalTaskId;

    @FindBy(xpath = "//form//label[text()='Категория']/..//div[contains(@class, 'MuiInputBase-root')]")
    private WebElement category;

    @FindBy(xpath = "//div[contains(@class, 'MuiBox')]//ul/li")
    private List<WebElement> categoriesItems;

    @FindBy(xpath = "//form//label[text()='Качество']/..//div[contains(@class, 'MuiInputBase-root')]")
    private WebElement quality;

    @FindBy(xpath = "//form//label[text()='Источник']/..//div[contains(@class, 'MuiInputBase-root')]")
    private WebElement source;

    @FindBy(xpath = "//form//label[text()='Теги']/..//div[contains(@class, 'MuiInputBase-root')]/input")
    private WebElement tags;

    @FindBy(xpath = "//form//label[text()='Теги']/..//p")
    private WebElement tagsMessage;

    @FindBy(xpath = "//form//label[text()='СТМ']/..//div[contains(@class, 'MuiInputBase-root')]")
    private WebElement isOwnTrademark;

    @FindBy(xpath = "//div[contains(@class, 'MuiPaper')]//ul/li")
    private List<WebElement> parametersItems;


    public Field getImportFileInput() {
        return new Field(importFileInput, "Загрузка медиа - скрытое поле", getWebDriver());
    }

    public Collection getMediaFilesCollection() {
        return new Collection(mediaFiles, "Медиафайлы", getWebDriver());
    }

    public Button getImportMoreMediaButton() {
        return new Button(importMoreMedia, "Добавить", getWebDriver());
    }

    public Button getCancelImportButton() {
        return new Button(cancelImport, "Отменить", getWebDriver());
    }

    public Button getImportMediaButton() {
        return new Button(importMedia, "Загрузить", getWebDriver());
    }

    @Deprecated
    public Label getMessageLabel() {
        return new Label(message, "Сообщение", getWebDriver());
    }

    public Button getRepeatedMediaTabButton() {
        return new Button(repeatedMediaTab, "С дублями - вкладка", getWebDriver());
    }

    public Button getAllMediaTabButton() {
        return new Button(allMediaTab, "Все файлы - вкладка", getWebDriver());
    }

    public CheckBox getNormalizeCheckBox() {
        return new CheckBox(normalize, "Нормализовать", getWebDriver());
    }

    public CheckBox getCenterCheckBox() {
        return new CheckBox(center, "5% поля (Центрировать)", getWebDriver());
    }

    public CheckBox getClippingCheckBox() {
        return new CheckBox(clipping, "Обтравка", getWebDriver());
    }

    public CheckBox getTakeSkuCheckBox() {
        return new CheckBox(takeSku, "Взять SKU и приоритет из имени", getWebDriver());
    }

    public ComboBox getCategoryComboBox() {
        return new ComboBox(category, categoriesItems, "Категория", getWebDriver());
    }

    public ComboBox getQualityComboBox() {
        return new ComboBox(quality, parametersItems, "Качество", getWebDriver());
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
}
