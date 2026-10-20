package com.dataflow.ingestion.storage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class LocalStorageService implements StorageService {
    private static final Logger log = LoggerFactory.getLogger(LocalStorageService.class);
    private final Path rootDir;

    public LocalStorageService(String rootPath) {
        this.rootDir = Paths.get(rootPath).toAbsolutePath();
        try {
            Files.createDirectories(this.rootDir);
        } catch (IOException e) {
            throw new RuntimeException("Failed to initialize local storage root directory: " + rootPath, e);
        }
    }

    @Override
    public void ensureBucketExists(String bucketName) throws Exception {
        Path bucketPath = rootDir.resolve(bucketName);
        if (!Files.exists(bucketPath)) {
            Files.createDirectories(bucketPath);
            log.info("Created local storage bucket directory: {}", bucketPath);
        }
    }

    @Override
    public String uploadFile(String bucketName, String objectKey, File file, String contentType) throws Exception {
        ensureBucketExists(bucketName);
        Path targetPath = rootDir.resolve(bucketName).resolve(objectKey);
        Files.createDirectories(targetPath.getParent());
        Files.copy(file.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);
        log.info("Saved file locally at: {}", targetPath);
        return targetPath.toUri().toString();
    }

    @Override
    public String uploadStream(String bucketName, String objectKey, InputStream inputStream, long size, String contentType) throws Exception {
        ensureBucketExists(bucketName);
        Path targetPath = rootDir.resolve(bucketName).resolve(objectKey);
        Files.createDirectories(targetPath.getParent());
        Files.copy(inputStream, targetPath, StandardCopyOption.REPLACE_EXISTING);
        return targetPath.toUri().toString();
    }

    @Override
    public boolean objectExists(String bucketName, String objectKey) throws Exception {
        Path targetPath = rootDir.resolve(bucketName).resolve(objectKey);
        return Files.exists(targetPath);
    }

    @Override
    public InputStream getObject(String bucketName, String objectKey) throws Exception {
        Path targetPath = rootDir.resolve(bucketName).resolve(objectKey);
        return Files.newInputStream(targetPath);
    }
}
