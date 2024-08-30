package ru.spice.at.api.dto.response.import_service.imports.item;

import java.util.List;

import com.google.gson.annotations.SerializedName;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true, fluent = true)
public class ImportItemsItem {

    private String filename;

    private boolean success;

    @SerializedName("validation_error")
    private List<ValidationErrorItem> validationError;

    private String id;
}