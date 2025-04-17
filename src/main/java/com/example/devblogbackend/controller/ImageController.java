package com.example.devblogbackend.controller;

import com.example.devblogbackend.service.FileStorageService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController
public class ImageController {

    private final FileStorageService fileStorageService;

    public ImageController(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
    }

    @PostMapping("/api/upload-image")
    @Operation(
            summary = "Upload an image"
    )
    public ResponseEntity<?> uploadFile(@RequestParam("image") MultipartFile file) {
        String fileName = fileStorageService.storeFile(file);

//        String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
//                .path("/api/images/")
//                .path(fileName)
//                .toUriString();


        Map<String, String> response = new HashMap<>();
        response.put("imageName", fileName);
//        response.put("imageLink", fileDownloadUri);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/images/{fileName:.+}")
    @Operation(
            summary = "View Image"
    )
    public ResponseEntity<Resource> downloadFile(@PathVariable String fileName, HttpServletRequest request) {
        Resource resource = fileStorageService.loadFileAsResource(fileName);

        // Xác định content type của file
        String contentType = null;
        try {
            contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
        } catch (IOException ex) {
            // logger.info("Could not determine file type.");
        }

        // Mặc định là octet-stream nếu không xác định được
        if (contentType == null) {
            contentType = "application/octet-stream";
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                // Header này cho phép hiển thị ảnh trực tiếp trên trình duyệt/ImageView
                // Bỏ .header(...) nếu muốn trình duyệt tự động tải file về thay vì hiển thị
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }
}