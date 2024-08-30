package ru.spice.at.common.emuns.dam;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

@AllArgsConstructor
@Getter
public enum ImageFormat {
    JPEG("jpeg"),
    PNG("png"),
    JPG("jpg"),
    INVALID("invalid"),
    CURRENT("В существующем формате");

    private final String formatName;

    public static ImageFormat getFormatName(String name) {
        return Arrays.stream(ImageFormat.values()).
                filter(x -> name.equalsIgnoreCase(x.toString())).findAny().
                orElseThrow(() -> new IllegalArgumentException(String.format("Указанное расширение '%s' не найдено", name)));
    }
}
