package ru.spice.at.ui.controls;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;

import static io.qameta.allure.Allure.step;

/**
 * Методы для веб-элемента Кнопка
 */
@Log4j2
public class Button extends BaseWebElement {
    @Getter
    private final Actions actions;

    public Button(WebElement element, String name, WebDriver webDriver) {
        super(element, name, webDriver);
        actions = new Actions(this.webDriver);
    }

    /**
     * Кликаем по элементу
     *
     * @return - объект Button
     */
    @Override
    public Button click() {
        return (Button) super.click();
    }

    /**
     * Дважды кликаем по элементу
     *
     * @return - объект Button
     */
    public Button dblClick() {
        log.info("Дважды кликаем по элементу '{}' {}", name, getElement());
        step(String.format("Дважды кликаем по элементу '%s'", name),
                () -> {
                    waitForClickableElement(getElement());
                    actions.doubleClick(getElement()).perform();
                }
        );
        return this;
    }

    /**
     * Проверка активность элемента кнопки
     *
     * @return - true - активно
     */
    public boolean isEnabled() {
        log.info("Проверка активность элемента кнопки '{}' {}", name, getElement());
        waitForElement(getElement());
        return getElement().isEnabled();
    }
}
