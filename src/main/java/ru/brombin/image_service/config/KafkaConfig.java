package ru.brombin.image_service.config;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import ru.brombin.image_service.dto.DeleteImageRequest;
import ru.brombin.image_service.properties.KafkaCustomProperties;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Configuration
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class KafkaConfig {
    KafkaCustomProperties kafkaCustomProperties;
    DefaultErrorHandler kafkaErrorHandler;

    @Bean
    public ConsumerFactory<String, DeleteImageRequest> consumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaCustomProperties.getBootstrapServers());
        props.put(ConsumerConfig.GROUP_ID_CONFIG, kafkaCustomProperties.getGroupId());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, kafkaCustomProperties.getAutoOffsetReset());

        // Указываем ErrorHandlingDeserializer как основной десериализатор
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);

        // Указываем делегаты для ErrorHandlingDeserializer
        props.put(ErrorHandlingDeserializer.KEY_DESERIALIZER_CLASS, StringDeserializer.class);
        props.put(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, JsonDeserializer.class);

        // Настройки JsonDeserializer
        props.put(JsonDeserializer.VALUE_DEFAULT_TYPE, DeleteImageRequest.class.getName());
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "*");

        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, kafkaCustomProperties.getMaxPollRecords());

        if (kafkaCustomProperties.getSessionTimeoutMs() != null) {
            props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, kafkaCustomProperties.getSessionTimeoutMs());
        }

        // Убираем ручное создание JsonDeserializer
        return new DefaultKafkaConsumerFactory<>(props);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, DeleteImageRequest> kafkaListenerContainerFactory() {
        log.info("Creating ConcurrentKafkaListenerContainerFactory");
        ConcurrentKafkaListenerContainerFactory<String, DeleteImageRequest> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        factory.setConcurrency(kafkaCustomProperties.getConcurrency());
        factory.setCommonErrorHandler(kafkaErrorHandler);
        log.info("KafkaListenerContainerFactory created successfully");
        return factory;
    }
}
