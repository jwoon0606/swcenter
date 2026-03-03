package com.thc.sprbasic2025.controller.page;

import com.thc.sprbasic2025.security.PrincipalDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@RequiredArgsConstructor
@RequestMapping("/news")
@Controller
public class NewsController {
    private Long getReqUserId(PrincipalDetails principalDetails) {
        if (principalDetails == null || principalDetails.getUser() == null) {
            return null;
        }
        return principalDetails.getUser().getId();
    }

    private String resolveNewsTemplate(String page, Long reqUserId) {
        if ("news_letter".equals(page)) {
            return "news/news_letter";
        }
        if ("extracurricular_activities".equals(page)) {
            return "news/extracurricular_activities";
        }
        return "news/" + page;
    }

    @RequestMapping("/{page}")
    public String page(@PathVariable String page, @AuthenticationPrincipal PrincipalDetails principalDetails){
        Long reqUserId = getReqUserId(principalDetails);
        return resolveNewsTemplate(page, reqUserId);
    }

    @RequestMapping("/news_letter/detail/{id}")
    public String newsletterDetail(@PathVariable String id) {
        if (id.startsWith("vol")) {
            int volNo = Integer.parseInt(id.replace("vol", ""));
            return String.format("news/newsletter_vol/newsletter_vol%02d", volNo);
        }
        return "news/news_letter_detail";
    }

    @RequestMapping({"/extracurricular_activities/detail/{id}", "/extracurricular/detail/{id}"})
    public String extracurricularDetail(@PathVariable String id) {
        return "news/extracurricular_activities_detail";
    }

    // alias: /news/newsletter -> /news/news_letter
    @RequestMapping({"/newsletter", "/news_letter.html"})
    public String newsletter(@AuthenticationPrincipal PrincipalDetails principalDetails) {
        Long reqUserId = getReqUserId(principalDetails);
        return resolveNewsTemplate("news_letter", reqUserId);
    }

    // alias: /news/extracurricular -> /news/extracurricular_activities
    @RequestMapping({"/extracurricular", "/extracurricular_activities.html"})
    public String extracurricular(@AuthenticationPrincipal PrincipalDetails principalDetails) {
        Long reqUserId = getReqUserId(principalDetails);
        return resolveNewsTemplate("extracurricular_activities", reqUserId);
    }
}
