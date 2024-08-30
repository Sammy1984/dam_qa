package ru.spice.at.ui.pages.spice.pims;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import ru.spice.at.ui.annotations.Mandatory;
import ru.spice.at.ui.annotations.Name;
import ru.spice.at.ui.controls.Button;
import ru.spice.at.ui.controls.ComboBox;
import ru.spice.at.ui.controls.Field;
import ru.spice.at.ui.controls.Label;
import ru.spice.at.ui.pages.spice.PimsBasePage;

import java.util.List;

/**
 * Страница - создание товаров PIMS
 */
@Name("Страница - создание товаров PIMS")
public class ProductCreationPimsPage extends PimsBasePage {

    public ProductCreationPimsPage(WebDriver webDriver) {
        super(webDriver);
    }

    @Mandatory
    @FindBy(xpath = "//div/h2")
    private WebElement title;

    @Mandatory
    @FindBy(xpath = "//div[@data-field='fillPercent']//div[@title]")
    private WebElement fillPercent;

    @FindBy(xpath = "//div[@data-colindex='2']")
    private WebElement offerId;

    @FindBy(xpath = "//div[@data-colindex='16']")
    private WebElement status;

    @FindBy(xpath = "//div[@data-colindex='21']")
    private WebElement sku;

    @FindBy(xpath = "//div[@data-colindex='22']")
    private WebElement masterCategory;

    @FindBy(xpath = "//div[@data-colindex='23']")
    private WebElement productName;

    @FindBy(xpath = "//div[@data-colindex='35']")
    private WebElement productUnit;

    @FindBy(xpath = "//div[@data-colindex='36']")
    private WebElement netWeight;

    @FindBy(xpath = "//div[@data-colindex='38']")
    private WebElement grossWeight;

    @FindBy(xpath = "//div[@data-colindex='44']")
    private WebElement gender;

    @FindBy(xpath = "//div[@data-colindex='62']")
    private WebElement type;

    @FindBy(xpath = "//div[@data-colindex='68']")
    private WebElement weight;

    @FindBy(xpath = "//div[@role='presentation' or @role='tooltip']//input[@placeholder='Поиск']")
    private WebElement parameterSearch;

    @FindBy(xpath = "//div[@role='presentation' or @role='tooltip']//ul//li")
    private List<WebElement> parameterList;

    @FindBy(xpath = "//div[@role='presentation' or @role='tooltip']//input[contains(@placeholder, 'Введите')]")
    private WebElement parameterField;

    @FindBy(xpath = "//button[contains(text(), 'Обновить')]")
    private WebElement update;

    @FindBy(xpath = "//li[contains(text(), 'Категорийные характеристики')]")
    private WebElement categoryCharacteristics;

    @FindBy(xpath = "//div[@role='dialog']//button[contains(text(), 'Обновить')]")
    private WebElement categoryCharacteristicsUpdate;

    @FindBy(xpath = "//button[contains(text(), 'Медиа')]")
    private WebElement media;

    @FindBy(xpath = "//input[@type='file' and @hidden]")
    private WebElement hiddenAdd;

    @FindBy(xpath = "//button[text()='Сохранить черновик']")
    private WebElement saveDraft;

    @FindBy(xpath = "//button[text()='Импорт в PIMS']")
    private WebElement importInPims;

    @FindBy(xpath = "//button[text()='Сохранить']")
    private WebElement save;

    @FindBy(xpath = "//input[@placeholder='Содержание выбранной ячейки']")
    private WebElement mainParameterField;

    @FindBy(xpath = "//div[text()='Медиа загружено']")
    private WebElement mediaUpload;

    public Label getTitleLabel() {
        return new Label(title, "Заголовок", getWebDriver());
    }

    public Label getFillPercentLabel() {
        return new Label(fillPercent, "Готовность", getWebDriver());
    }

    public Button getStatusButton() {
        return new Button(status, "Статус разбора оффера", getWebDriver());
    }

    public Button getOfferIdButton() {
        return new Button(offerId, "Id оффера", getWebDriver());
    }

    public Button getSkuButton() {
        return new Button(sku, "SKU", getWebDriver());
    }

    public Button getMasterCategoryButton() {
        return new Button(masterCategory, "Мастер категория", getWebDriver());
    }

    public Button getProductNameButton() {
        return new Button(productName, "Название продукта", getWebDriver());
    }

    public Button getProductUnitButton() {
        return new Button(productUnit, "Единица измерения товара", getWebDriver());
    }

    public Button getNetWeightButton() {
        return new Button(netWeight, "Вес нетто", getWebDriver());
    }

    public Button getGrossWeightButton() {
        return new Button(grossWeight, "Вес брутто", getWebDriver());
    }

    public Button getGenderButton() {
        return new Button(gender, "Пол (Знач 1)", getWebDriver());
    }

    public Button getTypeButton() {
        return new Button(type, "Тип (Знач 7)", getWebDriver());
    }

    public Button getWeightButton() {
        return new Button(weight, "Вес (Знач 9)", getWebDriver());
    }

    public ComboBox getParameterComboBox() {
        return new ComboBox(parameterSearch, parameterList, "Элемент выбора параметра", getWebDriver());
    }

    public Field getParameterField() {
        return new Field(parameterField, "Элемент поля ввода параметра", getWebDriver());
    }

    public Button getUpdateButton() {
        return new Button(update, "Обновить", getWebDriver());
    }

    public Button getCategoryCharacteristicsButton() {
        return new Button(categoryCharacteristics, "Категорийные характеристики", getWebDriver());
    }

    public Button getCategoryCharacteristicsUpdateButton() {
        return new Button(categoryCharacteristicsUpdate, "Категорийные характеристики - обновить", getWebDriver());
    }

    public Button getMediaButton() {
        return new Button(media, "Медиа", getWebDriver());
    }

    public Field getHiddenAddField() {
        return new Field(hiddenAdd, "Добавить (скрытое поле)", getWebDriver());
    }

    public Button getSaveDraftButton() {
        return new Button(saveDraft, "Сохранить черновик", getWebDriver());
    }

    public Button getImportInPimsButton() {
        return new Button(importInPims, "Импорт в PIMS", getWebDriver());
    }

    public Button getSaveButton() {
        return new Button(save, "Сохранить", getWebDriver());
    }

    public Field getMainParameterField() {
        return new Field(mainParameterField, "Элемент поля ввода параметра - поле сверху", getWebDriver());
    }

    public Label getMediaUploadLabel() {
        return new Label(mediaUpload, "Медиа загружено", getWebDriver());
    }
}
