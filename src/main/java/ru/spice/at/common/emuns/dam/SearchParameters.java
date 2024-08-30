package ru.spice.at.common.emuns.dam;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum SearchParameters {
    SEARCH("search"),
    SORT("sort"),
    LIMIT("limit"),
    OFFSET("offset"),
    Q_LINK("qlink");

    private final String name;
}
