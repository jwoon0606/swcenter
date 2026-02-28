package com.thc.sprbasic2025.service;

import com.thc.sprbasic2025.dto.DefaultDto;
import com.thc.sprbasic2025.dto.ExtracurricularActivityDto;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface ExtracurricularActivityService {
    DefaultDto.CreateResDto create(ExtracurricularActivityDto.CreateReqDto param, Long reqUserId);
    void update(ExtracurricularActivityDto.UpdateReqDto param, Long reqUserId);
    void delete(DefaultDto.DeleteReqDto param, Long reqUserId);
    void deleteList(DefaultDto.DeleteListReqDto param, Long reqUserId);

    ExtracurricularActivityDto.DetailResDto detail(DefaultDto.DetailReqDto param, Long reqUserId);
    List<ExtracurricularActivityDto.DetailResDto> scrollList(ExtracurricularActivityDto.ScrollListReqDto param, Long reqUserId);
}
