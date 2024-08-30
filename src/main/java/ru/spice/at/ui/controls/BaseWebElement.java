package ru.spice.at.ui.controls;

import static io.qameta.allure.Allure.step;

import lombok.extern.log4j.Log4j2;
import org.awaitility.Awaitility;
import org.awaitility.core.ConditionFactory;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import ru.spice.at.common.StandProperties;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static java.lang.String.format;

/**
 * Базовый класс для методов веб-элемента
 */
@Log4j2
public abstract class BaseWebElement {
    protected static final StandProperties standProperties = new StandProperties();
    protected static final Integer timeout = standProperties.getSettings().seleniumTimeout();
    protected final WebDriver webDriver;
    protected final WebDriverWait wait;
    protected final String name;

    private WebElement element;
    private List<WebElement> elements;

    protected BaseWebElement(WebElement element, String name, WebDriver webDriver) {
        this(name, webDriver);
        this.element = element;
    }

    protected BaseWebElement(List<WebElement> elements, String name, WebDriver webDriver) {
        this(name, webDriver);
        this.elements = elements;
    }

    protected BaseWebElement(WebElement element, List<WebElement> elements, String name, WebDriver webDriver) {
        this(name, webDriver);
        this.element = element;
        this.elements = elements;
    }

    private BaseWebElement(String name, WebDriver webDriver) {
        this.webDriver = webDriver;
        this.name = name;
        this.wait = new WebDriverWait(webDriver, Duration.ofSeconds(timeout));
    }

    public List<WebElement> getElements() {
        if (elements == null) {
            log.error("Элементы не проинициализированы");
            throw new NullPointerException("Элементы не проинициализированы");
        }
        return elements;
    }

    public WebElement getElement() {
        if (element == null) {
            log.error("Элемент не проинициализирован");
            throw new NullPointerException("Элемент не проинициализирован");
        }
        return element;
    }

    /**
     * Получаем веб-элемент класса clazz
     *
     * @param clazz   - Класс элемента
     * @param element - Веб-элемент
     * @return - веб-элемент типа clazz
     */
    protected <T extends BaseWebElement> T getElement(Class<T> clazz, WebElement element) {
        try {
            return clazz.getDeclaredConstructor(WebElement.class, String.class, WebDriver.class).newInstance(element, name, webDriver);
        } catch (Exception e) {
            log.error("Ошибка при преобразовании элементов в нужный тип данных {}", e.getMessage());
        }
        throw new IllegalArgumentException(format("Класс '%s' не поддерживается", clazz));
    }

    /**
     * Ожидаем видимость веб-элемента данного контроллера.
     *
     * @return - веб-элемент данного контроллера
     */
    public WebElement waitForElement() {
        return waitForElement(getElement());
    }

    /**
     * Ожидаем видимость веб-элемента
     *
     * @param element - веб-элемент
     * @return - веб-элемент
     */
    public WebElement waitForElement(WebElement element) {
        log.info("Ожидаем видимость веб-элемента {}", element);
        AtomicReference<WebElement> atomicWebElement = new AtomicReference<>();
        Awaitility.given().pollDelay(300, TimeUnit.MILLISECONDS)
                .pollInterval(1, TimeUnit.SECONDS)
                .and().timeout(timeout, TimeUnit.SECONDS)
                .await("Ожидание элемента")
                .until(() -> {
                    try {
                        atomicWebElement.set(wait.until(ExpectedConditions.visibilityOf(element)));
                        return true;
                    }
                    catch (Exception e) {
                        log.info("Ошибка при ожидании элемента");
                        return false;
                    }
                });
        return atomicWebElement.get();
    }

    /**
     * Ожидаем невидимость веб-элемента данного контроллера.
     *
     * @return - true/false
     */
    public boolean waitForInvisibilityElement() {
        log.info("Ожидаем видимость веб-элемента '{}' {}", name, getElement());
        return wait.until(ExpectedConditions.invisibilityOf(getElement()));
    }

    /**
     * Ожидаем кликабельность веб-элемента
     *
     * @return - веб-элемент
     */
    public WebElement waitForClickableElement() {
        return waitForClickableElement(getElement());
    }

    /**
     * Ожидаем кликабельность веб-элемента
     *
     * @param element - веб-элемент
     * @return - веб-элемент
     */
    public WebElement waitForClickableElement(WebElement element) {
        log.info("Ожидаем кликабельность веб-элемента {}", element);
        waitForElement(element);
        return wait.until(ExpectedConditions.elementToBeClickable(element));
    }

    /**
     * Ожидаем невидимость веб-элемента
     */
    public BaseWebElement waitForInvisibilityOfElement() {
        log.info("Ожидаем невидимость веб-элемента '{}' {}", name, getElement());
        AtomicBoolean isDisplayed = new AtomicBoolean(false);
        ConditionFactory given = Awaitility.given();
        given.pollInterval(100, TimeUnit.MILLISECONDS);
        given.and();
        given.timeout(standProperties.getSettings().seleniumTimeout(), TimeUnit.SECONDS);
        given.await("Элемент присутствует на странице");
        given.until(() -> {
            if (!isDisplayed.get()) {
                try {
                    isDisplayed.set(getElement().isDisplayed());
                    return false;
                } catch (Exception e) {
                    log.info("Страница не содержит элемент {}", name);
                    return true;
                }
            } else {
                boolean isNotExist = false;
                try {
                    isNotExist = !getElement().isDisplayed();
                } catch (Exception e) {
                    log.info("Страница не содержит элемент {}", name);
                    return true;
                }
                return isNotExist;
            }
        });
        return this;
    }

    /**
     * Получаем текст аттрибута элемента
     *
     * @param attribute - название аттрибута
     * @return - текст аттрибута
     */
    public String getAttributeText(String attribute) {
        log.info("Получаем текст из аттрибута '{}' элемента '{}' {}", attribute, name, getElement());
        waitForElement(getElement());
        return getElement().getAttribute(attribute);
    }

    /**
     * Получаем текст из текстового поля или из аттрибута value
     *
     * @return - текст
     */
    public String getTextValue() {
        log.info("Получаем текст из текстового поля '{}' {}", name, getElement());
        waitForElement(getElement());
        String text = getElement().getText();
        if (text.isEmpty()) {
            text = getAttributeText("value");
        }
        return text;
    }

    /**
     * Кликаем по элементу
     *
     * @return - объект BaseWebElement
     */
    public BaseWebElement click() {
        log.info("Кликаем по элементу '{}' {}", name, getElement());
        step(String.format("Кликаем по элементу '%s'", name), () -> {
            waitForClickableElement(getElement());
            getElement().click();
        });
        return this;
    }

    /**
     * Наводи курсор на элемент
     *
     * @return - объект Button
     */
    public BaseWebElement hover() {
        log.info("Наводи курсор на элемент '{}' {}", name, getElement());
        step(String.format("Наводи курсор на элемент '%s'", name),
                () -> {
                    waitForElement(getElement());
                    new Actions(this.webDriver).moveToElement(getElement()).perform();
                }
        );
        return this;
    }

    public BaseWebElement actionsSendKeys(CharSequence... keys) {
        log.info("Ввод горячими клавишами");
        step("Ввод горячими клавишами",
                () -> {
                    waitForElement(getElement());
                    new Actions(this.webDriver).sendKeys(keys).perform();
                }
        );
        return this;
    }

    public void scrollToElement() {
        scrollToElement(element);
    }

    public void scrollToElement(WebElement element) {
        int elementPosition = element.getLocation().getY();
        String js = String.format("window.scroll(0, %s)", elementPosition);
        ((JavascriptExecutor) webDriver).executeScript(js);
    }

    public String getCurrentUrl() {
        return webDriver.getCurrentUrl();
    }
}
