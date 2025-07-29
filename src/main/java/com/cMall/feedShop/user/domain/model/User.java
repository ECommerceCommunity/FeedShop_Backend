package com.cMall.feedShop.user.domain.model;

import com.cMall.feedShop.cart.domain.model.Cart;
import com.cMall.feedShop.common.BaseTimeEntity;
import com.cMall.feedShop.user.domain.enums.UserRole;
import com.cMall.feedShop.user.domain.enums.UserStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;


@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@Slf4j
//@EntityListeners(AuditingEntityListener.class)
public class User extends BaseTimeEntity implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    @OneToOne(mappedBy ="user", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private UserProfile userProfile;

    @OneToOne(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Cart cart;

    @Column(name = "login_id", unique = true, nullable = false, length = 100)
    private String loginId;

    @Column(nullable = false, length = 255)
    private String password;

    @Column(name = "password_changed_at")
    private LocalDateTime passwordChangedAt;

    @Column(unique = true, nullable = false, length = 255)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "ENUM('ACTIVE', 'INACTIVE', 'BLOCKED', 'PENDING', 'DELETED') DEFAULT 'ACTIVE'")
    private UserStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "ENUM('USER', 'ADMIN', 'SELLER') DEFAULT 'USER'") // ERD의 role
    private UserRole role;

    @Column(name = "verification_token", length = 36)
    private String verificationToken;

    @Column(name = "verification_token_expiry")
    private LocalDateTime verificationTokenExpiry;

    //(회원가입 시 사용)
    public User(String loginId, String password, String email, UserRole role) {
        this.loginId = loginId;
        this.password = password;
        this.email = email;
        this.role = role;
        this.status = UserStatus.PENDING;
        this.passwordChangedAt = LocalDateTime.now(); // 초기 비밀번호 변경 시간 설정
    }

    public User(Long id, String loginId, String password, String email, UserRole role) {
        this.id = id;
        this.loginId = loginId;
        this.password = password;
        this.email = email;
        this.role = role;
        this.status = UserStatus.ACTIVE; // 테스트용으로 ACTIVE 상태가 적합할 수 있습니다.
        this.passwordChangedAt = LocalDateTime.now();
    }

    public void setUserProfile(UserProfile userProfile) {
        this.userProfile = userProfile;
        if (userProfile != null && userProfile.getUser() != this) {
            userProfile.setUser(this); // 양방향 관계 동기화
        }
    }

    // 비즈니스 메서드
    public void changePassword(String newPassword) {
        // 도메인 규칙 검증
    }

    @Override
    public String getUsername() {
        return loginId;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // 사용자의 역할을 Spring Security의 권한(GrantedAuthority)으로 변환하여 반환합니다.
        // UserRole.USER -> new SimpleGrantedAuthority("USER")
        Collection<? extends GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_"+this.role.name()));
        log.debug("User entity authorities: {}", authorities);
        return authorities;
    }

    @Override
    public boolean isAccountNonExpired() {
        // 계정 만료 여부. 여기서는 항상 true를 반환하지만,
        // 필요에 따라 만료 일자를 User 엔티티에 추가하고 비교할 수 있습니다.
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return this.status != UserStatus.BLOCKED;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        // 비밀번호 만료 여부. passwordChangedAt을 활용하여 특정 기간이 지나면 만료되도록 할 수 있습니다.
        // 여기서는 항상 true를 반환하지만, 실제 서비스에서는 보안 정책에 따라 구현해야 합니다.
        return true;
    }

    @Override
    public boolean isEnabled() {
        return this.status == UserStatus.ACTIVE;
    }

}
