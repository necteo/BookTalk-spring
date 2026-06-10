package com.sist.web.dto;

// 수정 대상은 no로 식별, 작성자 검증은 서버에서 — 요청 본문엔 no, msg만
public record CommentUpdateRequest(int no, String msg) {}
