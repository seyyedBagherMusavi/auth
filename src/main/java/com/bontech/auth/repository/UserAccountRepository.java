package com.bontech.auth.repository;

import com.bontech.auth.entity.UserAccount;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserAccountRepository extends JpaRepository<UserAccount, Long> {
    @EntityGraph(attributePaths = {"roles", "roles.permissions", "tenant"})
    Optional<UserAccount> findByUsername(String username);
    List<UserAccount> findByRoles_Code(String roleCode);
    List<UserAccount> findByRoles_Permissions_Code(String permissionCode);
}
