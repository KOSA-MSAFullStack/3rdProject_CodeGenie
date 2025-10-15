package com.codegenie.member.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import com.codegenie.member.entity.RefreshToken;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByEmail(String email);
    Optional<RefreshToken> findByToken(String token);
    void deleteByEmail(String email);
}
