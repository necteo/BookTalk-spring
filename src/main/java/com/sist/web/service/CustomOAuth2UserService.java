package com.sist.web.service;

import java.util.Collections;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sist.web.entity.AuthProvider;
import com.sist.web.entity.Member;
import com.sist.web.entity.Role;
import com.sist.web.info.OAuth2UserInfo;
import com.sist.web.info.OAuth2UserInfoFactory;
import com.sist.web.repository.MemberRepository;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final MemberRepository memberRepository;
    private final DefaultOAuth2UserService delegate = new DefaultOAuth2UserService();

    @Override
    public OAuth2User loadUser(OAuth2UserRequest request) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = delegate.loadUser(request);

        String registrationId = request.getClientRegistration().getRegistrationId();
        String userNameAttr = request.getClientRegistration()
                .getProviderDetails().getUserInfoEndpoint().getUserNameAttributeName();

        OAuth2UserInfo userInfo = OAuth2UserInfoFactory.of(registrationId, oAuth2User.getAttributes());
        AuthProvider provider = AuthProvider.valueOf(registrationId.toUpperCase());

        Member member = saveOrUpdate(userInfo, provider);

        return new DefaultOAuth2User(
                Collections.singleton(new SimpleGrantedAuthority(member.getRole().getKey())),
                oAuth2User.getAttributes(),
                userNameAttr
        );
    }

    private Member saveOrUpdate(OAuth2UserInfo info, AuthProvider provider) {
        // ✅ 항상 존재하는 providerId로 조회
        return memberRepository
            .findByProviderAndProviderId(provider, info.getProviderId())
            .map(m -> m.update(info.getName(), info.getPicture(), info.getEmail()))
            .orElseGet(() -> handleNewUser(info, provider));
    }

    private Member handleNewUser(OAuth2UserInfo info, AuthProvider provider) {
        // 이메일이 있으면 — 다른 소셜로 이미 가입했는지 확인
        if (info.getEmail() != null) {
            memberRepository.findByEmailAndProviderNot(info.getEmail(), provider)
                .ifPresent(existing -> {
                    throw new OAuth2AuthenticationException(
                        new OAuth2Error("email_already_exists"),
                        existing.getProvider() + "로 이미 가입된 이메일입니다."
                    );
                });
        }

        return memberRepository.save(Member.builder()
                .email(info.getEmail())       // null이어도 저장
                .name(info.getName())
                .picture(info.getPicture())
                .providerId(info.getProviderId())
                .provider(provider)
                .role(Role.USER)
                .build());
    }
}
