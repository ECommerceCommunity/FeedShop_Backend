package com.cMall.feedShop.common.ai;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

// AI 응답을 담기 위한 제네릭 클래스
@Getter
@NoArgsConstructor
public class BaseAIResponse<T> {

    @JsonProperty("status")
    private String status;

    @JsonProperty("message")
    private String message;

    @JsonProperty("data")
    private T data;

    // 응답이 성공적인지 여부 확인
    public boolean isSuccess() {
        return "OK".equalsIgnoreCase(status);
    }

    // 데이터를 안전하게 반환
    public T getData() {
        return data;
    }

    // 데이터가 존재하는지 여부 확인
    public boolean hasData() {
        return data != null;
    }

}
