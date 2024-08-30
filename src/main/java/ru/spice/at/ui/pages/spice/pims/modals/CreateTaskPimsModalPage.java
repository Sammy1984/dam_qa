package ru.spice.at.ui.pages.spice.pims.modals;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import ru.spice.at.ui.annotations.Mandatory;
import ru.spice.at.ui.annotations.Name;
import ru.spice.at.ui.controls.Button;
import ru.spice.at.ui.controls.ComboBox;
import ru.spice.at.ui.controls.Label;
import ru.spice.at.ui.pages.BasePage;

import java.util.List;

/**
 * Модальное окно - создание задачи
 */
@Name("Модальное окно - создание задачи")
public class CreateTaskPimsModalPage extends BasePage {

    public CreateTaskPimsModalPage(WebDriver webDriver) {
        super(webDriver);
    }

    @Mandatory
    @FindBy(xpath = "//div[@role='dialog']//h2/div")
    private WebElement title;

    @FindBy(xpath = "//div[@role='dialog']//div[@data-qa='add-modal_type']")
    private WebElement addModalType;

    @FindBy(xpath = "//div[@role='dialog']//div[@data-qa='add-modal_subtype']")
    private WebElement addModalSubtype;

    @FindBy(xpath = "//ul[@role='listbox']/li")
    private List<WebElement> modalTypes;

    @FindBy(xpath = "//div[@role='dialog']//div[@data-qa='add-modal_executors']")
    private WebElement addModalExecutors;

    @FindBy(xpath = "//div[@data-qa='add-modal_executors_search']//input")
    private WebElement addModalExecutorsSearch;

    @FindBy(xpath = "//div[@data-qa='add-modal_executors_search']/..//li")
    private List<WebElement> modalExecutors;

    @FindBy(xpath = "//div[@role='dialog']//button[@data-qa='add-modal_submit']")
    private WebElement submit;

    public Label getTitleLabel() {
        return new Label(title, "Заголовок", getWebDriver());
    }

    public ComboBox getModalTypeComboBox() {
        return new ComboBox(addModalType, modalTypes, "Тип", getWebDriver());
    }

    public ComboBox getModalSubtypeComboBox() {
        return new ComboBox(addModalSubtype, modalTypes, "Подтип", getWebDriver());
    }

    public Button getAddModalExecutorsButton() {
        return new Button(addModalExecutors, "Исполнители", getWebDriver());
    }

    public ComboBox getModalExecutorsSearchComboBox() {
        return new ComboBox(addModalExecutorsSearch, modalExecutors, "Исполнители - поиск (комбобокс)", getWebDriver());
    }

    public Button getSubmitButton() {
        return new Button(submit, "Подтвердить", getWebDriver());
    }
}
