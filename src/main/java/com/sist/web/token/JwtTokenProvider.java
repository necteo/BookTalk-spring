package com.sist.web.token;

import java.nio.charset.StandardCharsets;
import java.time.Instant;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.BadJwtException;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.JwtValidationException;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.stereotype.Component;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import com.sist.web.exception.CustomException;
import com.sist.web.exception.ErrorCode;

@Component
public class JwtTokenProvider {

    private final SecretKey secretKey;
    private final JwtEncoder jwtEncoder;
    private final JwtDecoder jwtDecoder;

    @Value("${jwt.expiration}")
    private long expiration;

    @Value("${jwt.refresh-expiration}")
    private long refreshExpiration;

    public JwtTokenProvider(@Value("${jwt.secret}") String secret) {
        // 32바이트 이상의 시크릿으로 HMAC-SHA256 키 생성
        this.secretKey = new SecretKeySpec(
            secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"
        );
        this.jwtEncoder = new NimbusJwtEncoder(new ImmutableSecret<>(secretKey));
        this.jwtDecoder = NimbusJwtDecoder.withSecretKey(secretKey)
                            .macAlgorithm(MacAlgorithm.HS256)
                            .build();
    }

    // Access Token 생성
    public String createAccessToken(String subject, String role) {
        return createToken(subject, role, expiration);
    }

    // Refresh Token 생성
    public String createRefreshToken(String subject) {
        return createToken(subject, null, refreshExpiration);
    }

    private String createToken(String subject, String role, long expirationMs) {
        Instant now = Instant.now();

        JwtClaimsSet.Builder claimsBuilder = JwtClaimsSet.builder()
                .subject(subject)
                .issuedAt(now)
                .expiresAt(now.plusMillis(expirationMs));

        // role이 있을 때만 claim 추가 (refresh token은 role 불필요)
        if (role != null) {
            claimsBuilder.claim("role", role);
        }

        JwtEncoderParameters params = JwtEncoderParameters.from(
            JwsHeader.with(MacAlgorithm.HS256).build(),
            claimsBuilder.build()
        );

        return jwtEncoder.encode(params).getTokenValue();
    }

    // 토큰 검증 및 파싱
    public Jwt validateAndParse(String token) {
        try {
            return jwtDecoder.decode(token);
        } catch (JwtValidationException e) {
            // errors 안에 만료 관련 메시지가 있는지 확인
            boolean isExpired = e.getErrors().stream()
                    .anyMatch(error -> error.getDescription().contains("expired"));

            if (isExpired) {
                throw new CustomException(ErrorCode.TOKEN_EXPIRED);
            }
            throw new CustomException(ErrorCode.TOKEN_INVALID);

        } catch (BadJwtException e) {
            // 서명 불일치, 형식 오류
            throw new CustomException(ErrorCode.TOKEN_INVALID);

        } catch (JwtException e) {
            throw new CustomException(ErrorCode.TOKEN_INVALID);
        }
    }

    public String getSubject(String token) {
        return validateAndParse(token).getSubject();
    }

}
