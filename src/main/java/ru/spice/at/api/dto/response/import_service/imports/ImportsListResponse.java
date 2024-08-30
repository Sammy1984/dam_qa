package ru.spice.at.api.dto.response.import_service.imports;

import java.util.List;

import com.google.gson.annotations.SerializedName;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true, fluent = true)
public class ImportsListResponse {
	List<DataItem> data;

	@SerializedName("next_page_token")
	String nextPageToken;
}