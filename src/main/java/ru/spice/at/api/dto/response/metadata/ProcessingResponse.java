package ru.spice.at.api.dto.response.metadata;

import java.util.List;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true, fluent = true)
public class ProcessingResponse {
	private List<ProcessingItem> data;
}