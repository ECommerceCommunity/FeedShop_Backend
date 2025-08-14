package com.cMall.feedShop.user.domain.model;

import com.cMall.feedShop.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "user_addresses")
@Getter
@NoArgsConstructor
public class UserAddress extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "address_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "recipient_name", nullable = false, length = 100)
    private String recipientName;

    @Column(name = "recipient_phone", nullable = false, length = 20)
    private String recipientPhone;

    @Column(name = "zip_code", nullable = false, length = 20)
    private String zipCode;

    @Column(name = "address_line1", nullable = false, length = 255)
    private String addressLine1;

    @Column(name = "address_line2", length = 255)
    private String addressLine2;

    @Column(name = "is_default", nullable = false)
    private boolean isDefault;

    @Builder
    public UserAddress(User user, String recipientName, String recipientPhone, String zipCode, String addressLine1, String addressLine2, boolean isDefault) {
        this.user = user;
        this.recipientName = recipientName;
        this.recipientPhone = recipientPhone;
        this.zipCode = zipCode;
        this.addressLine1 = addressLine1;
        this.addressLine2 = addressLine2;
        this.isDefault = isDefault;
    }

    public void updateAddress(String recipientName, String recipientPhone, String zipCode, String addressLine1, String addressLine2, boolean isDefault) {
        this.recipientName = recipientName;
        this.recipientPhone = recipientPhone;
        this.zipCode = zipCode;
        this.addressLine1 = addressLine1;
        this.addressLine2 = addressLine2;
        this.isDefault = isDefault;
    }
<<<<<<< HEAD

    public void updateDefault(boolean isDefault) {
        this.isDefault = isDefault;
    }
=======
>>>>>>> rewardChange
}
