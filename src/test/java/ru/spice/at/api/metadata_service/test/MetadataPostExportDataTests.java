package ru.spice.at.api.metadata_service.test;

import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import ru.testit.annotations.WorkItemIds;
import org.apache.commons.lang3.RandomStringUtils;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.spice.at.api.dto.response.metadata.DataExportResponse;
import ru.spice.at.api.dto.response.metadata.ExportItem;
import ru.spice.at.api.metadata_service.MetadataSettings;
import ru.spice.at.api.metadata_service.MetadataStepDef;
import ru.spice.at.common.base_test.BaseApiTest;
import ru.spice.at.common.emuns.ApiServices;
import ru.spice.at.common.utils.Assert;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Feature("Metadata Service")
@Story("POST export data")
public class MetadataPostExportDataTests extends BaseApiTest<MetadataSettings> {
    private final MetadataStepDef metadataStepDef;
    private final DataExportResponse export = new DataExportResponse();

    protected MetadataPostExportDataTests() {
        super(ApiServices.METADATA_SERVICE);
        metadataStepDef = new MetadataStepDef();
    }

    @BeforeClass(description = "Создаем файлы в методате", alwaysRun = true)
    public void beforeClass() {
        List<ExportItem> data = getData().images().stream().
                map(image -> new ExportItem().filename(image.getFilename()).key(image.getKey()).id(metadataStepDef.createMetadataImage(image)))
                .collect(Collectors.toList());
        export.data(data);
    }

    //todo выключено для использования параллельного сьюта
    //@AfterClass(alwaysRun = true)
    public void afterClass() {
        metadataStepDef.deleteMetadata();
    }

    @Test(description = "Получение данных для экспорта одного объекта", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"191808"})
    public void successExportFileMetadataTest() {
        DataExportResponse singleExport = new DataExportResponse();
        singleExport.data(Collections.singletonList(export.data().get(0)));

        DataExportResponse exportResponse = metadataStepDef.getExportMetadata(
                Collections.singletonList(singleExport.data().get(0).id()));
        Assert.compareParameters(singleExport, exportResponse, "параметры экспорта");
    }

    @Test(description = "Получение данных для экспорта нескольких объектов", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"191809"})
    public void successExportFilesMetadataTest() {
        DataExportResponse exportResponse = metadataStepDef.getExportMetadata(
                export.data().stream().map(ExportItem::id).collect(Collectors.toList()));
        Assert.compareParameters(export, exportResponse, "параметры экспорта");
    }

    @Test(description = "Получение данных для экспорта с не существующем id", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"191810"})
    public void successExportNotExistMetadataTest() {
        DataExportResponse exportResponse = metadataStepDef.getExportMetadata(
                Collections.singletonList(UUID.randomUUID().toString()));
        Assert.mustBeEmptyList(exportResponse.data(), "параметры экспорта");
    }

    @Test(description = "Получение данных для экспорта с не валидным id", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"191811"})
    public void badRequestExportFileMetadataTest() {
        metadataStepDef.checkBadRequestExportMetadata(
                Collections.singletonList(UUID.randomUUID() + RandomStringUtils.randomNumeric(3)));
    }
}
