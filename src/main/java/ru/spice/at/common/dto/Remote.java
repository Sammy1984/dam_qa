package ru.spice.at.common.dto;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * ДТО для подгрузки данных удаленного запуска UI тестов
 * @author Aleksandr Osokin
 */
@Data
@Accessors(chain = true, fluent = true)
public class Remote {
    private String host;
    private String scheme;
    private String login;
    private String password;
}
