package ru.spice.at.ui.controls;

import lombok.extern.log4j.Log4j2;
import org.awaitility.Awaitility;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static io.qameta.allure.Allure.step;
import static java.lang.String.format;

/**
 * Методы для Списка веб-элементов
 */
@Log4j2
public class Collection extends BaseWebElement {
    private static final String NOT_FOUND = "Элемент не найден";
    private static final String EMPTY_LIST = "Пустой список элементов";
    private final Actions actions;

    public Collection(List<WebElement> elements, String name, WebDriver webDriver) {
        super(elements, name, webDriver);
        actions = new Actions(this.webDriver);
    }

    private void checkEmpty() {
        if (getElements().isEmpty()) {
            throw new UnsupportedOperationException(EMPTY_LIST);
        }
    }

    /**
     * Выполняется поиск элемента по заданному условию и выполняется заданное действие
     *
     * @param text - текст поиска
     * @param condition - условия поиска (Например {@link String#equals(Object)}, {@link String#contains(CharSequence)} и т.д. )
     * @param action - действие над элементом (Например {@link WebElement#click()}, {@link Actions#doubleClick()}
     * @return - WebElement
     */
    private WebElement searchElementAndMakeAction(String text, BiPredicate<String,
            String> condition, Consumer<WebElement> action) {
        checkEmpty();
        for (WebElement element : getElements()) {
            waitForElement(element);
            if (condition.test(element.getText(), text)) {
                waitForClickableElement(element);
                action.accept(element);
                return element;
            }
        }
        throw new UnsupportedOperationException(NOT_FOUND);
    }

    /**
     * Выполняется поиск элемента по соответствию текса
     *
     * @param text - текст поиска
     * @return - WebElement
     */
    public WebElement searchElement(String text) {
        checkEmpty();
        for (WebElement element : getElements()) {
            waitForElement(element);
            if (element.getText().equals(text)) {
                waitForClickableElement(element);
                return element;
            }
        }
        throw new UnsupportedOperationException(NOT_FOUND);
    }

    /**
     * Получаем лист элементов
     *
     * @param clazz - Класс элемента
     * @return - Лист элементов типа clazz
     */
    public <T extends BaseWebElement> List<T> getElementList(Class<T> clazz) {
        log.info("Получаем лист элементов класса {}", clazz);
        checkEmpty();
        AtomicInteger i = new AtomicInteger();
        return getElements().stream()
                .map(element -> {
                    try {
                        return clazz.getDeclaredConstructor(WebElement.class, String.class, WebDriver.class)
                                .newInstance(element, String.format("%s - элемент %d", name, i.getAndIncrement()), webDriver);
                    } catch (Exception e) {
                        log.error("Ошибка при преобразовании элементов в нужны тип данных {}", e.getMessage());
                    }
                    throw new IllegalArgumentException(format("Класс '%s' не поддерживается", clazz));
                })
                .collect(Collectors.toList());
    }

    /**
     * Получаем веб-элемент из коллекции по номеру
     *
     * @param clazz - Класс элемента
     * @param index - Номер элемента в списке
     * @return - веб-элемент типа clazz
     */
    public <T extends BaseWebElement> T getElement(Class<T> clazz, int index) {
        log.info("Получаем веб-элемент класса {} из коллекции по номеру {}", clazz, index);
        checkEmpty();
        return getElement(clazz, getElements().get(index));
    }

    /**
     * Получаем дочерний веб-элемент из коллекции по номеру
     *
     * @param clazz - Класс элемента
     * @param index - Номер элемента в списке
     * @param locator - Локатор
     * @return - дочерний веб-элемент типа clazz
     */
    public <T extends BaseWebElement> T getElement(Class<T> clazz, int index, By locator) {
        log.info("Получаем веб-элемент класса {} из коллекции по номеру {}", clazz, index);
        checkEmpty();
        return getElement(clazz, getElements().get(index).findElement(locator));
    }

    /**
     * Кликаем по элементу, содержащему определенный текст
     *
     * @param text - текст, содержащийся в элементе
     * @param equals - если true, ищем значение полностью совпадающее с text
     * @return - WebElement
     */
    public WebElement clickTo(String text, boolean equals) {
        log.info("Кликаем по элементу из списка '{}', содержащему определенный текст '{}'", name, text);
        step(String.format("Кликаем по элементу из списка '%s', содержащему определенный текст '%s'", name, text));
        return equals ? searchElementAndMakeAction(text, String::equals, WebElement::click) :
                searchElementAndMakeAction(text, String::contains, WebElement::click);
    }

    /**
     * Кликаем по элементу с определенным номером в списке
     *
     * @param index - номер элемента в списке
     * @return - WebElement
     */
    public WebElement clickTo(int index) {
        log.info("Кликаем по элементу с определенным номером {} в списке '{}'", index, name);
        step(String.format("Кликаем по элементу с определенным номером %d в списке '%s'", index, name));
        checkEmpty();

        WebElement element = getElements().get(index);
        waitForClickableElement(element);
        element.click();
        return element;
    }

    /**
     * Кликаем по первому элементу, который не должен содержать определенный текст
     *
     * @param notEquals - текст, не содержащийся в элементе
     * @param equals - если true, ищем значение полностью совпадающее с notEquals
     * @return - WebElement
     */
    public WebElement clickToNotEquals(String notEquals, boolean equals) {
        log.info("Кликаем по любому элементу из списка '{}', не содержащий определенный текст '{}'", name, notEquals);
        step(String.format("Кликаем по любому элементу из списка '%s', не содержащий определенный текст '%s'", name, notEquals));
        return equals ? searchElementAndMakeAction(notEquals, (str1, str2) -> !str1.equals(str2), WebElement::click) :
                searchElementAndMakeAction(notEquals, (str1, str2) -> !str1.contains(str2), WebElement::click);
    }

    /**
     * Выбираем одновременно все элементы с текстом
     *
     * @param elementsTitles - список с текстом в соответствующих элементах
     * @return - объект Collection
     */
    public Collection chooseElements(List<String> elementsTitles) {
        log.info("Выбираем одновременно все элементы с текстом '{}'", elementsTitles);
        step(String.format("Выбираем одновременно все элементы с текстом '%s'", elementsTitles));

        Keys key = System.getProperties().getProperty("os.name").contains("Mac OS") && !standProperties.getSettings().remote() ? Keys.COMMAND : Keys.CONTROL;

        actions.keyDown(key);
        elementsTitles.forEach(title -> actions.click(searchElement(title)));
        actions.keyUp(key).build().perform();
        return this;
    }

    /**
     * Кликаем по элементу, содержащему определенный текст
     *
     * @param text - текст, содержащийся в элементе
     * @param equals - если true, ищем значение полностью совпадающее с text
     * @return - WebElement
     */
    public WebElement dblClickTo(String text, boolean equals) {
        log.info("Кликаем дважды по элементу из списка '{}', содержащему определенный текст '{}'", name, text);
        step(String.format("Кликаем дважды по элементу из списка '%s', содержащему определенный текст '%s'", name, text));
        return equals ? searchElementAndMakeAction(text, String::equals, actions::doubleClick) :
                searchElementAndMakeAction(text, String::contains, actions::doubleClick);
    }

    /**
     * Дважды кликаем по элементу с определенным номером в списке
     *
     * @param index - номер элемента в списке
     * @return - WebElement
     */
    public WebElement dblClickTo(int index) {
        log.info("Кликаем дважды по элементу с определенным номером {} в списке '{}'", index, name);
        step(String.format("Кликаем дважды по элементу с определенным номером %d в списке '%s'", index, name));
        checkEmpty();

        WebElement element = getElements().get(index);
        waitForClickableElement(element);
        actions.doubleClick(element).perform();
        return element;
    }

    /**
     * Получаем текст из элемента
     *
     * @param index - номер элемента в списке
     * @return - текст из элемента
     */
    public String getText(int index) {
        log.info("Получаем текст из элемента с номером {} из списка '{}'", index, name);
        checkEmpty();

        WebElement element = getElements().get(index);
        return element.getText();
    }

    /**
     * Ожидаем количества элементов в списке больше 0
     *
     * @return - объект Collection
     */
    public Collection waitForElements() {
        return waitForElements(15);
    }

    /**
     * Ожидаем количества элементов в списке больше 0
     *
     * @param timeout - таймаут в секундах
     * @return - объект Collection
     */
    public Collection waitForElements(int timeout) {
        log.info("Ожидаем количества элементов в списке '{}' больше 0, в течении {} секунд", name, timeout);
        AtomicInteger size = new AtomicInteger();
        Awaitility.given().pollDelay(300, TimeUnit.MILLISECONDS)
                .pollInterval(1, TimeUnit.SECONDS)
                .and().timeout(timeout, TimeUnit.SECONDS)
                .await("Количество элементов равно 0")
                .until(() -> {
                    boolean answer = !getElements().isEmpty() && getElements().size() == size.get();
                    size.set(getElements().size());
                    return answer;
                });
        return this;
    }

    /**
     * Ожидаем отсутствие количества элементов
     *
     * @return - объект Collection
     */
    public Collection waitForEmpty() {
        log.info("Ожидаем отсутствие количества элементов в списке '{}'", name);
        Awaitility.given().pollDelay(300, TimeUnit.MILLISECONDS)
                .pollInterval(1, TimeUnit.SECONDS)
                .and().timeout(standProperties.getSettings().seleniumTimeout(), TimeUnit.SECONDS)
                .await("Количество элементов не равно 0")
                .until(() -> getElements().isEmpty());
        return this;
    }

    /**
     * Ожидаем изменения количества элементов
     *
     * @param size - изначальное количество элементов
     * @return - объект Collection
     */
    public Collection waitForChangeElementsSize(int size) {
        log.info("Ожидаем изменения количества элементов в списке '{}'", name);
        Awaitility.given().pollDelay(300, TimeUnit.MILLISECONDS)
                .pollInterval(1, TimeUnit.SECONDS)
                .and().timeout(standProperties.getSettings().seleniumTimeout(), TimeUnit.SECONDS)
                .await("Количество элементов не изменилось")
                .until(() -> getElements().size() != size);
        return this;
    }

    /**
     * Ожидаем определенного количества элементов
     *
     * @param size - ожидаемое количество элементов
     * @return - объект Collection
     */
    public Collection waitForElementsSize(int size) {
        log.info("Ожидаем количества элементов в списке '{}'", name);
        Awaitility.given().pollDelay(300, TimeUnit.MILLISECONDS)
                .pollInterval(1, TimeUnit.SECONDS)
                .and().timeout(standProperties.getSettings().seleniumTimeout(), TimeUnit.SECONDS)
                .await("Количество элементов не изменилось не равно " + size)
                .until(() -> getElements().size() == size);
        return this;
    }

    /**
     * Ожидаем определенного количества элементов c перезагрузкой
     *
     * @param size - ожидаемое количество элементов
     * @return - объект Collection
     */
    public Collection waitForElementsSizeWithReload(int size) {
        log.info("Ожидаем количества элементов в списке '{}'", name);
        Awaitility.given().pollDelay(3, TimeUnit.SECONDS)
                .pollInterval(1, TimeUnit.SECONDS)
                .and().timeout(standProperties.getSettings().seleniumTimeout(), TimeUnit.SECONDS)
                .await("Количество элементов не изменилось равно " + size)
                .until(() -> {
                    boolean check = getElements().size() == size;
                    if (!check) {
                        webDriver.navigate().refresh();
                    }
                    return check;
                });
        return this;
    }

    /**
     * Получаем количество элементов в списке
     *
     * @return - количество элементов списка
     */
    public int getSize() {
        log.info("Получаем количество элементов в списке '{}'", name);
        return getElements().size();
    }
}
