package com.thc.sprbasic2025.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@Table(indexes = {
        @Index(columnList = "deleted"),
        @Index(columnList = "noticeId"),
        @Index(columnList = "userId")
})
@Entity
public class NoticeComment extends AuditingFields {
    Long noticeId;
    Long userId;
    String content;

    protected NoticeComment() {}

    private NoticeComment(Long noticeId, Long userId, String content) {
        this.noticeId = noticeId;
        this.userId = userId;
        this.content = content;
    }

    public static NoticeComment of(Long noticeId, Long userId, String content) {
        return new NoticeComment(noticeId, userId, content);
    }
}
