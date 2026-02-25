package com.bontech.auth.controller;

import com.bontech.auth.dto.AuthDto;
import com.bontech.auth.service.AuthService;
import com.bontech.auth.service.RegistrationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final RegistrationService registrationService;
    private final AuthService authService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public void register(@Valid @RequestBody AuthDto.RegistrationRequest request) {
        registrationService.register(request);
    }

    @PostMapping("/login/phone")
    public AuthDto.TwoStepStartResponse loginStepOne(@Valid @RequestBody AuthDto.PhoneLoginRequest request) {
        return authService.loginStepOne(request);
    }

    @PostMapping("/verify-2fa")
    public AuthDto.TokenResponse verifySecondStep(@Valid @RequestBody AuthDto.TwoStepVerifyRequest request) {
        return authService.verifySecondStep(request);
    }

    @PostMapping("/password/change-expired")
    public void changeExpiredPassword(@Valid @RequestBody AuthDto.ChangeExpiredPasswordRequest request) {
        authService.changeExpiredPassword(request);
    }

    @PostMapping("/impersonate")
    public AuthDto.TokenResponse impersonate(@Valid @RequestBody AuthDto.ImpersonateRequest request) {
        return authService.impersonate(request);
    }
}
