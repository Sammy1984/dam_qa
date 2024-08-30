package ru.spice.at.common.utils;

import com.fasterxml.jackson.databind.JsonNode;
import io.qameta.allure.Step;
import lombok.extern.log4j.Log4j2;
import net.javacrumbs.jsonunit.JsonAssert;
import net.javacrumbs.jsonunit.core.Option;
import org.opentest4j.MultipleFailuresError;

import java.util.*;
import java.util.stream.Collectors;

import static org.testng.Assert.*;

@Log4j2
public class Assert {

    @Step("Проверяем равенство параметра {paramName} значению {expected}")
    public static void equalsParameters(Object expected, Object actual, String paramName) {
        assertEquals(actual, expected, "Значение параметра " + paramName + " не соответствует ожидаемому");
    }

    @Step("Проверяем соответствие параметра {paramName} значению {expected}")
    public static <T extends Enum<T>> void equalsParameters(T expected, String actual, String paramName) {
        assertEquals(actual, expected.name(),
                "Значение параметра " + paramName + " не соответствует ожидаемому");
    }

    @Step("Проверяем соответствует ли {actual} регулярному выражению {regex}")
    public static void compareRegexParameters(String regex, String actual, String paramName) {
        org.testng.Assert.assertTrue(actual.matches(regex), paramName + " не соответствует регулярному выражению: " + regex);
    }

    @Step("Проверяем соответствие параметра {paramName} значению {expected}")
    public static void compareParameters(Object expected, Object actual, String paramName) {
        if (CompereHelper.compareTo(expected, actual) != 0) {
            String exceptionText = String.format("Значение параметра '%s' не соответствует ожидаемому:%n\t expected: %s %n\t actual: %s",
                    paramName, expected, actual);
            fail(exceptionText);
        }
    }

    @Step("Проверяем, что значение параметра {paramName} не равно null, 0 и не пустое")
    public static void notNullOrEmptyParameter(Object value, String paramName) {
        assertNotNull(value, "Значение параметра " + paramName + " равно null");
        assertNotEquals(value, "", "Значение параметра " + paramName + " пустое");
        assertNotEquals(value, 0, "Значение параметра " + paramName + " равно 0");
    }

    @Step("Проверяем, что значение параметра {paramName} равно null")
    public static void mustBeNullParameter(Object value, String paramName) {
        assertNull(value, "Значение параметра " + paramName + " не равно null");
    }

    @Step("Проверяем, что значение параметра {paramName} пустое")
    public static void mustBeEmptyParameter(Object value, String paramName) {
        assertEquals(value, "", "Значение параметра " + paramName + " не пустое");
    }

    @Step("Проверяем, что массив {paramName} пустой")
    public static <T> void mustBeEmptyList(List<T> value, String paramName) {
        assertTrue(value.isEmpty(), "Массив " + paramName + " не пустой");
    }

    @Step("Проверяем, что массив {paramName} отсутствует")
    public static <T> void mustBeNullList(List<T> value, String paramName) {
        assertNull(value, "Массив " + paramName + " не null");
    }

    @Step("Проверяем соответствие ожидаемого и фактического json")
    public static void equalsJson(String expected, String actual) {
        String expectedJson = expected.replaceAll("\\s+", "");
        String actualJson = actual.replaceAll("\\s+", "");
        JsonAssert.assertJsonEquals(expectedJson, actualJson);
    }

    @Step("Проверяем, что значение параметра {paramName} равно true")
    public static void equalsTrueParameter(Boolean actual, String paramName) {
        assertTrue(actual, "Значение параметра " + paramName + " не соответствует true");
    }

    @Step("Проверяем, что значение параметра {paramName} равно false")
    public static void equalsFalseParameter(Boolean actual, String paramName) {
        assertFalse(actual, "Значение параметра " + paramName + " не соответствует false");
    }

    /**
     * Сравнивает два json node без учёта null nodes в актуальной json node.
     * То есть если вы ожидаете {"test":{"a":1}}, то {"test":{"a":1, "b": null}} будет считаться эквивалентным.
     *
     * @param expected шаблон объекта в виде JsonNode
     * @param actual   фактический объект в виде JsonNode
     */
    @Step("Проверяем равенство {expected} значению {actual}")
    public static void assertJsonEquals(JsonNode expected, JsonNode actual) {
        JsonAssert.setOptions(Option.TREATING_NULL_AS_ABSENT);
        JsonAssert.assertJsonEquals(expected, actual);
    }

//    //////////////////////////////////////////////////////////
//    //////////////////////////////////////////////////////////
//    ВТОРАЯ ЧАСТЬ С ДЕСКРИПШИНАМИ НАПИСАННЫМИ РУКАМИ
//    //////////////////////////////////////////////////////////


    @Step("{description}")
    @Deprecated
    public static void equals(Object expected, Object actual, String description) {
        log.info(description);
        assertEquals(actual, expected);
    }

    @Step("{description}")
    public static void contains(String expected, String actual, String description) {
        log.info(description);
        assertTrue(actual.contains(expected));
    }

    @Step("{description}")
    @Deprecated
    public static void equals(String expected, String actual, String description) {
        log.info(description);
        assertEquals(actual, expected);
    }

    @Step("{description}")
    @Deprecated
    public static void notNullOrEmpty(String value, String description) {
        log.info(description);
        assertNotNull(value);
        assertNotEquals(value, "");
    }

    @Step("{description}")
    @Deprecated
    public static void mustBeNull(String value, String description) {
        log.info(description);
        assertNull(value);
    }

    @Step("{description}")
    public static void notEquals(Object expected, Object actual, String description) {
        log.info(description);
        assertNotEquals(actual, expected);
    }

    @Step("{description}")
    public static void notEquals(String expected, String actual, String description) {
        log.info(description);
        assertNotEquals(actual, expected);
    }

    @Step("{description}")
    @Deprecated
    public static void compareDates(Date last, Date before, String description) {
        log.info(description);
        assertTrue(last.compareTo(before) > 0);
    }

    @Step("{description}")
    @Deprecated
    public static <T extends Enum<T>> void equals(T expected, String actual, String description) {
        log.info(description);
        assertEquals(actual, expected.toString());
    }

    @Step("{description}")
    @Deprecated
    public static void equalsTrue(Boolean actual, String description) {
        assertTrue(actual, description);
    }

    @Step("{description}")
    @Deprecated
    public static void equalsFalse(Boolean actual, String description) {
        assertFalse(actual, description);
    }

    //softAssert аналог junit
    public static void assertAll(Runnable... runnables) {
        assertAll(null, runnables);
    }

    public static void assertAll(String heading, Runnable... runnables) {
        if (runnables == null || runnables.length == 0) {
            throw new RuntimeException("executables array must not be null or empty");
        }
        Arrays.stream(runnables).forEach((runnable) -> {
            if (runnable == null)
                throw new RuntimeException("individual executables must not be null");
        });
        List<AssertionError> failures = Arrays.stream(runnables).map((runnable) -> {
            try {
                runnable.run();
                return null;
            } catch (AssertionError fail) {
                return fail;
            }
        }).filter(Objects::nonNull).collect(Collectors.toList());
        if (!failures.isEmpty()) {
            MultipleFailuresError multipleFailuresError = new MultipleFailuresError(heading, failures);
            Objects.requireNonNull(multipleFailuresError);
            failures.forEach(multipleFailuresError::addSuppressed);
            throw multipleFailuresError;
        }
    }
}

