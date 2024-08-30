package ru.spice.at.ui.controls;

import lombok.extern.log4j.Log4j2;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 * Методы для текстового веб-элемента
 */
@Log4j2
public class Label extends BaseWebElement {
    public Label(WebElement element, String name, WebDriver webDriver) {
        super(element, name, webDriver);
    }

    /**
     * Получаем текст из текстового поля
     *
     * @return - текст
     */
    public String getText() {
        log.info("Получаем текст из текстового поля '{}' {}", name, getElement());
        waitForElement(getElement());
        return getElement().getText();
    }
}
