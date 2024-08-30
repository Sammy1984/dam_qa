package ru.spice.at.ui.pages.spice.dam.modals;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import ru.spice.at.ui.annotations.Mandatory;
import ru.spice.at.ui.annotations.Name;
import ru.spice.at.ui.controls.Button;
import ru.spice.at.ui.controls.Collection;
import ru.spice.at.ui.controls.Label;
import ru.spice.at.ui.pages.BasePage;

import java.util.List;

/**
 * Информация о загрузке (валидация): модальное окно
 */
@Name("Информация о загрузке")
public class DownloadInfoModalPage extends BasePage {

    public DownloadInfoModalPage(WebDriver webDriver) {
        super(webDriver);
    }

    @Mandatory
    @FindBy(xpath = "//div[contains(@class, 'MuiPaper') and @role='dialog']//h5")
    private WebElement title;

    @FindBy(xpath = "//div[contains(@class, 'MuiPaper') and @role='dialog']//h2//button")
    private WebElement close;

    @FindBy(xpath = "//div[contains(@class, 'MuiPaper') and @role='dialog']//div[contains(@class, 'MuiDialogContent')]/div[2]/div")
    private List<WebElement> validationInfo;

    public Label getTitleLabel() {
        return new Label(title, "Заголовок", getWebDriver());
    }

    public Button getCloseButton() {
        return new Button(close, "Закрыть", getWebDriver());
    }

    public Collection getValidationInfoCollection() {
        return new Collection(validationInfo, "Ошибки валидации", getWebDriver());
    }
}