package ru.spice.at.api.end_to_end;

import lombok.Data;
import lombok.experimental.Accessors;
import ru.spice.at.api.retailer_media_import_service.RetailerMediaImportSettings;

@Data
@Accessors(chain = true, fluent = true)
public class EndToEndSettings {
    private RetailerMediaImportSettings.ImportParameters importParameters;
    private String baseExternalUrl;
    private String externalToken;
}