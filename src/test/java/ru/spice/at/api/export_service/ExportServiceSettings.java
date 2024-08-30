package ru.spice.at.api.export_service;

import lombok.Data;
import lombok.experimental.Accessors;
import ru.spice.at.api.dto.response.metadata.InvalidParamsItem;
import ru.spice.at.common.dto.dam.ImageData;

import java.util.List;

@Data
@Accessors(chain = true, fluent = true)
public class ExportServiceSettings {
    private List<ImageData> images;
    private List<InvalidParamsItem> invalidParams;
}
