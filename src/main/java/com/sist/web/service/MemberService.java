package com.sist.web.service;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sist.web.dto.MemberResponse;
import com.sist.web.entity.AuthProvider;
import com.sist.web.entity.Member;
import com.sist.web.exception.CustomException;
import com.sist.web.exception.ErrorCode;
import com.sist.web.repository.MemberRepository;

import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;

    public MemberResponse getMe(Authentication authentication) {
        if (authentication == null || authentication instanceof AnonymousAuthenticationToken) {
            return null;
        }

        // JWT subject = "PROVIDER_providerId"
        String subject = (String) authentication.getPrincipal();
        String[] parts = subject.split("_", 2);

        Member member = memberRepository.findByProviderAndProviderId(
                AuthProvider.valueOf(parts[0]),
                parts[1]
        ).orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        return new MemberResponse(member.getId(), member.getName(), member.getEmail(), member.getPicture());
    }
}
