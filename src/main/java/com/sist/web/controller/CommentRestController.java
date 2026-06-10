package com.sist.web.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sist.web.dto.CommentDTO;
import com.sist.web.service.CommentService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/comment")
@RequiredArgsConstructor
public class CommentRestController {
	private final CommentService cService;

	@PostMapping("/insert")
	public ResponseEntity<List<CommentDTO>> commentInsert(@RequestBody CommentDTO dto) {
		cService.commentInsert(dto);
		List<CommentDTO> list = cService.commentListData(dto.isbn());
		return ResponseEntity.ok(list);
	}

	@DeleteMapping("/delete/{no}/{isbn}")
	public ResponseEntity<List<CommentDTO>> commentDelete(@PathVariable("no") int no, @PathVariable("isbn") String isbn) {
		cService.commentDelete(no);
		List<CommentDTO> list = cService.commentListData(isbn);
		return ResponseEntity.ok(list);
	}

	@PutMapping("/update")
	public ResponseEntity<List<CommentDTO>> commentUpdate(@RequestBody CommentDTO dto) {
		cService.commentUpdate(dto.no(), dto.msg());
		List<CommentDTO> list = cService.commentListData(dto.isbn());
		return ResponseEntity.ok(list);
	}
}