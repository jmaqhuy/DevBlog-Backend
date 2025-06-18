package com.example.devblogbackend.repository;

import com.example.devblogbackend.entity.User;
import com.example.devblogbackend.enums.Role;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    // Search users by username or fullname containing keyword (case-insensitive), excluding users with ADMIN role
    @Query("SELECT u FROM User u WHERE (LOWER(u.username) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(u.fullname) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND :adminRole NOT MEMBER OF u.roles")
    List<User> findUserByUsernameOrFullnameNotContainingAdmin(@Param("keyword") String keyword, @Param("adminRole") Role adminRole, Pageable pageable);
}
