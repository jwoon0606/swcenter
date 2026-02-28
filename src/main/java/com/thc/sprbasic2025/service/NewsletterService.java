package com.thc.sprbasic2025.service;

import com.thc.sprbasic2025.dto.DefaultDto;
import com.thc.sprbasic2025.dto.NewsletterDto;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface NewsletterService {
    DefaultDto.CreateResDto create(NewsletterDto.CreateReqDto param, Long reqUserId);
    void update(NewsletterDto.UpdateReqDto param, Long reqUserId);
    void delete(DefaultDto.DeleteReqDto param, Long reqUserId);
    void deleteList(DefaultDto.DeleteListReqDto param, Long reqUserId);

    NewsletterDto.DetailResDto detail(DefaultDto.DetailReqDto param, Long reqUserId);
    List<NewsletterDto.DetailResDto> scrollList(NewsletterDto.ScrollListReqDto param, Long reqUserId);

    NewsletterDto.SubscribeResDto subscribe(NewsletterDto.SubscribeReqDto param);
}
