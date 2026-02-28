package com.thc.sprbasic2025.repository;

import com.thc.sprbasic2025.domain.NoticeComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NoticeCommentRepository extends JpaRepository<NoticeComment, Long> {
    List<NoticeComment> findByNoticeIdAndDeletedFalseOrderByIdAsc(Long noticeId);
}
