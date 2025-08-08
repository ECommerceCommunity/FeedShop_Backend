package com.cMall.feedShop.product.application.dto.response.info;

import com.cMall.feedShop.product.domain.enums.Color;
import com.cMall.feedShop.product.domain.enums.Gender;
import com.cMall.feedShop.product.domain.enums.Size;
import com.cMall.feedShop.product.domain.model.ProductOption;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ProductOptionInfo {
    private Long optionId;
    private Gender gender;
    private Size size;
    private Color color;
    private Integer stock;

    public static ProductOptionInfo from(ProductOption option) {
        return ProductOptionInfo.builder()
                .optionId(option.getOptionId())
                .gender(option.getGender())
                .size(option.getSize())
                .color(option.getColor())
                .stock(option.getStock())
                .build();
    }

    public static List<ProductOptionInfo> fromList(List<ProductOption> options)
    {
        return options.stream()
                .map(ProductOptionInfo::from)
                .toList();
    }
}
