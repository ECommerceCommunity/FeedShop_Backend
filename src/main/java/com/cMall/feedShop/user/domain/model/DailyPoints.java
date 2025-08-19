package com.cMall.feedShop.user.domain.model;

import java.time.LocalDate;

public class DailyPoints {
    private LocalDate date;
    private Long totalPoints;

    public DailyPoints(LocalDate date, Long totalPoints) {
        this.date = date;
        this.totalPoints = totalPoints;
    }

    // getters
    public LocalDate getDate() {
        return date;
    }

    public Long getTotalPoints() {
        return totalPoints;
    }
}
