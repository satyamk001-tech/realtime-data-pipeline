package com.dataflow.ingestion.storage;

import io.minio.*;
import io.minio.errors.ErrorResponseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.InputStream;

public class MinioStorageService implements StorageService {
    private static final Logger log = LoggerFactory.getLogger(MinioStorageService.class);

    private final MinioClient minioClient;

    public MinioStorageService(String endpoint, String accessKey, String secretKey) {
        this.minioClient = MinioClient.builder()
                .endpoint(endpoint)
                .credentials(accessKey, secretKey)
                .build();
    }

    public MinioStorageService(MinioClient minioClient) {
        this.minioClient = minioClient;
    }

    @Override
    public void ensureBucketExists(String bucketName) throws Exception {
        boolean found = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
        if (!found) {
            log.info("Bucket '{}' does not exist. Creating it now...", bucketName);
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
            log.info("Bucket '{}' created successfully.", bucketName);
        }
    }

    @Override
    public String uploadFile(String bucketName, String objectKey, File file, String contentType) throws Exception {
        ensureBucketExists(bucketName);
        log.info("Uploading '{}' ({} bytes) to MinIO '{}/{}'...", file.getName(), file.length(), bucketName, objectKey);
        
        minioClient.uploadObject(
                UploadObjectArgs.builder()
                        .bucket(bucketName)
                        .object(objectKey)
                        .filename(file.getAbsolutePath())
                        .contentType(contentType != null ? contentType : "application/octet-stream")
                        .build()
        );
        
        log.info("Successfully uploaded object '{}/{}'", bucketName, objectKey);
        return String.format("s3a://%s/%s", bucketName, objectKey);
    }

    @Override
    public String uploadStream(String bucketName, String objectKey, InputStream inputStream, long size, String contentType) throws Exception {
        ensureBucketExists(bucketName);
        minioClient.putObject(
                PutObjectArgs.builder()
                        .bucket(bucketName)
                        .object(objectKey)
                        .stream(inputStream, size, -1)
                        .contentType(contentType != null ? contentType : "application/json")
                        .build()
        );
        return String.format("s3a://%s/%s", bucketName, objectKey);
    }

    @Override
    public boolean objectExists(String bucketName, String objectKey) throws Exception {
        try {
            minioClient.statObject(StatObjectArgs.builder().bucket(bucketName).object(objectKey).build());
            return true;
        } catch (ErrorResponseException e) {
            if ("NoSuchKey".equals(e.errorResponse().code())) {
                return false;
            }
            throw e;
        }
    }

    @Override
    public InputStream getObject(String bucketName, String objectKey) throws Exception {
        return minioClient.getObject(GetObjectArgs.builder().bucket(bucketName).object(objectKey).build());
    }
}
