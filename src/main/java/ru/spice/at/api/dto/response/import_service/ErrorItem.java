package ru.spice.at.api.dto.response.import_service;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true, fluent = true)
public class ErrorItem {
    private String type;
    private String description;
}
