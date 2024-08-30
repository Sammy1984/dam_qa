package ru.spice.at.api.urls;

public class ImportServiceUrls extends BaseUrls {
    public static final String IMPORT = API_V1 + "/import";
    public static final String IMPORTS = API_V1 + "/imports";
    public static final String IMPORT_OPENING = API_V1 + "/import-opening";
    public static final String VALIDATIONS = API_V1 + "/validations";
    public static final String VALIDATIONS_DOWNLOAD = VALIDATIONS + "/download";
    public static final String BUSINESS_METADATA = API_V1 + "/business-metadata";
    public static final String EXTERNAL_IMPORT = API_V1 + "/external-import";

    public static String getImportsItem(String id) {
        return String.format("%s/%s", IMPORTS, id);
    }
}
