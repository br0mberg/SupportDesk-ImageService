package ru.brombin.image_service.service.grpc;

import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Service;
import image.ImageServiceOuterClass.*;
import image.ImageServiceGrpc.*;
import image.ImageServiceOuterClass.GetImagesResponse.*;
import org.springframework.web.multipart.MultipartFile;
import ru.brombin.image_service.dto.ImageDto;
import ru.brombin.image_service.dto.ImageDtoMessage;
import ru.brombin.image_service.entity.Image;
import ru.brombin.image_service.facade.ImageFacade;
import ru.brombin.image_service.mapper.ImageMapper;
import ru.brombin.image_service.util.exception.FileRetrievalException;
import ru.brombin.image_service.util.exception.FileStorageException;
import ru.brombin.image_service.util.exception.NotFoundException;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class GrpcImageServiceImpl extends ImageServiceImplBase {
    ImageFacade imageFacade;
    ImageMapper imageMapper;

    @Override
    public void saveImage(SaveImageRequest request, StreamObserver<SaveImageResponse> responseObserver) {
        try {
            ImageDto imageDto = imageMapper.toImageDto(request);

            byte[] fileData = request.getFileData().toByteArray();
            MultipartFile file = new MockMultipartFile("file", imageDto.fileName(), imageDto.type(), fileData);

            Image savedImage = imageFacade.saveImage(imageDto, file);

            SaveImageResponse response = imageMapper.toSaveImageResponse(savedImage);

            responseObserver.onNext(response);
            responseObserver.onCompleted();
            log.info("Successfully saved image with ID: {}, URL: {}", savedImage.getId(), savedImage.getUrl());
        } catch (FileStorageException e) {
            log.error("File storage error occurred while saving image: {}", e.getMessage(), e);
            responseObserver.onError(Status.INTERNAL
                    .withDescription("Failed to store the image")
                    .withCause(e)
                    .asRuntimeException());
        } catch (Exception e) {
            log.error("Unexpected error occurred while saving image: {}", e.getMessage(), e);
            responseObserver.onError(Status.INTERNAL
                    .withDescription("Unexpected error occurred while saving the image")
                    .withCause(e)
                    .asRuntimeException());
        }
    }

    @Override
    public void getImages(GetImagesRequest request, StreamObserver<GetImagesResponse> responseObserver) {
        Long incidentId = request.getIncidentId();
        try {
            List<ImageDtoMessage> imageMessages = imageFacade.getImageDtoMessagesByIncidentId(incidentId);

            Builder responseBuilder = GetImagesResponse.newBuilder();
            for (ImageDtoMessage imageMessage : imageMessages) {
                ImageData imageData = imageMapper.toImageData(imageMessage);
                responseBuilder.addImages(imageData);
            }

            GetImagesResponse response = responseBuilder.build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
            log.info("Successfully retrieved images for incident ID: {}", incidentId);
        } catch (NotFoundException e) {
            log.error("No images found for incident ID: {}", incidentId, e);
            responseObserver.onError(Status.NOT_FOUND
                    .withDescription("No images found for the given incident ID")
                    .withCause(e)
                    .asRuntimeException());
        } catch (FileRetrievalException e) {
            log.error("File retrieval error occurred while processing images for incident ID: {}", incidentId, e);
            responseObserver.onError(Status.INTERNAL
                    .withDescription("Error occurred while retrieving file contents for images")
                    .withCause(e)
                    .asRuntimeException());
        } catch (Exception e) {
            log.error("Unexpected error occurred while retrieving images for incident ID: {}", incidentId, e);
            responseObserver.onError(Status.INTERNAL
                    .withDescription("Unexpected error occurred while retrieving images")
                    .withCause(e)
                    .asRuntimeException());
        }
    }
}
