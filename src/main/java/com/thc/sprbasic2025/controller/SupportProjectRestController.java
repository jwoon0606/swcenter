package com.thc.sprbasic2025.controller;

import com.thc.sprbasic2025.dto.DefaultDto;
import com.thc.sprbasic2025.dto.SupportProjectDto;
import com.thc.sprbasic2025.security.PrincipalDetails;
import com.thc.sprbasic2025.service.SupportProjectService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RequiredArgsConstructor
@RequestMapping("/api/support_project")
@RestController
public class SupportProjectRestController {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    final SupportProjectService supportProjectService;

    @PreAuthorize("hasRole('USER')")
    @PutMapping("/sequence")
    public ResponseEntity<SupportProjectDto.SequenceResDto> sequence(
            @RequestBody SupportProjectDto.SequenceReqDto params,
            @AuthenticationPrincipal PrincipalDetails principalDetails
    ) {
        Long reqUserId = getReqUserId(principalDetails);
        return ResponseEntity.ok(supportProjectService.sequence(params, reqUserId));
    }

    @PreAuthorize("hasRole('USER')")
    @PostMapping("/seedSamples")
    public ResponseEntity<SupportProjectDto.SeedResDto> seedSamples(
            @AuthenticationPrincipal PrincipalDetails principalDetails
    ) {
        Long reqUserId = getReqUserId(principalDetails);
        return ResponseEntity.ok(supportProjectService.seedSamples(reqUserId));
    }

    public Long getReqUserId(PrincipalDetails principalDetails) {
        if (principalDetails == null || principalDetails.getUser() == null || principalDetails.getUser().getId() == null) {
            return null;
        }
        return principalDetails.getUser().getId();
    }

    @PreAuthorize("hasRole('USER')")
    @PostMapping("")
    public ResponseEntity<DefaultDto.CreateResDto> create(
            @RequestPart SupportProjectDto.CreateReqDto params,
            @RequestPart(required = false) MultipartFile file,
            @AuthenticationPrincipal PrincipalDetails principalDetails
    ) {
        Long reqUserId = getReqUserId(principalDetails);
        params.setFile(file);
        return ResponseEntity.ok(supportProjectService.create(params, reqUserId));
    }

    @PreAuthorize("hasRole('USER')")
    @PutMapping("")
    public ResponseEntity<Void> update(
            @RequestBody SupportProjectDto.UpdateReqDto params,
            @AuthenticationPrincipal PrincipalDetails principalDetails
    ) {
        Long reqUserId = getReqUserId(principalDetails);
        supportProjectService.update(params, reqUserId);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @PreAuthorize("hasRole('USER')")
    @DeleteMapping("")
    public ResponseEntity<Void> delete(
            @RequestBody DefaultDto.DeleteReqDto params,
            @AuthenticationPrincipal PrincipalDetails principalDetails
    ) {
        Long reqUserId = getReqUserId(principalDetails);
        supportProjectService.delete(params, reqUserId);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @PreAuthorize("hasRole('USER')")
    @DeleteMapping("/list")
    public ResponseEntity<Void> deleteList(
            @RequestBody DefaultDto.DeleteListReqDto params,
            @AuthenticationPrincipal PrincipalDetails principalDetails
    ) {
        Long reqUserId = getReqUserId(principalDetails);
        supportProjectService.deleteList(params, reqUserId);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @PreAuthorize("permitAll()")
    @GetMapping("")
    public ResponseEntity<SupportProjectDto.DetailResDto> detail(
            DefaultDto.DetailReqDto params,
            @AuthenticationPrincipal PrincipalDetails principalDetails
    ) {
        Long reqUserId = getReqUserId(principalDetails);
        return ResponseEntity.ok(supportProjectService.detail(params, reqUserId));
    }

    @PreAuthorize("permitAll()")
    @GetMapping("/list")
    public ResponseEntity<List<SupportProjectDto.DetailResDto>> list(
            SupportProjectDto.ListReqDto params,
            @AuthenticationPrincipal PrincipalDetails principalDetails
    ) {
        Long reqUserId = getReqUserId(principalDetails);
        return ResponseEntity.ok(supportProjectService.list(params, reqUserId));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/pagedList")
    public ResponseEntity<DefaultDto.PagedListResDto> pagedList(
            SupportProjectDto.PagedListReqDto params,
            @AuthenticationPrincipal PrincipalDetails principalDetails
    ) {
        Long reqUserId = getReqUserId(principalDetails);
        logger.info("reqUserId : " + reqUserId);
        return ResponseEntity.ok(supportProjectService.pagedList(params, reqUserId));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/scrollList")
    public ResponseEntity<List<SupportProjectDto.DetailResDto>> scrollList(
            SupportProjectDto.ScrollListReqDto params,
            @AuthenticationPrincipal PrincipalDetails principalDetails
    ) {
        Long reqUserId = getReqUserId(principalDetails);
        return ResponseEntity.ok(supportProjectService.scrollList(params, reqUserId));
    }
}
