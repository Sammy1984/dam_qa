package ru.spice.at.api.authorization.test;

import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import ru.spice.at.common.utils.Assert;
import ru.testit.annotations.WorkItemIds;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import ru.spice.at.api.authorization.AuthorizationSettings;
import ru.spice.at.api.authorization.AuthorizationStepDef;
import ru.spice.at.api.dto.request.metadata.RequestMultiEditData;
import ru.spice.at.api.dto.response.metadata.DictionariesItem;
import ru.spice.at.api.dto.response.metadata.UsersItem;
import ru.spice.at.api.dto.response.metadata.filtration.DataItem;
import ru.spice.at.api.import_service.ImportServiceStepDef;
import ru.spice.at.api.metadata_service.MetadataStepDef;
import ru.spice.at.common.base_test.BaseApiTest;
import ru.spice.at.common.dto.dam.ImageData;
import ru.spice.at.common.emuns.ApiServices;
import ru.spice.at.common.emuns.Role;
import ru.spice.at.common.emuns.dam.ImageFormat;

import java.util.*;
import java.util.stream.Collectors;

import static ru.spice.at.common.emuns.dam.SearchParameters.SEARCH;
import static ru.spice.at.common.emuns.Role.*;
import static ru.spice.at.common.emuns.dam.ImageParameters.*;
import static ru.spice.at.common.utils.Assert.*;

@Feature("Authorization")
@Story("Filtration and search media")
public class AuthorizationFiltrationSearchMediaTests extends BaseApiTest<AuthorizationSettings> {
    private final ImportServiceStepDef importServiceStepDef;
    private final MetadataStepDef metadataStepDef;
    private final AuthorizationStepDef authorizationStepDef;

    private List<String> administratorIds;
    private String categoryId;
    private List<UsersItem> users;
    private ImageData imageData;

    protected AuthorizationFiltrationSearchMediaTests() {
        super(ApiServices.AUTHORIZATION);
        importServiceStepDef = new ImportServiceStepDef();
        metadataStepDef = new MetadataStepDef();
        authorizationStepDef = new AuthorizationStepDef();
    }

    @BeforeClass(alwaysRun = true)
    public void beforeClass() {
        metadataStepDef.deleteMetadata();
        users = metadataStepDef.getListUsersMetadata();

        List<DictionariesItem> categories = metadataStepDef.getListCategoriesMetadata();
        categoryId = categories.get(0).id();
    }

    @BeforeMethod(alwaysRun = true)
    public void beforeMethod() {
        imageData = new ImageData(ImageFormat.JPEG);

        List<ImageData> imageDataList = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            imageDataList.add(new ImageData(ImageFormat.JPEG));
        }
        importServiceStepDef.setRole(ADMINISTRATOR);
        imageDataList.add(imageData);
        administratorIds = imageDataList.stream().map(importServiceStepDef::importRandomImages).collect(Collectors.toList());
    }

    @AfterMethod(alwaysRun = true)
    public void afterMethod() {
        metadataStepDef.deleteMetadata();
    }

    @Test(description = "Успешный поиск и фильтрация медиа - роль Administrator", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"227026"})
    public void successAdministratorFiltrationSearchTest() {
        importServiceStepDef.setRole(CONTENT_PRODUCTION);
        ImageData imageDataContentProduction =  new ImageData().setFormat(ImageFormat.JPEG);
        importServiceStepDef.importRandomImages(imageDataContentProduction);
        String assigneeIdContentProduction = metadataStepDef.getListUsersMetadata(CONTENT_PRODUCTION.getFullName()).get(0).id();

        importServiceStepDef.setRole(CONTENT_PRODUCTION_OUTSOURCE);
        ImageData imageDataContentProductionOutsource =  new ImageData().setFormat(ImageFormat.JPEG);
        importServiceStepDef.importRandomImages(imageDataContentProductionOutsource);
        String assigneeIdContentProductionOutsource = metadataStepDef.getListUsersMetadata(CONTENT_PRODUCTION_OUTSOURCE.getFullName()).get(0).id();
        Assert.notNullOrEmptyParameter(assigneeIdContentProductionOutsource, "id Контент Продакшена Аутсорс");

        metadataStepDef.setRole(ADMINISTRATOR);
        List<DataItem> dataItems = metadataStepDef.successMetadataSearching(
                CREATED_BY.getName(), Arrays.asList(assigneeIdContentProduction, assigneeIdContentProductionOutsource), false);
        assertAll(
                () -> compareParameters(2, dataItems.size(), "data.size()"),
                () -> compareParameters(
                        Arrays.asList(imageDataContentProduction.getFilename(), imageDataContentProductionOutsource.getFilename()),
                        dataItems.stream().map(DataItem::getFilename).collect(Collectors.toList()), "data.filename")
        );
    }

    @Test(description = "Успешный поиск и фильтрация медиа - роль Photoproduction", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"227028"})
    public void successPhotoproductionFiltrationSearchTest() {
        String assigneeId = authorizationStepDef.getAssigneeId(users, ADMINISTRATOR);
        Map<String, Object> queryParam = new HashMap<String, Object>() {{
            put(SEARCH.getName(), imageData.getFilename().split("\\.", 2)[0]);
            put(CREATED_BY.getName(), Collections.singletonList(assigneeId));
        }};

        metadataStepDef.setRole(PHOTOPRODUCTION);
        List<DataItem> dataItems = metadataStepDef.successMetadataSearching(
                queryParam, false);
        assertAll(
                () -> compareParameters(1, dataItems.size(), "data.size()"),
                () -> compareParameters(imageData.getFilename(), dataItems.get(0).getFilename(), "data.filename")
        );
    }

    @Test(description = "Успешный поиск и фильтрация медиа - роль Content Production", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"227029"})
    public void successContentProductionFiltrationSearchTest() {
        String assigneeId = authorizationStepDef.getAssigneeId(users, ADMINISTRATOR);
        Map<String, Object> queryParam = new HashMap<String, Object>() {{
            put(SEARCH.getName(), imageData.getFilename().split("\\.", 2)[0]);
            put(CREATED_BY.getName(), Collections.singletonList(assigneeId));
        }};

        metadataStepDef.setRole(CONTENT_PRODUCTION);
        List<DataItem> dataItems = metadataStepDef.successMetadataSearching(
                queryParam, false);
        assertAll(
                () -> compareParameters(1, dataItems.size(), "data.size()"),
                () -> compareParameters(imageData.getFilename(), dataItems.get(0).getFilename(), "data.filename")
        );
    }

    @Test(description = "Успешный поиск и фильтрация медиа - роль Content Support", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"227030"})
    public void successContentSupportFiltrationSearchTest() {
        importServiceStepDef.setRole(CONTENT_PRODUCTION_OUTSOURCE);
        ImageData imageDataContentProductionOutsource =  new ImageData().setFormat(ImageFormat.JPEG);
        importServiceStepDef.importRandomImages(imageDataContentProductionOutsource);
        String assigneeIdContentProductionOutsource = metadataStepDef.getListUsersMetadata(CONTENT_PRODUCTION_OUTSOURCE.getFullName()).get(0).id();
        String assigneeIdContentSupport = metadataStepDef.getListUsersMetadata(CONTENT_SUPPORT.getFullName()).get(0).id();
        Assert.notNullOrEmptyParameter(assigneeIdContentProductionOutsource, "id Контент Продакшен Аутсорс");
        Assert.notNullOrEmptyParameter(assigneeIdContentSupport, "id Контент Саппорта");

        metadataStepDef.setRole(CONTENT_SUPPORT);
        List<DataItem> dataItems = metadataStepDef.successMetadataSearching(
                CREATED_BY.getName(), Arrays.asList(assigneeIdContentSupport, assigneeIdContentProductionOutsource), false);
        assertAll(
                () -> compareParameters(1, dataItems.size(), "data.size()"),
                () -> compareParameters(imageDataContentProductionOutsource.getFilename(), dataItems.get(0).getFilename(), "data.filename")
        );
    }

    @Test(description = "Успешный поиск и фильтрация медиа - роль Photoproduction Outsource", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"227031"})
    public void successPhotoproductionOutsourceFiltrationSearchTest() {
        metadataStepDef.setRole(ADMINISTRATOR);
        String assigneeId = metadataStepDef.getListUsersMetadata(PHOTOPRODUCTION_OUTSOURCE.getFullName()).get(0).id();
        Assert.notNullOrEmptyParameter(assigneeId, "id Фотопродакшена Аутсорс");

        RequestMultiEditData requestMultiEditData = RequestMultiEditData.builder()
                .ids(administratorIds).assigneeId(assigneeId).masterCategoryId(categoryId).build();
        metadataStepDef.successMultiEditMetadata(requestMultiEditData);

        metadataStepDef.setRole(PHOTOPRODUCTION_OUTSOURCE);
        HashMap<String, Object> queryParam = new HashMap<String, Object>() {{
            put(SEARCH.getName(), imageData.getFilename().split("\\.", 2)[0]);
            put(MASTER_CATEGORY_IDS.getName(), Collections.singletonList(categoryId));
        }};
        List<DataItem> dataItems = metadataStepDef.successMetadataSearching(queryParam, false);
        assertAll(
                () -> compareParameters(1, dataItems.size(), "data.size()"),
                () -> compareParameters(imageData.getFilename(), dataItems.get(0).getFilename(), "data.filename")
        );
    }

    @Test(description = "Успешный поиск и фильтрация медиа - роль Content Production Outsource", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"227032"})
    public void successContentProductionOutsourceFiltrationSearchTest() {
        metadataStepDef.setRole(ADMINISTRATOR);
        String assigneeId = metadataStepDef.getListUsersMetadata(CONTENT_PRODUCTION_OUTSOURCE.getFullName()).get(0).id();
        Assert.notNullOrEmptyParameter(assigneeId, "id Контент Продакшен Аутсорс");

        RequestMultiEditData requestMultiEditData = RequestMultiEditData.builder()
                .ids(administratorIds).assigneeId(assigneeId).masterCategoryId(categoryId).build();
        metadataStepDef.successMultiEditMetadata(requestMultiEditData);

        metadataStepDef.setRole(CONTENT_PRODUCTION_OUTSOURCE);
        HashMap<String, Object> queryParam = new HashMap<String, Object>() {{
            put(SEARCH.getName(), imageData.getFilename().split("\\.", 2)[0]);
            put(MASTER_CATEGORY_IDS.getName(), Collections.singletonList(categoryId));
        }};
        List<DataItem> dataItems = metadataStepDef.successMetadataSearching(queryParam, false);
        assertAll(
                () -> compareParameters(1, dataItems.size(), "data.size()"),
                () -> compareParameters(imageData.getFilename(), dataItems.get(0).getFilename(), "data.filename")
        );
    }

    @Test(description = "Неуспешный поиск и фильтрация медиа - роль Photoproduction Outsource", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"227033"})
    public void unsuccessfulPhotoproductionOutsourceFiltrationSearchTest() {
        metadataStepDef.setRole(ADMINISTRATOR);

        RequestMultiEditData requestMultiEditData = RequestMultiEditData.builder().ids(administratorIds).masterCategoryId(categoryId).build();
        metadataStepDef.successMultiEditMetadata(requestMultiEditData);

        metadataStepDef.setRole(PHOTOPRODUCTION_OUTSOURCE);
        HashMap<String, Object> queryParam = new HashMap<String, Object>() {{
            put(SEARCH.getName(), imageData.getSku());
            put(MASTER_CATEGORY_IDS.getName(), Collections.singletonList(categoryId));
        }};
        metadataStepDef.successMetadataSearching(queryParam, true);
    }

    @Test(description = "Неуспешный поиск и фильтрация медиа - роль Content Production Outsource", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"227034"})
    public void unsuccessfulContentProductionOutsourceFiltrationSearchTest() {
        metadataStepDef.setRole(ADMINISTRATOR);

        RequestMultiEditData requestMultiEditData = RequestMultiEditData.builder().ids(administratorIds).masterCategoryId(categoryId).build();
        metadataStepDef.successMultiEditMetadata(requestMultiEditData);

        metadataStepDef.setRole(CONTENT_PRODUCTION_OUTSOURCE);
        HashMap<String, Object> queryParam = new HashMap<String, Object>() {{
            put(SEARCH.getName(), imageData.getSku());
            put(MASTER_CATEGORY_IDS.getName(), Collections.singletonList(categoryId));
        }};
        metadataStepDef.successMetadataSearching(queryParam, true);
    }

    @Test(description = "Неуспешный поиск и фильтрация медиа - роль Photoproduction Outsource (forbidden)", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"227033"})
    public void unsuccessfulPhotoproductionOutsourceFiltrationSearchForbiddenTest() {
        metadataStepDef.setRole(PHOTOPRODUCTION_OUTSOURCE);
        String assigneeId = authorizationStepDef.getAssigneeId(users, ADMINISTRATOR);
        metadataStepDef.unsuccessfulForbiddenMetadataSearching(ASSIGNEE_IDS.getName(), Collections.singletonList(assigneeId));
    }

    @Test(description = "Неуспешный поиск и фильтрация медиа - роль Content Production Outsource (forbidden)", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"227034"})
    public void unsuccessfulContentProductionOutsourceFiltrationSearchForbiddenTest() {
        metadataStepDef.setRole(CONTENT_PRODUCTION_OUTSOURCE);
        String assigneeId = authorizationStepDef.getAssigneeId(users, ADMINISTRATOR);
        metadataStepDef.unsuccessfulForbiddenMetadataSearching(CREATED_BY.getName(), Collections.singletonList(assigneeId));
    }
}
