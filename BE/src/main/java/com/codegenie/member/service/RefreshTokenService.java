package com.codegenie.member.service;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.codegenie.member.entity.RefreshToken;
import com.codegenie.member.repository.RefreshTokenRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshRepo;

    public void saveToken(String email, String token, long expiryMillis) {
        RefreshToken refresh = refreshRepo.findByEmail(email).orElse(new RefreshToken());
        refresh.setEmail(email);
        refresh.setToken(token);
        refresh.setExpiryDate(
        	    LocalDateTime.now().plusSeconds(expiryMillis / 1000)
        	);
        refreshRepo.save(refresh);
    }

    public boolean validateToken(String token) {
        Optional<RefreshToken> opt = refreshRepo.findByToken(token);
        return opt.isPresent() && opt.get().getExpiryDate().isAfter(LocalDateTime.now());
    }

    public String getEmailByToken(String token) {
        return refreshRepo.findByToken(token)
                .map(RefreshToken::getEmail)
                .orElse(null);
    }

    public void deleteByEmail(String email) {
        refreshRepo.deleteByEmail(email);
    }
}
