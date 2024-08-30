package ru.spice.at.api.dto.response.metadata;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true, fluent = true)
public class InvalidParamsItem {
	String reason;
	String name;
	String type;

	public InvalidParamsItem(InvalidParamsItem invalidParamsItem) {
		this.reason = invalidParamsItem.reason;
		this.name = invalidParamsItem.name;
		this.type = invalidParamsItem.type;
	}
}