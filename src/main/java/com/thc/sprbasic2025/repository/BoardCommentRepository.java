package com.thc.sprbasic2025.repository;

import com.thc.sprbasic2025.domain.BoardComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BoardCommentRepository extends JpaRepository<BoardComment, Long> {
    List<BoardComment> findByBoardIdAndDeletedFalseOrderByIdAsc(Long boardId);
}
