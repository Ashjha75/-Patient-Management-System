package com.patientmanagement.patientservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
public class S3Config {
    @Value("${AWS_REGION}")
    private String AWS_REGION;

    @Value("${AWS_ACCESS_KEY_ID}")
    private String ACCESS_KEY_ID;

    @Value("${AWS_SECRET_ACCESS_KEY}")
    private String SECRET_ACCESS_KEY;

    @Bean
    public S3Client s3Client() {
        return S3Client.builder()
                .region(Region.of(AWS_REGION)) // use your AWS region
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(
                                ACCESS_KEY_ID,
                                SECRET_ACCESS_KEY
                        )
                ))
                .build();
    }
}
