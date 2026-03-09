package com.thc.sprbasic2025.dto;

import com.thc.sprbasic2025.domain.ExtracurricularActivity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

public class ExtracurricularActivityDto {
    public static final String DEFAULT_DETAIL_URL = "https://walab.info/swplus/newsletter/2212_v14/newsletter_v14.html";

    @Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
    public static class CreateReqDto {
        String title;
        String summary;
        String img;
        String detailUrl;

        public ExtracurricularActivity toEntity() {
            return ExtracurricularActivity.of(
                    getTitle(),
                    getSummary(),
                    getImg(),
                    getDetailUrl()
            );
        }
    }

    @Getter @Setter @SuperBuilder @NoArgsConstructor @AllArgsConstructor
    public static class UpdateReqDto extends DefaultDto.UpdateReqDto {
        String title;
        String summary;
        String img;
        String detailUrl;
    }

    @Getter @Setter @SuperBuilder @NoArgsConstructor @AllArgsConstructor
    public static class DetailResDto extends DefaultDto.DetailResDto {
        String title;
        String summary;
        String img;
        String detailUrl;
    }

    @Getter @Setter @SuperBuilder @NoArgsConstructor
    public static class ScrollListReqDto extends DefaultDto.ScrollListReqDto {}

}
