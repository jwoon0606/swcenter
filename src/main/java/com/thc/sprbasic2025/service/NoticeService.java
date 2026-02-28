package com.thc.sprbasic2025.service;

import com.thc.sprbasic2025.dto.DefaultDto;
import com.thc.sprbasic2025.dto.NoticeDto;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface NoticeService {
    /**/
    DefaultDto.CreateResDto create(NoticeDto.CreateReqDto param, Long reqUserId);
    void update(NoticeDto.UpdateReqDto param, Long reqUserId);
    void delete(DefaultDto.DeleteReqDto param, Long reqUserId);
    void deleteList(DefaultDto.DeleteListReqDto param, Long reqUserId);
    NoticeDto.DetailResDto detail(DefaultDto.DetailReqDto param, Long reqUserId);
    NoticeDto.MetricsResDto increaseViewCount(DefaultDto.DetailReqDto param, Long reqUserId);
    NoticeDto.MetricsResDto updateLikeCount(NoticeDto.LikeReqDto param, Long reqUserId);
    NoticeDto.CommentResDto createComment(NoticeDto.CommentCreateReqDto param, Long reqUserId);
    NoticeDto.CommentResDto updateComment(NoticeDto.CommentUpdateReqDto param, Long reqUserId);
    void deleteComment(DefaultDto.DeleteReqDto param, Long reqUserId);
    List<NoticeDto.CommentResDto> listComments(DefaultDto.DetailReqDto param, Long reqUserId);
    Boolean report(NoticeDto.ReportReqDto param, Long reqUserId);
    List<NoticeDto.DetailResDto> list(NoticeDto.ListReqDto param, Long reqUserId);
    DefaultDto.PagedListResDto pagedList(NoticeDto.PagedListReqDto param, Long reqUserId);
    List<NoticeDto.DetailResDto> scrollList(NoticeDto.ScrollListReqDto param, Long reqUserId);
}
