package com.thc.sprbasic2025.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@Table(
        indexes = {
                @Index(columnList = "deleted"),
                @Index(columnList = "email"),
                @Index(columnList = "unsubscribeToken")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "UQ_newsletter_subscriber_email", columnNames = {"email"}),
                @UniqueConstraint(name = "UQ_newsletter_subscriber_unsubscribe_token", columnNames = {"unsubscribeToken"})
        }
)
@Entity
public class NewsletterSubscriber extends AuditingFields {
    String name;

    @Column(length = 191)
    String email;

    Boolean agreePrivacy;
    LocalDateTime subscribedAt;
    @Column(length = 64)
    String unsubscribeToken;

    protected NewsletterSubscriber() {}

    private NewsletterSubscriber(String name, String email, Boolean agreePrivacy) {
        this.name = name;
        this.email = email;
        this.agreePrivacy = agreePrivacy;
        this.subscribedAt = LocalDateTime.now();
        this.unsubscribeToken = newUnsubscribeToken();
    }

    public static NewsletterSubscriber of(String name, String email, Boolean agreePrivacy) {
        return new NewsletterSubscriber(name, email, agreePrivacy);
    }

    public void resubscribe(String name) {
        this.name = name;
        this.agreePrivacy = true;
        this.subscribedAt = LocalDateTime.now();
        ensureUnsubscribeToken();
        this.setDeleted(false);
    }

    public void unsubscribe() {
        this.setDeleted(true);
    }

    public void ensureUnsubscribeToken() {
        if (this.unsubscribeToken == null || this.unsubscribeToken.trim().isEmpty()) {
            this.unsubscribeToken = newUnsubscribeToken();
        }
    }

    private String newUnsubscribeToken() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}
