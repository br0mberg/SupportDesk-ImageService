package ru.brombin.image_service.facade;

import org.springframework.web.multipart.MultipartFile;
import ru.brombin.image_service.dto.DeleteImageRequest;
import ru.brombin.image_service.dto.ImageDto;
import ru.brombin.image_service.dto.ImageDtoMessage;
import ru.brombin.image_service.entity.Image;

import java.util.List;

public interface ImageFacade {
    Image saveImage(ImageDto imageDto, MultipartFile file);
    ImageDtoMessage getImageDtoMessage(Long id);
    List<ImageDtoMessage> getImageDtoMessagesByIncidentId(Long incidentId);
    void deleteImagesFromKafkaRequest(DeleteImageRequest deleteImageRequest);
    void deleteImage(Long id);
}
