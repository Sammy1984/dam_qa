package ru.spice.at.api.dto.response.import_service.imports.item;

import com.google.gson.annotations.SerializedName;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true, fluent = true)
public class ValidationErrorItem {

    private String reason;

    private int code;

    private String name;

    @SerializedName("local_name")
    private String localName;
}