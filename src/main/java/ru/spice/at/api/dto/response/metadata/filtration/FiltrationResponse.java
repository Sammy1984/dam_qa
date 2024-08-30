package ru.spice.at.api.dto.response.metadata.filtration;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class FiltrationResponse {
    @JsonProperty("data")
    private List<DataItem> data;

    @JsonProperty("total")
    private Integer total;

    @JsonProperty("unavailable")
    private Boolean unavailable;
}