package ru.brombin.image_service.service.kafka;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import ru.brombin.image_service.dto.DeleteImageRequest;

public interface KafkaImageConsumer {
    void consumeDeleteImageRequest(ConsumerRecord<String, DeleteImageRequest> record);
}
