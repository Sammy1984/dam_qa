package ru.spice.at.ui.pages.spice.pims;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import ru.spice.at.ui.annotations.Mandatory;
import ru.spice.at.ui.annotations.Name;
import ru.spice.at.ui.controls.*;
import ru.spice.at.ui.pages.spice.PimsBasePage;

import java.util.List;

/**
 * Страница офферов PIMS
 */
@Name("Страница офферов PIMS")
public class OffersPimsPage extends PimsBasePage {

    public OffersPimsPage(WebDriver webDriver) {
        super(webDriver);
    }

    @Mandatory
    @FindBy(xpath = "//h5")
    private WebElement title;

    @FindBy(xpath = "//div[@class='tableHeader']//div[@class='tableCell column-id']")
    private WebElement offerIdButton;

    @FindBy(xpath = "//div[@data-qa='header-cell_id-value']/input")
    private WebElement offerIdField;

    @FindBy(xpath = "//div[@data-qa='header-cell_id-value']/button[@data-qa='header-cell_id-value_search-button']")
    private WebElement offerIdSearchButton;

    @FindBy(xpath = "//div[@class='tableHeader']//span[@title='Toggle All Rows Selected']")
    private WebElement selectAllOffers;

    @FindBy(xpath = "//button[text()='Добавить офферы']")
    private WebElement addOffers;

    @FindBy(xpath = "//div[@data-qa='table-row']")
    private List<WebElement> offersCollection;

    public Label getTitleLabel() {
        return new Label(title, "Заголовок", getWebDriver());
    }

    public Button getOfferIdButton() {
        return new Button(offerIdButton, "Оффер id - кнопка (на таблице)", getWebDriver());
    }

    public Field getOfferIdInput() {
        return new Field(offerIdField, "Оффер id - поле", getWebDriver());
    }

    public Button getOfferIdSearchButton() {
        return new Button(offerIdSearchButton, "Оффер id - кнопка поиск", getWebDriver());
    }

    public CheckBox getSelectAllOffersCheckBox() {
        return new CheckBox(selectAllOffers, "Выбрать все оферы", getWebDriver());
    }

    public Button getAddOffersButton() {
        return new Button(addOffers, "Добавить офферы", getWebDriver());
    }

    public Collection getOffersCollection() {
        return new Collection(offersCollection, "Список оферов", getWebDriver());
    }
}