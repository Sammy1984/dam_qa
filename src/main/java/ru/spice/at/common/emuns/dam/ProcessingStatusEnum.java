package ru.spice.at.common.emuns.dam;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ProcessingStatusEnum {
    IN_PROCESS("В процессе"),
    SUCCESS("Успех"),
    ERROR("Ошибка"),
    AWAITING("Ожидает обработки");

    private final String name;
}
