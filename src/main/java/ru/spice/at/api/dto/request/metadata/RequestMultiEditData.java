package ru.spice.at.api.dto.request.metadata;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RequestMultiEditData {

	@JsonProperty("is_own_trademark")
	private Object isOwnTrademark;

	@JsonProperty("keywords")
	private List<String> keywords;

	@JsonProperty("received")
	private String received;

	@JsonProperty("is_copyright")
	private Object isCopyright;

	@JsonProperty("article")
	private List<String> article;

	@JsonProperty("offer")
	private String offer;

	@JsonProperty("status_id")
	private String statusId;

	@JsonProperty("master_category_id")
	private String masterCategoryId;

	@JsonProperty("is_main_image")
	private Object isMainImage;

	@JsonProperty("quality_id")
	private String qualityId;

	@JsonProperty("master_seller_id")
	private String masterSellerId;

	@JsonProperty("ids")
	private List<String> ids;

	@JsonProperty("source_id")
	private String sourceId;

	@JsonProperty("sku")
	private String sku;

	@JsonProperty("assignee_id")
	private String assigneeId;

	@JsonProperty("external_task_id")
	private String externalTaskId;
}