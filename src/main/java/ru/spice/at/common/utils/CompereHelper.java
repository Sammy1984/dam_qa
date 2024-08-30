package ru.spice.at.common.utils;

import lombok.SneakyThrows;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public final class CompereHelper {
    /**
     * Сравнение переданных объектов одного класса, по поля, поиск до первого расхождения.
     * 0 - сравниваемые объекты равны;
     * -10 - разные классы и разный hashcode;
     * -20 - enum объекты не равны;
     * -30 - размеры переданных List не равны;
     * -40 - в списке expected есть элементы, которые отличающиеся от actual;
     * -50 - только один из объектов равен null;
     *
     * @param expected - объект проверки;
     * @param actual   - ожидаемый объект;
     * @return - код результата сравнения;
     */
    public static int compareTo(Object expected, Object actual) {
        if (null != actual && null != expected) {
            if (isArray(expected) && isArray(actual)) {
                return checkList((List) expected, (List) actual);
            }
            if (actual.getClass() != expected.getClass()) {
                if (!actual.equals(expected)) {
                    return checkInnerFields(expected, actual);
                }
                if (actual.hashCode() != expected.hashCode()) {
                    return -10;
                }
                return 0;
            }
            if (expected.getClass().isEnum()) {
                return actual == expected ? 0 : -20;
            }
            if (Stream.of(expected.getClass().getInterfaces()).anyMatch(s -> s.isAssignableFrom(Comparable.class))) {
                return ((Comparable) expected).compareTo(actual);
            }
            return checkInnerFields(expected, actual);
        }
        if (null == actual ^ null == expected) {
            return -50;
        }
        return 0;
    }

    @SneakyThrows
    private static int checkInnerFields(Object expected, Object actual) {
        Field[] expectedFields = expected.getClass().getDeclaredFields();
        for (Field field : expectedFields) {
            field.setAccessible(true);
            Field actualField = actual.getClass().getDeclaredField(field.getName());
            actualField.setAccessible(true);
            Object actualValue = actualField.get(actual);
            Object expectedValue = field.get(expected);
            int res = compareTo(expectedValue, actualValue);
            if (res != 0) return res;
        }
        return 0;
    }

    private static int checkList(List expected, List actual) {
        if (expected.size() != actual.size()) {
            return -30;
        }
        if (!expected.isEmpty() && (!actual.containsAll(expected) || !expected.containsAll(actual))) {
            return actual.stream().anyMatch(a ->
                    expected.stream().noneMatch(e -> compareTo(e, a) == 0)) ? -40 : 0;
        }
        return 0;
    }

    private static boolean isArray(Object element) {
        return Stream.of(element.getClass().getInterfaces()).anyMatch(s -> s.isAssignableFrom(List.class))
                || element.getClass().getDeclaringClass() == Arrays.class;
    }
}

