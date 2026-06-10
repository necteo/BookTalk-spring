package com.sist.web.filter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.sist.web.exception.CustomException;
import com.sist.web.exception.ErrorCode;
import com.sist.web.token.JwtTokenProvider;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    // JWT 파싱을 건너뛸 경로 — 실제 인가는 SecurityConfig에서 처리
    private static final List<String> WHITELIST = List.of(
        "/", "/error", "/login", "/oauth2",
        "/api/book", "/api/auth"
    );

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        // 경로 세그먼트 경계로 매칭 — "/api/book"이 "/api/bookmark"를 잡지 않도록
        return WHITELIST.stream().anyMatch(white ->
            path.equals(white) || path.startsWith(white + "/")
        );
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String token = resolveToken(request);

        if (token != null) {
            try {
                Jwt jwt = jwtTokenProvider.validateAndParse(token);

                String subject = jwt.getSubject();             // "KAKAO_12345678"
                String role = jwt.getClaimAsString("role");    // "ROLE_USER"

                List<GrantedAuthority> authorities = role != null
                    ? List.of(new SimpleGrantedAuthority(role))
                    : List.of();

                UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(subject, null, authorities);

                // 요청 정보를 details에 저장 (감사 로그 등에 활용 가능)
                authentication.setDetails(
                    new WebAuthenticationDetailsSource().buildDetails(request)
                );

                SecurityContextHolder.getContext().setAuthentication(authentication);

            } catch (CustomException e) {
                // 토큰 오류 시 SecurityContext 비우고 401 반환
                log.warn("JWT 검증 실패: {}", e.getErrorCode().getMessage());
                SecurityContextHolder.clearContext();
                sendErrorResponse(response, e.getErrorCode());
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private String resolveToken(HttpServletRequest request) {
    	if (request.getCookies() == null) return null;

        return Arrays.stream(request.getCookies())
                .filter(cookie -> "access_token".equals(cookie.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);
    }

    private void sendErrorResponse(HttpServletResponse response,
                                   ErrorCode errorCode) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(
            "{\"error\": \"" + errorCode.getMessage() + "\"}"
        );
    }
}
