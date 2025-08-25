package com.cMall.feedShop.user.domain.model;

import com.cMall.feedShop.common.BaseTimeEntity;
import com.cMall.feedShop.user.domain.enums.MfaType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "user_mfa")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserMfa extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "mfa_id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", unique = true)
    private User user;

    @Column(name = "secret_key")
    private String secretKey;

    @Column(name = "temp_secret_key")
    private String tempSecretKey;

    @Column(name = "is_enabled", nullable = false)
    @Builder.Default
    private Boolean isEnabled = false;

    @Column(name = "backup_codes", columnDefinition = "TEXT")
    private String backupCodes; // JSON 형태로 저장

    @Enumerated(EnumType.STRING)
    @Column(name = "mfa_type", nullable = false)
    @Builder.Default
    private MfaType mfaType = MfaType.TOTP;

    // 비즈니스 메서드
    public void enableMfa() {
        if (this.tempSecretKey != null) {
            this.secretKey = this.tempSecretKey;
            this.tempSecretKey = null;
        }
        this.isEnabled = true;
    }

    public void disableMfa() {
        this.isEnabled = false;
        this.secretKey = null;
        this.tempSecretKey = null;
        this.backupCodes = null;
    }

    public void setTempSecret(String tempSecret) {
        this.tempSecretKey = tempSecret;
    }

    public void setBackupCodes(String backupCodes) {
        this.backupCodes = backupCodes;
    }

    public String getActiveSecret() {
        return isEnabled ? secretKey : tempSecretKey;
    }
}