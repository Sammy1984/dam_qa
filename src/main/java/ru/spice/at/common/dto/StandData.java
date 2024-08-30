package ru.spice.at.common.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * ДТО для подгрузки стендовых данных из файла
 * @author Aleksandr Osokin
 */
@Data
@Accessors(chain = true, fluent = true)
public class StandData {
    private String stand;
    private String baseUri;
    private String frontendUri;
    private AuthCredentials authCredentials;
    private Remote remote;
    private Kafka kafka;
    private DbConfig dbConfig;
    private KubernetesConfig kubernetesConfig;
    private List<User> users;
}
