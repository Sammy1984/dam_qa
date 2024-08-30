package ru.spice.at.api.dto.response.metadata;

import com.google.gson.annotations.SerializedName;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true, fluent = true)
public class RetailersItem {
    @SerializedName("ext_id")
    private int extId;
    private String name;
    @SerializedName("business_area")
    private BusinessArea businessArea;
    private String id;
}