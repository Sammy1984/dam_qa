package ru.spice.at.common.emuns;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * Описание Ui категорий
 */
@AllArgsConstructor
@Getter
public enum UiCategories {
    IMPORT_MEDIA("import_media"),
    EXPORT_MEDIA("export_media"),
    FILTRATION_MEDIA("filtration_media"),
    EDIT_MEDIA("edit_media"),
    DAM_PIMS_INTEGRATION("dam_pims_integration");

    private final String name;

    public static UiCategories fromName(String name) {
        return Arrays.stream(UiCategories.values()).
                filter(x -> name.equalsIgnoreCase(x.toString())).findAny().
                orElseThrow(() -> new IllegalArgumentException(String.format("Указанная категория '%s' не найдена", name)));
    }

    @Override
    public String toString() {
        return name;
    }
}
