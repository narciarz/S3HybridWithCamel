package com.mawi.s3hybridwithcamel.processor;

import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.stereotype.Component;

import java.io.InputStream;

@Component
@Slf4j
public class MyProcessor implements Processor {
    @Override
    public void process(Exchange exchange) throws Exception {
        log.info("DO whatever inside processor ");
        try(
                final InputStream is = exchange.getIn().getBody(InputStream.class)) {
            log.info("Inside of object " + new String(is.readAllBytes()));
        }
    }
}
