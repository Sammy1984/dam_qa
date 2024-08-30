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
 * Редактирование (мультиредактирование): модальное окно
 */
@Name("Редактирование")
public class MultiEditModalPage extends BasePage {
    public MultiEditModalPage(WebDriver webDriver) {
        super(webDriver);
    }

    @Mandatory
    @FindBy(xpath = "//div[@role='dialog']/form//h2/p")
    private WebElement title;

    @FindBy(xpath = "//input[@name='external_task_id' and @type='number']")
    private WebElement externalTaskId;

    @FindBy(xpath = "//input[@name='external_task_id' and @type='checkbox']/..")
    private WebElement externalTaskIdCheckBox;

    @FindBy(xpath = "//div[@role='dialog']//p[text()='Ретейлер']/../following-sibling::div[1]//button")
    private WebElement retailer;

    @FindBy(xpath = "//div[contains(@class, 'MuiPaper-elevation')]//input[@placeholder='Поиск']")
    private WebElement retailerSearch;

    @FindBy(xpath = "//input[@name='master_seller_id' and @type='checkbox']/..")
    private WebElement retailerCheckBox;

    @FindBy(xpath = "//div[contains(@class, 'MuiPaper-elevation')]//div[@role='rowgroup']//label")
    private List<WebElement> retailerItems;

    @FindBy(xpath = "//input[@name='sku' and @type='text']")
    private WebElement sku;

    @FindBy(xpath = "//input[@name='sku' and @type='checkbox']/..")
    private WebElement skuCheckBox;

    @FindBy(xpath = "//input[@name='priority' and @type='number']")
    private WebElement priority;

    @FindBy(xpath = "//input[@name='priority' and @type='checkbox']/..")
    private WebElement priorityCheckBox;

    @FindBy(xpath = "//div[@id='mui-component-select-source_id']")
    private WebElement sourceButton;

    @FindBy(xpath = "//input[@name='source_id' and @type='checkbox']/..")
    private WebElement sourceCheckBox;

    @FindBy(xpath = "//div[@id='mui-component-select-status_id']")
    private WebElement statusButton;

    @FindBy(xpath = "//div[@id='mui-component-select-quality_id']")
    private WebElement qualityButton;

    @FindBy(xpath = "//p[text()='Исполнитель']/../following-sibling::div[1]//button")
    private WebElement assigneeButton;

    @FindBy(xpath = "//input[@name='assignee_id' and @type='checkbox']/..")
    private WebElement assigneeCheckBox;

    @FindBy(xpath = "//div[@name='master_category_id']/div")
    private WebElement categoryButton;

    @FindBy(xpath = "//input[@name='master_category_id' and @type='checkbox']/..")
    private WebElement categoryCheckBox;

    @FindBy(xpath = "//ul[@role='tree']/li")
    private List<WebElement> treeItems;

    @FindBy(xpath = "//input[@name='keywords' and @type='text']")
    private WebElement keywords;

    @FindBy(xpath = "//input[@name='keywords' and @type='checkbox']/..")
    private WebElement keywordsCheckBox;

    @FindBy(xpath = "//div[@id='mui-component-select-is_main_image']")
    private WebElement isMainImage;

    @FindBy(xpath = "//div[@id='mui-component-select-is_own_trademark']")
    private WebElement isOwnTrademark;

    @FindBy(xpath = "//input[@name='is_own_trademark' and @type='checkbox']/..")
    private WebElement isOwnTrademarkCheckBox;

    @FindBy(xpath = "//div[@id='mui-component-select-is_copyright']")
    private WebElement isCopyright;

    @FindBy(xpath = "//input[@name='is_copyright' and @type='checkbox']/..")
    private WebElement isCopyrightCheckBox;

    @FindBy(xpath = "//ul[@role='listbox']/li")
    private List<WebElement> listBoxItems;

    @FindBy(xpath = "//div[@role='grid']//label")
    private List<WebElement> listGrid;

    @FindBy(xpath = "//button[text()='Отменить']")
    private WebElement cancel;

    @FindBy(xpath = "//button[text()='Сохранить']")
    private WebElement submit;

    public Label getTitleLabel() {
        return new Label(title, "Заголовок", getWebDriver());
    }

    public Field getExternalTaskIdField() {
        return new Field(externalTaskId, "Задача PIMS", getWebDriver());
    }

    public CheckBox getExternalTaskIdCheckBox() {
        return new CheckBox(externalTaskIdCheckBox, "Задача PIMS - чекбокс", getWebDriver());
    }

    public Button getRetailerButton() {
        return new Button(retailer, "Ритейлер - кнопка", getWebDriver());
    }

    public ComboBox getRetailerComboBox() {
        return new ComboBox(retailerSearch, retailerItems, "Ритейлер - комбобокс", getWebDriver());
    }

    public CheckBox getRetailerCheckBox() {
        return new CheckBox(retailerCheckBox, "Ритейлер - чекбокс", getWebDriver());
    }

    public Field getSkuField() {
        return new Field(sku, "SKU", getWebDriver());
    }

    public CheckBox getSkuCheckBox() {
        return new CheckBox(skuCheckBox, "SKU - чекбокс", getWebDriver());
    }

    public Field getPriorityField() {
        return new Field(priority, "Приоритет", getWebDriver());
    }

    public CheckBox getPriorityCheckBox() {
        return new CheckBox(priorityCheckBox, "Приоритет - чекбокс", getWebDriver());
    }

    public ComboBox getSourceComboBox() {
        return new ComboBox(sourceButton, listBoxItems, "Источник", getWebDriver());
    }

    public CheckBox getSourceCheckBox() {
        return new CheckBox(sourceCheckBox, "Источник - чекбокс", getWebDriver());
    }

    public ComboBox getStatusComboBox() {
        return new ComboBox(statusButton, listBoxItems, "Статус", getWebDriver());
    }

    public ComboBox getQualityComboBox() {
        return new ComboBox(qualityButton, listBoxItems, "Качество", getWebDriver());
    }

    public ComboBox getAssigneeComboBox() {
        return new ComboBox(assigneeButton, listGrid, "Исполнитель", getWebDriver());
    }

    public CheckBox getAssigneeCheckBox() {
        return new CheckBox(assigneeCheckBox, "Исполнитель - чекбокс", getWebDriver());
    }

    public ComboBox getCategoryComboBox() {
        return new ComboBox(categoryButton, treeItems, "Категория", getWebDriver());
    }

    public CheckBox getCategoryCheckBox() {
        return new CheckBox(categoryCheckBox, "Категория", getWebDriver());
    }

    public Field getKeywordsField() {
        return new Field(keywords, "Теги", getWebDriver());
    }

    public CheckBox getKeywordsCheckBox() {
        return new CheckBox(keywordsCheckBox, "Теги - чекбокс", getWebDriver());
    }

    @Deprecated
    public ComboBox getIsMainImageComboBox() {
        return new ComboBox(isMainImage, listBoxItems, "Главное", getWebDriver());
    }

    public ComboBox getIsOwnTrademarkComboBox() {
        return new ComboBox(isOwnTrademark, listBoxItems, "СТМ", getWebDriver());
    }

    public CheckBox getIsOwnTrademarkCheckBox() {
        return new CheckBox(isOwnTrademarkCheckBox, "СТМ - чекбокс", getWebDriver());
    }

    public ComboBox getIsCopyrightComboBox() {
        return new ComboBox(isCopyright, listBoxItems, "Авторские права", getWebDriver());
    }

    public CheckBox getIsCopyrightCheckBox() {
        return new CheckBox(isCopyrightCheckBox, "Авторские права - чекбокс", getWebDriver());
    }

    public Button getCancelButton() {
        return new Button(cancel, "Отменить", getWebDriver());
    }

    public Button getSubmitButton() {
        return new Button(submit, "Сохранить", getWebDriver());
    }
}
