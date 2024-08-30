package ru.spice.at.common.emuns.dam;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum Quality {
    BAD("Плохое"),
    GOOD("Хорошее"),
    TO_REVISION("На доработку");

    private final String name;
}
