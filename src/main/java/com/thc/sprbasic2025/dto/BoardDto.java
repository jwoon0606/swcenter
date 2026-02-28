package com.thc.sprbasic2025.dto;

import com.thc.sprbasic2025.domain.Board;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;

public class BoardDto {

    /**/

    @Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
    public static class CreateReqDto {
        Long userId;
        String subject;
        Boolean anonymous;
        String title;
        String content;
        String img;

        private MultipartFile file;

        public Board toEntity(){
            return Board.of(userId, subject, anonymous, title, content, img);
        }
    }

    @Getter @Setter @SuperBuilder @NoArgsConstructor @AllArgsConstructor
    public static class UpdateReqDto extends DefaultDto.UpdateReqDto{
        String subject;
        Boolean anonymous;
        String title;
        String content;
        String img;
        Integer countread;
        Integer likeCount;
    }

    @Getter @Setter @SuperBuilder @NoArgsConstructor @AllArgsConstructor
    public static class DetailResDto extends DefaultDto.DetailResDto{
        Long userId;
        String subject;
        Boolean anonymous;
        String title;
        String content;
        String img;
        Integer countread;
        Integer viewCount;
        Integer likeCount;
        Boolean canUpdate;

        String userUsername;
        String userNick;
        String userImg;
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
        Long boardId;
        String content;
    }

    @Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
    public static class CommentUpdateReqDto{
        Long id;
        String content;
    }

    @Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
    public static class CommentResDto{
        Long id;
        Long boardId;
        Long userId;
        String userNick;
        String userEmail;
        String content;
        LocalDateTime createdAt;
        Boolean editable;
        Boolean deletable;
    }

    @Getter @Setter @SuperBuilder @NoArgsConstructor @AllArgsConstructor
    public static class ListReqDto extends DefaultDto.ListReqDto{
        String subject;
        String title;
    }
    @Getter @Setter @SuperBuilder @NoArgsConstructor @AllArgsConstructor
    public static class PagedListReqDto extends DefaultDto.PagedListReqDto{
        String subject;
        String title;
    }
    @Getter @Setter @SuperBuilder @NoArgsConstructor @AllArgsConstructor
    public static class ScrollListReqDto extends DefaultDto.ScrollListReqDto{
        String subject;
        String title;
    }
}
