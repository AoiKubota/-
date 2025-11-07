package com.example.planvista.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC設定
 * インターセプターの登録
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private Adminauthinterceptor adminAuthInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 管理者専用ページにインターセプターを適用
        registry.addInterceptor(adminAuthInterceptor)
                .addPathPatterns("/all_users", "/all_users/**")
                .addPathPatterns("/user_history", "/user_history/**");
    }
}
