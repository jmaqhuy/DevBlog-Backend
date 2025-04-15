package com.example.devblogbackend.repository;

import com.example.devblogbackend.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TagRepository extends JpaRepository<Tag, Integer> {
    Tag findByName(String name);
    List<Tag> findAllByOrderByNameAsc();
}
