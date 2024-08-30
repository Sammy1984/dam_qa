package ru.spice.at.api.dto.response.metadata;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true, fluent = true)
public class ExportItem {
	private String filename;
	private String id;
	private String key;
}