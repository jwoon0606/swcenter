package com.thc.sprbasic2025.service;

import com.thc.sprbasic2025.dto.DefaultDto;
import com.thc.sprbasic2025.dto.BoardDto;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface BoardService {
    /**/
    DefaultDto.CreateResDto create(BoardDto.CreateReqDto param, Long reqUserId);
    void update(BoardDto.UpdateReqDto param, Long reqUserId);
    void delete(DefaultDto.DeleteReqDto param, Long reqUserId);
    void deleteList(DefaultDto.DeleteListReqDto param, Long reqUserId);
    BoardDto.DetailResDto detail(DefaultDto.DetailReqDto param, Long reqUserId);
    BoardDto.MetricsResDto increaseViewCount(DefaultDto.DetailReqDto param, Long reqUserId);
    BoardDto.MetricsResDto updateLikeCount(BoardDto.LikeReqDto param, Long reqUserId);
    BoardDto.CommentResDto createComment(BoardDto.CommentCreateReqDto param, Long reqUserId);
    BoardDto.CommentResDto updateComment(BoardDto.CommentUpdateReqDto param, Long reqUserId);
    void deleteComment(DefaultDto.DeleteReqDto param, Long reqUserId);
    List<BoardDto.CommentResDto> listComments(DefaultDto.DetailReqDto param, Long reqUserId);
    List<BoardDto.DetailResDto> list(BoardDto.ListReqDto param, Long reqUserId);
    DefaultDto.PagedListResDto pagedList(BoardDto.PagedListReqDto param, Long reqUserId);
    List<BoardDto.DetailResDto> scrollList(BoardDto.ScrollListReqDto param, Long reqUserId);
}
