package ru.spice.at.api.dto.response.import_service.imports.item;

import com.google.gson.annotations.SerializedName;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true, fluent = true)
public class CreatedBy {

    @SerializedName("full_name")
    private String fullName;

    private String id;
}