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
@AllArgsConstructor //생성자 자동 완성
@NoArgsConstructor //생성자 자동 완성
@EntityListeners(AuditingEntityListener.class)
@Entity(name="food")
public class Food {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "food_id")
    private Long id;

    @Column(name = "food_name", unique = true)
    private String foodName;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private FoodCategory category;

    @Column(name = "food_img")
    private String foodImg;

    @Enumerated(EnumType.STRING)
    @Column(name = "food_type")
    private FoodType foodType; // 추가된 부분

    @CreatedDate
    @Column(updatable = false, name = "created_date")
    private LocalDateTime createdDate;

    @LastModifiedDate
    @Column(name = "updated_date")
    private LocalDateTime updatedDate;

    public enum FoodType {
        점심,
        술안주,
        점심과술안주
    }
}