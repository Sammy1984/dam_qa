package ru.spice.at.common.utils;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import io.restassured.response.Response;
import lombok.extern.log4j.Log4j2;
import net.minidev.json.JSONObject;
import org.apache.commons.lang3.text.translate.UnicodeUnescaper;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;

/**
 * Утилитный класс для работы с json-файлами.
 */
@Log4j2
public final class JsonHelper {
    private static final ObjectMapper objectMapper = setMapper();
    private static final GsonBuilder gsonBuilder = new GsonBuilder().registerTypeAdapter(Instant.class,
            (JsonDeserializer<Instant>) (jsonElement, type, jsonDeserializationContext) ->
                    Instant.parse(jsonElement.getAsString()));
    private static final Gson gson = gsonBuilder.create();

    private JsonHelper() {
        throw new IllegalAccessError("Это утилитный класс. Создание экземпляра не требуется.");
    }

    private static ObjectMapper setMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        return objectMapper;
    }

    public static <T> T jsonParse(Response response, Class<T> typeOfT) {
        return jsonParse(response.asString(), typeOfT);
    }

    public static <T> T jsonParse(String json, Class<T> typeOfT) {
        return gson.fromJson(json, typeOfT);
    }

    /**
     * Устанавливает значение в переданном json и возвращает измененный json в виде строки.
     *
     * @param json      json, в котором нужно установить значение
     * @param pathToKey путь до значения
     * @param newValue  новое значение
     * @return измененный json в виде строки
     */
    public static String setValueInJson(String json, String pathToKey, Object newValue) {
        return JsonPath.parse(json, Configuration.defaultConfiguration()).set(pathToKey, newValue).jsonString();
    }

    /**
     * Устанавливает значения в переданном json и возвращает измененный json в виде строки.
     *
     * @param json      json, в котором нужно установить новые значения
     * @param newValues map вида "путь до значения, новое значение"
     * @return измененный json в виде строки
     */
    public static String setValuesInJson(String json, Map<String, ?> newValues) {
        final DocumentContext context = JsonPath.parse(json, Configuration.defaultConfiguration());
        newValues.forEach(context::set);
        return new UnicodeUnescaper().translate(context.jsonString());
    }

    /**
     * Добавляет в переданный json новые поля и возвращает измененный json в виде строки.
     *
     * @param json      исходный json
     * @param path      путь, куда нужно вставить новые поля
     * @param newValues map с новыми полями вида "название поля, значение"
     * @return измененный json в виде строки
     */
    public static String putValuesInJson(String json, String path, Map<String, ?> newValues) {
        final DocumentContext context = JsonPath.parse(json, Configuration.defaultConfiguration());
        newValues.forEach((pathToKey, newValue) -> context.put(path, pathToKey, newValue));
        return context.jsonString();
    }

    /**
     * Возвращает значение по jsonPath.
     *
     * @param response    ответ
     * @param path путь до ключа
     * @return значение параметра
     */
    public static <T> T getValueFromResponse(Response response, String path) {
        return JsonPath.parse(response.asString()).read(path);
    }

    /**
     * Формирует json из значения по jsonPath и возвращает его.
     *
     * @param json     входной json
     * @param jsonPath путь до ключа
     * @return новый json
     */
    public static Object getBodyAsJsonString(String json, String jsonPath) {
        return new JSONObject(JsonPath.parse(json).read(jsonPath)); // toJSONString()
    }

    /**
     * Возвращает Map из переданного json по пути
     * @param json из которого необходимо получить Map
     * @return Map из json строки
     */
    public static Map<String, Object> getMapFromJsonString(String json) {
        return JsonPath.parse(json).json();
    }

    /**
     * Возвращает Map из переданного json по пути
     * @param json из которого необходимо получить Map
     * @param jsonPath откуда получить Map
     * @return Map из json строки
     */
    public static Map<String, Object> getMapFromJsonString(String json, String jsonPath) {
        return JsonPath.parse(json).read(jsonPath);
    }

    /**
     * Преобразует {@link Map} в json-style {@link String}.
     *
     * @param json {@link Map}, которую нужно преобразовать в json-style строку
     * @return json-style {@link String}
     */
    public static String getJsonStringFromMap(Map<String, ?> json) {
        try {
            return objectMapper.writeValueAsString(json);
        } catch (JsonProcessingException e) {
            log.error("Can't convert map {} to JSON-string", json);
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * Формирует json-строку из любого объекта и возвращает её.
     */
    public static String getDtoAsString(Object dto) {
        try {
            return objectMapper.writeValueAsString(dto);
        } catch (JsonProcessingException e) {
            log.info("Ошибка преобразования JSON", e);
            throw new RuntimeException("Не удалось преобразовать json в String");
        }
    }

    /**
     * Формирует объект типа T из json-строки и возвращает его.
     */
    public static <T> T readValue(String json, TypeReference<T> dtoType) {
        try {
            return objectMapper.readValue(json, dtoType);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Формирует JsonNode из строки и возвращает её.
     * Удобно использовать для сравнения двух json
     */
    public static JsonNode readTree(String json) {
        try {
            return objectMapper.readTree(json);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
