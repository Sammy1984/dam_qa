package ru.spice.at.ui.controls;

import lombok.extern.log4j.Log4j2;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 * Обёртка над любым html element
 *
 * @author Aleksandr Osokin
 */
@Log4j2
public class Element extends BaseWebElement {

    public Element(WebElement element, String name, WebDriver driver) {
        super(element, name, driver);
    }

    /**
     * Получаем текст из текстового поля
     *
     * @return - текст
     */
    public String getText() {
        log.info("Получаем текст из элемента '{}' {}", name, getElement());
        waitForElement(getElement());
        return getElement().getText();
    }
}