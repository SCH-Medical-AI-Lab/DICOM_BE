package com.MedLab.dicom_converter_web.config;


import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // 모든 경로에 대해
                .allowedOriginPatterns("*") //  모든 도메인 (출처) 허용
                //.allowedOrigins("http://localhost:3000") // 프론트엔드 서버 주소 (React 디폴트값)
                .allowedMethods("GET","POST","PUT","DELETE") // 허용할 HTTP 메서드
                .allowedHeaders("*") // 모든 헤더 허용
                .allowCredentials(true); // 쿠키나 인증정보 허용
    }           
}

//주의 : allowedOrigins("*")와  allowCredentials(true)는 동시에 쓸 수 없음. 반드시 allowedOriginPatterns("*")를 사용할 것.