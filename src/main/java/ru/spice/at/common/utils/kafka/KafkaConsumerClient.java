package ru.spice.at.common.utils.kafka;

import com.google.protobuf.GeneratedMessageV3;
import io.qameta.allure.Step;
import lombok.extern.log4j.Log4j2;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

/**
 * Кафка консумер для чтения сообщений
 */
@Log4j2
public class KafkaConsumerClient extends KafkaClient {
    private KafkaConsumer<String, byte[]> kafkaConsumer;

    private Properties getConsumerProperties(String topic) {
        Properties properties = getProperties(topic);
        properties.put(ConsumerConfig.GROUP_ID_CONFIG, CLIENT_ID);
        properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ByteArrayDeserializer.class);
        return properties;
    }

    /**
     * Создаем кафка консумера (обязательно перед чтением сообщений)
     *
     * @return KafkaConsumerClient
     */
    public KafkaConsumerClient createConsumer(String topic) {
        log.info("Создаем кафка консумер");
        try {
            kafkaConsumer = new KafkaConsumer<>(getConsumerProperties(topic));
            kafkaConsumer.subscribe(Collections.singleton(topic));
        } catch (Exception e) {
            log.error("Не удалось создать кафка консумер");
            throw new RuntimeException("Не удалось создать кафка консумер", e);
        }
        return this;
    }

    /**
     * Получаем сообщения из кафки
     *
     * @param clazz        тип сообщения
     * @param milliseconds ожидание в миллисекундах
     * @return список сообщений с соответствующим типом
     */
    @Step("Получаем сообщения типа {clazz} из кафки за {milliseconds} миллисекунд")
    public <T extends GeneratedMessageV3> List<T> poll(Class<T> clazz, int milliseconds) {
        log.info("Получаем сообщения типа {} из кафки за {} миллисекунд", clazz, milliseconds);
        ConsumerRecords<String, byte[]> records = kafkaConsumer.poll(Duration.ofMillis(milliseconds));
        List<T> result = new ArrayList<>();
        for (ConsumerRecord<String, byte[]> record : records) {
            try {
                Method method = clazz.getMethod("parseFrom", byte[].class);
                T t = (T) method.invoke(null, record.value());
                result.add(t);
            } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
                log.error("Не удалось распарсить сообщение");
                throw new RuntimeException("Не удалось распарсить сообщение", e);
            }
        }
        return result;
    }

    /**
     * Закрываем кафка консумер
     */
    public void closeConsumer() {
        log.info("Закрываем кафка консумер");
        if (kafkaConsumer != null) {
            kafkaConsumer.close();
        }
    }
}
