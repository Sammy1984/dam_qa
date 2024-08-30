package ru.spice.at.common.base_test;

import com.google.common.collect.ImmutableMap;
import io.github.bonigarcia.wdm.WebDriverManager;
import io.qameta.allure.Epic;
import lombok.extern.log4j.Log4j2;
import org.awaitility.Awaitility;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.LocalFileDetector;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import ru.spice.at.common.dto.Remote;
import ru.spice.at.common.emuns.UiCategories;

import java.net.URI;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static java.lang.String.format;
import static ru.spice.at.ui.utils.UiUtils.getStandProperties;

/**
 * Базовый класс для тестовых классов для Ui
 * @author Aleksandr Osokin
 */
@Log4j2
@Epic("UI")
public abstract class BaseUiTest<T> extends BaseTest<T> {
    private static final String SETTINGS_UI_PATH = "src/test/resources/settings/ui/%s/settings.json";
    private static final String BROWSER_DRIVER_PATH = "src/test/resources/drivers/%s/%s%s";

    private WebDriver webDriver;

    protected BaseUiTest(UiCategories uiCategories) {
        super(uiCategories, SETTINGS_UI_PATH);
    }

    /**
     * Инициализация веб-драйвера
     */
    protected void initWebDriver() {
        ChromeOptions options = new ChromeOptions();

        options.addArguments("--lang=ru", "--ignore-certificate-errors",
                "--ignore-urlfetcher-cert-requests", "--start-maximized", "--disable-extensions");
        HashMap<String, Object> prefs = new HashMap<>() {
            {
                put("args", Arrays.asList("--disable-system-timezone-automatic-detection", "--local-timezone"));
                put("profile.default_content_settings.popups", 0);
                put("download.prompt_for_download", false);
                put("download.directory_upgrade", true);
                put("download.default_directory", downloadPath);
                put("safebrowsing.enabled", false);
                put("plugins.always_open_pdf_externally", true);
                put("plugins.plugins_disabled", Collections.singletonList("Chrome PDF Viewer"));
            }
        };
        options.setExperimentalOption("prefs", prefs);
        if (settings.remote()) {
            remoteInit(options);
        } else {
            localInit(options);
        }
    }

    /**
     * Инициализация веб-драйвера для удаленного запуска на Selenoid
     */
    private void remoteInit(ChromeOptions options) {
        log.info("Запуск теста удаленно");
        DesiredCapabilities capabilities = new DesiredCapabilities();
        capabilities.setCapability("browserName", "chrome");
        capabilities.setCapability("browserVersion", settings.version());
        capabilities.setCapability(CapabilityType.ACCEPT_INSECURE_CERTS, true);
        //todo убрано в новой версии selenium
        //capabilities.setCapability(CapabilityType.ACCEPT_SSL_CERTS, true);
        capabilities.setCapability("moon:options",
                ImmutableMap.<String, Object>builder().
                        put("enableVNC", true).
                        put("env", Arrays.asList("LANG=ru_RU.UTF-8", "LANGUAGE=ru:en", "LC_ALL=ru_RU.UTF-8")).
                        build());
        options.merge(capabilities);
        Remote remote = getStandProperties().getRemoteConnect();

        String connectUri = remote.login() == null ? format("%s://%s/wd/hub", remote.scheme(), remote.host()) :
                format("%s://%s:%s@%s/wd/hub", remote.scheme(), remote.login(), remote.password(), remote.host());
        Awaitility.given().pollDelay(300, TimeUnit.MILLISECONDS)
                .pollInterval(1, TimeUnit.SECONDS)
                .and().timeout(getStandProperties().getSettings().remoteConnectionTimeout(), TimeUnit.SECONDS)
                .await("Инициализация драйвера")
                .until(() -> {
                    try {
                        RemoteWebDriver remoteWebDriver = new RemoteWebDriver(
                                URI.create(connectUri).toURL(),
                                options
                        );
                        remoteWebDriver.setFileDetector(new LocalFileDetector());
                        webDriver = remoteWebDriver;
                        return true;
                    }
                    catch (Exception e) {
                        log.warn("Ошибка при инициализации драйвера");
                        return false;
                    }
                });
    }

    /**
     * Инициализация веб-драйвера для локального запуска
     */
    private void localInit(ChromeOptions options) {
        log.info("Запуск теста локально");
        if (settings.driver() != null) {
            String os = System.getProperty("os.name").toLowerCase(Locale.ROOT).contains("win") ? "win" : "mac";
            String driverPath = format(BROWSER_DRIVER_PATH, os, settings.driver(), os.equals("win") ? ".exe" : "");
            System.setProperty("webdriver.chrome.driver", driverPath);
        }
        else {
            WebDriverManager.chromedriver().setup();
        }
        webDriver = new ChromeDriver(options);
        webDriver.manage().window().maximize();
        webDriver.manage().timeouts().pageLoadTimeout(40, TimeUnit.SECONDS);
    }

    protected WebDriver getWebDriver() {
        return webDriver;
    }

    protected void closeWebDriver() {
        webDriver.quit();
    }

    @BeforeMethod(alwaysRun = true)
    public void initTestContext(ITestContext testContext, ITestResult testResult) {
        testContext.setAttribute(testResult.getTestClass().getName() + testResult.getMethod()
                .getConstructorOrMethod().getName(), getWebDriver());
    }

    @BeforeClass(description = "Инициализируем вебдрайвер", alwaysRun = true)
    public void init() {
        log.info("Инициализируем вебдрайвер");
        initWebDriver();
    }

    @AfterClass(description = "Закрываем вебдрайвер", alwaysRun = true)
    public void tearDown() {
        log.info("Закрываем вебдрайвер");
        closeWebDriver();
    }
}
