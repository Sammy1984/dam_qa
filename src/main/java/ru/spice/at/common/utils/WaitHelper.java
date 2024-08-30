package ru.spice.at.common.utils;

import io.qameta.allure.Step;
import org.awaitility.core.ThrowingRunnable;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import static java.time.temporal.ChronoUnit.SECONDS;
import static org.awaitility.Awaitility.with;

public class WaitHelper {

    /**
     * Ожидание успешного прохождения ассертов с временем ожидания 5 секунд
     *
     * @param assertion лямбда функция с ассертами
     */
    public static void withRetriesAsserted(final ThrowingRunnable assertion) {
        withRetriesAsserted(assertion, 5);
    }

    /**
     * Ожидание успешного прохождения ассертов с утановкой времени ожидания
     *
     * @param assertion    лямбда функция с ассертами
     * @param awaitSeconds время ожидания
     */
    @Step("Ожидание успешного assert с max ожиданием {awaitSeconds} сек.")
    public static void withRetriesAsserted(final ThrowingRunnable assertion, final Integer awaitSeconds) {
        with()
                .pollInSameThread()
                .pollInterval(Duration.of(1, SECONDS))
                .await()
                .atMost(awaitSeconds, TimeUnit.SECONDS)
                .untilAsserted(assertion);
    }
}
