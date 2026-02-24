package com.bontech.auth.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class SmsService {
    public void sendCode(String phone, String code) {
        log.info("SMS sent to {} with code {} (replace with your provider implementation)", phone, code);
    }
}
