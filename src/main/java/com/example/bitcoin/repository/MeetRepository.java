package com.example.bitcoin.repository;

import com.example.bitcoin.entity.Meet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MeetRepository extends JpaRepository<Meet, Long> {
    List<Meet> findByEndDateTimeBefore(LocalDateTime endDateTime);
}