package com.sist.web.dto;

import java.time.format.DateTimeFormatter;

import com.sist.web.entity.Comment;

// 응답(목록 표시)용 — 작성자 id/name 포함 (누가 썼는지 + 본인 글 판단)
public record CommentDTO(
		int no,
		String isbn,
		Long id,
		String name,
		String msg,
		String dbday) {

	public CommentDTO(Comment comm) {
		this(
			comm.getNo(),
			comm.getIsbn(),
			comm.getId(),
			comm.getName(),
			comm.getMsg(),
			comm.getRegdate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
		);
	}
}
