package com.mawi.s3hybridwithcamel.controller;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.mawi.s3hybridwithcamel.service.S3Service;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

import java.io.IOException;

@RestController
@RequestMapping(path = "/api")
@RequiredArgsConstructor
public class S3Controller {
    private final S3Service s3Service;
    ObjectMapper objectMapper = new ObjectMapper()
            .setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY)
            .registerModule(new JavaTimeModule());

    @GetMapping(value = "/buckets/list", produces = MediaType.APPLICATION_JSON_VALUE)
    public String showBuckets() {
        return s3Service.listBuckets().toString();
    }

    @GetMapping(value = "/bucket/{bucketName}/files", produces = MediaType.APPLICATION_JSON_VALUE)
    public String showFiles(
            @PathVariable final String bucketName,
            @RequestParam(value = "dir", required = false) final String dir
    ) throws JsonProcessingException {
        return objectMapper.writeValueAsString(s3Service.listObjects(bucketName, dir).contents());
    }

    @PostMapping(value = "bucket/{bucketName}/uploadFile",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public String uploadFile(@RequestPart("file") final MultipartFile file,
                             @PathVariable("bucketName") final String bucketName,
                             @RequestParam(value = "filename", required = false) final String filename,
                             @RequestParam(value = "dir", required = false) final String dir) throws IOException {
        return objectMapper.writeValueAsString(
                s3Service.putObject(bucketName, dir == null ? StringUtils.EMPTY : dir,
                        filename != null ? filename : file.getOriginalFilename(),
                        file.getInputStream()));
    }

    @DeleteMapping(value = "bucket/{bucketName}/deleteFile",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public String deleteObject(
            @PathVariable("bucketName") final String bucketName,
            @RequestParam(value = "filename") final String filename,
            @RequestParam(value = "dir", required = false) final String dir
    ) throws JsonProcessingException {
        return objectMapper.writeValueAsString(
        s3Service.deleteObject(bucketName, dir, filename)
        );
    }

    @GetMapping(path = "bucket/{bucketName}/downloadFile")
    public ResponseEntity<Resource> downloadFileOther(
            @PathVariable("bucketName") final String bucketName,
            @RequestParam(value = "dir", required = false) final String dir,
            @RequestParam("fileName") final String fileName) throws IOException {
        ResponseInputStream<GetObjectResponse> inputStream = s3Service.getObjectInputStream(bucketName, dir, fileName);
        InputStreamResource resource = new InputStreamResource(inputStream);
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName);
        headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
        headers.add("Pragma", "no-cache");
        headers.add("Expires", "0");
        return ResponseEntity.ok()
                .headers(headers)
                .contentLength(inputStream.available())
                .contentType(MediaType.parseMediaType("application/octet-stream"))
                .body(resource);
    }
}
