package ru.spice.at.common.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * ДТО для подгрузки данных для взаимодействия с kafka
 * @author Aleksandr Osokin
 */
@Data
@Accessors(chain = true, fluent = true)
public class Kafka {
    private String bootstrapServer;
    private List<KafkaCredentials> credentials;
}
