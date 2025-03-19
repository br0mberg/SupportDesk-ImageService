package ru.brombin.image_service.config;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import ru.brombin.image_service.dto.DeleteImageRequest;
import ru.brombin.image_service.properties.KafkaProperties;

import java.util.HashMap;
import java.util.Map;

import static java.util.Map.entry;

@Configuration
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class KafkaConfig {

    KafkaProperties kafkaProperties;
    DefaultErrorHandler kafkaErrorHandler;

    @Bean
    public ConsumerFactory<String, DeleteImageRequest> consumerFactory() {
        return new DefaultKafkaConsumerFactory<>(consumerConfigs());
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, DeleteImageRequest> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, DeleteImageRequest> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        factory.setConcurrency(kafkaProperties.getConcurrency());
        factory.setCommonErrorHandler(kafkaErrorHandler);
        return factory;
    }

    @Bean
    public Map<String, Object> consumerConfigs() {
        return Map.ofEntries(
                entry(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaProperties.getBootstrapServers()),
                entry(ConsumerConfig.GROUP_ID_CONFIG, kafkaProperties.getGroupId()),
                entry(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, kafkaProperties.getAutoOffsetReset()),
                entry(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, kafkaProperties.getKeyDeserializer()),
                entry(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, kafkaProperties.getValueDeserializer()),
                entry(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, kafkaProperties.getDelegateClassName()),
                entry(JsonDeserializer.VALUE_DEFAULT_TYPE, DeleteImageRequest.class.getName()),
                entry(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, kafkaProperties.getMaxPollRecords()),
                entry(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, kafkaProperties.getSessionTimeout())
        );
    }
}