package ru.spice.at.common.base_test;

import io.qameta.allure.Step;
import io.restassured.http.Cookies;
import lombok.extern.log4j.Log4j2;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.WebDriver;
import ru.spice.at.api.utils.UrlUtils;
import ru.spice.at.common.utils.auth.AuthUtils;
import ru.spice.at.ui.utils.UiUtils;

import java.util.List;

import static ru.spice.at.ui.utils.UiUtils.getStandProperties;

/**
 * Базовый класс для UI шагов тестовых классов
 * @author Aleksandr Osokin
 */
@Log4j2
public abstract class AbstractUiStepDef extends AbstractStepDef {
    protected String baseFrontUrl;
    private List<Cookie> cookies;

    protected AbstractUiStepDef(String subsystem) {
        baseFrontUrl = subsystem == null ?
                getStandProperties().getFrontendUri() :
                getStandProperties().getFrontendUri() + "/" + subsystem;
    }

    protected AbstractUiStepDef() {
        this(null);
    }

    @Override
    public String getAuthToken() {
        if (super.getAuthToken() == null) {
            login();
        }
        return super.getAuthToken();
    }

    /**
     * Достаем куки из браузера в виде куки rest assured
     *
     * @param webDriver веб-драйвер
     * @return куки
     */
    public Cookies getBrowserCookies(WebDriver webDriver) {
        return UiUtils.seleniumCookiesToRestAssured(webDriver.manage().getCookies());
    }

    /**
     * Загружаем куки в браузер
     *
     * @param webDriver веб-драйвер
     * @param url ссылка для перехода/перезагрузки
     */
    @Step("Проброс кук в WebDriver")
    public void updateWebDriverCookies(WebDriver webDriver, String url) {
        log.info("Проброс кук в WebDriver, переход на url {}", url);
        webDriver.get(url);
        webDriver.manage().deleteAllCookies();
        cookies = UiUtils.restAssuredCookiesToSelenium(getCookies(), UrlUtils.getHostFromUrl(standProperties.getAuthUtils(getRole()).getUrl()));
        cookies.forEach(cookie -> webDriver.manage().addCookie(cookie));
        webDriver.get(url);
    }

    @Step("Авторизация в системе")
    public boolean login() {
        log.info("Авторизация в системе");
        AuthUtils authUtils = standProperties.getAuthUtils(getRole());
        if (authUtils == null) {
            log.warn("Данные авторизации не указаны, авторизация не удалась!");
            return false;
        }
        setAuthToken((String) authUtils.login());
        setCookies(authUtils.getLoginCookies());
        return true;
    }

    @Step("Открываем базовую страницу с авторизованным пользователем")
    public void goToBaseUrlWithLogin(WebDriver webDriver) {
        if (baseFrontUrl == null) {
            log.error("Не определен url для базовой страницы");
            throw new IllegalArgumentException("Не определен url для базовой страницы");
        }
        if (cookies != null) {
            webDriver.get(baseFrontUrl);
        }
        else if (getAuthToken() != null || login()) {
            updateWebDriverCookies(webDriver, baseFrontUrl);
        }
        else {
            log.warn("Проброс кук не выполнен!");
        }
    }
}
