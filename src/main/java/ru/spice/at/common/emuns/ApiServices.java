package ru.spice.at.common.emuns;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * Описание Api сервисов
 */
@AllArgsConstructor
@Getter
public enum ApiServices {

    IMPORT_SERVICE("import_service"),
    EXPORT_SERVICE("export_service"),
    METADATA_SERVICE("metadata_service"),
    RETAILER_MEDIA_IMPORT_SERVICE("retailer_media_import_service"),

    /**
     * Проверка сервиса авторизации и ролевой модели
     */
    AUTHORIZATION("authorization"),

    /**
     * Проверка межсервесных end to end сценариев
     */
    END_TO_END("end_to_end");

    private final String name;

    public static ApiServices fromName(String name) {
        return Arrays.stream(ApiServices.values()).
                filter(x -> name.equalsIgnoreCase(x.toString())).findAny().
                orElseThrow(() -> new IllegalArgumentException(String.format("Указанный сервис '%s' не найден", name)));
    }

    @Override
    public String toString() {
        return name;
    }
}
