package com.thc.sprbasic2025.controller;

import com.thc.sprbasic2025.dto.DefaultDto;
import com.thc.sprbasic2025.dto.NewsletterDto;
import com.thc.sprbasic2025.security.PrincipalDetails;
import com.thc.sprbasic2025.service.NewsletterService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Locale;

@RequiredArgsConstructor
@RequestMapping("/api/extracurricular_activities")
@RestController
public class ExtracurricularActivitiesRestController {
    private static final String EXTRACURRICULAR_CATEGORY = "extracurricular";

    final NewsletterService newsletterService;

    public Long getReqUserId(PrincipalDetails principalDetails) {
        if (principalDetails == null || principalDetails.getUser() == null || principalDetails.getUser().getId() == null) {
            return null;
        }
        return principalDetails.getUser().getId();
    }

    @PreAuthorize("permitAll()")
    @GetMapping("/scrollList")
    public ResponseEntity<List<NewsletterDto.DetailResDto>> scrollList(
            NewsletterDto.ScrollListReqDto params,
            @AuthenticationPrincipal PrincipalDetails principalDetails
    ) {
        Long reqUserId = getReqUserId(principalDetails);
        params.setCategory(EXTRACURRICULAR_CATEGORY);
        return ResponseEntity.ok(newsletterService.scrollList(params, reqUserId));
    }

    @PreAuthorize("permitAll()")
    @GetMapping("")
    public ResponseEntity<NewsletterDto.DetailResDto> detail(
            DefaultDto.DetailReqDto params,
            @AuthenticationPrincipal PrincipalDetails principalDetails
    ) {
        Long reqUserId = getReqUserId(principalDetails);
        NewsletterDto.DetailResDto row = newsletterService.detail(params, reqUserId);
        if (!isExtracurricular(row.getCategory())) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(row);
    }

    private boolean isExtracurricular(String category) {
        if (category == null) {
            return false;
        }
        String normalized = category.trim().toLowerCase(Locale.ROOT);
        return EXTRACURRICULAR_CATEGORY.equals(normalized);
    }
}
