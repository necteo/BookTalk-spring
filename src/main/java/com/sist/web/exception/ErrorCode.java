package com.sist.web.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    TOKEN_EXPIRED("토큰이 만료되었습니다.",   HttpStatus.UNAUTHORIZED),
    TOKEN_INVALID("유효하지 않은 토큰입니다.", HttpStatus.UNAUTHORIZED),
    TOKEN_NOT_FOUND("없는 토큰입니다.",        HttpStatus.UNAUTHORIZED),
    MEMBER_NOT_FOUND("없는 회원입니다.",       HttpStatus.NOT_FOUND),
    COMMENT_NOT_FOUND("없는 댓글입니다.",      HttpStatus.NOT_FOUND),
    NO_PERMISSION("권한이 없습니다.",          HttpStatus.FORBIDDEN);

    private final String message;
    private final HttpStatus status;
}
