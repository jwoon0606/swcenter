package com.thc.sprbasic2025.controller;

import com.thc.sprbasic2025.dto.ExtracurricularActivityDto;
import com.thc.sprbasic2025.dto.DefaultDto;
import com.thc.sprbasic2025.security.PrincipalDetails;
import com.thc.sprbasic2025.service.ExtracurricularActivityService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequiredArgsConstructor
@RequestMapping("/api/extracurricular_activities")
@RestController
public class ExtracurricularActivitiesRestController {
    final ExtracurricularActivityService extracurricularActivityService;

    public Long getReqUserId(PrincipalDetails principalDetails) {
        if (principalDetails == null || principalDetails.getUser() == null || principalDetails.getUser().getId() == null) {
            return null;
        }
        return principalDetails.getUser().getId();
    }

    @PreAuthorize("hasRole('USER')")
    @PostMapping("")
    public ResponseEntity<DefaultDto.CreateResDto> create(
            @RequestBody ExtracurricularActivityDto.CreateReqDto params,
            @AuthenticationPrincipal PrincipalDetails principalDetails
    ) {
        Long reqUserId = getReqUserId(principalDetails);
        return ResponseEntity.ok(extracurricularActivityService.create(params, reqUserId));
    }

    @PreAuthorize("hasRole('USER')")
    @PutMapping("")
    public ResponseEntity<Void> update(
            @RequestBody ExtracurricularActivityDto.UpdateReqDto params,
            @AuthenticationPrincipal PrincipalDetails principalDetails
    ) {
        Long reqUserId = getReqUserId(principalDetails);
        extracurricularActivityService.update(params, reqUserId);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @PreAuthorize("hasRole('USER')")
    @DeleteMapping("")
    public ResponseEntity<Void> delete(
            @RequestBody DefaultDto.DeleteReqDto params,
            @AuthenticationPrincipal PrincipalDetails principalDetails
    ) {
        Long reqUserId = getReqUserId(principalDetails);
        extracurricularActivityService.delete(params, reqUserId);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @PreAuthorize("hasRole('USER')")
    @DeleteMapping("/list")
    public ResponseEntity<Void> deleteList(
            @RequestBody DefaultDto.DeleteListReqDto params,
            @AuthenticationPrincipal PrincipalDetails principalDetails
    ) {
        Long reqUserId = getReqUserId(principalDetails);
        extracurricularActivityService.deleteList(params, reqUserId);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @PreAuthorize("permitAll()")
    @GetMapping("/scrollList")
    public ResponseEntity<List<ExtracurricularActivityDto.DetailResDto>> scrollList(
            ExtracurricularActivityDto.ScrollListReqDto params,
            @AuthenticationPrincipal PrincipalDetails principalDetails
    ) {
        Long reqUserId = getReqUserId(principalDetails);
        return ResponseEntity.ok(extracurricularActivityService.scrollList(params, reqUserId));
    }

    @PreAuthorize("permitAll()")
    @GetMapping("")
    public ResponseEntity<ExtracurricularActivityDto.DetailResDto> detail(
            DefaultDto.DetailReqDto params,
            @AuthenticationPrincipal PrincipalDetails principalDetails
    ) {
        Long reqUserId = getReqUserId(principalDetails);
        return ResponseEntity.ok(extracurricularActivityService.detail(params, reqUserId));
    }
}
