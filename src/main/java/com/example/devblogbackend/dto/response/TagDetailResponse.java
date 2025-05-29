package com.example.devblogbackend.dto.response;

import com.example.devblogbackend.dto.PostDTO;
import com.example.devblogbackend.dto.TagWithScore;
import lombok.*;

import java.util.List;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TagDetailResponse {
    private TagWithScore tag;
    private List<PostDTO> posts;
}
