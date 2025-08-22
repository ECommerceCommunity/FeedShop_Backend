package com.cMall.feedShop.user.domain.model;

import com.cMall.feedShop.cart.domain.model.Cart;
import com.cMall.feedShop.cart.domain.model.WishList;
import com.cMall.feedShop.common.BaseTimeEntity;
import com.cMall.feedShop.order.domain.model.Order;
import com.cMall.feedShop.user.domain.enums.UserRole;
import com.cMall.feedShop.user.domain.enums.UserStatus;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.UUID;


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
    private UserPoint userPoint;

    @OneToOne(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Cart cart;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<WishList> wishlist;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Order> orders;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserAddress> addresses = new java.util.ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserSocialProvider> socialProviders = new java.util.ArrayList<>();

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
    @Column(nullable = false, columnDefinition = "ENUM('USER', 'ADMIN', 'SELLER') DEFAULT 'USER'")
    private UserRole role;

    @Column(name = "verification_token", length = 36)
    private String verificationToken;

    @Column(name = "verification_token_expiry")
    private LocalDateTime verificationTokenExpiry;

    @Builder
    public User(String loginId, String password, String email, UserRole role) {
        this.loginId = loginId;
        this.password = password;
        this.email = email;
        this.role = role;
        this.status = UserStatus.PENDING;
        this.passwordChangedAt = LocalDateTime.now();
    }

    // 소셜 로그인용 생성자
    public User(String loginId, String email, UserRole role) {
        this.loginId = loginId;
        this.password = UUID.randomUUID().toString(); // 소셜 로그인은 비밀번호가 없으므로 임시값
        this.email = email;
        this.role = role;
        this.status = UserStatus.ACTIVE; // 소셜 로그인은 즉시 활성화
        this.passwordChangedAt = LocalDateTime.now();
    }

    public User(Long id, String loginId, String password, String email, UserRole role) {
        this.id = id;
        this.loginId = loginId;
        this.password = password;
        this.email = email;
        this.role = role;
        this.status = UserStatus.ACTIVE;
        this.passwordChangedAt = LocalDateTime.now();
    }

    public void setUserProfile(UserProfile userProfile) {
        this.userProfile = userProfile;
        if (userProfile != null && userProfile.getUser() != this) {
            userProfile.setUser(this);
        }
    }

    // 비즈니스 메서드
    public void changePassword(String newPassword) {
        // 도메인 규칙 검증
    }

    // 소셜 로그인 관련 헬퍼 메서드
    public boolean isSocialUser() {
        return !socialProviders.isEmpty();
    }

    public void addSocialProvider(UserSocialProvider socialProvider) {
        this.socialProviders.add(socialProvider);
        socialProvider.setUser(this);
    }

    public void removeSocialProvider(UserSocialProvider socialProvider) {
        this.socialProviders.remove(socialProvider);
        socialProvider.setUser(null);
    }

    public boolean hasSocialProvider(String provider) {
        return socialProviders.stream()
            .anyMatch(sp -> sp.getProvider().equals(provider));
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
