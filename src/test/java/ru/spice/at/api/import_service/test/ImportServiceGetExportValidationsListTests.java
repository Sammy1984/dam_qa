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
import ru.spice.at.common.utils.FileHelper;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

import static ru.spice.at.common.utils.FileHelper.*;

@Feature("Import Service")
@Story("GET export validations list")
//todo на удаление
@Deprecated
public class ImportServiceGetExportValidationsListTests extends BaseApiTest<ImportServiceSettings> {
    private final ImportServiceStepDef importServiceStepDef;
    private final List<ImageData> imagesData = new ArrayList<>();
    private final List<String> idList = new ArrayList<>();

    protected ImportServiceGetExportValidationsListTests() {
        super(ApiServices.IMPORT_SERVICE);
        importServiceStepDef = new ImportServiceStepDef();
    }

    @BeforeClass(alwaysRun = true)
    public void beforeClass() {
        importServiceStepDef.deleteValidations();

        createFileDirection();
        imagesData.add(new ImageData(ImageFormat.INVALID));
        imagesData.add(new ImageData(ImageFormat.JPEG));

        getData().firstErrorDownloadTable().get(1).set(0, imagesData.get(0).getFilename());
        getData().secondErrorDownloadTable().get(1).set(0, imagesData.get(1).getFilename());
        getData().secondErrorDownloadTable().get(2).set(0, imagesData.get(1).getFilename());

        importServiceStepDef.importInvalidRandomImages(imagesData.get(0));
        importServiceStepDef.importInvalidRandomImages(imagesData.get(1).getFilename(), new byte[0]);

        imagesData.forEach(image -> {
            List<DataItem> actualDataItems = importServiceStepDef.getValidationsDataItems();
            String id = actualDataItems.stream().filter(item -> !item.files().isEmpty() && item.files().get(0).filename().equals(image.getFilename())).
                    map(DataItem::importId).findFirst().orElse(null);
            Assert.notNullOrEmptyParameter(id, "id запроса");
            idList.add(id);
        });
    }

    @AfterClass(alwaysRun = true)
    public void afterClass() {
        deleteFileDirection();
        importServiceStepDef.deleteValidations();
    }

    //@Test(description = "Успешное получение одного xlsx файла", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"225829"})
    public void successDownloadXlsxTest() {
        byte[] bytes = importServiceStepDef.downloadValidation(Collections.singletonList(idList.get(0)));

        List<List<String>> resultTable = getTableFromXlsxFile(bytes);
        Assert.compareParameters(getData().firstErrorDownloadTable(), resultTable, "таблица из xlsx");
    }

    //@Test(description = "Успешное получение несколько xlsx файлов", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"230060"})
    public void successDownloadZipTest() {
        byte[] bytes = importServiceStepDef.downloadValidation(idList);

        String name = createFileFromBytesArray(bytes, downloadPath, "zip");
        String extractFolder = extractZip(name, downloadPath, "xlsx");

        File[] files = new File(extractFolder).listFiles();
        Assert.notNullOrEmptyParameter(files, "файлы");
        Assert.compareParameters(2, files.length, "количество файлов");

        List<List<List<String>>> list = Arrays.stream(files).map(FileHelper::getTableFromXlsxFile).collect(Collectors.toList());
        Assert.compareParameters(
                new LinkedList<>(Arrays.asList(getData().firstErrorDownloadTable(), getData().secondErrorDownloadTable())),
                new LinkedList<>(list), "xlsx таблицы из архива");
    }
}
