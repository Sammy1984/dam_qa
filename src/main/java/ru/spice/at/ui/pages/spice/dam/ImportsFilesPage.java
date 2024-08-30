package ru.spice.at.ui.pages.spice.dam;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import ru.spice.at.ui.annotations.Mandatory;
import ru.spice.at.ui.annotations.Name;
import ru.spice.at.ui.controls.Button;
import ru.spice.at.ui.controls.Table;
import ru.spice.at.ui.pages.spice.DamBasePage;

/**
 * Загрузки файлов /validations
 */
@Name("Загрузки")
public class ImportsFilesPage extends DamBasePage {

    public ImportsFilesPage(WebDriver webDriver) {
        super(webDriver);
    }

    @Mandatory
    @FindBy(xpath = "//h5[contains(@class, 'MuiTypography')]")
    private WebElement title;

    @Mandatory
    @FindBy(xpath = "//button[contains(@id, 'Все загрузки')]")
    private WebElement allImports;

    @Mandatory
    @FindBy(xpath = "//button[contains(@id, 'Без ошибок')]")
    private WebElement importsWithoutErrors;

    @Mandatory
    @FindBy(xpath = "//button[contains(@id, 'С ошибками')]")
    private WebElement importsWithErrors;

    @Mandatory
    @FindBy(xpath = "//div[@role='tabpanel']//table")
    private WebElement importsTable;

    public Button getAllImportsButton() {
        return new Button(allImports, "Все импорты", getWebDriver());
    }

    public Button getImportsWithoutErrorsButton() {
        return new Button(importsWithoutErrors, "Без ошибок", getWebDriver());
    }

    public Button getImportsWithErrorsButton() {
        return new Button(importsWithErrors, "С ошибками", getWebDriver());
    }

    public Table getImportsTable() {
        return new Table(importsTable, "Таблица импортов", getWebDriver());
    }
}
