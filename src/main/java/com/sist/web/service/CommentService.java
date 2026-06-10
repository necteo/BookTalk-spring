package com.sist.web.service;

import java.util.List;

import com.sist.web.dto.CommentDTO;
import com.sist.web.dto.CommentInsertRequest;
import com.sist.web.dto.CommentUpdateRequest;

public interface CommentService {
	List<CommentDTO> commentListData(String isbn);
	List<CommentDTO> commentInsert(CommentInsertRequest req, Long memberId, String memberName);
	List<CommentDTO> commentUpdate(CommentUpdateRequest req, Long memberId);
	List<CommentDTO> commentDelete(int no, Long memberId);
}
