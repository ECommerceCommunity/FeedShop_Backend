package com.cMall.feedShop.user.presentation;

import com.cMall.feedShop.common.dto.ApiResponse;
import com.cMall.feedShop.user.application.dto.request.AddressRequestDto;
import com.cMall.feedShop.user.application.dto.response.AddressResponseDto;
import com.cMall.feedShop.user.application.service.UserAddressService;
import com.cMall.feedShop.user.domain.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users/addresses")
@RequiredArgsConstructor
public class UserAddressController {

    private final UserAddressService userAddressService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<AddressResponseDto>>> getAddresses(@AuthenticationPrincipal User user) {
        System.out.println("🎯 UserAddressController.getAddresses 호출됨");
        System.out.println("👤 현재 사용자 ID: " + user.getId());
        
        List<AddressResponseDto> addresses = userAddressService.getAddresses(user.getId());
        
        System.out.println("✅ 컨트롤러에서 반환할 배송지 개수: " + addresses.size());
        System.out.println("📨 최종 응답 데이터:");
        for (AddressResponseDto dto : addresses) {
            System.out.println("  - ID: " + dto.getId() + ", isDefault: " + dto.getIsDefault());
        }
        
        ApiResponse<List<AddressResponseDto>> response = ApiResponse.success("Successfully retrieved addresses.", addresses);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<ApiResponse<AddressResponseDto>> addAddress(@AuthenticationPrincipal User user, @RequestBody AddressRequestDto requestDto) {
        AddressResponseDto address = userAddressService.addAddress(user.getId(), requestDto);
        return ResponseEntity.ok(ApiResponse.success("Successfully added address.", address));
    }

    @PutMapping("/{addressId}")
    public ResponseEntity<ApiResponse<Void>> updateAddress(@AuthenticationPrincipal User user, @PathVariable Long addressId, @RequestBody AddressRequestDto requestDto) {
        userAddressService.updateAddress(user.getId(), addressId, requestDto);
        return ResponseEntity.ok(ApiResponse.success("Successfully updated address.", null));
    }

    @DeleteMapping("/{addressId}")
    public ResponseEntity<ApiResponse<Void>> deleteAddress(@AuthenticationPrincipal User user, @PathVariable Long addressId) {
        userAddressService.deleteAddress(user.getId(), addressId);
        return ResponseEntity.ok(ApiResponse.success("Successfully deleted address.", null));
    }
}
