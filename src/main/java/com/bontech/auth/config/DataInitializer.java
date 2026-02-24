package com.bontech.auth.config;

import com.bontech.auth.entity.Permission;
import com.bontech.auth.entity.Role;
import com.bontech.auth.entity.Tenant;
import com.bontech.auth.repository.PermissionRepository;
import com.bontech.auth.repository.RoleRepository;
import com.bontech.auth.repository.TenantRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataInitializer {
    @Bean
    CommandLineRunner seed(TenantRepository tenantRepository, RoleRepository roleRepository, PermissionRepository permissionRepository) {
        return args -> {
            Tenant tenant = tenantRepository.findByCode("default").orElseGet(() -> {
                Tenant t = new Tenant();
                t.setCode("default");
                t.setName("Default Tenant");
                t.setBaseUrl("https://default.example.com");
                return tenantRepository.save(t);
            });

            Permission p1 = permissionRepository.findByCode("USER_READ").orElseGet(() -> {
                Permission p = new Permission();
                p.setCode("USER_READ");
                p.setDescription("Read users");
                return permissionRepository.save(p);
            });
            Permission p2 = permissionRepository.findByCode("USER_IMPERSONATE").orElseGet(() -> {
                Permission p = new Permission();
                p.setCode("USER_IMPERSONATE");
                p.setDescription("Impersonate users");
                return permissionRepository.save(p);
            });

            roleRepository.findByCode("ADMIN").orElseGet(() -> {
                Role r = new Role();
                r.setCode("ADMIN");
                r.setTenantId(tenant.getId());
                r.getPermissions().add(p1);
                r.getPermissions().add(p2);
                return roleRepository.save(r);
            });
        };
    }
}
