package com.example.devblogbackend.dto;

import com.example.devblogbackend.entity.Tag;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TagWithScore {
    private int id;
    private String name;
    private String description;
    private Double totalScore;
    private Long postCount;
    private Boolean isFavorite;

    public static TagWithScore fromEntity(Tag tag, @Nullable Double totalScore,@Nullable Long postCount, Boolean isFavorite) {
        return new TagWithScore(tag.getId(), tag.getName(), tag.getDescription(), totalScore, postCount, isFavorite);
    }
}
