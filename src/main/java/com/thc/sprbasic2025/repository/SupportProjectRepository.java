package com.thc.sprbasic2025.repository;

import com.thc.sprbasic2025.domain.SupportProject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SupportProjectRepository extends JpaRepository<SupportProject, Long> {
    SupportProject findBySequence(Integer sequence);
    long countByDeletedFalse();
    boolean existsByTitleAndDeletedFalse(String title);
}
