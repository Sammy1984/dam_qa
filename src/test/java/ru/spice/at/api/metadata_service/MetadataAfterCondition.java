package ru.spice.at.api.metadata_service;

import org.testng.annotations.AfterSuite;

public class MetadataAfterCondition {

    @AfterSuite(alwaysRun = true)
    public void afterSuite() {
        new MetadataStepDef().deleteMetadata();
    }
}
