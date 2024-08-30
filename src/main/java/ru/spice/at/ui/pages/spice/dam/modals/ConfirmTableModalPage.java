package ru.spice.at.ui.pages.spice.dam.modals;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import ru.spice.at.ui.annotations.Mandatory;
import ru.spice.at.ui.annotations.Name;
import ru.spice.at.ui.controls.Button;
import ru.spice.at.ui.controls.CheckBox;
import ru.spice.at.ui.controls.Label;
import ru.spice.at.ui.pages.BasePage;

/**
 * Информационное сообщение: модальное окно
 */
@Name("Информационное сообщение с таблицей")
public class ConfirmTableModalPage extends BasePage {

    public ConfirmTableModalPage(WebDriver webDriver) {
        super(webDriver);
    }

    @Mandatory
    @FindBy(xpath = "//div[contains(@class, 'modal-content')]//div[contains(@class, 'title')]//span[contains(@class, 'typography')]/../div")
    private WebElement confirmTitle;

    @FindBy(xpath = "//div[contains(@class, 'modal-content')]//div[contains(@class, 'title')]//span[contains(@class, 'typography')]/../div/following-sibling::span")
    private WebElement confirmContent;

    //todo при изменении верстки возможно добавить элемент таблицы
    @FindBy(xpath = "//div[contains(@class, 'modal-content')]//thead/tr/th[contains(@class, 'selection-column')]")
    private WebElement confirmCheckBox;

    @FindBy(xpath = "//div[contains(@class, 'modal-content')]//span[text()='Подтвердить']/parent::button")
    private WebElement confirmButton;

    @FindBy(xpath = "//div[contains(@class, 'modal-content')]//span[text()='Отмена']/parent::button")
    private WebElement cancelButton;

    public Label getConfirmTitleLabel() {
        return new Label(confirmTitle, "Заголовок", getWebDriver());
    }

    public Label getConfirmContentLabel() {
        return new Label(confirmContent, "Описание", getWebDriver());
    }

    public CheckBox getConfirmCheckBox() {
        return new CheckBox(confirmCheckBox, "Чекбокс - согласие", getWebDriver());
    }

    public Button getConfirmButton() {
        return new Button(confirmButton, "Подтвердить", getWebDriver());
    }

    public Button getCancelButton() {
        return new Button(cancelButton, "Отмена", getWebDriver());
    }
}
