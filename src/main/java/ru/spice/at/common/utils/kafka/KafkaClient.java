package ru.spice.at.common.utils.kafka;

import lombok.extern.log4j.Log4j2;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.config.SaslConfigs;
import ru.spice.at.common.StandProperties;
import ru.spice.at.common.dto.KafkaCredentials;
import ru.spice.at.common.utils.CipherHelper;

import java.util.Properties;

/**
 * Базовый класс для кафка клиента
 */
@Log4j2
abstract class KafkaClient {
    private static final String SASL_CONFIG = "org.apache.kafka.common.security.scram.ScramLoginModule required username=\"%s\" password=\"%s\";";
    static final String CLIENT_ID = "qa-automation";

    static StandProperties standProperties = new StandProperties();

    Properties getProperties(String topic) {
        KafkaCredentials credentials = standProperties.getKafkaConnect().credentials().stream().
                filter(k -> k.topic().equals(topic)).findFirst().
                orElseThrow(() -> new RuntimeException(String.format("Топик '%s' не найден", topic)));

        Properties properties = new Properties();
        properties.put(ProducerConfig.CLIENT_ID_CONFIG, CLIENT_ID);
        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, standProperties.getKafkaConnect().bootstrapServer());

        properties.put(SaslConfigs.SASL_MECHANISM, "SCRAM-SHA-512");
        properties.put("security.protocol", "SASL_PLAINTEXT");
        properties.put("sasl.jaas.config", String.format(SASL_CONFIG, credentials.login(), CipherHelper.decrypt(credentials.password())));
        return properties;
    }
}
