package ru.spice.at.ui.pages.spice;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import ru.spice.at.ui.annotations.Hidden;
import ru.spice.at.ui.annotations.Mandatory;
import ru.spice.at.ui.annotations.Name;
import ru.spice.at.ui.controls.Button;
import ru.spice.at.ui.controls.Label;
import ru.spice.at.ui.pages.BasePage;

/**
 * Базовая оболочка DAM
 */
@Name("Базовая оболочка DAM")
public class DamBasePage extends BasePage {

    public DamBasePage(WebDriver webDriver) {
        super(webDriver);
    }

    @Mandatory
    @FindBy(xpath = "//div[contains(@class, 'MuiBox-root')]/p[text()='DAM']")
    private WebElement baseTitle;

    @Mandatory
    @FindBy(xpath = "//div[text()='Медиафайлы']/parent::a")
    private WebElement mediaFiles;

    @Mandatory
    @FindBy(xpath = "//a[@href='/exports']")
    private WebElement exports;

    @Mandatory
    @FindBy(xpath = "//a[@href='/imports']")
    private WebElement imports;

    @FindBy(xpath = "//div[contains(@class, 'MuiBox-root')]//button[text()='Профиль']")
    private WebElement profile;

    @FindBy(xpath = "//div[contains(@class, 'MuiBox-root')]//button[text()='Уведомления']")
    private WebElement notifications;

    @FindBy(xpath = "//div[contains(@class, 'MuiBox-root')]//button[text()='Свернуть']")
    private WebElement collapse;

    @Hidden
    @FindBy(xpath = "//*[contains(@class, 'skeleton')]")
    private WebElement skeleton;

    public Label getBaseTitleLabel() {
        return new Label(baseTitle, "Заголовок", getWebDriver());
    }

    public Button getMediaFilesButton() {
        return new Button(mediaFiles, "Медиафайлы - вкладка", getWebDriver());
    }

    public Button getImportsButton() {
        return new Button(imports, "Импорты - вкладка", getWebDriver());
    }

    public Button getExportsButton() {
        return new Button(exports, "Экспорты - вкладка", getWebDriver());
    }

    public Button getProfileButton() {
        return new Button(profile, "Профиль", getWebDriver());
    }

    public Button getNotificationsButton() {
        return new Button(notifications, "Уведомления", getWebDriver());
    }

    public Button getCollapseButton() {
        return new Button(collapse, "Свернуть", getWebDriver());
    }
}
