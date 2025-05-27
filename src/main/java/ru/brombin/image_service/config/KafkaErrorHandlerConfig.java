package ru.brombin.image_service.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DefaultErrorHandler;
import ru.brombin.image_service.dto.DeleteImageRequest;

@Configuration
@Slf4j
public class KafkaErrorHandlerConfig {

    @Bean
    public DefaultErrorHandler kafkaErrorHandler(KafkaTemplate<String, DeleteImageRequest> kafkaTemplate) {
        return new DefaultErrorHandler((record, exception) -> {
            log.error("Ошибка обработки сообщения из Kafka: {}", record.value(), exception);

            String key = record.key() != null ? (String) record.key() : "unknown-key";
            DeleteImageRequest value = (DeleteImageRequest) record.value();

            kafkaTemplate.send("iincident.media.command.image.delete-by-incident-id.dlt", key, value);
        });
    }
}
