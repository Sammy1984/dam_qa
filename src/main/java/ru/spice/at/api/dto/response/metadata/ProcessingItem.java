package ru.spice.at.api.dto.response.metadata;

import com.google.gson.annotations.SerializedName;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true, fluent = true)
public class ProcessingItem {
	@SerializedName("processing_status")
	private ProcessingStatus processingStatus;

	@SerializedName("processing_type")
	private ProcessingType processingType;
}