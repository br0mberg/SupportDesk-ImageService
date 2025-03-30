package ru.brombin.image_service.properties;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@FieldDefaults(level = AccessLevel.PRIVATE)
@ConfigurationProperties(prefix = "spring.kafka.consumer")
public class KafkaCustomProperties {
    String bootstrapServers;
    String groupId;
    String autoOffsetReset;
    String keyDeserializer;
    String valueDeserializer;
    String valueDeserializerDelegate;
    Integer maxPollRecords;
    Integer sessionTimeoutMs;
    Integer concurrency;
    Integer pollTimeout;
    String imageTopic;
}
