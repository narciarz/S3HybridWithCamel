package com.mawi.s3hybridwithcamel.routes;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

@Component
public class CamelAWSRoute extends RouteBuilder {
    static final String ROUTE = "AwsSimpleRouteWithMove";
    @Override
    public void configure() {
        from("aws2-s3://{{app.s3.bucket-name}}?delay=30000&prefix={{app.s3.camel-prefix}}&moveAfterRead=true&destinationBucket={{app.s3.bucket-name}}&destinationBucketPrefix=done/")
                .routeId(ROUTE)
                .log("Received body: ${body}");
    }
}
