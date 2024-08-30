package ru.spice.at.ui.pages.spice;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import ru.spice.at.ui.annotations.Hidden;
import ru.spice.at.ui.annotations.Name;
import ru.spice.at.ui.pages.BasePage;

/**
 * Базовая оболочка PIMS
 */
@Name("Базовая оболочка PIMS")
public class PimsBasePage extends BasePage {

    public PimsBasePage(WebDriver webDriver) {
        super(webDriver);
    }

    @Hidden
    @FindBy(xpath = "//div[@data-qa='global_loader' and contains(@style, 'flex')]//*[name()='svg']")
    private WebElement globalLoader;

    @Hidden
    @FindBy(xpath = "//div[@data-qa='loader']//*[name()='svg']")
    private WebElement loader;
}