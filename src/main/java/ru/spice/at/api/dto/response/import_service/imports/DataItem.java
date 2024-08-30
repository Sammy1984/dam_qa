package ru.spice.at.api.dto.response.import_service.imports;

import com.google.gson.annotations.SerializedName;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true, fluent = true)
public class DataItem {

	@SerializedName("total_error")
    int totalError;

	@SerializedName("import_id")
    String importId;

	@SerializedName("total_fact")
    int totalFact;

	@SerializedName("started_at")
    String startedAt;

	@SerializedName("created_by")
    CreatedBy createdBy;
}