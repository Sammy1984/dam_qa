package ru.spice.at.ui.export_media;

import lombok.Data;
import lombok.experimental.Accessors;
import ru.spice.at.common.dto.dam.ImageData;

import java.util.List;

@Data
@Accessors(chain = true, fluent = true)
public class ExportMediaSettings {
    private String archive;
    private List<ImageData> images;
}
