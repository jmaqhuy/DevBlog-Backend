package com.example.devblogbackend.controller;

import com.example.devblogbackend.dto.ApiResponse;
import com.example.devblogbackend.dto.response.TagDTO;
import com.example.devblogbackend.service.TagService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class TagController {
    private final TagService tagService;

    public TagController(TagService tagService) {
        this.tagService = tagService;
    }


    @GetMapping("/tags")
    public ApiResponse<List<TagDTO>> getAllTags() {
        return tagService.getAllTag();
    }
}
