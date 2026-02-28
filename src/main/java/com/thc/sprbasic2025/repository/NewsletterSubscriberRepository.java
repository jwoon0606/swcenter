package com.thc.sprbasic2025.repository;

import com.thc.sprbasic2025.domain.NewsletterSubscriber;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NewsletterSubscriberRepository extends JpaRepository<NewsletterSubscriber, Long> {
    NewsletterSubscriber findByEmail(String email);
}
