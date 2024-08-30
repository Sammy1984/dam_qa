package ru.spice.at.api.dto.response.metadata;

import java.util.List;

import com.google.gson.annotations.SerializedName;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true, fluent = true)
public class RetailersResponse {
    private List<RetailersItem> data;

    @SerializedName("next_page_token")
    private String nextPageToken;
}