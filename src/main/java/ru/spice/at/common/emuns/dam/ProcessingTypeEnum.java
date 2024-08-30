package ru.spice.at.common.emuns.dam;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ProcessingTypeEnum {
    NORMALIZATION("Нормализация", "e4439bed-45e9-48f4-a3cc-de0d31eb9843"),
    FIELDS("5% поля", "15864a32-d88f-4743-b9af-b2255ef2ec22"),
    CLIPPING("Обтравка", "2cc01d7c-172b-4760-8493-8531ddedebf1"),
    CREATING_DERIVATIVE("Создание производного", "19373173-2a65-4ceb-9eb1-aa2c4cfdcc91"),
    WATERMARK("Удаление WM", "71839e32-3460-4d24-be13-734ec5efa56d"),
    SQUARE("Квадрат", "9d2b79c3-f125-4037-b46d-c178b2359ec9");

    private final String name;
    private final String id;
}
