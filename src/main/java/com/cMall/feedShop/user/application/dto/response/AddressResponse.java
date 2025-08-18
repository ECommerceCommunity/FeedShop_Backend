package com.cMall.feedShop.user.application.dto.response;

import com.cMall.feedShop.user.domain.model.UserAddress;
import com.fasterxml.jackson.annotation.JsonGetter;
import lombok.Getter;

@Getter
public class AddressResponse {

    private Long id;
    private String recipientName;
    private String recipientPhone;
    private String zipCode;
    private String addressLine1;
    private String addressLine2;
    private boolean isDefault;

    public AddressResponse(UserAddress userAddress) {
        this.id = userAddress.getId();
        this.recipientName = userAddress.getRecipientName();
        this.recipientPhone = userAddress.getRecipientPhone();
        this.zipCode = userAddress.getZipCode();
        this.addressLine1 = userAddress.getAddressLine1();
        this.addressLine2 = userAddress.getAddressLine2();
        this.isDefault = userAddress.isDefault();
    }

    @JsonGetter("isDefault")
    public boolean getIsDefault() {
        return this.isDefault;
    }
}
