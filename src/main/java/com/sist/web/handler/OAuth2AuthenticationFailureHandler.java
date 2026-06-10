package com.sist.web.handler;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.web.WebAttributes;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
@Component
public class OAuth2AuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    @Value("${oauth2.redirect-url}")
    private String redirectUri;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        AuthenticationException exception) throws IOException {
        String errorMessage = resolveErrorMessage(exception);

        // 실패 원인을 쿼리파라미터로 프론트에 전달
        String targetUrl = UriComponentsBuilder.fromUriString(redirectUri + "/oauth2/error")
                .queryParam("error", URLEncoder.encode(errorMessage, StandardCharsets.UTF_8))
                .build().toUriString();

        // 세션에 남은 인증 관련 데이터 정리
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.removeAttribute(WebAttributes.AUTHENTICATION_EXCEPTION);
        }

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }

    private String resolveErrorMessage(AuthenticationException exception) {
        // OAuth2AuthenticationException이면 error code로 세분화
        if (exception instanceof OAuth2AuthenticationException oauth2Ex) {
            return switch (oauth2Ex.getError().getErrorCode()) {
                case "email_already_exists" -> "이미 다른 소셜 계정으로 가입된 이메일입니다.";
                case "invalid_token"        -> "유효하지 않은 소셜 토큰입니다.";
                case "access_denied"        -> "소셜 로그인 접근이 거부되었습니다.";
                default                     -> "소셜 로그인에 실패했습니다.";
            };
        }
        return "인증 처리 중 오류가 발생했습니다.";
    }
}
