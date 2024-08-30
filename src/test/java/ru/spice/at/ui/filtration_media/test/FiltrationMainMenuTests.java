package ru.spice.at.ui.filtration_media.test;

import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import ru.testit.annotations.WorkItemIds;
import org.testng.annotations.*;
import ru.spice.at.api.dto.response.metadata.DictionariesItem;
import ru.spice.at.api.dto.response.metadata.UsersItem;
import ru.spice.at.api.metadata_service.MetadataStepDef;
import ru.spice.at.common.base_test.BaseUiTest;
import ru.spice.at.common.dto.dam.ImageData;
import ru.spice.at.common.emuns.dam.ImageFormat;
import ru.spice.at.common.emuns.UiCategories;
import ru.spice.at.common.listeners.TestAllureListener;
import ru.spice.at.ui.filtration_media.FiltrationMediaSettings;
import ru.spice.at.ui.filtration_media.FiltrationMediaStepDef;

import java.util.ArrayList;
import java.util.List;

import static ru.spice.at.common.emuns.dam.ImageParameters.*;
import static ru.spice.at.common.emuns.dam.Quality.*;
import static ru.spice.at.common.emuns.dam.Source.BRAND;
import static ru.spice.at.common.emuns.dam.Source.RETAILER;
import static ru.spice.at.common.emuns.dam.Status.ARCHIVE;
import static ru.spice.at.common.emuns.dam.Status.IN_PROGRESS;

@Feature("Filtration media")
@Story("Filtration main menu")
@Listeners({TestAllureListener.class})
public class FiltrationMainMenuTests extends BaseUiTest<FiltrationMediaSettings> {
    private final static int COUNT_METADATA = 5;
    private final List<FiltrationMediaStepDef.Image> images = new ArrayList<>();
    private FiltrationMediaStepDef filtrationMediaStepDef;
    private MetadataStepDef metadataStepDef;

    protected FiltrationMainMenuTests() {
        super(UiCategories.FILTRATION_MEDIA);
    }

    @BeforeClass(alwaysRun = true)
    public void beforeClass() {
        filtrationMediaStepDef = new FiltrationMediaStepDef(getWebDriver());
        metadataStepDef = new MetadataStepDef(filtrationMediaStepDef.getAuthToken());

        for (int i = 0; i < COUNT_METADATA; i++) {
            ImageData imageData = new ImageData(ImageFormat.JPEG);
            String id = metadataStepDef.createMetadataImage(imageData);
            images.add(new FiltrationMediaStepDef.Image(imageData.getFilename(), id));
        }
    }

    @AfterClass(alwaysRun = true)
    public void afterClass() {
        metadataStepDef.deleteMetadata();
    }

    @BeforeMethod(alwaysRun = true)
    public void beforeMethod() {
        filtrationMediaStepDef.goToBaseUrlWithLogin(getWebDriver());
    }

    @Test(description = "Фильтрация по полю 'Качество'", timeOut = 60000, groups = {"regress"})
    @WorkItemIds({"225888"})
    public void successFiltrationQualityTest() {
        String qualityId = metadataStepDef.getListQualitiesMetadata().
                stream().filter(item -> item.name().equals(TO_REVISION.getName())).
                map(DictionariesItem::id).findFirst().orElse(null);
        metadataStepDef.successEditMetadata(images.get(0).id(), QUALITY_ID.getName(), qualityId);

        filtrationMediaStepDef.chooseMainMenuFilter(TO_REVISION);
        filtrationMediaStepDef.checkFiltrationFile(images.get(0).filename());
    }

    @Test(description = "Фильтрация по полю 'Статус'", timeOut = 60000, groups = {"regress"})
    @WorkItemIds({"225886"})
    public void successFiltrationStatusTest() {
        String statusId = metadataStepDef.getListStatusesMetadata()
                .stream().filter(item -> item.name().equals(IN_PROGRESS.getName()))
                .map(DictionariesItem::id).findFirst().orElse(null);
        metadataStepDef.successEditMetadata(images.get(1).id(), STATUS_ID.getName(), statusId);

        filtrationMediaStepDef.chooseMainMenuFilter(IN_PROGRESS);
        filtrationMediaStepDef.checkFiltrationFile(images.get(1).filename());
    }

    @Test(description = "Фильтрация по полю 'Источник'", timeOut = 60000, groups = {"regress"})
    @WorkItemIds({"225885"})
    public void successFiltrationSourceTest() {
        String sourceId = metadataStepDef.getListSourcesMetadata()
                .stream().filter(item -> item.name().equals(BRAND.getName())).
                map(DictionariesItem::id).findFirst().orElse(null);
        metadataStepDef.successEditMetadata(images.get(2).id(), SOURCE_ID.getName(), sourceId);

        filtrationMediaStepDef.chooseMainMenuFilter(BRAND);
        filtrationMediaStepDef.checkFiltrationFile(images.get(2).filename());
    }

    @Test(description = "Фильтрация по полю 'Исполнитель'", timeOut = 60000, groups = {"regress"})
    @WorkItemIds({"225895"})
    public void successFiltrationAssigneeTest() {
        UsersItem usersItem = metadataStepDef.getListUsersMetadata().get(0);
        metadataStepDef.successEditMetadata(images.get(4).id(), ASSIGNEE_ID.getName(), usersItem.id());

        filtrationMediaStepDef.chooseMainMenuFilter(usersItem);
        filtrationMediaStepDef.checkFiltrationFile(images.get(4).filename());
    }

    @Test(description = "Фильтрация по всем полям (главное меню)", timeOut = 60000, groups = {"regress"})
    @WorkItemIds({"225887"})
    public void successFiltrationAllMainMenuTest() {
        String qualityId = metadataStepDef.getListQualitiesMetadata().
                stream().filter(item -> item.name().equals(BAD.getName())).
                map(DictionariesItem::id).findFirst().orElse(null);
        metadataStepDef.successEditMetadata(images.get(3).id(), QUALITY_ID.getName(), qualityId);

        String statusId = metadataStepDef.getListStatusesMetadata()
                .stream().filter(item -> item.name().equals(ARCHIVE.getName()))
                .map(DictionariesItem::id).findFirst().orElse(null);
        metadataStepDef.successEditMetadata(images.get(3).id(), STATUS_ID.getName(), statusId);

        String sourceId = metadataStepDef.getListSourcesMetadata()
                .stream().filter(item -> item.name().equals(RETAILER.getName())).
                map(DictionariesItem::id).findFirst().orElse(null);
        metadataStepDef.successEditMetadata(images.get(3).id(), SOURCE_ID.getName(), sourceId);

        UsersItem usersItem = metadataStepDef.getListUsersMetadata().get(1);
        metadataStepDef.successEditMetadata(images.get(3).id(), ASSIGNEE_ID.getName(), usersItem.id());

        filtrationMediaStepDef.chooseMainMenuFilter(BAD);
        filtrationMediaStepDef.chooseMainMenuFilter(ARCHIVE, false);
        filtrationMediaStepDef.chooseMainMenuFilter(RETAILER, false);

        filtrationMediaStepDef.checkFiltrationFile(images.get(3).filename());
    }
}
