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
 * Информация о загрузке: модальное окно
 */
@Name("Информация о загрузке")
public class ImportInfoModalPage extends BasePage {

    public ImportInfoModalPage(WebDriver webDriver) {
        super(webDriver);
    }

    @Mandatory
    @FindBy(xpath = "//h2//p[text()='Информация о загрузке']")
    private WebElement title;

    @Mandatory
    @FindBy(xpath = "//div[@role='dialog']//button[contains(@id, 'Все файлы')]")
    private WebElement allFiles;

    @FindBy(xpath = "//div[@role='dialog']//button[contains(@id, 'Без ошибок')]")
    private WebElement filesWithoutErrors;

    @FindBy(xpath = "//div[@role='dialog']//button[contains(@id, 'С ошибками')]")
    private WebElement filesWithErrors;

    @FindBy(xpath = "//div[@role='dialog']//div[@role='tabpanel' and not(@hidden)]/div/div")
    private List<WebElement> filesList;

    @FindBy(xpath = "//div[@role='dialog']//div[contains(@class, 'MuiDialogContent')]/div[1]/p[1]")
    private WebElement importDate;

    @FindBy(xpath = "//div[@role='dialog']//div[contains(@class, 'MuiDialogContent')]/div[1]/p[2]")
    private WebElement importUser;

    public Button getAllFilesButton() {
        return new Button(allFiles, "Все файлы", getWebDriver());
    }

    public Button getFilesWithoutErrorsButton() {
        return new Button(filesWithoutErrors, "Без ошибок", getWebDriver());
    }

    public Button getFilesWithErrorsButton() {
        return new Button(filesWithErrors, "С ошибками", getWebDriver());
    }

    public Collection getFilesListCollection() {
        return new Collection(filesList, "Файлы - список", getWebDriver());
    }

    public Label getImportDateLabel() {
        return new Label(importDate, "Дата импорта", getWebDriver());
    }

    public Label getImportUserLabel() {
        return new Label(importUser, "Загрузчик", getWebDriver());
    }
}
