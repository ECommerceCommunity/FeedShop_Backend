package com.cMall.feedShop.product.domain.converter;

import com.cMall.feedShop.product.domain.enums.Size;
import jakarta.persistence.AttributeConverter;

public class SizeConverter implements AttributeConverter<Size, String> {
    @Override
    public String convertToDatabaseColumn(Size size) {
        return size != null ? size.getValue() : null;
    }

    @Override
    public Size convertToEntityAttribute(String dbData) {
        return dbData != null ? Size.fromValue(dbData) : null;
    }
}
