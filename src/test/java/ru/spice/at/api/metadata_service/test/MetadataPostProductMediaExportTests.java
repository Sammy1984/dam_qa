package ru.spice.at.api.metadata_service.test;

import io.qameta.allure.Feature;
import io.qameta.allure.Issue;
import io.qameta.allure.Story;
import lombok.Data;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.RandomStringUtils;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.spice.at.api.dto.response.metadata.DictionariesItem;
import ru.spice.at.api.dto.response.metadata.InvalidParamsItem;
import ru.spice.at.api.import_service.ImportServiceStepDef;
import ru.spice.at.api.metadata_service.MetadataSettings;
import ru.spice.at.api.metadata_service.MetadataStepDef;
import ru.spice.at.common.base_test.BaseApiTest;
import ru.spice.at.common.dto.dam.ImageData;
import ru.spice.at.common.dto.dam.grpc.ProductMediaExport;
import ru.spice.at.common.emuns.ApiServices;
import ru.spice.at.common.emuns.dam.ImageFormat;
import ru.spice.at.common.emuns.dam.Status;
import ru.spice.at.common.utils.Assert;
import ru.testit.annotations.WorkItemIds;

import java.util.*;

import static ru.spice.at.common.constants.TestConstants.FILENAME_MASK;
import static ru.spice.at.common.constants.TestConstants.NINETY_NINE;
import static ru.spice.at.common.emuns.dam.ImageParameters.*;
import static ru.spice.at.common.utils.Assert.*;


@Feature("Metadata Service")
@Story("POST product media export")
public class MetadataPostProductMediaExportTests extends BaseApiTest<MetadataSettings> {

    private final MetadataStepDef metadataStepDef;
    private final ImportServiceStepDef importServiceStepDef;
    private final ProductMediaExportData mediaExportData = new ProductMediaExportData();

    private String actualStatusId;

    protected MetadataPostProductMediaExportTests() {
        super(ApiServices.METADATA_SERVICE);
        metadataStepDef = new MetadataStepDef();
        importServiceStepDef = new ImportServiceStepDef();
    }

    @BeforeClass(alwaysRun = true)
    public void beforeClass() {
        actualStatusId = metadataStepDef.getListStatusesMetadata().stream().filter(item -> item.name().equals(Status.ACTUAL.getName())).
                map(DictionariesItem::id).findFirst().orElse(null);

        String id = importServiceStepDef.importRandomImages(new ImageData(ImageFormat.JPG));

        String sku = RandomStringUtils.randomNumeric(3);
        int priority = new Random().nextInt(NINETY_NINE) + 1;
        Map<String, Object> editValues = new HashMap<>() {{
            put(SKU.getName(), sku);
            put(PRIORITY.getName(), priority);
            put(STATUS_ID.getName(), actualStatusId);
        }};

        metadataStepDef.successEditMetadata(id, editValues);
        mediaExportData.id(id).sku(sku).priority(priority);
    }

    //todo выключено для использования параллельного сьюта
    //@AfterClass(alwaysRun = true)
    public void afterMethod() {
        metadataStepDef.deleteMetadata();
    }

    @Test(description = "Успешная выгрузка одного изображения в PIMS", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"282556"})
    public void successProductMediaExportTest() {
        boolean fullReplacement = true;

        metadataStepDef.successPostProductMediaExport(Collections.singletonList(mediaExportData.id()), fullReplacement);
        ProductMediaExport.UpdateProductMedia updateProductMedia = metadataStepDef.pollProductMediaExport(mediaExportData.sku());

        assertAll(
                () -> compareParameters(mediaExportData.sku(), updateProductMedia.getSku(), "sku"),
                () -> compareParameters(fullReplacement, updateProductMedia.getFullReplacement(), "full_replacement"),
                () -> compareParameters(1, updateProductMedia.getMediaList().size(), "size"),
                () -> compareParameters(
                        String.format(FILENAME_MASK, mediaExportData.sku(), mediaExportData.priority(), ImageFormat.JPG.getFormatName()),
                        updateProductMedia.getMediaList().get(0).getFilename(), "filename"),
                () -> notNullOrEmptyParameter(updateProductMedia.getMediaList().get(0).getUrl(), "url")
        );
    }

    @Test(description = "Успешная выгрузка нескольких изображений в PIMS - одинаковые SKU", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"282557"})
    public void successSomeProductMediaExportTest() {
        boolean fullReplacement = false;

        String id = importServiceStepDef.importRandomImages(new ImageData(ImageFormat.JPG));

        int priority = new Random().nextInt(NINETY_NINE) + 1;
        Map<String, Object> editValues = new HashMap<>() {{
            put(SKU.getName(), mediaExportData.sku());
            put(PRIORITY.getName(), priority);
            put(STATUS_ID.getName(), actualStatusId);
        }};

        metadataStepDef.successEditMetadata(id, editValues);
        ProductMediaExportData secondMediaExportData =
                new ProductMediaExportData().id(id).sku(mediaExportData.sku()).priority(priority);

        metadataStepDef.successPostProductMediaExport(Arrays.asList(mediaExportData.id(), secondMediaExportData.id()), fullReplacement);

        ProductMediaExport.UpdateProductMedia updateProductMedia = metadataStepDef.pollProductMediaExport(mediaExportData.sku());
        String filename = String.format(FILENAME_MASK, mediaExportData.sku(), mediaExportData.priority(), ImageFormat.JPG.getFormatName());
        String secondFilename = String.format(FILENAME_MASK, secondMediaExportData.sku(), secondMediaExportData.priority(), ImageFormat.JPG.getFormatName());

        assertAll(
                () -> compareParameters(mediaExportData.sku(), updateProductMedia.getSku(), "sku"),
                () -> compareParameters(fullReplacement, updateProductMedia.getFullReplacement(), "full_replacement"),
                () -> compareParameters(2, updateProductMedia.getMediaList().size(), "size"),
                () -> compareParameters(
                        Arrays.asList(filename, secondFilename),
                        Arrays.asList(updateProductMedia.getMediaList().get(0).getFilename(), updateProductMedia.getMediaList().get(1).getFilename()),
                                "filename"),
                () -> notNullOrEmptyParameter(updateProductMedia.getMediaList().get(0).getUrl(), "url"),
                () -> notNullOrEmptyParameter(updateProductMedia.getMediaList().get(1).getUrl(), "url")
        );
    }

    @Test(description = "Успешная выгрузка нескольких изображений в PIMS - разные SKU", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"282559"})
    public void successSomeOtherProductMediaExportTest() {
        boolean fullReplacement = true;

        String id = importServiceStepDef.importRandomImages(new ImageData(ImageFormat.JPG));

        String sku = RandomStringUtils.randomNumeric(3);
        int priority = new Random().nextInt(NINETY_NINE) + 1;
        Map<String, Object> editValues = new HashMap<>() {{
            put(SKU.getName(), sku);
            put(PRIORITY.getName(), priority);
            put(STATUS_ID.getName(), actualStatusId);
        }};

        metadataStepDef.successEditMetadata(id, editValues);
        ProductMediaExportData secondMediaExportData = new ProductMediaExportData().id(id).sku(sku).priority(priority);

        metadataStepDef.successPostProductMediaExport(Arrays.asList(mediaExportData.id(), secondMediaExportData.id()), fullReplacement);

        ProductMediaExport.UpdateProductMedia updateProductMedia = metadataStepDef.pollProductMediaExport(mediaExportData.sku());

        assertAll(
                () -> compareParameters(mediaExportData.sku(), updateProductMedia.getSku(), "sku"),
                () -> compareParameters(fullReplacement, updateProductMedia.getFullReplacement(), "full_replacement"),
                () -> compareParameters(1, updateProductMedia.getMediaList().size(), "size"),
                () -> compareParameters(
                        String.format(FILENAME_MASK, mediaExportData.sku(), mediaExportData.priority(), ImageFormat.JPG.getFormatName()),
                        updateProductMedia.getMediaList().get(0).getFilename(), "filename"),
                () -> notNullOrEmptyParameter(updateProductMedia.getMediaList().get(0).getUrl(), "url")
        );

        ProductMediaExport.UpdateProductMedia secondUpdateProductMedia = metadataStepDef.pollProductMediaExport(secondMediaExportData.sku());

        assertAll(
                () -> compareParameters(secondMediaExportData.sku(), secondUpdateProductMedia.getSku(), "sku"),
                () -> compareParameters(fullReplacement, secondUpdateProductMedia.getFullReplacement(), "full_replacement"),
                () -> compareParameters(1, secondUpdateProductMedia.getMediaList().size(), "size"),
                () -> compareParameters(
                        String.format(FILENAME_MASK, secondMediaExportData.sku(), secondMediaExportData.priority(), ImageFormat.JPG.getFormatName()),
                        secondUpdateProductMedia.getMediaList().get(0).getFilename(), "filename"),
                () -> notNullOrEmptyParameter(secondUpdateProductMedia.getMediaList().get(0).getUrl(), "url")
        );
    }

    @Test(description = "Неуспешная выгрузка изображения в PIMS - статус не актуальный", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"282563"})
    public void unsuccessfulNotActualProductMediaExportTest() {
        ImageData image = new ImageData(ImageFormat.JPG);
        String id = importServiceStepDef.importRandomImages(image);

        List<InvalidParamsItem> invalidParamsItems =
                metadataStepDef.unsuccessfulPostProductMediaExport(Collections.singletonList(id), false);

        getData().invalidParameterValueActual().name("status");
        getData().invalidParameterValueActual().
                reason(String.format(getData().invalidParameterValueActual().reason(), image.getFilename()));

        getData().invalidParameterValueName().name("status");
        getData().invalidParameterValueName().
                reason(String.format(getData().invalidParameterValueName().reason(), image.getFilename()));

        Assert.compareParameters(
                Arrays.asList(getData().invalidParameterValueActual(), getData().invalidParameterValueName()),
                invalidParamsItems, "ошибки");
    }

    @Issue("SPC-3530")
    @Test(description = "Неуспешная выгрузка изображения в PIMS - пустой список", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"282565"})
    public void unsuccessfulEmptyProductMediaExportTest() {
        List<InvalidParamsItem> invalidParamsItems =
                metadataStepDef.unsuccessfulPostProductMediaExport(Collections.emptyList(), true);
        getData().invalidParams().get(9).name("data");
        Assert.compareParameters(Collections.singletonList(getData().invalidParams().get(9)), invalidParamsItems, "ошибки");
    }

    @Test(description = "Неуспешная выгрузка изображения в PIMS - несуществующий файл", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"282571"})
    public void unsuccessfulNotExistProductMediaExportTest() {
        List<InvalidParamsItem> invalidParamsItems =
                metadataStepDef.unsuccessfulPostProductMediaExport(Collections.singletonList(UUID.randomUUID().toString()), true);
        getData().invalidNotFoundParam().name("ids");
        Assert.compareParameters(Collections.singletonList(getData().invalidNotFoundParam()), invalidParamsItems, "ошибки");
    }

    @Test(description = "Неуспешная выгрузка изображения в PIMS - невалидный id", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"282574"})
    public void unsuccessfulInvalidProductMediaExportTest() {
        List<InvalidParamsItem> invalidParamsItems = metadataStepDef.unsuccessfulPostProductMediaExport(
                Collections.singletonList(RandomStringUtils.randomAlphabetic(10)), true);
        getData().invalidBodyStringParam().name("data");
        Assert.compareParameters(Collections.singletonList(getData().invalidBodyStringParam()), invalidParamsItems, "ошибки");
    }

    @Test(description = "Неуспешная выгрузка изображения в PIMS - нет full_replacement", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"282577"})
    public void unsuccessfulNoFullReplacementProductMediaExportTest() {
        List<InvalidParamsItem> invalidParamsItems = metadataStepDef.unsuccessfulPostProductMediaExport(
                Collections.singletonList(mediaExportData.id()), null);
        getData().invalidEmptyBodyStringParam().name("full_replacement");
        Assert.compareParameters(Collections.singletonList(getData().invalidEmptyBodyStringParam()), invalidParamsItems, "ошибки");
    }

    @Test(description = "Неуспешная выгрузка изображения в PIMS - невалидный full_replacement", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"282578"})
    public void unsuccessfulInvalidFullReplacementProductMediaExportTest() {
        List<InvalidParamsItem> invalidParamsItems = metadataStepDef.unsuccessfulPostProductMediaExport(
                Collections.singletonList(mediaExportData.id()), RandomStringUtils.randomAlphabetic(7));
        getData().invalidBodyStringParam().name("full_replacement");
        Assert.compareParameters(Collections.singletonList(getData().invalidBodyStringParam()), invalidParamsItems, "ошибки");
    }

    @Data
    @Accessors(chain = true, fluent = true)
    private static class ProductMediaExportData {
        private String sku;
        private int priority;
        private String id;
    }
}
