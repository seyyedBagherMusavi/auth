package com.bontech.auth.repository;

import com.bontech.auth.entity.UserSession;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserSessionRepository extends JpaRepository<UserSession, Long> {
    List<UserSession> findByUser_UsernameAndActiveTrue(String username);
    Optional<UserSession> findBySessionTokenId(String tokenId);
}
