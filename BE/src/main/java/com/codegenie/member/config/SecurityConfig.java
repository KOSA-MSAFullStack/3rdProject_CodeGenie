package com.codegenie.member.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandlerImpl;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.codegenie.member.jwt.JWTFilter;
import com.codegenie.member.jwt.JWTUtil;
import com.codegenie.member.jwt.LoginFilter;
import com.codegenie.member.service.CustomUserDetailsService;
import com.codegenie.member.service.RefreshTokenService;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    // 비밀번호 암호화
    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // AuthenticationManager 등록
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    // ✅ LoginFilter Bean 등록 (생성자 주입 방식 유지)
    @Bean
    public LoginFilter loginFilter(AuthenticationManager authenticationManager,
                                   JWTUtil jwtUtil,
                                   RefreshTokenService refreshTokenService) {
        LoginFilter filter = new LoginFilter(authenticationManager, jwtUtil, refreshTokenService);
        filter.setFilterProcessesUrl("/api/login");
        return filter;
    }

    // ✅ JWTFilter Bean 등록
    @Bean
    public JWTFilter jwtFilter(JWTUtil jwtUtil,
                               CustomUserDetailsService userDetailsService,
                               RefreshTokenService refreshTokenService) {
        return new JWTFilter(jwtUtil, userDetailsService, refreshTokenService);
    }
    
    
    // SecurityFilterChain
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http,
    										LoginFilter loginFilter,
    										JWTFilter jwtFilter) throws Exception {

        // 기본 설정
        http.csrf(csrf -> csrf.disable());
        http.formLogin(form -> form.disable());
        http.httpBasic(basic -> basic.disable());

        // ✅ 인가 설정 (기존 + /error, OPTIONS 허용 추가)
        http.authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll() // 프리플라이트 허용
                .requestMatchers("/", "/api/join", "/api/login", "/api/reissue", "/error").permitAll() // /error 허용 추가
                .anyRequest().authenticated()
        );

        // ✅ 예외 처리 명시 (401/403 구분)
        http.exceptionHandling(ex -> ex
                .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)) // 인증 실패 → 401
                .accessDeniedHandler(new AccessDeniedHandlerImpl()) // 권한 없음 → 403
        );

//        // 커스텀 로그인 필터 (JWT 발급)
//        LoginFilter loginFilter = new LoginFilter(authenticationManager, jwtUtil);
//        loginFilter.setFilterProcessesUrl("/api/login"); // 로그인 URL 명시
//
//        // JWT 검증 필터 (모든 요청 검증)
//        JWTFilter jwtFilter = new JWTFilter(jwtUtil, userDetailsService);

        // 필터 체인에 순서대로 등록
        http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
        http.addFilterAt(loginFilter, UsernamePasswordAuthenticationFilter.class);

        // CORS 허용 (Authorization 헤더용)
        http.cors(cors -> cors.configurationSource(request -> {
            var config = new org.springframework.web.cors.CorsConfiguration();
            config.setAllowedOrigins(java.util.List.of("http://localhost:5173"));
            config.setAllowedMethods(java.util.List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
            config.setAllowedHeaders(java.util.List.of("Authorization", "Content-Type"));
            config.setExposedHeaders(java.util.List.of("Authorization"));
            config.setAllowCredentials(true);
            return config;
        }));

        // 세션 비활성화
        http.sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        );

        return http.build();
    }
}
