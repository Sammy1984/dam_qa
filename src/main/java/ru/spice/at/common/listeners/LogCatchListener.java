package ru.spice.at.common.listeners;

import io.qameta.allure.Attachment;
import org.testng.ITestResult;
import org.testng.TestListenerAdapter;

public class LogCatchListener extends TestListenerAdapter {

    @Override
    public void beforeConfiguration(ITestResult tr) {
        MultithreadedConsoleOutputCatcher.startCatch();
    }

    @SuppressWarnings("UnusedReturnValue")
    @Attachment(value = "Test Log", type = "text/plain")
    public String stopCatch() {
        String result = MultithreadedConsoleOutputCatcher.getContent();
        MultithreadedConsoleOutputCatcher.stopCatch();
        return result;
    }

    @Override
    public void onConfigurationSkip(ITestResult itr) {
        stopCatch();
    }

    @Override
    public void onConfigurationFailure(ITestResult itr) {
        stopCatch();
    }

    @Override
    public void onTestFailure(ITestResult tr) {
        stopCatch();
    }

    @Override
    public void onTestSuccess(ITestResult tr) {
        stopCatch();
    }

    @Override
    public void onTestSkipped(ITestResult tr) {
        stopCatch();
    }
}
