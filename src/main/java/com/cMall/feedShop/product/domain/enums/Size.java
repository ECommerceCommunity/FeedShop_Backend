package com.cMall.feedShop.product.domain.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum Size {
    SIZE_230("230"),
    SIZE_235("235"),
    SIZE_240("240"),
    SIZE_245("245"),
    SIZE_250("250"),
    SIZE_255("255"),
    SIZE_260("260"),
    SIZE_265("265"),
    SIZE_270("270"),
    SIZE_275("275"),
    SIZE_280("280"),
    SIZE_285("285"),
    SIZE_290("290"),
    SIZE_295("295"),
    SIZE_300("300");

    private final String value;

    Size(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static Size fromValue(String value) {
        for (Size size : Size.values()) {
            if (size.getValue().equals(value)) {
                return size;
            }
        }
        throw new IllegalArgumentException("Invalid size: " + value);
    }

    // JSON 에서 숫자로 표시
    @Override
    public String toString() {
        return value;
    }
}
