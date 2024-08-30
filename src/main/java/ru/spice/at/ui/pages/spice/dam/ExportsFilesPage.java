package ru.spice.at.ui.pages.spice.dam;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import ru.spice.at.ui.annotations.Mandatory;
import ru.spice.at.ui.annotations.Name;
import ru.spice.at.ui.controls.Button;
import ru.spice.at.ui.controls.Collection;
import ru.spice.at.ui.controls.Field;
import ru.spice.at.ui.pages.spice.DamBasePage;

import java.util.List;

/**
 * Экспорты файлов /exports
 */
@Name("Экспорты")
public class ExportsFilesPage extends DamBasePage {

    public ExportsFilesPage(WebDriver webDriver) {
        super(webDriver);
    }

    @Mandatory
    @FindBy(xpath = "//h1[contains(@class, 'MuiTypography')]")
    private WebElement title;

    @Mandatory
    @FindBy(xpath = "//p[text()='Дата выгрузки']")
    private WebElement dateHeader;

    @Mandatory
    @FindBy(xpath = "//p[text()='Кол-во']")
    private WebElement countHeader;

    @Mandatory
    @FindBy(xpath = "//p[text()='Имя архива']")
    private WebElement archiveNameHeader;

    @Mandatory
    @FindBy(xpath = "//p[text()='Ссылка на скачивание']")
    private WebElement linkHeader;

    @FindBy(xpath = "//label[text()='Дата экспорта']/..//input")
    private WebElement dateField;

    @FindBy(xpath = "//label[text()='Дата экспорта']/..//input//..//button[contains(@class, 'close')]")
    private WebElement dateClear;

    @FindBy(xpath = "//div[@data-testid='virtuoso-item-list']/div")
    private List<WebElement> exports;

    public Field getDateField() {
        return new Field(dateField, "Дата экспорта", getWebDriver());
    }

    public Button getDateClearButton() {
        return new Button(dateClear, "Дата экспорта - очистить", getWebDriver());
    }

    public Collection getExportsCollection() {
        return new Collection(exports, "Экспорты", getWebDriver());
    }
}
