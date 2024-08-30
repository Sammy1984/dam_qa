package ru.spice.at.common.emuns;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * Описание ролей для пользователей
 */
@AllArgsConstructor
@Getter
public enum Role {

    ADMINISTRATOR("administrator", "Админов Админ"),
    PHOTOPRODUCTION("photoproduction", "Продакшенов Фото"),
    CONTENT_PRODUCTION("content_production", "Продакшенов Контент"),
    CONTENT_SUPPORT("content_support", "Саппортов Контент"),
    PHOTOPRODUCTION_OUTSOURCE("photoproduction_outsource", "Продакшенов Фото Аутсорсович"),
    CONTENT_PRODUCTION_OUTSOURCE("content_production_outsource", "Продакшенов Контент Аутсорсович");

    private final String name;
    private final String fullName;

    public static Role fromName(String name) {
        return Arrays.stream(Role.values()).
                filter(x -> name.equalsIgnoreCase(x.toString())).findAny().
                orElseThrow(() -> new IllegalArgumentException(String.format("Указанный роль '%s' не найдена", name)));
    }

    @Override
    public String toString() {
        return name;
    }
}
