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

    @Column(length = 100)
    String category;

    String img;
    String detailUrl;

    protected Newsletter() {}

    private Newsletter(Integer vol, String title, String summary, String category, String img, String detailUrl) {
        this.vol = vol;
        this.title = title;
        this.summary = summary;
        this.category = category;
        this.img = img;
        this.detailUrl = detailUrl;
    }

    public static Newsletter of(Integer vol, String title, String summary, String category, String img, String detailUrl) {
        return new Newsletter(vol, title, summary, category, img, detailUrl);
    }

    public DefaultDto.CreateResDto toCreateResDto() {
        return DefaultDto.CreateResDto.builder().id(getId()).build();
    }

    public void update(NewsletterDto.UpdateReqDto param) {
        if (param.getDeleted() != null) { setDeleted(param.getDeleted()); }
        if (param.getVol() != null) { setVol(param.getVol()); }
        if (param.getTitle() != null) { setTitle(param.getTitle()); }
        if (param.getSummary() != null) { setSummary(param.getSummary()); }
        if (param.getCategory() != null) { setCategory(param.getCategory()); }
        if (param.getImg() != null) { setImg(param.getImg()); }
        if (param.getDetailUrl() != null) { setDetailUrl(param.getDetailUrl()); }
    }
}
