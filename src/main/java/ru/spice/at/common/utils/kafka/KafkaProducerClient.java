package ru.spice.at.common.utils.kafka;

import com.google.protobuf.GeneratedMessageV3;
import io.qameta.allure.Step;
import lombok.extern.log4j.Log4j2;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Properties;
import java.util.concurrent.ExecutionException;

import static ru.spice.at.common.utils.Assert.*;

/**
 * Кафка продюсер для отправки сообщений
 */
@Log4j2
public class KafkaProducerClient extends KafkaClient {
    private KafkaProducer<String, byte[]> kafkaProducer;
    private String topic;

    private Properties getProducerProperties() {
        Properties properties = getProperties(topic);
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, ByteArraySerializer.class);
        return properties;
    }

    /**
     * Создаем кафка продюсера (обязательно перед отправкой сообщения)
     *
     * @return KafkaProducerClient
     */
    public KafkaProducerClient createProducer(String topic) {
        log.info("Создаем кафка продюсера");
        this.topic = topic;
        try {
            kafkaProducer = new KafkaProducer<>(getProducerProperties());
        } catch (Exception e) {
            log.error("Не удалось создать кафка продюсер");
            throw new RuntimeException("Не удалось создать кафка продюсер", e);
        }
        return this;
    }

    /**
     * Посылаем сообщение в кафку типовой с проверкой ответа
     *
     * @param message - сообщение
     * @return KafkaProducerClient
     */
    public KafkaProducerClient sendCheck(GeneratedMessageV3 message) {
        RecordMetadata recordMetadata = send(message);
        assertAll(
                () -> equalsTrueParameter(recordMetadata.hasOffset(), "offset"),
                () -> notNullOrEmptyParameter(recordMetadata.timestamp(), "timestamp"),
                () -> compareParameters(topic, recordMetadata.topic(), "topic")
        );
        return this;
    }

    /**
     * Посылаем сообщение в кафку
     *
     * @param message - сообщение
     * @return ответ в виде RecordMetadata
     */
    @Step("Посылаем сообщение в кафку")
    public RecordMetadata send(GeneratedMessageV3 message) {
        log.info("Посылаем сообщение в кафку");
        if (kafkaProducer == null) {
            log.error("Продюсер не создан");
            throw new IllegalArgumentException("Продюсер не создан");
        }

        ProducerRecord<String, byte[]> producerRecord = new ProducerRecord<>(topic, message.toByteArray());
        try {
            return kafkaProducer.send(producerRecord).get();
        } catch (InterruptedException | ExecutionException e) {
            log.error("Отправить сообщение не удалось");
            throw new RuntimeException("Отправить сообщение не удалось", e);
        }
    }

    /**
     * Закрываем кафка продюсер
     */
    public void closeProducer() {
        log.info("Закрываем кафка продюсер");
        if (kafkaProducer != null) {
            kafkaProducer.close();
        }
    }
}
