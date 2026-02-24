package com.bontech.auth.service;

import com.bontech.auth.dto.AuthDto;
import com.bontech.auth.entity.PasswordHistory;
import com.bontech.auth.entity.TwoStepChallenge;
import com.bontech.auth.entity.UserAccount;
import com.bontech.auth.entity.UserPhoneNumber;
import com.bontech.auth.entity.UserSession;
import com.bontech.auth.repository.PasswordHistoryRepository;
import com.bontech.auth.repository.TwoStepChallengeRepository;
import com.bontech.auth.repository.UserPhoneNumberRepository;
import com.bontech.auth.repository.UserSessionRepository;
import com.bontech.auth.security.JwtService;
import jakarta.transaction.Transactional;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserPhoneNumberRepository phoneRepository;
    private final TwoStepChallengeRepository challengeRepository;
    private final UserSessionRepository sessionRepository;
    private final PasswordHistoryRepository passwordHistoryRepository;
    private final PasswordEncoder encoder;
    private final CaptchaService captchaService;
    private final SmsService smsService;
    private final JwtService jwtService;
    private final ActivityLogService logService;

    public AuthDto.TwoStepStartResponse loginStepOne(AuthDto.PhoneLoginRequest request) {
        if (!captchaService.validate(request.captchaToken())) {
            throw new IllegalArgumentException("Captcha validation failed");
        }

        UserPhoneNumber phone = phoneRepository.findByPhoneNumber(request.phoneNumber())
                .orElseThrow(() -> new IllegalArgumentException("Phone not found"));
        UserAccount user = phone.getUser();

        List<UserPhoneNumber> phones = phoneRepository.findByUser_Username(user.getUsername());
        String code = String.format("%06d", new Random().nextInt(999999));
        UserPhoneNumber target = phones.stream().filter(UserPhoneNumber::isPreferredNumber).findFirst().orElse(phones.getFirst());

        TwoStepChallenge challenge = new TwoStepChallenge();
        challenge.setUserId(user.getId());
        challenge.setCode(code);
        challenge.setTargetPhone(target.getPhoneNumber());
        challenge.setExpiresAt(Instant.now().plusSeconds(180));
        challenge.setUsed(false);
        challengeRepository.save(challenge);

        smsService.sendCode(target.getPhoneNumber(), code);
        logService.log(user, "LOGIN_STEP_1", "Phone + captcha validated");

        boolean passwordChangeRequired = user.isPasswordChangeRequired() || user.getPasswordExpiresAt().isBefore(Instant.now());
        return new AuthDto.TwoStepStartResponse("Two-step code sent",
                phones.stream().map(p -> new AuthDto.MaskedPhone(mask(p.getPhoneNumber()), p.getNationalCode())).toList(),
                passwordChangeRequired);
    }

    @Transactional
    public AuthDto.TokenResponse verifySecondStep(AuthDto.TwoStepVerifyRequest request) {
        UserPhoneNumber phone = phoneRepository.findByPhoneNumber(request.phoneNumber())
                .orElseThrow(() -> new IllegalArgumentException("Phone not found"));
        UserAccount user = phone.getUser();

        TwoStepChallenge challenge = challengeRepository.findByUser_UsernameAndCodeAndUsedFalse(user.getUsername(), request.code())
                .orElseThrow(() -> new IllegalArgumentException("Invalid challenge"));
        if (challenge.getExpiresAt().isBefore(Instant.now())) {
            throw new IllegalArgumentException("Challenge expired");
        }

        List<UserPhoneNumber> phones = phoneRepository.findByUser_Username(user.getUsername());
        if (phones.size() > 1 && request.nationalCode() != null) {
            boolean exists = phones.stream().map(UserPhoneNumber::getNationalCode).filter(Objects::nonNull)
                    .anyMatch(nc -> nc.equals(request.nationalCode()));
            if (!exists) throw new IllegalArgumentException("Invalid national code for selected phone");
        }

        challenge.setUsed(true);

        if (user.isPasswordChangeRequired() || user.getPasswordExpiresAt().isBefore(Instant.now())) {
            logService.log(user, "PASSWORD_EXPIRED", "Password expired; change password required");
            return new AuthDto.TokenResponse("PASSWORD_CHANGE_REQUIRED", "None", 0);
        }

        String token = jwtService.generate(user, null);
        UserSession session = new UserSession();
        session.setUserId(user.getId());
        session.setSessionTokenId(jwtService.extractTokenId(token));
        session.setExpiresAt(Instant.now().plusSeconds(jwtService.getExpirationSeconds()));
        session.setActive(true);
        sessionRepository.save(session);
        logService.log(user, "LOGIN_SUCCESS", "Second step validated");
        return new AuthDto.TokenResponse(token, "Bearer", jwtService.getExpirationSeconds());
    }

    @Transactional
    public void changeExpiredPassword(AuthDto.ChangeExpiredPasswordRequest request) {
        UserPhoneNumber phone = phoneRepository.findByPhoneNumber(request.phoneNumber())
                .orElseThrow(() -> new IllegalArgumentException("Phone not found"));
        UserAccount user = phone.getUser();

        if (!encoder.matches(request.currentPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Current password is invalid");
        }

        List<PasswordHistory> last3 = passwordHistoryRepository.findTop3ByUser_UsernameOrderByCreatedAtDesc(user.getUsername());
        boolean reused = last3.stream().anyMatch(p -> encoder.matches(request.newPassword(), p.getPasswordHash()));
        if (reused) {
            throw new IllegalArgumentException("New password cannot match last 3 passwords");
        }

        String newHash = encoder.encode(request.newPassword());
        user.setPasswordHash(newHash);
        user.setPasswordChangeRequired(false);
        user.setPasswordExpiresAt(Instant.now().plus(90, ChronoUnit.DAYS));

        PasswordHistory history = new PasswordHistory();
        history.setUserId(user.getId());
        history.setPasswordHash(newHash);
        passwordHistoryRepository.save(history);

        logService.log(user, "PASSWORD_CHANGED", "Password changed after expiry/login flow");
    }

    private String mask(String phone) {
        if (phone.length() < 4) return "****";
        return "*".repeat(phone.length() - 4) + phone.substring(phone.length() - 4);
    }

    public AuthDto.TokenResponse impersonate(AuthDto.ImpersonateRequest request) {
        UserPhoneNumber actorPhone = phoneRepository.findByUser_Username(request.actorUsername()).stream().findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Actor not found"));
        UserPhoneNumber targetPhone = phoneRepository.findByUser_Username(request.targetUsername()).stream().findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Target not found"));
        UserAccount actor = actorPhone.getUser();
        UserAccount target = targetPhone.getUser();

        String token = jwtService.generate(target, actor.getUsername());
        logService.log(actor, "IMPERSONATE", "Impersonated " + target.getUsername());
        return new AuthDto.TokenResponse(token, "Bearer", jwtService.getExpirationSeconds());
    }
}
