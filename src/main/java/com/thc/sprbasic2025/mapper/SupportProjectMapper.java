package com.thc.sprbasic2025.mapper;

import com.thc.sprbasic2025.dto.SupportProjectDto;

import java.util.List;

public interface SupportProjectMapper {
    SupportProjectDto.DetailResDto detail(Long id);
    List<SupportProjectDto.DetailResDto> list(SupportProjectDto.ListReqDto param);

    List<SupportProjectDto.DetailResDto> pagedList(SupportProjectDto.PagedListReqDto param);
    int pagedListCount(SupportProjectDto.PagedListReqDto param);
    List<SupportProjectDto.DetailResDto> scrollList(SupportProjectDto.ScrollListReqDto param);
}
