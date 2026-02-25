package com.bontech.auth.service;

import com.bontech.auth.dto.AuthDto;
import com.bontech.auth.entity.PasswordHistory;
import com.bontech.auth.entity.Tenant;
import com.bontech.auth.entity.UserAccount;
import com.bontech.auth.entity.UserPhoneNumber;
import com.bontech.auth.repository.PasswordHistoryRepository;
import com.bontech.auth.repository.RoleRepository;
import com.bontech.auth.repository.TenantRepository;
import com.bontech.auth.repository.UserAccountRepository;
import com.bontech.auth.repository.UserPhoneNumberRepository;
import jakarta.transaction.Transactional;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RegistrationService {
    private final TenantRepository tenantRepository;
    private final UserAccountRepository userRepository;
    private final UserPhoneNumberRepository phoneRepository;
    private final RoleRepository roleRepository;
    private final PasswordHistoryRepository passwordHistoryRepository;
    private final PasswordEncoder encoder;

    @Transactional
    public UserAccount register(AuthDto.RegistrationRequest request) {
        Tenant tenant = tenantRepository.findByCode(request.tenantCode()).orElseGet(() -> {
            Tenant t = new Tenant();
            t.setCode(request.tenantCode());
            t.setName(request.tenantName());
            t.setBaseUrl(request.tenantBaseUrl());
            return tenantRepository.save(t);
        });

        UserAccount user = new UserAccount();
        user.setUsername(request.username());
        user.setPasswordHash(encoder.encode(request.password()));
        user.setTenantId(tenant.getId());
        user.setSystemUser(request.systemUser());
        user.setPasswordExpiresAt(Instant.now().plus(90, ChronoUnit.DAYS));
        user.setPasswordChangeRequired(false);
        Optional.ofNullable(request.roleCodes()).orElseGet(java.util.List::of)
                .forEach(code -> roleRepository.findByCode(code).ifPresent(user.getRoles()::add));
        UserAccount savedUser = userRepository.save(user);

        PasswordHistory history = new PasswordHistory();
        history.setUserId(savedUser.getId());
        history.setPasswordHash(savedUser.getPasswordHash());
        passwordHistoryRepository.save(history);

        for (AuthDto.PhoneInput phone : request.phones()) {
            UserPhoneNumber p = new UserPhoneNumber();
            p.setUserId(savedUser.getId());
            p.setPhoneNumber(phone.phoneNumber());
            p.setNationalCode(phone.nationalCode());
            p.setPreferredNumber(phone.preferred());
            phoneRepository.save(p);
        }
        return savedUser;
    }
}
