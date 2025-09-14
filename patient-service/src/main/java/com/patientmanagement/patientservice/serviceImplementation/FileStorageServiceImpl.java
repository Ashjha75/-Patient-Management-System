package com.patientmanagement.patientservice.serviceImplementation;

import com.patientmanagement.patientservice.exception.ApiException;
import com.patientmanagement.patientservice.service.FileStorageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.UUID;

@Service
public class FileStorageServiceImpl implements FileStorageService {
    private final S3Client s3Client;
    @Value("${AWS_S3_BUCKET}")
    private String AWS_S3_BUCKET;

    public FileStorageServiceImpl(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    public String upload(MultipartFile file, String folder) {
        String key = folder + "/" + UUID.randomUUID() + "_" + file.getOriginalFilename();

        try {
            // configure in application.yml if you want
            String bucketName =
                    AWS_S3_BUCKET;
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .contentType(file.getContentType())
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromBytes(file.getBytes()));

            // Public URL (if bucket allows public access)
            return String.format("https://%s.s3.%s.amazonaws.com/%s",
                    bucketName,
                    s3Client.serviceClientConfiguration().region().id(),
                    key);

        } catch (IOException e) {
            throw new ApiException("Failed to upload file to S3", e);
        }
    }
}
