package ru.spice.at.api.dto.response.authorization;

import java.util.List;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true, fluent = true)
public class PermissionsResponse {
	private List<DataItem> data;
}