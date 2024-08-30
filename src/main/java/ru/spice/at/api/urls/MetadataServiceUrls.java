package ru.spice.at.api.urls;

public class MetadataServiceUrls extends BaseUrls {
    public static final String METADATA = API_V1 + "/metadata";
    public static final String METADATA_SEARCHING = API_V1 + "/metadata-searching";
    public static final String METADATA_SELECTION = API_V1 + "/metadata-selection";
    public static final String METADATA_CHECK = METADATA + "/check";
    public static final String METADATA_EXPORT = METADATA + "/export-data";
    public static final String METADATA_LINKS = METADATA + "/links";
    public static final String METADATA_UPDATES = METADATA + "/updates";
    public static final String METADATA_PRODUCT_MEDIA_EXPORT = METADATA + "/product-media-export";
    public static final String METADATA_DICTIONARIES = METADATA + "/dictionaries";
    public static final String METADATA_DICTIONARIES_USERS = METADATA_DICTIONARIES + "/users";
    public static final String METADATA_DICTIONARIES_STATUSES = METADATA_DICTIONARIES + "/statuses";
    public static final String METADATA_DICTIONARIES_QUALITIES = METADATA_DICTIONARIES + "/qualities";
    public static final String METADATA_DICTIONARIES_SOURCES = METADATA_DICTIONARIES + "/sources";
    public static final String METADATA_DICTIONARIES_CATEGORIES = METADATA_DICTIONARIES + "/categories";
    public static final String METADATA_DICTIONARIES_RETAILERS = METADATA_DICTIONARIES + "/retailers";

    private static final String METADATA_ID_PROCESSINGS = METADATA + "/%s/processings";
    public static final String METADATA_PROCESSINGS = METADATA + "/processings";

    public static String getMetadata(String id) {
        return String.format("%s/%s", METADATA, id);
    }

    public static String getMetadataProcessings(String id) {
        return String.format(METADATA_ID_PROCESSINGS, id);
    }
}
