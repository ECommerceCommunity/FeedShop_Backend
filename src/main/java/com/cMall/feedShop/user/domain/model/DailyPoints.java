package com.cMall.feedShop.user.domain.model;

import java.time.LocalDate;

public class DailyPoints {
    private LocalDate date;
    private Integer totalPoints;

    public DailyPoints(LocalDate date, Integer totalPoints) {
        this.date = date;
        this.totalPoints = totalPoints;
    }

    // getters
    public LocalDate getDate() {
        return date;
    }

    public Integer getTotalPoints() {
        return totalPoints;
    }
}
