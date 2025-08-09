package com.cMall.feedShop.user.domain.model;

import com.cMall.feedShop.common.BaseTimeEntity;
import com.cMall.feedShop.user.domain.enums.Gender;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "user_profiles")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AttributeOverrides({
        @AttributeOverride(name = "createdAt", column = @Column(name = "created_at")),
        @AttributeOverride(name = "updatedAt", column = @Column(name = "updated_at"))
})
public class UserProfile extends BaseTimeEntity {
    @Id
    @Column(name = "user_id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "user_id", referencedColumnName = "user_id")
    private User user;

    @Column(name="name")
    private String name;

    @Column(name="nickname")
    private String nickname;

    @Column(nullable = false, length = 20)
    private String phone;

    @Column(nullable = false, length = 20)
    private Gender gender;

    @Column(name="birth_date")
    private LocalDate birthDate;

    @Column(name="height")
    private Integer height;

    @Column(name="foot_size")
    private Integer footSize;

    @Column(name="profile_image_url")
    private String profileImageUrl;

    @Builder
    public UserProfile(User user, String name, String nickname, String phone,
                       Gender gender, LocalDate birthDate,
                       Integer height, Integer footSize, String profileImageUrl) {
        this.user = user;
        this.name = name;
        this.nickname = nickname;
        this.phone = phone;
        this.gender = gender;
        this.birthDate = birthDate;
        this.height = height;
        this.footSize = footSize;
        this.profileImageUrl = profileImageUrl;
    }

    // 양방향 관계를 설정하는 메서드를 생성자 밖에서 정의
    public void setUser(User user) {
        if (this.user != null) {
            this.user.setUserProfile(null); // 기존 연관관계 제거
        }
        this.user = user;
        if (user != null) {
            user.setUserProfile(this); // 새로운 연관관계 설정
        }
    }

    public void updateProfile(String name, String nickname, String phone, Integer height, Integer footSize, Gender gender, LocalDate birthDate) {
        if (name != null) {
            this.name = name;
        }

        if (nickname != null) {
            if (nickname.length() < 2) {
                throw new IllegalArgumentException("닉네임은 2자 이상이어야 합니다.");
            }
            this.nickname = nickname;
        }

        if (phone != null) {
            if (!phone.matches("\\d{10,11}")) {
                throw new IllegalArgumentException("올바른 전화번호 형식이 아닙니다.");
            }
            this.phone = phone;
        }

        if (height != null) {
            this.height = height;
        }

        if (footSize != null) {
            this.footSize = footSize;
        }

        if(gender != null) {
            this.gender = gender;
        }

        if(birthDate != null) {
            this.birthDate = birthDate;
        }
    }

    // 프로필 이미지만 업데이트하는 메서드
    public void updateProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

}
