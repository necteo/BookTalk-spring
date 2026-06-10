package com.sist.web.exception;

import java.sql.SQLException;
import java.util.NoSuchElementException;

import org.hibernate.TypeMismatchException;
import org.hibernate.query.sqm.sql.ConversionException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConversionException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class BookExceptionHandler {
    @ExceptionHandler(CustomException.class)
    public ResponseEntity<String> handleCustomException(CustomException e) {
        log.error("Custom error: {}", e.getErrorCode().getMessage());
        return ResponseEntity.status(e.getErrorCode().getStatus()).body(e.getErrorCode().getMessage());
    }

    @ExceptionHandler(TypeMismatchException.class)
    public ResponseEntity<String> handleTypeMismatchException(TypeMismatchException e) {
    	log.error("Type mismatch error", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
    }
	
	@ExceptionHandler(HttpMessageConversionException.class)
	public ResponseEntity<String> handleHttpMessageConversionException(HttpMessageConversionException e) {
		log.error("HTTP message conversion error", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
    }
	
	@ExceptionHandler(NoSuchElementException.class)
	public ResponseEntity<String> handleNoSuchElementException(NoSuchElementException e) {
		log.error("No such element error", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
    }
	
	@ExceptionHandler(ClassCastException.class)
	public ResponseEntity<String> handleClassCastException(ClassCastException e) {
		log.error("Class cast error", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
    }
	
	@ExceptionHandler(ConversionException.class)
	public ResponseEntity<String> handleConverterException(ConversionException e) {
		log.error("Conversion error", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
    }
	
	@ExceptionHandler(SQLException.class)
    public ResponseEntity<String> handleSQLException(SQLException e) {
		log.error("SQL error", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
    }
}
