package com.thc.sprbasic2025.controller;

import com.thc.sprbasic2025.service.NewsletterPageViewCountService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/views")
public class NewsletterPageViewCountRestController {

    private final NewsletterPageViewCountService newsletterPageViewCountService;

    public NewsletterPageViewCountRestController(NewsletterPageViewCountService newsletterPageViewCountService) {
        this.newsletterPageViewCountService = newsletterPageViewCountService;
    }

    // 조회수 증가 + 반환
    @GetMapping("/increase")
    public Map<String, Object> increaseView(
            @RequestParam String targetType,
            @RequestParam String targetKey
    ) {
        Long viewCount = newsletterPageViewCountService.increaseViewCount(targetType, targetKey);

        Map<String, Object> result = new HashMap<>();
        result.put("targetType", targetType);
        result.put("targetKey", targetKey);
        result.put("viewCount", viewCount);

        return result;
    }

    // 조회만
    @GetMapping
    public Map<String, Object> getView(
            @RequestParam String targetType,
            @RequestParam String targetKey
    ) {
        Long viewCount = newsletterPageViewCountService.getViewCount(targetType, targetKey);

        Map<String, Object> result = new HashMap<>();
        result.put("targetType", targetType);
        result.put("targetKey", targetKey);
        result.put("viewCount", viewCount);

        return result;
    }
}