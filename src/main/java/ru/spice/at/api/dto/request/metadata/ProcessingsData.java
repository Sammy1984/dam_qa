package ru.spice.at.api.dto.request.metadata;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ProcessingsData {
	@JsonProperty("metadata_ids")
	private List<String> metadataIds;
	@JsonProperty("processing_ids")
	private List<String> processingIds;
}