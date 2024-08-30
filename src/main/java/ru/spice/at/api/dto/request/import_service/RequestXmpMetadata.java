package ru.spice.at.api.dto.request.import_service;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true, fluent = true)
public class RequestXmpMetadata {
    private String assigneeId;
    private String copyright;
    private String createdAt;
    private String createdBy;
    private String description;
    private String externalDraftDone;
    private String externalOfferId;
    private String externalOfferName;
    private String externalTaskId;
    private String filename;
    private String importTypeCode;
    private String importTypeId;
    private String importTypeName;
    private String key;
    private List<String> keywords;
    private String masterCategoryId;
    private String masterCategoryName;
    private String masterSellerId;
    private String metadataId;
    private String originFilename;
    private String ownTrademark;
    private String priority;
    private String qualityCode;
    private String qualityId;
    private String qualityName;
    private String rawImage;
    private String received;
    private String sku;
    private String sourceCode;
    private String sourceId;
    private String sourceName;
    private String statusCode;
    private String statusName;
    private String statusId;
    private String updatedAt;
    private String updatedBy;

    @Override
    public RequestXmpMetadata clone() {
        return new RequestXmpMetadata()
                .assigneeId(this.assigneeId)
                .copyright(this.copyright)
                .createdAt(this.createdAt)
                .createdBy(this.createdBy)
                .description(this.description)
                .externalDraftDone(this.externalDraftDone)
                .externalOfferId(this.externalOfferId)
                .externalOfferName(this.externalOfferName)
                .externalTaskId(this.externalTaskId)
                .filename(this.filename)
                .importTypeCode(this.importTypeCode)
                .importTypeId(this.importTypeId)
                .importTypeName(this.importTypeName)
                .key(this.key)
                .keywords(this.keywords)
                .masterCategoryId(this.masterCategoryId)
                .masterCategoryName(this.masterCategoryName)
                .masterSellerId(this.masterSellerId)
                .metadataId(this.metadataId)
                .originFilename(this.originFilename)
                .ownTrademark(this.ownTrademark)
                .priority(this.priority)
                .qualityCode(this.qualityCode)
                .qualityId(this.qualityId)
                .qualityName(this.qualityName)
                .rawImage(this.rawImage)
                .received(this.received)
                .sku(this.sku)
                .sourceCode(this.sourceCode)
                .sourceId(this.sourceId)
                .sourceName(this.sourceName)
                .statusCode(this.statusCode)
                .statusName(this.statusName)
                .statusId(this.statusId)
                .updatedAt(this.updatedAt)
                .updatedBy(this.updatedAt);
    }
}
