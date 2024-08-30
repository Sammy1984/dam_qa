package ru.spice.at.ui.controls;

import lombok.extern.log4j.Log4j2;
import org.awaitility.Awaitility;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static io.qameta.allure.Allure.step;

/**
 * Методы для веб-элемента Поле ввода
 */
@Log4j2
public class Field extends BaseWebElement {

    public Field(WebElement element, String name, WebDriver webDriver) {
        super(element, name, webDriver);
    }

    /**
     * Вводим текст в поле ввода
     *
     * @param var - текст для ввода
     * @return - объект Field
     */
    public Field setText(CharSequence... var) {
        log.info("Вводим текст в поле ввода '{}' {}", name, getElement());
        step(String.format("Вводим текст в поле ввода '%s'", name), () -> {
            waitForElement(getElement());
            getElement().sendKeys(var);
        });
        return this;
    }

    /**
     * Вводим текст в поле ввода
     *
     * @param text - текст для ввода
     * @param timeout - задержка между вводом символов в миллисекундах
     * @return - объект Field
     */
    public Field setText(String text, int timeout) {
        log.info("Вводим текст '{}' в поле ввода '{}' {}", text, name, getElement());
        step(String.format("Вводим текст '%s' в поле ввода '%s'", text, name), () -> {
            waitForElement(getElement());
            List<String> symbols = Arrays.asList(text.split(""));
            symbols.forEach(s -> {
                try {
                    TimeUnit.MILLISECONDS.sleep(timeout);
                } catch (InterruptedException e) {
                    log.error("Ошибка ввода символов: {}", e.getMessage());
                }
                getElement().sendKeys(s);
            });
        });
        return this;
    }

    /**
     * Вводим текст в скрытое поле ввода
     * Применяется для вставки ссылки на файл без использования всплывающего меню браузера
     *
     * @param var - текст для ввода
     * @return - объект Field
     */
    public Field setHiddenText(CharSequence... var) {
        log.info("Вводим тест в скрытое поле ввода {}", getElement());
        step(String.format("Вводим текст в скрытое поле ввода '%s'", name), () ->
                Awaitility.given().pollDelay(300, TimeUnit.MILLISECONDS)
                        .pollInterval(1, TimeUnit.SECONDS)
                        .and().timeout(timeout, TimeUnit.SECONDS)
                        .until(() -> {
                            try {
                                waitForInvisibilityElement();
                                getElement().sendKeys(var);
                                return true;
                            } catch (Exception e) {
                                log.info("Ошибка при вводе в скрытое поле");
                                return false;
                            }
                        }));
        return this;
    }

    /**
     * Удаляем текст в поле ввода
     *
     * @return - объект Field
     */
    public Field deleteText() {
        log.info("Удаляем тест в поле ввода '{}' {}", name, getElement());
        step(String.format("Удаляем тест в поле ввода '%s'", name), () -> {
            waitForElement(getElement());
            getElement().clear();
        });
        return this;
    }

    /**
     * Выделяем текст в поле ввода
     *
     * @return - объект Field
     */
    public Field highlightText() {
        log.info("Выделяем тест в поле ввода '{}' {}", name, getElement());
        step(String.format("Выделяем тест в поле ввода '%s'", name), () -> {
            waitForElement(getElement());
            getElement().sendKeys(System.getProperties().getProperty("os.name").contains("Mac OS") &&
                    !standProperties.getSettings().remote() ? Keys.COMMAND : Keys.CONTROL, "a");
        });
        return this;
    }

    /**
     * Выделяем текст в поле ввода и удаляем его
     *
     * @return - объект Field
     */
    public Field highlightDeleteText() {
        log.info("Удаляем тест в поле ввода '{}' {}", name, getElement());
        step(String.format("Удаляем тест в поле ввода '%s'", name), () -> {
            highlightText();
            getElement().sendKeys(Keys.DELETE);
        });
        return this;
    }

    /**
     * Передаём нажатие клавиш в поле ввода
     * @param keys клавиши, нажатия которых хотим передать
     * @return - объект Field
     */
    public Field sendKeys(Keys... keys) {
        log.info("Передаем нажатие клавиш в поле ввода '{}' {}", name, getElement());
        step(String.format("Передаем нажатие клавиш в поле ввода '%s'", name), () -> {
            waitForElement(getElement());
            getElement().sendKeys(keys);
        });
        return this;
    }

    /**
     * Получаем текст в поле ввода
     *
     * @return - текст в поле ввода
     */
    public String getText() {
        log.info("Получаем текст в поле ввода '{}' {}", name, getElement());
        waitForElement(getElement());
        return getElement().getAttribute("value");
    }

    /**
     * Проверка активности поля ввода
     *
     * @return - true - активно
     */
    public boolean isEnabled() {
        log.info("Проверка активности поля ввода '{}' {}", name, getElement());
        waitForElement(getElement());
        return getElement().isEnabled();
    }
}