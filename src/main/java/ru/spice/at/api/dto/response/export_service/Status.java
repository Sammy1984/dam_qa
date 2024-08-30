package ru.spice.at.api.dto.response.export_service;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true, fluent = true)
public class Status{
	private int code;
	private String name;
	private String id;
}