package ru.spice.at.api.dto.response.metadata;

import java.util.List;

import com.google.gson.annotations.SerializedName;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true, fluent = true)
public class Error {
	@SerializedName("invalid_params")
	List<InvalidParamsItem> invalidParams;
	String type;
	String title;
	Integer status;
}