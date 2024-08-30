package ru.spice.at.api.authorization.test;

import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import ru.testit.annotations.WorkItemIds;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.spice.at.api.authorization.AuthorizationSettings;
import ru.spice.at.api.dto.response.metadata.filtration.DataItem;
import ru.spice.at.api.import_service.ImportServiceStepDef;
import ru.spice.at.api.metadata_service.MetadataStepDef;
import ru.spice.at.common.base_test.BaseApiTest;
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
public class AuthorizationCopyLinksTests extends BaseApiTest<AuthorizationSettings> {
    private final ImportServiceStepDef importServiceStepDef;
    private final MetadataStepDef metadataStepDef;

    private final Map<Role, String> imageRoleIds = new HashMap<>();

    protected AuthorizationCopyLinksTests() {
        super(ApiServices.AUTHORIZATION);
        importServiceStepDef = new ImportServiceStepDef();
        metadataStepDef = new MetadataStepDef();
    }

    @BeforeClass(alwaysRun = true)
    public void beforeClass() {
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

    @Test(description = "Успешный просмотр файлов по ссылке - роль Administrator", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"248798"})
    public void successAdministratorTest() {
        metadataStepDef.setRole(ADMINISTRATOR);

        List<String> expIds = new LinkedList<>();
        imageRoleIds.forEach((k, v) -> expIds.add(v));
        String qLink = metadataStepDef.successGetQLink(expIds);

        List<String> actualIds = metadataStepDef.successMetadataSearchingQLink(qLink).stream().map(DataItem::getId).collect(Collectors.toList());

        Assert.compareParameters(expIds, new LinkedList<>(actualIds), "id файлов");
    }

    @Test(description = "Успешный просмотр файлов по ссылке - роль Administrator (копирует Content Support)", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"248798"})
    public void successAdministratorContentSupportTest() {
        metadataStepDef.setRole(CONTENT_SUPPORT);

        List<String> expIds = new LinkedList<>();
        imageRoleIds.forEach((k, v) -> expIds.add(v));
        String qLink = metadataStepDef.successGetQLink(expIds);

        metadataStepDef.setRole(ADMINISTRATOR);
        List<String> actualIds = metadataStepDef.successMetadataSearchingQLink(qLink).stream().map(DataItem::getId).collect(Collectors.toList());

        Assert.compareParameters(expIds, new LinkedList<>(actualIds), "id файлов");
    }

    @Test(description = "Успешный просмотр файлов по ссылке - роль Administrator (копирует Content Production Outsource)", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"248798"})
    public void successAdministratorContentProductionOutsourceTest() {
        metadataStepDef.setRole(CONTENT_PRODUCTION_OUTSOURCE);

        List<String> expIds = new LinkedList<>();
        imageRoleIds.forEach((k, v) -> {
            if (k.equals(CONTENT_PRODUCTION_OUTSOURCE)) {
                expIds.add(v);
            }
        });
        String qLink = metadataStepDef.successGetQLink(expIds);

        metadataStepDef.setRole(ADMINISTRATOR);
        List<String> actualIds = metadataStepDef.successMetadataSearchingQLink(qLink).stream().map(DataItem::getId).collect(Collectors.toList());

        Assert.compareParameters(expIds, new LinkedList<>(actualIds), "id файлов");
    }

    @Test(description = "Успешный просмотр файлов по ссылке - роль Photoproduction", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"248799"})
    public void successPhotoproductionTest() {
        metadataStepDef.setRole(PHOTOPRODUCTION);

        List<String> expIds = new LinkedList<>();
        imageRoleIds.forEach((k, v) -> expIds.add(v));
        String qLink = metadataStepDef.successGetQLink(expIds);

        List<String> actualIds = metadataStepDef.successMetadataSearchingQLink(qLink).stream().map(DataItem::getId).collect(Collectors.toList());

        Assert.compareParameters(expIds, new LinkedList<>(actualIds), "id файлов");
    }

    @Test(description = "Успешный просмотр файлов по ссылке - роль Content Production", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"248801"})
    public void successContentProductionTest() {
        metadataStepDef.setRole(ADMINISTRATOR);

        List<String> expIds = new LinkedList<>();
        imageRoleIds.forEach((k, v) -> expIds.add(v));
        String qLink = metadataStepDef.successGetQLink(expIds);

        metadataStepDef.setRole(CONTENT_PRODUCTION);
        List<String> actualIds = metadataStepDef.successMetadataSearchingQLink(qLink).stream().map(DataItem::getId).collect(Collectors.toList());

        Assert.compareParameters(expIds, new LinkedList<>(actualIds), "id файлов");
    }

    @Test(description = "Успешный просмотр файлов по ссылке - роль Content Support", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"248802"})
    public void successContentSupportTest() {
        metadataStepDef.setRole(CONTENT_SUPPORT);

        List<String> expIds = new LinkedList<>();
        imageRoleIds.forEach((k, v) -> expIds.add(v));
        String qLink = metadataStepDef.successGetQLink(expIds);

        List<String> actualIds = metadataStepDef.successMetadataSearchingQLink(qLink).stream().map(DataItem::getId).collect(Collectors.toList());

        Assert.compareParameters(expIds, new LinkedList<>(actualIds), "id файлов");
    }

    @Test(description = "Успешный просмотр файлов по ссылке - роль Content Support (копирует Photoproduction)", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"248802"})
    public void successContentSupportPhotoproductionTest() {
        metadataStepDef.setRole(PHOTOPRODUCTION);

        List<String> expIds = new LinkedList<>();
        imageRoleIds.forEach((k, v) -> expIds.add(v));
        String qLink = metadataStepDef.successGetQLink(expIds);

        metadataStepDef.setRole(CONTENT_SUPPORT);
        List<String> actualIds = metadataStepDef.successMetadataSearchingQLink(qLink).stream().map(DataItem::getId).collect(Collectors.toList());

        Assert.compareParameters(expIds, new LinkedList<>(actualIds), "id файлов");
    }

    @Test(description = "Успешный просмотр файлов по ссылке - роль Photoproduction Outsource", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"248804"})
    public void successPhotoproductionOutsourceTest() {
        metadataStepDef.setRole(PHOTOPRODUCTION_OUTSOURCE);

        List<String> expIds = new LinkedList<>();
        imageRoleIds.forEach((k, v) -> {
            if (k.equals(PHOTOPRODUCTION_OUTSOURCE)) {
                expIds.add(v);
            }
        });

        String qLink = metadataStepDef.successGetQLink(expIds);

        List<String> actualIds = metadataStepDef.successMetadataSearchingQLink(qLink).stream().map(DataItem::getId).collect(Collectors.toList());

        Assert.compareParameters(expIds, new LinkedList<>(actualIds), "id файлов");
    }

    @Test(description = "Успешный просмотр файлов по ссылке - роль Photoproduction Outsource (доступные и недоступные файлы," +
            " копирует Administrator)",
            timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"248804"})
    public void successPhotoproductionOutsourceAdministratorTest() {
        metadataStepDef.setRole(ADMINISTRATOR);

        List<String> copyIds = new LinkedList<>();
        imageRoleIds.forEach((k, v) -> copyIds.add(v));
        String qLink = metadataStepDef.successGetQLink(copyIds);

        metadataStepDef.setRole(PHOTOPRODUCTION_OUTSOURCE);
        List<String> actualIds = metadataStepDef.successMetadataSearchingQLink(qLink).stream().map(DataItem::getId).collect(Collectors.toList());

        List<String> expIds = new LinkedList<>();
        imageRoleIds.forEach((k, v) -> {
            if (k.equals(PHOTOPRODUCTION_OUTSOURCE)) {
                expIds.add(v);
            }
        });
        Assert.compareParameters(expIds, new LinkedList<>(actualIds), "id файлов");
    }

    @Test(description = "Успешный просмотр файлов по ссылке - роль Photoproduction Outsource (доступные и недоступные файлы)",
            timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"248804"})
    public void successPhotoproductionOutsourceSomeIdsTest() {
        metadataStepDef.setRole(PHOTOPRODUCTION_OUTSOURCE);

        List<String> copyIds = new LinkedList<>();
        imageRoleIds.forEach((k, v) -> copyIds.add(v));
        String qLink = metadataStepDef.successGetQLink(copyIds);

        List<String> actualIds = metadataStepDef.successMetadataSearchingQLink(qLink).stream().map(DataItem::getId).collect(Collectors.toList());

        List<String> expIds = new LinkedList<>();
        imageRoleIds.forEach((k, v) -> {
            if (k.equals(PHOTOPRODUCTION_OUTSOURCE)) {
                expIds.add(v);
            }
        });
        Assert.compareParameters(expIds, new LinkedList<>(actualIds), "id файлов");
    }

    @Test(description = "Неуспешный просмотр файлов по ссылке - роль Photoproduction Outsource", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"248807"})
    public void unsuccessfulPhotoproductionOutsourceNoIdsTest() {
        metadataStepDef.setRole(PHOTOPRODUCTION_OUTSOURCE);

        List<String> expIds = new LinkedList<>();
        imageRoleIds.forEach((k, v) -> {
            if (!k.equals(PHOTOPRODUCTION_OUTSOURCE)) {
                expIds.add(v);
            }
        });

        String qLink = metadataStepDef.successGetQLink(expIds);
        metadataStepDef.unsuccessfulForbiddenQLinkSearching(qLink);
    }

    @Test(description = "Успешный просмотр файлов по ссылке - роль Content Production Outsource", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"248812"})
    public void successContentProductionOutsourceTest() {
        metadataStepDef.setRole(CONTENT_PRODUCTION_OUTSOURCE);

        List<String> expIds = new LinkedList<>();
        imageRoleIds.forEach((k, v) -> {
            if (k.equals(CONTENT_PRODUCTION_OUTSOURCE)) {
                expIds.add(v);
            }
        });

        String qLink = metadataStepDef.successGetQLink(expIds);

        List<String> actualIds = metadataStepDef.successMetadataSearchingQLink(qLink).stream().map(DataItem::getId).collect(Collectors.toList());

        Assert.compareParameters(expIds, new LinkedList<>(actualIds), "id файлов");
    }

    @Test(description = "Успешный просмотр файлов по ссылке - роль Content Production Outsource (доступные и недоступные файлы," +
            " копирует Photoproduction)", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"248812"})
    public void successContentProductionOutsourcePhotoproductionTest() {
        metadataStepDef.setRole(PHOTOPRODUCTION);

        List<String> copyIds = new LinkedList<>();
        imageRoleIds.forEach((k, v) -> copyIds.add(v));
        String qLink = metadataStepDef.successGetQLink(copyIds);

        metadataStepDef.setRole(CONTENT_PRODUCTION_OUTSOURCE);
        List<String> actualIds = metadataStepDef.successMetadataSearchingQLink(qLink).stream().map(DataItem::getId).collect(Collectors.toList());

        List<String> expIds = new LinkedList<>();
        imageRoleIds.forEach((k, v) -> {
            if (k.equals(CONTENT_PRODUCTION_OUTSOURCE)) {
                expIds.add(v);
            }
        });
        Assert.compareParameters(expIds, new LinkedList<>(actualIds), "id файлов");
    }

    @Test(description = "Успешный просмотр файлов по ссылке - роль Content Production Outsource (доступные и недоступные файлы)",
            timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"248812"})
    public void successContentProductionOutsourceSomeIdsTest() {
        metadataStepDef.setRole(CONTENT_PRODUCTION_OUTSOURCE);

        List<String> copyIds = new LinkedList<>();
        imageRoleIds.forEach((k, v) -> copyIds.add(v));
        String qLink = metadataStepDef.successGetQLink(copyIds);

        List<String> actualIds = metadataStepDef.successMetadataSearchingQLink(qLink).stream().map(DataItem::getId).collect(Collectors.toList());

        List<String> expIds = new LinkedList<>();
        imageRoleIds.forEach((k, v) -> {
            if (k.equals(CONTENT_PRODUCTION_OUTSOURCE)) {
                expIds.add(v);
            }
        });
        Assert.compareParameters(expIds, new LinkedList<>(actualIds), "id файлов");
    }

    @Test(description = "Неуспешный просмотр файлов по ссылке - роль Content Production Outsource", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"248814"})
    public void unsuccessfulContentProductionOutsourceNoIdsTest() {
        metadataStepDef.setRole(CONTENT_PRODUCTION_OUTSOURCE);

        List<String> expIds = new LinkedList<>();
        imageRoleIds.forEach((k, v) -> {
            if (!k.equals(CONTENT_PRODUCTION_OUTSOURCE)) {
                expIds.add(v);
            }
        });

        String qLink = metadataStepDef.successGetQLink(expIds);
        metadataStepDef.unsuccessfulForbiddenQLinkSearching(qLink);
    }
}
