package com.thc.sprbasic2025.service.impl;

import com.thc.sprbasic2025.domain.Newsletter;
import com.thc.sprbasic2025.domain.NewsletterSubscriber;
import com.thc.sprbasic2025.dto.DefaultDto;
import com.thc.sprbasic2025.dto.NewsletterDto;
import com.thc.sprbasic2025.dto.PermissionDto;
import com.thc.sprbasic2025.exception.NoMatchingDataException;
import com.thc.sprbasic2025.repository.NewsletterRepository;
import com.thc.sprbasic2025.repository.NewsletterSubscriberRepository;
import com.thc.sprbasic2025.service.NewsletterService;
import com.thc.sprbasic2025.service.PermittedService;
import com.thc.sprbasic2025.util.FileUpload;
import com.thc.sprbasic2025.util.NewsletterMailSender;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@RequiredArgsConstructor
@Service
public class NewsletterServiceimpl implements NewsletterService {
    final String target = "news_letter";

    final NewsletterRepository newsletterRepository;
    final NewsletterSubscriberRepository newsletterSubscriberRepository;
    final PermittedService permittedService;
    final NewsletterMailSender newsletterMailSender;

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
        param.setCategory(normalizeCategoryString(param.getCategory()));

        return newsletterRepository.save(param.toEntity()).toCreateResDto();
    }

    @Override
    public void update(NewsletterDto.UpdateReqDto param, Long reqUserId) {
        permittedService.isPermitted(reqUserId, target, 120);
        if (param.getCategory() != null) {
            param.setCategory(normalizeCategoryString(param.getCategory()));
        }
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
        boolean canUpdate = canManageNewsletter(reqUserId);
        return toDetail(newsletter, canUpdate);
    }

    @Override
    public List<NewsletterDto.DetailResDto> scrollList(NewsletterDto.ScrollListReqDto param, Long reqUserId) {
        param.init();
        if (param.getDeleted() == null) {
            param.setDeleted(false);
        }
        boolean canUpdate = canManageNewsletter(reqUserId);

        Sort.Direction direction = "ASC".equalsIgnoreCase(param.getOrderway())
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;

        PageRequest pageable = PageRequest.of(0, param.getPerpage(), Sort.by(direction, "id"));

        Specification<Newsletter> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("deleted"), param.getDeleted()));

            LocalDate sDate = parseDate(param.getSdate());
            if (sDate != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), sDate.atStartOfDay()));
            }

            LocalDate fDate = parseDate(param.getFdate());
            if (fDate != null) {
                predicates.add(cb.lessThan(root.get("createdAt"), fDate.plusDays(1).atStartOfDay()));
            }

            String keyword = safeTrim(param.getKeyword());
            if (!keyword.isEmpty()) {
                String likeKeyword = "%" + keyword + "%";
                predicates.add(cb.or(
                        cb.like(root.get("title"), likeKeyword),
                        cb.like(root.get("summary"), likeKeyword)
                ));
            }

            String category = safeTrim(param.getCategory());
            if (!category.isEmpty() && !"all".equalsIgnoreCase(category)) {
                List<String> categoryTags = parseCategoryTags(category);
                for (String eachTag : categoryTags) {
                    predicates.add(cb.like(cb.lower(root.get("category")), "%" + eachTag.toLowerCase(Locale.ROOT) + "%"));
                }
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
            result.add(toDetail(row, canUpdate));
        }
        return result;
    }

    @Override
    public List<NewsletterDto.SubscriberDetailResDto> subscriberScrollList(NewsletterDto.SubscriberScrollListReqDto param, Long reqUserId) {
        permittedService.isPermitted(reqUserId, target, 120);
        param.init();

        Sort.Direction direction = "ASC".equalsIgnoreCase(param.getOrderway())
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;

        PageRequest pageable = PageRequest.of(0, param.getPerpage(), Sort.by(direction, "id"));
        Specification<NewsletterSubscriber> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            String keyword = safeTrim(param.getKeyword());
            if (!keyword.isEmpty()) {
                String likeKeyword = "%" + keyword + "%";
                predicates.add(cb.or(
                        cb.like(root.get("name"), likeKeyword),
                        cb.like(cb.lower(root.get("email")), "%" + keyword.toLowerCase(Locale.ROOT) + "%")
                ));
            }

            if (param.getSubscribed() != null) {
                predicates.add(cb.equal(root.get("deleted"), !param.getSubscribed()));
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

        List<NewsletterSubscriber> rows = newsletterSubscriberRepository.findAll(spec, pageable).getContent();
        List<NewsletterDto.SubscriberDetailResDto> result = new ArrayList<>();
        for (NewsletterSubscriber row : rows) {
            result.add(toSubscriberDetail(row));
        }
        return result;
    }

    @Override
    public void updateSubscriber(NewsletterDto.SubscriberUpdateReqDto param, Long reqUserId) {
        permittedService.isPermitted(reqUserId, target, 120);
        if (param == null || param.getId() == null) {
            throw new RuntimeException("id required");
        }
        NewsletterSubscriber subscriber = newsletterSubscriberRepository.findById(param.getId())
                .orElseThrow(() -> new NoMatchingDataException("no data"));

        if (param.getSubscribed() != null) {
            if (param.getSubscribed()) {
                String name = safeTrim(subscriber.getName());
                if (name.isEmpty()) {
                    name = "구독자";
                }
                subscriber.resubscribe(name);
            } else {
                subscriber.unsubscribe();
            }
            newsletterSubscriberRepository.save(subscriber);
        }
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
            String unsubscribeToken = ensureUnsubscribeToken(created);
            newsletterMailSender.sendSubscriptionConfirmed(email, name, unsubscribeToken);
            return NewsletterDto.SubscribeResDto.builder()
                    .id(created.getId())
                    .created(true)
                    .subscribed(true)
                    .build();
        }

        existed.resubscribe(name);
        NewsletterSubscriber updated = newsletterSubscriberRepository.save(existed);
        String unsubscribeToken = ensureUnsubscribeToken(updated);
        newsletterMailSender.sendSubscriptionConfirmed(email, name, unsubscribeToken);
        return NewsletterDto.SubscribeResDto.builder()
                .id(updated.getId())
                .created(false)
                .subscribed(true)
                .build();
    }

    @Override
    public NewsletterDto.UnsubscribeResDto unsubscribeByToken(String token) {
        String unsubscribeToken = safeTrim(token);
        if (unsubscribeToken.isEmpty()) {
            return NewsletterDto.UnsubscribeResDto.builder().unsubscribed(false).build();
        }

        NewsletterSubscriber subscriber = newsletterSubscriberRepository.findByUnsubscribeToken(unsubscribeToken);
        if (subscriber == null) {
            return NewsletterDto.UnsubscribeResDto.builder().unsubscribed(false).build();
        }
        if (!Boolean.TRUE.equals(subscriber.getDeleted())) {
            subscriber.unsubscribe();
            newsletterSubscriberRepository.save(subscriber);
        }
        return NewsletterDto.UnsubscribeResDto.builder().unsubscribed(true).build();
    }

    @Override
    public NewsletterDto.SendResDto sendToSubscribers(DefaultDto.DetailReqDto param, Long reqUserId) {
        permittedService.isPermitted(reqUserId, target, 120);
        if (!newsletterMailSender.isConfigured()) {
            throw new RuntimeException("mailbox config is incomplete: " + newsletterMailSender.getMissingConfigKeys());
        }

        Newsletter newsletter = newsletterRepository.findByIdAndDeletedFalse(param.getId())
                .orElseThrow(() -> new NoMatchingDataException("no data"));

        String detailUrl = safeDetailUrl(newsletter.getDetailUrl());
        List<NewsletterSubscriber> subscribers = newsletterSubscriberRepository.findAllByDeletedFalseOrderByIdAsc();
        int successCount = 0;
        int failedCount = 0;

        for (NewsletterSubscriber subscriber : subscribers) {
            String email = safeTrim(subscriber.getEmail()).toLowerCase(Locale.ROOT);
            if (!isValidEmail(email)) {
                failedCount++;
                continue;
            }

            String name = safeTrim(subscriber.getName());
            if (name.isEmpty()) {
                name = "구독자";
            }
            String unsubscribeToken = ensureUnsubscribeToken(subscriber);

            boolean sent = newsletterMailSender.sendNewsletterIssue(
                    email,
                    newsletter.getVol(),
                    newsletter.getTitle(),
                    detailUrl,
                    unsubscribeToken
            );

            if (sent) {
                successCount++;
            } else {
                failedCount++;
            }
        }

        return NewsletterDto.SendResDto.builder()
                .newsletterId(newsletter.getId())
                .totalCount(subscribers.size())
                .successCount(successCount)
                .failedCount(failedCount)
                .build();
    }

    private NewsletterDto.DetailResDto toDetail(Newsletter row, boolean canUpdate) {
        return NewsletterDto.DetailResDto.builder()
                .id(row.getId())
                .deleted(row.getDeleted())
                .createdAt(row.getCreatedAt())
                .modifiedAt(row.getModifiedAt())
                .vol(row.getVol())
                .title(row.getTitle())
                .summary(row.getSummary())
                .category(normalizeCategoryString(row.getCategory()))
                .img(row.getImg())
                .detailUrl(safeDetailUrl(row.getDetailUrl()))
                .canUpdate(canUpdate)
                .build();
    }

    private NewsletterDto.SubscriberDetailResDto toSubscriberDetail(NewsletterSubscriber row) {
        return NewsletterDto.SubscriberDetailResDto.builder()
                .id(row.getId())
                .deleted(row.getDeleted())
                .createdAt(row.getCreatedAt())
                .modifiedAt(row.getModifiedAt())
                .name(row.getName())
                .email(row.getEmail())
                .agreePrivacy(row.getAgreePrivacy())
                .subscribedAt(row.getSubscribedAt())
                .subscribed(!Boolean.TRUE.equals(row.getDeleted()))
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

    private LocalDate parseDate(String value) {
        String text = safeTrim(value);
        if (text.isEmpty()) {
            return null;
        }
        try {
            return LocalDate.parse(text);
        } catch (Exception ignored) {
            return null;
        }
    }

    private boolean canManageNewsletter(Long reqUserId) {
        if (reqUserId == null || reqUserId <= 0) {
            return false;
        }
        return permittedService.permitted(PermissionDto.PermittedReqDto.builder()
                .userId(reqUserId)
                .target(target)
                .func(120)
                .build());
    }

    private boolean isValidEmail(String email) {
        return !email.isEmpty() && email.contains("@") && !email.startsWith("@") && !email.endsWith("@");
    }

    private String ensureUnsubscribeToken(NewsletterSubscriber subscriber) {
        if (subscriber == null) {
            return "";
        }
        String token = safeTrim(subscriber.getUnsubscribeToken());
        if (!token.isEmpty()) {
            return token;
        }
        subscriber.ensureUnsubscribeToken();
        NewsletterSubscriber saved = newsletterSubscriberRepository.save(subscriber);
        return safeTrim(saved.getUnsubscribeToken());
    }

    private List<String> parseCategoryTags(String value) {
        Set<String> tags = new LinkedHashSet<>();
        Set<String> seenLower = new LinkedHashSet<>();
        String raw = safeTrim(value);
        if (raw.isEmpty()) {
            return new ArrayList<>();
        }

        String[] splits = raw.split("[,;\\n]+");
        for (String split : splits) {
            String token = safeTrim(split);
            if (token.startsWith("#")) {
                token = safeTrim(token.substring(1));
            }
            token = token.replaceAll("\\s+", " ");
            String lower = token.toLowerCase(Locale.ROOT);
            if (!token.isEmpty() && !seenLower.contains(lower)) {
                tags.add(token);
                seenLower.add(lower);
            }
        }
        return new ArrayList<>(tags);
    }

    private String normalizeCategoryString(String value) {
        List<String> tags = parseCategoryTags(value);
        return String.join(", ", tags);
    }
}
