package com.thc.sprbasic2025.repository;

import com.thc.sprbasic2025.domain.ExtracurricularActivity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ExtracurricularActivityRepository extends JpaRepository<ExtracurricularActivity, Long>, JpaSpecificationExecutor<ExtracurricularActivity> {
    Optional<ExtracurricularActivity> findByIdAndDeletedFalse(Long id);
    long countByDeletedFalse();
    boolean existsByTitleAndDeletedFalse(String title);
}
