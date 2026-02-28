package com.thc.sprbasic2025.service.impl;

import com.thc.sprbasic2025.domain.Newsletter;
import com.thc.sprbasic2025.domain.NewsletterSubscriber;
import com.thc.sprbasic2025.dto.DefaultDto;
import com.thc.sprbasic2025.dto.NewsletterDto;
import com.thc.sprbasic2025.exception.NoMatchingDataException;
import com.thc.sprbasic2025.repository.NewsletterRepository;
import com.thc.sprbasic2025.repository.NewsletterSubscriberRepository;
import com.thc.sprbasic2025.service.NewsletterService;
import com.thc.sprbasic2025.service.PermittedService;
import com.thc.sprbasic2025.util.FileUpload;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@RequiredArgsConstructor
@Service
public class NewsletterServiceimpl implements NewsletterService {
    final String target = "news_letter";

    final NewsletterRepository newsletterRepository;
    final NewsletterSubscriberRepository newsletterSubscriberRepository;
    final PermittedService permittedService;

    @Override
    public DefaultDto.CreateResDto create(NewsletterDto.CreateReqDto param, Long reqUserId) {
        permittedService.isPermitted(reqUserId, target, 110);
        try {
            if (param.getFile() != null) {
                param.setImg("/image/" + FileUpload.upload(param.getFile()));
            }
        } catch (Exception ignored) {}

        if (param.getDetailUrl() == null || param.getDetailUrl().trim().isEmpty()) {
            param.setDetailUrl(NewsletterDto.DEFAULT_DETAIL_URL);
        }

        return newsletterRepository.save(param.toEntity()).toCreateResDto();
    }

    @Override
    public void update(NewsletterDto.UpdateReqDto param, Long reqUserId) {
        permittedService.isPermitted(reqUserId, target, 120);
        Newsletter newsletter = newsletterRepository.findById(param.getId())
                .orElseThrow(() -> new NoMatchingDataException("no data"));
        newsletter.update(param);
        newsletterRepository.save(newsletter);
    }

    @Override
    public void delete(DefaultDto.DeleteReqDto param, Long reqUserId) {
        update(NewsletterDto.UpdateReqDto.builder().id(param.getId()).deleted(true).build(), reqUserId);
    }

    @Override
    public void deleteList(DefaultDto.DeleteListReqDto param, Long reqUserId) {
        for (Long id : param.getIds()) {
            delete(DefaultDto.DeleteReqDto.builder().id(id).build(), reqUserId);
        }
    }

    @Override
    public NewsletterDto.DetailResDto detail(DefaultDto.DetailReqDto param, Long reqUserId) {
        Newsletter newsletter = newsletterRepository.findByIdAndDeletedFalse(param.getId())
                .orElseThrow(() -> new NoMatchingDataException("no data"));
        return toDetail(newsletter);
    }

    @Override
    public List<NewsletterDto.DetailResDto> scrollList(NewsletterDto.ScrollListReqDto param, Long reqUserId) {
        param.init();
        if (param.getDeleted() == null) {
            param.setDeleted(false);
        }

        Sort.Direction direction = "ASC".equalsIgnoreCase(param.getOrderway())
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;

        PageRequest pageable = PageRequest.of(0, param.getPerpage(), Sort.by(direction, "id"));

        Specification<Newsletter> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("deleted"), param.getDeleted()));

            String keyword = safeTrim(param.getKeyword());
            if (!keyword.isEmpty()) {
                String likeKeyword = "%" + keyword + "%";
                predicates.add(cb.or(
                        cb.like(root.get("title"), likeKeyword),
                        cb.like(root.get("summary"), likeKeyword),
                        cb.like(root.get("tags"), likeKeyword)
                ));
            }

            String category = safeTrim(param.getCategory());
            if (!category.isEmpty() && !"all".equalsIgnoreCase(category)) {
                predicates.add(cb.equal(cb.lower(root.get("category")), category.toLowerCase(Locale.ROOT)));
            }

            if (param.getCursor() != null) {
                if (direction == Sort.Direction.DESC) {
                    predicates.add(cb.lessThan(root.get("id"), param.getCursor()));
                } else {
                    predicates.add(cb.greaterThan(root.get("id"), param.getCursor()));
                }
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        List<Newsletter> rows = newsletterRepository.findAll(spec, pageable).getContent();
        List<NewsletterDto.DetailResDto> result = new ArrayList<>();
        for (Newsletter row : rows) {
            result.add(toDetail(row));
        }
        return result;
    }

    @Override
    public NewsletterDto.SubscribeResDto subscribe(NewsletterDto.SubscribeReqDto param) {
        String name = safeTrim(param.getName());
        String email = safeTrim(param.getEmail()).toLowerCase(Locale.ROOT);

        if (name.isEmpty()) {
            throw new RuntimeException("name required");
        }
        if (email.isEmpty() || !email.contains("@") || email.startsWith("@") || email.endsWith("@")) {
            throw new RuntimeException("email required");
        }
        if (!Boolean.TRUE.equals(param.getAgreePrivacy())) {
            throw new RuntimeException("privacy agreement required");
        }

        NewsletterSubscriber existed = newsletterSubscriberRepository.findByEmail(email);
        if (existed == null) {
            NewsletterSubscriber created = newsletterSubscriberRepository.save(
                    NewsletterSubscriber.of(name, email, true)
            );
            return NewsletterDto.SubscribeResDto.builder()
                    .id(created.getId())
                    .created(true)
                    .subscribed(true)
                    .build();
        }

        existed.resubscribe(name);
        NewsletterSubscriber updated = newsletterSubscriberRepository.save(existed);
        return NewsletterDto.SubscribeResDto.builder()
                .id(updated.getId())
                .created(false)
                .subscribed(true)
                .build();
    }

    private NewsletterDto.DetailResDto toDetail(Newsletter row) {
        return NewsletterDto.DetailResDto.builder()
                .id(row.getId())
                .deleted(row.getDeleted())
                .createdAt(row.getCreatedAt())
                .modifiedAt(row.getModifiedAt())
                .vol(row.getVol())
                .title(row.getTitle())
                .summary(row.getSummary())
                .content(row.getContent())
                .tags(row.getTags())
                .category(row.getCategory())
                .img(row.getImg())
                .detailUrl(safeDetailUrl(row.getDetailUrl()))
                .publishedDate(row.getPublishedDate())
                .build();
    }

    private String safeDetailUrl(String detailUrl) {
        String value = safeTrim(detailUrl);
        if (value.isEmpty()) {
            return NewsletterDto.DEFAULT_DETAIL_URL;
        }
        return value;
    }

    private String safeTrim(String value) {
        return value == null ? "" : value.trim();
    }
}
