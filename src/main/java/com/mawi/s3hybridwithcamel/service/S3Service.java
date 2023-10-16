package com.mawi.s3hybridwithcamel.service;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.IOException;
import java.io.InputStream;

@Service
@RequiredArgsConstructor
public class S3Service {
    private final S3Client s3Client;
    public ListBucketsResponse listBuckets(){
        return s3Client.listBuckets();
    }

    public ListObjectsV2Response listObjects(String bucketName, String dir) {
        ListObjectsV2Request listObjectsV2Request = ListObjectsV2Request.builder()
                .bucket(bucketName)
                .prefix(StringUtils.isBlank(dir)?StringUtils.EMPTY:dir)
                .build();
        return s3Client.listObjectsV2(listObjectsV2Request);
    }

    public PutObjectResponse putObject(String bucketName, String dir, String filename, InputStream inputStream) throws IOException {
        PutObjectRequest putOb = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(getFileName(dir, filename))
                .build();
        return s3Client.putObject(putOb, RequestBody.fromInputStream(inputStream, inputStream.available()));
    }

    public ResponseInputStream<GetObjectResponse> getObjectInputStream(String bucketName, String dir, String fileName) {
        GetObjectRequest getOb = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(getFileName(dir, fileName))
                .build();

        return s3Client.getObject(getOb);
    }

    public DeleteObjectResponse deleteObject(String bucketName, String dir, String fileName) {
        DeleteObjectRequest deleteOb = DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(getFileName(dir, fileName))
                .build();
        return s3Client.deleteObject(deleteOb);
    }

    private String getFileName(String dir, String fileName) {
        return StringUtils.isBlank(dir) ? fileName : dir.concat("/").concat(fileName);
    }
}
