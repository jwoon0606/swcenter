package com.thc.sprbasic2025.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@Table(
        indexes = {
                @Index(columnList = "deleted"),
                @Index(columnList = "noticeId"),
                @Index(columnList = "userId")
        },
        uniqueConstraints = {@UniqueConstraint(
                name = "UQ_noticereport_noticeId_userId",
                columnNames = {"noticeId", "userId"}
        )}
)
@Entity
public class NoticeReport extends AuditingFields {
    Long noticeId;
    Long userId;
    String content;

    protected NoticeReport() {}

    private NoticeReport(Long noticeId, Long userId, String content) {
        this.noticeId = noticeId;
        this.userId = userId;
        this.content = content;
    }

    public static NoticeReport of(Long noticeId, Long userId, String content) {
        return new NoticeReport(noticeId, userId, content);
    }
}
