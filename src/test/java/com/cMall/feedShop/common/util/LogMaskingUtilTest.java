package com.cMall.feedShop.common.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("LogMaskingUtil 테스트")
class LogMaskingUtilTest {

    @Test
    @DisplayName("이메일 마스킹 - 정상적인 이메일")
    void maskEmail_NormalEmail() {
        // given
        String email = "user@example.com";

        // when
        String result = LogMaskingUtil.maskEmail(email);

        // then
        assertThat(result).isEqualTo("u**r@example.com");
    }

    @Test
    @DisplayName("이메일 마스킹 - 짧은 로컬 파트")
    void maskEmail_ShortLocalPart() {
        // given
        String email = "ab@example.com";

        // when
        String result = LogMaskingUtil.maskEmail(email);

        // then
        assertThat(result).isEqualTo("a*@example.com");
    }

    @Test
    @DisplayName("이메일 마스킹 - 매우 짧은 로컬 파트")
    void maskEmail_VeryShortLocalPart() {
        // given
        String email = "a@example.com";

        // when
        String result = LogMaskingUtil.maskEmail(email);

        // then
        assertThat(result).isEqualTo("a*@example.com");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"invalid-email", "no-at-sign"})
    @DisplayName("이메일 마스킹 - 잘못된 이메일 형식")
    void maskEmail_InvalidEmail(String email) {
        // when
        String result = LogMaskingUtil.maskEmail(email);

        // then
        assertThat(result).isEqualTo(email);
    }

    @Test
    @DisplayName("전화번호 마스킹 - 11자리 전화번호")
    void maskPhoneNumber_11Digits() {
        // given
        String phoneNumber = "010-1234-5678";

        // when
        String result = LogMaskingUtil.maskPhoneNumber(phoneNumber);

        // then
        assertThat(result).isEqualTo("***-****-5678");
    }

    @Test
    @DisplayName("전화번호 마스킹 - 10자리 전화번호")
    void maskPhoneNumber_10Digits() {
        // given
        String phoneNumber = "02-123-4567";

        // when
        String result = LogMaskingUtil.maskPhoneNumber(phoneNumber);

        // then
        assertThat(result).isEqualTo("*****4567");
    }

    @Test
    @DisplayName("전화번호 마스킹 - 하이픈 없는 전화번호")
    void maskPhoneNumber_NoHyphens() {
        // given
        String phoneNumber = "01012345678";

        // when
        String result = LogMaskingUtil.maskPhoneNumber(phoneNumber);

        // then
        assertThat(result).isEqualTo("*******5678");
    }

    @Test
    @DisplayName("전화번호 마스킹 - 짧은 전화번호")
    void maskPhoneNumber_ShortNumber() {
        // given
        String phoneNumber = "123";

        // when
        String result = LogMaskingUtil.maskPhoneNumber(phoneNumber);

        // then
        assertThat(result).isEqualTo("123");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"123", "abc"})
    @DisplayName("전화번호 마스킹 - 잘못된 전화번호")
    void maskPhoneNumber_InvalidPhoneNumber(String phoneNumber) {
        // when
        String result = LogMaskingUtil.maskPhoneNumber(phoneNumber);

        // then
        assertThat(result).isEqualTo(phoneNumber);
    }

    @Test
    @DisplayName("토큰 마스킹 - 긴 토큰")
    void maskToken_LongToken() {
        // given
        String token = "abc123def456ghi789";

        // when
        String result = LogMaskingUtil.maskToken(token);

        // then
        assertThat(result).isEqualTo("abc************789");
    }

    @Test
    @DisplayName("토큰 마스킹 - 중간 길이 토큰")
    void maskToken_MediumToken() {
        // given
        String token = "abc123def";

        // when
        String result = LogMaskingUtil.maskToken(token);

        // then
        assertThat(result).isEqualTo("abc***def");
    }

    @Test
    @DisplayName("토큰 마스킹 - 짧은 토큰")
    void maskToken_ShortToken() {
        // given
        String token = "abc123";

        // when
        String result = LogMaskingUtil.maskToken(token);

        // then
        assertThat(result).isEqualTo("ab**23");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"123", "ab", "a"})
    @DisplayName("토큰 마스킹 - 잘못된 토큰")
    void maskToken_InvalidToken(String token) {
        // when
        String result = LogMaskingUtil.maskToken(token);

        // then
        assertThat(result).isEqualTo(token);
    }

    @Test
    @DisplayName("MFA 토큰 마스킹 - 정상적인 6자리 토큰")
    void maskMfaToken_ValidToken() {
        // given
        String token = "123456";

        // when
        String result = LogMaskingUtil.maskMfaToken(token);

        // then
        assertThat(result).isEqualTo("12****");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"12345", "1234567", "12", "123"})
    @DisplayName("MFA 토큰 마스킹 - 잘못된 토큰")
    void maskMfaToken_InvalidToken(String token) {
        // when
        String result = LogMaskingUtil.maskMfaToken(token);

        // then
        assertThat(result).isEqualTo(token);
    }

    @Test
    @DisplayName("백업 코드 마스킹 - 정상적인 8자리 코드")
    void maskBackupCode_ValidCode() {
        // given
        String code = "12345678";

        // when
        String result = LogMaskingUtil.maskBackupCode(code);

        // then
        assertThat(result).isEqualTo("12****78");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"1234567", "123456789", "12", "123"})
    @DisplayName("백업 코드 마스킹 - 잘못된 코드")
    void maskBackupCode_InvalidCode(String code) {
        // when
        String result = LogMaskingUtil.maskBackupCode(code);

        // then
        assertThat(result).isEqualTo(code);
    }

    @Test
    @DisplayName("사용자 ID 마스킹 - 긴 ID")
    void maskUserId_LongId() {
        // given
        Long userId = 12345L;

        // when
        String result = LogMaskingUtil.maskUserId(userId);

        // then
        assertThat(result).isEqualTo("1****");
    }

    @Test
    @DisplayName("사용자 ID 마스킹 - 짧은 ID")
    void maskUserId_ShortId() {
        // given
        Long userId = 123L;

        // when
        String result = LogMaskingUtil.maskUserId(userId);

        // then
        assertThat(result).isEqualTo("1**");
    }

    @Test
    @DisplayName("사용자 ID 마스킹 - 한 자리 ID")
    void maskUserId_SingleDigitId() {
        // given
        Long userId = 5L;

        // when
        String result = LogMaskingUtil.maskUserId(userId);

        // then
        assertThat(result).isEqualTo("5");
    }

    @Test
    @DisplayName("사용자 ID 마스킹 - null ID")
    void maskUserId_NullId() {
        // when
        String result = LogMaskingUtil.maskUserId(null);

        // then
        assertThat(result).isNull();
    }

    @ParameterizedTest
    @CsvSource({
        "user@example.com, email, u**r@example.com",
        "010-1234-5678, phone, ***-****-5678",
        "abc123def456, token, abc******456",
        "123456, mfa, 12****",
        "12345678, backup, 12****78",
        "unknown, unknown, unknown"
    })
    @DisplayName("민감 정보 마스킹 - 타입별 마스킹")
    void maskSensitiveInfo_ByType(String input, String type, String expected) {
        // when
        String result = LogMaskingUtil.maskSensitiveInfo(input, type);

        // then
        assertThat(result).isEqualTo(expected);
    }

    @Test
    @DisplayName("민감 정보 마스킹 - null 입력")
    void maskSensitiveInfo_NullInput() {
        // when
        String result = LogMaskingUtil.maskSensitiveInfo(null, "email");

        // then
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("민감 정보 마스킹 - 대소문자 구분 없는 타입")
    void maskSensitiveInfo_CaseInsensitiveType() {
        // given
        String email = "user@example.com";

        // when
        String result1 = LogMaskingUtil.maskSensitiveInfo(email, "EMAIL");
        String result2 = LogMaskingUtil.maskSensitiveInfo(email, "Email");

        // then
        assertThat(result1).isEqualTo("u**r@example.com");
        assertThat(result2).isEqualTo("u**r@example.com");
    }

    @Test
    @DisplayName("이메일 마스킹 - 다양한 이메일 형식")
    void maskEmail_VariousFormats() {
        // given & when & then
        assertThat(LogMaskingUtil.maskEmail("test@example.com")).isEqualTo("t**t@example.com");
        assertThat(LogMaskingUtil.maskEmail("admin@company.co.kr")).isEqualTo("a***n@company.co.kr");
        assertThat(LogMaskingUtil.maskEmail("user123@domain.org")).isEqualTo("u*****3@domain.org");
        assertThat(LogMaskingUtil.maskEmail("a@b.c")).isEqualTo("a*@b.c");
        assertThat(LogMaskingUtil.maskEmail("ab@c.d")).isEqualTo("a*@c.d");
    }

    @Test
    @DisplayName("전화번호 마스킹 - 다양한 전화번호 형식")
    void maskPhoneNumber_VariousFormats() {
        // given & when & then
        assertThat(LogMaskingUtil.maskPhoneNumber("010-1234-5678")).isEqualTo("***-****-5678");
        assertThat(LogMaskingUtil.maskPhoneNumber("02-123-4567")).isEqualTo("*****4567");
        assertThat(LogMaskingUtil.maskPhoneNumber("031-123-4567")).isEqualTo("***-***-4567");
        assertThat(LogMaskingUtil.maskPhoneNumber("01012345678")).isEqualTo("*******5678");
        assertThat(LogMaskingUtil.maskPhoneNumber("0212345678")).isEqualTo("******5678");
    }

    @Test
    @DisplayName("토큰 마스킹 - 다양한 토큰 길이")
    void maskToken_VariousLengths() {
        // given & when & then
        assertThat(LogMaskingUtil.maskToken("abc123")).isEqualTo("ab**23"); // 6자리
        assertThat(LogMaskingUtil.maskToken("abc123def")).isEqualTo("abc***def"); // 9자리
        assertThat(LogMaskingUtil.maskToken("abc123def456")).isEqualTo("abc******456"); // 12자리
        assertThat(LogMaskingUtil.maskToken("abc123def456ghi789")).isEqualTo("abc************789"); // 18자리
    }

    @Test
    @DisplayName("사용자 ID 마스킹 - 다양한 ID 길이")
    void maskUserId_VariousLengths() {
        // given & when & then
        assertThat(LogMaskingUtil.maskUserId(1L)).isEqualTo("1");
        assertThat(LogMaskingUtil.maskUserId(12L)).isEqualTo("1*");
        assertThat(LogMaskingUtil.maskUserId(123L)).isEqualTo("1**");
        assertThat(LogMaskingUtil.maskUserId(1234L)).isEqualTo("1***");
        assertThat(LogMaskingUtil.maskUserId(12345L)).isEqualTo("1****");
        assertThat(LogMaskingUtil.maskUserId(123456L)).isEqualTo("1*****");
    }

    @Test
    @DisplayName("엣지 케이스 - 빈 문자열과 공백")
    void edgeCases_EmptyAndWhitespace() {
        // given & when & then
        assertThat(LogMaskingUtil.maskEmail("")).isEqualTo("");
        assertThat(LogMaskingUtil.maskEmail("   ")).isEqualTo("   ");
        assertThat(LogMaskingUtil.maskPhoneNumber("")).isEqualTo("");
        assertThat(LogMaskingUtil.maskPhoneNumber("   ")).isEqualTo("   ");
        assertThat(LogMaskingUtil.maskToken("")).isEqualTo("");
        assertThat(LogMaskingUtil.maskToken("   ")).isEqualTo("   ");
    }

    @Test
    @DisplayName("엣지 케이스 - 특수 문자 포함")
    void edgeCases_SpecialCharacters() {
        // given & when & then
        assertThat(LogMaskingUtil.maskEmail("user+tag@example.com")).isEqualTo("u******g@example.com");
        assertThat(LogMaskingUtil.maskPhoneNumber("010-1234-5678 (mobile)")).isEqualTo("***-****-5678");
        assertThat(LogMaskingUtil.maskToken("abc-123_def.456")).isEqualTo("abc*********456");
    }
}
