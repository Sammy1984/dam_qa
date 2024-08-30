package ru.spice.at.ui.pages.spice.pims.modals;

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
 * Информационное модальное окно
 */
@Name("Информационное модальное окно")
public class InfoPimsModalPage extends BasePage {

    public InfoPimsModalPage(WebDriver webDriver) {
        super(webDriver);
    }

    @Mandatory
    @FindBy(xpath = "//div[@role='dialog']//h2")
    private WebElement title;

    @FindBy(xpath = "//div[@role='dialog']//button[@type='submit']")
    private WebElement submit;

    @FindBy(xpath = "//div[@role='dialog']//button[text()='Да']")
    private WebElement yes;

    @FindBy(xpath = "//div[@role='dialog']//button[text()='Нет']")
    private WebElement no;

    @FindBy(xpath = "//div[@role='dialog']//input[@type='checkbox']/../..")
    private WebElement checkbox;

    public Label getTitleLabel() {
        return new Label(title, "Заголовок", getWebDriver());
    }

    public Button getSubmitButton() {
        return new Button(submit, "Подтверждение", getWebDriver());
    }

    public CheckBox getCheckbox() {
        return new CheckBox(checkbox, "Чекбокс", getWebDriver());
    }

    public Button getYesButton() {
        return new Button(yes, "Да", getWebDriver());
    }

    public Button getNoButton() {
        return new Button(no, "Нет", getWebDriver());
    }
}