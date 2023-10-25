package com.mawi.s3hybridwithcamel.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@EntityScan(basePackages = {
        "com.mawi.s3hybridwithcamel",
        "org.apache.camel.processor.idempotent.jpa"
})
public class JpaConfig {
}
