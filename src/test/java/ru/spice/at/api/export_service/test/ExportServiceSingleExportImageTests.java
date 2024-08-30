package ru.spice.at.api.export_service.test;

import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import ru.testit.annotations.WorkItemIds;
import org.apache.commons.lang3.RandomStringUtils;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.spice.at.api.dto.response.metadata.InvalidParamsItem;
import ru.spice.at.api.import_service.ImportServiceStepDef;
import ru.spice.at.api.export_service.ExportServiceSettings;
import ru.spice.at.api.export_service.ExportServiceStepDef;
import ru.spice.at.api.metadata_service.MetadataStepDef;
import ru.spice.at.common.base_test.BaseApiTest;
import ru.spice.at.common.dto.dam.ImageData;
import ru.spice.at.common.emuns.ApiServices;
import ru.spice.at.common.utils.Assert;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static ru.spice.at.common.constants.TestConstants.*;
import static ru.spice.at.common.utils.ImageHelper.getRandomByteImage;

@Feature("Export Service")
@Story("POST export media")
public class ExportServiceSingleExportImageTests extends BaseApiTest<ExportServiceSettings> {
    private final ExportServiceStepDef exportServiceStepDef;

    private ImportServiceStepDef importServiceStepDef;
    private List<String> idList;

    protected ExportServiceSingleExportImageTests() {
        super(ApiServices.EXPORT_SERVICE);
        exportServiceStepDef = new ExportServiceStepDef();
    }

    @BeforeClass(description = "Добавляем файлы в систему", alwaysRun = true)
    public void beforeClass() {
        importServiceStepDef = new ImportServiceStepDef(exportServiceStepDef.getAuthToken());
        createFileDirection();
        Map<String, byte[]> images = getData().images().stream().collect(Collectors.toMap(ImageData::getFilename,
                image -> getRandomByteImage(image.getWidth(), image.getHeight(), image.getFormat().getFormatName())));
        idList = importServiceStepDef.importImages(images);
    }

    @AfterClass(alwaysRun = true)
    public void afterClassDelete() {
        deleteFileDirection();
    }

    //todo выключено для использования параллельного сьюта
    //@AfterClass(alwaysRun = true)
    public void afterClass() {
        importServiceStepDef.deleteMedia();
        new MetadataStepDef(exportServiceStepDef.getAuthToken()).deleteMetadata();
    }

    @Test(description = "Успешный экспорт 1 файла", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"225865"})
    public void successExportImageTest() {
        byte[] resultBytes = exportServiceStepDef.exportImage(Collections.singletonList(idList.get(0)));
        Assert.notNullOrEmptyParameter(resultBytes.length, "байты");
    }

    @Test(description = "Неуспешный экспорт - несуществующий файл", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"232841"})
    public void unsuccessfulExportImageNotExistTest() {
        List<InvalidParamsItem> invalidParamsItems = exportServiceStepDef.unsuccessfulExportImage(Collections.singletonList(UUID.randomUUID().toString()));
        Assert.compareParameters(Collections.singletonList(getData().invalidParams().get(0)), invalidParamsItems, "ошибки");
    }

    @Test(description = "Неуспешный экспорт - невалидный uuid", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"232842"})
    public void unsuccessfulExportImageInvalidUuidTest() {
        List<InvalidParamsItem> invalidParamsItems =
                exportServiceStepDef.unsuccessfulExportImage(Collections.singletonList(idList.get(0) + RandomStringUtils.randomAlphabetic(2)));
        Assert.compareParameters(Collections.singletonList(getData().invalidParams().get(4)), invalidParamsItems, "ошибки");
    }
}
