package ru.spice.at.api.import_service;

import lombok.Data;
import lombok.experimental.Accessors;
import ru.spice.at.api.dto.request.import_service.RequestXmpMetadata;
import ru.spice.at.api.dto.response.import_service.InvalidParamsItem;
import ru.spice.at.common.dto.dam.ImageData;

import java.util.List;

@Data
@Accessors(chain = true, fluent = true)
public class ImportServiceSettings {
      private List<ImageData> validImages;
      private List<ImageData> invalidNameImages;
      private List<ErrorType> errorTypes;
      private List<List<String>> firstErrorDownloadTable;
      private List<List<String>> secondErrorDownloadTable;
      private String errorInvalidType;
      private String errorInvalidTypeReason;
      private String errorNotFound;
      private String errorNotFoundReason;
      private InvalidParamsItem invalidEmptyBodyStringParam;
      private InvalidParamsItem invalidBodyStringParam;
      private List<ImageData> badQualityImages;
      private List<ImageData> goodQualityImages;
      private List<ImageData> toRevisionQualityImages;
      private String baseExternalUrl;
      private String externalToken;
      private RequestXmpMetadata xmpMetadata;
      private Object[] retouchFiles;
      private List<String> retouchKeywords;

      @Data
      @Accessors(chain = true, fluent = true)
      public static class ErrorType {
            private String type;
            private String description;
      }
}
