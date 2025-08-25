package com.cMall.feedShop.user.presentation;

import com.cMall.feedShop.common.dto.ApiResponse;
import com.cMall.feedShop.user.application.dto.request.AddressRequest;
import com.cMall.feedShop.user.application.dto.response.AddressResponse;
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
    public ResponseEntity<ApiResponse<List<AddressResponse>>> getAddresses(@AuthenticationPrincipal User user) {
        List<AddressResponse> addresses = userAddressService.getAddresses(user.getId());
        return ResponseEntity.ok(ApiResponse.success("Successfully retrieved addresses.", addresses));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<AddressResponse>> addAddress(@AuthenticationPrincipal User user, @RequestBody AddressRequest requestDto) {
        AddressResponse address = userAddressService.addAddress(user.getId(), requestDto);
        return ResponseEntity.ok(ApiResponse.success("Successfully added address.", address));
    }

    @PutMapping("/{addressId}")
    public ResponseEntity<ApiResponse<Void>> updateAddress(@AuthenticationPrincipal User user, @PathVariable Long addressId, @RequestBody AddressRequest requestDto) {
        userAddressService.updateAddress(user.getId(), addressId, requestDto);
        return ResponseEntity.ok(ApiResponse.success("Successfully updated address.", null));
    }

    @DeleteMapping("/{addressId}")
    public ResponseEntity<ApiResponse<Void>> deleteAddress(@AuthenticationPrincipal User user, @PathVariable Long addressId) {
        userAddressService.deleteAddress(user.getId(), addressId);
        return ResponseEntity.ok(ApiResponse.success("Successfully deleted address.", null));
    }
}
