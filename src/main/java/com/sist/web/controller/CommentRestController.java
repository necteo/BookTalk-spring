package com.sist.web.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sist.web.dto.CommentDTO;
import com.sist.web.dto.CommentInsertRequest;
import com.sist.web.dto.CommentUpdateRequest;
import com.sist.web.dto.MemberResponse;
import com.sist.web.service.CommentService;
import com.sist.web.service.MemberService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/comment")
@RequiredArgsConstructor
public class CommentRestController {
	private final CommentService cService;
	private final MemberService memberService;

	@PostMapping("/insert")
	public ResponseEntity<List<CommentDTO>> commentInsert(@RequestBody CommentInsertRequest req,
			Authentication authentication) {
		MemberResponse me = memberService.getMe(authentication);
		return ResponseEntity.ok(cService.commentInsert(req, me.id(), me.name()));
	}

	@DeleteMapping("/delete/{no}")
	public ResponseEntity<List<CommentDTO>> commentDelete(@PathVariable("no") int no,
			Authentication authentication) {
		MemberResponse me = memberService.getMe(authentication);
		return ResponseEntity.ok(cService.commentDelete(no, me.id()));
	}

	@PutMapping("/update")
	public ResponseEntity<List<CommentDTO>> commentUpdate(@RequestBody CommentUpdateRequest req,
			Authentication authentication) {
		MemberResponse me = memberService.getMe(authentication);
		return ResponseEntity.ok(cService.commentUpdate(req, me.id()));
	}
}
