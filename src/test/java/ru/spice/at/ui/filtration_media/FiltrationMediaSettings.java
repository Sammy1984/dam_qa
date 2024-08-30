package ru.spice.at.ui.filtration_media;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true, fluent = true)
public class FiltrationMediaSettings {
    private String createdAsc;
    private String createdDesc;
    private String updatedAsc;
    private String updatedDesc;
    private String emptyField;
}
