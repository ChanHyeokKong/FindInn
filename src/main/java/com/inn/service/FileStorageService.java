package com.inn.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileStorageService {

    private final Path rootLocation;

    // Inject the upload path from application.properties
    public FileStorageService(@Value("${file.upload-dir}") String uploadDir) {
        this.rootLocation = Paths.get(uploadDir);
        try {
            // Create the directory if it doesn't exist
            Files.createDirectories(rootLocation);
        } catch (IOException e) {
            throw new RuntimeException("Could not initialize storage location", e);
        }
    }

    public String store(MultipartFile file, String subDirectory) {
        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());

        try {
            if (file.isEmpty() || originalFilename.contains("..")) {
                throw new RuntimeException("Invalid file provided");
            }

            Path targetDirectory = this.rootLocation.resolve(subDirectory).normalize();
            Files.createDirectories(targetDirectory);

            String fileExtension = "";
            int lastDot = originalFilename.lastIndexOf('.');
            if (lastDot > 0) {
                fileExtension = originalFilename.substring(lastDot);
            }
            String uniqueFilename = UUID.randomUUID().toString() + fileExtension;

            Path destinationFile = targetDirectory.resolve(uniqueFilename);

            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, destinationFile, StandardCopyOption.REPLACE_EXISTING);
            }

            return uniqueFilename;

        } catch (IOException e) {
            throw new RuntimeException("Failed to store file " + originalFilename, e);
        }
    }

    public void delete(String filename, String subfolder) {
        if (filename == null || filename.isBlank()) {
            return; // Do nothing if the filename is invalid
        }

        try {
            // 1. Construct the full path to the file
            Path filePath = this.rootLocation.resolve(subfolder).resolve(filename).normalize();

            // 2. Delete the file if it exists
            // Using deleteIfExists prevents an exception if the file is already gone for some reason
            Files.deleteIfExists(filePath);

        } catch (IOException ex) {
            // You might want to log this error instead of throwing a fatal exception,
            // as a failed deletion might not be a critical failure.
            System.err.println("Could not delete file: " + filename + ". Reason: " + ex.getMessage());
            // Or re-throw as a custom exception if it's critical
            // throw new RuntimeException("Could not delete file: " + filename, ex);
        }
    }
}
