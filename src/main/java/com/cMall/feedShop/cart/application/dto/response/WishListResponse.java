
package com.cMall.feedShop.cart.application.dto.response;

import com.cMall.feedShop.cart.application.dto.response.info.WishlistInfo;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.data.domain.Page;

import java.util.List;

@Getter
@AllArgsConstructor
public class WishListResponse {
    private List<WishlistInfo> wishlists;
    private int totalPages;
    private long totalElements;
    private boolean hasNext;

    public static WishListResponse of(Page<WishlistInfo> page) {
        return new WishListResponse(
                page.getContent(),
                page.getTotalPages(),
                page.getTotalElements(),
                page.hasNext()
        );
    }
}