package ru.spice.at.api.dto.response.export_service;

import com.google.gson.annotations.SerializedName;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true, fluent = true)
public class ExportsDataItem {
	@SerializedName("plan_metadata_ids_count")
	private int planMetadataIdsCount;
	private String filename;
	@SerializedName("created_at")
	private String createdAt;
	private String id;
	private String url;
	private Status status;
}