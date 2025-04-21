package com.example.devblogbackend.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

@Slf4j
@Service
public class FileStorageService {

    private final Path storageDir;
    private final String cwebpPath;

    public FileStorageService(@Value("${file.upload-dir}") Path storageDir,
                              @Value("${file.cwebp-dir}") String cwebpPath) {
        this.storageDir = storageDir;
        this.cwebpPath = cwebpPath;
    }

    public String storeImage(MultipartFile file) {
        String originalFileName = StringUtils.cleanPath(file.getOriginalFilename());
        String uniqueFileName = getRandomFileName();

        try {
            Path tempFile = Files.createTempFile("upload_", "_" + originalFileName );
            file.transferTo(tempFile);

            if (!isImage(tempFile)){
                Files.deleteIfExists(tempFile);
                throw new IllegalArgumentException("Uploaded file is not a valid image.");
            }

            Path targetLocation = this.storageDir.resolve(uniqueFileName);
            boolean success = convertToWebP(tempFile, targetLocation, 80);

            Files.deleteIfExists(tempFile);
            if (!success) {
                throw new RuntimeException("Failed to convert image to WebP.");
            }

            return uniqueFileName;
        } catch (IOException ex) {
            throw new RuntimeException("Could not store file " + uniqueFileName + ". Please try again!", ex);
        }
    }

    private boolean convertToWebP(Path inputPath, Path outputPath, int quality) {
        try {
            ProcessBuilder pb = new ProcessBuilder(
                    cwebpPath,
                    "-q", String.valueOf(quality),
                    inputPath.toAbsolutePath().toString(),
                    "-o", outputPath.toAbsolutePath().toString()
            );
            pb.redirectErrorStream(true);
            Process process = pb.start();

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    log.info(line);
                }
            }

            int exitCode = process.waitFor();
            return exitCode == 0;
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean isImage(Path filePath){
        try(InputStream is = Files.newInputStream(filePath)){
            BufferedImage image = ImageIO.read(is);
            return image != null;
        } catch (IOException e) {
            return false;
        }
    }

    private String getRandomFileName() {
        String uniqueFileName = UUID.randomUUID().toString();
        while (uniqueFileName.contains("..") || uniqueFileName.endsWith(".")) {
            uniqueFileName = UUID.randomUUID().toString();
        }
        return uniqueFileName + ".webp";
    }

    public Resource loadFileAsResource(String fileName) {
        try {
            Path filePath = this.storageDir.resolve(fileName).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists()) {
                return resource;
            } else {
                throw new RuntimeException("File not found " + fileName);
            }
        } catch (MalformedURLException ex) {
            throw new RuntimeException("File not found " + fileName, ex);
        }
    }
}
