package com.bontech.auth.service;

import com.bontech.auth.dto.UserDto;
import com.bontech.auth.entity.UserAccount;
import com.bontech.auth.repository.UserAccountRepository;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserQueryService {
    private final UserAccountRepository repository;


    public UserDto.UserAuthzResponse getAuthz(String username) {
        UserAccount user = repository.findByUsername(username).orElseThrow();
        Set<String> permissions = user.getRoles().stream()
                .flatMap(role -> role.getPermissions().stream())
                .map(p -> p.getCode())
                .collect(Collectors.toSet());
        return new UserDto.UserAuthzResponse(user.getUsername(), String.valueOf(user.getTenant().getId()), user.getTenant().getName(),
                user.getRoles().stream().map(r -> r.getCode()).toList(), permissions.stream().toList());
    }

    public UserDto.RoleUsersResponse usersByRole(String roleCode) {
        List<UserDto.UserSummary> users = repository.findByRoles_Code(roleCode).stream().map(this::toSummary).toList();
        return new UserDto.RoleUsersResponse(roleCode, users);
    }

    public UserDto.PermissionUsersResponse usersByPermission(String permissionCode) {
        List<UserDto.UserSummary> users = repository.findByRoles_Permissions_Code(permissionCode).stream().map(this::toSummary).toList();
        return new UserDto.PermissionUsersResponse(permissionCode, users);
    }

    private UserDto.UserSummary toSummary(UserAccount user) {
        return new UserDto.UserSummary(user.getUsername(), user.getTenant().getName(), user.isSystemUser());
    }
}
