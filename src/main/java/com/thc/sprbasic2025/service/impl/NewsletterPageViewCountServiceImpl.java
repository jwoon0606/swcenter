package com.thc.sprbasic2025.service.impl;

import com.thc.sprbasic2025.domain.NewsletterPageViewCount;
import com.thc.sprbasic2025.repository.NewsletterPageViewCountRepository;
import com.thc.sprbasic2025.service.NewsletterPageViewCountService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NewsletterPageViewCountServiceImpl implements NewsletterPageViewCountService {

    private final NewsletterPageViewCountRepository newsletterPageViewCountRepository;

    public NewsletterPageViewCountServiceImpl(NewsletterPageViewCountRepository newsletterPageViewCountRepository) {
        this.newsletterPageViewCountRepository = newsletterPageViewCountRepository;
    }

    @Override
    @Transactional
    public Long increaseViewCount(String targetType, String targetKey) {
        NewsletterPageViewCount pageViewCount = newsletterPageViewCountRepository
                .findByTargetTypeAndTargetKey(targetType, targetKey)
                .orElseGet(() -> new NewsletterPageViewCount(targetType, targetKey, 0L));

        pageViewCount.setViewCount(pageViewCount.getViewCount() + 1);
        newsletterPageViewCountRepository.save(pageViewCount);

        return pageViewCount.getViewCount();
    }

    @Override
    @Transactional(readOnly = true)
    public Long getViewCount(String targetType, String targetKey) {
        return newsletterPageViewCountRepository
                .findByTargetTypeAndTargetKey(targetType, targetKey)
                .map(NewsletterPageViewCount::getViewCount)
                .orElse(0L);
    }
}