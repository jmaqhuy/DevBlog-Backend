package com.example.devblogbackend.repository;

import com.example.devblogbackend.entity.UserInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserInfoRepository extends JpaRepository<UserInfo, String> {
    boolean existsByUsernameAndUserIdNot(String email, String userId);
}
