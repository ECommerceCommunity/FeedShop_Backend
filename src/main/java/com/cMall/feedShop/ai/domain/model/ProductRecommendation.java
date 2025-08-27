package com.cMall.feedShop.ai.domain.model;

import com.cMall.feedShop.common.BaseTimeEntity;
import com.cMall.feedShop.user.domain.model.User;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "product_recommendations")
@Getter
@NoArgsConstructor
public class ProductRecommendation extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "recommendation_id")
    private Long recommendationId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "prompt", nullable = false, length = 500)
    private String prompt;

    @Column(name = "recommended_product_ids", columnDefinition = "JSON")
    private String recommendedProductIds;

    @Column(name = "response", columnDefinition = "TEXT")
    private String response;

    @Builder
    public ProductRecommendation(User user, String prompt, String recommendedProductIds, String response) {
        this.user = user;
        this.prompt = prompt;
        this.recommendedProductIds = recommendedProductIds;
        this.response = response;
    }

}
