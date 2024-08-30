package ru.spice.at.ui.filtration_media.test;

import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import ru.testit.annotations.WorkItemIds;
import org.testng.annotations.*;
import ru.spice.at.api.dto.response.metadata.DictionariesItem;
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

import static ru.spice.at.common.emuns.dam.ImageParameters.QUALITY_ID;
import static ru.spice.at.common.emuns.dam.Quality.*;

@Feature("Filtration media")
@Story("Filtration sorting")
@Listeners({TestAllureListener.class})
public class FiltrationSortingTests extends BaseUiTest<FiltrationMediaSettings> {
    private final static int COUNT_METADATA = 2;
    private final List<FiltrationMediaStepDef.Image> images = new ArrayList<>();
    private FiltrationMediaStepDef filtrationMediaStepDef;
    private MetadataStepDef metadataStepDef;

    protected FiltrationSortingTests() {
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
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
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

    @Test(description = "Сортировка 'По дате загрузки (возрастание)'", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"225892"})
    public void successSearchCreatedAscTest() {
        filtrationMediaStepDef.chooseSearchFilter(getData().createdAsc());
        filtrationMediaStepDef.checkSortFile(images.get(0).filename(), images.size());
    }

    @Test(description = "Сортировка 'По дате загрузки (убывание)'", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"225891"})
    public void successSearchCreatedDescTest() {
        filtrationMediaStepDef.chooseSearchFilter(getData().createdDesc());
        filtrationMediaStepDef.checkSortFile(images.get(1).filename(), images.size());
    }

    @Test(description = "Сортировка 'По дате изменения (возрастание)'", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"225889"})
    public void successSearchUpdatedAscTest() {
        String qualityId = metadataStepDef.getListQualitiesMetadata().
                stream().filter(item -> item.name().equals(BAD.getName())).
                map(DictionariesItem::id).findFirst().orElse(null);
        metadataStepDef.successEditMetadata(images.get(0).id(), QUALITY_ID.getName(), qualityId);

        filtrationMediaStepDef.chooseSearchFilter(getData().updatedAsc());
        filtrationMediaStepDef.checkSortFile(images.get(1).filename(), images.size());
    }

    @Test(description = "Сортировка 'По дате изменения (убывание)'", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"225890"})
    public void successSearchUpdatedDescTest() {
        String qualityId = metadataStepDef.getListQualitiesMetadata().
                stream().filter(item -> item.name().equals(TO_REVISION.getName())).
                map(DictionariesItem::id).findFirst().orElse(null);
        metadataStepDef.successEditMetadata(images.get(0).id(), QUALITY_ID.getName(), qualityId);

        filtrationMediaStepDef.chooseSearchFilter(getData().updatedDesc());
        filtrationMediaStepDef.checkSortFile(images.get(0).filename(), images.size());
    }
}
