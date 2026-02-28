package com.thc.sprbasic2025.controller;

import com.thc.sprbasic2025.dto.DefaultDto;
import com.thc.sprbasic2025.dto.NewsletterDto;
import com.thc.sprbasic2025.security.PrincipalDetails;
import com.thc.sprbasic2025.service.NewsletterService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;
import java.util.List;

@RequiredArgsConstructor
@RequestMapping("/api/newsletter")
@RestController
public class NewsletterRestController {
    final NewsletterService newsletterService;

    public Long getReqUserId(PrincipalDetails principalDetails) {
        if (principalDetails == null || principalDetails.getUser() == null || principalDetails.getUser().getId() == null) {
            return null;
        }
        return principalDetails.getUser().getId();
    }

    @PreAuthorize("hasRole('USER')")
    @PostMapping(value = "", consumes = "multipart/form-data")
    public ResponseEntity<DefaultDto.CreateResDto> create(
            @RequestPart NewsletterDto.CreateReqDto params,
            @RequestPart(required = false) MultipartFile file,
            @AuthenticationPrincipal PrincipalDetails principalDetails
    ) {
        Long reqUserId = getReqUserId(principalDetails);
        params.setFile(file);
        return ResponseEntity.ok(newsletterService.create(params, reqUserId));
    }

    @PreAuthorize("hasRole('USER')")
    @PostMapping(value = "", consumes = "application/json")
    public ResponseEntity<DefaultDto.CreateResDto> createByJson(
            @RequestBody NewsletterDto.CreateReqDto params,
            @AuthenticationPrincipal PrincipalDetails principalDetails
    ) {
        Long reqUserId = getReqUserId(principalDetails);
        return ResponseEntity.ok(newsletterService.create(params, reqUserId));
    }

    @PreAuthorize("hasRole('USER')")
    @PutMapping("")
    public ResponseEntity<Void> update(
            @RequestBody NewsletterDto.UpdateReqDto params,
            @AuthenticationPrincipal PrincipalDetails principalDetails
    ) {
        Long reqUserId = getReqUserId(principalDetails);
        newsletterService.update(params, reqUserId);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @PreAuthorize("hasRole('USER')")
    @DeleteMapping("")
    public ResponseEntity<Void> delete(
            @RequestBody DefaultDto.DeleteReqDto params,
            @AuthenticationPrincipal PrincipalDetails principalDetails
    ) {
        Long reqUserId = getReqUserId(principalDetails);
        newsletterService.delete(params, reqUserId);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @PreAuthorize("hasRole('USER')")
    @DeleteMapping("/list")
    public ResponseEntity<Void> deleteList(
            @RequestBody DefaultDto.DeleteListReqDto params,
            @AuthenticationPrincipal PrincipalDetails principalDetails
    ) {
        Long reqUserId = getReqUserId(principalDetails);
        newsletterService.deleteList(params, reqUserId);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @PreAuthorize("permitAll()")
    @GetMapping("")
    public ResponseEntity<NewsletterDto.DetailResDto> detail(
            DefaultDto.DetailReqDto params,
            @AuthenticationPrincipal PrincipalDetails principalDetails
    ) {
        Long reqUserId = getReqUserId(principalDetails);
        return ResponseEntity.ok(newsletterService.detail(params, reqUserId));
    }

    @PreAuthorize("permitAll()")
    @GetMapping("/scrollList")
    public ResponseEntity<List<NewsletterDto.DetailResDto>> scrollList(
            NewsletterDto.ScrollListReqDto params,
            @AuthenticationPrincipal PrincipalDetails principalDetails
    ) {
        Long reqUserId = getReqUserId(principalDetails);
        return ResponseEntity.ok(newsletterService.scrollList(params, reqUserId));
    }

    @PreAuthorize("permitAll()")
    @PostMapping("/subscribe")
    public ResponseEntity<NewsletterDto.SubscribeResDto> subscribe(
            @RequestBody NewsletterDto.SubscribeReqDto params
    ) {
        return ResponseEntity.ok(newsletterService.subscribe(params));
    }

    @PreAuthorize("permitAll()")
    @GetMapping("/unsubscribe")
    public ResponseEntity<Void> unsubscribeByToken(@RequestParam String token) {
        NewsletterDto.UnsubscribeResDto res = newsletterService.unsubscribeByToken(token);
        String status = (res != null && Boolean.TRUE.equals(res.getUnsubscribed())) ? "success" : "invalid";
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create("/news/unsubscribe_result?status=" + status))
                .build();
    }

    @PreAuthorize("hasRole('USER')")
    @PostMapping("/send")
    public ResponseEntity<NewsletterDto.SendResDto> sendToSubscribers(
            @RequestBody DefaultDto.DetailReqDto params,
            @AuthenticationPrincipal PrincipalDetails principalDetails
    ) {
        Long reqUserId = getReqUserId(principalDetails);
        return ResponseEntity.ok(newsletterService.sendToSubscribers(params, reqUserId));
    }
}
