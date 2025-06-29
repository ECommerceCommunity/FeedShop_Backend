package com.cMall.feedShop.user.domain.model;

import com.cMall.feedShop.user.domain.enums.UserRole;
import com.cMall.feedShop.user.domain.enums.UserStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority; // <-- 추가
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;


@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name="user_id")
    private UserProfile userProfile;

    @Column(name = "login_id", unique = true, nullable = false, length = 100)
    private String loginId;

    @Column(nullable = false, length = 255)
    private String password;

    @Column(name = "password_changed_at")
    private LocalDateTime passwordChangedAt;

    @Column(unique = true, nullable = false, length = 255)
    private String email;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "ENUM('ACTIVE', 'INACTIVE', 'BLOCKED', 'DELETED') DEFAULT 'ACTIVE'")
    private UserStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "ENUM('ROLE_USER', 'ROLE_ADMIN', 'ROLE_SELLER') DEFAULT 'ROLE_USER'") // ERD의 role
    private UserRole role;

    @Column(nullable = false, length = 20)
    private String phone;

    //(회원가입 시 사용)
    public User(String loginId, String password, String email, String phone, UserRole role) {
        this.loginId = loginId;
        this.password = password;
        this.email = email;
        this.phone = phone;
        this.role = role;
        this.status = UserStatus.ACTIVE; // 기본 상태 활성화
        // @CreatedDate, @LastModifiedDate가 자동 처리하므로 생성자에서 초기화 제거 가능
        // this.createdAt = LocalDateTime.now();
        // this.updatedAt = LocalDateTime.now();
        this.passwordChangedAt = LocalDateTime.now(); // 초기 비밀번호 변경 시간 설정
    }

    // 비즈니스 메서드
    public void changePassword(String newPassword) {
        // 도메인 규칙 검증
    }

    @Override
    public String getUsername() {
        return loginId;
    }

    // UserDetails 인터페이스의 다른 메서드 구현 (중요!)
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // 사용자의 역할을 Spring Security의 권한(GrantedAuthority)으로 변환하여 반환합니다.
        // UserRole.ROLE_USER -> new SimpleGrantedAuthority("ROLE_USER")
        return List.of(new SimpleGrantedAuthority(this.role.name()));
    }

    @Override
    public boolean isAccountNonExpired() {
        // 계정 만료 여부. 여기서는 항상 true를 반환하지만,
        // 필요에 따라 만료 일자를 User 엔티티에 추가하고 비교할 수 있습니다.
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        // 계정 잠금 여부. UserStatus를 활용하여 BLOCKED 상태일 경우 잠긴 것으로 간주할 수 있습니다.
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
        // 계정 활성화 여부. UserStatus가 ACTIVE일 때만 활성화된 것으로 간주합니다.
        return this.status == UserStatus.ACTIVE;
    }

    // canLogin() 메서드는 UserDetails의 isEnabled()와 역할이 중복되거나 유사할 수 있으므로
    // UserDetails의 isEnabled()를 사용하는 것을 권장합니다.
    // public boolean canLogin() {
    //     return status == UserStatus.ACTIVE;
    // }
}