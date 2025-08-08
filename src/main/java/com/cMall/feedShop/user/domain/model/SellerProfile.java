package com.cMall.feedShop.user.domain.model;


import com.cMall.feedShop.common.BaseTimeEntity;
import jakarta.persistence.*;

@Entity
@Table(name="seller_profile")
@AttributeOverrides({
        @AttributeOverride(name = "createdAt", column = @Column(name = "created_at")),
        @AttributeOverride(name = "updatedAt", column = @Column(name = "updated_at"))
})
public class SellerProfile extends BaseTimeEntity {
    @Id
    @Column(name = "user_id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "user_id", referencedColumnName = "user_id")
    private User user;


    @Column(name="business_number")
    private String businessNumber;



}
