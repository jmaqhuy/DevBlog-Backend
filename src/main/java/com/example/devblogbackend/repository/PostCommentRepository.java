package com.example.devblogbackend.repository;

import com.example.devblogbackend.entity.Post;
import com.example.devblogbackend.entity.PostComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostCommentRepository extends JpaRepository<PostComment, Long> {
    List<PostComment> findByPostOrderByCommentAtDesc(Post post);
}
