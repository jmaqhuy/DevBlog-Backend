package com.example.devblogbackend.controller;

import com.example.devblogbackend.service.FileStorageService;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import jakarta.servlet.http.HttpServletRequest; // Sửa đổi nếu dùng javax
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ImageController {

    private final FileStorageService fileStorageService;

    public ImageController(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
    }

    @PostMapping("/upload")
    public ResponseEntity<?> uploadFile(@RequestParam("image") MultipartFile file) {
        String fileName = fileStorageService.storeFile(file);

        // Tạo URL để client có thể tải file về
        String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/images/") // Đường dẫn tới endpoint tải ảnh
                .path(fileName)
                .toUriString();

        // Bạn có thể trả về nhiều thông tin hơn nếu cần
        Map<String, String> response = new HashMap<>();
        response.put("fileName", fileName);
        response.put("fileDownloadUri", fileDownloadUri);

        // Thông thường, sau khi upload, bạn sẽ lưu fileName hoặc fileDownloadUri
        // vào database cùng với thông tin khác (ví dụ: thông tin sản phẩm, bài viết...).
        // API lấy danh sách sản phẩm/bài viết sẽ trả về kèm fileDownloadUri này.

        return ResponseEntity.ok(response);
    }

    // Endpoint để tải/hiển thị ảnh
    @GetMapping("/images/{fileName:.+}")
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