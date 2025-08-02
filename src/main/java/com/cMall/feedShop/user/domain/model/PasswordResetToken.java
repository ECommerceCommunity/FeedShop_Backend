package com.cMall.feedShop.user.domain.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "password_reset_tokens")
@Getter
@Setter
@NoArgsConstructor
public class PasswordResetToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String token; // 실제 재설정 토큰 값

    @OneToOne(targetEntity = User.class, fetch = FetchType.EAGER)
    @JoinColumn(nullable = false, name = "user_id")
    private User user;

    @Column(nullable = false)
    private LocalDateTime expiryDate; // 토큰 만료 시간

    // 생성자 (토큰 생성 시 사용)
    public PasswordResetToken(User user) {
        this.user = user;
        this.token = UUID.randomUUID().toString(); // UUID로 고유한 토큰 생성
        this.expiryDate = LocalDateTime.now().plusHours(24); // 24시간 후 만료 설정 (원하는 시간으로 변경 가능)
    }

    // 토큰 만료 여부 확인 메서드
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiryDate);
    }
}