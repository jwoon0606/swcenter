package com.thc.sprbasic2025.dto;

import com.thc.sprbasic2025.domain.Newsletter;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

public class NewsletterDto {
    public static final String DEFAULT_DETAIL_URL = "https://walab.info/swplus/newsletter/2212_v14/newsletter_v14.html";

    @Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
    public static class CreateReqDto {
        Integer vol;
        String title;
        String summary;
        String content;
        String tags;
        String category;
        String img;
        String detailUrl;
        LocalDate publishedDate;

        private MultipartFile file;

        public Newsletter toEntity() {
            return Newsletter.of(
                    getVol(),
                    getTitle(),
                    getSummary(),
                    getContent(),
                    getTags(),
                    getCategory(),
                    getImg(),
                    getDetailUrl(),
                    getPublishedDate()
            );
        }
    }

    @Getter @Setter @SuperBuilder @NoArgsConstructor @AllArgsConstructor
    public static class UpdateReqDto extends DefaultDto.UpdateReqDto {
        Integer vol;
        String title;
        String summary;
        String content;
        String tags;
        String category;
        String img;
        String detailUrl;
        LocalDate publishedDate;
    }

    @Getter @Setter @SuperBuilder @NoArgsConstructor @AllArgsConstructor
    public static class DetailResDto extends DefaultDto.DetailResDto {
        Integer vol;
        String title;
        String summary;
        String content;
        String tags;
        String category;
        String img;
        String detailUrl;
        LocalDate publishedDate;
    }

    @Getter @Setter @SuperBuilder @NoArgsConstructor @AllArgsConstructor
    public static class ScrollListReqDto extends DefaultDto.ScrollListReqDto {
        String category;
    }

    @Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
    public static class SubscribeReqDto {
        String name;
        String email;
        Boolean agreePrivacy;
    }

    @Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
    public static class SubscribeResDto {
        Long id;
        Boolean created;
        Boolean subscribed;
    }
}
