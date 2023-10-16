package com.mawi.s3hybridwithcamel.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.services.s3.S3Client;

import java.net.URI;
import java.net.URISyntaxException;

@Configuration
public class S3Config {
    @Bean
    S3Client s3Client(S3Properties s3Properties) throws URISyntaxException {
        return S3Client.builder().credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(s3Properties.getAccessKey(), s3Properties.getSecretKey())))
                .region(s3Properties.getRegion())
                .endpointOverride(new URI(s3Properties.getUrl()))
                .build();
    }
}
