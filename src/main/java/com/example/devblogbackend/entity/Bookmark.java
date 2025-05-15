package com.example.devblogbackend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@IdClass(Bookmark.BookmarkID.class)
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class Bookmark {
    @Id
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Id
    @ManyToOne
    @JoinColumn(name = "post_id" , nullable = false)
    private Post post;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;


    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    public static class BookmarkID implements Serializable {
        private String user;
        private Long post;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof BookmarkID that)) return false;
            return Objects.equals(user, that.user) && post == that.post;
        }

        @Override
        public int hashCode() {
            return Objects.hash(user, post);
        }
    }

}
