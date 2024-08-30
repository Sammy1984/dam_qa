package ru.spice.at.api.export_service.test;

import com.adobe.internal.xmp.XMPMeta;
import com.adobe.internal.xmp.impl.XMPMetaImpl;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import ru.testit.annotations.WorkItemIds;
import io.restassured.response.Response;
import lombok.SneakyThrows;
import org.apache.commons.lang3.RandomStringUtils;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import ru.spice.at.api.dto.response.metadata.DictionariesItem;
import ru.spice.at.api.dto.response.metadata.UsersItem;
import ru.spice.at.api.export_service.ExportServiceSettings;
import ru.spice.at.api.export_service.ExportServiceStepDef;
import ru.spice.at.api.import_service.ImportServiceStepDef;
import ru.spice.at.api.metadata_service.MetadataStepDef;
import ru.spice.at.common.base_test.BaseApiTest;
import ru.spice.at.common.dto.dam.ImageData;
import ru.spice.at.common.emuns.ApiServices;
import ru.spice.at.common.emuns.dam.ImageFormat;
import ru.spice.at.common.emuns.dam.Source;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

import static com.adobe.internal.xmp.XMPConst.*;
import static ru.spice.at.common.constants.TestConstants.NINETY_NINE;
import static ru.spice.at.common.emuns.dam.ImageParameters.*;
import static ru.spice.at.common.utils.Assert.compareParameters;
import static ru.spice.at.common.utils.FileHelper.createFileFromBytesArray;
import static ru.spice.at.common.utils.ImageHelper.getRandomByteImage;
import static ru.spice.at.common.utils.JsonHelper.getValueFromResponse;
import static ru.spice.at.common.utils.XmpUtil.getXMPFromBytes;
import static ru.spice.at.common.utils.XmpUtil.writeXMPMeta;

@Deprecated
@Feature("Export Service")
@Story("POST export media with metadata XMP")
public class ExportServiceMetadataXMPTests extends BaseApiTest<ExportServiceSettings> {
    private ExportServiceStepDef exportServiceStepDef;
    private ImportServiceStepDef importServiceStepDef;
    private MetadataStepDef metadataStepDef;

    protected ExportServiceMetadataXMPTests() {
        super(ApiServices.EXPORT_SERVICE);
    }

    @BeforeClass(description = "Добавляем файлы в систему", alwaysRun = true)
    public void beforeClass() {
        createFileDirection();
        exportServiceStepDef = new ExportServiceStepDef();
        metadataStepDef = new MetadataStepDef();
        importServiceStepDef = new ImportServiceStepDef();
    }

    @AfterClass(alwaysRun = true)
    public void afterClass() {
        deleteFileDirection();
        importServiceStepDef.deleteMedia();
        metadataStepDef.deleteMetadata();
    }

    //@Test(description = "Экспорт файла jpeg с несколькими заполненными полями XMP", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"242456"})
    public void successExportXmpJpegTest() {
        ImageData image = new ImageData(ImageFormat.JPEG);
        byte[] bytes = getRandomByteImage(image.getWidth(), image.getHeight(), image.getFormat().getFormatName());
        List<String> idList = importServiceStepDef.importImages(Collections.singletonMap(image.getFilename(), bytes));

        Response metadataResponse = metadataStepDef.checkMetadata(idList.get(0));

        byte[] resultBytes = exportServiceStepDef.exportImage(Collections.singletonList(idList.get(0)));
        XMPMeta metadataXmp = getXMPFromBytes(resultBytes);
        exportServiceStepDef.checkXMP(metadataXmp, metadataResponse, false);
    }

    //@Test(description = "Экспорт файла jpg с несколькими заполненными полями XMP", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"242457"})
    public void successExportXmpJpgTest() {
        ImageData image = new ImageData(ImageFormat.JPG);
        byte[] bytes = getRandomByteImage(image.getWidth(), image.getHeight(), image.getFormat().getFormatName());
        List<String> idList = importServiceStepDef.importImages(Collections.singletonMap(image.getFilename(), bytes));

        Response metadataResponse = metadataStepDef.checkMetadata(idList.get(0));

        byte[] resultBytes = exportServiceStepDef.exportImage(Collections.singletonList(idList.get(0)));
        XMPMeta metadataXmp = getXMPFromBytes(resultBytes);
        exportServiceStepDef.checkXMP(metadataXmp, metadataResponse, false);
    }

    //@Test(description = "Экспорт файла png с несколькими заполненными полями XMP", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"242458"})
    public void successExportXmpPngTest() {
        ImageData image = new ImageData(ImageFormat.PNG);
        byte[] bytes = getRandomByteImage(image.getWidth(), image.getHeight(), image.getFormat().getFormatName());
        List<String> idList = importServiceStepDef.importImages(Collections.singletonMap(image.getFilename(), bytes));

        Response metadataResponse = metadataStepDef.checkMetadata(idList.get(0));

        byte[] resultBytes = exportServiceStepDef.exportImage(Collections.singletonList(idList.get(0)));
        XMPMeta metadataXmp = getXMPFromBytes(resultBytes);
        exportServiceStepDef.checkXMP(metadataXmp, metadataResponse, NS_PNG, false);
    }

    //@Test(description = "Экспорт файла сo всеми заполненными полями XMP", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"242460"})
    public void successExportAllXmpTest() {
        ImageData image = new ImageData(ImageFormat.JPEG);
        byte[] bytes = getRandomByteImage(image.getWidth(), image.getHeight(), image.getFormat().getFormatName());
        List<String> idList = importServiceStepDef.importImages(Collections.singletonMap(image.getFilename(), bytes));

        String assigneeId = metadataStepDef.getListUsersMetadata().stream().map(UsersItem::id).findFirst().orElse(null);
        String categoryId = metadataStepDef.getListCategoriesMetadata().stream().map(DictionariesItem::id).findFirst().orElse(null);
        String goodSourceId = metadataStepDef.getListSourcesMetadata().stream().filter(item -> item.name().equals(Source.BRAND.getName())).
                map(DictionariesItem::id).findFirst().orElse(null);

        Map<String, Object> editValues = new HashMap<String, Object>() {{
            put(ASSIGNEE_ID.getName(), assigneeId);
            put(DESCRIPTION.getName(), RandomStringUtils.randomAlphabetic(12));
            put(EXTERNAL_TASK_ID.getName(), String.valueOf(new Random().nextInt(10000)));
            put(KEYWORDS.getName(), Arrays.asList(RandomStringUtils.randomAlphabetic(5), RandomStringUtils.randomAlphabetic(7)));
            put(MASTER_CATEGORY_ID.getName(), categoryId);
            put(PRIORITY.getName(), new Random().nextInt(NINETY_NINE) + 1);
            put(RECEIVED.getName(), RandomStringUtils.randomAlphabetic(6));
            put(SKU.getName(), RandomStringUtils.randomAlphabetic(6));
            put(SOURCE_ID.getName(), goodSourceId);
        }};

        Response metadataResponse = metadataStepDef.successEditMetadata(idList.get(0), editValues);

        byte[] resultBytes = exportServiceStepDef.exportImage(Collections.singletonList(idList.get(0)));
        XMPMeta metadataXmp = getXMPFromBytes(resultBytes);
        exportServiceStepDef.checkXMP(metadataXmp, metadataResponse, true);
    }

    @SneakyThrows
    //@Test(description = "Экспорт нескольких файлов с несколькими заполненными полями XMP", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"242461"})
    public void successExportXmpImagesTest() {
        ImageData firstImage = new ImageData(ImageFormat.JPEG);
        byte[] firstBytes = getRandomByteImage(firstImage.getWidth(), firstImage.getHeight(), firstImage.getFormat().getFormatName());
        ImageData secondImage = new ImageData(ImageFormat.JPEG);
        byte[] secondBytes = getRandomByteImage(secondImage.getWidth(), secondImage.getHeight(), secondImage.getFormat().getFormatName());

        Map<String, byte[]> images = new HashMap<String, byte[]>() {{
            put(firstImage.getFilename(), firstBytes);
            put(secondImage.getFilename(), secondBytes);
        }};
        List<String> idList = importServiceStepDef.importImages(images);

        Response firstMetadataResponse = metadataStepDef.checkMetadata(idList.get(0));
        Response secondMetadataResponse = metadataStepDef.checkMetadata(idList.get(1));

        byte[] bytesZip = exportServiceStepDef.exportImage(idList);
        List<byte[]> imagesByte = exportServiceStepDef.getFilesBytesFromZip(bytesZip, downloadPath, Arrays.asList(firstImage.getFilename(), secondImage.getFilename()));

        XMPMeta metadataXmp = getXMPFromBytes(imagesByte.get(0));

        if (metadataXmp.getProperty(NS_JPEG, FILENAME.getName()).getValue().equals(getValueFromResponse(firstMetadataResponse, FILENAME.getPath()))) {
            exportServiceStepDef.checkXMP(metadataXmp, firstMetadataResponse, false);

            metadataXmp = getXMPFromBytes(imagesByte.get(1));
            exportServiceStepDef.checkXMP(metadataXmp, secondMetadataResponse, false);
        } else {
            exportServiceStepDef.checkXMP(metadataXmp, secondMetadataResponse, false);

            metadataXmp = getXMPFromBytes(imagesByte.get(1));
            exportServiceStepDef.checkXMP(metadataXmp, firstMetadataResponse, false);
        }
    }

    @SneakyThrows
    //@Test(description = "Экспорт файла с другими полями XMP", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"242463"})
    public void successExportOtherXmpTest() {
        ImageData image = new ImageData(ImageFormat.JPEG);
        byte[] bytes = getRandomByteImage(image.getWidth(), image.getHeight(), image.getFormat().getFormatName());

        String firstKey = "FIRST_KEY";
        String secondKey = "SECOND_KEY";
        XMPMeta metadataXmp = new XMPMetaImpl();
        metadataXmp.setProperty(NS_CAMERARAW, firstKey,  RandomStringUtils.randomAlphabetic(8));
        metadataXmp.setProperty(NS_ADOBESTOCKPHOTO, secondKey,  RandomStringUtils.randomAlphabetic(5));

        String fileName = createFileFromBytesArray(bytes, downloadPath, ImageFormat.JPEG.getFormatName()) + "." + ImageFormat.JPEG.getFormatName();
        writeXMPMeta(downloadPath + fileName, metadataXmp);

        bytes = Files.readAllBytes(Paths.get(downloadPath + fileName));
        List<String> idList = importServiceStepDef.importImages(Collections.singletonMap(image.getFilename(), bytes));


        Response metadataResponse = metadataStepDef.checkMetadata(idList.get(0));

        byte[] resultBytes = exportServiceStepDef.exportImage(Collections.singletonList(idList.get(0)));
        XMPMeta actualMetadataXmp = getXMPFromBytes(resultBytes);
        exportServiceStepDef.checkXMP(actualMetadataXmp, metadataResponse, false);

        compareParameters(
                metadataXmp.getProperty(NS_CAMERARAW, firstKey).getValue(),
                actualMetadataXmp.getProperty(NS_CAMERARAW, firstKey).getValue(), "firstKey");
        compareParameters(
                metadataXmp.getProperty(NS_ADOBESTOCKPHOTO, secondKey).getValue(),
                actualMetadataXmp.getProperty(NS_ADOBESTOCKPHOTO, secondKey).getValue(), "firstKey");
    }
}
