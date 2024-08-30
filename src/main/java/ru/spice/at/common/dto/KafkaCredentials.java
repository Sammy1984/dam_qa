package ru.spice.at.common.dto;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * ДТО для подгрузки данных авторизации кафка
 * @author Aleksandr Osokin
 */
@Data
@Accessors(chain = true, fluent = true)
public class KafkaCredentials {
    private String topic;
    private String login;
    private String password;
}
