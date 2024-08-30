package ru.spice.at.api.dto.response.metadata.filtration;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class DataItem {
    @JsonProperty("filename")
    private String filename;

    @JsonProperty("id")
    private String id;

    @JsonProperty("sku")
    private String sku;

    @JsonProperty("url")
    private String url;

    @JsonProperty("key")
    private String key;
}