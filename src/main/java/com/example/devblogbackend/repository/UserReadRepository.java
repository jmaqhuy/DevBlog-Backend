package com.example.devblogbackend.repository;

import com.example.devblogbackend.entity.UserReadHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserReadRepository extends JpaRepository<UserReadHistory, UserReadHistory.UserReadHistoryID> {
}
