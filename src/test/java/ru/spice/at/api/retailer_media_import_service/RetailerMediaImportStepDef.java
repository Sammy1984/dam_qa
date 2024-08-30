package ru.spice.at.api.retailer_media_import_service;

import com.google.protobuf.GeneratedMessageV3;
import io.qameta.allure.Step;
import lombok.Data;
import lombok.experimental.Accessors;
import ru.spice.at.common.base_test.AbstractApiStepDef;
import ru.spice.at.common.dto.dam.grpc.OfferProcessed;
import ru.spice.at.common.dto.dam.grpc.OffersRecieved;
import ru.spice.at.common.emuns.ApiServices;
import ru.spice.at.common.emuns.dam.OfferProcessStatusEnum;
import ru.spice.at.common.emuns.dam.OfferProcessTypeEnum;
import ru.spice.at.common.utils.kafka.KafkaConsumerClient;
import ru.spice.at.common.utils.kafka.KafkaProducerClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static ru.spice.at.common.constants.Topics.CONTENT_OFFERS;
import static ru.spice.at.common.constants.Topics.CONTENT_OFFER_PROCESSED;
import static ru.spice.at.common.utils.kafka.KafkaHelper.jsonToProtoParse;
import static ru.spice.at.common.utils.kafka.KafkaHelper.protoToJsonParse;

public class RetailerMediaImportStepDef extends AbstractApiStepDef {

    public RetailerMediaImportStepDef() {
        super(ApiServices.RETAILER_MEDIA_IMPORT_SERVICE);
    }

    @Step("Получаем объект для импорта медиа")
    public String buildImportOneMediaRequest(RetailerMediaImportSettings.ImportParameters importParameters) {
        Map<String, Object> newValues = new HashMap<>() {{
            put("name", importParameters.externalOfferName());
            put("externalOfferId", importParameters.externalOfferId());
            put("createdAt", importParameters.createdAt());
            put("updatedAt", importParameters.updatedAt());
            put("seller.masterSellerId", importParameters.masterSellerId());
            put("media[0].name", importParameters.mediaName());
            put("media[0].url", importParameters.mediaUrl());
            put("media[0].priority", importParameters.mediaPriority());
        }};
        return getReqJson("offersReceived", newValues);
    }

    public String buildImportSomeMediaRequest(List<String> names) {
        return buildImportSomeMediaRequest(names, null);
    }

    public String buildImportSomeMediaWithS3Url(List<String> names, RetailerMediaImportSettings.ImportParameters importParameters) {
        Map<String, Object> newValues = new HashMap<>() {{
            put("media[1].url", importParameters.mediaUrl());
            put("externalOfferId", importParameters.externalOfferId());
            put("seller.masterSellerId", importParameters.masterSellerId());
        }};
        return buildImportSomeMediaRequest(names, newValues);
    }

    public String buildImportSomeMediaWithParamsRequest(List<String> names, RetailerMediaImportSettings.ImportParameters importParameters) {
        Map<String, Object> newValues = new HashMap<>() {{
            put("name", importParameters.externalOfferName());
            put("externalOfferId", importParameters.externalOfferId());
            put("createdAt", importParameters.createdAt());
            put("updatedAt", importParameters.updatedAt());
            put("seller.masterSellerId", importParameters.masterSellerId());
        }};
        return buildImportSomeMediaRequest(names, newValues);
    }

    @Step("Получаем объект для импорта нескольких медиа")
    private String buildImportSomeMediaRequest(List<String> names, Map<String, Object> values) {
        Map<String, Object> newValues = new HashMap<>() {{
            for (int i = 0; i < names.size(); i++) {
                put(String.format("media[%d].name", i), names.get(i));
                if (values != null) putAll(values);
            }
        }};
        return getReqJson("offersReceivedSomeImage", newValues);
    }

    @Step("Получаем объект для импорта без файлов")
    public String buildImportNoMediaRequest() {
        return getReqJson("offersReceivedNoImage");
    }

    @Step("Получаем объект для импорта метадаты")
    public String buildImportMetadataRequest(Integer externalOfferId, Integer masterSellerId,
                                             RetailerMediaImportSettings.ImportMetadata importMetadata) {
        Map<String, Object> newValues = new HashMap<>() {{
            put("external_offer_id", externalOfferId);
            put("master_seller_id", masterSellerId);
            put("external_task_id.data", importMetadata.taskIdForDam());
            put("sku.data", importMetadata.sku());
            put("is_own_trademark", importMetadata.isOwnTrademark());
            put("draft_done", importMetadata.draftDone());
        }};
        return getReqJson("offerUpdate", newValues);
    }

    public String buildImportModerationMetadataRequest(Integer externalOfferId, Integer masterSellerId, OfferProcessTypeEnum offerProcessType,
                                                       RetailerMediaImportSettings.ImportMetadata importMetadata) {
        return buildImportModerationMetadataRequest(externalOfferId, masterSellerId, offerProcessType, OfferProcessStatusEnum.BINDED, null, importMetadata);
    }

    public String buildImportModerationMetadataRequest(Integer externalOfferId, Integer masterSellerId, OfferProcessTypeEnum offerProcessType,
                                                       String readyForRetouchAt, RetailerMediaImportSettings.ImportMetadata importMetadata) {
        return buildImportModerationMetadataRequest(externalOfferId, masterSellerId, offerProcessType, OfferProcessStatusEnum.BINDED, readyForRetouchAt, importMetadata);
    }

    @Step("Получаем объект для импорта по задаче MODERATION_PRODUCTS")
    public String buildImportModerationMetadataRequest(Integer externalOfferId, Integer masterSellerId, OfferProcessTypeEnum offerProcessType,
                                                       OfferProcessStatusEnum offerProcessStatus, String readyForRetouchAt,
                                                       RetailerMediaImportSettings.ImportMetadata importMetadata) {
        Map<String, Object> newValues = new HashMap<>() {{
            put("external_offer_id", externalOfferId);
            put("master_seller_id", masterSellerId);
            put("process_type", offerProcessType.toString());
            put("status", offerProcessStatus.toString());
            if (readyForRetouchAt != null)
                put("updated_at", readyForRetouchAt);
            put("master_category_id", importMetadata.masterCategoryId());
            put("task_id_for_dam.data", importMetadata.taskIdForDam());
            put("sku.data", importMetadata.sku());
            put("is_own_trademark", importMetadata.isOwnTrademark());
        }};
        return getReqJson("offerProcessedModeration", newValues);
    }

    public String buildImportCreateMetadataRequest(String metadataId, OfferProcessTypeEnum offerProcessType,
                                                   RetailerMediaImportSettings.ImportMetadata importMetadata) {
        return buildImportCreateMetadataRequest(metadataId, UUID.randomUUID().toString(), offerProcessType, importMetadata);
    }

    public String buildImportCreateMetadataRequest(String firstMetadataId, String secondMetadataId, OfferProcessTypeEnum offerProcessType,
                                                   RetailerMediaImportSettings.ImportMetadata importMetadata) {
        return buildImportCreateMetadataRequest(
                new Media().mediaId(firstMetadataId), new Media().mediaId(secondMetadataId), offerProcessType, importMetadata);
    }

    public String buildImportCreateMetadataRequest(Media firstMedia, OfferProcessTypeEnum offerProcessType,
                                                   RetailerMediaImportSettings.ImportMetadata importMetadata) {
        return buildImportCreateMetadataRequest(
                firstMedia, new Media().mediaId(UUID.randomUUID().toString()), offerProcessType, OfferProcessStatusEnum.BINDED, importMetadata);
    }

    public String buildImportCreateMetadataRequest(Media firstMedia, Media secondMedia, OfferProcessTypeEnum offerProcessType,
                                                   RetailerMediaImportSettings.ImportMetadata importMetadata) {
        return buildImportCreateMetadataRequest(firstMedia, secondMedia, offerProcessType, OfferProcessStatusEnum.BINDED, importMetadata);
    }

    public String buildImportCreateMetadataRequest(Media firstMedia, Media secondMedia, OfferProcessTypeEnum offerProcessType, OfferProcessStatusEnum offerProcessStatus,
                                                   RetailerMediaImportSettings.ImportMetadata importMetadata) {
        return buildImportCreateMetadataRequest(firstMedia, secondMedia, null, null, offerProcessType, offerProcessStatus, importMetadata);
    }

    @Step("Получаем объект для импорта по задаче CREATE_PRODUCTS")
    public String buildImportCreateMetadataRequest(Media firstMedia, Media secondMedia, Integer externalOfferId, Integer masterSellerId,
                                                   OfferProcessTypeEnum offerProcessType,
                                                   OfferProcessStatusEnum offerProcessStatus,
                                                   RetailerMediaImportSettings.ImportMetadata importMetadata) {
        Map<String, Object> newValues = new HashMap<>() {{
            if (externalOfferId != null)
                put("external_offer_id", externalOfferId);
            if (masterSellerId != null)
                put("master_seller_id", masterSellerId);
            put("process_type", offerProcessType.toString());
            put("status", offerProcessStatus.toString());
            put("master_category_id", importMetadata.masterCategoryId());
            put("task_id_for_dam.data", importMetadata.taskIdForDam());
            put("sku.data", importMetadata.sku());
            put("is_own_trademark", importMetadata.isOwnTrademark());
            put("media[0].media_id", firstMedia.mediaId());
            if (firstMedia.priority() != null)
                put("media[0].priority", firstMedia.priority());
            if (firstMedia.isDeleted() != null)
                put("media[0].is_deleted", firstMedia.isDeleted());
            put("media[1].media_id", secondMedia.mediaId());
            if (secondMedia.priority() != null)
                put("media[1].priority", secondMedia.priority());
            if (secondMedia.isDeleted() != null)
                put("media[1].is_deleted", secondMedia.isDeleted());
        }};
        return getReqJson("offerProcessedCreate", newValues);
    }

    @Deprecated
    @Step("Получаем объект для импорта по задаче CREATE_PRODUCTS (CANCEL_TASK, REMOVE_FROM_TASK)")
    public String buildImportCreateCancelMetadataRequest(Integer externalOfferId, Integer masterSellerId, OfferProcessTypeEnum offerProcessType,
                                                         RetailerMediaImportSettings.ImportMetadata importMetadata) {
        Map<String, Object> newValues = new HashMap<>() {{
            put("external_offer_id", externalOfferId);
            put("master_seller_id", masterSellerId);
            put("process_type", offerProcessType.toString());
            put("master_category_id", importMetadata.masterCategoryId());
            put("is_own_trademark", importMetadata.isOwnTrademark());
        }};
        return getReqJson("offerProcessedCreateCancel", newValues);
    }

    @Step("Получаем объект для импорта по пустой задаче (CONTENT_AUTOGENARATION)")
    public String buildImportModerationNullExternalTaskMetadataRequest(Integer externalOfferId, Integer masterSellerId, OfferProcessTypeEnum offerProcessType,
                                                                       RetailerMediaImportSettings.ImportMetadata importMetadata) {
        Map<String, Object> newValues = new HashMap<>() {{
            put("external_offer_id", externalOfferId);
            put("master_seller_id", masterSellerId);
            put("process_type", offerProcessType.toString());
            put("master_category_id", importMetadata.masterCategoryId());
            put("sku.data", importMetadata.sku());
            put("is_own_trademark", importMetadata.isOwnTrademark());
        }};
        return getReqJson("offerProcessedNullExternalTask", newValues);
    }

    @Step("Загружаем сообщение с медиафайлами")
    public void sendMediaFile(String json) {
        GeneratedMessageV3 message = jsonToProtoParse(json, OffersRecieved.Offer.newBuilder());

        KafkaProducerClient producer = new KafkaProducerClient();
        producer.createProducer(CONTENT_OFFERS).sendCheck(message).closeProducer();
    }

    @Step("Просматриваем сообщения сообщение с медиафайлами")
    public String pollMediaFileInfo() {
        KafkaConsumerClient consumer = new KafkaConsumerClient();
        List<OffersRecieved.Offer> offers = consumer.createConsumer(CONTENT_OFFERS).poll(OffersRecieved.Offer.class, 10000);
        consumer.closeConsumer();
        return protoToJsonParse(offers.get(0));
    }

    @Step("Загружаем сообщение с метадатой")
    public void sendMetadata(String json) {
        GeneratedMessageV3 message = jsonToProtoParse(json, OfferProcessed.OfferUpdate.newBuilder());

        KafkaProducerClient producer = new KafkaProducerClient();
        producer.createProducer(CONTENT_OFFER_PROCESSED).sendCheck(message).closeProducer();
    }

    @Data
    @Accessors(chain = true, fluent = true)
    public static class Media {
        private String mediaId;
        private Integer priority;
        private Boolean isDeleted;
    }
}