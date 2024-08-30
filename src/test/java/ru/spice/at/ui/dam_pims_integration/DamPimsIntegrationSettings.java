package ru.spice.at.ui.dam_pims_integration;

import lombok.Data;
import lombok.experimental.Accessors;
import ru.spice.at.api.retailer_media_import_service.RetailerMediaImportSettings;

import java.util.List;

@Data
@Accessors(chain = true, fluent = true)
public class DamPimsIntegrationSettings {
    private String pimsUrl;
    private String pimsLogin;
    private String pimsEncryptPassword;
    private String taskEndpoint;
    private String taskListEndpoint;
    private String taskId;
    private List<Offer> offers;
    private List<String> mediaUrls;
    private RetailerMediaImportSettings.ImportParameters importParameters;
    private Product createProduct;

    @Data
    @Accessors(chain = true, fluent = true)
    public static class Offer {
        private String offerId;
        private String externalOfferId;
        private String masterSellerId;
        private String sku;
        private Boolean isOwnTrademark;
        private Boolean draftDone;
    }

    @Data
    @Accessors(chain = true, fluent = true)
    public static class Product {
        private String taskType;
        private String taskSubtype;
        private String category;
        private String weight;
        private String status;
        private String importType;
        private String createdBy;
    }
}