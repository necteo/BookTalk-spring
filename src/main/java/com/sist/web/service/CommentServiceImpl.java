package com.sist.web.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sist.web.dto.CommentDTO;
import com.sist.web.dto.CommentInsertRequest;
import com.sist.web.dto.CommentUpdateRequest;
import com.sist.web.entity.Comment;
import com.sist.web.exception.CustomException;
import com.sist.web.exception.ErrorCode;
import com.sist.web.repository.CommentRepository;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {
	private final CommentRepository cRepo;

	@Override
	public List<CommentDTO> commentListData(String isbn) {
		return cRepo.findByIsbn(isbn).stream().map(CommentDTO::new).toList();
	}

	@Override
	public List<CommentDTO> commentInsert(CommentInsertRequest req, Long memberId, String memberName) {
		Comment vo = new Comment();
		vo.setIsbn(req.isbn());
		vo.setId(memberId);       // 작성자는 JWT 기반 — client가 보낸 값 사용 안 함
		vo.setName(memberName);
		vo.setMsg(req.msg());
		vo.setRegdate(LocalDate.now());
		cRepo.save(vo);
		return commentListData(req.isbn());
	}

	@Override
	public List<CommentDTO> commentUpdate(CommentUpdateRequest req, Long memberId) {
		Comment vo = cRepo.findById(req.no())
				.orElseThrow(() -> new CustomException(ErrorCode.COMMENT_NOT_FOUND));
		checkOwner(vo, memberId);
		vo.setMsg(req.msg());   // @Transactional Dirty Checking으로 자동 반영
		return commentListData(vo.getIsbn());
	}

	@Override
	public List<CommentDTO> commentDelete(int no, Long memberId) {
		Comment vo = cRepo.findById(no)
				.orElseThrow(() -> new CustomException(ErrorCode.COMMENT_NOT_FOUND));
		checkOwner(vo, memberId);
		String isbn = vo.getIsbn();
		cRepo.delete(vo);
		return commentListData(isbn);
	}

	// 본인 글만 수정/삭제 가능
	private void checkOwner(Comment comment, Long memberId) {
		if (comment.getId() == null || !comment.getId().equals(memberId)) {
			throw new CustomException(ErrorCode.NO_PERMISSION);
		}
	}
}
