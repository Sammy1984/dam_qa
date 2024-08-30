package ru.spice.at.api.dto.response.import_service;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true, fluent = true)
public class FilesItem {
	private String filename;
	private List<ErrorItem> errors;
}