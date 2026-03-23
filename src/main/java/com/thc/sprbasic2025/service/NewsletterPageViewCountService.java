package com.thc.sprbasic2025.service;

public interface NewsletterPageViewCountService {

    Long increaseViewCount(String targetType, String targetKey);

    Long getViewCount(String targetType, String targetKey);
}