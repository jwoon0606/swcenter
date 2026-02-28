package com.thc.sprbasic2025.service.impl;

import com.thc.sprbasic2025.domain.Board;
import com.thc.sprbasic2025.domain.BoardComment;
import com.thc.sprbasic2025.domain.User;
import com.thc.sprbasic2025.dto.DefaultDto;
import com.thc.sprbasic2025.dto.BoardDto;
import com.thc.sprbasic2025.dto.PermissionDto;
import com.thc.sprbasic2025.exception.NoMatchingDataException;
import com.thc.sprbasic2025.exception.NoPermissionException;
import com.thc.sprbasic2025.mapper.BoardMapper;
import com.thc.sprbasic2025.repository.BoardCommentRepository;
import com.thc.sprbasic2025.repository.BoardRepository;
import com.thc.sprbasic2025.repository.UserRepository;
import com.thc.sprbasic2025.service.BoardService;
import com.thc.sprbasic2025.service.PermittedService;
import com.thc.sprbasic2025.util.FileUpload;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RequiredArgsConstructor
@Service
public class BoardServiceimpl implements BoardService {
    private static final Set<String> ALLOWED_SUBJECTS = new HashSet<>(Arrays.asList("일상", "자유", "고민"));
    private static final String DEFAULT_SUBJECT = "자유";

    final String target = "board";

    final BoardRepository boardRepository;
    final BoardCommentRepository boardCommentRepository;
    final UserRepository userRepository;
    final BoardMapper boardMapper;
    final PermittedService permittedService;


    @Override
    public DefaultDto.CreateResDto create(BoardDto.CreateReqDto param, Long reqUserId) {
        //permittedService.isPermitted(reqUserId, target, 110);
        try{
            if(param.getFile() != null){
                param.setImg("/image/" + FileUpload.upload(param.getFile()));
            }
        } catch (Exception e){}

        param.setSubject(normalizeSubject(param.getSubject()));
        param.setAnonymous(Boolean.TRUE.equals(param.getAnonymous()));

        // 본인 아이디로 강제 지정!
        if(param.getUserId() == null || param.getUserId() == (long) 0){
            param.setUserId(reqUserId);
        }
        if(!param.getUserId().equals(reqUserId)){
            // 입력하려는 userId가 본인 정보가 아닐때!!
            permittedService.isPermitted(reqUserId, target, 110);
        }
        DefaultDto.CreateResDto res = boardRepository.save(param.toEntity()).toCreateResDto();
        return res;
    }

    @Override
    public void update(BoardDto.UpdateReqDto param, Long reqUserId) {
        Board board = boardRepository.findById(param.getId()).orElseThrow(() -> new NoMatchingDataException("no data"));
        if(!board.getUserId().equals(reqUserId)){
            // 본인 글이 아닐때는 권한 있는 사람만 수정 가능!
            permittedService.isPermitted(reqUserId, target, 120);
        }
        if (param.getSubject() != null) {
            param.setSubject(normalizeSubject(param.getSubject()));
        }

        board.update(param);
        boardRepository.save(board);
    }

    @Override
    public void delete(DefaultDto.DeleteReqDto param, Long reqUserId) {
        update(BoardDto.UpdateReqDto.builder().id(param.getId()).deleted(true).build(), reqUserId);
    }
    @Override
    public void deleteList(DefaultDto.DeleteListReqDto param, Long reqUserId) {
        for(Long id : param.getIds()){
            delete(DefaultDto.DeleteReqDto.builder().id(id).build(), reqUserId);
        }
    }

    public BoardDto.DetailResDto get(DefaultDto.DetailReqDto param, Long reqUserId) {
        //permittedService.isPermitted(reqUserId, target, 200);
        BoardDto.DetailResDto res = boardMapper.detail(param.getId());
        if (res == null) {
            throw new NoMatchingDataException("no data");
        }
        if (res.getViewCount() == null) {
            res.setViewCount(res.getCountread() == null ? 0 : res.getCountread());
        }
        if (res.getLikeCount() == null) {
            res.setLikeCount(0);
        }
        if (res.getSubject() == null || res.getSubject().trim().isEmpty()) {
            res.setSubject(extractLegacySubjectFromTitle(res.getTitle()));
        }
        if (res.getAnonymous() == null) {
            res.setAnonymous(false);
        }
        res.setCanUpdate(canManageBoard(reqUserId, res.getUserId()));
        return res;
    }
    @Override
    public BoardDto.DetailResDto detail(DefaultDto.DetailReqDto param, Long reqUserId) {
        return get(param, reqUserId);
    }

    @Override
    public BoardDto.MetricsResDto increaseViewCount(DefaultDto.DetailReqDto param, Long reqUserId) {
        Board board = boardRepository.findById(param.getId()).orElseThrow(() -> new NoMatchingDataException("no data"));
        Integer currentViewCount = board.getCountread();
        if (currentViewCount == null) {
            currentViewCount = 0;
        }
        board.setCountread(currentViewCount + 1);
        boardRepository.save(board);
        return BoardDto.MetricsResDto.builder()
                .viewCount(board.getCountread())
                .likeCount(board.getLikeCount() == null ? 0 : board.getLikeCount())
                .build();
    }

    @Override
    public BoardDto.MetricsResDto updateLikeCount(BoardDto.LikeReqDto param, Long reqUserId) {
        Board board = boardRepository.findById(param.getId()).orElseThrow(() -> new NoMatchingDataException("no data"));
        Integer currentLikeCount = board.getLikeCount();
        if (currentLikeCount == null) {
            currentLikeCount = 0;
        }
        boolean liked = Boolean.TRUE.equals(param.getLiked());
        if (liked) {
            currentLikeCount += 1;
        } else {
            currentLikeCount = Math.max(0, currentLikeCount - 1);
        }
        board.setLikeCount(currentLikeCount);
        boardRepository.save(board);
        return BoardDto.MetricsResDto.builder()
                .viewCount(board.getCountread() == null ? 0 : board.getCountread())
                .likeCount(board.getLikeCount())
                .build();
    }

    @Override
    public BoardDto.CommentResDto createComment(BoardDto.CommentCreateReqDto param, Long reqUserId) {
        if (reqUserId == null || reqUserId <= 0) {
            throw new RuntimeException("login required");
        }
        if (param.getContent() == null || param.getContent().trim().isEmpty()) {
            throw new RuntimeException("empty comment");
        }
        BoardComment comment = boardCommentRepository.save(
                BoardComment.of(param.getBoardId(), reqUserId, param.getContent().trim())
        );
        User user = userRepository.findById(reqUserId).orElse(null);
        return BoardDto.CommentResDto.builder()
                .id(comment.getId())
                .boardId(comment.getBoardId())
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
    public BoardDto.CommentResDto updateComment(BoardDto.CommentUpdateReqDto param, Long reqUserId) {
        if (reqUserId == null || reqUserId <= 0) {
            throw new RuntimeException("login required");
        }
        if (param.getContent() == null || param.getContent().trim().isEmpty()) {
            throw new RuntimeException("empty comment");
        }
        BoardComment comment = boardCommentRepository.findById(param.getId()).orElseThrow(() -> new NoMatchingDataException("no data"));
        if (Boolean.TRUE.equals(comment.getDeleted())) {
            throw new NoMatchingDataException("no data");
        }
        if (!canManageComment(reqUserId, comment)) {
            throw new NoPermissionException("no auth");
        }
        comment.setContent(param.getContent().trim());
        BoardComment saved = boardCommentRepository.save(comment);
        User user = userRepository.findById(saved.getUserId()).orElse(null);
        boolean editable = canManageComment(reqUserId, saved);
        return BoardDto.CommentResDto.builder()
                .id(saved.getId())
                .boardId(saved.getBoardId())
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
        BoardComment comment = boardCommentRepository.findById(param.getId()).orElseThrow(() -> new NoMatchingDataException("no data"));
        if (Boolean.TRUE.equals(comment.getDeleted())) {
            return;
        }
        if (!canManageComment(reqUserId, comment)) {
            throw new NoPermissionException("no auth");
        }
        comment.setDeleted(true);
        boardCommentRepository.save(comment);
    }

    @Override
    public List<BoardDto.CommentResDto> listComments(DefaultDto.DetailReqDto param, Long reqUserId) {
        List<BoardComment> comments = boardCommentRepository.findByBoardIdAndDeletedFalseOrderByIdAsc(param.getId());
        List<BoardDto.CommentResDto> result = new ArrayList<>();
        for (BoardComment comment : comments) {
            User user = userRepository.findById(comment.getUserId()).orElse(null);
            result.add(BoardDto.CommentResDto.builder()
                    .id(comment.getId())
                    .boardId(comment.getBoardId())
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
    public List<BoardDto.DetailResDto> list(BoardDto.ListReqDto param, Long reqUserId) {
        return detailList(boardMapper.list(param), reqUserId);
    }
    public List<BoardDto.DetailResDto> detailList(List<BoardDto.DetailResDto> list, Long reqUserId){
        List<BoardDto.DetailResDto> newList = new ArrayList<>();
        for(BoardDto.DetailResDto each : list){
            newList.add(get(DefaultDto.DetailReqDto.builder().id(each.getId()).build(), reqUserId));
        }
        return newList;
    }

    @Override
    public DefaultDto.PagedListResDto pagedList(BoardDto.PagedListReqDto param, Long reqUserId) {
        DefaultDto.PagedListResDto res = param.init(boardMapper.pagedListCount(param));
        res.setList(detailList(boardMapper.pagedList(param), reqUserId));
        return res;
    }

    @Override
    public List<BoardDto.DetailResDto> scrollList(BoardDto.ScrollListReqDto param, Long reqUserId) {
        param.init();

        //타이틀 로 스크롤 더 요청하는 경우 어쩔수 없이 작업!
        if("title".equals(param.getOrderby())){
            Long cursor = param.getCursor();
            if(cursor != null){
                BoardDto.DetailResDto board = boardMapper.detail(cursor);
                if(board != null){
                    param.setMark(board.getTitle() + "_" + board.getId());
                }
            }
        }
        return detailList(boardMapper.scrollList(param), reqUserId);
    }

    private boolean canManageBoard(Long reqUserId, Long authorUserId) {
        if (reqUserId == null || reqUserId <= 0) {
            return false;
        }
        if (authorUserId != null && reqUserId.equals(authorUserId)) {
            return true;
        }
        return permittedService.permitted(PermissionDto.PermittedReqDto.builder()
                .userId(reqUserId)
                .target(target)
                .func(120)
                .build());
    }

    private boolean canManageComment(Long reqUserId, BoardComment comment) {
        if (reqUserId == null || reqUserId <= 0 || comment == null) {
            return false;
        }
        if (reqUserId.equals(comment.getUserId())) {
            return true;
        }
        Board board = boardRepository.findById(comment.getBoardId()).orElse(null);
        return canManageBoard(reqUserId, board == null ? null : board.getUserId());
    }

    private String normalizeSubject(String rawSubject) {
        if (rawSubject == null) {
            return DEFAULT_SUBJECT;
        }
        String subject = rawSubject.trim();
        if (ALLOWED_SUBJECTS.contains(subject)) {
            return subject;
        }
        return DEFAULT_SUBJECT;
    }

    private String extractLegacySubjectFromTitle(String rawTitle) {
        if (rawTitle == null) {
            return DEFAULT_SUBJECT;
        }
        String title = rawTitle.trim();
        if (!title.startsWith("[")) {
            return DEFAULT_SUBJECT;
        }
        int closeIdx = title.indexOf("]");
        if (closeIdx <= 1) {
            return DEFAULT_SUBJECT;
        }
        String candidate = title.substring(1, closeIdx).trim();
        if (ALLOWED_SUBJECTS.contains(candidate)) {
            return candidate;
        }
        return DEFAULT_SUBJECT;
    }
}
