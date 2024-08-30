package ru.spice.at.ui.filtration_media.test;

import io.qameta.allure.Feature;
import io.qameta.allure.Issue;
import io.qameta.allure.Story;
import ru.testit.annotations.WorkItemIds;
import org.apache.commons.lang3.RandomStringUtils;
import org.testng.annotations.*;
import ru.spice.at.api.dto.response.metadata.DictionariesItem;
import ru.spice.at.api.dto.response.metadata.RetailersItem;
import ru.spice.at.api.metadata_service.MetadataStepDef;
import ru.spice.at.common.base_test.BaseUiTest;
import ru.spice.at.common.dto.dam.ImageData;
import ru.spice.at.common.emuns.UiCategories;
import ru.spice.at.common.emuns.dam.ImageFormat;
import ru.spice.at.common.emuns.dam.Status;
import ru.spice.at.common.listeners.TestAllureListener;
import ru.spice.at.ui.filtration_media.FiltrationMediaSettings;
import ru.spice.at.ui.filtration_media.FiltrationMediaStepDef;

import java.util.*;

import static ru.spice.at.common.constants.TestConstants.NINETY_NINE;
import static ru.spice.at.common.emuns.dam.ImageParameters.*;

@Feature("Filtration media")
@Story("Filtration view")
@Listeners({TestAllureListener.class})
public class FiltrationViewTests extends BaseUiTest<FiltrationMediaSettings> {
    private final static int COUNT_METADATA = 3;
    private List<FiltrationMediaStepDef.Image> images = new ArrayList<>();
    private FiltrationMediaStepDef filtrationMediaStepDef;
    private MetadataStepDef metadataStepDef;

    protected FiltrationViewTests() {
        super(UiCategories.FILTRATION_MEDIA);
    }

    @BeforeClass(alwaysRun = true)
    public void beforeClass() {
        filtrationMediaStepDef = new FiltrationMediaStepDef(getWebDriver());
        metadataStepDef = new MetadataStepDef();
        metadataStepDef.deleteMetadata();

        for (int i = 0; i < COUNT_METADATA; i++) {
            ImageData imageData = new ImageData(ImageFormat.JPEG);
            String id = metadataStepDef.createMetadataImage(imageData);
            images.add(new FiltrationMediaStepDef.Image(imageData.getFilename(), id));
        }
    }

    @BeforeMethod(alwaysRun = true)
    public void beforeMethod() {
        filtrationMediaStepDef.goToBaseUrlWithLogin(getWebDriver());
    }

    @AfterClass(alwaysRun = true)
    public void afterClass() {
        metadataStepDef.deleteMetadata();
        images = new ArrayList<>();
    }

    @Test(description = "Отображение файлов в виде плитки", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"260761"})
    public void successModuleViewTest() {
        filtrationMediaStepDef.changeView(true);
    }

    @Test(description = "Отображение файлов в виде списка", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"260762"})
    public void successListViewTest() {
        filtrationMediaStepDef.changeView(true);
        filtrationMediaStepDef.changeView(false);
    }

    @Issue("SPC-2870")
    @Test(description = "Отображение файлов в виде списка - проверка метаданных", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"260764"})
    public void successListMetadataViewTest() {
        String sku = RandomStringUtils.randomAlphabetic(5);
        Integer priority = new Random().nextInt(NINETY_NINE) + 1;
        RetailersItem retailersItem = metadataStepDef.getListRetailersMetadata().data().get(0);

        Map<String, Object> editValues = new HashMap<String, Object>() {{
            put(SKU.getName(), sku);
            put(PRIORITY.getName(), priority);
            put(MASTER_SELLER_ID.getName(), retailersItem.id());
        }};
        metadataStepDef.successEditMetadata(images.get(0).id(), editValues);

        filtrationMediaStepDef.changeView(false);
        filtrationMediaStepDef.checkMetadataListView(
                images.get(0).filename(), sku, String.valueOf(priority), retailersItem.name(), String.valueOf(retailersItem.extId()));
    }

    @Test(description = "Отображение с фильтром по статусам при первичной загрузке", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"260766"})
    public void successFirstFilterViewTest() {
        List<DictionariesItem> dictionaries = metadataStepDef.getListStatusesMetadata();
        List<Status> notChooseStatuses = Arrays.asList(Status.DELETE, Status.ARCHIVE);

        notChooseStatuses.forEach(status -> {
            String statusId = dictionaries.stream().filter(item -> item.name().equals(status.getName())).
                    map(DictionariesItem::id).findFirst().orElseThrow(() -> new RuntimeException("Статус не найден"));
            String id = metadataStepDef.createMetadataImage(new ImageData(ImageFormat.JPEG));
            metadataStepDef.successEditMetadata(id, STATUS_ID.getName(), statusId);
        });

        filtrationMediaStepDef.checkFirstFilterStatus(
                Arrays.asList(Status.NEW, Status.IN_PROGRESS, Status.ACTUAL, Status.READY_FOR_TEST),
                COUNT_METADATA, COUNT_METADATA + notChooseStatuses.size());
    }
}
