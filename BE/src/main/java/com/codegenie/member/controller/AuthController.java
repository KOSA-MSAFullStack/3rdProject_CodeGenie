package com.codegenie.member.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import com.codegenie.member.jwt.JWTUtil;
import com.codegenie.member.service.RefreshTokenService;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

/**
 * 인증 관련 컨트롤러
 * - /api/reissue : Refresh Token으로 Access Token 재발급
 * - /api/logout  : Refresh Token 무효화 + 쿠키 삭제
 */
@RestController
@RequiredArgsConstructor
public class AuthController {

    private final JWTUtil jwtUtil;
    private final RefreshTokenService refreshTokenService;

    // Access Token 재발급 (프론트의 /api/reissue 호출 시)
    @PostMapping("/api/reissue")
    public ResponseEntity<?> reissue(HttpServletRequest request, HttpServletResponse response) {

        // 쿠키에서 Refresh Token 추출
        String refreshToken = getRefreshTokenFromCookies(request);
        if (refreshToken == null) {
            return ResponseEntity.status(401).body("Refresh Token not found");
        }

        // Refresh Token 검증
        boolean isValid = jwtUtil.validateToken(refreshToken)
                && refreshTokenService.validateToken(refreshToken);

        if (!isValid) {
            return ResponseEntity.status(401).body("Invalid or expired Refresh Token");
        }

        // 유효하다면 DB에서 이메일 조회 후 새 Access Token 발급
        String email = refreshTokenService.getEmailByToken(refreshToken);
        if (email == null) {
            return ResponseEntity.status(401).body("Email not found for Refresh Token");
        }

        // 새 Access Token 1시간짜리 생성
        String newAccessToken = jwtUtil.createJwt(email, 60 * 60 * 1000L);

        // Authorization 헤더에 새 토큰 담아 응답
        return ResponseEntity.ok()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + newAccessToken)
                .body("Access Token reissued successfully");
    }

    // 로그아웃 처리
    @PostMapping("/api/logout")
    public ResponseEntity<?> logout(HttpServletRequest request, HttpServletResponse response) {

        // 쿠키에서 Refresh Token 추출
        String refreshToken = getRefreshTokenFromCookies(request);
        if (refreshToken == null) {
            return ResponseEntity.badRequest().body("No refresh token found");
        }

        // Refresh Token이 DB에 있으면 해당 이메일 찾아서 삭제
        String email = refreshTokenService.getEmailByToken(refreshToken);
        if (email != null) {
            refreshTokenService.deleteByEmail(email);
        }

        // 클라이언트의 쿠키를 무효화 (Max-Age=0)
        Cookie cookie = new Cookie("refresh", null);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setMaxAge(0);
        response.addCookie(cookie);

        // 프론트에서도 로컬 토큰 삭제를 유도
        return ResponseEntity.ok("Logout success");
    }

    // HttpOnly Refresh Token 쿠키 꺼내는 헬퍼 메서드
    private String getRefreshTokenFromCookies(HttpServletRequest request) {
        if (request.getCookies() == null) return null;
        for (Cookie cookie : request.getCookies()) {
            if ("refresh".equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }
}
