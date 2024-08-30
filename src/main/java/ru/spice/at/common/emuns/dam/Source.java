package ru.spice.at.common.emuns.dam;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum Source {
    BRAND("Бренд"),
    RESTAURANT("Ресторан"),
    RETAILER("Ретейлер"),
    PRODUCER("Производитель"),
    OWN("Собственные"),
    INTERNET_RESOURCE("Интернет ресурс"),
    EXTERNAL_CONTENT_PROVIDER("Внешний контент-поставщик");

    private final String name;
}
