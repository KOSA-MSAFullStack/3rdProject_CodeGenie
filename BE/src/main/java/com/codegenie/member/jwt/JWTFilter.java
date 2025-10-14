package com.codegenie.member.jwt;

import java.io.IOException;

import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * JWT 검증 필터
 * 모든 요청마다 Authorization 헤더의 JWT를 검증하여 유효하면 인증 처리
 */
public class JWTFilter extends OncePerRequestFilter {

    private final JWTUtil jwtUtil;
    private final UserDetailsService userDetailsService;

    public JWTFilter(JWTUtil jwtUtil, UserDetailsService userDetailsService) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                   @NonNull HttpServletResponse response,
                                   @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        // ✅ 필터 실행 확인용 로그
        System.out.println("=======================================");
        System.out.println("JWTFilter 실행됨 → 요청 URI: " + request.getRequestURI());
        System.out.println("Authorization 헤더 = " + authHeader);

        // ✅ Authorization 헤더가 없는 경우
        if (authHeader == null) {
            System.out.println("❌ Authorization 헤더가 없습니다.");
            System.out.println("=======================================");
            filterChain.doFilter(request, response);
            return;
        }

        // ✅ Authorization 형식이 잘못된 경우
        if (!authHeader.startsWith("Bearer ")) {
            System.out.println("❌ Authorization 헤더 형식 오류: " + authHeader);
            System.out.println("=======================================");
            filterChain.doFilter(request, response);
            return;
        }

        // ✅ 토큰 분리 ("Bearer " 제거)
        String token = authHeader.substring(7);

        try {
            // JWT 만료 여부 확인
            if (jwtUtil.isExpired(token)) {
                System.out.println("❌ JWT 만료됨");
                System.out.println("=======================================");
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }

            System.out.println("✅ JWT 유효");

            // JWT에서 이메일 추출
            String email = jwtUtil.getEmail(token);
            System.out.println("✅ JWT 이메일 = " + email);

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

        } catch (Exception e) {
            System.out.println("❌ JWT 검증 중 예외 발생: " + e.getMessage());
            System.out.println("=======================================");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        System.out.println("✅ 인증 성공, 다음 필터로 이동");
        System.out.println("=======================================");

        filterChain.doFilter(request, response);
    }
}
