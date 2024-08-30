package ru.spice.at.common.utils.kafka;

import com.google.protobuf.GeneratedMessageV3;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.util.JsonFormat;
import lombok.extern.log4j.Log4j2;

/**
 * Утилитный класс для работы с кафкой.
 */
@Log4j2
public final class KafkaHelper {

    /**
     * Формирует кафка сообщение из json и прото объекта.
     *
     * @param json    входной json
     * @param builder билдер proto объекта
     * @return готовое кафка сообщение
     */
    public static GeneratedMessageV3 jsonToProtoParse(String json, GeneratedMessageV3.Builder builder) {
        log.info("Формируем кафка сообщение из json и proto объекта");
        try {
            JsonFormat.parser().ignoringUnknownFields().merge(json, builder);
        } catch (InvalidProtocolBufferException e) {
            log.error("Не удалось сформировать кафка сообщение из json");
            throw new RuntimeException("Не удалось сформировать кафка сообщение из json", e);
        }
        return (GeneratedMessageV3) builder.build();
    }

    /**
     * Формируем json-строку из кафка сообщения.
     *
     * @param message кафка сообщение
     * @return json-строка
     */
    public static String protoToJsonParse(GeneratedMessageV3 message) {
        log.info("Формируем json-строку из кафка сообщения");
        try {
            return JsonFormat.printer().print(message);
        } catch (InvalidProtocolBufferException e) {
            log.error("Не удалось сформировать json-строку из кафка сообщения");
            throw new RuntimeException("Не удалось сформировать json-строку из кафка сообщения", e);
        }
    }
}
