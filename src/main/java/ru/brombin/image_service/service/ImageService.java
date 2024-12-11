package ru.brombin.image_service.service;

import ru.brombin.image_service.entity.Image;

import java.util.List;

public interface ImageService {
    Image saveImage(Image image);
    Image findById(Long id);
    void deleteImage(Long id);
    List<Image> findByIncidentId(Long incidentId);
}
