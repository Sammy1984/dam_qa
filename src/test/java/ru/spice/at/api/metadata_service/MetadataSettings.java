package ru.spice.at.api.metadata_service;

import lombok.Data;
import lombok.experimental.Accessors;
import ru.spice.at.api.dto.response.metadata.InvalidParamsItem;
import ru.spice.at.api.retailer_media_import_service.RetailerMediaImportSettings;
import ru.spice.at.common.dto.dam.ImageData;

import java.util.List;

@Data
@Accessors(chain = true, fluent = true)
public class MetadataSettings {
    private List<ImageData> images;
    private String archiveStatus;
    private String newStatus;
    private String assigneeName;
    private String createdName;
    private String categoryName;
    private String unknownCategoryName;
    private String retailerName;
    private String retailersExtId;
    private List<String> invalidFilenames;
    private List<String> invalidFormatFilenames;
    private List<InvalidParamsItem> invalidParams;
    private List<InvalidParamsItem> invalidBodyParams;
    private InvalidParamsItem invalidEmptyBodyStringParam;
    private InvalidParamsItem invalidBodyStringParam;
    private InvalidParamsItem invalidProcessingParam;
    private InvalidParamsItem invalidNotFoundParam;
    private InvalidParamsItem invalidParameterValueActual;
    private InvalidParamsItem invalidParameterValueName;
    private RetailerMediaImportSettings.ImportParameters importParameters;
    private RetailerMediaImportSettings.ImportMetadata importMetadata;
    private List<InvalidParamsItem> invalidRetailersDictionaryParams;
}
