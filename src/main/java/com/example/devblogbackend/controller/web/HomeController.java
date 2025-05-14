package com.example.devblogbackend.controller.web;

import com.example.devblogbackend.service.PostService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.core.io.FileSystemResource;

import java.io.File;

@Controller
public class HomeController {

    @Value("${file.apk}")
    private String apkFilePath;


    private final PostService postService;
    public HomeController(PostService postService) {
        this.postService = postService;
    }

    @GetMapping("/")
    public String homePage(Model model) {
        model.addAttribute("listPosts", postService.getSamplePosts());
        return "index";
    }

    @GetMapping("/download-apk")
    public ResponseEntity<Resource> downloadApk() {
        System.out.println("APK file path: {}" + apkFilePath);
        File file = new File(apkFilePath);
        if (!file.exists()) {
            return ResponseEntity.notFound().build();
        }

        Resource resource = new FileSystemResource(file);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + file.getName())
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }
}
