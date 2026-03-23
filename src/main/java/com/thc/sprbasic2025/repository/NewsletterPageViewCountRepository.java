package com.thc.sprbasic2025.repository;

import com.thc.sprbasic2025.domain.NewsletterPageViewCount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface NewsletterPageViewCountRepository
        extends JpaRepository<NewsletterPageViewCount, Long> {

    Optional<NewsletterPageViewCount> findByTargetTypeAndTargetKey(String targetType, String targetKey);
}