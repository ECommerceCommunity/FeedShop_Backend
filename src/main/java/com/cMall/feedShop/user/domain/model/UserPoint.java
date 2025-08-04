package com.cMall.feedShop.user.domain.model;

import com.cMall.feedShop.common.BaseTimeEntity;
import com.cMall.feedShop.common.exception.ErrorCode;
import com.cMall.feedShop.user.domain.exception.UserException;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "user_points")
@Getter
@NoArgsConstructor
public class UserPoint extends BaseTimeEntity {

    @Id
    @Column(name = "user_id")
    private Long userId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "current_points")
    private Integer currentPoints;

    @Builder
    public UserPoint(User user, Integer currentPoints) {
        this.user = user;
        this.currentPoints = currentPoints;
    }

    // 포인트 사용 가능 여부 확인
    public boolean canUsePoints(Integer points) {
        return points != null && points <= this.currentPoints;
    }

    // 포인트 사용
    public void usePoints(Integer points) {
        if (points == null || points <= 0) {
            return;
        }

        if (this.currentPoints < points) {
            throw new UserException(ErrorCode.OUT_OF_POINT);
        }

        this.currentPoints -= points;
    }

    // 포인트 적립
    public void earnPoints(Integer points) {
        if (points != null && points > 0) {
            this.currentPoints += points;
        }
    }
}
