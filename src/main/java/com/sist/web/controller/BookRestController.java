package com.sist.web.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sist.web.dto.BookDetailPageDTO;
import com.sist.web.dto.BookItem;
import com.sist.web.dto.ListDTO;
import com.sist.web.dto.MainPageDTO;
import com.sist.web.service.BookService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/book")
@RequiredArgsConstructor
public class BookRestController {
	private final BookService bService;

	@GetMapping("/main")
	public ResponseEntity<MainPageDTO> mainPage() {
		MainPageDTO dto = bService.mainPageData();
		return ResponseEntity.ok(dto);
	}

	@GetMapping("/list/{page}")
	public ResponseEntity<ListDTO<BookItem>> bookList(@PathVariable("page") int page) {
		ListDTO<BookItem> dto = bService.bookListData(page);
		return ResponseEntity.ok(dto);
	}

	@GetMapping("/detail/{isbn}")
	public ResponseEntity<BookDetailPageDTO> bookDetail(@PathVariable("isbn") String isbn) {
		BookDetailPageDTO dto = bService.bookDetailData(isbn);
		return ResponseEntity.ok(dto);
	}
}
