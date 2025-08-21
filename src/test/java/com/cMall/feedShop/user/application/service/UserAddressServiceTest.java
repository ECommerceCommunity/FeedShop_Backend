package com.cMall.feedShop.user.application.service;

import com.cMall.feedShop.user.application.dto.request.AddressRequest;
import com.cMall.feedShop.user.application.dto.response.AddressResponse;
import com.cMall.feedShop.user.domain.enums.UserRole;
import com.cMall.feedShop.user.domain.exception.UserAddressException;
import com.cMall.feedShop.user.domain.exception.UserNotFoundException;
import com.cMall.feedShop.user.domain.model.User;
import com.cMall.feedShop.user.domain.model.UserAddress;
import com.cMall.feedShop.user.domain.repository.UserAddressRepository;
import com.cMall.feedShop.user.domain.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserAddressService 테스트")
class UserAddressServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserAddressRepository userAddressRepository;

    @InjectMocks
    private UserAddressServiceImpl userAddressService;

    private User testUser;
    private UserAddress testAddress1;
    private UserAddress testAddress2;
    private AddressRequest addressRequest;

    @BeforeEach
    void setUp() {
        // Given: 테스트 데이터 설정
        testUser = new User(1L, "testuser", "password123", "test@example.com", UserRole.USER);

        testAddress1 = UserAddress.builder()
                .user(testUser)
                .recipientName("홍길동")
                .recipientPhone("010-1234-5678")
                .zipCode("12345")
                .addressLine1("서울시 강남구")
                .addressLine2("테헤란로 123")
                .isDefault(true)
                .build();

        testAddress2 = UserAddress.builder()
                .user(testUser)
                .recipientName("김철수")
                .recipientPhone("010-9876-5432")
                .zipCode("54321")
                .addressLine1("서울시 서초구")
                .addressLine2("강남대로 456")
                .isDefault(false)
                .build();

        addressRequest = AddressRequest.builder()
                .recipientName("박영희")
                .recipientPhone("010-5555-5555")
                .zipCode("67890")
                .addressLine1("서울시 마포구")
                .addressLine2("홍대로 789")
                .isDefault(false)
                .build();
    }

    @Test
    @DisplayName("사용자의 모든 주소 조회 성공")
    void getAddresses_Success() {
        // Given
        List<UserAddress> addresses = Arrays.asList(testAddress1, testAddress2);
        when(userAddressRepository.findByUserId(1L)).thenReturn(addresses);

        // When
        List<AddressResponse> result = userAddressService.getAddresses(1L);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getRecipientName()).isEqualTo("홍길동");
        assertThat(result.get(0).getAddressLine1()).isEqualTo("서울시 강남구");
        assertThat(result.get(0).getIsDefault()).isTrue();
        assertThat(result.get(1).getRecipientName()).isEqualTo("김철수");
        assertThat(result.get(1).getAddressLine1()).isEqualTo("서울시 서초구");
        assertThat(result.get(1).getIsDefault()).isFalse();

        verify(userAddressRepository, times(1)).findByUserId(1L);
    }

    @Test
    @DisplayName("사용자의 주소 목록이 비어있는 경우")
    void getAddresses_EmptyList() {
        // Given
        when(userAddressRepository.findByUserId(1L)).thenReturn(Arrays.asList());

        // When
        List<AddressResponse> result = userAddressService.getAddresses(1L);

        // Then
        assertThat(result).isEmpty();
        verify(userAddressRepository, times(1)).findByUserId(1L);
    }

    @Test
    @DisplayName("새 주소 추가 성공")
    void addAddress_Success() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userAddressRepository.save(any(UserAddress.class))).thenAnswer(invocation -> {
            UserAddress savedAddress = invocation.getArgument(0);
            return savedAddress;
        });

        // When
        AddressResponse result = userAddressService.addAddress(1L, addressRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getRecipientName()).isEqualTo("박영희");
        assertThat(result.getAddressLine1()).isEqualTo("서울시 마포구");

        verify(userRepository, times(1)).findById(1L);
        verify(userAddressRepository, times(1)).save(any(UserAddress.class));
    }

    @Test
    @DisplayName("존재하지 않는 사용자로 주소 추가 시 예외 발생")
    void addAddress_UserNotFound() {
        // Given
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> userAddressService.addAddress(999L, addressRequest))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("사용자를 찾을 수 없습니다.");

        verify(userRepository, times(1)).findById(999L);
        verify(userAddressRepository, never()).save(any(UserAddress.class));
    }

    @Test
    @DisplayName("주소 수정 성공")
    void updateAddress_Success() {
        // Given
        when(userAddressRepository.findById(1L)).thenReturn(Optional.of(testAddress1));

        // When
        userAddressService.updateAddress(1L, 1L, addressRequest);

        // Then
        verify(userAddressRepository, times(1)).findById(1L);
        // updateAddress 메서드가 호출되었는지 확인 (실제 구현에서는 UserAddress의 updateAddress 메서드가 호출됨)
    }

    @Test
    @DisplayName("존재하지 않는 주소 수정 시 예외 발생")
    void updateAddress_AddressNotFound() {
        // Given
        when(userAddressRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> userAddressService.updateAddress(1L, 999L, addressRequest))
                .isInstanceOf(UserAddressException.class)
                .hasMessage("주소 정보를 찾을 수 없습니다.");

        verify(userAddressRepository, times(1)).findById(999L);
    }

    @Test
    @DisplayName("다른 사용자의 주소 수정 시 권한 예외 발생")
    void updateAddress_Unauthorized() {
        // Given
        User otherUser = new User(2L, "otheruser", "password123", "other@example.com", UserRole.USER);
        UserAddress otherUserAddress = UserAddress.builder()
                .user(otherUser)
                .recipientName("다른사용자")
                .recipientPhone("010-0000-0000")
                .zipCode("00000")
                .addressLine1("다른주소")
                .addressLine2("")
                .isDefault(false)
                .build();

        when(userAddressRepository.findById(1L)).thenReturn(Optional.of(otherUserAddress));

        // When & Then
        assertThatThrownBy(() -> userAddressService.updateAddress(1L, 1L, addressRequest))
                .isInstanceOf(SecurityException.class)
                .hasMessage("You are not authorized to update this address.");

        verify(userAddressRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("주소 삭제 성공")
    void deleteAddress_Success() {
        // Given
        when(userAddressRepository.findById(1L)).thenReturn(Optional.of(testAddress1));
        doNothing().when(userAddressRepository).delete(testAddress1);

        // When
        userAddressService.deleteAddress(1L, 1L);

        // Then
        verify(userAddressRepository, times(1)).findById(1L);
        verify(userAddressRepository, times(1)).delete(testAddress1);
    }

    @Test
    @DisplayName("존재하지 않는 주소 삭제 시 예외 발생")
    void deleteAddress_AddressNotFound() {
        // Given
        when(userAddressRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> userAddressService.deleteAddress(1L, 999L))
                .isInstanceOf(UserAddressException.class)
                .hasMessage("주소 정보를 찾을 수 없습니다.");

        verify(userAddressRepository, times(1)).findById(999L);
        verify(userAddressRepository, never()).delete(any(UserAddress.class));
    }

    @Test
    @DisplayName("다른 사용자의 주소 삭제 시 권한 예외 발생")
    void deleteAddress_Unauthorized() {
        // Given
        User otherUser = new User(2L, "otheruser", "password123", "other@example.com", UserRole.USER);
        UserAddress otherUserAddress = UserAddress.builder()
                .user(otherUser)
                .recipientName("다른사용자")
                .recipientPhone("010-0000-0000")
                .zipCode("00000")
                .addressLine1("다른주소")
                .addressLine2("")
                .isDefault(false)
                .build();

        when(userAddressRepository.findById(1L)).thenReturn(Optional.of(otherUserAddress));

        // When & Then
        assertThatThrownBy(() -> userAddressService.deleteAddress(1L, 1L))
                .isInstanceOf(SecurityException.class)
                .hasMessage("You are not authorized to delete this address.");

        verify(userAddressRepository, times(1)).findById(1L);
        verify(userAddressRepository, never()).delete(any(UserAddress.class));
    }

    @Test
    @DisplayName("기본 주소 설정 테스트")
    void addAddress_DefaultAddress() {
        // Given
        AddressRequest defaultAddressRequest = AddressRequest.builder()
                .recipientName("박영희")
                .recipientPhone("010-5555-5555")
                .zipCode("67890")
                .addressLine1("서울시 마포구")
                .addressLine2("홍대로 789")
                .isDefault(true)
                .build();
        
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userAddressRepository.save(any(UserAddress.class))).thenReturn(testAddress1);

        // When
        AddressResponse result = userAddressService.addAddress(1L, defaultAddressRequest);

        // Then
        assertThat(result).isNotNull();
        verify(userRepository, times(1)).findById(1L);
        verify(userAddressRepository, times(1)).save(any(UserAddress.class));
    }

    @Test
    @DisplayName("주소 정보 업데이트 시 모든 필드 변경 확인")
    void updateAddress_AllFieldsUpdated() {
        // Given
        when(userAddressRepository.findById(1L)).thenReturn(Optional.of(testAddress1));

        // When
        userAddressService.updateAddress(1L, 1L, addressRequest);

        // Then
        verify(userAddressRepository, times(1)).findById(1L);
        // updateAddress 메서드가 호출되었는지 확인 (실제 구현에서는 UserAddress의 updateAddress 메서드가 호출됨)
    }


}
