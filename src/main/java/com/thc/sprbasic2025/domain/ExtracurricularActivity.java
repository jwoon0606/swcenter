package com.thc.sprbasic2025.domain;

import com.thc.sprbasic2025.dto.DefaultDto;
import com.thc.sprbasic2025.dto.ExtracurricularActivityDto;
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
        @Index(columnList = "deleted")
})
@Entity
public class ExtracurricularActivity extends AuditingFields {
    String title;
    String summary;
    String detailUrl;

    protected ExtracurricularActivity() {}

    private ExtracurricularActivity(
            String title,
            String summary,
            String detailUrl
    ) {
        this.title = title;
        this.summary = summary;
        this.detailUrl = detailUrl;
    }

    public static ExtracurricularActivity of(
            String title,
            String summary,
            String detailUrl
    ) {
        return new ExtracurricularActivity(title, summary, detailUrl);
    }

    public DefaultDto.CreateResDto toCreateResDto() {
        return DefaultDto.CreateResDto.builder().id(getId()).build();
    }

    public void update(ExtracurricularActivityDto.UpdateReqDto param) {
        if (param.getDeleted() != null) { setDeleted(param.getDeleted()); }
        if (param.getTitle() != null) { setTitle(param.getTitle()); }
        if (param.getSummary() != null) { setSummary(param.getSummary()); }
        if (param.getDetailUrl() != null) { setDetailUrl(param.getDetailUrl()); }
    }
}
