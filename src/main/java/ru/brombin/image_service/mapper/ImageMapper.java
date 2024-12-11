package ru.brombin.image_service.mapper;

import com.google.protobuf.ByteString;
import image.ImageServiceOuterClass.ImageData;
import image.ImageServiceOuterClass.SaveImageResponse;
import image.ImageServiceOuterClass.SaveImageRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.core.io.InputStreamResource;
import ru.brombin.image_service.dto.ImageDto;
import ru.brombin.image_service.dto.ImageDtoMessage;
import ru.brombin.image_service.entity.Image;

import java.io.IOException;
import java.io.InputStream;

@Mapper(componentModel = "spring")
public interface ImageMapper {
    ImageDto toImageDto(Image image);
    ImageDto toImageDto(SaveImageRequest request);
    Image toImage(ImageDto imageDto);

    @Mapping(target = "imageId", source = "id")
    @Mapping(target = "url", source = "url")
    SaveImageResponse toSaveImageResponse(Image image);

    @Mapping(target = "incidentId", source = "imageMessage.imageDto.incidentId")
    @Mapping(target = "fileName", source = "imageMessage.imageDto.fileName")
    @Mapping(target = "fileData", source = "imageMessage.content", qualifiedByName = "toByteString")
    @Mapping(target = "type", source = "imageMessage.imageDto.type")
    @Mapping(target = "size", source = "imageMessage.imageDto.size")
    ImageData toImageData(ImageDtoMessage imageMessage);

    @Named("toByteString")
    default ByteString toByteString(InputStreamResource content) {
        try (InputStream inputStream = content.getInputStream()) {
            return ByteString.copyFrom(inputStream.readAllBytes());
        } catch (IOException e) {
            throw new RuntimeException("Failed to convert InputStreamResource to ByteString", e);
        }
    }
}
