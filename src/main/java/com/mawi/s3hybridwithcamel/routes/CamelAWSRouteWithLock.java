package com.mawi.s3hybridwithcamel.routes;

import com.mawi.s3hybridwithcamel.processor.MyProcessor;
import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.aws2.s3.AWS2S3Constants;
import org.apache.camel.processor.idempotent.jpa.JpaMessageIdRepository;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CamelAWSRouteWithLock extends RouteBuilder {
    private final EntityManagerFactory emf;
    private final MyProcessor myProcessor;
    static final String ROUTE = "AwsRouteWithLockByService";
    static final String IDEMPOTENT_KEY = "${header."+AWS2S3Constants.LAST_MODIFIED+"}#${header."+AWS2S3Constants.KEY+"}";
    static final String COMPLETE_PATH = "complete";
    static final String INPUT_PATH = "readByAll";

    @Override
    public void configure() {
        from("aws2-s3://{{app.s3.bucket-name}}?delay=30000&prefix="+INPUT_PATH+"&deleteAfterRead=false")
                .routeId(ROUTE)
                .idempotentConsumer(simple(IDEMPOTENT_KEY), JpaMessageIdRepository.jpaMessageIdRepository(emf, ROUTE))
                .eager(true)
                .skipDuplicate(true)
                .log(LoggingLevel.INFO, "Trying to consume ${header."+ AWS2S3Constants.KEY +"}")
                .process(myProcessor)
                .setHeader(AWS2S3Constants.BUCKET_DESTINATION_NAME, simple("${header."+AWS2S3Constants.BUCKET_NAME+"}"))
                .setHeader(AWS2S3Constants.DESTINATION_KEY, simple(COMPLETE_PATH +"/${header."+AWS2S3Constants.KEY+"}"))
                .to("aws2-s3://{{app.s3.bucket-name}}?operation=copyObject")
                .log(LoggingLevel.INFO, "Copied: ${header."+ AWS2S3Constants.KEY +"} to "+ COMPLETE_PATH)
                .to("aws2-s3://{{app.s3.bucket-name}}?operation=deleteObject")
                .log(LoggingLevel.INFO, "Deleted: ${header."+ AWS2S3Constants.KEY +"}");
    }
}
