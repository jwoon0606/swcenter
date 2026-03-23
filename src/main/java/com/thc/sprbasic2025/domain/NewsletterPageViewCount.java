package com.thc.sprbasic2025.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "newsletter_page_view_count",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_target", columnNames = {"target_type", "target_key"})
        }
)
public class NewsletterPageViewCount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "target_type", nullable = false, length = 30)
    private String targetType;

    @Column(name = "target_key", nullable = false, length = 100)
    private String targetKey;

    @Column(name = "view_count", nullable = false)
    private Long viewCount = 0L;

    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false, insertable = false, updatable = false)
    private LocalDateTime updatedAt;

    public NewsletterPageViewCount() {
    }

    public NewsletterPageViewCount(String targetType, String targetKey, Long viewCount) {
        this.targetType = targetType;
        this.targetKey = targetKey;
        this.viewCount = viewCount;
    }

    public Long getId() {
        return id;
    }

    public String getTargetType() {
        return targetType;
    }

    public void setTargetType(String targetType) {
        this.targetType = targetType;
    }

    public String getTargetKey() {
        return targetKey;
    }

    public void setTargetKey(String targetKey) {
        this.targetKey = targetKey;
    }

    public Long getViewCount() {
        return viewCount;
    }

    public void setViewCount(Long viewCount) {
        this.viewCount = viewCount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}