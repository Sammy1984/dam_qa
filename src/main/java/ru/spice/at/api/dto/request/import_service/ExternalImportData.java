package ru.spice.at.api.dto.request.import_service;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true, fluent = true)
public class ExternalImportData {

	@JsonProperty("file")
	private String file;

	@JsonProperty("filename")
	private String filename;

	@JsonProperty("own_trademark")
	private Boolean ownTrademark;

	@JsonProperty("external_offer_name")
	private String externalOfferName;

	@JsonProperty("external_task_id")
	private String externalTaskId;

	@JsonProperty("external_offer_id")
	private Integer externalOfferId;

	@JsonProperty("external_master_category_id")
	private Integer externalMasterCategoryId;

	@JsonProperty("sku")
	private String sku;

	@JsonProperty("priority")
	private Integer priority;

	@JsonProperty("master_seller_id")
	private Integer masterSellerId;

	@JsonProperty("ready_for_retouch_at")
	private String readyForRetouchAt;
}