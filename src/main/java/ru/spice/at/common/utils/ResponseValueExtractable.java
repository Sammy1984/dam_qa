package ru.spice.at.common.utils;

import io.restassured.response.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

/**
 * Интерфейс для извлечения данных из ответа сервера по JsonPath
 * Подключаем там, где требуется получать значения из ответа сервера
 */
public interface ResponseValueExtractable {
    Logger LOG = LoggerFactory.getLogger(ResponseValueExtractable.class);

    /**
     * Метод получает информацию из ответа сервера по типовому JsonPath
     *
     * @param response   ответ сервера
     * @param valuePath путь до требуемого значения
     * @return информация из ответа сервера по типовому JsonPath
     */
    default String getResponseValue(Response response, String valuePath) {
        Optional<String> value = Optional.ofNullable(response.jsonPath().getString(valuePath));
        return value.orElseGet(() -> {
            LOG.warn("Value not found by path: {}", valuePath);
            return "";
        });
    }
}
