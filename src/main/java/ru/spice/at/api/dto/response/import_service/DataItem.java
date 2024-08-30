package ru.spice.at.api.dto.response.import_service;

import java.util.List;

import com.google.gson.annotations.SerializedName;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true, fluent = true)
public class DataItem {
	@SerializedName("import_id")
	private String importId;

	@SerializedName("created_at")
	private String createdAt;

	private List<FilesItem> files;

	private User user;

	@SerializedName("ended_at")
	private String endedAt;
}