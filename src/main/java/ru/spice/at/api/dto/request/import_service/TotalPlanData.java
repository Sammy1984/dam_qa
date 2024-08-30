package ru.spice.at.api.dto.request.import_service;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true, fluent = true)
public class TotalPlanData {

    @JsonProperty("total_plan")
    private Object totalPlan;
}