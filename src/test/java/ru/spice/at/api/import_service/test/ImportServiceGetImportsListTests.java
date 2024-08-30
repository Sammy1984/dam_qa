package ru.spice.at.api.import_service.test;

import io.qameta.allure.Feature;
import io.qameta.allure.Issue;
import io.qameta.allure.Story;
import ru.testit.annotations.WorkItemIds;
import org.apache.commons.lang3.RandomStringUtils;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import ru.spice.at.api.dto.response.import_service.InvalidParamsItem;
import ru.spice.at.api.dto.response.import_service.imports.DataItem;
import ru.spice.at.api.dto.response.import_service.imports.ImportsListResponse;
import ru.spice.at.api.import_service.ImportServiceSettings;
import ru.spice.at.api.import_service.ImportServiceStepDef;
import ru.spice.at.api.metadata_service.MetadataStepDef;
import ru.spice.at.common.base_test.BaseApiTest;
import ru.spice.at.common.dto.dam.ImageData;
import ru.spice.at.common.emuns.ApiServices;
import ru.spice.at.common.emuns.dam.ImageFormat;
import ru.spice.at.common.utils.Assert;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

import static ru.spice.at.common.utils.Assert.*;

@Feature("Import Service")
@Story("GET imports list")
public class ImportServiceGetImportsListTests extends BaseApiTest<ImportServiceSettings> {
    private final ImportServiceStepDef importServiceStepDef;

    protected ImportServiceGetImportsListTests() {
        super(ApiServices.IMPORT_SERVICE);
        importServiceStepDef = new ImportServiceStepDef();
    }

    @BeforeMethod(alwaysRun = true)
    public void beforeMethod() {
        importServiceStepDef.deleteImports();
    }

    @AfterMethod(alwaysRun = true)
    public void afterMethod() {
        importServiceStepDef.deleteImports();
        new MetadataStepDef(importServiceStepDef.getAuthToken()).deleteMetadata();
    }

    @Test(description = "Успешное получение пустого списка загрузок", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"244589"})
    public void successEmptyImportsTest() {
        importServiceStepDef.deleteImports();
        ImportsListResponse importsList = importServiceStepDef.getImports();

        assertAll(
                () -> mustBeEmptyList(importsList.data(), "Data Items"),
                () -> mustBeNullParameter(importsList.nextPageToken(), "Next Page Token")
        );
    }

    @Issue("SPC-1935")
    @Test(description = "Успешное получение списка всех загрузок", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"244590"})
    public void successAllImportsTest() {
        for (int i = 0; i < 10; i++) {
            String importId = importServiceStepDef.successImportOpen(1);
            if (i % 2 == 0) {
                importServiceStepDef.importRandomImages(new ImageData(ImageFormat.JPEG), importId);
            } else {
                importServiceStepDef.importInvalidRandomImages(new ImageData(ImageFormat.INVALID), importId);
            }
        }

        ImportsListResponse importsList = importServiceStepDef.getImports();
        Assert.compareParameters(10, importsList.data().size(), "size");

        //Проверка порядка от большего к меньшему
        List<String> startedAtTimes = importsList.data().stream().map(DataItem::startedAt).collect(Collectors.toList());

        long startedAt = Long.MAX_VALUE;
        for (String startedAtTime : startedAtTimes) {
            Instant instant = Instant.parse(startedAtTime.replaceAll("\\+00:00", "Z"));
            long epochSecond = instant.toEpochMilli();
            Assert.equalsTrueParameter(startedAt > epochSecond, "started at");
            startedAt = epochSecond;
        }
    }

    @Test(description = "Успешное получение списка всех загрузок - следующая страница", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"244591"})
    public void successNextPageImportsTest() {
        for (int i = 0; i < 26; i++) {
            String importId = importServiceStepDef.successImportOpen(1);
            importServiceStepDef.importInvalidRandomImages(new ImageData(ImageFormat.INVALID), importId);
        }

        ImportsListResponse importsList = importServiceStepDef.getImports();
        assertAll(
                () -> compareParameters(25, importsList.data().size(), "size"),
                () -> notNullOrEmptyParameter(importsList.nextPageToken(), "Next Page Token")
        );

        ImportsListResponse nextImportsList = importServiceStepDef.getImports(Collections.singletonMap("page_token", importsList.nextPageToken()));
        assertAll(
                () -> compareParameters(1, nextImportsList.data().size(), "size"),
                () -> mustBeNullParameter(nextImportsList.nextPageToken(), "Next Page Token")
        );
    }

    @Test(description = "Неуспешное получение списка всех загрузок - следующая страница не невалидная", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"244597"})
    public void unsuccessfulNextPageImportsTest() {
        List<InvalidParamsItem> invalidParamsItems = importServiceStepDef.unsuccessfulGetImports(Collections.singletonMap("page_token", RandomStringUtils.randomAlphanumeric(5)));
        assertAll(
                () -> compareParameters(1, invalidParamsItems.size(), "size"),
                () -> compareParameters("page_token", invalidParamsItems.get(0).name(), "name"),
                () -> compareParameters(getData().errorInvalidType(), invalidParamsItems.get(0).type(), "type"),
                () -> compareParameters(getData().errorInvalidTypeReason(), invalidParamsItems.get(0).reason(), "reason")
        );
    }


    @Test(description = "Успешное получение списка успешных загрузок", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"244592"})
    public void successValidImportsTest() {
        for (int i = 0; i < 10; i++) {
            String importId = importServiceStepDef.successImportOpen(1);
            if (i % 2 == 0) {
                importServiceStepDef.importRandomImages(new ImageData(ImageFormat.JPEG), importId);
            } else {
                importServiceStepDef.importInvalidRandomImages(new ImageData(ImageFormat.INVALID), importId);
            }
        }

        ImportsListResponse importsList = importServiceStepDef.getImports(Collections.singletonMap("import_filter", "success"));
        Assert.compareParameters(5, importsList.data().size(), "size");
        importsList.data().stream().map(DataItem::totalError).
                forEach(totalError -> Assert.compareParameters(0, totalError, "totalError"));
    }

    @Test(description = "Успешное получение списка неуспешных загрузок", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"244593"})
    public void successInvalidImportsTest() {
        for (int i = 0; i < 10; i++) {
            String importId = importServiceStepDef.successImportOpen(1);
            if (i % 2 == 0) {
                importServiceStepDef.importRandomImages(new ImageData(ImageFormat.JPEG), importId);
            } else {
                importServiceStepDef.importInvalidRandomImages(new ImageData(ImageFormat.INVALID), importId);
            }
        }

        ImportsListResponse importsList = importServiceStepDef.getImports(Collections.singletonMap("import_filter", "failed"));
        Assert.compareParameters(5, importsList.data().size(), "size");
        importsList.data().stream().map(DataItem::totalError).
                forEach(totalError -> Assert.compareParameters(1, totalError, "totalError"));
    }
}