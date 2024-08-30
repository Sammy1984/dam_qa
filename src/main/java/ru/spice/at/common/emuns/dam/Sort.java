package ru.spice.at.common.emuns.dam;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum Sort {
    CREATED_ASC("created_asc"),
    CREATED_DESC("created_desc"),
    UPDATED_ASC("updated_asc"),
    UPDATED_DESC("updated_desc"),
    FILENAME_ASC("filename_asc"),
    FILENAME_DESC("filename_desc");

    private final String name;
}
