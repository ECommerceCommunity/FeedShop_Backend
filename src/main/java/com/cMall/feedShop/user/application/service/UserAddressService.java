package com.cMall.feedShop.user.application.service;

import com.cMall.feedShop.user.application.dto.request.AddressRequestDto;
import com.cMall.feedShop.user.application.dto.response.AddressResponseDto;

import java.util.List;

public interface UserAddressService {
    List<AddressResponseDto> getAddresses(Long userId);
    AddressResponseDto addAddress(Long userId, AddressRequestDto requestDto);
    void updateAddress(Long userId, Long addressId, AddressRequestDto requestDto);
    void deleteAddress(Long userId, Long addressId);
}
