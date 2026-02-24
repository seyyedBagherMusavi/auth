package com.bontech.auth.controller;

import com.bontech.auth.dto.UserDto;
import com.bontech.auth.service.UserQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserQueryService service;


    @GetMapping("/{username}/authorization")
    public UserDto.UserAuthzResponse userAuthz(@PathVariable String username) {
        return service.getAuthz(username);
    }

    @GetMapping("/by-role/{roleCode}")
    public UserDto.RoleUsersResponse usersByRole(@PathVariable String roleCode) {
        return service.usersByRole(roleCode);
    }

    @GetMapping("/by-permission/{permissionCode}")
    public UserDto.PermissionUsersResponse usersByPermission(@PathVariable String permissionCode) {
        return service.usersByPermission(permissionCode);
    }
}
