package com.example.devblogbackend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

@IdClass(UserReadHistory.UserReadHistoryID.class)
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserReadHistory {
    @Id
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Id
    @ManyToOne
    @JoinColumn(name = "post_id")
    private Post post;

    private LocalDateTime readAt;

    @Getter @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserReadHistoryID implements Serializable {
        private String user;
        private long post;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof UserReadHistoryID that)) return false;
            return Objects.equals(user, that.user) && post == that.post;
        }

        @Override
        public int hashCode() {
            return Objects.hash(user, post);
        }
    }
}
