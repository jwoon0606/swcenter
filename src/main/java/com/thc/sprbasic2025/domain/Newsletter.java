package com.thc.sprbasic2025.domain;

import com.thc.sprbasic2025.dto.DefaultDto;
import com.thc.sprbasic2025.dto.NewsletterDto;
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
@Table(indexes = {
        @Index(columnList = "deleted"),
        @Index(columnList = "category"),
        @Index(columnList = "vol")
})
@Entity
public class Newsletter extends AuditingFields {
    Integer vol;
    String title;

    @Column(columnDefinition = "TEXT")
    String summary;

    @Column(columnDefinition = "TEXT")
    String content;

    @Column(length = 1000)
    String tags;

    @Column(length = 100)
    String category;

    String img;
    String detailUrl;
    LocalDate publishedDate;

    protected Newsletter() {}

    private Newsletter(Integer vol, String title, String summary, String content, String tags, String category,
                       String img, String detailUrl, LocalDate publishedDate) {
        this.vol = vol;
        this.title = title;
        this.summary = summary;
        this.content = content;
        this.tags = tags;
        this.category = category;
        this.img = img;
        this.detailUrl = detailUrl;
        this.publishedDate = publishedDate;
    }

    public static Newsletter of(Integer vol, String title, String summary, String content, String tags, String category,
                                String img, String detailUrl, LocalDate publishedDate) {
        return new Newsletter(vol, title, summary, content, tags, category, img, detailUrl, publishedDate);
    }

    public DefaultDto.CreateResDto toCreateResDto() {
        return DefaultDto.CreateResDto.builder().id(getId()).build();
    }

    public void update(NewsletterDto.UpdateReqDto param) {
        if (param.getDeleted() != null) { setDeleted(param.getDeleted()); }
        if (param.getVol() != null) { setVol(param.getVol()); }
        if (param.getTitle() != null) { setTitle(param.getTitle()); }
        if (param.getSummary() != null) { setSummary(param.getSummary()); }
        if (param.getContent() != null) { setContent(param.getContent()); }
        if (param.getTags() != null) { setTags(param.getTags()); }
        if (param.getCategory() != null) { setCategory(param.getCategory()); }
        if (param.getImg() != null) { setImg(param.getImg()); }
        if (param.getDetailUrl() != null) { setDetailUrl(param.getDetailUrl()); }
        if (param.getPublishedDate() != null) { setPublishedDate(param.getPublishedDate()); }
    }
}
