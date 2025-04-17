package com.example.devblogbackend.dto;

import com.example.devblogbackend.entity.Tag;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TagDTO {
    private int id;
    private String name;

    public static TagDTO fromEntity(Tag tag) {
        return new TagDTO(tag.getId(), tag.getName());
    }
}
