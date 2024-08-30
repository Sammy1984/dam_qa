package ru.spice.at.ui.pages.spice.pims;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import ru.spice.at.ui.annotations.Mandatory;
import ru.spice.at.ui.annotations.Name;
import ru.spice.at.ui.controls.Button;
import ru.spice.at.ui.controls.Label;
import ru.spice.at.ui.pages.spice.PimsBasePage;

/**
 * Страница - список задач PIMS
 */
@Name("Страница - список задач PIMS")
public class TasksListPimsPage extends PimsBasePage {

    public TasksListPimsPage(WebDriver webDriver) {
        super(webDriver);
    }

    @Mandatory
    @FindBy(xpath = "//h5[text()='Задачи']")
    private WebElement title;

    @Mandatory
    @FindBy(xpath = "//button[text()='Создать задачу']")
    private WebElement createTask;

    public Label getTitleLabel() {
        return new Label(title, "Заголовок", getWebDriver());
    }

    public Button getCreateTaskButton() {
        return new Button(createTask, "Создать задачу", getWebDriver());
    }
}
