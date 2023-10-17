package com.mawi.s3hybridwithcamel.controller;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.mawi.s3hybridwithcamel.service.S3Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.NoSuchBucketException;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;

import java.io.IOException;

@RestController
@RequestMapping(path = "/api")
@RequiredArgsConstructor
@Slf4j
public class S3Controller {
    private final S3Service s3Service;
    private static final String NO_KEY_OR_BUCKET = "No such bucket / key found";
    ObjectMapper objectMapper = new ObjectMapper()
            .setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY)
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .registerModule(new JavaTimeModule());

    @GetMapping(value = "/buckets/list", produces = MediaType.APPLICATION_JSON_VALUE)
    public String showBuckets() throws JsonProcessingException {
        return objectMapper.writeValueAsString(s3Service.listBuckets().buckets());
    }

    @GetMapping(value = "/bucket/{bucketName}/files", produces = MediaType.APPLICATION_JSON_VALUE)
    public String showFiles(
            @PathVariable final String bucketName,
            @RequestParam(value = "dir", required = false) final String dir
    ) throws JsonProcessingException {
        try {
            return objectMapper.writeValueAsString(s3Service.listObjects(bucketName, dir));
        } catch (NoSuchBucketException | NoSuchKeyException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, NO_KEY_OR_BUCKET);
        }
    }

    @GetMapping(value = "/bucket/{bucketName}/copyFile",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public String copyFile(@PathVariable("bucketName") final String bucketName,
                           @RequestParam("fileName") final String fileName,
                           @RequestParam(value = "srcDir", required = false) final String srcDir,
                           @RequestParam(value = "dstDir", required = false) final String dstDir
    ) throws JsonProcessingException {
        try {
            return objectMapper.writeValueAsString(
                    s3Service.copyObject(bucketName,
                            srcDir == null ? StringUtils.EMPTY : srcDir,
                            dstDir == null ? StringUtils.EMPTY : dstDir, fileName)
            );
        } catch (NoSuchBucketException | NoSuchKeyException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, NO_KEY_OR_BUCKET);
        }
    }

    @PostMapping(value = "/bucket/{bucketName}/uploadFile",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public String uploadFile(@RequestPart("file") final MultipartFile file,
                             @PathVariable("bucketName") final String bucketName,
                             @RequestParam(value = "fileName", required = false) final String fileName,
                             @RequestParam(value = "dir", required = false) final String dir) throws IOException {
        try {
            return objectMapper.writeValueAsString(
                    s3Service.putObject(bucketName, dir == null ? StringUtils.EMPTY : dir,
                            fileName != null ? fileName : file.getOriginalFilename(),
                            file.getInputStream()));
        } catch (NoSuchBucketException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No such bucket found");
        }
    }

    @DeleteMapping(value = "/bucket/{bucketName}/deleteFile",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public String deleteObject(
            @PathVariable("bucketName") final String bucketName,
            @RequestParam(value = "fileName") final String fileName,
            @RequestParam(value = "dir", required = false) final String dir
    ) throws JsonProcessingException {
        try {
            return objectMapper.writeValueAsString(s3Service.deleteObject(bucketName, dir, fileName));
        } catch (NoSuchBucketException | NoSuchKeyException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, NO_KEY_OR_BUCKET);
        }
    }

    @GetMapping(path = "/bucket/{bucketName}/downloadFile")
    public ResponseEntity<Resource> downloadFileOther(
            @PathVariable("bucketName") final String bucketName,
            @RequestParam(value = "dir", required = false) final String dir,
            @RequestParam("fileName") final String fileName) {
        try {
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
        } catch (NoSuchBucketException | NoSuchKeyException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Cannot download [{}] from [{}] {}", fileName, bucketName, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
}
