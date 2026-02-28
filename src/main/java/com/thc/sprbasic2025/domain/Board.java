package com.thc.sprbasic2025.domain;

import com.thc.sprbasic2025.dto.DefaultDto;
import com.thc.sprbasic2025.dto.BoardDto;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@Table(indexes = {@Index(columnList = "deleted")})
@Entity
public class Board extends AuditingFields{
    Long userId;

    String subject;
    Boolean anonymous;

    String title;
    String content;
    String img;

    Integer countread;
    Integer likeCount;

    protected Board(){}
    private Board(Long userId, String subject, Boolean anonymous, String title, String content, String img, Integer countread, Integer likeCount) {
        this.userId = userId;
        this.subject = subject;
        this.anonymous = anonymous;
        this.title = title;
        this.content = content;
        this.img = img;
        this.countread = countread;
        this.likeCount = likeCount;
    }
    public static Board of(Long userId, String subject, Boolean anonymous, String title, String content){
        return new Board(userId, subject, anonymous, title, content, null, 0, 0);
    }
    public static Board of(Long userId, String subject, Boolean anonymous, String title, String content, String img){
        return new Board(userId, subject, anonymous, title, content, img, 0, 0);
    }
    public DefaultDto.CreateResDto toCreateResDto() {
        return DefaultDto.CreateResDto.builder().id(getId()).build();
    }

    public void update(BoardDto.UpdateReqDto param) {
        if(param.getDeleted() != null){ setDeleted(param.getDeleted()); }
        if(param.getSubject() != null){ setSubject(param.getSubject()); }
        if(param.getAnonymous() != null){ setAnonymous(param.getAnonymous()); }
        if(param.getTitle() != null){ setTitle(param.getTitle()); }
        if(param.getContent() != null){ setContent(param.getContent()); }
        if(param.getImg() != null){ setImg(param.getImg()); }
        if(param.getCountread() != null){ setCountread(param.getCountread()); }
        if(param.getLikeCount() != null){ setLikeCount(param.getLikeCount()); }
    }
}
