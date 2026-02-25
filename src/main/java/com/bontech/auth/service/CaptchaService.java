package com.bontech.auth.service;

import org.springframework.stereotype.Service;

@Service
public class CaptchaService {
    public boolean validate(String token) {
        return token != null && !token.isBlank() && !"invalid".equalsIgnoreCase(token);
    }
}
