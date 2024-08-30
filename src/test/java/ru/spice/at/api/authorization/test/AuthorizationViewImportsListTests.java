package ru.spice.at.api.authorization.test;

import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import ru.spice.at.api.authorization.AuthorizationSettings;
import ru.spice.at.api.dto.response.import_service.imports.ImportsListResponse;
import ru.spice.at.api.import_service.ImportServiceStepDef;
import ru.spice.at.api.metadata_service.MetadataStepDef;
import ru.spice.at.common.base_test.BaseApiTest;
import ru.spice.at.common.dto.dam.ImageData;
import ru.spice.at.common.emuns.ApiServices;
import ru.spice.at.common.emuns.dam.ImageFormat;
import ru.spice.at.common.utils.Assert;
import ru.testit.annotations.WorkItemIds;

import java.util.stream.IntStream;

import static ru.spice.at.common.emuns.Role.*;

@Feature("Authorization")
@Story("View imports list")
public class AuthorizationViewImportsListTests extends BaseApiTest<AuthorizationSettings> {
    private final ImportServiceStepDef importServiceStepDef;

    protected AuthorizationViewImportsListTests() {
        super(ApiServices.AUTHORIZATION);
        importServiceStepDef = new ImportServiceStepDef();
    }

    @BeforeMethod(alwaysRun = true)
    public void beforeMethod() {
        importServiceStepDef.deleteImports();
    }

    @AfterMethod(alwaysRun = true)
    public void afterMethod() {
        importServiceStepDef.setRole(ADMINISTRATOR);
        importServiceStepDef.deleteImports();
    }

    @AfterClass(alwaysRun = true)
    public void afterClass() {
        new MetadataStepDef(importServiceStepDef.getAuthToken()).deleteMetadata();
    }

    @Test(description = "Получение списка загрузок - роль Administrator", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"281372"})
    public void successImportsAdministratorTest() {
        importServiceStepDef.setRole(PHOTOPRODUCTION_OUTSOURCE);
        int listSize = 2;
        int fileSize = 2;
        IntStream.range(0, listSize).forEach(i -> {
            String importId = importServiceStepDef.successImportOpen(1);
            IntStream.range(0, fileSize).forEach(j -> importServiceStepDef.importRandomImages(new ImageData(ImageFormat.JPEG), importId));
        });

        importServiceStepDef.setRole(ADMINISTRATOR);
        ImportsListResponse importsList = importServiceStepDef.getImports();

        Assert.compareParameters(importsList.data().size(), listSize, "list size");
        importsList.data().forEach(item -> Assert.compareParameters(item.totalFact(), fileSize, "file size"));
    }

    @Test(description = "Получение списка загрузок - роль Photoproduction", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"281373"})
    public void successImportsPhotoproductionTest() {
        importServiceStepDef.setRole(PHOTOPRODUCTION);
        int listSize = 1;
        int fileSize = 2;
        IntStream.range(0, listSize).forEach(i -> {
            String importId = importServiceStepDef.successImportOpen(1);
            IntStream.range(0, fileSize).forEach(j -> importServiceStepDef.importRandomImages(new ImageData(ImageFormat.JPEG), importId));
        });

        ImportsListResponse importsList = importServiceStepDef.getImports();

        Assert.compareParameters(importsList.data().size(), listSize, "list size");
        importsList.data().forEach(item -> Assert.compareParameters(item.totalFact(), fileSize, "file size"));
    }

    @Test(description = "Получение списка загрузок - роль Content Production", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"281374"})
    public void successImportsContentProductionTest() {
        importServiceStepDef.setRole(ADMINISTRATOR);
        int listSize = 3;
        int fileSize = 1;
        IntStream.range(0, listSize).forEach(i -> {
            String importId = importServiceStepDef.successImportOpen(1);
            IntStream.range(0, fileSize).forEach(j -> importServiceStepDef.importRandomImages(new ImageData(ImageFormat.JPEG), importId));
        });

        importServiceStepDef.setRole(CONTENT_PRODUCTION);
        ImportsListResponse importsList = importServiceStepDef.getImports();

        Assert.compareParameters(importsList.data().size(), listSize, "list size");
        importsList.data().forEach(item -> Assert.compareParameters(item.totalFact(), fileSize, "file size"));
    }

    @Test(description = "Получение списка загрузок - роль Content Support", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"281376"})
    public void successImportsContentSupportTest() {
        importServiceStepDef.setRole(ADMINISTRATOR);
        int listSize = 1;
        int fileSize = 1;
        IntStream.range(0, listSize).forEach(i -> {
            String importId = importServiceStepDef.successImportOpen(1);
            IntStream.range(0, fileSize).forEach(j -> importServiceStepDef.importRandomImages(new ImageData(ImageFormat.JPEG), importId));
        });

        importServiceStepDef.setRole(CONTENT_SUPPORT);
        ImportsListResponse importsList = importServiceStepDef.getImports();

        Assert.compareParameters(importsList.data().size(), listSize, "list size");
        importsList.data().forEach(item -> Assert.compareParameters(item.totalFact(), fileSize, "file size"));
    }

    @Test(description = "Получение списка загрузок - роль Photoproduction Outsource", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"281377"})
    public void successImportsPhotoproductionOutsourceTest() {
        importServiceStepDef.setRole(PHOTOPRODUCTION_OUTSOURCE);
        int listSize = 2;
        int fileSize = 2;
        IntStream.range(0, listSize).forEach(i -> {
            String importId = importServiceStepDef.successImportOpen(1);
            IntStream.range(0, fileSize).forEach(j -> importServiceStepDef.importRandomImages(new ImageData(ImageFormat.JPEG), importId));
        });

        ImportsListResponse importsList = importServiceStepDef.getImports();

        Assert.compareParameters(importsList.data().size(), listSize, "list size");
        importsList.data().forEach(item -> Assert.compareParameters(item.totalFact(), fileSize, "file size"));
    }

    @Test(description = "Получение списка загрузок - роль Photoproduction Outsource (загруженные Administrator)", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"281378"})
    public void unsuccessfulImportsPhotoproductionOutsourceTest() {
        importServiceStepDef.setRole(ADMINISTRATOR);
        int listSize = 2;
        int fileSize = 2;
        IntStream.range(0, listSize).forEach(i -> {
            String importId = importServiceStepDef.successImportOpen(1);
            IntStream.range(0, fileSize).forEach(j -> importServiceStepDef.importRandomImages(new ImageData(ImageFormat.JPEG), importId));
        });

        importServiceStepDef.setRole(PHOTOPRODUCTION_OUTSOURCE);
        ImportsListResponse importsList = importServiceStepDef.getImports();

        Assert.mustBeEmptyList(importsList.data(), "list size");
    }

    @Test(description = "Получение списка загрузок - роль Content Production Outsource", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"281379"})
    public void successImportsContentProductionOutsourceTest() {
        importServiceStepDef.setRole(CONTENT_PRODUCTION_OUTSOURCE);
        int listSize = 1;
        int fileSize = 2;
        IntStream.range(0, listSize).forEach(i -> {
            String importId = importServiceStepDef.successImportOpen(1);
            IntStream.range(0, fileSize).forEach(j -> importServiceStepDef.importRandomImages(new ImageData(ImageFormat.JPEG), importId));
        });

        ImportsListResponse importsList = importServiceStepDef.getImports();

        Assert.compareParameters(importsList.data().size(), listSize, "list size");
        importsList.data().forEach(item -> Assert.compareParameters(item.totalFact(), fileSize, "file size"));
    }

    @Test(description = "Получение списка загрузок - роль Content Production Outsource (загруженные Photoproduction Outsource)", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"281380"})
    public void unsuccessfulImportsContentProductionOutsourceTest() {
        importServiceStepDef.setRole(PHOTOPRODUCTION_OUTSOURCE);
        int listSize = 1;
        int fileSize = 1;
        IntStream.range(0, listSize).forEach(i -> {
            String importId = importServiceStepDef.successImportOpen(1);
            IntStream.range(0, fileSize).forEach(j -> importServiceStepDef.importRandomImages(new ImageData(ImageFormat.JPEG), importId));
        });

        importServiceStepDef.setRole(CONTENT_PRODUCTION_OUTSOURCE);
        ImportsListResponse importsList = importServiceStepDef.getImports();

        Assert.mustBeEmptyList(importsList.data(), "list size");
    }
}
