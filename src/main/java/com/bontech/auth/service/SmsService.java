package com.bontech.auth.service;

import com.bontech.auth.config.AppProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class SmsService {
    private final AppProperties properties;

    public void sendCode(String phone, String code) {
        String url = properties.getSms().getSenderApiUrl();
        String key = properties.getSms().getSenderApiKey();
        log.info("SMS sent to {} with code {} via {} (key length {})", phone, code, url, key == null ? 0 : key.length());
    }
}
