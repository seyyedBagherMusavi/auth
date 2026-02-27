package com.bontech.auth.service;

import com.bontech.auth.config.AppProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CaptchaService {
    private final AppProperties properties;

    public boolean validate(String token) {
        if (token == null || token.isBlank() || "invalid".equalsIgnoreCase(token)) {
            return false;
        }
        String validateUrl = properties.getCaptcha().getValidateUrl();
        return validateUrl != null && !validateUrl.isBlank();
    }
}
