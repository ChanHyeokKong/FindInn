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

    public FileStorageService(@Value("${file.upload-dir}") String uploadDir) {
        this.rootLocation = Paths.get(uploadDir);
        try {
            // Create the directory if it doesn't exist
            Files.createDirectories(rootLocation);
        } catch (IOException e) {
            throw new RuntimeException("저장경로를 지정할 수 없습니다", e);
        }
    }

    public String store(MultipartFile file, String subDirectory) {
        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());

        try {
            if (file.isEmpty() || originalFilename.contains("..")) {
                throw new RuntimeException("잘못된 파일입니다.");
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
            throw new RuntimeException("파일 저장에 실패했습니다: " + originalFilename, e);
        }
    }

    public void delete(String filename, String subfolder) {
        if (filename == null || filename.isBlank()) {
            return;
        }

        try {
            // 경로 지정
            Path filePath = this.rootLocation.resolve(subfolder).resolve(filename).normalize();
            Files.deleteIfExists(filePath);

        } catch (IOException ex) {
            System.err.println("파일을 지우는데 실패했습니다: " + filename + "." + ex.getMessage());
        }
    }
}
