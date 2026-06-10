package com.sist.web.handler;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.WebAttributes;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.sist.web.entity.AuthProvider;
import com.sist.web.entity.Member;
import com.sist.web.entity.RefreshToken;
import com.sist.web.exception.CustomException;
import com.sist.web.exception.ErrorCode;
import com.sist.web.info.OAuth2UserInfo;
import com.sist.web.info.OAuth2UserInfoFactory;
import com.sist.web.repository.MemberRepository;
import com.sist.web.repository.RefreshTokenRepository;
import com.sist.web.token.JwtTokenProvider;
import com.sist.web.util.CookieUtil;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final MemberRepository memberRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final CookieUtil cookieUtil;

    @Value("${oauth2.redirect-url}")
    private String redirectUri;

    @Transactional
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        OAuth2AuthenticationToken authToken = (OAuth2AuthenticationToken) authentication;
        String registrationId = authToken.getAuthorizedClientRegistrationId();
        AuthProvider provider = AuthProvider.valueOf(registrationId.toUpperCase());

        OAuth2UserInfo userInfo = OAuth2UserInfoFactory.of(registrationId, oAuth2User.getAttributes());
        Member member = memberRepository
                .findByProviderAndProviderId(provider, userInfo.getProviderId())
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        String subject = provider + "_" + userInfo.getProviderId();

        // JWT 발급
        String accessToken  = jwtTokenProvider.createAccessToken(subject, member.getRole().getKey());
        String refreshToken = jwtTokenProvider.createRefreshToken(subject);

        // RefreshToken DB 저장
        RefreshToken saved = refreshTokenRepository.findByMember(member)
                .map(t -> t.updateToken(refreshToken))
                .orElseGet(() -> RefreshToken.builder()
                        .member(member)
                        .token(refreshToken)
                        .build());
        refreshTokenRepository.save(saved);

        // OAuth2 임시 인증 정보 삭제
        super.clearAuthenticationAttributes(request);
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.removeAttribute(WebAttributes.AUTHENTICATION_EXCEPTION);
        }

        // JWT를 HttpOnly 쿠키에 직접 설정
        cookieUtil.addAccessTokenCookie(response, accessToken);
        cookieUtil.addRefreshTokenCookie(response, refreshToken);

        // 추가 정보 입력 필요 여부에 따라 redirect
        String targetUrl = member.getEmail() == null
                ? redirectUri + "/additional-info"
                : redirectUri + "/";

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }

}
