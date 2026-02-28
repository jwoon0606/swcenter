package com.thc.sprbasic2025.service.impl;

import com.thc.sprbasic2025.domain.SupportProject;
import com.thc.sprbasic2025.dto.DefaultDto;
import com.thc.sprbasic2025.dto.SupportProjectDto;
import com.thc.sprbasic2025.exception.NoMatchingDataException;
import com.thc.sprbasic2025.mapper.SupportProjectMapper;
import com.thc.sprbasic2025.repository.SupportProjectRepository;
import com.thc.sprbasic2025.service.PermittedService;
import com.thc.sprbasic2025.service.SupportProjectService;
import com.thc.sprbasic2025.util.FileUpload;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Service
public class SupportProjectServiceimpl implements SupportProjectService {

    final String target = "support_project";

    final SupportProjectRepository supportProjectRepository;
    final SupportProjectMapper supportProjectMapper;
    final PermittedService permittedService;

    @Override
    public SupportProjectDto.SequenceResDto sequence(SupportProjectDto.SequenceReqDto param, Long reqUserId) {
        permittedService.isPermitted(reqUserId, target, 120);

        SupportProject supportProject = supportProjectRepository.findById(param.getId()).orElse(null);
        if (supportProject == null) {
            return SupportProjectDto.SequenceResDto.builder().result(false).build();
        }

        int nowSequence = supportProject.getSequence();
        int toSequence;

        Boolean way = param.getWay();
        if (way) {
            toSequence = nowSequence - 1;
            if (toSequence < 1) {
                return SupportProjectDto.SequenceResDto.builder().result(false).build();
            }
        } else {
            toSequence = nowSequence + 1;
            int count = supportProjectMapper.pagedListCount(SupportProjectDto.PagedListReqDto.builder().build());
            if (toSequence > count) {
                return SupportProjectDto.SequenceResDto.builder().result(false).build();
            }
        }

        SupportProject targetSupportProject = supportProjectRepository.findBySequence(toSequence);
        if (targetSupportProject == null) {
            return SupportProjectDto.SequenceResDto.builder().result(false).build();
        }

        targetSupportProject.setSequence(0);
        supportProjectRepository.save(targetSupportProject);

        supportProject.setSequence(toSequence);
        supportProjectRepository.save(supportProject);

        targetSupportProject.setSequence(nowSequence);
        supportProjectRepository.save(targetSupportProject);

        return SupportProjectDto.SequenceResDto.builder().result(true).build();
    }

    @Override
    public SupportProjectDto.SeedResDto seedSamples(Long reqUserId) {
        permittedService.isPermitted(reqUserId, target, 110);

        long seqCount = supportProjectRepository.countByDeletedFalse();
        int createdCount = 0;

        for (SupportProject sample : sampleProjects()) {
            if (supportProjectRepository.existsByTitleAndDeletedFalse(sample.getTitle())) {
                continue;
            }
            sample.setSequence((int) (++seqCount));
            supportProjectRepository.save(sample);
            createdCount++;
        }

        return SupportProjectDto.SeedResDto.builder()
                .createdCount(createdCount)
                .totalCount((int) supportProjectRepository.countByDeletedFalse())
                .build();
    }

    @Override
    public DefaultDto.CreateResDto create(SupportProjectDto.CreateReqDto param, Long reqUserId) {
        permittedService.isPermitted(reqUserId, target, 110);

        int count = supportProjectMapper.pagedListCount(SupportProjectDto.PagedListReqDto.builder().build());
        param.setSequence(++count);

        try {
            if (param.getFile() != null) {
                param.setImg("/image/" + FileUpload.upload(param.getFile()));
            }
        } catch (Exception ignored) {}

        return supportProjectRepository.save(param.toEntity()).toCreateResDto();
    }

    @Override
    public void update(SupportProjectDto.UpdateReqDto param, Long reqUserId) {
        permittedService.isPermitted(reqUserId, target, 120);
        SupportProject supportProject = supportProjectRepository.findById(param.getId())
                .orElseThrow(() -> new NoMatchingDataException("no data"));
        supportProject.update(param);
        supportProjectRepository.save(supportProject);
    }

    @Override
    public void delete(DefaultDto.DeleteReqDto param, Long reqUserId) {
        update(SupportProjectDto.UpdateReqDto.builder().id(param.getId()).deleted(true).build(), reqUserId);
    }

    @Override
    public void deleteList(DefaultDto.DeleteListReqDto param, Long reqUserId) {
        for (Long id : param.getIds()) {
            delete(DefaultDto.DeleteReqDto.builder().id(id).build(), reqUserId);
        }
    }

    public SupportProjectDto.DetailResDto get(DefaultDto.DetailReqDto param, Long reqUserId) {
        return supportProjectMapper.detail(param.getId());
    }

    @Override
    public SupportProjectDto.DetailResDto detail(DefaultDto.DetailReqDto param, Long reqUserId) {
        return get(param, reqUserId);
    }

    @Override
    public List<SupportProjectDto.DetailResDto> list(SupportProjectDto.ListReqDto param, Long reqUserId) {
        return detailList(supportProjectMapper.list(param), reqUserId);
    }

    public List<SupportProjectDto.DetailResDto> detailList(List<SupportProjectDto.DetailResDto> list, Long reqUserId) {
        List<SupportProjectDto.DetailResDto> newList = new ArrayList<>();
        for (SupportProjectDto.DetailResDto each : list) {
            newList.add(get(DefaultDto.DetailReqDto.builder().id(each.getId()).build(), reqUserId));
        }
        return newList;
    }

    @Override
    public DefaultDto.PagedListResDto pagedList(SupportProjectDto.PagedListReqDto param, Long reqUserId) {
        DefaultDto.PagedListResDto res = param.init(supportProjectMapper.pagedListCount(param));
        res.setList(detailList(supportProjectMapper.pagedList(param), reqUserId));
        return res;
    }

    @Override
    public List<SupportProjectDto.DetailResDto> scrollList(SupportProjectDto.ScrollListReqDto param, Long reqUserId) {
        param.init();

        if ("title".equals(param.getOrderby())) {
            Long cursor = param.getCursor();
            if (cursor != null) {
                SupportProjectDto.DetailResDto supportProject = supportProjectMapper.detail(cursor);
                if (supportProject != null) {
                    param.setMark(supportProject.getTitle() + "_" + supportProject.getId());
                }
            }
        }

        return detailList(supportProjectMapper.scrollList(param), reqUserId);
    }

    private List<SupportProject> sampleProjects() {
        List<SupportProject> list = new ArrayList<>();
        list.add(SupportProject.of(
                null,
                "AI 기반 모션 인식 맞춤형 다이어트 댄스 플랫폼",
                "모션 인식 기반 맞춤형 다이어트 댄스 플랫폼",
                "https://zizzy.imweb.me/",
                "/frontswcenter/img/value-spread/sw_val_candidate1.png",
                LocalDate.of(2025, 8, 22)
        ));
        list.add(SupportProject.of(
                null,
                "부동산 예측 알리미 가격 모아",
                "지역별 부동산 가격 예측 정보를 제공하는 알림 서비스",
                "https://example.com/real-estate-price",
                "/frontswcenter/img/value-spread/sw_val_candidate2.png",
                LocalDate.of(2024, 7, 13)
        ));
        list.add(SupportProject.of(
                null,
                "교통 약자를 위한 자연체험 매칭 플랫폼",
                "교통 약자 대상 체험형 프로그램 매칭 플랫폼",
                "https://example.com/forest-mate",
                "/frontswcenter/img/value-spread/sw_val_candidate3.png",
                LocalDate.of(2024, 10, 4)
        ));
        return list;
    }
}
