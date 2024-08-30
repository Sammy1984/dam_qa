package ru.spice.at.common.base_test;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Базовые настройки
 */
@Data
@Accessors(chain = true, fluent = true)
public class BaseTestSettings {
    private String url;

    private String fpName;
}
