package com.sports.board;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.sports.Category.Category;
import com.sports.user.entito.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;
import java.util.Date;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Board {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId; // 게시글 작성자아이디

    private String title; // 제목

    @Column(columnDefinition = "TEXT")
    private String content; // 본문

    @Column(length = 2048)
    private String imgUrl;  // 이미지

    @CreationTimestamp
    @Column(updatable = false, nullable = false)
    private LocalDateTime createdAt; // 작성일

    @LastModifiedDate
    private LocalDateTime updatedAt; // 수정일

    @Column(nullable = false)
    private int likes;               // 좋아요

    @Column(nullable = false)
    private int views;               // 조회수

    // 게시글 작성자
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User author;

    // 게시글 카테고리
    @ManyToOne
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    // 채팅용 필드
    private boolean chatroom;

    // 위치 정보
    private Double latitude;   // 위도, 기본값 null
    private Double longitude;  // 경도, 기본값 null
}
