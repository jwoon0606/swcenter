package com.thc.sprbasic2025.repository;

import com.thc.sprbasic2025.domain.Newsletter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NewsletterRepository extends JpaRepository<Newsletter, Long>, JpaSpecificationExecutor<Newsletter> {
    Optional<Newsletter> findByIdAndDeletedFalse(Long id);
}
