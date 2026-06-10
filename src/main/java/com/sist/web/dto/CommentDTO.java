package com.sist.web.dto;

import java.time.format.DateTimeFormatter;

import com.sist.web.entity.Comment;

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
	
	public Comment createComment() {
		Comment vo = new Comment();
		vo.setIsbn(this.isbn);
		vo.setId(this.id);
		vo.setName(this.name);
		vo.setMsg(this.msg);
		return vo;
	}
}
