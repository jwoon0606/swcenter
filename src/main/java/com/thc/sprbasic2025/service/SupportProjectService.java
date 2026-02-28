package com.thc.sprbasic2025.service;

import com.thc.sprbasic2025.dto.DefaultDto;
import com.thc.sprbasic2025.dto.SupportProjectDto;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface SupportProjectService {
    SupportProjectDto.SequenceResDto sequence(SupportProjectDto.SequenceReqDto param, Long reqUserId);
    SupportProjectDto.SeedResDto seedSamples(Long reqUserId);

    DefaultDto.CreateResDto create(SupportProjectDto.CreateReqDto param, Long reqUserId);
    void update(SupportProjectDto.UpdateReqDto param, Long reqUserId);
    void delete(DefaultDto.DeleteReqDto param, Long reqUserId);
    void deleteList(DefaultDto.DeleteListReqDto param, Long reqUserId);

    SupportProjectDto.DetailResDto detail(DefaultDto.DetailReqDto param, Long reqUserId);
    List<SupportProjectDto.DetailResDto> list(SupportProjectDto.ListReqDto param, Long reqUserId);
    DefaultDto.PagedListResDto pagedList(SupportProjectDto.PagedListReqDto param, Long reqUserId);
    List<SupportProjectDto.DetailResDto> scrollList(SupportProjectDto.ScrollListReqDto param, Long reqUserId);
}
