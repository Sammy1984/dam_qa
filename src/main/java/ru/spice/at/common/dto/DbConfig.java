package ru.spice.at.common.dto;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * ДТО для подгрузки конфигурации базы данных
 * @author Aleksandr Osokin
 */
@Data
@Accessors(chain = true, fluent = true)
public class DbConfig {
    private String host;
    private String port;
    private String dbName;
    private String username;
    private String password;
    private String type;
}