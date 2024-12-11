package ru.brombin.image_service.controller;

import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.brombin.image_service.dto.ImageDto;
import ru.brombin.image_service.dto.ImageDtoMessage;
import ru.brombin.image_service.entity.Image;
import ru.brombin.image_service.facade.ImageFacade;

@Validated
@RestController
@RequestMapping("/api/images")
@RequiredArgsConstructor
@FieldDefaults(level= AccessLevel.PRIVATE, makeFinal=true)
public class ImageController {
    ImageFacade imageFacade;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ANALYST', 'USER')")
    public ResponseEntity<Image> uploadImage(
            @Valid @ModelAttribute ImageDto imageDto,
            @RequestParam("file") MultipartFile file) {

        Image savedImage = imageFacade.saveImage(imageDto, file);
        return ResponseEntity.ok(savedImage);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ANALYST')")
    public ResponseEntity<InputStreamResource> downloadImage(@PathVariable Long id) {
        ImageDtoMessage imageDtoMessage = imageFacade.getImageDtoMessage(id);
        ImageDto imageDto = imageDtoMessage.imageDto();

        HttpHeaders headers = new HttpHeaders();
        headers.add("X-File-Name", imageDto.fileName());
        headers.add("X-File-Type", imageDto.type());
        headers.add("X-File-Size", String.valueOf(imageDto.size()));
        headers.add("X-Incident-Id", String.valueOf(imageDto.incidentId()));

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(imageDtoMessage.content());
    }

    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<Void> deleteImage(@PathVariable Long id) {
        imageFacade.deleteImage(id);
        return ResponseEntity.noContent().build();
    }
}
