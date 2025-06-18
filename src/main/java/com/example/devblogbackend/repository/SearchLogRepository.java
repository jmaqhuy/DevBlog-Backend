package com.example.devblogbackend.repository;

import com.example.devblogbackend.entity.SearchLog;
import com.example.devblogbackend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface SearchLogRepository extends JpaRepository<SearchLog, Long> {
    List<SearchLog> findByUserOrderBySearchedAtDesc(User user);

    // Find a search log by user and keyword
    SearchLog findByUserAndKeyword(User user, String keyword);

    List<SearchLog> findByKeywordContainsIgnoreCase(String keyword);
}
