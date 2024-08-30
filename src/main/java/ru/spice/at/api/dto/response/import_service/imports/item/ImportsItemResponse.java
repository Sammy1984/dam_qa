package ru.spice.at.api.dto.response.import_service.imports.item;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true, fluent = true)
public class ImportsItemResponse {
	private ImportsData data;
}