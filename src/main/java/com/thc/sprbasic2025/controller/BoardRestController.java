package com.thc.sprbasic2025.controller;

import com.thc.sprbasic2025.dto.DefaultDto;
import com.thc.sprbasic2025.dto.BoardDto;
import com.thc.sprbasic2025.security.PrincipalDetails;
import com.thc.sprbasic2025.service.BoardService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RequiredArgsConstructor
@RequestMapping("/api/board")
@RestController
public class BoardRestController {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    final BoardService boardService;

    public Long getReqUserId(PrincipalDetails principalDetails){
        if(principalDetails == null || principalDetails.getUser() == null || principalDetails.getUser().getId() == null){
            return null;
        }
        return principalDetails.getUser().getId();
    }

    @PreAuthorize("hasRole('USER')")
    @PostMapping(value = "", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<DefaultDto.CreateResDto> create(@RequestBody BoardDto.CreateReqDto params, @AuthenticationPrincipal PrincipalDetails principalDetails){
        Long reqUserId = getReqUserId(principalDetails);
        return ResponseEntity.ok(boardService.create(params, reqUserId));
    }

    @PreAuthorize("hasRole('USER')")
    @PostMapping(value = "", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DefaultDto.CreateResDto> createMultipart(
            @RequestPart BoardDto.CreateReqDto params
            , @RequestPart(required = false) MultipartFile file
            , @AuthenticationPrincipal PrincipalDetails principalDetails
    ){
        Long reqUserId = getReqUserId(principalDetails);
        params.setFile(file);
        return ResponseEntity.ok(boardService.create(params, reqUserId));
    }

    @PreAuthorize("hasRole('USER')")
    @PutMapping("")
    public ResponseEntity<Void> update(@RequestBody BoardDto.UpdateReqDto params, @AuthenticationPrincipal PrincipalDetails principalDetails){
        Long reqUserId = getReqUserId(principalDetails);
        boardService.update(params, reqUserId);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @PreAuthorize("hasRole('USER')")
    @DeleteMapping("")
    public ResponseEntity<Void> delete(@RequestBody DefaultDto.DeleteReqDto params, @AuthenticationPrincipal PrincipalDetails principalDetails){
        Long reqUserId = getReqUserId(principalDetails);
        boardService.delete(params, reqUserId);
        return ResponseEntity.status(HttpStatus.OK).build();
    }
    @PreAuthorize("hasRole('USER')")
    @DeleteMapping("/list")
    public ResponseEntity<Void> deleteList(@RequestBody DefaultDto.DeleteListReqDto params, @AuthenticationPrincipal PrincipalDetails principalDetails){
        Long reqUserId = getReqUserId(principalDetails);
        boardService.deleteList(params, reqUserId);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @PreAuthorize("permitAll()")
    @GetMapping("")
    public ResponseEntity<BoardDto.DetailResDto> detail(DefaultDto.DetailReqDto params
            , @AuthenticationPrincipal PrincipalDetails principalDetails){
        Long reqUserId = getReqUserId(principalDetails);
        return ResponseEntity.ok(boardService.detail(params, reqUserId));
    }

    @PreAuthorize("permitAll()")
    @PostMapping("/view")
    public ResponseEntity<BoardDto.MetricsResDto> view(@RequestBody DefaultDto.DetailReqDto params
            , @AuthenticationPrincipal PrincipalDetails principalDetails){
        Long reqUserId = getReqUserId(principalDetails);
        return ResponseEntity.ok(boardService.increaseViewCount(params, reqUserId));
    }

    @PreAuthorize("hasRole('USER')")
    @PostMapping("/like")
    public ResponseEntity<BoardDto.MetricsResDto> like(@RequestBody BoardDto.LikeReqDto params
            , @AuthenticationPrincipal PrincipalDetails principalDetails){
        Long reqUserId = getReqUserId(principalDetails);
        return ResponseEntity.ok(boardService.updateLikeCount(params, reqUserId));
    }

    @PreAuthorize("permitAll()")
    @GetMapping("/comment/list")
    public ResponseEntity<List<BoardDto.CommentResDto>> commentList(DefaultDto.DetailReqDto params
            , @AuthenticationPrincipal PrincipalDetails principalDetails){
        Long reqUserId = getReqUserId(principalDetails);
        return ResponseEntity.ok(boardService.listComments(params, reqUserId));
    }

    @PreAuthorize("hasRole('USER')")
    @PostMapping("/comment")
    public ResponseEntity<BoardDto.CommentResDto> comment(@RequestBody BoardDto.CommentCreateReqDto params
            , @AuthenticationPrincipal PrincipalDetails principalDetails){
        Long reqUserId = getReqUserId(principalDetails);
        return ResponseEntity.ok(boardService.createComment(params, reqUserId));
    }

    @PreAuthorize("hasRole('USER')")
    @PutMapping("/comment")
    public ResponseEntity<BoardDto.CommentResDto> commentUpdate(@RequestBody BoardDto.CommentUpdateReqDto params
            , @AuthenticationPrincipal PrincipalDetails principalDetails){
        Long reqUserId = getReqUserId(principalDetails);
        return ResponseEntity.ok(boardService.updateComment(params, reqUserId));
    }

    @PreAuthorize("hasRole('USER')")
    @DeleteMapping("/comment")
    public ResponseEntity<Void> commentDelete(@RequestBody DefaultDto.DeleteReqDto params
            , @AuthenticationPrincipal PrincipalDetails principalDetails){
        Long reqUserId = getReqUserId(principalDetails);
        boardService.deleteComment(params, reqUserId);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    //@PreAuthorize("permitAll()")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/list")
    public ResponseEntity<List<BoardDto.DetailResDto>> list(BoardDto.ListReqDto params
            , @AuthenticationPrincipal PrincipalDetails principalDetails){
        Long reqUserId = getReqUserId(principalDetails);
        return ResponseEntity.ok(boardService.list(params, reqUserId));
    }
    @PreAuthorize("permitAll()")
    @GetMapping("/pagedList")
    public ResponseEntity<DefaultDto.PagedListResDto> pagedList(BoardDto.PagedListReqDto params, @AuthenticationPrincipal PrincipalDetails principalDetails){
        Long reqUserId = getReqUserId(principalDetails);
        logger.info("reqUserId : " + reqUserId);
        return ResponseEntity.ok(boardService.pagedList(params, reqUserId));
    }
    @PreAuthorize("permitAll()")
    @GetMapping("/scrollList")
    public ResponseEntity<List<BoardDto.DetailResDto>> scrollList(BoardDto.ScrollListReqDto params, @AuthenticationPrincipal PrincipalDetails principalDetails){
        Long reqUserId = getReqUserId(principalDetails);
        return ResponseEntity.ok(boardService.scrollList(params, reqUserId));
    }

}
