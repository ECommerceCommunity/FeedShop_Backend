package com.cMall.feedShop.user.application.service;

import com.cMall.feedShop.user.application.dto.request.AddressRequest;
import com.cMall.feedShop.user.application.dto.response.AddressResponse;

import java.util.List;

public interface UserAddressService {
    List<AddressResponse> getAddresses(Long userId);
    AddressResponse addAddress(Long userId, AddressRequest requestDto);
    void updateAddress(Long userId, Long addressId, AddressRequest requestDto);
    void deleteAddress(Long userId, Long addressId);
}
