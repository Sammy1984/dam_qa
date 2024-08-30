package ru.spice.at.api.dto.response.import_service;

import java.util.List;

import com.google.gson.annotations.SerializedName;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true, fluent = true)
public class Error {
	@SerializedName("invalid_params")
	private List<InvalidParamsItem> invalidParams;
	private String type;
	private String title;
	private int status;
}