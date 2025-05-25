package ru.brombin.image_service.service.kafka;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Header;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import ru.brombin.image_service.dto.DeleteImageRequest;
import ru.brombin.image_service.facade.ImageFacade;
import ru.brombin.image_service.security.JwtAuthenticationService;
import ru.brombin.image_service.util.exception.FileStorageException;
import org.springframework.kafka.support.Acknowledgment;

@Component
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level= AccessLevel.PRIVATE, makeFinal=true)
public class KafkaImageConsumerImpl implements KafkaImageConsumer {
    ImageFacade imageFacade;
    JwtAuthenticationService jwtAuthenticationService;

    @Override
    @KafkaListener(
            topics = "${spring.kafka.consumer.image.topic}",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumeDeleteImageRequest(ConsumerRecord<String, DeleteImageRequest> record, Acknowledgment ack) {
        try {
            jwtAuthenticationService.authenticateJwt(extractJwtToken(record));
            DeleteImageRequest deleteImageRequest = record.value();
            processAndDeleteImage(deleteImageRequest);

            ack.acknowledge();
        } catch (Exception e) {
            log.error("Ошибка обработки сообщения. Offset НЕ будет зафиксирован. Kafka повторит доставку.", e);
        }
    }

    private String extractJwtToken(ConsumerRecord<String, DeleteImageRequest> record) {
        Header authHeader = record.headers().lastHeader("Authorization");
        if (authHeader == null) {
            throw new SecurityException("JWT токен отсутствует в заголовке Kafka сообщения.");
        }
        return new String(authHeader.value());
    }

    private void processAndDeleteImage(DeleteImageRequest deleteImageRequest) {
        try {
            imageFacade.deleteImagesFromKafkaRequest(deleteImageRequest);
            log.info("Successfully processed delete request for incidentId: {}", deleteImageRequest.incidentId());
        } catch (Exception e) {
            log.error("Failed to process delete request for incidentId: {}. Error: {}", deleteImageRequest.incidentId(), e.getMessage(), e);
            throw new FileStorageException("Ошибка обработки сообщения для incidentId: " + deleteImageRequest.incidentId(), e);
        }
    }
}
