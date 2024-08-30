package ru.spice.at.ui.controls;

import lombok.extern.log4j.Log4j2;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.List;

import static io.qameta.allure.Allure.step;

/**
 * Методы для веб-элемента Комбобокс (комбинация стандартных веб-элементов)
 */
@Log4j2
public class ComboBox extends BaseWebElement {

    public ComboBox(WebElement element, List<WebElement> elements, String name, WebDriver webDriver) {
        super(element, elements, name, webDriver);
    }

    /**
     * Кликаем по элементу и выбираем элемент из списка
     *
     * @param text - текст, содержащийся в элементе
     * @return - объект ComboBox
     */
    public ComboBox clickSelect(String text) {
        log.info("Кликаем по элементу '{}' {} и выбираем элемент из списка", name, getElement());
        step(String.format("Кликаем по элементу '%s' и выбираем элемент из списка", name), () -> {
            waitForClickableElement(getElement());
            getElement().click();
            Collection collection = new Collection(getElements(), name, webDriver);
            collection.waitForElements().clickTo(text, false);
        });
        return this;
    }

    /**
     * Кликаем по элементу и выбираем любой из списка, который не
     * соответствует заданному имени
     *
     * @param notEqualsOption - текст, содержащийся в элементе, на котором кликать не нужно
     * @return - объект ComboBox
     */
    public ComboBox clickSelectAnyNotEqualsTo(String notEqualsOption) {
        log.info("Кликаем по элементу '{}' {} и выбираем элемент из списка", name, getElement());
        step(String.format("Кликаем по элементу '%s' и выбираем элемент из списка", name), () -> {
            waitForClickableElement(getElement());
            getElement().click();
            Collection collection = new Collection(getElements(), name, webDriver);
            collection.waitForElements().clickToNotEquals(notEqualsOption, false);
        });
        return this;
    }

    /**
     * Кликаем по элементу и выбираем первый элемент из списка
     *
     * @return - объект ComboBox
     */
    public ComboBox clickSelectFirst() {
        return clickSelectByIndex(0);
    }

    /**
     * Кликаем по элементу и выбираем элемент по индексу
     * @param index - индекс выбора элемента в списке
     * @return - объект ComboBox
     */
    public ComboBox clickSelectByIndex(int index) {
        log.info("Кликаем по элементу '{}' {} и выбираем элемент из списка по индексу {}", name, getElement(), index);
        waitForClickableElement(getElement());
        getElement().click();
        return selectByIndex(index);
    }

    /**
     * Кликаем по элементу и выбираем первый элемент из списка клавишей Enter
     *
     * @return - объект ComboBox
     */
    public ComboBox clickEnterSelectFirst() {
        log.info("Кликаем по элементу '{}' {} и выбираем первый элемент из списка клавишей Enter", name, getElement());
        step(String.format("Кликаем по элементу '%s' и выбираем первый элемент из списка клавишей Enter", name), () -> {
            waitForClickableElement(getElement());
            getElement().click();
            getElement().sendKeys(Keys.DOWN, Keys.ENTER);
        });
        return this;
    }

    /**
     * Кликаем, вводим текст и выбираем первый элемент из списка
     *
     * @param text - текст, содержащийся в элементе
     * @return - объект ComboBox
     */
    public ComboBox clickInputSelectFirst(String text) {
        log.info("Кликаем, вводим текст '{}' и выбираем первый элемент из списка '{}' {}", text, name, getElement());
        waitForClickableElement(getElement());
        getElement().click();
        getElement().sendKeys(text);
        return selectByIndex(0);
    }

    /**
     * Вводим текст и выбираем первый элемент из списка
     *
     * @param text - текст, содержащийся в элементе
     * @return - объект ComboBox
     */
    public ComboBox inputSelectFirst(String text) {
        log.info("Вводим текст {} и выбираем первый элемент из списка '{}' {}", text, name, getElement());
        waitForClickableElement(getElement());
        getElement().sendKeys(text);
        return selectByIndex(0);
    }

    /**
     * Вводим текст, нажимаем Enter и выбираем первый элемент из списка
     *
     * @param text - текст, содержащийся в элементе
     * @return - объект ComboBox
     */
    public ComboBox inputWithEnterSelectFirst(String text) {
        log.info("Вводим текст {} и выбираем первый элемент из списка '{}' {}", text, name, getElement());
        waitForClickableElement(getElement());
        getElement().sendKeys(text);
        getElement().sendKeys(Keys.ENTER);
        return selectByIndex(0);
    }

    /**
     * Выбираем элемент в открывшемся списке
     *
     * @param index - Выбираем элемент по индексу
     * @return - объект ComboBox
     */
    public ComboBox selectByIndex(int index) {
        log.info("Выбираем элемент из списка '{}' {}", name, getElements());
        step(String.format("Выбираем элемент из списка '%s'", name), () -> {
            Collection collection = new Collection(getElements(), name, webDriver);
            collection.waitForElements().clickTo(index);
        });
        return this;
    }

    /**
     * Получаем текущий текст элемента
     * @return текст, который содержится в элементе
     */
    public String getElementText() {
        log.info("Получаем текст элемента {}", getElement());
        return waitForElement(getElement()).getText();
    }

    /**
     * Получаем коллекцию элементов
     * @return - коллекция элементов
     */
    public Collection getElementsCollection() {
        return new Collection(getElements(), name, webDriver);
    }
}
