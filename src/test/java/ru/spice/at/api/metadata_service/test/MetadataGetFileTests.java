package ru.spice.at.api.metadata_service.test;

import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import ru.testit.annotations.WorkItemIds;
import org.apache.commons.lang3.RandomStringUtils;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;
import ru.spice.at.api.metadata_service.MetadataSettings;
import ru.spice.at.api.metadata_service.MetadataStepDef;
import ru.spice.at.common.base_test.BaseApiTest;
import ru.spice.at.common.emuns.ApiServices;

import java.util.UUID;

@Feature("Metadata Service")
@Story("GET metadata file")
public class MetadataGetFileTests extends BaseApiTest<MetadataSettings> {
    private final MetadataStepDef metadataStepDef;

    protected MetadataGetFileTests() {
        super(ApiServices.METADATA_SERVICE);
        metadataStepDef = new MetadataStepDef();
    }

    //todo выключено для использования параллельного сьюта
    //@AfterClass(alwaysRun = true)
    public void afterClass() {
        metadataStepDef.deleteMetadata();
    }

    @Test(description = "Успешное получение метаданных об одном файле", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"191800"})
    public void successGetMetadataImageTest() {
        String id = metadataStepDef.createMetadataImage(getData().images().get(0));
        metadataStepDef.checkMetadataStatusImage(id, getData().newStatus());
    }

    @Test(description = "Получение метаданных о несуществующем файле", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"191801"})
    public void notFoundGetMetadataImageTest() {
        metadataStepDef.checkNotFoundMetadataImage(UUID.randomUUID().toString());
    }

    @Test(description = "Получение метаданных о файле c не валидным id", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"191802"})
    public void badRequestGetMetadataImageTest() {
        metadataStepDef.checkBadRequestMetadataImage(UUID.randomUUID() + RandomStringUtils.randomNumeric(3));
    }
}
