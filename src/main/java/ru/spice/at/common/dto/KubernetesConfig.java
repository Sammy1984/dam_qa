package ru.spice.at.common.dto;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * ДТО для работы с kubernetes (port-forward)
 * @author Aleksandr Osokin
 */
@Data
@Accessors(chain = true, fluent = true)
public class KubernetesConfig {
    private String namespace;
    private String label;
}