package ru.spice.at.api.import_service.test;

import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import ru.testit.annotations.WorkItemIds;
import org.testng.annotations.*;
import ru.spice.at.api.dto.response.import_service.DataItem;
import ru.spice.at.api.import_service.ImportServiceSettings;
import ru.spice.at.api.import_service.ImportServiceStepDef;
import ru.spice.at.common.base_test.BaseApiTest;
import ru.spice.at.common.dto.dam.ImageData;
import ru.spice.at.common.emuns.ApiServices;
import ru.spice.at.common.emuns.dam.ImageFormat;
import ru.spice.at.common.utils.Assert;

import java.util.List;

@Feature("Import Service")
@Story("GET validations list")
//todo на удаление
@Deprecated
public class ImportServiceGetValidationsListTests extends BaseApiTest<ImportServiceSettings> {
    private final ImportServiceStepDef importServiceStepDef;

    protected ImportServiceGetValidationsListTests() {
        super(ApiServices.IMPORT_SERVICE);
        importServiceStepDef = new ImportServiceStepDef();
    }

    @BeforeMethod(description = "Добавляем файлы с ошибкой при загрузке", alwaysRun = true)
    public void beforeMethod() {
        importServiceStepDef.deleteValidations();

        for (int i = 0; i < 12; i++) {
            ImageData imageData = new ImageData(ImageFormat.INVALID);
            importServiceStepDef.importInvalidRandomImages(imageData);
        }
    }

    @AfterMethod(alwaysRun = true)
    public void afterMethod() {
        importServiceStepDef.deleteValidations();
    }

    //@Test(description = "Успешное получение списка ошибок без query параметров", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"225830"})
    public void successGetValidationsWithoutQueryTest() {
        List<DataItem> actualDataItems = importServiceStepDef.getValidationsDataItems(null);
        Assert.compareParameters(10, actualDataItems.size(), "количество элементов списка ошибок");
    }

    //@Test(description = "Успешное получение списка ошибок с query параметрами", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"225832"})
    public void successGetValidationsWithQueryTest() {
        List<DataItem> actualDataItems = importServiceStepDef.getValidationsDataItems(1);
        Assert.compareParameters(1, actualDataItems.size(), "количество элементов списка ошибок");
    }

    //@Test(description = "Успешное получение пустого списка ошибок", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"225831"})
    public void successGetValidationsEmptyQueryTest() {
        importServiceStepDef.deleteValidations();
        List<DataItem> actualDataItems = importServiceStepDef.getValidationsDataItems(null);
        Assert.compareParameters(0, actualDataItems.size(), "количество элементов списка ошибок");
    }
}
