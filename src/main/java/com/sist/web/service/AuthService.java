package com.sist.web.service;

import org.springframework.stereotype.Service;

import com.sist.web.dto.TokenResult;
import com.sist.web.entity.RefreshToken;
import com.sist.web.exception.CustomException;
import com.sist.web.exception.ErrorCode;
import com.sist.web.repository.RefreshTokenRepository;
import com.sist.web.token.JwtTokenProvider;

import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {

    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;

    // 반환 타입 TokenResponse → String (새 Access Token만 반환)
    public TokenResult refresh(String refreshTokenValue) {
        // 1. DB에서 Refresh Token 조회 — 없으면 이미 로그아웃된 것
        RefreshToken savedToken = refreshTokenRepository.findByToken(refreshTokenValue)
                .orElseThrow(() -> new CustomException(ErrorCode.TOKEN_INVALID));

        // 2. 토큰 파싱 (만료 시 TOKEN_EXPIRED 예외 발생)
        String subject;
        try {
            subject = jwtTokenProvider.getSubject(refreshTokenValue);
        } catch (CustomException e) {
            refreshTokenRepository.delete(savedToken);
            throw e;
        }
        String role    = savedToken.getMember().getRole().getKey();
        String newAccessToken = jwtTokenProvider.createAccessToken(subject, role);

        // 4. Refresh Token Rotation
        String newRefreshToken = jwtTokenProvider.createRefreshToken(subject);
        savedToken.updateToken(newRefreshToken);

        // 쿠키 설정은 Controller에서 — Service는 토큰 값만 반환
        return new TokenResult(newAccessToken, newRefreshToken);
    }

    // logout은 원래도 토큰 값만 받으면 됨
    public void logout(String refreshTokenValue) {
        RefreshToken savedToken = refreshTokenRepository.findByToken(refreshTokenValue)
                .orElseThrow(() -> new CustomException(ErrorCode.TOKEN_INVALID));

        refreshTokenRepository.delete(savedToken);
    }
}
