package ru.brombin.image_service.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;
import ru.brombin.image_service.entity.Image;
import ru.brombin.image_service.repository.ImageRepository;
import ru.brombin.image_service.util.exception.NotFoundException;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ImageServiceImpl implements ImageService {

    ImageRepository imageRepository;

    @Override
    public Image saveImage(Image image) {
        Image savedImage = imageRepository.save(image);
        log.info("Image '{}' has been successfully saved", image.getId());
        return savedImage;
    }

    @Override
    public Image findById(Long id) {
        log.info("Fetching image with ID '{}'", id);
        return imageRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Image with ID " + id + " not found"));
    }

    @Override
    public void deleteImage(Long id) {
        try {
            imageRepository.deleteById(id);
            log.info("Image with ID {} deleted successfully", id);
        } catch (EmptyResultDataAccessException e) {
            throw new NotFoundException("Image with ID " + id + " not found");
        }
    }

    @Override
    public List<Image> findByIncidentId(Long incidentId) {
        return imageRepository.findByIncidentId(incidentId);
    }
}