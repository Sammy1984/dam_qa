package ru.spice.at.common.listeners;

import io.qameta.allure.Allure;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.testng.ITestResult;
import org.testng.TestListenerAdapter;

import java.io.ByteArrayInputStream;

public class TestAllureListener extends TestListenerAdapter {

    private String getTestMethodName(ITestResult result) {
        return result.getMethod().getConstructorOrMethod().getName();
    }

    private String getTestClassName(ITestResult result) {
        return result.getTestClass().getName();
    }

    @Override
    public void onTestFailure(ITestResult result) {
        try {
            Object webDriverAttribute = result.getTestContext().getAttribute(getTestClassName(result) + getTestMethodName(result));
            boolean interrupted = Thread.interrupted();
            byte[] screenshot = ((TakesScreenshot) webDriverAttribute).getScreenshotAs(OutputType.BYTES);
            try (ByteArrayInputStream is = new ByteArrayInputStream(screenshot)) {
                Allure.addAttachment("Cкриншот страницы падения теста", "image/png", is, ".png");
            }
            if (interrupted) {
                Thread.currentThread().interrupt();
            }
        } catch (Exception e) {
            Allure.addAttachment("Сообщение обработчика", "text/txt", "Не удалось сделать скриншот страницы падения теста:\n" + e.getMessage());
        }
    }
}
