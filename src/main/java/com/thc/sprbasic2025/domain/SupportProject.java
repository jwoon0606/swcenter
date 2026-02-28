package com.thc.sprbasic2025.domain;

import com.thc.sprbasic2025.dto.DefaultDto;
import com.thc.sprbasic2025.dto.SupportProjectDto;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;

@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@Table(indexes = {@Index(columnList = "deleted"), @Index(columnList = "sequence")})
@Entity
public class SupportProject extends AuditingFields {
    Integer sequence;
    String title;

    @Column(columnDefinition = "TEXT")
    String content;

    String url;
    String img;
    LocalDate publishedDate;

    protected SupportProject() {}

    private SupportProject(Integer sequence, String title, String content, String url, String img, LocalDate publishedDate) {
        this.sequence = sequence;
        this.title = title;
        this.content = content;
        this.url = url;
        this.img = img;
        this.publishedDate = publishedDate;
    }

    public static SupportProject of(Integer sequence, String title, String content, String url, String img, LocalDate publishedDate) {
        return new SupportProject(sequence, title, content, url, img, publishedDate);
    }

    public DefaultDto.CreateResDto toCreateResDto() {
        return DefaultDto.CreateResDto.builder().id(getId()).build();
    }

    public void update(SupportProjectDto.UpdateReqDto param) {
        if (param.getDeleted() != null) { setDeleted(param.getDeleted()); }
        if (param.getSequence() != null) { setSequence(param.getSequence()); }
        if (param.getTitle() != null) { setTitle(param.getTitle()); }
        if (param.getContent() != null) { setContent(param.getContent()); }
        if (param.getUrl() != null) { setUrl(param.getUrl()); }
        if (param.getImg() != null) { setImg(param.getImg()); }
        if (param.getPublishedDate() != null) { setPublishedDate(param.getPublishedDate()); }
    }
}
