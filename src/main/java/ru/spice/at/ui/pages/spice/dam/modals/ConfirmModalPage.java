package ru.spice.at.ui.pages.spice.dam.modals;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import ru.spice.at.ui.annotations.Mandatory;
import ru.spice.at.ui.annotations.Name;
import ru.spice.at.ui.controls.Button;
import ru.spice.at.ui.controls.Label;
import ru.spice.at.ui.pages.BasePage;

/**
 * Информационное сообщение: модальное окно
 */
@Name("Информационное сообщение")
public class ConfirmModalPage extends BasePage {

    public ConfirmModalPage(WebDriver webDriver) {
        super(webDriver);
    }

    @Mandatory
    @FindBy(xpath = "//form//h2/h2")
    private WebElement confirmTitle;

    @FindBy(xpath = "//form//div[contains(@class, 'MuiAlert-message')]/span")
    private WebElement confirmContent;

    @FindBy(xpath = "//form//button[text()='Закрыть']")
    private WebElement closeButton;

    @FindBy(xpath = "//form//button[text()='Подтвердить']")
    private WebElement confirmButton;

    public Label getConfirmTitleLabel() {
        return new Label(confirmTitle, "Заголовок", getWebDriver());
    }

    public Label getConfirmContentLabel() {
        return new Label(confirmContent, "Описание", getWebDriver());
    }

    public Button getCloseButton() {
        return new Button(closeButton, "Закрыть", getWebDriver());
    }

    public Button getConfirmButton() {
        return new Button(confirmButton, "Подтвердить", getWebDriver());
    }
}
