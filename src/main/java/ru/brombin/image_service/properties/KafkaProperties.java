package ru.brombin.image_service.properties;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@FieldDefaults(level= AccessLevel.PRIVATE)
@ConfigurationProperties(prefix = "spring.kafka.consumer")
public class KafkaProperties {
    String bootstrapServers;
    String groupId;
    String autoOffsetReset;
    String keyDeserializer;
    String valueDeserializer;
    Integer maxPollRecords;
    Integer sessionTimeout;
    Integer concurrency;
    String delegateClassName;
}
