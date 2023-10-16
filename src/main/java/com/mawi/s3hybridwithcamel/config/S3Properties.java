package com.mawi.s3hybridwithcamel.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;

@Configuration
@ConfigurationProperties(prefix = "app.s3")
@Data
public class S3Properties {
    private String url;
    private String bucketName;
    private String accessKey;
    private String secretKey;
    private Region region;
}
