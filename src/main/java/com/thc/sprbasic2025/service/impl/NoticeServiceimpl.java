package com.thc.sprbasic2025.service.impl;

import com.thc.sprbasic2025.domain.Notice;
import com.thc.sprbasic2025.domain.NoticeComment;
import com.thc.sprbasic2025.domain.NoticeReport;
import com.thc.sprbasic2025.domain.User;
import com.thc.sprbasic2025.dto.DefaultDto;
import com.thc.sprbasic2025.dto.NoticeDto;
import com.thc.sprbasic2025.dto.PermissionDto;
import com.thc.sprbasic2025.exception.NoMatchingDataException;
import com.thc.sprbasic2025.exception.NoPermissionException;
import com.thc.sprbasic2025.mapper.NoticeMapper;
import com.thc.sprbasic2025.repository.NoticeCommentRepository;
import com.thc.sprbasic2025.repository.NoticeRepository;
import com.thc.sprbasic2025.repository.NoticeReportRepository;
import com.thc.sprbasic2025.repository.UserRepository;
import com.thc.sprbasic2025.service.NoticeService;
import com.thc.sprbasic2025.service.PermittedService;
import com.thc.sprbasic2025.util.FileUpload;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Service
public class NoticeServiceimpl implements NoticeService {

    final String target = "notice";

    final NoticeRepository noticeRepository;
    final NoticeCommentRepository noticeCommentRepository;
    final NoticeReportRepository noticeReportRepository;
    final UserRepository userRepository;
    final NoticeMapper noticeMapper;
    final PermittedService permittedService;


    @Override
    public DefaultDto.CreateResDto create(NoticeDto.CreateReqDto param, Long reqUserId) {
        permittedService.isPermitted(reqUserId, target, 110);
        try{
            if(param.getFile() != null){
                param.setImg("/image/" + FileUpload.upload(param.getFile()));
            }
        } catch (Exception e){}
        DefaultDto.CreateResDto res = noticeRepository.save(param.toEntity()).toCreateResDto();
        return res;
    }

    @Override
    public void update(NoticeDto.UpdateReqDto param, Long reqUserId) {
        permittedService.isPermitted(reqUserId, target, 120);
        Notice notice = noticeRepository.findById(param.getId()).orElseThrow(() -> new NoMatchingDataException("no data"));
        notice.update(param);
        noticeRepository.save(notice);
    }

    @Override
    public void delete(DefaultDto.DeleteReqDto param, Long reqUserId) {
        update(NoticeDto.UpdateReqDto.builder().id(param.getId()).deleted(true).build(), reqUserId);
    }
    @Override
    public void deleteList(DefaultDto.DeleteListReqDto param, Long reqUserId) {
        for(Long id : param.getIds()){
            delete(DefaultDto.DeleteReqDto.builder().id(id).build(), reqUserId);
        }
    }

    public NoticeDto.DetailResDto get(DefaultDto.DetailReqDto param, Long reqUserId) {
        //permittedService.isPermitted(reqUserId, target, 200);
        NoticeDto.DetailResDto res = noticeMapper.detail(param.getId());
        if (res == null) {
            throw new NoMatchingDataException("no data");
        }
        res.setCanUpdate(canManageNotice(reqUserId));
        return res;
    }
    @Override
    public NoticeDto.DetailResDto detail(DefaultDto.DetailReqDto param, Long reqUserId) {
        return get(param, reqUserId);
    }

    @Override
    public NoticeDto.MetricsResDto increaseViewCount(DefaultDto.DetailReqDto param, Long reqUserId) {
        Notice notice = noticeRepository.findById(param.getId()).orElseThrow(() -> new NoMatchingDataException("no data"));
        Integer currentViewCount = notice.getViewCount();
        if (currentViewCount == null) {
            currentViewCount = 0;
        }
        notice.setViewCount(currentViewCount + 1);
        noticeRepository.save(notice);
        return NoticeDto.MetricsResDto.builder()
                .viewCount(notice.getViewCount())
                .likeCount(notice.getLikeCount() == null ? 0 : notice.getLikeCount())
                .build();
    }

    @Override
    public NoticeDto.MetricsResDto updateLikeCount(NoticeDto.LikeReqDto param, Long reqUserId) {
        Notice notice = noticeRepository.findById(param.getId()).orElseThrow(() -> new NoMatchingDataException("no data"));
        Integer currentLikeCount = notice.getLikeCount();
        if (currentLikeCount == null) {
            currentLikeCount = 0;
        }
        boolean liked = Boolean.TRUE.equals(param.getLiked());
        if (liked) {
            currentLikeCount += 1;
        } else {
            currentLikeCount = Math.max(0, currentLikeCount - 1);
        }
        notice.setLikeCount(currentLikeCount);
        noticeRepository.save(notice);
        return NoticeDto.MetricsResDto.builder()
                .viewCount(notice.getViewCount() == null ? 0 : notice.getViewCount())
                .likeCount(notice.getLikeCount())
                .build();
    }

    @Override
    public NoticeDto.CommentResDto createComment(NoticeDto.CommentCreateReqDto param, Long reqUserId) {
        if (reqUserId == null || reqUserId <= 0) {
            throw new RuntimeException("login required");
        }
        if (param.getContent() == null || param.getContent().trim().isEmpty()) {
            throw new RuntimeException("empty comment");
        }
        NoticeComment comment = noticeCommentRepository.save(
                NoticeComment.of(param.getNoticeId(), reqUserId, param.getContent().trim())
        );
        User user = userRepository.findById(reqUserId).orElse(null);
        return NoticeDto.CommentResDto.builder()
                .id(comment.getId())
                .noticeId(comment.getNoticeId())
                .userId(comment.getUserId())
                .userNick(user == null ? "" : user.getNick())
                .userEmail(user == null ? "" : user.getEmail())
                .content(comment.getContent())
                .createdAt(comment.getCreatedAt())
                .editable(true)
                .deletable(true)
                .build();
    }

    @Override
    public NoticeDto.CommentResDto updateComment(NoticeDto.CommentUpdateReqDto param, Long reqUserId) {
        if (reqUserId == null || reqUserId <= 0) {
            throw new RuntimeException("login required");
        }
        if (param.getContent() == null || param.getContent().trim().isEmpty()) {
            throw new RuntimeException("empty comment");
        }
        NoticeComment comment = noticeCommentRepository.findById(param.getId()).orElseThrow(() -> new NoMatchingDataException("no data"));
        if (Boolean.TRUE.equals(comment.getDeleted())) {
            throw new NoMatchingDataException("no data");
        }
        if (!canManageComment(reqUserId, comment)) {
            throw new NoPermissionException("no auth");
        }
        comment.setContent(param.getContent().trim());
        NoticeComment saved = noticeCommentRepository.save(comment);
        User user = userRepository.findById(saved.getUserId()).orElse(null);
        boolean editable = canManageComment(reqUserId, saved);
        return NoticeDto.CommentResDto.builder()
                .id(saved.getId())
                .noticeId(saved.getNoticeId())
                .userId(saved.getUserId())
                .userNick(user == null ? "" : user.getNick())
                .userEmail(user == null ? "" : user.getEmail())
                .content(saved.getContent())
                .createdAt(saved.getCreatedAt())
                .editable(editable)
                .deletable(editable)
                .build();
    }

    @Override
    public void deleteComment(DefaultDto.DeleteReqDto param, Long reqUserId) {
        if (reqUserId == null || reqUserId <= 0) {
            throw new RuntimeException("login required");
        }
        NoticeComment comment = noticeCommentRepository.findById(param.getId()).orElseThrow(() -> new NoMatchingDataException("no data"));
        if (Boolean.TRUE.equals(comment.getDeleted())) {
            return;
        }
        if (!canManageComment(reqUserId, comment)) {
            throw new NoPermissionException("no auth");
        }
        comment.setDeleted(true);
        noticeCommentRepository.save(comment);
    }

    @Override
    public List<NoticeDto.CommentResDto> listComments(DefaultDto.DetailReqDto param, Long reqUserId) {
        List<NoticeComment> comments = noticeCommentRepository.findByNoticeIdAndDeletedFalseOrderByIdAsc(param.getId());
        List<NoticeDto.CommentResDto> result = new ArrayList<>();
        for (NoticeComment comment : comments) {
            User user = userRepository.findById(comment.getUserId()).orElse(null);
            result.add(NoticeDto.CommentResDto.builder()
                    .id(comment.getId())
                    .noticeId(comment.getNoticeId())
                    .userId(comment.getUserId())
                    .userNick(user == null ? "" : user.getNick())
                    .userEmail(user == null ? "" : user.getEmail())
                    .content(comment.getContent())
                    .createdAt(comment.getCreatedAt())
                    .editable(canManageComment(reqUserId, comment))
                    .deletable(canManageComment(reqUserId, comment))
                    .build());
        }
        return result;
    }

    @Override
    public Boolean report(NoticeDto.ReportReqDto param, Long reqUserId) {
        if (reqUserId == null || reqUserId <= 0) {
            throw new RuntimeException("login required");
        }
        NoticeReport report = noticeReportRepository.findByNoticeIdAndUserId(param.getNoticeId(), reqUserId);
        if (report == null) {
            noticeReportRepository.save(NoticeReport.of(param.getNoticeId(), reqUserId, param.getContent()));
            return true;
        }
        if (Boolean.TRUE.equals(report.getDeleted())) {
            report.setDeleted(false);
            report.setContent(param.getContent());
            noticeReportRepository.save(report);
            return true;
        }
        return false;
    }

    @Override
    public List<NoticeDto.DetailResDto> list(NoticeDto.ListReqDto param, Long reqUserId) {
        return detailList(noticeMapper.list(param), reqUserId);
    }
    public List<NoticeDto.DetailResDto> detailList(List<NoticeDto.DetailResDto> list, Long reqUserId){
        List<NoticeDto.DetailResDto> newList = new ArrayList<>();
        for(NoticeDto.DetailResDto each : list){
            newList.add(get(DefaultDto.DetailReqDto.builder().id(each.getId()).build(), reqUserId));
        }
        return newList;
    }

    @Override
    public DefaultDto.PagedListResDto pagedList(NoticeDto.PagedListReqDto param, Long reqUserId) {
        DefaultDto.PagedListResDto res = param.init(noticeMapper.pagedListCount(param));
        res.setList(detailList(noticeMapper.pagedList(param), reqUserId));
        return res;
    }

    @Override
    public List<NoticeDto.DetailResDto> scrollList(NoticeDto.ScrollListReqDto param, Long reqUserId) {
        param.init();

        //타이틀 로 스크롤 더 요청하는 경우 어쩔수 없이 작업!
        if("title".equals(param.getOrderby())){
            Long cursor = param.getCursor();
            if(cursor != null){
                NoticeDto.DetailResDto notice = noticeMapper.detail(cursor);
                if(notice != null){
                    param.setMark(notice.getTitle() + "_" + notice.getId());
                }
            }
        }
        return detailList(noticeMapper.scrollList(param), reqUserId);
    }

    private boolean canManageNotice(Long reqUserId) {
        if (reqUserId == null || reqUserId <= 0) {
            return false;
        }
        return permittedService.permitted(PermissionDto.PermittedReqDto.builder()
                .userId(reqUserId)
                .target(target)
                .func(120)
                .build());
    }

    private boolean canManageComment(Long reqUserId, NoticeComment comment) {
        if (reqUserId == null || reqUserId <= 0 || comment == null) {
            return false;
        }
        if (reqUserId.equals(comment.getUserId())) {
            return true;
        }
        return canManageNotice(reqUserId);
    }
}
