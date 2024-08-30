package ru.spice.at.ui.pages.spice.pims;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import ru.spice.at.ui.annotations.Mandatory;
import ru.spice.at.ui.annotations.Name;
import ru.spice.at.ui.controls.Button;
import ru.spice.at.ui.controls.Field;
import ru.spice.at.ui.pages.BasePage;

/**
 * Страница авторизации PIMS
 */
@Name("Страница авторизации PIMS")
public class AuthorizationPimsPage extends BasePage {

    public AuthorizationPimsPage(WebDriver webDriver) {
        super(webDriver);
    }

    @Mandatory
    @FindBy(xpath = "//input[@name='username']")
    private WebElement username;

    @Mandatory
    @FindBy(xpath = "//input[@name='password']")
    private WebElement password;

    @Mandatory
    @FindBy(xpath = "//button[@name='submit']")
    private WebElement submit;

    public Field getUsernameField() {
        return new Field(username, "Логин", getWebDriver());
    }

    public Field getPasswordField() {
        return new Field(password, "Пароль", getWebDriver());
    }

    public Button getSubmitButton() {
        return new Button(submit, "Войти", getWebDriver());
    }
}