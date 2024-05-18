package com.example.bitcoin.repository;

import com.example.bitcoin.entity.Visitor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface VisitorRepository extends JpaRepository<Visitor, Long> {
    Optional<Visitor> findByDate(LocalDate date);
}
