package com.sist.web.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

import com.sist.web.filter.JwtAuthenticationFilter;
import com.sist.web.handler.OAuth2AuthenticationFailureHandler;
import com.sist.web.handler.OAuth2AuthenticationSuccessHandler;
import com.sist.web.service.CustomOAuth2UserService;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomOAuth2UserService oAuth2UserService;
    private final OAuth2AuthenticationSuccessHandler successHandler;
    private final OAuth2AuthenticationFailureHandler failureHandler;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CorsConfigurationSource corsConfigurationSource;

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
        	.cors(cors -> cors.configurationSource(corsConfigurationSource))
            // REST API이므로 CSRF, Form 로그인, HTTP Basic 비활성화
            .csrf(AbstractHttpConfigurer::disable)
            .formLogin(AbstractHttpConfigurer::disable)
            .httpBasic(AbstractHttpConfigurer::disable)
            .logout(AbstractHttpConfigurer::disable)

            // Stateless 세션
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )

            // 요청별 권한 설정
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/", "/error").permitAll()
                .requestMatchers("/api/book/**").permitAll()
                .requestMatchers("/api/comment/**").permitAll()
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/login/oauth2/**", "/oauth2/**").permitAll()
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )

            // OAuth2 로그인 설정
            .oauth2Login(oauth2 -> oauth2
                .authorizationEndpoint(endpoint ->
                    endpoint.baseUri("/oauth2/authorization")
                )
                .redirectionEndpoint(endpoint ->
                    endpoint.baseUri("/login/oauth2/code/*")
                )
                .userInfoEndpoint(userInfo ->
                    userInfo.userService(oAuth2UserService)
                )
                .successHandler(successHandler)
                .failureHandler(failureHandler)
            )

            // 인증 예외 처리
            .exceptionHandling(exception -> exception
                .authenticationEntryPoint((request, response, e) -> {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.getWriter().write("{\"error\": \"인증이 필요합니다.\"}");
                })
                .accessDeniedHandler((request, response, e) -> {
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    response.getWriter().write("{\"error\": \"권한이 없습니다.\"}");
                })
            )

            // JWT 필터를 UsernamePasswordAuthenticationFilter 앞에 배치
            .addFilterBefore(jwtAuthenticationFilter,
                UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
