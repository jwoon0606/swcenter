package com.thc.sprbasic2025.dto;

import com.thc.sprbasic2025.domain.Permission;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.List;

public class PermissionDto {

    public static String[][] targets = {
            {"admin","관리자페이지"}
            , {"permission","권한설정"}
            , {"user","사용자"}
            , {"notice", "공지사항"}
            , {"board", "일반게시판"}
            , {"support_project", "지원 프로젝트"}
            , {"news_letter", "뉴스레터"}
            , {"extracurricular_activities", "대외활동"}
    };

    @NoArgsConstructor @AllArgsConstructor @SuperBuilder @Getter @Setter
    public static class ExistReqDto {
        private Long userId;
        private String target;
        private Integer func;
    }

    @Getter @Setter @SuperBuilder @NoArgsConstructor @AllArgsConstructor
    public static class PermittedReqDto {
        Long userId;
        String target;
        Integer func;
    }

    /**/

    @Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
    public static class CreateReqDto {
        String title;
        String content;

        public Permission toEntity(){
            return Permission.of(getTitle(), getContent());
        }
    }

    @Getter @Setter @SuperBuilder @NoArgsConstructor @AllArgsConstructor
    public static class UpdateReqDto extends DefaultDto.UpdateReqDto{
        String title;
        String content;
    }

    @Getter @Setter @SuperBuilder @NoArgsConstructor @AllArgsConstructor
    public static class DetailResDto extends DefaultDto.DetailResDto{
        String title;
        String content;

        String[][] targets; // 타겟 이름 가져가기!
        List<PermissiondetailDto.DetailResDto> details; // 이 권한이 가진 모든 디테일 가져가기!
    }

    @Getter @Setter @SuperBuilder @NoArgsConstructor @AllArgsConstructor
    public static class ListReqDto extends DefaultDto.ListReqDto{
        String title;
    }
    @Getter @Setter @SuperBuilder @NoArgsConstructor @AllArgsConstructor
    public static class PagedListReqDto extends DefaultDto.PagedListReqDto{
        String title;
    }
    @Getter @Setter @SuperBuilder @NoArgsConstructor @AllArgsConstructor
    public static class ScrollListReqDto extends DefaultDto.ScrollListReqDto{
        String title;
    }
}
