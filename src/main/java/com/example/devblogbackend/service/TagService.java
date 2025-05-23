package com.example.devblogbackend.service;

import com.example.devblogbackend.dto.ApiResponse;
import com.example.devblogbackend.dto.Meta;
import com.example.devblogbackend.dto.TagDTO;
import com.example.devblogbackend.dto.TagScoreProjection;
import com.example.devblogbackend.entity.Tag;
import com.example.devblogbackend.repository.TagRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class TagService {
    private TagRepository tagRepository;
    private final String API_VERSION = "v1";
    public TagService(TagRepository tagRepository) {
        this.tagRepository = tagRepository;
    }

    public Tag add(Tag tag) {
        tag.setName(tag.getName().toLowerCase());
        Tag savedTag = tagRepository.findByName(tag.getName());
        if (savedTag == null) {
            savedTag = tagRepository.save(tag);
        }
        return savedTag;
    }

    public Set<Tag> addCollection(Set<Tag> tags) {
        Set<Tag> savedTags = new HashSet<>();
        for (Tag tag : tags) {
            savedTags.add(add(tag));
        }
        return savedTags;
    }

    public Tag add(String tagName, String description) {
        return add(Tag.builder()
                .name(tagName)
                .description(description)
                .build());
    }

    public Tag findByName(String name) {
        return tagRepository.findByName(name);
    }

    public Set<Tag> getTagsByName(List<String> tagNames) {
        return tagRepository.findByNameIn(tagNames);
    }

    public Tag findById(int id) {
        return tagRepository.findById(id).orElse(null);
    }

    public ApiResponse<List<TagDTO>> getAllTag() {
        List<TagDTO> tagDtos = tagRepository.findAllByOrderByNameAsc()
                .stream()
                .map(tag -> TagDTO.builder()
                        .id(tag.getId())
                        .name(tag.getName())
                        .build())
                .toList();

        return ApiResponse.<List<TagDTO>>builder()
                .data(tagDtos)
                .meta(new Meta(API_VERSION))
                .build();
    }

    public void delete(String name) {
        Tag tag = findByName(name);
        if (tag != null) {
            tagRepository.delete(tag);
        }
    }

    public void delete(Tag tag){
        delete(tag.getName());
    }

    public Tag update(Tag tag) {
        Tag currentTag = tagRepository.findById(tag.getId())
                .orElseThrow();

        currentTag.setName(tag.getName());
        currentTag.setDescription(tag.getDescription());
        return tagRepository.save(currentTag);
    }

//    public List<TagDTO> getTopTags() {
//        List<TagScoreProjection> projections = tagRepository.findTopTags(1, 0.5, 2, 0.1, LocalDate.now().minusDays(30), 10);
//        return projections.stream()
//                .map(p -> new TagDTO(p.getTagId(), p.getTagName(), p.getTotalScore()))
//                .collect(Collectors.toList());
//    }

}
