// AppConfig.java
// [전역 설정] 애플리케이션 설정
/*
 * 설명:
 * - 애플리케이션 전반에 사용될 Bean을 등록하고 관리하는 설정 클래스
 *
 * 주요 기능:
 * - RestTemplate Bean 등록
 */
package com.codegenie.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

// * author: 김기성
@Configuration
public class AppConfig {

    // 외부 API와 통신하기 위한 RestTemplate을 Bean으로 등록
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}