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
        @Index(columnList = "boardId"),
        @Index(columnList = "userId")
})
@Entity
public class BoardComment extends AuditingFields {
    Long boardId;
    Long userId;
    String content;

    protected BoardComment() {}

    private BoardComment(Long boardId, Long userId, String content) {
        this.boardId = boardId;
        this.userId = userId;
        this.content = content;
    }

    public static BoardComment of(Long boardId, Long userId, String content) {
        return new BoardComment(boardId, userId, content);
    }
}
