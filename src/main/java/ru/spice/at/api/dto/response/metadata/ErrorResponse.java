package ru.spice.at.api.dto.response.metadata;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true, fluent = true)
public class ErrorResponse {
	Error error;
}