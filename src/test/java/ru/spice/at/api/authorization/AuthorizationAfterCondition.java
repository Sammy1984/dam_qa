package ru.spice.at.api.authorization;

import org.testng.annotations.AfterSuite;
import ru.spice.at.api.import_service.ImportServiceStepDef;
import ru.spice.at.api.metadata_service.MetadataStepDef;

public class AuthorizationAfterCondition {

    @AfterSuite(alwaysRun = true)
    public void afterSuite() {
        MetadataStepDef metadataStepDef = new MetadataStepDef();
        ImportServiceStepDef importServiceStepDef = new ImportServiceStepDef(metadataStepDef.getAuthToken());

        metadataStepDef.deleteMetadata();
        importServiceStepDef.deleteImports();
        importServiceStepDef.deleteMedia();
    }
}
