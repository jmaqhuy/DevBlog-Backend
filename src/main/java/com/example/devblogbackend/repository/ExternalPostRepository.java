package com.example.devblogbackend.repository;

import com.example.devblogbackend.entity.ExternalPost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ExternalPostRepository extends JpaRepository<ExternalPost, Integer> {
}
