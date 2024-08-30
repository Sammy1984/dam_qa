package ru.spice.at.ui.filtration_media.test;

import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import ru.spice.at.api.dto.response.metadata.RetailersItem;
import ru.testit.annotations.WorkItemIds;
import org.apache.commons.lang3.RandomStringUtils;
import org.testng.annotations.*;
import ru.spice.at.api.dto.response.metadata.DictionariesItem;
import ru.spice.at.api.dto.response.metadata.UsersItem;
import ru.spice.at.api.metadata_service.MetadataStepDef;
import ru.spice.at.common.base_test.BaseUiTest;
import ru.spice.at.common.dto.dam.ImageData;
import ru.spice.at.common.emuns.dam.ImageFormat;
import ru.spice.at.common.emuns.dam.ImageParameters;
import ru.spice.at.common.emuns.UiCategories;
import ru.spice.at.common.listeners.TestAllureListener;
import ru.spice.at.ui.filtration_media.FiltrationMediaSettings;
import ru.spice.at.ui.filtration_media.FiltrationMediaStepDef;

import java.util.*;

import static ru.spice.at.common.constants.TestConstants.EMPTY_VALUE;
import static ru.spice.at.common.constants.TestConstants.YES;
import static ru.spice.at.common.emuns.Role.ADMINISTRATOR;
import static ru.spice.at.common.emuns.Role.CONTENT_PRODUCTION;
import static ru.spice.at.common.emuns.dam.ImageParameters.*;
import static ru.spice.at.common.emuns.dam.Quality.TO_REVISION;
import static ru.spice.at.common.emuns.dam.Source.BRAND;
import static ru.spice.at.common.emuns.dam.Status.IN_PROGRESS;

@Feature("Filtration media")
@Story("Filtration more menu")
@Listeners({TestAllureListener.class})
public class FiltrationMoreMenuTests extends BaseUiTest<FiltrationMediaSettings> {
    private final static int COUNT_METADATA = 2;
    private List<FiltrationMediaStepDef.Image> images = new ArrayList<>();
    private FiltrationMediaStepDef filtrationMediaStepDef;
    private MetadataStepDef metadataStepDef;
    private List<UsersItem> usersItems;
    private List<DictionariesItem> dictionariesItems;

    protected FiltrationMoreMenuTests() {
        super(UiCategories.FILTRATION_MEDIA);
    }

    @BeforeClass(alwaysRun = true)
    public void beforeClass() {
        filtrationMediaStepDef = new FiltrationMediaStepDef(getWebDriver());
        metadataStepDef = new MetadataStepDef();
        metadataStepDef.deleteMetadata();

        usersItems = metadataStepDef.getListUsersMetadata();
        dictionariesItems = metadataStepDef.getListCategoriesMetadata();
    }

    @BeforeMethod(alwaysRun = true)
    public void beforeMethod() {
        metadataStepDef.setRole(ADMINISTRATOR);
        for (int i = 0; i < COUNT_METADATA; i++) {
            ImageData imageData = new ImageData(ImageFormat.JPEG);
            imageData.setSku(EMPTY_VALUE);
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

    @Test(description = "Фильтрация по полю 'Качество'", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"240594"})
    public void successQualityFiltrationTest() {
        String qualityId = metadataStepDef.getListQualitiesMetadata().
                stream().filter(item -> item.name().equals(TO_REVISION.getName())).
                map(DictionariesItem::id).findFirst().orElse(null);
        metadataStepDef.successEditMetadata(images.get(0).id(), QUALITY_ID.getName(), qualityId);

        filtrationMediaStepDef.chooseMoreMenuFilter(QUALITY_ID, TO_REVISION.getName());
        filtrationMediaStepDef.checkFiltrationFile(images.get(0).filename());
    }

    @Test(description = "Фильтрация по полю 'Статус'", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"240595"})
    public void successStatusFiltrationTest() {
        String statusId = metadataStepDef.getListStatusesMetadata()
                .stream().filter(item -> item.name().equals(IN_PROGRESS.getName()))
                .map(DictionariesItem::id).findFirst().orElse(null);
        metadataStepDef.successEditMetadata(images.get(1).id(), STATUS_ID.getName(), statusId);

        filtrationMediaStepDef.chooseMoreMenuFilter(STATUS_ID, IN_PROGRESS.getName());
        filtrationMediaStepDef.checkFiltrationFile(images.get(1).filename());
    }

    @Test(description = "Фильтрация по полю 'Источник'", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"240596"})
    public void successSourceFiltrationTest() {
        String sourceId = metadataStepDef.getListSourcesMetadata()
                .stream().filter(item -> item.name().equals(BRAND.getName())).
                map(DictionariesItem::id).findFirst().orElse(null);
        metadataStepDef.successEditMetadata(images.get(0).id(), SOURCE_ID.getName(), sourceId);

        filtrationMediaStepDef.chooseMoreMenuFilter(SOURCE_ID, BRAND.getName());
        filtrationMediaStepDef.checkFiltrationFile(images.get(0).filename());
    }

    @Test(description = "Фильтрация по полю 'Исполнитель'", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"240597"})
    public void successAssigneeFiltrationTest() {
        UsersItem usersItem = usersItems.get(0);
        metadataStepDef.successEditMetadata(images.get(0).id(), ASSIGNEE_ID.getName(), usersItem.id());

        filtrationMediaStepDef.chooseMoreMenuFilter(ASSIGNEE_ID, usersItem.fullName());
        filtrationMediaStepDef.checkFiltrationFile(images.get(0).filename());
    }

    @Test(description = "Фильтрация по полю 'Исполнитель' - Не заполнено", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"240597"})
    public void successNoAssigneeFiltrationTest() {
        UsersItem usersItem = usersItems.get(0);
        metadataStepDef.successEditMetadata(images.get(0).id(), ASSIGNEE_ID.getName(), usersItem.id());

        filtrationMediaStepDef.chooseMoreMenuFilter(ASSIGNEE_ID, getData().emptyField());
        filtrationMediaStepDef.checkFiltrationFile(images.get(1).filename());
    }

    @Test(description = "Фильтрация по полю 'Загрузчик'", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"240598"})
    public void successCreatedByFiltrationTest() {
        metadataStepDef.setRole(CONTENT_PRODUCTION);
        ImageData imageData = new ImageData(ImageFormat.JPEG);
        metadataStepDef.createMetadataImage(imageData);

        filtrationMediaStepDef.chooseMoreMenuFilter(CREATED_BY, CONTENT_PRODUCTION.getFullName());
        filtrationMediaStepDef.checkFiltrationFile(imageData.getFilename());
    }

    @Test(description = "Фильтрация по полю 'Категории'", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"225896"})
    public void successCategoryFiltrationTest() {
        DictionariesItem dictionary = dictionariesItems.get(0);
        metadataStepDef.successEditMetadata(images.get(0).id(), MASTER_CATEGORY_ID.getName(), dictionary.id());

        filtrationMediaStepDef.chooseMoreMenuFilter(MASTER_CATEGORY_ID, dictionary.name());
        filtrationMediaStepDef.checkFiltrationFile(images.get(0).filename());
    }

    @Test(description = "Фильтрация по полю 'Категории' - Не заполнено", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"225896"})
    public void successNoCategoryFiltrationTest() {
        DictionariesItem dictionary = dictionariesItems.get(0);
        metadataStepDef.successEditMetadata(images.get(0).id(), MASTER_CATEGORY_ID.getName(), dictionary.id());

        filtrationMediaStepDef.chooseMoreMenuFilter(MASTER_CATEGORY_ID, getData().emptyField());
        filtrationMediaStepDef.checkFiltrationFile(images.get(1).filename());
    }

    @Test(description = "Фильтрация по параметру 'СТМ'", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"225904"})
    public void successIsOwnTrademarkFiltrationTest() {
        metadataStepDef.successEditMetadata(images.get(1).id(), IS_OWN_TRADEMARK.getName(), true);

        filtrationMediaStepDef.chooseMoreMenuFilter(IS_OWN_TRADEMARK, YES);
        filtrationMediaStepDef.checkFiltrationFile(images.get(1).filename());
    }

    @Test(description = "Фильтрация по параметру 'СТМ' - Не заполнено", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"225904"})
    public void successNoIsOwnTrademarkFiltrationTest() {
        metadataStepDef.successEditMetadata(images.get(1).id(), IS_OWN_TRADEMARK.getName(), true);

        filtrationMediaStepDef.chooseMoreMenuFilter(IS_OWN_TRADEMARK, getData().emptyField());
        filtrationMediaStepDef.checkFiltrationFile(images.get(0).filename());
    }

    @Test(description = "Фильтрация по параметру 'Авторские права'", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"225899"})
    public void successCopyrightFiltrationTest() {
        metadataStepDef.successEditMetadata(images.get(0).id(), IS_COPYRIGHT.getName(), true);

        filtrationMediaStepDef.chooseMoreMenuFilter(IS_COPYRIGHT, YES);
        filtrationMediaStepDef.checkFiltrationFile(images.get(0).filename());
    }

    @Test(description = "Фильтрация по параметру 'Авторские права' - Не заполнено", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"225899"})
    public void successNoCopyrightFiltrationTest() {
        metadataStepDef.successEditMetadata(images.get(0).id(), IS_COPYRIGHT.getName(), true);

        filtrationMediaStepDef.chooseMoreMenuFilter(IS_COPYRIGHT, getData().emptyField());
        filtrationMediaStepDef.checkFiltrationFile(images.get(1).filename());
    }

    @Test(description = "Фильтрация по параметру 'Исходное изображение'", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"225901"})
    public void successRawImageFiltrationTest() {
        metadataStepDef.successEditMetadata(images.get(1).id(), IS_RAW_IMAGE.getName(), true);

        filtrationMediaStepDef.chooseMoreMenuFilter(IS_RAW_IMAGE, YES);
        filtrationMediaStepDef.checkFiltrationFile(images.get(1).filename());
    }

    @Test(description = "Фильтрация по параметру 'Исходное изображение' - Не заполнено", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"225901"})
    public void successNoRawImageFiltrationTest() {
        metadataStepDef.successEditMetadata(images.get(1).id(), IS_RAW_IMAGE.getName(), true);
        metadataStepDef.successEditMetadata(images.get(0).id(), IS_RAW_IMAGE.getName(), null);

        filtrationMediaStepDef.chooseMoreMenuFilter(IS_RAW_IMAGE, getData().emptyField());
        filtrationMediaStepDef.checkFiltrationFile(images.get(0).filename());
    }

    @Test(description = "Фильтрация по параметру 'Ретейлер'", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"286285"})
    public void successRetainerFiltrationTest() {
        RetailersItem retailersItem = metadataStepDef.getListRetailersMetadata().data().get(0);
        metadataStepDef.successEditMetadata(
                images.get(0).id(), MASTER_SELLER_ID.getName(), retailersItem.id());

        filtrationMediaStepDef.chooseMoreMenuFilter(MASTER_SELLER_ID, retailersItem.name());
        filtrationMediaStepDef.checkFiltrationFile(images.get(0).filename());
    }

    @Test(description = "Фильтрация по полю 'SKU'", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"225898"})
    public void successSkuFiltrationTest() {
        String sku = RandomStringUtils.randomAlphabetic(5);
        metadataStepDef.successEditMetadata(images.get(0).id(), SKU.getName(), sku);

        filtrationMediaStepDef.chooseMoreMenuFilter(SKU, sku);
        filtrationMediaStepDef.checkFiltrationFile(images.get(0).filename());
    }

    @Test(description = "Фильтрация по полю 'SKU' - пустое значение", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"225898"})
    public void successNoSkuFiltrationTest() {
        String sku = RandomStringUtils.randomAlphabetic(5);
        metadataStepDef.successEditMetadata(images.get(0).id(), SKU.getName(), sku);

        filtrationMediaStepDef.chooseMoreMenuFilter(SKU, EMPTY_VALUE);
        filtrationMediaStepDef.checkFiltrationFile(images.get(1).filename());
    }

    @Test(description = "Фильтрация по полю 'Приоритет'", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"225897"})
    public void successPriorityFiltrationTest() {
        int priority = new Random().nextInt(99) + 1;
        metadataStepDef.successEditMetadata(images.get(0).id(), PRIORITY.getName(), priority);

        filtrationMediaStepDef.chooseMoreMenuFilter(PRIORITY, String.valueOf(priority));
        filtrationMediaStepDef.checkFiltrationFile(images.get(0).filename());
    }

    @Test(description = "Фильтрация по полю 'Приоритет' - пустое значение", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"225897"})
    public void successNoPriorityFiltrationTest() {
        int priority = new Random().nextInt(99) + 1;
        metadataStepDef.successEditMetadata(images.get(0).id(), PRIORITY.getName(), priority);

        filtrationMediaStepDef.chooseMoreMenuFilter(PRIORITY, EMPTY_VALUE);
        filtrationMediaStepDef.checkFiltrationFile(images.get(1).filename());
    }

    @Test(description = "Фильтрация по полю 'Задача PIMS'", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"240599"})
    public void successPIMSTaskFiltrationTest() {
        int taskId = new Random().nextInt(1000) + 1;
        metadataStepDef.successEditMetadata(images.get(0).id(), EXTERNAL_TASK_ID.getName(), taskId);

        filtrationMediaStepDef.chooseMoreMenuFilter(EXTERNAL_TASK_ID, String.valueOf(taskId));
        filtrationMediaStepDef.checkFiltrationFile(images.get(0).filename());
    }

    @Test(description = "Фильтрация по полю 'Задача PIMS' - пустое значение", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"240599"})
    public void successNoPIMSTaskFiltrationTest() {
        int taskId = new Random().nextInt(1000) + 1;
        metadataStepDef.successEditMetadata(images.get(0).id(), EXTERNAL_TASK_ID.getName(), taskId);

        filtrationMediaStepDef.chooseMoreMenuFilter(EXTERNAL_TASK_ID, EMPTY_VALUE);
        filtrationMediaStepDef.checkFiltrationFile(images.get(1).filename());
    }

    @Test(description = "Фильтрация по нескольким параметрам", timeOut = 60000, groups = {"regress"})
    @WorkItemIds({"225902"})
    public void successSomeFiltrationTest() {
        String qualityId = metadataStepDef.getListQualitiesMetadata().
                stream().filter(item -> item.name().equals(TO_REVISION.getName())).
                map(DictionariesItem::id).findFirst().orElse(null);
        metadataStepDef.successEditMetadata(images.get(0).id(), QUALITY_ID.getName(), qualityId);

        String sku = RandomStringUtils.randomAlphabetic(5);
        metadataStepDef.successEditMetadata(images.get(0).id(), SKU.getName(), sku);

        DictionariesItem dictionary = dictionariesItems.get(0);
        metadataStepDef.successEditMetadata(images.get(0).id(), MASTER_CATEGORY_ID.getName(), dictionary.id());

        Map<ImageParameters, List<String>> filter = new TreeMap<>() {{
            put(QUALITY_ID, Collections.singletonList(TO_REVISION.getName()));
            put(SKU, Collections.singletonList(sku));
            put(MASTER_CATEGORY_ID, Collections.singletonList(dictionary.name()));
        }};

        filtrationMediaStepDef.chooseMoreMenuFilter(filter);
        filtrationMediaStepDef.checkFiltrationFile(images.get(0).filename());
    }
}
