package ru.spice.at.api.dto.response.metadata;

import com.google.gson.annotations.SerializedName;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true, fluent = true)
public class UsersItem {
    private String id;

    @SerializedName("full_name")
    private String fullName;

    private String position;
}
