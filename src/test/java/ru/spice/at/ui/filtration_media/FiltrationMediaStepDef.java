package ru.spice.at.ui.filtration_media;

import io.qameta.allure.Step;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.extern.log4j.Log4j2;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.interactions.Actions;
import ru.spice.at.api.dto.response.metadata.UsersItem;
import ru.spice.at.common.base_test.AbstractUiStepDef;
import ru.spice.at.common.emuns.dam.Quality;
import ru.spice.at.common.emuns.dam.ImageParameters;
import ru.spice.at.common.emuns.dam.Source;
import ru.spice.at.common.emuns.dam.Status;
import ru.spice.at.common.utils.Assert;
import ru.spice.at.ui.controls.Button;
import ru.spice.at.ui.controls.Label;
import ru.spice.at.ui.pages.spice.dam.*;
import ru.spice.at.ui.pages.spice.dam.blocks.MediaContextMenuBlock;
import ru.spice.at.ui.pages.spice.dam.blocks.MetadataBlock;
import ru.spice.at.ui.pages.spice.dam.modals.MoreFiltersModalPage;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static ru.spice.at.common.constants.TestConstants.EMPTY_VALUE;
import static ru.spice.at.common.utils.Assert.*;

@Log4j2
public class FiltrationMediaStepDef extends AbstractUiStepDef {
    private static final String FILE_LIST_METADATA_XPATH = "//p[text()='%s']/following-sibling::p";

    private final MediaFilesPage mediaFilesPage;
    private final MoreFiltersModalPage moreFiltersModalPage;
    private final Actions actions;

    public FiltrationMediaStepDef(WebDriver webDriver) {
        mediaFilesPage = new MediaFilesPage(webDriver);
        moreFiltersModalPage = new MoreFiltersModalPage(webDriver);
        actions = new Actions(webDriver);
    }

    public <T> void chooseMainMenuFilter(T filter) {
        chooseMainMenuFilter(filter, true);
    }

    @Step("Выбираем фильтр по параметру на главном меню '{filter}'")
    public <T> void chooseMainMenuFilter(T filter, boolean cancelFilters) {
        mediaFilesPage.checkLoadPage();
        mediaFilesPage.getViewModuleButton().click();

        if (cancelFilters) {
            mediaFilesPage.getCancelFiltersButton().click();
        }

        if (filter instanceof Quality) {
            mediaFilesPage.getQualityComboBox().clickSelect(((Quality) filter).getName());
            mediaFilesPage.getBackPopupButton().click();
        } else if (filter instanceof Status) {
            mediaFilesPage.getStatusComboBox().clickSelect(((Status) filter).getName());
            mediaFilesPage.getBackPopupButton().click();
        } else if (filter instanceof Source) {
            mediaFilesPage.getSourceComboBox().clickSelect(((Source) filter).getName());
            mediaFilesPage.getBackPopupButton().click();
        } else if (filter instanceof UsersItem) {
            mediaFilesPage.getAssignerComboBox().clickSelect(((UsersItem) filter).fullName());
        } else {
            throw new RuntimeException("Неверный тип параметра");
        }
        mediaFilesPage.getApplyFiltersButton().click();
        mediaFilesPage.getMediaFilesModuleCollection().waitForElementsSize(1);
    }

    public void chooseMoreMenuFilter(ImageParameters filtration, String value) {
        chooseMoreMenuFilter(Collections.singletonMap(filtration, Collections.singletonList(value)));
    }

    public void chooseMoreMenuFilter(ImageParameters filtration, List<String> value) {
        chooseMoreMenuFilter(Collections.singletonMap(filtration, value));
    }

    @Step("Фильтр по параметру на меню 'Еще фильтры': '{filter}'")
    public void chooseMoreMenuFilter(Map<ImageParameters, List<String>> filter) {
        mediaFilesPage.checkLoadPage();
        mediaFilesPage.getViewModuleButton().click();
        mediaFilesPage.getCancelFiltersButton().click();
        mediaFilesPage.getAllFiltersButton().click();
        moreFiltersModalPage.checkLoadPage();

        filter.forEach((filtration, values) -> {
                    values.forEach(value -> {
                        switch (filtration) {
                            case QUALITY_ID:
                                moreFiltersModalPage.getQualityComboBox().clickSelect(value).actionsSendKeys(Keys.ESCAPE);
                                break;
                            case STATUS_ID:
                                moreFiltersModalPage.getStatusComboBox().clickSelect(value).actionsSendKeys(Keys.ESCAPE);
                                break;
                            case SOURCE_ID:
                                moreFiltersModalPage.getSourceComboBox().clickSelect(value).actionsSendKeys(Keys.ESCAPE);
                                break;
                            case ASSIGNEE_ID:
                                moreFiltersModalPage.getAssigneeComboBox().clickSelect(value);
                                moreFiltersModalPage.getTitleLabel().click();
                                break;
                            case CREATED_BY:
                                moreFiltersModalPage.getCreatedByComboBox().clickSelect(value);
                                moreFiltersModalPage.getTitleLabel().click();
                                break;
                            case MASTER_CATEGORY_ID:
                                moreFiltersModalPage.getCategoryComboBox().clickSelect(value).actionsSendKeys(Keys.ESCAPE);
                                break;
                            case IS_MAIN_IMAGE:
                                moreFiltersModalPage.getIsMainImageComboBox().clickSelect(value);
                                break;
                            case IS_OWN_TRADEMARK:
                                moreFiltersModalPage.getIsOwnTrademarkComboBox().clickSelect(value);
                                break;
                            case IS_COPYRIGHT:
                                moreFiltersModalPage.getIsCopyrightComboBox().clickSelect(value);
                                break;
                            case IS_RAW_IMAGE:
                                moreFiltersModalPage.getIsRawImageComboBox().clickSelect(value);
                                break;
                            case MASTER_SELLER_ID:
                                moreFiltersModalPage.getMasterSellerComboBox().clickSelect(value);
                                moreFiltersModalPage.getTitleLabel().click();
                                break;
                            case SKU:
                                if (value.equals(EMPTY_VALUE)) {
                                    moreFiltersModalPage.getSkuCheckbox().check();
                                } else {
                                    moreFiltersModalPage.getSkuField().setText(value);
                                }
                                break;
                            case PRIORITY:
                                if (value.equals(EMPTY_VALUE)) {
                                    moreFiltersModalPage.getPriorityCheckbox().check();
                                } else {
                                    moreFiltersModalPage.getPriorityField().setText(value);
                                }
                                break;
                            case EXTERNAL_OFFER:
                                moreFiltersModalPage.getOfferField().setText(value);
                                break;
                            case EXTERNAL_TASK_ID:
                                if (value.equals(EMPTY_VALUE)) {
                                    moreFiltersModalPage.getPimsTaskCheckbox().check();
                                } else {
                                    moreFiltersModalPage.getPimsTaskField().setText(value);
                                }
                                break;
                            default:
                                throw new RuntimeException("Неверный параметр фильтрации");
                        }
                    });
                }
        );

        moreFiltersModalPage.getSaveButton().click();
    }

    @Step("Копируем ссылки на файлы")
    public String copyLink(List<Image> images, boolean contextClick) {
        mediaFilesPage.checkLoadPage();
        mediaFilesPage.getMediaFilesModuleCollection().waitForElements().
                chooseElements(images.stream().map(Image::filename).collect(Collectors.toList()));
        if(contextClick) {
            actions.contextClick().build().perform();
        } else {
            mediaFilesPage.getMetadataBlock().<MetadataBlock>checkLoadPage().getMoreActionButton().click();
        }
        Button copyLinkButton = mediaFilesPage.getMediaContextMenuBlock().<MediaContextMenuBlock>checkLoadPage().getCopyLinkButton();
        Assert.equalsTrueParameter(copyLinkButton.getTextValue().contains(String.valueOf(images.size())), "size");
        copyLinkButton.click();
        mediaFilesPage.getCopyLinkLabel().waitForElement();

        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        try {
            return (String) clipboard.getData(DataFlavor.stringFlavor);
        } catch (UnsupportedFlavorException | IOException e) {
            log.error("Не удалось скопировать текст");
            throw new RuntimeException(e);
        }
    }

    @Step("Сброс фильтра по скопированной ссылке")
    public void cancelCopyLink(List<Image> images, boolean cancelButton) {
        mediaFilesPage.checkLoadPage();
        List<Label> labels = mediaFilesPage.getMediaFilesModuleCollection().waitForElementsSize(images.size()).getElementList(Label.class);

        Assert.compareParameters(
                new LinkedList<>(images.stream().map(Image::filename).collect(Collectors.toList())),
                new LinkedList<>(labels.stream().map(Label::getText).collect(Collectors.toList())),
                "названия изображений"
        );

        if (cancelButton) {
            mediaFilesPage.getCancelFiltersButton().click();
        } else {
            mediaFilesPage.getCancelLinkFilterButton().click();
        }

        mediaFilesPage.getMediaFilesModuleCollection().waitForChangeElementsSize(images.size());
    }

    @Step("Выбираем сортировку '{searchTitle}'")
    public void chooseSearchFilter(String searchTitle) {
        mediaFilesPage.checkLoadPage();
        mediaFilesPage.getViewModuleButton().click();
        mediaFilesPage.getSortingComboBox().clickSelect(searchTitle);
    }

    public void checkFiltrationFile(String filename) {
        checkFiltrationFile(Collections.singletonList(filename));
    }

    @Step("Проверяем наличие отфильтрованного изображения '{filenames}'")
    public void checkFiltrationFile(List<String> filenames) {
        mediaFilesPage.getMediaFilesModuleCollection().waitForElementsSize(filenames.size());
        List<Label> images  = mediaFilesPage.getMediaFilesModuleCollection().waitForElements().getElementList(Label.class);
        List<String> actualFilenames = images.stream().map(Label::getText).collect(Collectors.toList());

        Assert.compareParameters(new LinkedList<>(filenames), new LinkedList<>(actualFilenames), "filenames");
    }

    @Step("Проверяем сортировку {count} изображений с первым элементом {firstFilename}")
    public void checkSortFile(String firstFilename, int count) {
        mediaFilesPage.getMediaFilesModuleCollection().waitForElementsSize(count);
        String actualFirstFilename = mediaFilesPage.getMediaFilesModuleCollection().waitForElements().getElement(Label.class, 0).getText();

        Assert.compareParameters(firstFilename, actualFirstFilename, "filename");
    }

    @Step("Меняем отображение файлов на модульное - true / списком - false: '{moduleView}'")
    public void changeView(boolean moduleView) {
        mediaFilesPage.checkLoadPage();
        if (moduleView) {
            mediaFilesPage.getViewModuleButton().click();
            mediaFilesPage.getMediaFilesModuleCollection().waitForElements();
        } else {
            mediaFilesPage.getViewListButton().click();
            mediaFilesPage.getMediaFilesListCollection().waitForElements();
        }
    }

    @Step("Проверяем метадату для отображения списком")
    public void checkMetadataListView(String fileName, String sku, String priority, String masterSellerName, String masterSellerId) {
        mediaFilesPage.checkLoadPage();
        mediaFilesPage.getSearchField().setText(fileName.split("\\.", 2)[0]).sendKeys(Keys.ENTER);
        mediaFilesPage.getMediaFilesListCollection().waitForElements();

        String actualFileName = mediaFilesPage.getMediaFilesListCollection().
                getElement(Label.class, 0, By.xpath(String.format(FILE_LIST_METADATA_XPATH, "Имя файла"))).getText();
        String actualSku = mediaFilesPage.getMediaFilesListCollection().
                getElement(Label.class, 0, By.xpath(String.format(FILE_LIST_METADATA_XPATH, "SKU"))).getText();
        String actualPriority = mediaFilesPage.getMediaFilesListCollection().
                getElement(Label.class, 0, By.xpath(String.format(FILE_LIST_METADATA_XPATH, "Приоритет"))).getText();
        String actualMasterSellerName = mediaFilesPage.getMediaFilesListCollection().
                getElement(Label.class, 0, By.xpath(String.format(FILE_LIST_METADATA_XPATH, "Ретейлер"))).getText();
        String actualExternalOfferId = mediaFilesPage.getMediaFilesListCollection().
                getElement(Label.class, 0, By.xpath(String.format(FILE_LIST_METADATA_XPATH, "Артикул ретейлера"))).getText();
        String actualMasterSellerId = mediaFilesPage.getMediaFilesListCollection().
                getElement(Label.class, 0, By.xpath(String.format(FILE_LIST_METADATA_XPATH, "ID ретейлера"))).getText();

        assertAll(
                () -> compareParameters(fileName, actualFileName, "Название файла"),
                () -> compareParameters(sku, actualSku, "SKU"),
                () -> compareParameters(priority, actualPriority, "Приоритет"),
                () -> compareParameters(masterSellerName, actualMasterSellerName, "Ретейлер"),
                () -> compareParameters("Не заполнено", actualExternalOfferId, "Артикул ретейлера"),
                () -> compareParameters(masterSellerId, actualMasterSellerId, "ID ретейлера")
        );
    }

    @Step("Проверяем первоначальный фильтр по статусам '{statuses}'")
    public void checkFirstFilterStatus(List<Status> statuses, int filteredImagesCount, int allImagesCount) {
        mediaFilesPage.checkLoadPage();
        mediaFilesPage.refresh();
        mediaFilesPage.checkLoadPage();
        mediaFilesPage.getMediaFilesListCollection().waitForElementsSize(filteredImagesCount);

        mediaFilesPage.getStatusComboBox().click();

        List<String> statusesName = statuses.stream().map(Status::getName).collect(Collectors.toList());
        mediaFilesPage.getStatusComboBox().getElementsCollection().getElementList(Label.class).forEach(statusElement -> {
            String classAttribute = statusElement.getElement().findElement(By.xpath("./span[1]")).getAttribute("class");
            if (statusesName.contains(statusElement.getText())) {
                equalsTrueParameter(classAttribute.contains("checked"), "статус не выбран");
            } else {
                equalsFalseParameter(classAttribute.contains("checked"), "статус выбран");
            }
        });

        mediaFilesPage.getBackPopupButton().click();
        mediaFilesPage.getCancelFiltersButton().click();
        mediaFilesPage.getMediaFilesListCollection().waitForElementsSize(allImagesCount);
    }

    public String concatValues(List<String> values) {
        AtomicReference<String> concatValues = new AtomicReference<>(EMPTY_VALUE);
        values.forEach(value -> concatValues.set(concatValues.get() + value + "; "));
        return concatValues.get();
    }

    @AllArgsConstructor
    @Data
    @Accessors(chain = true, fluent = true)
    public static class Image {
        private String filename;
        private String id;
    }
}
