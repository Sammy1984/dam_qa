package ru.spice.at.api.retailer_media_import_service;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true, fluent = true)
public class RetailerMediaImportSettings {

    private ImportParameters importParameters;
    private String pdfMediaUrl;
    private String s3MediaUrl;
    private String anotherMediaUrl;
    private ImportMetadata importMetadata;
    private List<String> normalizationKeywords;
    private List<String> keywordsIsDeleted;
    private String unknownCategory;
    private String readyForRetouchAt;

    @Data
    @Accessors(chain = true, fluent = true)
    public static class ImportParameters {
        private String externalOfferName;
        private String externalOfferId;
        private String createdAt;
        private String updatedAt;
        private Integer masterSellerId;
        private String mediaName;
        private String mediaUrl;
        private Integer mediaPriority;

        @Override
        public ImportParameters clone() {
            return new ImportParameters().
                    externalOfferName(this.externalOfferName).
                    externalOfferId(this.externalOfferId).
                    createdAt(this.createdAt).
                    updatedAt(this.updatedAt).
                    masterSellerId(this.masterSellerId).
                    mediaName(this.mediaName).
                    mediaUrl(this.mediaUrl).
                    mediaPriority(this.mediaPriority);
        }
    }

    @Data
    @Accessors(chain = true, fluent = true)
    public static class ImportMetadata {
        private Integer taskIdForDam;
        private Integer sku;
        private Integer masterCategoryId;
        private Boolean isOwnTrademark;
        private Boolean draftDone;

        @Override
        public ImportMetadata clone() {
            return new ImportMetadata().
                    taskIdForDam(this.taskIdForDam).
                    sku(this.sku).
                    masterCategoryId(this.masterCategoryId).
                    isOwnTrademark(this.isOwnTrademark).
                    draftDone(this.draftDone);
        }
    }
}
