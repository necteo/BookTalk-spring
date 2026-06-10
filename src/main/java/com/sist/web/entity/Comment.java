package com.sist.web.entity;

import java.time.LocalDate;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "comments")
@Getter
@Setter
@DynamicInsert
@DynamicUpdate
public class Comment {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int no;
	
	@Column(updatable = false, insertable = true)
	private String isbn;
	
	@Column(updatable = false, insertable = true)
	private Long id;
	
	private String name;
	private String msg;
	private LocalDate regdate;
}
