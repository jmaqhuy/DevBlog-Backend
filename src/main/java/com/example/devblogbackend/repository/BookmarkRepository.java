package com.example.devblogbackend.repository;

import com.example.devblogbackend.entity.Bookmark;
import com.example.devblogbackend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookmarkRepository extends JpaRepository<Bookmark, Bookmark.BookmarkID> {
    List<Bookmark> findByUser(User user);
}
