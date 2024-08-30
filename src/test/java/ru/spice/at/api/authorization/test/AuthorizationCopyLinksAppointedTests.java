package ru.spice.at.api.authorization.test;

import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import ru.testit.annotations.WorkItemIds;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import ru.spice.at.api.authorization.AuthorizationSettings;
import ru.spice.at.api.dto.request.metadata.RequestMultiEditData;
import ru.spice.at.api.dto.response.metadata.UsersItem;
import ru.spice.at.api.dto.response.metadata.filtration.DataItem;
import ru.spice.at.api.import_service.ImportServiceStepDef;
import ru.spice.at.api.metadata_service.MetadataStepDef;
import ru.spice.at.common.base_test.AbstractApiStepDef;
import ru.spice.at.common.base_test.BaseApiTest;
import ru.spice.at.common.dto.User;
import ru.spice.at.common.dto.dam.ImageData;
import ru.spice.at.common.emuns.ApiServices;
import ru.spice.at.common.emuns.Role;
import ru.spice.at.common.emuns.dam.ImageFormat;
import ru.spice.at.common.utils.Assert;

import java.util.*;
import java.util.stream.Collectors;

import static ru.spice.at.common.emuns.Role.*;

@Feature("Authorization")
@Story("Copy links")
public class AuthorizationCopyLinksAppointedTests extends BaseApiTest<AuthorizationSettings> {
    private final ImportServiceStepDef importServiceStepDef;
    private final MetadataStepDef metadataStepDef;

    private final Map<Role, String> imageRoleIds = new HashMap<>();

    protected AuthorizationCopyLinksAppointedTests() {
        super(ApiServices.AUTHORIZATION);
        importServiceStepDef = new ImportServiceStepDef();
        metadataStepDef = new MetadataStepDef();
    }

    @BeforeMethod(alwaysRun = true)
    public void beforeMethod() {
        Arrays.stream(Role.values()).forEach(role -> {
                    importServiceStepDef.setRole(role);
                    imageRoleIds.put(role, importServiceStepDef.importRandomImages(new ImageData().setFormat(ImageFormat.JPEG)));
                }
        );
    }

    //todo выключено для использования параллельного сьюта
    //@AfterClass(alwaysRun = true)
    public void afterClass() {
        metadataStepDef.deleteMetadata();
    }

    @Test(description = "Успешный просмотр файлов по ссылке - роль Photoproduction Outsource (файлы назначен Administrator)",
            timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"248804"})
    public void successPhotoproductionOutsourceAppointedAdministratorTest() {
        metadataStepDef.setRole(ADMINISTRATOR);

        List<String> expIds = new LinkedList<>();
        imageRoleIds.forEach((k, v) -> expIds.add(v));
        String qLink = metadataStepDef.successGetQLink(expIds);

        String name = AbstractApiStepDef.standProperties.getUsers().stream().
                filter(user -> user.role().equals(PHOTOPRODUCTION_OUTSOURCE)).
                map(User::name).findFirst().orElse(null);
        String assigneeId = metadataStepDef.getListUsersMetadata(PHOTOPRODUCTION_OUTSOURCE.getFullName()).stream().
                filter(user -> user.fullName().equals(name)).
                map(UsersItem::id).findFirst().orElseThrow(() -> new RuntimeException("Пользователь не найден"));
        Assert.notNullOrEmptyParameter(name, "id Фотопродакшена Аутсорс");

        RequestMultiEditData requestMultiEditData = RequestMultiEditData.builder()
                .ids(expIds).assigneeId(assigneeId).build();
        metadataStepDef.successMultiEditMetadata(requestMultiEditData);

        metadataStepDef.setRole(PHOTOPRODUCTION_OUTSOURCE);
        List<String> actualIds = metadataStepDef.successMetadataSearchingQLink(qLink).stream().map(DataItem::getId).collect(Collectors.toList());

        Assert.compareParameters(expIds, new LinkedList<>(actualIds), "id файлов");
    }

    //todo неактуальный для ролевой на настоящий момент, удалить
    @Deprecated
    //@Test(description = "Успешный просмотр файлов по ссылке - роль Photoproduction Outsource (файлы назначен Photoproduction)",
    //        timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"248812"})
    public void successContentProductionOutsourceAppointedPhotoproductionTest() {
        metadataStepDef.setRole(PHOTOPRODUCTION);

        List<String> expIds = new LinkedList<>();
        imageRoleIds.forEach((k, v) -> expIds.add(v));
        String qLink = metadataStepDef.successGetQLink(expIds);

        String name = AbstractApiStepDef.standProperties.getUsers().stream().
                filter(user -> user.role().equals(CONTENT_PRODUCTION_OUTSOURCE)).
                map(User::name).findFirst().orElse(null);
        String assigneeId = metadataStepDef.getListUsersMetadata().stream().
                filter(user -> user.fullName().equals(name)).
                map(UsersItem::id).findFirst().orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        RequestMultiEditData requestMultiEditData = RequestMultiEditData.builder()
                .ids(expIds).assigneeId(assigneeId).build();
        metadataStepDef.successMultiEditMetadata(requestMultiEditData);

        metadataStepDef.setRole(CONTENT_PRODUCTION_OUTSOURCE);
        List<String> actualIds = metadataStepDef.successMetadataSearchingQLink(qLink).stream().map(DataItem::getId).collect(Collectors.toList());

        Assert.compareParameters(expIds, new LinkedList<>(actualIds), "id файлов");
    }
}
