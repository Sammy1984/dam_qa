package ru.spice.at.ui.pages.spice.dam.blocks;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import ru.spice.at.ui.annotations.Name;
import ru.spice.at.ui.controls.*;
import ru.spice.at.ui.pages.BasePage;

import java.util.List;

/**
 * Блок с параметрами метадаты
 */
@Name("Блок с параметрами метадаты")
public class MetadataBlock extends BasePage {
    public MetadataBlock(WebDriver webDriver) {
        super(webDriver);
    }

    @FindBy(xpath = "//button[text()='Скачать']")
    private WebElement download;

    @FindBy(xpath = "//button[contains(text(),'Экспортировать')]")
    private WebElement export;

    @FindBy(xpath = "//button[text()='Правка метаданных']")
    private WebElement edit;

    @FindBy(xpath = "//button[text()='Сохранить']")
    private WebElement save;

    @FindBy(xpath = "//button[text()='Отменить']")
    private WebElement cancel;

    @FindBy(xpath = "//*[@data-testid='MoreVertIcon']/parent::button")
    private WebElement moreAction;

    @FindBy(xpath = "//textarea[@name='filename']")
    private WebElement filename;

    @FindBy(xpath = "//textarea[@name='filename']/../../../p")
    private WebElement filenameMessage;

    @FindBy(xpath = "//input[@name='priority']")
    private WebElement priority;

    @FindBy(xpath = "//input[@name='priority']/../../p")
    private WebElement priorityMessage;

    @FindBy(xpath = "//input[@name='quality_id']/../div")
    private WebElement quality;

    @FindBy(xpath = "//input[@name='is_raw_image']/../div")
    private WebElement isRawImage;

    @FindBy(xpath = "//input[@name='is_own_trademark']/../div")
    private WebElement isOwnTrademark;

    @FindBy(xpath = "//input[@name='is_main_image']/../div")
    private WebElement isMainImage;

    @FindBy(xpath = "//textarea[@name='description']")
    private WebElement description;

    @FindBy(xpath = "//textarea[@name='description']/../../../p")
    private WebElement descriptionMessage;

    @FindBy(xpath = "//input[@name='keywords']")
    private WebElement tagsField;

    @FindBy(xpath = "//input[@name='keywords']/..//span[contains(@class, 'label')]")
    private List<WebElement> tagsCollection;

    @FindBy(xpath = "//input[@name='keywords']/../../../p")
    private WebElement tagsMessage;

    @FindBy(xpath = "//textarea[@name='sku']")
    private WebElement sku;

    @FindBy(xpath = "//textarea[@name='sku']/../../../p")
    private WebElement skuMessage;

    @FindBy(xpath = "//input[@name='source_id']/../div")
    private WebElement source;

    @FindBy(xpath = "//p[text()='Исполнитель']/../following-sibling::div[1]//button")
    private WebElement assignee;

    @FindBy(xpath = "//p[text()='Исполнитель']/../following-sibling::div[1]//p[@name='assignee_id']")
    private WebElement assigneeName;

    @FindBy(xpath = "//div[contains(@class, 'MuiGrid-item')]/p[text()='Категории']/../following-sibling::div[1]/div//input")
    private WebElement category;

    @FindBy(xpath = "//div[contains(@class, 'MuiBox')]//ul/li")
    private List<WebElement> categoryItems;

    @FindBy(xpath = "//input[@name='status_id']/../div")
    private WebElement status;

    @FindBy(xpath = "//input[@name='status_id']/../../p")
    private WebElement statusMessage;

    @FindBy(xpath = "//textarea[@name='received']")
    private WebElement received;

    @FindBy(xpath = "//textarea[@name='received']/../../../p")
    private WebElement receivedMessage;

    @FindBy(xpath = "//input[@name='is_copyright']/../div")
    private WebElement isCopyright;

    @FindBy(xpath = "//input[@name='external_task_id']")
    private WebElement externalTaskId;

    @FindBy(xpath = "//ul[@role='listbox']/li")
    private List<WebElement> listBox;

    @FindBy(xpath = "//div[@role='grid']//label")
    private List<WebElement> listGrid;

    public Button getDownloadButton() {
        return new Button(download, "Скачать", getWebDriver());
    }

    public Button getExportButton() {
        return new Button(export, "Загрузить", getWebDriver());
    }

    public Button getEditButton() {
        return new Button(edit, "Правка метаданных", getWebDriver());
    }

    public Button getSaveButton() {
        return new Button(save, "Сохранить", getWebDriver());
    }

    public Button getCancelButton() {
        return new Button(cancel, "Отменить", getWebDriver());
    }

    public Button getMoreActionButton() {
        return new Button(moreAction, "Больше действий (три точки)", getWebDriver());
    }

    public Field getFilenameField() {
        return new Field(filename, "Название файла", getWebDriver());
    }

    public Label getFilenameMessageLabel() {
        return new Label(filenameMessage, "Сообщение на поле - Название файла", getWebDriver());
    }

    public Field getPriorityField() {
        return new Field(priority, "Приоритет", getWebDriver());
    }

    public Label getPriorityMessageLabel() {
        return new Label(priorityMessage, "Сообщение на поле - Приоритет", getWebDriver());
    }

    public ComboBox getQualityComboBox() {
        return new ComboBox(quality, listBox, "Качество", getWebDriver());
    }

    public ComboBox getIsRawImageComboBox() {
        return new ComboBox(isRawImage, listBox, "Исходное изображение", getWebDriver());
    }

    public ComboBox getIsOwnTrademarkComboBox() {
        return new ComboBox(isOwnTrademark, listBox, "СТМ", getWebDriver());
    }

    public ComboBox getIsMainImageComboBox() {
        return new ComboBox(isMainImage, listBox, "Главное изображение", getWebDriver());
    }

    public Field getDescriptionField() {
        return new Field(description, "Описание", getWebDriver());
    }

    public Label getDescriptionMessageLabel() {
        return new Label(descriptionMessage, "Сообщение на поле - Описание", getWebDriver());
    }

    public Field getTagsField() {
        return new Field(tagsField, "Теги - поле ввода", getWebDriver());
    }

    public Collection getTagsCollection() {
        return new Collection(tagsCollection, "Теги - коллекция", getWebDriver());
    }

    public Label getTagsMessageLabel() {
        return new Label(tagsMessage, "Сообщение на поле - Теги", getWebDriver());
    }

    public Field getSkuField() {
        return new Field(sku, "SKU", getWebDriver());
    }

    public Label getSkuMessageLabel() {
        return new Label(skuMessage, "Сообщение на поле - SKU", getWebDriver());
    }

    public ComboBox getSourceComboBox() {
        return new ComboBox(source, listBox, "Источник", getWebDriver());
    }

    public ComboBox getAssigneeComboBox() {
        return new ComboBox(assignee, listGrid, "Исполнитель", getWebDriver());
    }

    public Label getAssigneeLabel() {
        return new Label(assigneeName, "Исполнитель", getWebDriver());
    }

    public ComboBox getCategoryComboBox() {
        return new ComboBox(category, categoryItems, "Категории", getWebDriver());
    }

    public ComboBox getStatusComboBox() {
        return new ComboBox(status, listBox, "Статус", getWebDriver());
    }

    public Label getStatusMessageLabel() {
        return new Label(statusMessage, "Сообщение на поле - Статус", getWebDriver());
    }

    public Field getReceivedField() {
        return new Field(received, "Получено от", getWebDriver());
    }

    public Label getReceivedMessageLabel() {
        return new Label(receivedMessage, "Сообщение на поле - Получено от", getWebDriver());
    }

    public ComboBox getIsCopyrightComboBox() {
        return new ComboBox(isCopyright, listBox, "Автор. права", getWebDriver());
    }

    public Field getExternalTaskIdField() {
        return new Field(externalTaskId, "Задача PIMS", getWebDriver());
    }
}
