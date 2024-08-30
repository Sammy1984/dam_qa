package ru.spice.at.ui.edit_media;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true, fluent = true)
public class EditMediaSettings {
    private String assigneeName;
    private String categoryName;
    private String formatFilenameMessage;
    private String longFilenameMessage;
    private String longPriorityMessage;
    private String warningPriorityMessage;
    private String longDescriptionMessage;
    private String longTagsMessage;
    private String longSkuMessage;
    private String warningSkuMessage;
    private String longReceivedMessage;
    private String actualStatusMessage;
    private String masterSellerName;
}