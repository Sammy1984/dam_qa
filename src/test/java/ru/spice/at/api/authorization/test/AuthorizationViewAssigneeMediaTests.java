package ru.spice.at.api.authorization.test;

import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import ru.testit.annotations.WorkItemIds;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.spice.at.api.authorization.AuthorizationSettings;
import ru.spice.at.api.dto.request.metadata.RequestMultiEditData;
import ru.spice.at.api.dto.response.metadata.UsersItem;
import ru.spice.at.api.import_service.ImportServiceStepDef;
import ru.spice.at.api.metadata_service.MetadataStepDef;
import ru.spice.at.common.base_test.AbstractApiStepDef;
import ru.spice.at.common.base_test.BaseApiTest;
import ru.spice.at.common.dto.User;
import ru.spice.at.common.dto.dam.ImageData;
import ru.spice.at.common.emuns.ApiServices;
import ru.spice.at.common.emuns.dam.ImageFormat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static ru.spice.at.common.emuns.Role.*;

@Feature("Authorization")
@Story("View media")
public class AuthorizationViewAssigneeMediaTests extends BaseApiTest<AuthorizationSettings> {
    private final ImportServiceStepDef importServiceStepDef;
    private final MetadataStepDef metadataStepDef;

    protected AuthorizationViewAssigneeMediaTests() {
        super(ApiServices.AUTHORIZATION);
        importServiceStepDef = new ImportServiceStepDef();
        metadataStepDef = new MetadataStepDef();
    }

    @BeforeClass(alwaysRun = true)
    public void beforeClass() {
        metadataStepDef.deleteMetadata();
    }

    @AfterMethod(alwaysRun = true)
    public void afterMethod() {
        metadataStepDef.setRole(ADMINISTRATOR);
        metadataStepDef.deleteMetadata();
    }

    @Test(description = "Успешный просмотр медиа - роль Photoproduction Outsource (с назначением)", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"226829"})
    public void successPhotoproductionOutsourceViewAssigneeTest() {
        List<String> administratorIds = new ArrayList<>();
        Arrays.stream(values()).forEach(role -> {
                    importServiceStepDef.setRole(role);
                    administratorIds.add(importServiceStepDef.importRandomImages(new ImageData().setFormat(ImageFormat.JPEG)));
                }
        );

        metadataStepDef.setRole(ADMINISTRATOR);
        String name = AbstractApiStepDef.standProperties.getUsers().stream().
                filter(user -> user.role().equals(PHOTOPRODUCTION_OUTSOURCE)).
                map(User::name).findFirst().orElse(null);
        String assigneeId = metadataStepDef.getListUsersMetadata(PHOTOPRODUCTION_OUTSOURCE.getFullName()).stream().
                filter(user -> user.fullName().equals(name)).
                map(UsersItem::id).findFirst().orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        RequestMultiEditData requestMultiEditData = RequestMultiEditData.builder()
                .ids(administratorIds).assigneeId(assigneeId).build();

        metadataStepDef.successMultiEditMetadata(requestMultiEditData);

        metadataStepDef.setRole(PHOTOPRODUCTION_OUTSOURCE);
        metadataStepDef.getMediaFilesMetadata(administratorIds.size());
    }

    @Test(description = "Неуспешный просмотр медиа - роль Photoproduction Outsource", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"226832"})
    public void unsuccessfulPhotoproductionOutsourceViewTest() {
        Arrays.stream(values()).filter(role -> role != PHOTOPRODUCTION_OUTSOURCE).forEach(role -> {
                    importServiceStepDef.setRole(role);
                    importServiceStepDef.importRandomImages(new ImageData().setFormat(ImageFormat.JPEG));
                }
        );

        metadataStepDef.setRole(PHOTOPRODUCTION_OUTSOURCE);
        metadataStepDef.getMediaFilesMetadata(0);
    }

    @Test(description = "Успешный просмотр медиа - роль Content Production Outsource (с назначением)", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"226830"})
    public void successContentProductionOutsourceViewAssigneeTest() {
        List<String> administratorIds = new ArrayList<>();
        Arrays.stream(values()).forEach(role -> {
                    importServiceStepDef.setRole(role);
                    administratorIds.add(importServiceStepDef.importRandomImages(new ImageData().setFormat(ImageFormat.JPEG)));
                }
        );
        String name = AbstractApiStepDef.standProperties.getUsers().stream().
                filter(user -> user.role().equals(CONTENT_PRODUCTION_OUTSOURCE)).
                map(User::name).findFirst().orElse(null);
        String assigneeId = metadataStepDef.getListUsersMetadata(CONTENT_PRODUCTION_OUTSOURCE.getFullName()).stream().
                filter(user -> user.fullName().equals(name)).
                map(UsersItem::id).findFirst().orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        RequestMultiEditData requestMultiEditData = RequestMultiEditData.builder()
                .ids(administratorIds).assigneeId(assigneeId).build();

        metadataStepDef.successMultiEditMetadata(requestMultiEditData);

        metadataStepDef.setRole(CONTENT_PRODUCTION_OUTSOURCE);
        metadataStepDef.getMediaFilesMetadata(administratorIds.size());
    }

    @Test(description = "Неуспешный просмотр медиа - роль Content Production Outsource", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"226836"})
    public void unsuccessfulContentProductionOutsourceViewTest() {
        Arrays.stream(values()).filter(role -> role != CONTENT_PRODUCTION_OUTSOURCE).forEach(role -> {
                    importServiceStepDef.setRole(role);
                    importServiceStepDef.importRandomImages(new ImageData().setFormat(ImageFormat.JPEG));
                }
        );

        metadataStepDef.setRole(CONTENT_PRODUCTION_OUTSOURCE);
        metadataStepDef.getMediaFilesMetadata(0);
    }
}