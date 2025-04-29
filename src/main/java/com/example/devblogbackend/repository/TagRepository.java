package com.example.devblogbackend.repository;

import com.example.devblogbackend.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface TagRepository extends JpaRepository<Tag, Integer> {
    Tag findByName(String name);
    List<Tag> findAllByOrderByNameAsc();

    Set<Tag> findByNameIn(List<String> names);


    /*@Query(value = """
        SELECT 
          t.id AS tagId,
          t.name AS tagName,
          SUM(
            (SELECT COUNT(*) FROM post_like pl WHERE pl.post_id = p.id) * :wLike +
            (SELECT COUNT(*) FROM post_comment pc WHERE pc.post_id = p.id) * :wComment +
            (SELECT COUNT(*) FROM bookmark pb WHERE pb.post_id = p.id) * :wBookmark -
            (DATEDIFF(CURRENT_DATE, p.publication_date) * :decay)
          ) AS totalScore
        FROM tag t
        JOIN post_tag pt ON pt.tag_id = t.id
        JOIN post p      ON pt.post_id = p.id
        WHERE p.publication_date >= :cutoffDate
        GROUP BY t.id, t.name
        ORDER BY totalScore DESC
        LIMIT :topN
        """,
            nativeQuery = true)
    List<TagScoreProjection> findTopTags(
            @Param("wLike") double wLike,
            @Param("wComment") double wComment,
            @Param("wBookmark") double wBookmark,
            @Param("decay") double decay,
            @Param("cutoffDate") LocalDate cutoffDate,
            @Param("topN") int topN
    );*/


}
