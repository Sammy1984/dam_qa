package ru.spice.at.api.dto.response.export_service;

import java.util.List;

import com.google.gson.annotations.SerializedName;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true, fluent = true)
public class ExportsResponse {
	private List<ExportsDataItem> data;

	@SerializedName("next_page_token")
	private String nextPageToken;
}