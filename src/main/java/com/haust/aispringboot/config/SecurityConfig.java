package com.haust.aispringboot.config;

import cn.hutool.core.text.AntPathMatcher;
import com.haust.aispringboot.util.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.SessionManagementConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private static final AntPathMatcher antPathMatcher = new AntPathMatcher();
    private static final String[] PUBLIC_PATHs = {
            "/",
            "/api/test",
            "/api/user/add",
            "/api/user/login"
    };

    public static Boolean isPublicPath(String requestURI) {
        for (String publicPath : PUBLIC_PATHs) {
            if (antPathMatcher.match(publicPath, requestURI)) {
                return true;
            }
        }
        return false;
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // 配置安全过滤链
        http
                //禁用CSRF保护（API服务通常不需要）
                .csrf(AbstractHttpConfigurer::disable)
                 // 配置会话管理为无状态（API服务通常不需要）
                .sessionManagement(SessionManagementConfigurer<HttpSecurity>::disable)
                //配置请求的授权规则
                .authorizeHttpRequests(auth -> auth
                        //设置公开的路径，无需登录即可访问
                        .requestMatchers(PUBLIC_PATHs).permitAll()
                        // 其他请求需要登录
                        .anyRequest().authenticated()
                )
                // 配置JWT认证过滤器
                .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
