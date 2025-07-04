package com.example.devblogbackend.repository;

import com.example.devblogbackend.entity.Post;
import com.example.devblogbackend.entity.Tag;
import com.example.devblogbackend.entity.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    @Query("SELECT DISTINCT p FROM Post p " +
            "LEFT JOIN p.tags t " +
            "WHERE t.id IN :tagIds " +
            "ORDER BY p.score DESC")
    List<Post> findPostsByTagsOrderByScoreDesc(@Param("tagIds") Set<Integer> tagIds, Pageable pageable);

    @Query("SELECT p FROM Post p ORDER BY p.score DESC")
    List<Post> findAllByOrderByScoreDesc(Pageable pageable);

    @Query(
            value = """
                    SELECT p.*,
                           (SELECT COUNT(*) FROM post_comment pc WHERE pc.post_id = p.id) AS commentCount,
                           (
                              (
                                  COUNT(DISTINCT ur.user_id) * :wReadHistory
                                + COUNT(DISTINCT l.user_id) * :wLike
                                + COUNT(DISTINCT c.id) * :wComment
                                + COUNT(DISTINCT b.user_id) * :wBookmark
                              )
                              * EXP(-DATEDIFF(CURRENT_DATE, p.publication_date) / :decay)
                          ) AS score
                      FROM post p
                      LEFT JOIN post_like    l ON l.post_id = p.id
                      LEFT JOIN post_comment c ON c.post_id = p.id
                      LEFT JOIN bookmark b ON b.post_id = p.id
                      LEFT JOIN user_read_history ur ON ur.post_id = p.id
                    WHERE p.publication_date >= :cutoff
                      AND (
                        :#{#tagIds == null || #tagIds.isEmpty()} = true
                        OR EXISTS (
                          SELECT 1
                            FROM post_tag pt
                           WHERE pt.post_id = p.id
                             AND pt.tag_id IN (:tagIds)
                        )
                      )
                    GROUP BY p.id
                    ORDER BY score DESC
                    """,
            nativeQuery = true
    )
    List<Post> findPostsByScore(
            @Param("tagIds") Set<Integer> tagIds,
            @Param("cutoff") LocalDateTime cutoff,
            @Param("wLike") double wLike,
            @Param("wComment") double wComment,
            @Param("wBookmark") double wBookmark,
            @Param("wReadHistory") double wReadHistory,
            @Param("decay") double decay,
            Pageable pageable
    );


    @Query("""
            select p from Post p
            where p.publicationDate >= :cutoff
              and p.author in :following
            order by p.publicationDate desc
            """)
    List<Post> findPostsOfFollowing(
            @Param("following") Set<User> following,
            @Param("cutoff") LocalDateTime cutoff,
            Pageable pageable
    );

    List<Post> findPostsByAuthor(User author);

    List<Post> findPostsByTags(Set<Tag> tags);

    // Search posts by title, content, externalPost.title, externalPost.domain, or tag name (case-insensitive)
    @Query("""
        SELECT DISTINCT p FROM Post p
        LEFT JOIN p.tags t
        LEFT JOIN p.externalPost ep
        WHERE LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
           OR p.content LIKE CONCAT('%', :keyword, '%')
           OR (ep IS NOT NULL AND (LOWER(ep.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
                                 OR LOWER(ep.domain) LIKE LOWER(CONCAT('%', :keyword, '%'))))
           OR LOWER(t.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
    """)
    List<Post> searchPostsByKeyword(@Param("keyword") String keyword, Pageable pageable);
}
