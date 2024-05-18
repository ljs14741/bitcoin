package com.example.bitcoin.service;

import com.example.bitcoin.dto.VisitorDTO;
import com.example.bitcoin.entity.Visitor;
import com.example.bitcoin.repository.VisitorRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Optional;

@Service
public class VisitorService {
    @Autowired
    private VisitorRepository visitorRepository;

    @PostConstruct
    public void init() {
        Optional<Visitor> optionalVisitor = visitorRepository.findByDate(LocalDate.now());
        if (!optionalVisitor.isPresent()) {
            Visitor visitor = new Visitor();
            visitorRepository.save(visitor);
        }
    }

    public void incrementVisitorCount() {
        Visitor visitor = visitorRepository.findByDate(LocalDate.now())
                .orElse(new Visitor());
        visitor.setDailyCount(visitor.getDailyCount() + 1);
        visitor.setTotalCount(visitor.getTotalCount() + 1);
        visitorRepository.save(visitor);
    }

    public VisitorDTO getVisitorCount() {
        Visitor visitor = visitorRepository.findByDate(LocalDate.now()).orElse(new Visitor());
        int totalCount = visitorRepository.findAll().stream().mapToInt(Visitor::getTotalCount).sum();
        return new VisitorDTO(visitor.getDailyCount(), totalCount);
    }
}
