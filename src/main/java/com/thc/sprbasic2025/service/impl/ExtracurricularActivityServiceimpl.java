package com.thc.sprbasic2025.service.impl;

import com.thc.sprbasic2025.domain.ExtracurricularActivity;
import com.thc.sprbasic2025.dto.DefaultDto;
import com.thc.sprbasic2025.dto.ExtracurricularActivityDto;
import com.thc.sprbasic2025.exception.NoMatchingDataException;
import com.thc.sprbasic2025.repository.ExtracurricularActivityRepository;
import com.thc.sprbasic2025.service.ExtracurricularActivityService;
import com.thc.sprbasic2025.service.PermittedService;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Service
public class ExtracurricularActivityServiceimpl implements ExtracurricularActivityService {
    final String target = "extracurricular_activities";

    final ExtracurricularActivityRepository extracurricularActivityRepository;
    final PermittedService permittedService;

    @Override
    public DefaultDto.CreateResDto create(ExtracurricularActivityDto.CreateReqDto param, Long reqUserId) {
        permittedService.isPermitted(reqUserId, target, 110);

        if (param.getDetailUrl() == null || param.getDetailUrl().trim().isEmpty()) {
            param.setDetailUrl(ExtracurricularActivityDto.DEFAULT_DETAIL_URL);
        }

        return extracurricularActivityRepository.save(param.toEntity()).toCreateResDto();
    }

    @Override
    public void update(ExtracurricularActivityDto.UpdateReqDto param, Long reqUserId) {
        permittedService.isPermitted(reqUserId, target, 120);
        ExtracurricularActivity activity = extracurricularActivityRepository.findById(param.getId())
                .orElseThrow(() -> new NoMatchingDataException("no data"));
        activity.update(param);
        extracurricularActivityRepository.save(activity);
    }

    @Override
    public void delete(DefaultDto.DeleteReqDto param, Long reqUserId) {
        update(ExtracurricularActivityDto.UpdateReqDto.builder().id(param.getId()).deleted(true).build(), reqUserId);
    }

    @Override
    public void deleteList(DefaultDto.DeleteListReqDto param, Long reqUserId) {
        for (Long id : param.getIds()) {
            delete(DefaultDto.DeleteReqDto.builder().id(id).build(), reqUserId);
        }
    }

    @Override
    public ExtracurricularActivityDto.DetailResDto detail(DefaultDto.DetailReqDto param, Long reqUserId) {
        ExtracurricularActivity activity = extracurricularActivityRepository.findByIdAndDeletedFalse(param.getId())
                .orElseThrow(() -> new NoMatchingDataException("no data"));
        return toDetail(activity);
    }

    @Override
    public List<ExtracurricularActivityDto.DetailResDto> scrollList(ExtracurricularActivityDto.ScrollListReqDto param, Long reqUserId) {
        param.init();
        if (param.getDeleted() == null) {
            param.setDeleted(false);
        }

        Sort.Direction direction = "ASC".equalsIgnoreCase(param.getOrderway())
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;

        PageRequest pageable = PageRequest.of(0, param.getPerpage(), Sort.by(direction, "id"));

        Specification<ExtracurricularActivity> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("deleted"), param.getDeleted()));

            String keyword = safeTrim(param.getKeyword());
            if (!keyword.isEmpty()) {
                String likeKeyword = "%" + keyword + "%";
                predicates.add(cb.or(
                        cb.like(root.get("title"), likeKeyword),
                        cb.like(root.get("summary"), likeKeyword)
                ));
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

        List<ExtracurricularActivity> rows = extracurricularActivityRepository.findAll(spec, pageable).getContent();
        List<ExtracurricularActivityDto.DetailResDto> result = new ArrayList<>();
        for (ExtracurricularActivity row : rows) {
            result.add(toDetail(row));
        }
        return result;
    }

    private ExtracurricularActivityDto.DetailResDto toDetail(ExtracurricularActivity row) {
        return ExtracurricularActivityDto.DetailResDto.builder()
                .id(row.getId())
                .deleted(row.getDeleted())
                .createdAt(row.getCreatedAt())
                .modifiedAt(row.getModifiedAt())
                .title(row.getTitle())
                .summary(row.getSummary())
                .img(row.getImg())
                .detailUrl(safeDetailUrl(row.getDetailUrl()))
                .build();
    }

    private String safeDetailUrl(String detailUrl) {
        String value = safeTrim(detailUrl);
        if (value.isEmpty()) {
            return ExtracurricularActivityDto.DEFAULT_DETAIL_URL;
        }
        return value;
    }

    private String safeTrim(String value) {
        return value == null ? "" : value.trim();
    }
}
