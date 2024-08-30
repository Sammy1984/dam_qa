package ru.spice.at.api.dto.response.metadata;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true, fluent = true)
public class ProcessingType {
	private int code;
	private String name;
	private String id;
}