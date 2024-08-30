package ru.spice.at.ui.filtration_media.test;

import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import ru.testit.annotations.WorkItemIds;
import org.apache.commons.lang3.RandomStringUtils;
import org.testng.annotations.*;
import ru.spice.at.api.dto.request.metadata.RequestMultiEditData;
import ru.spice.at.api.dto.response.metadata.DictionariesItem;
import ru.spice.at.api.dto.response.metadata.UsersItem;
import ru.spice.at.api.metadata_service.MetadataStepDef;
import ru.spice.at.common.base_test.BaseUiTest;
import ru.spice.at.common.dto.dam.ImageData;
import ru.spice.at.common.emuns.UiCategories;
import ru.spice.at.common.emuns.dam.ImageFormat;
import ru.spice.at.common.emuns.dam.ImageParameters;
import ru.spice.at.common.listeners.TestAllureListener;
import ru.spice.at.ui.filtration_media.FiltrationMediaSettings;
import ru.spice.at.ui.filtration_media.FiltrationMediaStepDef;

import java.util.*;

import static ru.spice.at.common.emuns.Role.*;
import static ru.spice.at.common.emuns.dam.ImageParameters.*;
import static ru.spice.at.common.emuns.dam.Quality.BAD;
import static ru.spice.at.common.emuns.dam.Quality.TO_REVISION;
import static ru.spice.at.common.emuns.dam.Source.BRAND;
import static ru.spice.at.common.emuns.dam.Source.RESTAURANT;
import static ru.spice.at.common.emuns.dam.Status.ARCHIVE;
import static ru.spice.at.common.emuns.dam.Status.IN_PROGRESS;

@Feature("Filtration media")
@Story("Filtration many choose")
@Listeners({TestAllureListener.class})
public class FiltrationManyChooseTests extends BaseUiTest<FiltrationMediaSettings> {
    private final static int COUNT_METADATA = 3;
    private List<FiltrationMediaStepDef.Image> images = new ArrayList<>();
    private FiltrationMediaStepDef filtrationMediaStepDef;
    private MetadataStepDef metadataStepDef;

    protected FiltrationManyChooseTests() {
        super(UiCategories.FILTRATION_MEDIA);
    }

    @BeforeClass(alwaysRun = true)
    public void beforeClass() {
        filtrationMediaStepDef = new FiltrationMediaStepDef(getWebDriver());
        metadataStepDef = new MetadataStepDef();
        metadataStepDef.deleteMetadata();
    }

    @BeforeMethod(alwaysRun = true)
    public void beforeMethod() {
        metadataStepDef.setRole(ADMINISTRATOR);
        for (int i = 0; i < COUNT_METADATA; i++) {
            ImageData imageData = new ImageData(ImageFormat.JPEG);
            String id = metadataStepDef.createMetadataImage(imageData);
            images.add(new FiltrationMediaStepDef.Image(imageData.getFilename(), id));
        }

        filtrationMediaStepDef.goToBaseUrlWithLogin(getWebDriver());
    }

    @AfterMethod(alwaysRun = true)
    public void afterMethod() {
        metadataStepDef.deleteMetadata();
        images = new ArrayList<>();
    }

    @Test(description = "Фильтрация по полю 'Качество' - несколько значений", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"230133"})
    public void successManyQualityFiltrationTest() {
        List<DictionariesItem> qualities = metadataStepDef.getListQualitiesMetadata();

        String toRevisionId = qualities.stream().filter(item -> item.name().equals(TO_REVISION.getName())).
                map(DictionariesItem::id).findFirst().orElse(null);
        String badId = qualities.stream().filter(item -> item.name().equals(BAD.getName())).
                map(DictionariesItem::id).findFirst().orElse(null);

        metadataStepDef.successEditMetadata(images.get(0).id(), QUALITY_ID.getName(), toRevisionId);
        metadataStepDef.successEditMetadata(images.get(1).id(), QUALITY_ID.getName(), badId);

        filtrationMediaStepDef.chooseMoreMenuFilter(QUALITY_ID, Arrays.asList(TO_REVISION.getName(), BAD.getName()));
        filtrationMediaStepDef.checkFiltrationFile(Arrays.asList(images.get(0).filename(), images.get(1).filename()));
    }

    @Test(description = "Фильтрация по полю 'Статус' - несколько значений", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"230129"})
    public void successManyStatusFiltrationTest() {
        List<DictionariesItem> statuses = metadataStepDef.getListStatusesMetadata();

        String inProgressId = statuses.stream().filter(item -> item.name().equals(IN_PROGRESS.getName())).
                map(DictionariesItem::id).findFirst().orElse(null);
        String archiveId = statuses.stream().filter(item -> item.name().equals(ARCHIVE.getName())).
                map(DictionariesItem::id).findFirst().orElse(null);

        metadataStepDef.successEditMetadata(images.get(0).id(), STATUS_ID.getName(), inProgressId);
        metadataStepDef.successEditMetadata(images.get(1).id(), STATUS_ID.getName(), archiveId);

        filtrationMediaStepDef.chooseMoreMenuFilter(STATUS_ID, Arrays.asList(IN_PROGRESS.getName(), ARCHIVE.getName()));
        filtrationMediaStepDef.checkFiltrationFile(Arrays.asList(images.get(0).filename(), images.get(1).filename()));
    }

    @Test(description = "Фильтрация по полю 'Источник' - несколько значений", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"230123"})
    public void successManySourceFiltrationTest() {
        List<DictionariesItem> sources = metadataStepDef.getListSourcesMetadata();

        String brandId = sources.stream().filter(item -> item.name().equals(BRAND.getName())).
                map(DictionariesItem::id).findFirst().orElse(null);
        String restaurantId = sources.stream().filter(item -> item.name().equals(RESTAURANT.getName())).
                map(DictionariesItem::id).findFirst().orElse(null);

        metadataStepDef.successEditMetadata(images.get(0).id(), SOURCE_ID.getName(), brandId);
        metadataStepDef.successEditMetadata(images.get(1).id(), SOURCE_ID.getName(), restaurantId);

        filtrationMediaStepDef.chooseMoreMenuFilter(SOURCE_ID, Arrays.asList(BRAND.getName(), RESTAURANT.getName()));
        filtrationMediaStepDef.checkFiltrationFile(Arrays.asList(images.get(0).filename(), images.get(1).filename()));
    }

    @Test(description = "Фильтрация по полю 'Исполнитель' - несколько значений", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"230130"})
    public void successManyAssigneeFiltrationTest() {
        List<UsersItem> usersItems = metadataStepDef.getListUsersMetadata();

        UsersItem firstUser = usersItems.get(0);
        UsersItem secondUser = usersItems.get(1);

        metadataStepDef.successEditMetadata(images.get(0).id(), ASSIGNEE_ID.getName(), firstUser.id());
        metadataStepDef.successEditMetadata(images.get(1).id(), ASSIGNEE_ID.getName(), secondUser.id());

        filtrationMediaStepDef.chooseMoreMenuFilter(ASSIGNEE_ID, Arrays.asList(firstUser.fullName(), secondUser.fullName()));
        filtrationMediaStepDef.checkFiltrationFile(Arrays.asList(images.get(0).filename(), images.get(1).filename()));
    }

    @Test(description = "Фильтрация по полю 'Загрузчик' - несколько значений", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"230131"})
    public void successManyCreatedByFiltrationTest() {
        metadataStepDef.setRole(CONTENT_PRODUCTION);
        ImageData firstImageData = new ImageData(ImageFormat.JPEG);
        metadataStepDef.createMetadataImage(firstImageData);

        metadataStepDef.setRole(PHOTOPRODUCTION);
        ImageData secondImageData = new ImageData(ImageFormat.JPEG);
        metadataStepDef.createMetadataImage(secondImageData);

        filtrationMediaStepDef.chooseMoreMenuFilter(CREATED_BY, Arrays.asList(PHOTOPRODUCTION.getFullName(), CONTENT_PRODUCTION.getFullName()));
        filtrationMediaStepDef.checkFiltrationFile(Arrays.asList(firstImageData.getFilename(), secondImageData.getFilename()));
    }

    @Test(description = "Фильтрация по полю 'Категории' - несколько значений", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"230132"})
    public void successManyCategoryByFiltrationTest() {
        List<DictionariesItem> dictionaries = metadataStepDef.getListCategoriesMetadata();

        metadataStepDef.successEditMetadata(images.get(0).id(), MASTER_CATEGORY_ID.getName(), dictionaries.get(0).id());
        metadataStepDef.successEditMetadata(images.get(1).id(), MASTER_CATEGORY_ID.getName(), dictionaries.get(1).id());

        filtrationMediaStepDef.chooseMoreMenuFilter(MASTER_CATEGORY_ID, Arrays.asList(dictionaries.get(0).name(), dictionaries.get(1).name()));
        filtrationMediaStepDef.checkFiltrationFile(Arrays.asList(images.get(0).filename(), images.get(1).filename()));
    }

    @Test(description = "Фильтрация по полю 'SKU' - несколько значений", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"231369"})
    public void successManySkuByFiltrationTest() {
        String firstSku = RandomStringUtils.randomAlphabetic(5);
        String secondSku = RandomStringUtils.randomAlphabetic(10);

        metadataStepDef.successEditMetadata(images.get(0).id(), SKU.getName(), firstSku);
        metadataStepDef.successEditMetadata(images.get(1).id(), SKU.getName(), secondSku);

        String skus = filtrationMediaStepDef.concatValues(Arrays.asList(firstSku, secondSku));

        filtrationMediaStepDef.chooseMoreMenuFilter(SKU, skus);
        filtrationMediaStepDef.checkFiltrationFile(Arrays.asList(images.get(0).filename(), images.get(1).filename()));
    }

    @Test(description = "Фильтрация по полю 'Priority' - несколько значений", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"231370"})
    public void successManyPriorityByFiltrationTest() {
        String firstPriority = String.valueOf(new Random().nextInt(99) + 1);
        String secondPriority = String.valueOf(new Random().nextInt(99) + 1);

        metadataStepDef.successEditMetadata(images.get(0).id(), PRIORITY.getName(), firstPriority);
        metadataStepDef.successEditMetadata(images.get(1).id(), PRIORITY.getName(), secondPriority);

        String priorities = filtrationMediaStepDef.concatValues(Arrays.asList(firstPriority, secondPriority));

        filtrationMediaStepDef.chooseMoreMenuFilter(PRIORITY, priorities);
        filtrationMediaStepDef.checkFiltrationFile(Arrays.asList(images.get(0).filename(), images.get(1).filename()));
    }

    @Test(description = "Фильтрация по нескольким параметрам с множественным выбором", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"230134"})
    public void successManyParametersSomeFiltrationTest() {
        List<DictionariesItem> statuses = metadataStepDef.getListStatusesMetadata();

        String inProgressId = statuses.stream().filter(item -> item.name().equals(IN_PROGRESS.getName())).
                map(DictionariesItem::id).findFirst().orElse(null);
        String archiveId = statuses.stream().filter(item -> item.name().equals(ARCHIVE.getName())).
                map(DictionariesItem::id).findFirst().orElse(null);

        metadataStepDef.successEditMetadata(images.get(0).id(), STATUS_ID.getName(), inProgressId);
        metadataStepDef.successEditMetadata(images.get(1).id(), STATUS_ID.getName(), archiveId);

        List<DictionariesItem> dictionaries = metadataStepDef.getListCategoriesMetadata();

        metadataStepDef.successEditMetadata(images.get(0).id(), MASTER_CATEGORY_ID.getName(), dictionaries.get(0).id());
        metadataStepDef.successEditMetadata(images.get(1).id(), MASTER_CATEGORY_ID.getName(), dictionaries.get(1).id());

        String firstPriority = String.valueOf(new Random().nextInt(99) + 1);
        String secondPriority = String.valueOf(new Random().nextInt(99) + 1);

        metadataStepDef.successEditMetadata(images.get(0).id(), PRIORITY.getName(), firstPriority);
        metadataStepDef.successEditMetadata(images.get(1).id(), PRIORITY.getName(), secondPriority);

        String priorities = filtrationMediaStepDef.concatValues(Arrays.asList(firstPriority, secondPriority));

        Map<ImageParameters, List<String>> filter = new TreeMap<ImageParameters, List<String>>() {{
            put(STATUS_ID, Arrays.asList(IN_PROGRESS.getName(), ARCHIVE.getName()));
            put(MASTER_CATEGORY_ID, Arrays.asList(dictionaries.get(0).name(), dictionaries.get(1).name()));
            put(PRIORITY, Collections.singletonList(priorities));
        }};
        filtrationMediaStepDef.chooseMoreMenuFilter(filter);
        filtrationMediaStepDef.checkFiltrationFile(Arrays.asList(images.get(0).filename(), images.get(1).filename()));
    }

    @Test(description = "Фильтрация по нескольким параметрам с множественным и единичным выбором", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"230135"})
    public void successManyParametersSomeAndSingleFiltrationTest() {
        String inProgressId =  metadataStepDef.getListStatusesMetadata().stream().filter(item -> item.name().equals(IN_PROGRESS.getName())).
                map(DictionariesItem::id).findFirst().orElse(null);
        List<DictionariesItem> dictionaries = metadataStepDef.getListCategoriesMetadata();

        RequestMultiEditData requestMultiEditData = RequestMultiEditData.builder()
                .ids(Arrays.asList(images.get(0).id(), images.get(1).id())).statusId(inProgressId).masterCategoryId(dictionaries.get(0).id()).build();

        metadataStepDef.successMultiEditMetadata(requestMultiEditData);

        String firstPriority = String.valueOf(new Random().nextInt(99) + 1);
        String secondPriority = String.valueOf(new Random().nextInt(99) + 1);

        metadataStepDef.successEditMetadata(images.get(0).id(), PRIORITY.getName(), firstPriority);
        metadataStepDef.successEditMetadata(images.get(1).id(), PRIORITY.getName(), secondPriority);

        String priorities = filtrationMediaStepDef.concatValues(Arrays.asList(firstPriority, secondPriority));

        Map<ImageParameters, List<String>> filter = new TreeMap<ImageParameters, List<String>>() {{
            put(STATUS_ID, Collections.singletonList(IN_PROGRESS.getName()));
            put(MASTER_CATEGORY_ID, Collections.singletonList(dictionaries.get(0).name()));
            put(PRIORITY, Collections.singletonList(priorities));
        }};
        filtrationMediaStepDef.chooseMoreMenuFilter(filter);
        filtrationMediaStepDef.checkFiltrationFile(Arrays.asList(images.get(0).filename(), images.get(1).filename()));
    }
}