package com.cMall.feedShop.review.domain.enums;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("ReportReason 열거형 테스트")
class ReportReasonTest {

    @Test
    @DisplayName("모든 신고 사유가 올바른 설명을 가지고 있는지 확인")
    void reportReasons_HaveCorrectDescriptions() {
        assertThat(ReportReason.ABUSIVE_LANGUAGE.getDescription()).isEqualTo("욕설 및 비방");
        assertThat(ReportReason.SPAM.getDescription()).isEqualTo("스팸 및 도배");
        assertThat(ReportReason.INAPPROPRIATE_CONTENT.getDescription()).isEqualTo("부적절한 내용");
        assertThat(ReportReason.FALSE_INFORMATION.getDescription()).isEqualTo("허위 정보");
        assertThat(ReportReason.ADVERTISING.getDescription()).isEqualTo("광고성 내용");
        assertThat(ReportReason.COPYRIGHT_VIOLATION.getDescription()).isEqualTo("저작권 침해");
        assertThat(ReportReason.OTHER.getDescription()).isEqualTo("기타");
    }

    @Test
    @DisplayName("신고 사유 개수가 7개인지 확인")
    void reportReasons_Count() {
        ReportReason[] reasons = ReportReason.values();
        assertThat(reasons).hasSize(7);
    }

    @Test
    @DisplayName("신고 사유별 toString 메서드 동작 확인")
    void reportReasons_ToString() {
        assertThat(ReportReason.ABUSIVE_LANGUAGE.toString()).isEqualTo("ABUSIVE_LANGUAGE");
        assertThat(ReportReason.SPAM.toString()).isEqualTo("SPAM");
        assertThat(ReportReason.INAPPROPRIATE_CONTENT.toString()).isEqualTo("INAPPROPRIATE_CONTENT");
        assertThat(ReportReason.FALSE_INFORMATION.toString()).isEqualTo("FALSE_INFORMATION");
        assertThat(ReportReason.ADVERTISING.toString()).isEqualTo("ADVERTISING");
        assertThat(ReportReason.COPYRIGHT_VIOLATION.toString()).isEqualTo("COPYRIGHT_VIOLATION");
        assertThat(ReportReason.OTHER.toString()).isEqualTo("OTHER");
    }

    @Test
    @DisplayName("valueOf 메서드로 정상적으로 변환되는지 확인")
    void reportReasons_ValueOf() {
        assertThat(ReportReason.valueOf("ABUSIVE_LANGUAGE")).isEqualTo(ReportReason.ABUSIVE_LANGUAGE);
        assertThat(ReportReason.valueOf("SPAM")).isEqualTo(ReportReason.SPAM);
        assertThat(ReportReason.valueOf("INAPPROPRIATE_CONTENT")).isEqualTo(ReportReason.INAPPROPRIATE_CONTENT);
        assertThat(ReportReason.valueOf("FALSE_INFORMATION")).isEqualTo(ReportReason.FALSE_INFORMATION);
        assertThat(ReportReason.valueOf("ADVERTISING")).isEqualTo(ReportReason.ADVERTISING);
        assertThat(ReportReason.valueOf("COPYRIGHT_VIOLATION")).isEqualTo(ReportReason.COPYRIGHT_VIOLATION);
        assertThat(ReportReason.valueOf("OTHER")).isEqualTo(ReportReason.OTHER);
    }

    @Test
    @DisplayName("존재하지 않는 값으로 valueOf 호출 시 예외 발생")
    void reportReasons_ValueOf_InvalidValue_ThrowsException() {
        assertThatThrownBy(() -> ReportReason.valueOf("INVALID_REASON"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}