package ru.spice.at.ui.pages.spice.dam;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import ru.spice.at.ui.annotations.Mandatory;
import ru.spice.at.ui.annotations.Name;
import ru.spice.at.ui.controls.*;
import ru.spice.at.ui.pages.spice.DamBasePage;
import ru.spice.at.ui.pages.spice.dam.blocks.MediaContextMenuBlock;
import ru.spice.at.ui.pages.spice.dam.blocks.MetadataBlock;

import java.util.List;

/**
 * Главная страница DAM Медиафайлы
 */
@Name("Медиафайлы")
public class MediaFilesPage extends DamBasePage {

    public MediaFilesPage(WebDriver webDriver) {
        super(webDriver);
    }

    @Mandatory
    @FindBy(xpath = "//*[contains(@class, 'MuiTypography')]")
    private WebElement title;

    @FindBy(xpath = "//button[text()='Добавить файлы']")
    private WebElement importFile;

    @FindBy(xpath = "//input[@placeholder='Поиск']")
    private WebElement searchField;

    @FindBy(xpath = "//input[@placeholder='Поиск']/..//button")
    private WebElement cancelSearchButton;

    @FindBy(xpath = "//form//p[text()='Качество']/ancestor::button")
    private WebElement quality;

    @FindBy(xpath = "//form//p[contains(text(),'Статус')]/ancestor::button")
    private WebElement status;

    @FindBy(xpath = "//form//*[text()='Источник']/ancestor::button")
    private WebElement source;

    @FindBy(xpath = "//form//*[text()='Исполнитель']/ancestor::button")
    private WebElement assigner;

    @FindBy(xpath = "//div[contains(@class, 'MuiFormGroup')]//label[contains(@class, 'MuiFormControlLabel')]")
    private List<WebElement> selectFiltrationItems;

    @FindBy(xpath = "//div[@aria-haspopup='listbox']")
    private WebElement sorting;

    @FindBy(xpath = "//ul[contains(@class, 'MuiList-root')]/li[contains(@class, 'MuiButtonBase')]")
    private List<WebElement> selectSortingItems;

    @FindBy(xpath = "//form//button[contains(text(), 'Все фильтры')]")
    private WebElement allFilters;

    @FindBy(xpath = "//form//p[text()='Файлы по ссылке']/..//div[@data-qa='clearBox']")
    private WebElement cancelLinkFilter;

    @FindBy(xpath = "//form//button[text()='Применить']")
    private WebElement applyFilters;

    @FindBy(xpath = "//form//button[text()='Сбросить']")
    private WebElement applyCancel;

    @FindBy(xpath = "//p[contains(text(), 'Выбрано:')]")
    private WebElement selectedItemsHeader;

    @FindBy(xpath = "//div[@aria-hidden='true' and contains(@class, 'MuiBackdrop') and contains(@class, 'MuiModal')]")
    private WebElement backPopup;

    @FindBy(xpath = "//p[contains(text(), 'Выбрано:')]/../..//button")
    private WebElement cleanSelect;

    @FindBy(xpath = "//div[@role='presentation']//p[contains(text(),'Ссылка скопирована')]")
    private WebElement copyLinkMassage;

    @FindBy(xpath = "//div[contains(@class, 'MuiBox-root')]//div[contains(@class, 'MuiCard-root')]")
    private List<WebElement> mediaFilesModule;

    @FindBy(xpath = "//div[@data-qa='grid-container']//*[name()='img']/../..")
    private List<WebElement> mediaFilesList;

    @FindBy(xpath = "//*[@id='filled/action/view-module']/../parent::button")
    private WebElement viewModule;

    @FindBy(xpath = "//*[@id='filled/action/view-list']/../parent::button")
    private WebElement viewList;

    public Button getImportFileButton() {
        return new Button(importFile, "Загрузка медиа - кнопка", getWebDriver());
    }

    public Field getSearchField() {
        return new Field(searchField, "Поиск - поле", getWebDriver());
    }

    public Button getCancelSearchButton() {
        return new Button(cancelSearchButton, "Отменить поиск", getWebDriver());
    }

    public ComboBox getQualityComboBox() {
        return new ComboBox(quality, selectFiltrationItems, "Качество", getWebDriver());
    }

    public ComboBox getStatusComboBox() {
        return new ComboBox(status, selectFiltrationItems, "Статус", getWebDriver());
    }

    public ComboBox getSourceComboBox() {
        return new ComboBox(source, selectFiltrationItems, "Источник", getWebDriver());
    }

    public ComboBox getAssignerComboBox() {
        return new ComboBox(assigner, selectFiltrationItems, "Исполнитель", getWebDriver());
    }

    public ComboBox getSortingComboBox() {
        return new ComboBox(sorting, selectSortingItems, "Сортировка", getWebDriver());
    }

    public Button getAllFiltersButton() {
        return new Button(allFilters, "Все фильтры", getWebDriver());
    }

    public Button getCancelLinkFilterButton() {
        return new Button(cancelLinkFilter, "Сбросить фильтр по ссылке (крестик)", getWebDriver());
    }

    public Button getApplyFiltersButton() {
        return new Button(applyFilters, "Применить", getWebDriver());
    }

    public Button getCancelFiltersButton() {
        return new Button(applyCancel, "Сбросить", getWebDriver());
    }

    public Label getSelectedItemsHeaderLabel() {
        return new Label(selectedItemsHeader, "Выбрано файлов", getWebDriver());
    }

    public Button getCleanSelectButton() {
        return new Button(cleanSelect, "Очистить выбор", getWebDriver());
    }

    public Button getBackPopupButton() {
        return new Button(backPopup, "Скрыть попап", getWebDriver());
    }

    public Label getCopyLinkLabel() {
        return new Label(copyLinkMassage, "Сообщение - Ссылка скопирована", getWebDriver());
    }

    public Collection getMediaFilesModuleCollection() {
        return new Collection(mediaFilesModule, "Медиафайлы - модули", getWebDriver());
    }

    public Collection getMediaFilesListCollection() {
        return new Collection(mediaFilesList, "Медиафайлы - список", getWebDriver());
    }

    public Button getViewModuleButton() {
        return new Button(viewModule, "Отображение модулями", getWebDriver());
    }

    public Button getViewListButton() {
        return new Button(viewList, "Отображение списком", getWebDriver());
    }

    public MetadataBlock getMetadataBlock() {
        return new MetadataBlock(getWebDriver());
    }

    public MediaContextMenuBlock getMediaContextMenuBlock() {
        return new MediaContextMenuBlock(getWebDriver());
    }
}
