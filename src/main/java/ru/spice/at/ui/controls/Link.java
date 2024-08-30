package ru.spice.at.ui.controls;

import lombok.extern.log4j.Log4j2;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 * Методы для веб-элемента Ссылка
 */
@Log4j2
public class Link extends BaseWebElement {

    public Link(WebElement element, String name, WebDriver webDriver) {
        super(element, name, webDriver);
    }

    /**
     * Кликаем по элементу
     *
     * @return - объект Link
     */
    @Override
    public Link click() {
        return (Link) super.click();
    }

    /**
     * Получаем ссылку из элемента
     *
     * @return - ссылка
     */
    public String getLink() {
        log.info("Получаем ссылку из элемента '{}' {}", name, getElement());
        waitForElement(getElement());
        return getElement().getAttribute("href");
    }

    /**
     * Получаем текст ссылки элемента
     *
     * @return - текст
     */
    public String getText() {
        log.info("Получаем текст ссылки элемента '{}' {}", name, getElement());
        waitForElement(getElement());
        return getElement().getText();
    }

    /**
     * Проверка активность элемента ссылки
     *
     * @return - true - активно
     */
    public boolean isEnabled() {
        log.info("Проверка активность элемента кнопки '{}' {}", name, getElement());
        waitForElement(getElement());
        return getElement().isEnabled();
    }
}
