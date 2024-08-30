package ru.spice.at.db.dto;

import com.google.gson.annotations.SerializedName;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

@Data
@Builder(toBuilder = true)
@Accessors(chain = true, fluent = true)
public class MetadataDto {
    @SerializedName("id")
    private String id;

    @SerializedName("key")
    private String key;

    @SerializedName("url")
    private String url;

    @SerializedName("format")
    private String format;

    @SerializedName("size")
    private Integer size;

    @SerializedName("width")
    private Integer width;

    @SerializedName("height")
    private Integer height;

    @SerializedName("resolution")
    private String resolution;

    @SerializedName("description")
    private String description;

    @SerializedName("keywords")
    private String keywords;

    @SerializedName("created_at")
    private String createdAt;

    @SerializedName("updated_at")
    private String updatedAt;

    @SerializedName("filename")
    private String filename;

    @SerializedName("received")
    private String received;

    @SerializedName("source_id")
    private String sourceId;

    @SerializedName("status_id")
    private String statusId;

    @SerializedName("quality_id")
    private String qualityId;

    @SerializedName("is_main_image")
    private Boolean isMainImage;

    @SerializedName("is_raw_image")
    private Boolean isRawImage;

    @SerializedName("is_own_trademark")
    private Boolean isOwnTrademark;

    @SerializedName("is_copyright")
    private Boolean isCopyright;

    @SerializedName("assignee_id")
    private String assigneeId;

    @SerializedName("master_category_id")
    private String masterCategoryId;

    @SerializedName("priority")
    private Integer priority;

    @SerializedName("created_by")
    private String createdBy;

    @SerializedName("updated_by")
    private String updatedBy;

    @SerializedName("origin_filename")
    private String originFilename;

    @SerializedName("master_seller_id")
    private Integer masterSellerId;

    @SerializedName("external_task_id")
    private Integer externalTaskId;

    @SerializedName("external_draft_done")
    private Boolean externalDraftDone;

    @SerializedName("external_offer_id")
    private BigDecimal externalOfferId;

    @SerializedName("external_offer_name")
    private String externalOfferName;

    @SerializedName("hash")
    private String hash;

    @SerializedName("is_duplicate")
    private Boolean isDuplicate;

    @SerializedName("import_type_id")
    private String importTypeId;
}