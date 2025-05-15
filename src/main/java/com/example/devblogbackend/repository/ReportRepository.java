package com.example.devblogbackend.repository;

import com.example.devblogbackend.entity.Report;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReportRepository extends JpaRepository<Report, Long> {
}
