package ru.spice.at.api.dto.response.import_service.imports.item;

import com.google.gson.annotations.SerializedName;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true, fluent = true)
public class ImportsData {

    @SerializedName("next_page_token")
    private String nextPageToken;

    @SerializedName("import_id")
    private String importId;

    @SerializedName("started_at")
    private String startedAt;

    @SerializedName("import_items")
    private List<ImportItemsItem> importItems;

    @SerializedName("created_by")
    private CreatedBy createdBy;
}