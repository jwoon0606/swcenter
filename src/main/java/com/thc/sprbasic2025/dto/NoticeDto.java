package com.thc.sprbasic2025.dto;

import com.thc.sprbasic2025.domain.Notice;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.time.LocalDateTime;

public class NoticeDto {

    /**/

    @Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
    public static class CreateReqDto {
        String title;
        String content;
        String img;

        private MultipartFile file;

        public Notice toEntity(){
            return Notice.of(getTitle(), getContent(), getImg());
        }
    }

    @Getter @Setter @SuperBuilder @NoArgsConstructor @AllArgsConstructor
    public static class UpdateReqDto extends DefaultDto.UpdateReqDto{
        String title;
        String content;
        String img;
        Integer viewCount;
        Integer likeCount;
    }

    @Getter @Setter @SuperBuilder @NoArgsConstructor @AllArgsConstructor
    public static class DetailResDto extends DefaultDto.DetailResDto{
        String title;
        String content;
        String img;
        Integer viewCount;
        Integer likeCount;
        Boolean canUpdate;
    }

    @Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
    public static class LikeReqDto{
        Long id;
        Boolean liked;
    }

    @Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
    public static class MetricsResDto{
        Integer viewCount;
        Integer likeCount;
    }

    @Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
    public static class CommentCreateReqDto{
        Long noticeId;
        String content;
    }

    @Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
    public static class CommentResDto{
        Long id;
        Long noticeId;
        Long userId;
        String userNick;
        String userEmail;
        String content;
        LocalDateTime createdAt;
        Boolean editable;
        Boolean deletable;
    }

    @Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
    public static class CommentUpdateReqDto{
        Long id;
        String content;
    }

    @Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
    public static class ReportReqDto{
        Long noticeId;
        String content;
    }

    @Getter @Setter @SuperBuilder @NoArgsConstructor @AllArgsConstructor
    public static class ListReqDto extends DefaultDto.ListReqDto{
        String title;
        String searchType;
    }
    @Getter @Setter @SuperBuilder @NoArgsConstructor @AllArgsConstructor
    public static class PagedListReqDto extends DefaultDto.PagedListReqDto{
        String title;
        String searchType;
    }
    @Getter @Setter @SuperBuilder @NoArgsConstructor @AllArgsConstructor
    public static class ScrollListReqDto extends DefaultDto.ScrollListReqDto{
        String title;
        String searchType;
    }
}
