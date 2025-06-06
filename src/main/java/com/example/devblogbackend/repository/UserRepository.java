package com.example.devblogbackend.repository;

import com.example.devblogbackend.entity.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
    Optional<User> findByUsername(String username);
    User findByEmail(String email);
    boolean existsByEmail(String email);
    boolean existsByEmailAndIdNot(String email, String userId);
    boolean existsByUsernameAndIdNot(String email, String userId);

    // Search users by username or fullname containing keyword (case-insensitive)
    List<User> findByUsernameContainingIgnoreCaseOrFullnameContainingIgnoreCase(String username, String fullname, Pageable pageable);
}
