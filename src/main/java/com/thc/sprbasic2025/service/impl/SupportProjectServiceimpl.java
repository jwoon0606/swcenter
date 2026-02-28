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
}
