package ru.spice.at.common.dto;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * ДТО для подгрузки данных авторизации
 * @author Aleksandr Osokin
 */
@Data
@Accessors(chain = true, fluent = true)
public class AuthCredentials {
    private String alias;
    private String authUrl;
}
