package ru.spice.at.api.metadata_service.test;

import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import ru.testit.annotations.WorkItemIds;
import org.apache.commons.lang3.RandomStringUtils;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.spice.at.api.metadata_service.MetadataSettings;
import ru.spice.at.api.metadata_service.MetadataStepDef;
import ru.spice.at.common.base_test.BaseApiTest;
import ru.spice.at.common.emuns.ApiServices;
import ru.spice.at.common.utils.Assert;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Feature("Metadata Service")
@Story("POST check filename")
public class MetadataPostCheckFilenameTests extends BaseApiTest<MetadataSettings> {
    private final MetadataStepDef metadataStepDef;
    private final List<String> names = new ArrayList<>();

    protected MetadataPostCheckFilenameTests() {
        super(ApiServices.METADATA_SERVICE);
        metadataStepDef = new MetadataStepDef();
    }

    @BeforeClass(description = "Создаем файлы в методате", alwaysRun = true)
    public void beforeClass() {
        getData().images().forEach(image -> {
            metadataStepDef.createMetadataImage(image);
            names.add(image.getFilename());
        });
    }

    //todo выключено для использования параллельного сьюта
    //@AfterClass(alwaysRun = true)
    public void afterClass() {
        metadataStepDef.deleteMetadata();
    }

    @Test(description = "Проверка названия несуществующего файла", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"191803"})
    public void successCheckNoExistFilenameTest() {
        List<String> names = metadataStepDef.getExistMetadata(
                Collections.singletonList(RandomStringUtils.randomNumeric(3)));
        Assert.mustBeEmptyList(names, "файлы");
    }

    @Test(description = "Проверка с пустым телом запроса", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"191804"})
    public void successCheckEmptyFilenameTest() {
        List<String> names = metadataStepDef.getExistMetadata(Collections.emptyList());
        Assert.mustBeEmptyList(names, "файлы");
    }

    @Test(description = "Проверка названия существующего файла", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"191805"})
    public void successCheckExistFilenameTest() {
        List<String> nameList = Collections.singletonList(names.get(0));
        List<String> resultList = metadataStepDef.getExistMetadata(nameList);
        Assert.compareParameters(nameList, resultList, "файлы");
    }

    @Test(description = "Проверка названий существующих файлов", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"191806"})
    public void successCheckExistFilenamesTest() {
        List<String> resultList = metadataStepDef.getExistMetadata(names);
        Assert.compareParameters(names, resultList, "файлы");
    }

    @Test(description = "Проверка названий существующих и не существующих файлов", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"191807"})
    public void successCheckExistAndNoExistFilenamesTest() {
        List<String> namesList = new ArrayList<>(names);
        namesList.add(RandomStringUtils.randomNumeric(3));

        List<String> resultList = metadataStepDef.getExistMetadata(namesList);
        Assert.compareParameters(names, resultList, "файлы");
    }
}
