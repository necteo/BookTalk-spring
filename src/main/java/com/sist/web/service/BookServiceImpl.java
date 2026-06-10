package com.sist.web.service;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sist.web.commons.Pagination;
import com.sist.web.dto.BookDetailDTO;
import com.sist.web.dto.BookDetailPageDTO;
import com.sist.web.dto.BookItem;
import com.sist.web.dto.CommentDTO;
import com.sist.web.dto.ListDTO;
import com.sist.web.dto.MainPageDTO;
import com.sist.web.entity.Comment;
import com.sist.web.entity.WikiBook;
import com.sist.web.repository.BookRepository;
import com.sist.web.repository.CommentRepository;

import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class BookServiceImpl implements BookService {
	private final BookRepository bRepo;
	private final CommentRepository cRepo;

	@Override
	public MainPageDTO mainPageData() {
		BookItem main = new BookItem(bRepo.mainBookData().orElseThrow());
		
		Pageable pg = PageRequest.of(0, Pagination.MAIN_BLOCK, Sort.by("pubdate").descending());
		List<BookItem> newList = bRepo.findAll(pg).map(BookItem::new).toList();
		
		pg = PageRequest.of(0, Pagination.MAIN_BLOCK, Sort.by("hit").descending());
		List<BookItem> hitList = bRepo.findAll(pg).map(BookItem::new).toList();
		
		MainPageDTO dto = new MainPageDTO();
		dto.setMain(main);
		dto.setNewList(newList);
		dto.setHitList(hitList);
		return dto;
	}

	@Override
	public ListDTO<BookItem> bookListData(int page) {
		Pageable pg = PageRequest.of(page - 1, Pagination.ROW_SIZE, Sort.by("isbn").ascending());
		List<BookItem> list = bRepo.findAll(pg).map(BookItem::new).toList();
		int count = (int) bRepo.count();
		int totalpage = (int) (Math.ceil(1.0 * count / Pagination.ROW_SIZE));
		return Pagination.createPagination(list, page, totalpage);
	}

	@Override
	public BookDetailPageDTO bookDetailData(String isbn) {
		WikiBook book = bRepo.findById(isbn).orElseThrow();
		List<Comment> cList = cRepo.findByIsbn(isbn);
		BookDetailDTO detail = new BookDetailDTO(book);
		List<CommentDTO> comments = cList.stream().map(CommentDTO::new).toList();
		return new BookDetailPageDTO(detail, comments);
	}
}
