package ru.spice.at.ui.controls;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import static io.qameta.allure.Allure.step;

/**
 * Методы для веб-элемента Чекбокс (переключатель)
 */
@Log4j2
public class CheckBox extends BaseWebElement {
    @Getter
    private WebElement checkElement;

    public CheckBox(WebElement element, String name, WebDriver webDriver) {
        super(element, name, webDriver);
        this.checkElement = element;
    }

    public CheckBox(WebElement element, WebElement checkElement, String name, WebDriver webDriver) {
        super(element, name, webDriver);
        this.checkElement = checkElement;
    }

    /**
     * Кликаем по элементу, переключаем чекбокс
     *
     * @return - объект CheckBox
     */
    public CheckBox check() {
        log.info("Кликаем по элементу '{}', переключаем чекбокс {}", name, getElement());
        step(String.format("Кликаем по элементу, '%s' переключаем чекбокс", name), () -> {
            waitForClickableElement(getElement());
            getElement().click();
        });
        return this;
    }

    /**
     * Кликаем по элементу, переключаем чекбокс в зависимости от параметров
     * Если checkParameter содержится в атрибуте attribute элемента, то переключение чекбокса не происходит
     *
     * @param attribute - атрибут данного веб-элемента (тега), в котором не содержится checkParameter
     * @param checkParameter - параметр отвечающий за состояние чекбокса
     * @return - объект CheckBox
     */
    public CheckBox check(String attribute, String checkParameter) {
        log.info("Кликаем по элементу '{}', переключаем чекбокс в зависимости от параметров {}", name, getElement());
        step(String.format("Кликаем по элементу, '%s' переключаем чекбокс в зависимости от параметров", name), () -> {
            if (checkElement != null) {
                waitForElement(checkElement);
                if (!checkElement.getAttribute(attribute).contains(checkParameter)) {
                    check();
                }
            } else {
                check();
            }
        });
        return this;
    }

    /**
     * Кликаем по элементу, переключаем чекбокс в зависимости от параметров
     * Если checkParameter содержится в атрибуте attribute элемента, то переключение чекбокса не происходит
     *
     * @param attribute - атрибут данного веб-элемента (тега), в котором содержится checkParameter
     * @param checkParameter - параметр отвечающий за состояние чекбокса
     * @return - объект CheckBox
     */
    public CheckBox uncheck(String attribute, String checkParameter) {
        log.info("Кликаем по элементу '{}', переключаем чекбокс в зависимости от параметров {}", name, getElement());
        step(String.format("Кликаем по элементу, '%s' переключаем чекбокс в зависимости от параметров", name), () -> {
            if (checkElement != null) {
                waitForElement(checkElement);
                if (checkElement.getAttribute(attribute).contains(checkParameter)) {
                    check();
                }
            } else {
                check();
            }
        });
        return this;
    }
}
