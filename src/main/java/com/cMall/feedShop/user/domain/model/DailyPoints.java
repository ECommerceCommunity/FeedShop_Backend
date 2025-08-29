package com.cMall.feedShop.user.domain.model;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;

@Schema(description = "일별 활동 점수 DTO")
public class DailyPoints {
    @Schema(description = "날짜", example = "2024-01-01")
    private LocalDate date;
    
    @Schema(description = "해당 날짜의 총 활동 점수", example = "150")
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
