package ru.spice.at.ui.pages.spice.dam;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import ru.spice.at.ui.annotations.Mandatory;
import ru.spice.at.ui.annotations.Name;
import ru.spice.at.ui.controls.Label;
import ru.spice.at.ui.controls.Table;
import ru.spice.at.ui.pages.spice.DamBasePage;

/**
 * Ошибки валидации
 */
@Deprecated
@Name("Ошибки валидации")
public class ValidationErrorsPage extends DamBasePage {

    public static final By TABLE_DOWNLOAD_BUTTON = By.xpath(".//button[contains(@class, 'link')]");

    public ValidationErrorsPage(WebDriver webDriver) {
        super(webDriver);
    }

    @Mandatory
    @FindBy(xpath = "//h5[contains(@class, 'MuiTypography')]")
    private WebElement title;

    @FindBy(xpath = "//table[contains(@class, 'MuiTable')]")
    private WebElement validationTable;

    public Label getTitleLabel() {
        return new Label(title, "Заголовок", getWebDriver());
    }

    public Table getValidationTable() {
        return new Table(validationTable, "Таблица валидации", getWebDriver());
    }
}
