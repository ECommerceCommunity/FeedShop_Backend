package com.cMall.feedShop.user.application.service;

import com.cMall.feedShop.common.exception.ErrorCode;
import com.cMall.feedShop.user.application.dto.request.AddressRequestDto;
import com.cMall.feedShop.user.application.dto.response.AddressResponseDto;
import com.cMall.feedShop.user.domain.exception.UserAddressException;
import com.cMall.feedShop.user.domain.exception.UserNotFoundException;
import com.cMall.feedShop.user.domain.model.User;
import com.cMall.feedShop.user.domain.model.UserAddress;
import com.cMall.feedShop.user.domain.repository.UserAddressRepository;
import com.cMall.feedShop.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class UserAddressServiceImpl implements UserAddressService {

    private final UserRepository userRepository;
    private final UserAddressRepository userAddressRepository;

    @Override
    @Transactional(readOnly = true)
    public List<AddressResponseDto> getAddresses(Long userId) {
        List<UserAddress> addresses = userAddressRepository.findByUserId(userId);
        return addresses.stream()
                .map(AddressResponseDto::new)
                .collect(Collectors.toList());
    }

    @Override
    public AddressResponseDto addAddress(Long userId, AddressRequestDto requestDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException());

        if (requestDto.isDefault()) {
            List<UserAddress> existingDefaultAddresses = userAddressRepository.findByUserIdAndIsDefault(userId, true);
            existingDefaultAddresses.forEach(address -> {
                address.updateDefault(false);
            });
        }

        UserAddress userAddress = UserAddress.builder()
                .user(user)
                .recipientName(requestDto.getRecipientName())
                .recipientPhone(requestDto.getRecipientPhone())
                .zipCode(requestDto.getZipCode())
                .addressLine1(requestDto.getAddressLine1())
                .addressLine2(requestDto.getAddressLine2())
                .isDefault(requestDto.isDefault())
                .build();

        userAddressRepository.save(userAddress);
        return new AddressResponseDto(userAddress);
    }

    @Override
    @Transactional
    public void updateAddress(Long userId, Long addressId, AddressRequestDto requestDto) {
        UserAddress userAddress = userAddressRepository.findById(addressId)
                .orElseThrow(UserAddressException::new);

        if (!userAddress.getUser().getId().equals(userId)) {
            throw new SecurityException("You are not authorized to update this address.");
        }

        if (requestDto.isDefault()) {
            // ✅ 기존 기본 배송지를 한 번의 쿼리로 초기화
            userAddressRepository.resetDefaultAddress(userId);
        }

        userAddress.updateAddress(
                requestDto.getRecipientName(),
                requestDto.getRecipientPhone(),
                requestDto.getZipCode(),
                requestDto.getAddressLine1(),
                requestDto.getAddressLine2(),
                requestDto.isDefault()
        );

        // ✅ 현재 주소만 저장
        userAddressRepository.save(userAddress);
    }

    @Override
    public void deleteAddress(Long userId, Long addressId) {
        UserAddress userAddress = userAddressRepository.findById(addressId)
                .orElseThrow(() -> new UserAddressException());

        if (!userAddress.getUser().getId().equals(userId)) {
            throw new SecurityException("You are not authorized to delete this address.");
        }

        userAddressRepository.delete(userAddress);
    }
}
