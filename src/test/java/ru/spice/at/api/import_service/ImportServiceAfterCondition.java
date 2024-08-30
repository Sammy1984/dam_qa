package ru.spice.at.api.import_service;

import org.testng.annotations.AfterSuite;
import ru.spice.at.api.metadata_service.MetadataStepDef;

public class ImportServiceAfterCondition {

    @AfterSuite(alwaysRun = true)
    public void afterSuite() {
        MetadataStepDef metadataStepDef = new MetadataStepDef();
        ImportServiceStepDef importServiceStepDef = new ImportServiceStepDef(metadataStepDef.getAuthToken());

        metadataStepDef.deleteMetadata();
        importServiceStepDef.deleteImports();
        importServiceStepDef.deleteMedia();
    }
}
