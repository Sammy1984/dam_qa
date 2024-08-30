package ru.spice.at.ui.pages;

import lombok.extern.log4j.Log4j2;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.PageFactory;
import ru.spice.at.ui.utils.UiUtils;
import ru.spice.at.ui.annotations.Hidden;
import ru.spice.at.ui.annotations.Mandatory;
import ru.spice.at.ui.annotations.Name;
import ru.spice.at.ui.controls.Element;

import java.util.ArrayList;
import java.util.Arrays;


/**
 * Базовый класс от которого наследуются все Page
 * @author Aleksandr Osokin
 */
@Log4j2
public abstract class BasePage extends PageFactory {
    private final WebDriver webDriver;
    private String pageName;

    protected BasePage(WebDriver webDriver) {
        try {
            pageName = getClass().getAnnotation(Name.class).value();
            log.info("Инициализируем страницу '{}'", pageName);
        }
        catch (NullPointerException e) {
            log.warn("Инициализируем страницу без названия");
        }
        this.webDriver = webDriver;
        UiUtils.initWebDriverWaitPageLoad(webDriver);
        initElements(webDriver, this);
    }

    protected WebDriver getWebDriver() {
        return webDriver;
    }

    protected String getPageName() {
        return pageName;
    }

    public <T extends BasePage> T checkLoadPage() {
        Arrays.stream(getClass().getDeclaredFields()).
                forEach(field -> {
                            try {
                                if (field.isAnnotationPresent(Mandatory.class)) {
                                    field.setAccessible(true);
                                    WebElement element = (WebElement) field.get(this);
                                    new Element(element, "Главный элемент", webDriver).waitForElement();
                                } else if (field.isAnnotationPresent(Hidden.class)) {
                                    field.setAccessible(true);
                                    WebElement element = (WebElement) field.get(this);
                                    new Element(element, "Скелетон/шиммер", webDriver).waitForInvisibilityOfElement();
                                }
                            } catch (IllegalAccessException e) {
                                e.printStackTrace();
                            }
                        }
                );
        return (T) this;
    }

    public void goToPage(String pageUrl) {
        if (webDriver != null) {
            webDriver.get(pageUrl);
        }
    }

    public void back() {
        if (webDriver != null) {
            webDriver.navigate().back();
        }
    }

    public void forward() {
        if (webDriver != null) {
            webDriver.navigate().forward();
        }
    }

    public void refresh() {
        if (webDriver != null) {
            webDriver.navigate().refresh();
        }
    }

    public void switchTo(int tabNumber) {
        if (webDriver != null) {
            ArrayList<String> tabs = new ArrayList<>(webDriver.getWindowHandles());
            webDriver.switchTo().window(tabs.get(tabNumber));
        }
    }

    public void clearCookies() {
        if (webDriver != null) {
            webDriver.manage().deleteAllCookies();
        }
    }
}
