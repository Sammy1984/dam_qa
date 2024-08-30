package ru.spice.at.ui.edit_media;

import io.qameta.allure.Step;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.RandomStringUtils;
import org.awaitility.Awaitility;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.interactions.Actions;
import ru.spice.at.common.base_test.AbstractUiStepDef;
import ru.spice.at.common.emuns.dam.ImageParameters;
import ru.spice.at.common.utils.Assert;
import ru.spice.at.ui.pages.spice.dam.MediaFilesPage;
import ru.spice.at.ui.pages.spice.dam.blocks.MediaContextMenuBlock;
import ru.spice.at.ui.pages.spice.dam.blocks.MetadataBlock;
import ru.spice.at.ui.pages.spice.dam.modals.MultiEditModalPage;

import java.util.*;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Log4j2
public class EditMediaStepDef extends AbstractUiStepDef {
    private final MediaFilesPage mediaFilesPage;
    private final MultiEditModalPage multiEditModalPage;
    private final Actions actions;

    public EditMediaStepDef(WebDriver webDriver) {
        mediaFilesPage = new MediaFilesPage(webDriver);
        multiEditModalPage = new MultiEditModalPage(webDriver);
        actions = new Actions(webDriver);
    }

    @Step("Находим изображение с именем '{filename}'")
    public void searchImage(String filename) {
        mediaFilesPage.checkLoadPage();
        mediaFilesPage.getViewModuleButton().click();
        mediaFilesPage.getSearchField().setText(filename.split("\\.", 2)[0]).sendKeys(Keys.ENTER);
        mediaFilesPage.getMediaFilesModuleCollection().waitForElementsSize(1).clickTo(0);
    }

    @Step("Переходим к странице мультиредактирования изображения")
    public void goToMultiEditLink(List<Image> images, boolean contextClick) {
        mediaFilesPage.checkLoadPage();
        mediaFilesPage.getViewModuleButton().click();
        mediaFilesPage.getMediaFilesModuleCollection().waitForElements().
                chooseElements(images.stream().map(Image::filename).collect(Collectors.toList()));
        if(contextClick) {
            actions.contextClick().build().perform();
            mediaFilesPage.getMediaContextMenuBlock().<MediaContextMenuBlock>checkLoadPage().getEditButton().click();
        } else {
            mediaFilesPage.getMetadataBlock().<MetadataBlock>checkLoadPage().getEditButton().click();
        }
        multiEditModalPage.checkLoadPage();
    }

    public void editImageParameters(ImageParameters parameter, String value) {
        editImageParameters(Collections.singletonMap(parameter, value));
    }

    @Step("Редактируем параметры '{imageParameters}'")
    public void editImageParameters(Map<ImageParameters, String> imageParameters) {
        mediaFilesPage.checkLoadPage();
        mediaFilesPage.getViewModuleButton().click();
        MetadataBlock metadataBlock = mediaFilesPage.getMetadataBlock();
        imageParameters.forEach((k, v) -> {
            Awaitility.given().pollDelay(300, TimeUnit.MILLISECONDS)
                    .pollInterval(1, TimeUnit.SECONDS)
                    .and().timeout(70, TimeUnit.SECONDS)
                    .until(() -> {
                        try {
                            metadataBlock.getEditButton().click();
                            metadataBlock.getCancelButton().waitForElement();
                            return true;
                        } catch (Exception e) {
                            log.info("Ошибка при вводе в скрытое поле");
                            return false;
                        }
                    });
            String actualValue;
            switch (k) {
                case FILENAME: {
                    metadataBlock.getFilenameField().highlightText().setText(v);
                    metadataBlock.getSaveButton().click();
                    metadataBlock.getEditButton().waitForElement();
                    actualValue = metadataBlock.getFilenameField().getTextValue();
                    break;
                }
                case PRIORITY: {
                    metadataBlock.getPriorityField().setText(v);
                    metadataBlock.getSaveButton().click();
                    metadataBlock.getEditButton().waitForElement();
                    actualValue = metadataBlock.getPriorityField().getTextValue();
                    break;
                }
                case QUALITY_ID: {
                    metadataBlock.getQualityComboBox().clickSelect(v);
                    metadataBlock.getSaveButton().click();
                    metadataBlock.getEditButton().waitForElement();
                    actualValue = metadataBlock.getQualityComboBox().getTextValue();
                    break;
                }
                case IS_RAW_IMAGE: {
                    metadataBlock.getIsRawImageComboBox().clickSelect(v);
                    metadataBlock.getSaveButton().click();
                    metadataBlock.getEditButton().waitForElement();
                    actualValue = metadataBlock.getIsRawImageComboBox().getTextValue();
                    break;
                }
                case IS_OWN_TRADEMARK: {
                    metadataBlock.getIsOwnTrademarkComboBox().clickSelect(v);
                    metadataBlock.getSaveButton().click();
                    metadataBlock.getEditButton().waitForElement();
                    actualValue = metadataBlock.getIsOwnTrademarkComboBox().getTextValue();
                    break;
                }
                case IS_MAIN_IMAGE: {
                    metadataBlock.getIsMainImageComboBox().clickSelect(v);
                    metadataBlock.getSaveButton().click();
                    metadataBlock.getEditButton().waitForElement();
                    actualValue = metadataBlock.getIsMainImageComboBox().getTextValue();
                    break;
                }
                case DESCRIPTION: {
                    metadataBlock.getDescriptionField().setText(v);
                    metadataBlock.getSaveButton().click();
                    metadataBlock.getEditButton().waitForElement();
                    actualValue = metadataBlock.getDescriptionField().getTextValue();
                    break;
                }
                case KEYWORDS: {
                    metadataBlock.getTagsField().setText(v).sendKeys(Keys.ENTER);
                    metadataBlock.getSaveButton().click();
                    metadataBlock.getEditButton().waitForElement();
                    actualValue = metadataBlock.getTagsCollection().waitForElementsSize(1).getText(0);
                    break;
                }
                case SKU: {
                    metadataBlock.getSkuField().highlightText().setText(v);
                    metadataBlock.getSaveButton().click();
                    metadataBlock.getEditButton().waitForElement();
                    actualValue = metadataBlock.getSkuField().getTextValue();
                    break;
                }
                case SOURCE_ID: {
                    metadataBlock.getSourceComboBox().clickSelect(v);
                    metadataBlock.getSaveButton().click();
                    metadataBlock.getEditButton().waitForElement();
                    actualValue = metadataBlock.getSourceComboBox().getTextValue();
                    break;
                }
                case ASSIGNEE_ID: {
                    metadataBlock.getAssigneeComboBox().clickSelect(v);
                    metadataBlock.getSaveButton().click();
                    metadataBlock.getEditButton().waitForElement();
                    actualValue = metadataBlock.getAssigneeLabel().getText();
                    break;
                }
                case MASTER_CATEGORY_ID: {
                    metadataBlock.getCategoryComboBox().clickSelect(v);
                    metadataBlock.getSaveButton().click();
                    metadataBlock.getEditButton().waitForElement();
                    actualValue = metadataBlock.getCategoryComboBox().getTextValue();
                    break;
                }
                case STATUS_ID: {
                    metadataBlock.getStatusComboBox().clickSelect(v);
                    metadataBlock.getSaveButton().click();
                    metadataBlock.getEditButton().waitForElement();
                    actualValue = metadataBlock.getStatusComboBox().getTextValue();
                    break;
                }
                case RECEIVED: {
                    metadataBlock.getReceivedField().setText(v);
                    metadataBlock.getSaveButton().click();
                    metadataBlock.getEditButton().waitForElement();
                    actualValue = metadataBlock.getReceivedField().getTextValue();
                    break;
                }
                case IS_COPYRIGHT: {
                    metadataBlock.getIsCopyrightComboBox().clickSelect(v);
                    metadataBlock.getSaveButton().click();
                    metadataBlock.getEditButton().waitForElement();
                    actualValue = metadataBlock.getIsCopyrightComboBox().getTextValue();
                    break;
                }
                default:
                    throw new RuntimeException("Нужный параметр не поддерживается");
            }
            Assert.compareParameters(v, actualValue, k.getName());
        });
    }

    public void multiEditImageParameters(ImageParameters parameter, String value) {
        multiEditImageParameters(Collections.singletonMap(parameter, value));
    }

    @Step("Редактируем параметры '{imageParameters}' для нескольких файлов")
    public void multiEditImageParameters(Map<ImageParameters, String> imageParameters) {
        multiEditModalPage.checkLoadPage();
        imageParameters.forEach((k, v) -> {
            switch (k) {
                case EXTERNAL_TASK_ID: {
                    if (v == null) {
                        multiEditModalPage.getExternalTaskIdCheckBox().check();
                    } else {
                        multiEditModalPage.getExternalTaskIdField().setText(v);
                    }
                    break;
                }
                case MASTER_SELLER: {
                    if (v == null) {
                        multiEditModalPage.getRetailerCheckBox().check();
                    } else {
                        multiEditModalPage.getRetailerButton().click();
                        multiEditModalPage.getRetailerComboBox().inputSelectFirst(v);
                    }
                    break;
                }
                case SKU: {
                    if (v == null) {
                        multiEditModalPage.getSkuCheckBox().check();
                    } else {
                        multiEditModalPage.getSkuField().setText(v);
                    }
                    break;
                }
                case PRIORITY: {
                    if (v == null) {
                        multiEditModalPage.getPriorityCheckBox().check();
                    } else {
                        multiEditModalPage.getPriorityField().setText(v);
                    }
                    break;
                }
                case SOURCE: {
                    if (v == null) {
                        multiEditModalPage.getSourceCheckBox().check();
                    } else {
                        multiEditModalPage.getSourceComboBox().clickSelect(v);
                    }
                    break;
                }
                case STATUS_NAME: {
                    multiEditModalPage.getStatusComboBox().clickSelect(v);
                    break;
                }
                case QUALITY_NAME: {
                    multiEditModalPage.getQualityComboBox().clickSelect(v);
                    break;
                }
                case ASSIGNEE_ID: {
                    if (v == null) {
                        multiEditModalPage.getAssigneeCheckBox().check();
                    } else {
                        multiEditModalPage.getAssigneeComboBox().clickSelect(v);
                    }
                    break;
                }
                case MASTER_CATEGORY: {
                    if (v == null) {
                        multiEditModalPage.getCategoryCheckBox().check();
                    } else {
                        multiEditModalPage.getCategoryComboBox().clickSelect(v);
                    }
                    break;
                }
                case KEYWORDS: {
                    if (v == null) {
                        multiEditModalPage.getKeywordsCheckBox().check();
                    } else {
                        multiEditModalPage.getKeywordsField().setText(v).sendKeys(Keys.ENTER);
                    }
                    break;
                }
                case IS_OWN_TRADEMARK: {
                    if (v == null) {
                        multiEditModalPage.getIsOwnTrademarkCheckBox().check();
                    } else {
                        multiEditModalPage.getIsOwnTrademarkComboBox().clickSelect(v);
                    }
                    break;
                }
                case IS_COPYRIGHT: {
                    if (v == null) {
                        multiEditModalPage.getIsCopyrightCheckBox().check();
                    } else {
                        multiEditModalPage.getIsCopyrightComboBox().clickSelect(v);
                    }
                    break;
                }
                default:
                    throw new RuntimeException("Нужный параметр не поддерживается");
            }
        });
        multiEditModalPage.getSubmitButton().click();
    }

    public void unsuccessfulEditImageParameters(EditImage editImage) {
        unsuccessfulEditImageParameters(Collections.singletonList(editImage));
    }

    @Step("Редактируем параметры с ошибкой '{editImage}'")
    public void unsuccessfulEditImageParameters(List<EditImage> editImage) {
        mediaFilesPage.checkLoadPage();
        mediaFilesPage.getViewModuleButton().click();
        MetadataBlock metadataBlock = mediaFilesPage.getMetadataBlock();
        editImage.forEach(v -> {
            metadataBlock.getEditButton().click();
            List<String> actualMessages;
            switch (v.imageParameter) {
                case FILENAME: {
                    if (v.parameterValue == null) {
                        metadataBlock.getFilenameField().highlightDeleteText();
                    } else {
                        metadataBlock.getFilenameField().highlightText().setText(v.parameterValue);
                    }
                    metadataBlock.getSaveButton().click();
                    actualMessages = Collections.singletonList(metadataBlock.getFilenameMessageLabel().getText());
                    break;
                }
                case PRIORITY: {
                    metadataBlock.getPriorityField().setText(v.parameterValue);
                    metadataBlock.getSaveButton().click();
                    actualMessages = Collections.singletonList(metadataBlock.getPriorityMessageLabel().getText());
                    break;
                }
                case DESCRIPTION: {
                    metadataBlock.getDescriptionField().setText(v.parameterValue);
                    metadataBlock.getSaveButton().click();
                    actualMessages = Collections.singletonList(metadataBlock.getDescriptionMessageLabel().getText());
                    break;
                }
                case KEYWORDS: {
                    int count = Integer.parseInt(v.parameterValue);
                    for (int i = 0; i < count; i++) {
                        metadataBlock.getTagsField().setText(RandomStringUtils.randomAlphabetic(1)).sendKeys(Keys.ENTER);
                    }
                    metadataBlock.getSaveButton().click();
                    actualMessages = Collections.singletonList(metadataBlock.getTagsMessageLabel().getText());
                    break;
                }
                case SKU: {
                    metadataBlock.getSkuField().highlightText().setText(v.parameterValue);
                    metadataBlock.getSaveButton().click();
                    actualMessages = Collections.singletonList(metadataBlock.getSkuMessageLabel().getText());
                    break;
                }
                case STATUS_ID: {
                    metadataBlock.getStatusComboBox().clickSelect(v.parameterValue);
                    metadataBlock.getSaveButton().click();
                    actualMessages = new ArrayList<>();
                    actualMessages.add(metadataBlock.getStatusMessageLabel().getText());
                    actualMessages.add(metadataBlock.getSkuMessageLabel().getText());
                    actualMessages.add(metadataBlock.getPriorityMessageLabel().getText());
                    metadataBlock.getCancelButton().click();
                    break;
                }
                case RECEIVED: {
                    metadataBlock.getReceivedField().setText(v.parameterValue);
                    metadataBlock.getSaveButton().click();
                    actualMessages = Collections.singletonList(metadataBlock.getReceivedMessageLabel().getText());
                    break;
                }
                default:
                    throw new RuntimeException("Нужный параметр не поддерживается");
            }
            Assert.compareParameters(new LinkedList<>(v.messages), new LinkedList<>(actualMessages), v.imageParameter.getName());
        });
    }

    @Data
    @Accessors(chain = true, fluent = true)
    @AllArgsConstructor
    public static class EditImage {
        private ImageParameters imageParameter;
        private String parameterValue;
        private List<String> messages;

        public EditImage(ImageParameters imageParameter, String parameterValue, String message) {
            this.imageParameter = imageParameter;
            this.parameterValue = parameterValue;
            this.messages = Collections.singletonList(message);
        }
    }

    @AllArgsConstructor
    @Data
    @Accessors(chain = true, fluent = true)
    public static class Image {
        private String filename;
        private String id;
    }
}
