package com.dataflow.ingestion.storage;

import java.io.File;
import java.io.InputStream;

public interface StorageService {
    void ensureBucketExists(String bucketName) throws Exception;
    String uploadFile(String bucketName, String objectKey, File file, String contentType) throws Exception;
    String uploadStream(String bucketName, String objectKey, InputStream inputStream, long size, String contentType) throws Exception;
    boolean objectExists(String bucketName, String objectKey) throws Exception;
    InputStream getObject(String bucketName, String objectKey) throws Exception;
}
