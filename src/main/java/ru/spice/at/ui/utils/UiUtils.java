package ru.spice.at.ui.utils;

import ru.spice.at.common.StandProperties;

import io.restassured.http.Cookies;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.reporters.Files;

import java.io.FileInputStream;
import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Утилитный класс
 * @author Aleksandr Osokin
 */
public class UiUtils {
    private static final StandProperties standProperties = new StandProperties();

    private UiUtils() {
        // private constructor
    }

    public static StandProperties getStandProperties() {
        return standProperties;
    }

    public static void initWebDriverWaitPageLoad(WebDriver webDriver) {
        new WebDriverWait(webDriver, Duration.ofSeconds(100)).until(driver ->
                executeScriptFromFile("src/main/resources/script/getReadyState.js", driver)
                        .equals("complete"));
    }

    public static Cookie restAssuredCookiesToSelenium(io.restassured.http.Cookie cookie, String domain) {
        Cookie.Builder builder = new Cookie.Builder(cookie.getName(), cookie.getValue())
                .isHttpOnly(cookie.isHttpOnly())
                .isSecure(cookie.isSecured())
                .path(cookie.getPath());
        if (cookie.hasExpiryDate()) {
            builder.expiresOn(cookie.getExpiryDate());
        }
        if (cookie.getDomain() == null) {
            builder.domain(domain);
        }
        return builder.build();
    }

    public static List<Cookie> restAssuredCookiesToSelenium(io.restassured.http.Cookies cookies, String domain) {
        List<Cookie> cookieList = new ArrayList<>();
        cookies.asList().forEach(cookie -> cookieList.add(restAssuredCookiesToSelenium(cookie, domain)));
        return cookieList;
    }

    public static io.restassured.http.Cookie seleniumCookiesToRestAssured(Cookie cookie) {
        io.restassured.http.Cookie.Builder builder = new io.restassured.http.Cookie.Builder(cookie.getName(), cookie.getValue())
                .setDomain(cookie.getDomain())
                .setPath(cookie.getPath())
                .setSecured(cookie.isSecure())
                .setHttpOnly(cookie.isHttpOnly());
        if (cookie.getExpiry() != null) {
            builder.setExpiryDate(cookie.getExpiry());
        }
        return builder.build();
    }

    public static io.restassured.http.Cookies seleniumCookiesToRestAssured(Set<Cookie> cookies) {
        List<io.restassured.http.Cookie> cookieList = new ArrayList<>();
        cookies.forEach(cookie -> cookieList.add(seleniumCookiesToRestAssured(cookie)));
        return new Cookies(cookieList);
    }

    public static Object executeScriptFromFile(String scriptFilePath, WebDriver webDriver, Object... args) {
        try (FileInputStream fis = new FileInputStream(scriptFilePath)) {
            String script = Files.readFile(fis);
            return ((JavascriptExecutor) webDriver).executeScript(script, args);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
}
