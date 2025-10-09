// SecurityConfig.java
// [설정] Spring Security 구성
/*
 * 설명:
 * - 웹 애플리케이션의 보안 설정을 담당하는 클래스
 * - 로그인 처리, 리소스 접근 제어 등 정의
 * 
 * 주요 기능:
 * - 모든 요청을 임시로 허용하여 로그인 없이 접근 가능하도록 설정
 */
package com.codegenie.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // 모든 사용자가 모든 요청에 접근할 수 있도록 임시로 허용
        http
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll()
                )
                .csrf(csrf -> csrf.disable()); // CSRF 보호 기능 임시 비활성화

        return http.build();
    }
}
