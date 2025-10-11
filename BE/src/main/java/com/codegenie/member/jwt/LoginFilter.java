package com.codegenie.member.jwt;

import java.io.IOException;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class LoginFilter extends UsernamePasswordAuthenticationFilter {

    private final AuthenticationManager authenticationManager;
    private final JWTUtil jwtUtil;

    public LoginFilter(AuthenticationManager authenticationManager, JWTUtil jwtUtil) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        // 기본 파라미터 이름이 username → email 로 변경
        setUsernameParameter("email");
        setPasswordParameter("password");
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException {

        // 클라이언트 요청에서 email, password 추출
        String email = obtainUsername(request);
        String password = obtainPassword(request);

        if (email == null) email = "";
        if (password == null) password = "";

        email = email.trim();

        // 이메일과 비밀번호를 담은 인증 토큰 생성
        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(email, password);

        // AuthenticationManager로 인증 시도
        return authenticationManager.authenticate(authToken);
    }

    // 로그인 성공 시 (JWT 발급 시점)
    @Override
    protected void successfulAuthentication(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain,
            Authentication authentication
    ) throws IOException, ServletException {

        System.out.println("로그인 성공: " + authentication.getName());

        // JWT 발급 로직
        String token = jwtUtil.createJwt(authentication.getName(), 60 * 60 * 1000L); // 1시간

        response.addHeader("Authorization", "Bearer " + token);
    }

    // 로그인 실패 시
    @Override
    protected void unsuccessfulAuthentication(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException failed
    ) throws IOException, ServletException {

        System.out.println("로그인 실패: " + failed.getMessage());
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    }
}
