package ru.spice.at.api.metadata_service.test;

import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import org.apache.commons.lang3.RandomStringUtils;
import org.testng.annotations.Test;
import ru.spice.at.api.dto.response.metadata.*;
import ru.spice.at.api.metadata_service.MetadataSettings;
import ru.spice.at.api.metadata_service.MetadataStepDef;
import ru.spice.at.common.base_test.BaseApiTest;
import ru.spice.at.common.emuns.ApiServices;

import ru.spice.at.common.utils.Assert;
import ru.testit.annotations.WorkItemIds;


import java.util.*;
import java.util.stream.Collectors;

import static ru.spice.at.common.utils.Assert.*;
import static ru.spice.at.common.utils.Assert.compareParameters;

@Feature("Metadata Service")
@Story("GET dictionaries")
public class MetadataGetDictionariesTests extends BaseApiTest<MetadataSettings> {
    private final MetadataStepDef metadataStepDef;

    protected MetadataGetDictionariesTests() {
        super(ApiServices.METADATA_SERVICE);
        metadataStepDef = new MetadataStepDef();
    }

    @Test(description = "Получение списка пользователей", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"191812"})
    public void successGetListUsersTest() {
        List<UsersItem> dictionaries = metadataStepDef.getListUsersMetadata();
        compareParameters(dictionaries.size(), new HashSet<>(dictionaries).size(), "список пользователей");
    }

    @Test(description = "Получение списка пользователей - поиск по полному совпадению названия", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"279184"})
    public void successGetListUsersFullSearchTest() {
        String searchValue = getData().createdName();
        List<UsersItem> dictionaries = metadataStepDef.getListUsersMetadata(searchValue);
        List<String> dictionariesNames = dictionaries.stream().map(UsersItem::fullName).collect(Collectors.toList());

        assertAll(
                () -> compareParameters(1, dictionariesNames.size(), "size"),
                () -> compareParameters(searchValue, dictionariesNames.get(0), "name")
        );
    }

    @Test(description = "Получение списка пользователей - поиск по частичному совпадению названия", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"279184"})
    public void successGetListUsersSearchTest() {
        String searchValue = getData().createdName().split(" ")[0];
        List<UsersItem> dictionaries = metadataStepDef.getListUsersMetadata(searchValue);
        List<String> dictionariesNames = dictionaries.stream().map(UsersItem::fullName).collect(Collectors.toList());

        assertAll(
                () -> equalsTrueParameter(dictionariesNames.size() >= 1, "size"),
                () -> dictionariesNames.forEach(name -> Assert.contains(searchValue, name, "name"))
        );
    }

    @Test(description = "Получение списка пользователей - поиск по несуществующему значению", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"279184"})
    public void successGetListUsersEmptySearchTest() {
        List<UsersItem> dictionaries = metadataStepDef.getListUsersMetadata(RandomStringUtils.randomAlphanumeric(19));
        mustBeEmptyList(dictionaries, "size");
    }

    @Test(description = "Получение списка пользователей с max_page_size", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"279190"})
    public void successGetListUsersWithMaxPageSizeTest() {
        Integer maxPageSize = 21;
        List<UsersItem> dictionaries = metadataStepDef.getListUsersMetadata(maxPageSize, null);
        compareParameters(maxPageSize, dictionaries.size(), "size");
    }

    @Test(description = "Получение списка пользователей с page_token", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"279191"})
    public void successGetListUsersWithPageTokenTest() {
        UsersResponse usersResponse = metadataStepDef.getListUsersMetadataResponse(Collections.emptyMap());
        String nextPageToken = usersResponse.nextPageToken();
        List<UsersItem> nextDictionaries = metadataStepDef.getListUsersMetadata(null, nextPageToken);

        assertAll(
                () -> compareParameters(nextPageToken, nextDictionaries.get(0).id(), "user id"),
                () -> compareParameters(10, nextDictionaries.size(), "size")
        );
    }

    @Test(description = "Получение списка пользователей с page_token - несуществующий", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"279188"})
    public void successGetListUsersWithNotExistPageTokenTest() {
        List<UsersItem> usersItems = metadataStepDef.getListUsersMetadata(null, UUID.randomUUID().toString());
        mustBeEmptyList(usersItems, "size");
    }

    @Test(description = "Неуспешное получение списка пользователей с page_token и max_page_size - невалидные", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"279189"})
    public void unsuccessfulGetListUsersWithInvalidPageTokenAndMaxPageSizeTest() {
        List<InvalidParamsItem> invalidParamsItems = metadataStepDef.getUnsuccessfulListUsersMetadata(RandomStringUtils.randomAlphanumeric(5), RandomStringUtils.randomNumeric(5));
        compareParameters(new LinkedList<>(getData().invalidRetailersDictionaryParams()), new LinkedList<>(invalidParamsItems), "Invalid Retailers Dictionary Params");
    }

    @Test(description = "Получение списка статусов", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"191813"})
    public void successGetListStatusTest() {
        List<DictionariesItem> dictionaries = metadataStepDef.getListStatusesMetadata();
        metadataStepDef.checkDictionaries(dictionaries);
    }

    @Test(description = "Получение списка качества", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"191814"})
    public void successGetListQualitiesTest() {
        List<DictionariesItem> dictionaries = metadataStepDef.getListQualitiesMetadata();
        metadataStepDef.checkDictionaries(dictionaries);
    }

    @Test(description = "Получение списка источников", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"191815"})
    public void successGetListSourcesTest() {
        List<DictionariesItem> dictionaries = metadataStepDef.getListSourcesMetadata();
        metadataStepDef.checkDictionaries(dictionaries);
    }

    @Test(description = "Получение списка категорий", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"226161"})
    public void successGetListCategoriesTest() {
        List<DictionariesItem> dictionaries = metadataStepDef.getListCategoriesMetadata();
        metadataStepDef.checkDictionaries(dictionaries);
    }

    @Test(description = "Получение списка категорий - поиск по полному совпадению названия", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"279185"})
    public void successGetListCategoriesFullSearchTest() {
        String searchValue = getData().categoryName();
        List<DictionariesItem> dictionaries = metadataStepDef.getListCategoriesMetadata(searchValue);
        List<String> dictionariesNames = dictionaries.stream().map(DictionariesItem::name).collect(Collectors.toList());

        assertAll(
                () -> compareParameters(1, dictionariesNames.size(), "size"),
                () -> compareParameters(searchValue, dictionariesNames.get(0), "name")
        );
    }

    @Test(description = "Получение списка категорий - поиск по частичному совпадению названия", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"279185"})
    public void successGetListCategoriesSearchTest() {
        String searchValue = getData().categoryName().split(" ")[0];
        List<DictionariesItem> dictionaries = metadataStepDef.getListCategoriesMetadata(searchValue);
        List<String> dictionariesNames = dictionaries.stream().map(DictionariesItem::name).collect(Collectors.toList());

        assertAll(
                () -> equalsTrueParameter(dictionariesNames.size() >= 1, "size"),
                () -> dictionariesNames.forEach(name -> Assert.contains(searchValue, name, "name"))
        );
    }

    @Test(description = "Получение списка категорий - поиск по несуществующему значению", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"279185"})
    public void successGetListCategoriesEmptySearchTest() {
        List<DictionariesItem> dictionaries = metadataStepDef.getListCategoriesMetadata(RandomStringUtils.randomAlphabetic(15));
        mustBeEmptyList(dictionaries, "size");
    }

    @Test(description = "Получение списка ритейлеров", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"246916"})
    public void successGetListRetailersTest() {
        RetailersResponse retailers = metadataStepDef.getListRetailersMetadata();
        List<String> retailersNames =  retailers.data().stream().map(RetailersItem::name).collect(Collectors.toList());

        compareParameters(10, retailersNames.size(), "size");
        char minChar = Character.MIN_VALUE;
        for (String name : retailersNames) {
            name = name.replaceAll("\\+", "").toLowerCase();
            equalsTrueParameter(name.charAt(0) >= minChar,
                    String.format("символы в порядке возрастания, от %s до первого символа %s", minChar, name));
            minChar = name.charAt(0);
        }
    }

    @Test(description = "Получение списка ритейлеров - поиск по полному совпадению названия", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"279186"})
    public void successGetListRetailersFullSearchTest() {
        String searchValue = getData().retailerName();
        RetailersResponse retailers = metadataStepDef.getListRetailersMetadata(searchValue);
        List<String> retailersNames =  retailers.data().stream().map(RetailersItem::name).collect(Collectors.toList());

        assertAll(
                () -> compareParameters(1, retailersNames.size(), "size"),
                () -> compareParameters(searchValue, retailersNames.get(0), "name")
        );
    }

    @Test(description = "Получение списка ритейлеров - поиск по частичному совпадению названия", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"279186"})
    public void successGetListRetailersSearchTest() {
        String searchValue = getData().retailerName().split(" ")[1];
        RetailersResponse retailers = metadataStepDef.getListRetailersMetadata(searchValue);
        List<String> retailersNames =  retailers.data().stream().map(RetailersItem::name).collect(Collectors.toList());

        assertAll(
                () -> equalsTrueParameter(retailersNames.size() >= 1, "size"),
                () -> retailersNames.forEach(name -> Assert.contains(searchValue, name, "name"))
        );
    }

    @Test(description = "Получение списка ритейлеров - поиск по external id", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"279187"})
    public void successGetListRetailersExternalIdSearchTest() {
        String searchValue = getData().retailersExtId();
        RetailersResponse retailers = metadataStepDef.getListRetailersMetadata(searchValue);
        List<Integer> retailersExtIds =  retailers.data().stream().map(RetailersItem::extId).collect(Collectors.toList());

        assertAll(
                () -> compareParameters(1, retailersExtIds.size(), "size"),
                () -> compareParameters(searchValue, retailersExtIds.get(0).toString(), "name")
        );
    }

    @Test(description = "Получение списка ритейлеров - поиск по несуществующему значению", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"279186"})
    public void successGetListRetailersEmptySearchTest() {
        RetailersResponse retailers = metadataStepDef.getListRetailersMetadata(RandomStringUtils.randomAlphabetic(18));
        List<Integer> retailersExtIds =  retailers.data().stream().map(RetailersItem::extId).collect(Collectors.toList());
        mustBeEmptyList(retailersExtIds, "size");
    }

    @Test(description = "Получение списка ритейлеров с max_page_size", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"246917"})
    public void successGetListRetailersWithMaxPageSizeTest() {
        Integer maxPageSize = 200;
        RetailersResponse retailers = metadataStepDef.getListRetailersMetadata(maxPageSize, null);
        List<String> retailersNames =  retailers.data().stream().map(RetailersItem::name).collect(Collectors.toList());

        compareParameters(maxPageSize, retailersNames.size(), "size");
        char minChar = Character.MIN_VALUE;
        for (String name : retailersNames) {
            name = name.replaceAll("\\+*«*»*", "").toLowerCase();
            equalsTrueParameter(name.charAt(0) >= minChar,
                    String.format("символы в порядке возрастания, от '%s' до первого символа '%s'", minChar, name));
            minChar = name.charAt(0);
        }
    }

    @Test(description = "Получение списка ритейлеров с page_token", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"246919"})
    public void successGetListRetailersWithPageTokenTest() {
        RetailersResponse retailers = metadataStepDef.getListRetailersMetadata();
        String nextPageToken = retailers.nextPageToken();
        RetailersResponse nextRetailers = metadataStepDef.getListRetailersMetadata(null, nextPageToken);

        assertAll(
                () -> compareParameters(nextPageToken, nextRetailers.data().get(0).id(), "retailer id"),
                () -> compareParameters(10, nextRetailers.data().size(), "size")
        );

        List<String> retailersNames =  nextRetailers.data().stream().map(RetailersItem::name).collect(Collectors.toList());
        char minChar = Character.MIN_VALUE;
        for (String name : retailersNames) {
            name = name.replaceAll("\\+*«*»*", "").toLowerCase();
            equalsTrueParameter(name.charAt(0) >= minChar,
                    String.format("символы в порядке возрастания, от %s до первого символа %s", minChar, name));
            minChar = name.charAt(0);
        }
    }

    @Test(description = "Получение списка ритейлеров с page_token - несуществующий", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"246920"})
    public void successGetListRetailersWithNotExistPageTokenTest() {
        RetailersResponse retailers = metadataStepDef.getListRetailersMetadata(null, UUID.randomUUID().toString());
        assertAll(
                () -> mustBeEmptyList(retailers.data(), "size"),
                () -> mustBeNullParameter(retailers.nextPageToken(), "nextPageToken")
        );
    }

    @Test(description = "Неуспешное получение списка ритейлеров с page_token и max_page_size - невалидные", timeOut = 600000, groups = {"regress"})
    @WorkItemIds({"246921"})
    public void unsuccessfulGetListRetailersWithInvalidPageTokenAndMaxPageSizeTest() {
        List<InvalidParamsItem> invalidParamsItems = metadataStepDef.getUnsuccessfulListRetailersMetadata(RandomStringUtils.randomAlphanumeric(5), RandomStringUtils.randomNumeric(5));
        compareParameters(new LinkedList<>(getData().invalidRetailersDictionaryParams()), new LinkedList<>(invalidParamsItems), "Invalid Retailers Dictionary Params");
    }
}
