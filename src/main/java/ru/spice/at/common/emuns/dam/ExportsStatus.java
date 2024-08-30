package ru.spice.at.common.emuns.dam;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ExportsStatus {
    WORK("work"),
    DONE("done"),
    FAILED("failed"),
    DELETED("deleted");

    private final String name;
}