package ru.spice.at.api.dto.response.metadata;

import com.google.gson.annotations.SerializedName;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true, fluent = true)
public class UsersResponse {
    private List<UsersItem> data;

    @SerializedName("next_page_token")
    private String nextPageToken;
}