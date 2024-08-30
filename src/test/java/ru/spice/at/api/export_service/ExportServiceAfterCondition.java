package ru.spice.at.api.export_service;

import org.testng.annotations.AfterSuite;
import ru.spice.at.api.import_service.ImportServiceStepDef;
import ru.spice.at.api.metadata_service.MetadataStepDef;

public class ExportServiceAfterCondition {

    @AfterSuite(alwaysRun = true)
    public void afterSuite() {
        MetadataStepDef metadataStepDef = new MetadataStepDef();
        ImportServiceStepDef importServiceStepDef = new ImportServiceStepDef(metadataStepDef.getAuthToken());

        metadataStepDef.deleteMetadata();
        importServiceStepDef.deleteMedia();
    }
}
