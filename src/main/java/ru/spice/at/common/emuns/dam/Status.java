package ru.spice.at.common.emuns.dam;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum Status {
    NEW("Новый"),
    IN_PROGRESS("В работе"),
    ACTUAL("Актуальный"),
    ARCHIVE("Архивный"),
    DELETE("Удаленный"),
    READY_FOR_TEST("Готов к проверке");

    private final String name;
}
