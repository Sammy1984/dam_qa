package ru.spice.at.common.listeners;

import io.qameta.allure.Allure;
import org.testng.ITestListener;
import org.testng.ITestResult;
import ru.testit.annotations.WorkItemId;
import ru.testit.annotations.WorkItemIds;

import java.lang.reflect.Method;
import java.util.Arrays;

public class TmsLinksListener implements ITestListener {

    private static final String TEST_IT_DAM_LINK = "https://testit.sbmt.io/projects/191796/tests/%s";

    @Override
    public void onTestSuccess(ITestResult result) {
        addTmsLink(result);
    }

    @Override
    public void onTestFailure(ITestResult result) {
        addTmsLink(result);
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        addTmsLink(result);
    }

    private void addTmsLink(ITestResult result) {
        Method method = result.getMethod().getConstructorOrMethod().getMethod();
        final WorkItemId workItem = method.getAnnotation(WorkItemId.class);
        if (workItem != null) {
            Allure.tms(workItem.value(), String.format(TEST_IT_DAM_LINK, workItem.value()));
            return;
        }

        final WorkItemIds workItems = method.getAnnotation(WorkItemIds.class);
        if (workItems != null) {
            Arrays.stream(workItems.value()).forEach(link ->
                    Allure.tms(link, String.format(TEST_IT_DAM_LINK, link)));
        }
    }
}
