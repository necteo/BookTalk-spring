package com.sist.web.dto;

// 작성자(id/name)는 JWT에서 결정 — 요청 본문엔 isbn, msg만
public record CommentInsertRequest(String isbn, String msg) {}
