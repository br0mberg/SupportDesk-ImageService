package ru.brombin.image_service.facade;

import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import ru.brombin.image_service.dto.DeleteImageRequest;
import ru.brombin.image_service.dto.ImageDto;
import ru.brombin.image_service.dto.ImageDtoMessage;
import ru.brombin.image_service.entity.Image;
import ru.brombin.image_service.mapper.ImageMapper;
import ru.brombin.image_service.service.ImageService;
import ru.brombin.image_service.service.minio.MinioService;
import ru.brombin.image_service.util.exception.FileRetrievalException;
import ru.brombin.image_service.util.exception.NotFoundException;

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.UUID;

import static lombok.AccessLevel.PRIVATE;

@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
@FieldDefaults(level=PRIVATE, makeFinal=true)
public class ImageFacadeImpl implements ImageFacade {
    ImageService imageService;
    MinioService minioService;
    ImageMapper imageMapper;

    @Override
    @Transactional
    public Image saveImage(ImageDto imageDto, MultipartFile file) {
        Image imageToSave = imageMapper.toImage(imageDto);
        String uniqueFileName = "image_" + imageToSave.getFileName() + UUID.randomUUID();
        imageToSave.setFileName(uniqueFileName);

        String url = minioService.uploadFile(file, uniqueFileName);
        imageToSave.setUrl(url);

        return imageService.saveImage(imageToSave);
    }

    @Override
    @Transactional(readOnly = true)
    public ImageDtoMessage getImageDtoMessage(Long id) {
        Image image = imageService.findById(id);
        ImageDto imageDto = imageMapper.toImageDto(image);

        return new ImageDtoMessage(imageDto, retrieveFileContent(image.getFileName(), id));
    }

    @Override
    @Transactional
    public List<ImageDtoMessage> getImageDtoMessagesByIncidentId(Long incidentId) {
        List<Image> images = imageService.findByIncidentId(incidentId);

        if (images.isEmpty()) {
            throw new NotFoundException("No images found for incident ID: " + incidentId);
        }

        return images.stream()
                .map(image -> {
                    ImageDto imageDto = imageMapper.toImageDto(image);
                    return new ImageDtoMessage(imageDto, retrieveFileContent(image.getFileName(), incidentId));
                })
                .toList();
    }

    private InputStreamResource retrieveFileContent(String fileName, Long id) {
        try {
            byte[] fileContent = minioService.getFileContentAsByteArray(fileName);
            return new InputStreamResource(new ByteArrayInputStream(fileContent));
        } catch (Exception e) {
            log.error("Failed to retrieve file content for image id: {}. Error: {}", id, e.getMessage(), e);
            throw new FileRetrievalException("Failed to retrieve file content for image file: " + fileName, e);
        }
    }

    @Override
    @Transactional
    public void deleteImagesFromKafkaRequest(DeleteImageRequest deleteImageRequest) {
        Long incidentId = deleteImageRequest.incidentId();

        List<Image> images = imageService.findByIncidentId(incidentId);
        if (images.isEmpty()) {
            log.warn("No images found for incidentId: {}", incidentId);
            return;
        }

        for (Image image : images) {
            deleteImageInternal(image, incidentId);
        }
    }

    @Override
    @Transactional
    public void deleteImage(Long id) {
        Image image = imageService.findById(id);
        deleteImageInternal(image, null);
    }

    private void deleteImageInternal(Image image, Long incidentId) {
        minioService.deleteFile(image.getFileName());
        imageService.deleteImage(image.getId());
        log.info("Successfully deleted image with id: {}{}",
                image.getId(),
                incidentId != null ? " for incidentId: " + incidentId : "");
    }
}
