package ru.brombin.image_service.service.minio;

import io.minio.*;
import io.minio.http.Method;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.brombin.image_service.util.exception.MinioFileException;

import java.io.InputStream;

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class MinioServiceImpl implements MinioService {

    MinioClient minioClient;

    @Value("${minio.bucket.name}")
    String bucketName;

    @Override
    public String uploadFile(MultipartFile file, String fileName) {
        try {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(fileName)
                            .stream(file.getInputStream(), file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build()
            );
            log.info("File {} uploaded successfully to MinIO", fileName);
            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .bucket(bucketName)
                            .object(fileName)
                            .method(Method.GET)
                            .build()
            );
        } catch (Exception e) {
            throw new MinioFileException("Failed to upload file " + fileName, e);
        }
    }

    @Override
    public byte[] getFileContentAsByteArray(String fileName) {
        try (InputStream fileStream = minioClient.getObject(
                GetObjectArgs.builder()
                        .bucket(bucketName)
                        .object(fileName)
                        .build()
        )) {
            return fileStream.readAllBytes();
        } catch (Exception e) {
            throw new MinioFileException("Failed to retrieve file content for " + fileName, e);
        }
    }

    @Override
    public void deleteFile(String fileName) {
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucketName)
                            .object(fileName)
                            .build()
            );
            log.info("File {} deleted successfully from MinIO", fileName);
        } catch (Exception e) {
            throw new MinioFileException("Failed to delete file " + fileName, e);
        }
    }
}
