package ru.spice.at.api.dto.response.import_service;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true, fluent = true)
public class ErrorResponse {
	private Error error;
}