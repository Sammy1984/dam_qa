package ru.spice.at.ui.dam_pims_integration.test;

import io.qameta.allure.Feature;
import io.qameta.allure.Issue;
import io.qameta.allure.Story;
import ru.testit.annotations.WorkItemIds;
import io.restassured.response.Response;
import org.testng.annotations.*;
import ru.spice.at.api.dto.response.metadata.filtration.FiltrationResponse;
import ru.spice.at.api.metadata_service.MetadataStepDef;
import ru.spice.at.common.base_test.BaseUiTest;
import ru.spice.at.common.dto.dam.ImageData;
import ru.spice.at.common.emuns.UiCategories;
import ru.spice.at.common.emuns.dam.ImageFormat;
import ru.spice.at.common.listeners.TestAllureListener;
import ru.spice.at.ui.dam_pims_integration.DamPimsIntegrationSettings;
import ru.spice.at.ui.dam_pims_integration.DamPimsIntegrationStepDef;

import java.util.*;
import java.util.stream.Collectors;

import static ru.spice.at.common.emuns.dam.ImageParameters.*;
import static ru.spice.at.common.utils.Assert.*;
import static ru.spice.at.common.utils.FileHelper.createFileFromBytesArray;
import static ru.spice.at.common.utils.ImageHelper.getRandomByteImage;
import static ru.spice.at.common.utils.JsonHelper.getValueFromResponse;

@Feature("DAM PIMS integration")
@Story("PIMSdoc integration")
@Listeners({TestAllureListener.class})
public class DamPimsDocIntegrationTests extends BaseUiTest<DamPimsIntegrationSettings> {
    private DamPimsIntegrationStepDef damPimsIntegrationStepDef;
    private MetadataStepDef metadataStepDef;

    private List<String> filePaths;

    protected DamPimsDocIntegrationTests() {
        super(UiCategories.DAM_PIMS_INTEGRATION);
    }

    @BeforeClass(alwaysRun = true)
    public void beforeClass() {
        damPimsIntegrationStepDef = new DamPimsIntegrationStepDef(getWebDriver());
        metadataStepDef = new MetadataStepDef();
    }

    @BeforeMethod(alwaysRun = true)
    public void beforeMethod() {
        createFileDirection();
        List<ImageData> imageDataList = Arrays.asList(new ImageData(ImageFormat.JPG), new ImageData(ImageFormat.JPG));
        filePaths = imageDataList.stream().map(image -> {
            byte[] bytes = getRandomByteImage(image.getWidth(), image.getHeight(), image.getFormat().getFormatName());
            return downloadPath + createFileFromBytesArray(bytes, downloadPath, image.getFormat().getFormatName()) + "." + image.getFormat().getFormatName();
        }).collect(Collectors.toList());

        damPimsIntegrationStepDef.urlAuthorization(getData().pimsUrl() + getData().taskListEndpoint(), getData().pimsLogin(), getData().pimsEncryptPassword());
    }

    @AfterMethod(alwaysRun = true)
    public void afterMethod() {
        metadataStepDef.deleteMetadata();
        deleteFileDirection();
        metadataStepDef.setAuthToken(null);
    }

    @Issue("SPC-2122")
    @Test(description = "Успешная отправка одного изображения из PIMS", timeOut = 6000000, groups = {"regress"})
    @WorkItemIds({"247361"})
    public void successIntegrationExternalImportImageTest() {
        String taskNumber = damPimsIntegrationStepDef.createTask(getData().createProduct().taskType(), getData().createProduct().taskSubtype(), getData().pimsLogin());
        DamPimsIntegrationStepDef.ProductOffer offer = damPimsIntegrationStepDef.addFirstOffer();
        damPimsIntegrationStepDef.goToDraft(getData().pimsLogin());
        String sku = damPimsIntegrationStepDef.createProduct(getData().createProduct().category(), getData().createProduct().weight(), getData().createProduct().status());
        List<String> filePaths = Collections.singletonList(this.filePaths.get(0));
        damPimsIntegrationStepDef.addImagesToProduct(filePaths);
        damPimsIntegrationStepDef.importPimsProductAndSendImage();

        FiltrationResponse filtrationResponse =
                metadataStepDef.successFiltrationMetadata(Collections.singletonMap(SKUS.getName(), sku), false).extract().as(FiltrationResponse.class);
        compareParameters(filePaths.size(), filtrationResponse.getData().size(), "image size");

        filtrationResponse.getData().forEach(dataItem -> {
            Response response = metadataStepDef.checkMetadata(dataItem.getId());
            assertAll(
                    () -> compareParameters(sku, getValueFromResponse(response, SKU.getPath()), "sku"),
                    () -> contains(sku + "_", getValueFromResponse(response, FILENAME.getPath()), "filename"),
                    () -> compareParameters(getData().createProduct().importType(), getValueFromResponse(response, IMPORT_TYPE.getPath() + ".name"), "import_type"),
                    () -> compareParameters(getData().createProduct().createdBy(), getValueFromResponse(response, CREATED_BY.getPath() + ".full_name"), "created_by"),
                    () -> compareParameters(getData().createProduct().category(), getValueFromResponse(response, MASTER_CATEGORY.getPath() + ".name"), "master_category"),
                    () -> compareParameters(offer.externalOfferId(), getValueFromResponse(response, EXTERNAL_OFFER.getPath() + ".id").toString(), "external_offer"),
                    () -> compareParameters(offer.retailerMaster(), getValueFromResponse(response, MASTER_SELLER.getPath() + ".name"), "master_seller")
                    //todo добавить после закрытия бага
                    //() -> compareParameters(taskNumber, getValueFromResponse(response, EXTERNAL_TASK_ID.getPath()).toString(), "external_task_id")
            );
        });
    }

    @Issue("SPC-2122")
    @Test(description = "Успешная отправка нескольких изображений из PIMS", timeOut = 6000000, groups = {"regress"})
    @WorkItemIds({"247362"})
    public void successIntegrationExternalImportImagesTest() {
        String taskNumber = damPimsIntegrationStepDef.createTask(getData().createProduct().taskType(), getData().createProduct().taskSubtype(), getData().pimsLogin());
        DamPimsIntegrationStepDef.ProductOffer offer = damPimsIntegrationStepDef.addFirstOffer();
        damPimsIntegrationStepDef.goToDraft(getData().pimsLogin());
        String sku = damPimsIntegrationStepDef.createProduct(getData().createProduct().category(), getData().createProduct().weight(), getData().createProduct().status());
        damPimsIntegrationStepDef.addImagesToProduct(filePaths);
        damPimsIntegrationStepDef.importPimsProductAndSendImage();

        FiltrationResponse filtrationResponse =
                metadataStepDef.successFiltrationMetadata(Collections.singletonMap(SKUS.getName(), sku), false).extract().as(FiltrationResponse.class);
        compareParameters(filePaths.size(), filtrationResponse.getData().size(), "image size");

        filtrationResponse.getData().forEach(dataItem -> {
            Response response = metadataStepDef.checkMetadata(dataItem.getId());
            assertAll(
                    () -> compareParameters(sku, getValueFromResponse(response, SKU.getPath()), "sku"),
                    () -> contains(sku + "_", getValueFromResponse(response, FILENAME.getPath()), "filename"),
                    () -> compareParameters(getData().createProduct().importType(), getValueFromResponse(response, IMPORT_TYPE.getPath() + ".name"), "import_type"),
                    () -> compareParameters(getData().createProduct().createdBy(), getValueFromResponse(response, CREATED_BY.getPath() + ".full_name"), "created_by"),
                    () -> compareParameters(getData().createProduct().category(), getValueFromResponse(response, MASTER_CATEGORY.getPath() + ".name"), "master_category"),
                    () -> compareParameters(offer.externalOfferId(), getValueFromResponse(response, EXTERNAL_OFFER.getPath() + ".id").toString(), "external_offer"),
                    () -> compareParameters(offer.retailerMaster(), getValueFromResponse(response, MASTER_SELLER.getPath() + ".name"), "master_seller")
                    //todo добавить после закрытия бага
                    //() -> compareParameters(taskNumber, getValueFromResponse(response, EXTERNAL_TASK_ID.getPath()).toString(), "external_task_id")
            );
        });
    }
}
