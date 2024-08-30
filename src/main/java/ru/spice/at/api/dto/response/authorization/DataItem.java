package ru.spice.at.api.dto.response.authorization;

import java.util.List;

import com.google.gson.annotations.SerializedName;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true, fluent = true)
public class DataItem {
	private List<String> access;
	private String resource;
	@SerializedName("service_id")
	private String serviceId;
	private String permission;
}