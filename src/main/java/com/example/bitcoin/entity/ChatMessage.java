package com.example.bitcoin.entity;


import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Getter //클래스의 포함된 멤버 변수의 모든 getter 매서드를 생성
@Setter
@Builder // sql에 값 넣는것
@ToString // 객체의 값 확인
@AllArgsConstructor //생성자 자동 완성
@NoArgsConstructor //생성자 자동 완성
@EntityListeners(AuditingEntityListener.class)
@Entity(name="chat_message")
public class ChatMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "chat_message_id")
    private Long id;

    @Column(name = "user_name")
    private String username;

    @Column(name = "message")
    private String message;

    @Column(name = "image_url")  // 이미지 URL 저장할 필드 추가
    private String imageUrl;

    @CreatedDate
    @Column(updatable = false, name = "created_date")
    private LocalDateTime createdDate;

    @LastModifiedDate
    @Column(name = "updated_date")
    private LocalDateTime updatedDate;
}
