package ru.spice.at.common.base_test;

import io.qameta.allure.Epic;
import ru.spice.at.common.emuns.ApiServices;

@Epic("API")
public abstract class BaseApiTest<T> extends BaseTest<T> {
    private static final String SETTINGS_API_PATH = "src/test/resources/settings/api/%s/settings.json";

    protected BaseApiTest(ApiServices service) {
        super(service, SETTINGS_API_PATH);
        System.setProperty("load.service", service.toString());
    }
}
