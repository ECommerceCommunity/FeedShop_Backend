package com.cMall.feedShop.user.domain.model;

import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Table(name = "users_profile")
@Getter
public class UserProfile {

    @Id
    @Column(name="user_id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId // User 엔티티의 PK를 UserProfile의 PK로 사용 (공유 기본 키 전략)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name="name")
    private String name;

    @Column(name="nickname")
    private String nickname;
}
