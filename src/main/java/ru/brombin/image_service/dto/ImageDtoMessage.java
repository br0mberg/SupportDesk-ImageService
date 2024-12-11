package ru.brombin.image_service.dto;

import org.springframework.core.io.InputStreamResource;

public record ImageDtoMessage (
        ImageDto imageDto,
        InputStreamResource content
){
}
