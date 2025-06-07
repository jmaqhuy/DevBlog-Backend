package com.example.devblogbackend.repository;

import com.example.devblogbackend.entity.Tag;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface TagRepository extends JpaRepository<Tag, Integer> {
    Tag findByName(String name);
    List<Tag> findAllByOrderByNameAsc();

    Set<Tag> findByNameIn(List<String> names);

    @Query("SELECT t, SUM(p.score) as totalScore, COUNT(p) as postCount FROM Post p JOIN p.tags t GROUP BY t ORDER BY totalScore DESC")
    List<Object[]> findTagsWithScore();

    // Search tags by name containing keyword (case-insensitive)
    List<Tag> findByNameContainingIgnoreCase(String name, Pageable pageable);
}
