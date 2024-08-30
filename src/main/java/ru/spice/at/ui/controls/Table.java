package ru.spice.at.ui.controls;

import lombok.extern.log4j.Log4j2;
import org.awaitility.Awaitility;
import org.openqa.selenium.*;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Класс-адаптер, содержащий методы для веб-элемента "Таблица".
 */
@Log4j2
public class Table extends BaseWebElement {
    private static final By TABLE_ROW_LOCATOR = By.xpath(".//tbody/tr");
    private static final By TABLE_HEADER_LOCATOR = By.xpath(".//th");
    private static final By TABLE_CELL_LOCATOR = By.xpath(".//td");
    private static final String EMPTY_LIST = "Пустой список элементов";

    /**
     * Локатор для всех строк в таблице.
     */
    private final By rowLocator;

    /**
     * Локатор для всех заголовков столбцов в таблице. Поскольку таблицы могут быть построены по-разному, не всегда возможно
     * ссылаться на столбцы прямо, но через заголовки на них всегда можно сослаться косвенно, т.к. их порядок одинаков.
     */
    private final By headerLocator;

    /**
     * Локатор для поиска ячеек в таблице в пределах конкретной строки. Поиск в рамках всей таблицы часто приводит к
     * путанице и неоднозначности, т.к. не всегда ячейки можно уникально идентифицировать без ссылки на строку.
     */
    private final By cellLocator;

    /**
     * Создает объект веб-элемента таблицы. Используются локаторы по-умолчанию
     * row     .//tbody/tr
     * header  .//th
     * cell    .//td
     *
     * @param element {@code WebElement}, представляющий таблицу на веб-странице
     * @param driver {@code WebDriver}, используемый для работы с веб-страницей
     */
    public Table(WebElement element, String name, WebDriver driver) {
        this(element, TABLE_ROW_LOCATOR, TABLE_HEADER_LOCATOR, TABLE_CELL_LOCATOR, name, driver);
    }

    /**
     * Создает объект веб-элемента таблицы.
     *
     * @param element {@code WebElement}, представляющий таблицу на веб-странице
     * @param driver {@code WebDriver}, используемый для работы с веб-страницей
     * @param rowLocator локатор, позволяющий найти все строки в таблице
     * @param headerLocator локатор, позволяющий найти все столбцы в таблице
     * @param cellLocator локатор, позволяющий найти все ячейки в рамках одной строки (но не во всей таблице)
     */
    public Table(WebElement element, By rowLocator, By headerLocator, By cellLocator, String name, WebDriver driver) {
        super(element, name, driver);
        waitForElement(element);
        this.rowLocator = rowLocator;
        this.headerLocator = headerLocator;
        this.cellLocator = cellLocator;
    }

    private List<WebElement> getRowList() {
        return getElement().findElements(rowLocator);
    }

    private List<WebElement> getHeaderList() {
        return getElement().findElements(headerLocator);
    }

    /**
     * Возвращает коллекцию строк в таблице.
     *
     * @return коллекция строк в таблице
     */
    public Collection getRowsCollection() {
        log.info("Получаем коллекцию из строк в таблице '{}'", name);
        return new Collection(getRowList(), name, webDriver);
    }

    /**
     * Возвращает коллекцию столбцов в таблице.
     *
     * @return коллекция столбцов в таблице
     */
    public Collection getColumnsCollection() {
        log.info("Получаем коллекцию из столбцов в таблице '{}'", name);
        return new Collection(getHeaderList(), name, webDriver);
    }

    /**
     * Возвращает количество строк в таблице.
     *
     * @return количество строк в таблице
     */
    public int getRowAmount() {
        log.info("Получаем количество строк в таблице '{}'", name);
        return getRowList().size();
    }

    /**
     * Возвращает количество столбцов в таблице.
     *
     * @return количество столбцов в таблице
     */
    public int getColumnAmount() {
        log.info("Получаем количество столбцов в таблице '{}'", name);
        return getHeaderList().size();
    }

    /**
     * Возвращает текстовое значение из ячейки таблицы на пересечении указанной строки и столбца.
     *
     * @param rowIndex номер строки
     * @param columnHeader заголовок столбца
     * @return значение из ячейки таблицы
     */
    public String getCellValue(int rowIndex, String columnHeader) {
        log.info("Получаем текстовое значение ячейки таблицы '{}' с координатами {},'{}'", name, rowIndex, columnHeader);
        return getCellValue(rowIndex, getColumnIndex(columnHeader));
    }

    /**
     * Возвращает текстовое значение из ячейки таблицы на пересечении указанной строки и столбца.
     *
     * @param rowIndex номер строки
     * @param columnIndex номер столбца
     * @return значение из ячейки таблицы
     */
    public String getCellValue(int rowIndex, int columnIndex) {
        log.info("Получаем текстовое значение ячейки таблицы '{}' с координатами {},{}", name, rowIndex, columnIndex);
        if (getRowList().isEmpty()) {
            return null;
        }
        return getRowList().get(rowIndex)
                .findElements(cellLocator)//индекс ячейки равен индексу заголовка столбца
                .get(columnIndex)
                .getText();
    }

    /**
     * Кликаем на ячейку таблицы на пересечении указанной строки и столбца.
     *
     * @param rowIndex номер строки
     * @param columnIndex номер столбца
     */
    public void clickCell(int rowIndex, int columnIndex) {
        log.info("Кликаем на ячейку таблицы '{}' с координатами {},{}", name, rowIndex, columnIndex);
        if (getRowList().isEmpty()) {
            throw new UnsupportedOperationException(EMPTY_LIST);
        }

        Awaitility.given().pollDelay(300, TimeUnit.MILLISECONDS)
                .pollInterval(1, TimeUnit.SECONDS)
                .and().timeout(timeout, TimeUnit.SECONDS)
                .await("Не удалось кликнуть на ячейку")
                .until(() -> {
                    try {
                        WebElement element = getRowList().get(rowIndex);
                        waitForElement(element);
                        element = element.findElements(cellLocator).get(columnIndex);
                        waitForClickableElement(element);
                        element.click();
                        return true;
                    }
                    catch (StaleElementReferenceException e) {
                        log.info("Неуспешный клик на ячейку");
                        return false;
                    }
                });
    }

    /**
     * Возвращает набор веб-элементов в ячейке.
     *
     * @param rowIndex номер строки
     * @param columnHeader заголовок столбца
     * @param innerElementsLocator локатор для поиска элементов внутри ячейки
     * @return экземпляр {@code Collection} с набором веб-элементов
     */
    public Collection getElementsInCell(int rowIndex, String columnHeader, By innerElementsLocator) {
        log.info("Получаем коллекцию элементов {} из ячейки таблицы '{}' в строке {}, в столбце с названием'{}'", innerElementsLocator, name, rowIndex, columnHeader);
        return getElementsInCell(rowIndex, getColumnIndex(columnHeader), innerElementsLocator);
    }

    /**
     * Возвращает набор веб-элементов в ячейке.
     *
     * @param rowIndex номер строки
     * @param columnIndex номер столбца
     * @param innerElementsLocator локатор для поиска элементов внутри ячейки
     * @return экземпляр {@code Collection} с набором веб-элементов
     */
    public Collection getElementsInCell(int rowIndex, int columnIndex, By innerElementsLocator) {
        log.info("Получаем коллекцию элементов {} из ячейки таблицы '{}' с координатами {},{}", innerElementsLocator, name, rowIndex, columnIndex);
        if (getRowList().isEmpty()) {
            return new Collection(Collections.emptyList(), name, webDriver);
        }
        List<WebElement> innerElements = getRowList().get(rowIndex)
                .findElements(cellLocator)//индекс ячейки равен индексу заголовка столбца
                .get(columnIndex)
                .findElements(innerElementsLocator);
        return new Collection(innerElements, name, webDriver);
    }

    /**
     * Возвращает веб-элемент в ячейке.
     *
     * @param clazz - класс элемента
     * @param rowIndex номер строки
     * @param columnHeader заголовок столбца
     * @param innerElementLocator локатор для поиска элемента внутри ячейки
     * @return веб-элемент типа clazz
     */
    public <T extends BaseWebElement> T getElementInCell(Class<T> clazz, int rowIndex, String columnHeader, By innerElementLocator) {
        log.info("Получаем элемент {} из ячейки таблицы '{}' в строке {}, в столбце с названием'{}'", innerElementLocator, name, rowIndex, columnHeader);
        return getElementInCell(clazz, rowIndex, getColumnIndex(columnHeader), innerElementLocator);
    }

    /**
     * Возвращает веб-элемент в ячейке.
     *
     * @param clazz - класс элемента
     * @param rowIndex номер строки
     * @param columnIndex номер столбца
     * @param innerElementLocator локатор для поиска элемента внутри ячейки
     * @return веб-элемент типа clazz
     */
    public <T extends BaseWebElement> T getElementInCell(Class<T> clazz, int rowIndex, int columnIndex, By innerElementLocator) {
        log.info("Получаем элемент {} из ячейки таблицы '{}' с координатами {},{}", innerElementLocator, name, rowIndex, columnIndex);
        if (getRowList().isEmpty()) {
            throw new UnsupportedOperationException(EMPTY_LIST);
        }
        WebElement innerElement = getRowList().get(rowIndex)
                .findElements(cellLocator)//индекс ячейки равен индексу заголовка столбца
                .get(columnIndex)
                .findElement(innerElementLocator);
        return getElement(clazz, innerElement);
    }

    /**
     * Возвращает веб-элемент из шапки таблицы.
     *
     * @param clazz - класс элемента
     * @param columnIndex номер столбца
     * @param innerElementLocator локатор для поиска элемента внутри ячейки
     * @return веб-элемент типа clazz
     */
    public <T extends BaseWebElement> T getElementInHeader(Class<T> clazz, int columnIndex, By innerElementLocator) {
        log.info("Получаем элемент {} из шапки таблицы '{}' со столбцом {}", innerElementLocator, name, columnIndex);
        if (getHeaderList().isEmpty()) {
            throw new UnsupportedOperationException(EMPTY_LIST);
        }
        WebElement innerElement = getHeaderList().get(columnIndex).findElement(innerElementLocator);
        return getElement(clazz, innerElement);
    }

    /**
     * Возвращает номер строки таблицы, содержащей указанное значение в указанном столбце.
     *
     * @param columnIndex номер столбца
     * @param value искомое значение
     * @return номер строки таблицы
     */
    public int getRowIndex(int columnIndex, String value) {
        log.info("Получаем индекс строки таблицы '{}' для значения '{}' в столбце {}", name, value, columnIndex);
        return getRowList().stream()
                .filter(row -> getCellValue(getRowList().indexOf(row), columnIndex).startsWith(value))
                .mapToInt(getRowList()::indexOf)
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException(
                        String.format("Не найдено строк, содержащих '%s' в столбце %d.", value, columnIndex)));
    }

    /**
     * Возвращает заголовок столбца таблицы, содержащий указанное значение в указанной строке.
     *
     * @param rowIndex номер строки
     * @param value искомое значение
     * @return заголовок столбца таблицы
     */
    public String getColumnHeader(int rowIndex, String value) {
        log.info("Получаем заголовок столбца таблицы '{}' для значения '{}' в строке {}", name, value, rowIndex);
        List<WebElement> cellList = getRowList().get(rowIndex).findElements(cellLocator);
        return cellList.stream()
                .filter(cell -> cell.getText().startsWith(value))
                //индекс ячейки равен индексу заголовка столбца
                .map(cell -> getHeaderList().get(cellList.indexOf(cell)).getText())//индекс ячейки равен индексу заголовка столбца
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException(
                        String.format("Не найдено столбцов, содержащих '%s' в строке %d.", value, rowIndex)));
    }

    /**
     * Возвращает номер столбца таблицы, содержащий указанное значение в указанной строке.
     *
     * @param rowIndex номер строки
     * @param value искомое значение
     * @return номер столбца таблицы
     */
    public int getColumnIndex(int rowIndex, String value) {
        log.info("Получаем индекс столбца таблицы '{}' для значения '{}' в строке {}", name, value, rowIndex);
        return getColumnIndex(getColumnHeader(rowIndex, value));
    }


    /**
     * Возвращает номер столбца таблицы, соответствующий указанному заголовку.
     *
     * @param columnHeader заголовок, соответствующий искомому номеру
     * @return номер столбца таблицы
     */
    public int getColumnIndex(String columnHeader) {
        log.info("Получаем индекс столбца таблицы '{}' с заголовком '{}'", name, columnHeader);
        return getHeaderList().stream()
                .filter(header -> header.getText().startsWith(columnHeader))
                .mapToInt(getHeaderList()::indexOf)
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("Столбец с именем '" + columnHeader + "' не найден."));
    }
}
