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
 * Страница задачи PIMS
 */
@Name("Страница задачи PIMS")
public class TaskPimsPage extends PimsBasePage {

    public TaskPimsPage(WebDriver webDriver) {
        super(webDriver);
    }

    @Mandatory
    @FindBy(xpath = "//h1")
    private WebElement title;

    @FindBy(xpath = "//button[text()='Добавить офферы']")
    private WebElement addOffers;

    @FindBy(xpath = "//button[text()='Отвязать офферы']")
    private WebElement deleteOffers;

    @FindBy(xpath = "//div[@class='tableHeader']//span[@title='Toggle All Rows Selected']")
    private WebElement selectAllOffers;

    @FindBy(xpath = "//div[@class='tableBodyWrap']//div[contains(@class, 'tableRow')]")
    private List<WebElement> offersCollection;

    @FindBy(xpath = "//button[text()='Начать выполнение']")
    private WebElement startExecution;

    @FindBy(xpath = "//a[text()='Черновики']")
    private WebElement drafts;

    @FindBy(xpath = "//div[@data-qa='table-cell']//a")
    private WebElement draftsPassCell;

    @FindBy(xpath = "//button[text()='Отправить на проверку']")
    private WebElement submitForReview;

    @FindBy(xpath = "//button[text()='Начать проверку']")
    private WebElement startChecking;

    @FindBy(xpath = "//button[text()='Отправить на приемку']")
    private WebElement sendForAcceptance;

    @FindBy(xpath = "//button[text()='Принять задачу']")
    private WebElement acceptTask;

    @FindBy(xpath = "//div[@data-name='executor']")
    private WebElement executor;

    @FindBy(xpath = "//input[@placeholder='Поиск']")
    private WebElement search;

    @FindBy(xpath = "//ul[@role='listbox']/li")
    private List<WebElement> listBox;

    @FindBy(xpath = "//div[@data-name='employee_table']//a")
    private WebElement draftEmployee;

    public Label getTitleLabel() {
        return new Label(title, "Заголовок", getWebDriver());
    }

    public Button getAddOffersButton() {
        return new Button(addOffers, "Добавить оферы", getWebDriver());
    }

    public Button getDeleteOffersButton() {
        return new Button(deleteOffers, "Отвязать оферы", getWebDriver());
    }

    public CheckBox getSelectAllOffersCheckBox() {
        return new CheckBox(selectAllOffers, "Выбрать все оферы", getWebDriver());
    }

    public Collection getOffersCollection() {
        return new Collection(offersCollection, "Список оферов", getWebDriver());
    }

    public Button getStartExecutionButton() {
        return new Button(startExecution, "Начать выполнение", getWebDriver());
    }

    public Button getDraftsButton() {
        return new Button(drafts, "Черновики", getWebDriver());
    }

    public Button getDraftsPassCellButton() {
        return new Button(draftsPassCell, "Черновики - переход к созданию товаров", getWebDriver());
    }

    public Button getSubmitForReviewButton() {
        return new Button(submitForReview, "Отправить на проверку", getWebDriver());
    }

    public Button getStartCheckingButton() {
        return new Button(startChecking, "Начать проверку", getWebDriver());
    }

    public Button getSendForAcceptanceButton() {
        return new Button(sendForAcceptance, "Отправить на приемку", getWebDriver());
    }

    public Button getAcceptTaskButton() {
        return new Button(acceptTask, "Принять задачу", getWebDriver());
    }

    public Button getExecutorButton() {
        return new Button(executor, "Исполнитель (в таблице)", getWebDriver());
    }

    public ComboBox getParameterComboBox() {
        return new ComboBox(search, listBox, "Комбобокс для выбора параметра", getWebDriver());
    }

    public Link getDraftEmployeeLink() {
        return new Link(draftEmployee, "Исполнитель (Таблица исполнителя) в черновиках", getWebDriver());
    }
}