package ru.spice.at.ui.dam_pims_integration;

import io.qameta.allure.Step;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.RandomStringUtils;
import org.awaitility.Awaitility;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.html5.LocalStorage;
import org.openqa.selenium.html5.WebStorage;
import org.openqa.selenium.remote.Augmenter;
import ru.spice.at.common.base_test.AbstractUiStepDef;
import ru.spice.at.common.utils.Assert;
import ru.spice.at.common.utils.CipherHelper;
import ru.spice.at.ui.controls.CheckBox;
import ru.spice.at.ui.controls.Label;
import ru.spice.at.ui.pages.spice.pims.*;
import ru.spice.at.ui.pages.spice.pims.modals.CreateTaskPimsModalPage;
import ru.spice.at.ui.pages.spice.pims.modals.InfoPimsModalPage;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

@Log4j2
public class DamPimsIntegrationStepDef extends AbstractUiStepDef {
    private final AuthorizationPimsPage authorizationPimsPage;
    private final TaskPimsPage taskPage;
    private final OffersPimsPage offersPage;
    private final InfoPimsModalPage infoPimsModalPage;
    private final TasksListPimsPage tasksListPimsPage;
    private final CreateTaskPimsModalPage createTaskPimsModalPage;
    private final ProductCreationPimsPage productCreationPimsPage;
    private final WebDriver webDriver;

    private boolean isLogin = false;

    public DamPimsIntegrationStepDef(WebDriver webDriver) {
        this.webDriver = webDriver;
        authorizationPimsPage = new AuthorizationPimsPage(webDriver);
        taskPage = new TaskPimsPage(webDriver);
        offersPage = new OffersPimsPage(webDriver);
        infoPimsModalPage = new InfoPimsModalPage(webDriver);
        tasksListPimsPage = new TasksListPimsPage(webDriver);
        createTaskPimsModalPage = new CreateTaskPimsModalPage(webDriver);
        productCreationPimsPage = new ProductCreationPimsPage(webDriver);
    }

    public void urlAuthorization(String url, String login, String encryptPass) {
        urlAuthorization(url, login, encryptPass, false);
    }

    @Step("Авторизуемся в PIMS по url: {url}")
    public void urlAuthorization(String url, String login, String encryptPass, boolean addToggle) {
        authorizationPimsPage.goToPage(url);

        if (addToggle) {
            WebStorage webStorage = (WebStorage) new Augmenter().augment(webDriver);
            LocalStorage localStorage = webStorage.getLocalStorage();
            localStorage.setItem("__PIMS_features", "{\"pimsMediaDebug\":true}");
            webDriver.navigate().refresh();
        }

        if (!isLogin) {
            authorizationPimsPage.checkLoadPage();
            authorizationPimsPage.getUsernameField().setText(login);
            authorizationPimsPage.getPasswordField().setText(CipherHelper.decrypt(encryptPass));
            authorizationPimsPage.getSubmitButton().click();
            isLogin = true;
        }
    }

    @Step("Добавляем офер '{offerId}' к задаче '{taskId}'")
    public void addOffers(String taskId, String offerId) {
        String title = taskPage.<TaskPimsPage>checkLoadPage().getTitleLabel().getText();
        Assert.compareParameters(taskId, title, "номер задачи");

        deleteOffers();
        taskPage.getAddOffersButton().click();

        offersPage.<OffersPimsPage>checkLoadPage().getOffersCollection().waitForElements();
        offersPage.getOfferIdButton().click();
        offersPage.getOfferIdInput().setText(offerId);
        offersPage.getOfferIdSearchButton().click();

        offersPage.getOffersCollection().waitForElementsSize(1);
        offersPage.getSelectAllOffersCheckBox().check();
        offersPage.getAddOffersButton().click();

        infoPimsModalPage.<InfoPimsModalPage>checkLoadPage().getSubmitButton().click();
        taskPage.checkLoadPage();
    }

    @Step("Удаляем офера у текущей задачи")
    public void deleteOffers() {
        if (taskPage.getOffersCollection().getSize() > 0) {
            taskPage.checkLoadPage();
            taskPage.getSelectAllOffersCheckBox().check();
            taskPage.getDeleteOffersButton().click();

            infoPimsModalPage.<InfoPimsModalPage>checkLoadPage().getSubmitButton().click();

            taskPage.checkLoadPage();
            taskPage.getOffersCollection().waitForEmpty();
        }
    }

    @Step("Создаем задачу '{taskType}' с исполнителем '{executor}' и переходим в нее")
    public String createTask(String taskType, String taskSubtype, String executor) {
        tasksListPimsPage.checkLoadPage();
        tasksListPimsPage.getCreateTaskButton().click();

        createTaskPimsModalPage.checkLoadPage();
        createTaskPimsModalPage.getModalTypeComboBox().clickSelect(taskType);
        createTaskPimsModalPage.getSubmitButton().click();

        createTaskPimsModalPage.checkLoadPage();
        createTaskPimsModalPage.getAddModalExecutorsButton().click();
        createTaskPimsModalPage.getModalExecutorsSearchComboBox().inputSelectFirst(executor);
        createTaskPimsModalPage.getModalSubtypeComboBox().clickSelect(taskSubtype);
        createTaskPimsModalPage.getSubmitButton().click();

        taskPage.checkLoadPage();
        return taskPage.getTitleLabel().getText();
    }

    @Step("Добавляем первый офер к задаче")
    public ProductOffer addFirstOffer() {
        taskPage.checkLoadPage();
        taskPage.getAddOffersButton().click();

        offersPage.<OffersPimsPage>checkLoadPage().getOffersCollection().waitForElements();

        offersPage.getOffersCollection().getElement(CheckBox.class, 0, By.xpath(".//input/../..")).check();
        String externalOfferId = offersPage.getOffersCollection().
                getElement(Label.class, 0, By.xpath(".//div[@data-name='external_offer_id']")).getText();
        String retailerMaster = offersPage.getOffersCollection().
                getElement(Label.class, 0, By.xpath(".//div[@data-name='retailer_master']")).getText();
        offersPage.getAddOffersButton().click();

        infoPimsModalPage.<InfoPimsModalPage>checkLoadPage().getSubmitButton().click();
        return new ProductOffer().externalOfferId(externalOfferId).retailerMaster(retailerMaster);
    }

    @Step("Переходим к черновику")
    public void goToDraft(String executor) {
        taskPage.checkLoadPage();
        taskPage.getExecutorButton().dblClick();
        taskPage.checkLoadPage();
        taskPage.getExecutorButton().click();
        taskPage.getParameterComboBox().inputSelectFirst(executor);

        taskPage.getStartExecutionButton().click();

        infoPimsModalPage.checkLoadPage();
        infoPimsModalPage.getYesButton().click();

        taskPage.checkLoadPage();
        Awaitility.given().pollDelay(10, TimeUnit.SECONDS)
                .pollInterval(1, TimeUnit.SECONDS)
                .and().timeout(3, TimeUnit.MINUTES)
                .await("Ожидание элемента")
                .until(() -> {
                    try {
                        taskPage.refresh();
                        taskPage.getDraftsButton().click();
                        return true;
                    }
                    catch (Exception e) {
                        log.info("Ошибка при ожидании элемента");
                        return false;
                    }
                });
        taskPage.getDraftEmployeeLink().click();
        taskPage.switchTo(1);
        productCreationPimsPage.checkLoadPage();
    }

    @Step("Заполняем черновик (создаем продукт)")
    public String createProduct(String category, String weight, String status) {
        productCreationPimsPage.checkLoadPage();

        productCreationPimsPage.getOfferIdButton().click();
        IntStream.range(0, 14).forEach(i -> productCreationPimsPage.getOfferIdButton().getActions().sendKeys(Keys.ARROW_RIGHT).perform());

        productCreationPimsPage.getStatusButton().dblClick();
        productCreationPimsPage.getParameterComboBox().inputSelectFirst(status);
        IntStream.range(0, 5).forEach(i -> productCreationPimsPage.getStatusButton().getActions().sendKeys(Keys.ARROW_RIGHT).perform());

        String sku = productCreationPimsPage.getSkuButton().getTextValue();

        productCreationPimsPage.getMasterCategoryButton().dblClick();
        productCreationPimsPage.getParameterComboBox().inputSelectFirst(category);

        productCreationPimsPage.getUpdateButton().click();
        productCreationPimsPage.getCategoryCharacteristicsButton().click();
        productCreationPimsPage.getCategoryCharacteristicsUpdateButton().click();
        productCreationPimsPage.checkLoadPage();

        productCreationPimsPage.getOfferIdButton().waitForElement();
        productCreationPimsPage.getOfferIdButton().click();
        IntStream.range(0, 21).forEach(i -> productCreationPimsPage.getOfferIdButton().getActions().sendKeys(Keys.ARROW_RIGHT).perform());

        productCreationPimsPage.getProductNameButton().click();
        productCreationPimsPage.getMainParameterField().setText(RandomStringUtils.randomAlphabetic(5));
        productCreationPimsPage.getProductNameButton().getActions().sendKeys(Keys.ENTER).perform();
        productCreationPimsPage.getProductNameButton().click();
        IntStream.range(0, 15).forEach(i -> productCreationPimsPage.getProductNameButton().getActions().sendKeys(Keys.ARROW_RIGHT).perform());

        productCreationPimsPage.getProductUnitButton().dblClick();
        productCreationPimsPage.getParameterComboBox().clickSelectFirst();

        productCreationPimsPage.getNetWeightButton().click();
        productCreationPimsPage.getMainParameterField().setText(weight);
        productCreationPimsPage.getNetWeightButton().getActions().sendKeys(Keys.ENTER).perform();
        productCreationPimsPage.getNetWeightButton().click();

        productCreationPimsPage.getGrossWeightButton().click();
        productCreationPimsPage.getMainParameterField().setText(weight);
        productCreationPimsPage.getGrossWeightButton().getActions().sendKeys(Keys.ENTER).perform();
        productCreationPimsPage.getGrossWeightButton().click();
        IntStream.range(0, 5).forEach(i -> productCreationPimsPage.getGrossWeightButton().getActions().sendKeys(Keys.ARROW_RIGHT).perform());

        productCreationPimsPage.getGenderButton().dblClick();
        productCreationPimsPage.getParameterComboBox().clickSelectFirst();
        productCreationPimsPage.getGenderButton().click().getActions().sendKeys(Keys.ENTER).perform();
        IntStream.range(0, 25).forEach(i -> productCreationPimsPage.getGenderButton().getActions().sendKeys(Keys.ARROW_RIGHT).perform());

        productCreationPimsPage.getTypeButton().dblClick();
        productCreationPimsPage.getParameterComboBox().clickSelectFirst();

        productCreationPimsPage.getWeightButton().dblClick();
        productCreationPimsPage.getParameterComboBox().clickSelectFirst();

        return sku;
    }

    @Step("Добавляем изображения для продукта")
    public void addImagesToProduct(List<String> imagePaths) {
        productCreationPimsPage.checkLoadPage();

        productCreationPimsPage.getMediaButton().click();

        imagePaths.forEach(imagePath -> productCreationPimsPage.getHiddenAddField().setHiddenText(imagePath));

        productCreationPimsPage.checkLoadPage();
        productCreationPimsPage.getMediaUploadLabel().waitForElement();
        productCreationPimsPage.getSaveDraftButton().click();
    }

    @Step("Импорт продукта в PIMS и отправка изображения")
    public void importPimsProductAndSendImage() {
        productCreationPimsPage.checkLoadPage();
        productCreationPimsPage.getImportInPimsButton().click();
        productCreationPimsPage.getSaveButton().click();
        productCreationPimsPage.checkLoadPage();

        productCreationPimsPage.switchTo(0);


        taskPage.<TaskPimsPage>checkLoadPage().getSubmitForReviewButton().click();
        infoPimsModalPage.<InfoPimsModalPage>checkLoadPage().getYesButton().click();

        taskPage.<TaskPimsPage>checkLoadPage().getStartCheckingButton().click();
        infoPimsModalPage.<InfoPimsModalPage>checkLoadPage().getYesButton().click();

        taskPage.<TaskPimsPage>checkLoadPage().getSendForAcceptanceButton().click();
        infoPimsModalPage.<InfoPimsModalPage>checkLoadPage().getYesButton().click();

        taskPage.<TaskPimsPage>checkLoadPage().getAcceptTaskButton().click();
        infoPimsModalPage.<InfoPimsModalPage>checkLoadPage().getCheckbox().check();
        infoPimsModalPage.getYesButton().click();
        taskPage.checkLoadPage();
    }

    @Data
    @Accessors(chain = true, fluent = true)
    public static class ProductOffer {
        private String externalOfferId;
        private String retailerMaster;
    }
}