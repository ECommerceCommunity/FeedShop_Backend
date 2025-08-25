package com.cMall.feedShop.user.domain.model;

import com.cMall.feedShop.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 사용자 소셜 로그인 제공자 정보 엔티티
 * 한 사용자가 여러 소셜 로그인 제공자를 연동할 수 있도록 별도 테이블로 관리
 */
@Entity
@Table(name = "users_social_providers")
@Getter
@Setter
@NoArgsConstructor
public class UserSocialProvider extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "social_user_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "provider", nullable = false, length = 50)
    private String provider; // google, kakao, naver 등

    @Column(name = "provider_social_user_id", nullable = false, length = 100)
    private String providerSocialUserId; // 소셜 로그인 제공자에서 제공하는 고유 ID

    @Column(name = "social_email", length = 255)
    private String socialEmail; // 소셜 로그인에서 가져온 이메일

    @Column(name = "connected_at")
    private java.time.LocalDateTime connectedAt; // 연동된 시간

    // 생성자
    public UserSocialProvider(User user, String provider, String providerSocialUserId, String socialEmail) {
        this.user = user;
        this.provider = provider;
        this.providerSocialUserId = providerSocialUserId;
        this.socialEmail = socialEmail;
        this.connectedAt = java.time.LocalDateTime.now();
    }

    // 비즈니스 메서드
    public void updateSocialInfo(String socialEmail) {
        this.socialEmail = socialEmail;
    }

    public boolean isFromProvider(String provider) {
        return this.provider.equals(provider);
    }
}