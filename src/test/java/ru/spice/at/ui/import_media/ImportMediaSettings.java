package ru.spice.at.ui.import_media;

import lombok.Data;
import lombok.experimental.Accessors;
import ru.spice.at.common.dto.dam.ImageData;

import java.util.List;

@Data
@Accessors(chain = true, fluent = true)
public class ImportMediaSettings {
    private List<ImageData> images;
    private String successTitle;
    private String successImport;
    private String successMessage;
    private String unsuccessfulTitle;
    private String unsuccessfulMessage;
    private String unsuccessfulAddFileMessage;
    private String repeatedTitle;
    private String repeatedMessage;
    private String repeatedMainMessage;
    private String typeError;
    private String fileError;
    private String formatError;
    private String nameError;
    private String longNameError;
    private String tagsError;

    private List<String> invalidNames;
}
