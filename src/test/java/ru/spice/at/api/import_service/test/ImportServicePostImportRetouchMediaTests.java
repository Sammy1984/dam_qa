package ru.spice.at.api.import_service.test;

import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import ru.spice.at.common.emuns.dam.ProcessingTypeEnum;
import ru.testit.annotations.WorkItemIds;
import io.restassured.response.Response;
import org.awaitility.Awaitility;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import ru.spice.at.api.export_service.ExportServiceStepDef;
import ru.spice.at.api.import_service.ImportServiceSettings;
import ru.spice.at.api.import_service.ImportServiceStepDef;
import ru.spice.at.api.metadata_service.MetadataStepDef;
import ru.spice.at.common.base_test.BaseApiTest;
import ru.spice.at.common.dto.dam.ImageData;
import ru.spice.at.common.emuns.ApiServices;
import ru.spice.at.common.emuns.dam.ImageFormat;
import ru.spice.at.common.utils.Assert;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static ru.spice.at.common.emuns.dam.ImageFormat.*;
import static ru.spice.at.common.emuns.dam.ImageParameters.*;
import static ru.spice.at.common.emuns.dam.Quality.*;
import static ru.spice.at.common.emuns.dam.Status.*;
import static ru.spice.at.common.utils.Assert.*;
import static ru.spice.at.common.utils.ImageHelper.*;
import static ru.spice.at.common.utils.JsonHelper.getValueFromResponse;

//todo Добавить в обработку изображения при параллельном запуске
/*
    "без_фона_совсем_кривое.png",
    "без_фона_совсем_смещение.png",
    "белый_фон_в_углу.png",
    "белый_фон_смещение.png",
    "белый_фон_смещение_вправо.png",
    "белый_фон_широкая_рамка.png",
    "без_фона_бутылка_сложная.png",
    "без_фона_бутылка.png",
    "белый_фон_светлое_озображение.jpg",
    "белый_фон_сложные_границы_изображения.jpg",
    "черный_фон_контрастное_изображение.jpeg",
    "черный_фон_темное_изображение.jpg"
 */
@Feature("Import Service")
@Story("POST Import retouch media")
public class ImportServicePostImportRetouchMediaTests extends BaseApiTest<ImportServiceSettings> {
    private ImportServiceStepDef importServiceStepDef;
    private MetadataStepDef metadataStepDef;
    private ExportServiceStepDef exportServiceStepDef;

    protected ImportServicePostImportRetouchMediaTests() {
        super(ApiServices.IMPORT_SERVICE);
    }

    @BeforeClass(alwaysRun = true)
    public void beforeClass() {
        importServiceStepDef = new ImportServiceStepDef();
        metadataStepDef = new MetadataStepDef(importServiceStepDef.getAuthToken());
        exportServiceStepDef = new ExportServiceStepDef(importServiceStepDef.getAuthToken());
    }

    //todo выключено для использования параллельного сьюта
    //@AfterClass(alwaysRun = true)
    public void afterClass() {
        importServiceStepDef.deleteMedia();
        metadataStepDef.deleteMetadata();
    }

    @DataProvider(name = "images")
    public Object[] getImageName() {
        importServiceStepDef.setAuthToken(null);
        metadataStepDef.setAuthToken(null);
        exportServiceStepDef.setAuthToken(null);
        return getData().retouchFiles();
    }

    @Test(description = "Нормализация изображения", timeOut = 600000, groups = {"regress"}, dataProvider = "images")
    @WorkItemIds({"247813"})
    public void successNormalizationTest(String imageName) {
        File noRetouchFile = importServiceStepDef.getImportFile(imageName);
        List<String> metadataIds = importServiceStepDef.importFiles(Collections.singletonList(noRetouchFile), Collections.singletonList(ProcessingTypeEnum.NORMALIZATION));

        AtomicReference<String> derivedMetadataIdAtomic = new AtomicReference<>();
        Awaitility.await().atMost(20, TimeUnit.SECONDS).pollInterval(1, TimeUnit.SECONDS).until(() ->
        {
            Response response = metadataStepDef.checkMetadata(metadataIds.get(0));
            String id = getValueFromResponse(response, DERIVED_METADATA_ID.getPath());
            derivedMetadataIdAtomic.set(id);
            return id != null;
        });

        File expRetouchFile = exportServiceStepDef.getExportFile(imageName.split("\\.")[0] + "_нормализация.jpg");
        byte[] actualRetouchBytes = exportServiceStepDef.exportImage(Collections.singletonList(derivedMetadataIdAtomic.get()));
        boolean sameImage = checkSameImage(expRetouchFile, toBufferedImage(actualRetouchBytes));

        Assert.equalsTrueParameter(sameImage, "отретушированные изображения");
    }

    @Test(description = "Нормализация и центрирование изображения", timeOut = 600000, groups = {"regress"}, dataProvider = "images")
    @WorkItemIds({"247814"})
    public void successNormalizationCenteringTest(String imageName) {
        File noRetouchFile = importServiceStepDef.getImportFile(imageName);
        List<String> metadataIds = importServiceStepDef.importFiles(Collections.singletonList(noRetouchFile), Arrays.asList(ProcessingTypeEnum.NORMALIZATION, ProcessingTypeEnum.FIELDS));

        AtomicReference<String> derivedMetadataIdAtomic = new AtomicReference<>();
        Awaitility.await().atMost(20, TimeUnit.SECONDS).pollInterval(1, TimeUnit.SECONDS).until(() ->
        {
            Response response = metadataStepDef.checkMetadata(metadataIds.get(0));
            String id = getValueFromResponse(response, DERIVED_METADATA_ID.getPath());
            derivedMetadataIdAtomic.set(id);
            return id != null;
        });

        File expRetouchFile = exportServiceStepDef.getExportFile(imageName.split("\\.")[0] + "_нормализация_центрирование.jpg");
        byte[] actualRetouchBytes = exportServiceStepDef.exportImage(Collections.singletonList(derivedMetadataIdAtomic.get()));
        boolean sameImage = checkSameImage(expRetouchFile, toBufferedImage(actualRetouchBytes));

        Assert.equalsTrueParameter(sameImage, "отретушированные изображения");
    }

    @Test(description = "Нормализация изображения - проверка метаданных", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"247816"})
    public void successNormalizationMetadataTest() {
        ImageData image = new ImageData(PNG);
        byte[] bytes = getRandomByteImage(image.getWidth(), image.getHeight(), image.getFormat().getFormatName());
        List<String> metadataIds = importServiceStepDef.importImages(Collections.singletonMap(image.getFilename(), bytes), Collections.singletonList(ProcessingTypeEnum.NORMALIZATION));

        Response metadataResponse = metadataStepDef.checkMetadata(metadataIds.get(0));
        Assert.compareParameters(NEW.getName(), getValueFromResponse(metadataResponse, STATUS_NAME.getPath()), "status");

        Awaitility.await("Статус не перешел в 'Архивный'").atMost(20, TimeUnit.SECONDS).pollInterval(1, TimeUnit.SECONDS).until(() ->
        {
            Response response = metadataStepDef.checkMetadata(metadataIds.get(0));
            String status = getValueFromResponse(response, STATUS_NAME.getPath());
            return status.equals(ARCHIVE.getName());
        });

        metadataResponse = metadataStepDef.checkMetadata(metadataIds.get(0));
        String derivedMetadataId = getValueFromResponse(metadataResponse, DERIVED_METADATA_ID.getPath());
        Assert.notNullOrEmptyParameter(derivedMetadataId, "id обработанного файла");

        Response metadataDerivedResponse = metadataStepDef.checkMetadata(derivedMetadataId);
        assertAll(
                () -> compareParameters(NEW.getName(), getValueFromResponse(metadataDerivedResponse, STATUS_NAME.getPath()), "status"),
                () -> compareParameters(metadataIds.get(0), getValueFromResponse(metadataDerivedResponse, ORIGINAL_METADATA_ID.getPath()), "original_metadata_id"),
                () -> mustBeNullParameter(getValueFromResponse(metadataDerivedResponse, DERIVED_METADATA_ID.getPath()), "derived_metadata_id"),
                () -> mustBeNullParameter(getValueFromResponse(metadataDerivedResponse, KEYWORDS.getPath()), "keywords"),
                () -> equalsFalseParameter(getValueFromResponse(metadataDerivedResponse, IS_RAW_IMAGE.getPath()), "is_raw_image"),
                () -> compareParameters(JPEG.getFormatName(), getValueFromResponse(metadataDerivedResponse, FORMAT.getPath()).toString().toLowerCase(), "format"),
                () -> contains(JPG.getFormatName(), getValueFromResponse(metadataDerivedResponse, FILENAME.getPath()), "filename")
        );
    }

    @Test(description = "Нормализация и центрирование изображения - проверка метаданных", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"247817"})
    public void successNormalizationCenteringMetadataTest() {
        ImageData image = new ImageData(JPG);
        byte[] bytes = getRandomByteImage(image.getWidth(), image.getHeight(), image.getFormat().getFormatName());
        List<String> metadataIds = importServiceStepDef.importImages(Collections.singletonMap(image.getFilename(), bytes), Arrays.asList(ProcessingTypeEnum.NORMALIZATION, ProcessingTypeEnum.FIELDS));

        Response metadataResponse = metadataStepDef.checkMetadata(metadataIds.get(0));
        Assert.compareParameters(NEW.getName(), getValueFromResponse(metadataResponse, STATUS_NAME.getPath()), "status");

        Awaitility.await("Статус не перешел в 'Архивный'").atMost(20, TimeUnit.SECONDS).pollInterval(1, TimeUnit.SECONDS).until(() ->
        {
            Response response = metadataStepDef.checkMetadata(metadataIds.get(0));
            String status = getValueFromResponse(response, STATUS_NAME.getPath());
            return status.equals(ARCHIVE.getName());
        });

        metadataResponse = metadataStepDef.checkMetadata(metadataIds.get(0));
        String derivedMetadataId = getValueFromResponse(metadataResponse, DERIVED_METADATA_ID.getPath());
        Assert.notNullOrEmptyParameter(derivedMetadataId, "id обработанного файла");

        Response metadataDerivedResponse = metadataStepDef.checkMetadata(derivedMetadataId);
        assertAll(
                () -> compareParameters(NEW.getName(), getValueFromResponse(metadataDerivedResponse, STATUS_NAME.getPath()), "status"),
                () -> compareParameters(metadataIds.get(0), getValueFromResponse(metadataDerivedResponse, ORIGINAL_METADATA_ID.getPath()), "original_metadata_id"),
                () -> mustBeNullParameter(getValueFromResponse(metadataDerivedResponse, DERIVED_METADATA_ID.getPath()), "derived_metadata_id"),
                () -> mustBeNullParameter(getValueFromResponse(metadataDerivedResponse, KEYWORDS.getPath()), "keywords"),
                () -> equalsFalseParameter(getValueFromResponse(metadataDerivedResponse, IS_RAW_IMAGE.getPath()), "is_raw_image"),
                () -> compareParameters(JPEG.getFormatName(), getValueFromResponse(metadataDerivedResponse, FORMAT.getPath()).toString().toLowerCase(), "format"),
                () -> contains(JPG.getFormatName(), getValueFromResponse(metadataDerivedResponse, FILENAME.getPath()), "filename")
        );
    }

    @Test(description = "Нормализация изображения - проверка качества (большой размер изображения)", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"247818"})
    public void successNormalizationBigImageTest() {
        ImageData image = new ImageData(ImageFormat.JPG).setHeight(3455).setWidth(2555);
        byte[] bytes = getRandomByteImage(image.getWidth(), image.getHeight(), image.getFormat().getFormatName());
        List<String> metadataIds = importServiceStepDef.importImages(Collections.singletonMap(image.getFilename(), bytes), Collections.singletonList(ProcessingTypeEnum.NORMALIZATION));

        Awaitility.await("Статус не перешел в 'Архивный'").atMost(20, TimeUnit.SECONDS).pollInterval(1, TimeUnit.SECONDS).until(() ->
        {
            Response response = metadataStepDef.checkMetadata(metadataIds.get(0));
            String status = getValueFromResponse(response, STATUS_NAME.getPath());
            return status.equals(ARCHIVE.getName());
        });

        Response metadataResponse = metadataStepDef.checkMetadata(metadataIds.get(0));
        String derivedMetadataId = getValueFromResponse(metadataResponse, DERIVED_METADATA_ID.getPath());
        assertAll(
                () -> notNullOrEmptyParameter(derivedMetadataId, "id обработанного файла"),
                () -> compareParameters(TO_REVISION.getName(), getValueFromResponse(metadataResponse, QUALITY_NAME.getPath()), "quality_name")
        );

        Response metadataDerivedResponse = metadataStepDef.checkMetadata(derivedMetadataId);
        assertAll(
                () -> compareParameters(GOOD.getName(), getValueFromResponse(metadataDerivedResponse, QUALITY_NAME.getPath()), "quality_name"),
                () -> compareParameters(2000, getValueFromResponse(metadataDerivedResponse, WIDTH.getPath()), "width"),
                () -> compareParameters(2000, getValueFromResponse(metadataDerivedResponse, HEIGHT.getPath()), "height")
        );
    }

    @Test(description = "Нормализация изображения - проверка качества (маленький размер изображения)", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"247819"})
    public void successNormalizationSmallImageTest() {
        ImageData image = new ImageData(ImageFormat.JPG).setHeight(488).setWidth(379);
        byte[] bytes = getRandomByteImage(image.getWidth(), image.getHeight(), image.getFormat().getFormatName());
        List<String> metadataIds = importServiceStepDef.importImages(Collections.singletonMap(image.getFilename(), bytes), Collections.singletonList(ProcessingTypeEnum.NORMALIZATION));

        Awaitility.await("Статус не перешел в 'Архивный'").atMost(20, TimeUnit.SECONDS).pollInterval(1, TimeUnit.SECONDS).until(() ->
        {
            Response response = metadataStepDef.checkMetadata(metadataIds.get(0));
            String status = getValueFromResponse(response, STATUS_NAME.getPath());
            return status.equals(ARCHIVE.getName());
        });

        Response metadataResponse = metadataStepDef.checkMetadata(metadataIds.get(0));
        String derivedMetadataId = getValueFromResponse(metadataResponse, DERIVED_METADATA_ID.getPath());
        assertAll(
                () -> notNullOrEmptyParameter(derivedMetadataId, "id обработанного файла"),
                () -> compareParameters(BAD.getName(), getValueFromResponse(metadataResponse, QUALITY_NAME.getPath()), "quality_name")
        );

        Response metadataDerivedResponse = metadataStepDef.checkMetadata(derivedMetadataId);
        assertAll(
                () -> compareParameters(GOOD.getName(), getValueFromResponse(metadataDerivedResponse, QUALITY_NAME.getPath()), "quality_name"),
                () -> compareParameters(500, getValueFromResponse(metadataDerivedResponse, WIDTH.getPath()), "width"),
                () -> compareParameters(500, getValueFromResponse(metadataDerivedResponse, HEIGHT.getPath()), "height")
        );
    }
}
