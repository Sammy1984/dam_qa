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
 * Все фильтры: модальное окно
 */
@Name("Все фильтры")
public class MoreFiltersModalPage extends BasePage {

    public MoreFiltersModalPage(WebDriver webDriver) {
        super(webDriver);
    }

    @Mandatory
    @FindBy(xpath = "//div[@role='dialog']/form//p[text()='Все фильтры']")
    private WebElement title;

    @FindBy(xpath = "//input[@placeholder='Качество']")
    private WebElement quality;

    @FindBy(xpath = "//input[@placeholder='Статус']")
    private WebElement status;

    @FindBy(xpath = "//input[@placeholder='Источник']")
    private WebElement source;

    @FindBy(xpath = "//div[@role='dialog']//p[text()='Исполнитель']/..//button")
    private WebElement assignee;

    @FindBy(xpath = "//div[@role='dialog']//p[text()='Загрузчик']/..//button")
    private WebElement createdBy;

    @FindBy(xpath = "//div[@id='simple-popover']//label[contains(@class, 'MuiFormControlLabel')]")
    private List<WebElement> selectFiltrationItems;

    @FindBy(xpath = "//input[@placeholder='Категории']")
    private WebElement categories;

    @FindBy(xpath = "//div[contains(@class, 'MuiBox')]//ul/li")
    private List<WebElement> categoriesItems;

    @FindBy(xpath = "//input[@placeholder='Главное']")
    private WebElement isMainImage;

    @FindBy(xpath = "//input[@placeholder='CTM']")
    private WebElement isOwnTrademark;

    @FindBy(xpath = "//input[@placeholder='Автор. права']")
    private WebElement isCopyright;

    @FindBy(xpath = "//input[@placeholder='Исх. изобр.']")
    private WebElement isRawImage;

    @FindBy(xpath = "//div[@role='dialog']//p[text()='Ретейлер']/..//button")
    private WebElement masterSeller;

    @FindBy(xpath = "//div[contains(@class, 'FormControl-root')]//li")
    private List<WebElement> selectFiltrationBoolItems;

    @FindBy(xpath = "//p[text()='SKU']/..//input[@type='text']")
    private WebElement skuField;

    @FindBy(xpath = "//p[text()='SKU']/..//input[@type='checkbox']/..")
    private WebElement skuCheckbox;

    @FindBy(xpath = "//p[text()='Приоритет']/..//input[@type='text']")
    private WebElement priorityField;

    @FindBy(xpath = "//p[text()='Приоритет']/..//input[@type='checkbox']/..")
    private WebElement priorityCheckbox;

    @FindBy(xpath = "//p[text()='Оффер']/..//input[@type='text']")
    private WebElement offerField;

    @FindBy(xpath = "//p[text()='Оффер']/..//input[@type='checkbox']")
    private WebElement offerCheckbox;

    @FindBy(xpath = "//p[text()='Задача PIMS']/..//input[@type='text']")
    private WebElement pimsTaskField;

    @FindBy(xpath = "//p[text()='Задача PIMS']/..//input[@type='checkbox']/..")
    private WebElement pimsTaskCheckbox;

    @FindBy(xpath = "//div[@role='dialog']//button[text()='Отменить']")
    private WebElement cancel;

    @FindBy(xpath = "//div[@role='dialog']//button[@type='submit']")
    private WebElement save;

    public Label getTitleLabel() {
        return new Label(title, "Заголовок", getWebDriver());
    }

    public ComboBox getQualityComboBox() {
        return new ComboBox(quality, selectFiltrationItems, "Качество", getWebDriver());
    }

    public ComboBox getStatusComboBox() {
        return new ComboBox(status, selectFiltrationItems, "Статус", getWebDriver());
    }

    public ComboBox getSourceComboBox() {
        return new ComboBox(source, selectFiltrationItems, "Источник", getWebDriver());
    }

    public ComboBox getAssigneeComboBox() {
        return new ComboBox(assignee, selectFiltrationItems, "Исполнитель", getWebDriver());
    }

    public ComboBox getCreatedByComboBox() {
        return new ComboBox(createdBy, selectFiltrationItems, "Загрузчик", getWebDriver());
    }

    public ComboBox getCategoryComboBox() {
        return new ComboBox(categories, categoriesItems, "Категория", getWebDriver());
    }

    public ComboBox getIsMainImageComboBox() {
        return new ComboBox(isMainImage, selectFiltrationBoolItems, "Главное", getWebDriver());
    }

    public ComboBox getIsOwnTrademarkComboBox() {
        return new ComboBox(isOwnTrademark, selectFiltrationBoolItems, "CTM", getWebDriver());
    }

    public ComboBox getIsCopyrightComboBox() {
        return new ComboBox(isCopyright, selectFiltrationBoolItems, "Автор. права", getWebDriver());
    }

    public ComboBox getIsRawImageComboBox() {
        return new ComboBox(isRawImage, selectFiltrationBoolItems, "Исх. изобр.", getWebDriver());
    }

    public ComboBox getMasterSellerComboBox() {
        return new ComboBox(masterSeller, selectFiltrationItems, "Ретейлер", getWebDriver());
    }

    public Field getSkuField() {
        return new Field(skuField, "SKU - поле", getWebDriver());
    }

    public CheckBox getSkuCheckbox() {
        return new CheckBox(skuCheckbox, "SKU - чекбокс (пустое значение)", getWebDriver());
    }

    public Field getPriorityField() {
        return new Field(priorityField, "Приоритет - поле", getWebDriver());
    }

    public CheckBox getPriorityCheckbox() {
        return new CheckBox(priorityCheckbox, "Приоритет - чекбокс (пустое значение)", getWebDriver());
    }

    public Field getOfferField() {
        return new Field(offerField, "Оффер - поле", getWebDriver());
    }

    public CheckBox getOfferCheckbox() {
        return new CheckBox(offerCheckbox, "Оффер - чекбокс (пустое значение)", getWebDriver());
    }

    public Field getPimsTaskField() {
        return new Field(pimsTaskField, "Задача PIMS - поле", getWebDriver());
    }

    public CheckBox getPimsTaskCheckbox() {
        return new CheckBox(pimsTaskCheckbox, "Задача PIMS - чекбокс (пустое значение)", getWebDriver());
    }

    public Button getCancelButton() {
        return new Button(cancel, "Отменить", getWebDriver());
    }

    public Button getSaveButton() {
        return new Button(save, "Применить", getWebDriver());
    }
}
