package com.codegenie.member.jwt;

import java.io.IOException;

import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import com.codegenie.member.service.RefreshTokenService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;


/**
 * JWT 검증 필터 (Access Token + Refresh Token)
 * 모든 요청마다 Authorization 헤더의 JWT를 검증하여 유효하면 인증 처리.
 * 만료된 Access Token이면 Refresh Token 검증 후 재발급.
 */
public class JWTFilter extends OncePerRequestFilter {

    private final JWTUtil jwtUtil;
    private final UserDetailsService userDetailsService;
    private final RefreshTokenService refreshTokenService;

    public JWTFilter(JWTUtil jwtUtil, 
    				UserDetailsService userDetailsService,
    				RefreshTokenService refreshTokenService) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
		this.refreshTokenService = refreshTokenService;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                   @NonNull HttpServletResponse response,
                                   @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        // 필터 실행 확인용 로그
        System.out.println("=======================================");
        System.out.println("JWTFilter 실행됨 → 요청 URI: " + request.getRequestURI());
        System.out.println("Authorization 헤더 = " + authHeader);

        // Authorization 헤더가 없는 경우
        if (authHeader == null) {
            System.out.println("❌ Authorization 헤더가 없습니다.");
            System.out.println("=======================================");
            filterChain.doFilter(request, response);
            return;
        }

        // Authorization 형식이 잘못된 경우
        if (!authHeader.startsWith("Bearer ")) {
            System.out.println("❌ Authorization 헤더 형식 오류: " + authHeader);
            System.out.println("=======================================");
            filterChain.doFilter(request, response);
            return;
        }

        // 토큰 분리 ("Bearer " 제거)
        String token = authHeader.substring(7);
        
        try {
            // (1) Access Token 만료 여부 확인
            if (jwtUtil.isExpired(token)) {
                System.out.println("❌ Access Token 만료됨 → Refresh Token 검증 시도");

                // 쿠키에서 refresh 토큰 꺼내기
                String refreshToken = getRefreshTokenFromCookies(request);

                if (refreshToken != null && jwtUtil.validateToken(refreshToken)) {
                    // DB에도 존재하는지 확인
                    if (refreshTokenService.validateToken(refreshToken)) {
                        String email = refreshTokenService.getEmailByToken(refreshToken);

                        // 새 Access Token 재발급
                        String newAccessToken = jwtUtil.createJwt(email, 60 * 60 * 1000L); // 1시간
                        response.addHeader("Authorization", "Bearer " + newAccessToken);
                        System.out.println("✅ 새 Access Token 재발급 완료: " + email);

                        // 인증 객체 세팅
                        // DB에서 사용자 정보 조회
                        UserDetails userDetails = userDetailsService.loadUserByUsername(email);
                        
                        // 시큐리티 인증 객체 생성
                        UsernamePasswordAuthenticationToken authentication =
                                new UsernamePasswordAuthenticationToken(
                                        userDetails, null, userDetails.getAuthorities());
                        
                        authentication.setDetails(
                        		new WebAuthenticationDetailsSource().buildDetails(request));
                        // 시큐리티 컨텍스트에 등록
                        SecurityContextHolder.getContext().setAuthentication(authentication);

                        filterChain.doFilter(request, response);
                        return;
                    } else {
                        System.out.println("❌ Refresh Token이 DB에 존재하지 않음 (무효)");
                    }
                } else {
                    System.out.println("❌ Refresh Token이 없거나 만료됨");
                }

                // 둘 다 실패 시 401 응답
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }

            // Access Token 유효 → 인증 처리
            String email = jwtUtil.getEmail(token);
            System.out.println("✅ Access Token 유효, 이메일 = " + email);

            UserDetails userDetails = userDetailsService.loadUserByUsername(email);
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);

        } catch (Exception e) {
            System.out.println("❌ JWT 검증 중 예외 발생: " + e.getMessage());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        filterChain.doFilter(request, response);
    }

    // Refresh Token 쿠키 추출 메서드
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
