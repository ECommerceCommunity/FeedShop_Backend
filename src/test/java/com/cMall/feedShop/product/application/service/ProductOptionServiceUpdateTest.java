package com.cMall.feedShop.product.application.service;

import com.cMall.feedShop.common.exception.ErrorCode;
import com.cMall.feedShop.product.application.dto.request.ProductOptionUpdateRequest;
import com.cMall.feedShop.product.domain.enums.Color;
import com.cMall.feedShop.product.domain.enums.Gender;
import com.cMall.feedShop.product.domain.enums.Size;
import com.cMall.feedShop.product.domain.exception.ProductException;
import com.cMall.feedShop.product.domain.model.Product;
import com.cMall.feedShop.product.domain.model.ProductOption;
import com.cMall.feedShop.product.domain.repository.ProductOptionRepository;
import com.cMall.feedShop.store.domain.model.Store;
import com.cMall.feedShop.user.domain.enums.UserRole;
import com.cMall.feedShop.user.domain.model.User;
import com.cMall.feedShop.user.domain.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

/**
 * ProductOptionService 테스트 클래스
 * - 상품 옵션 수정 기능을 테스트합니다
 * - 초등학생도 이해할 수 있도록 자세한 주석을 작성했습니다
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)  // UnnecessaryStubbingException 방지
@DisplayName("상품 옵션 서비스 테스트")
class ProductOptionServiceUpdateTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProductOptionRepository productOptionRepository;


    @InjectMocks
    private ProductOptionService productOptionService;

    private User seller;
    private User buyer;
    private Store store;
    private Product product;
    private ProductOption productOption;
    private ProductOptionUpdateRequest request;

    @BeforeEach
    void setUp() {
        // 1. 판매자 계정 생성 - 실제 객체로 생성
        seller = new User("seller123", "password123", "seller@test.com", UserRole.SELLER);
        ReflectionTestUtils.setField(seller, "id", 1L);

        // 2. 일반 구매자 계정 생성
        buyer = new User("buyer123", "password123", "buyer@test.com", UserRole.USER);
        ReflectionTestUtils.setField(buyer, "id", 2L);

        // 3. 가게 생성 - 실제 객체로 생성
        store = Store.builder()
                .storeName("테스트 가게")
                .sellerId(1L)
                .description("테스트용 가게입니다")
                .build();
        ReflectionTestUtils.setField(store, "storeId", 1L);

        // 4. 상품 생성 - mock으로 생성
        product = mock(Product.class);
        given(product.getStore()).willReturn(store);

        // 5. 상품 옵션 생성 - mock으로 생성
        productOption = mock(ProductOption.class);
        given(productOption.getProduct()).willReturn(product);

        // 6. 수정 요청 데이터 생성
        request = createUpdateRequest(50, "BLACK", "245", "UNISEX");
    }

    private ProductOptionUpdateRequest createUpdateRequest(Integer stock, String color, String size, String gender) {
        ProductOptionUpdateRequest request = new ProductOptionUpdateRequest();
        if (stock != null) {
            ReflectionTestUtils.setField(request, "stock", stock);
        }
        if (color != null) {
            ReflectionTestUtils.setField(request, "color", color);
        }
        if (size != null) {
            ReflectionTestUtils.setField(request, "size", size);
        }
        if (gender != null) {
            ReflectionTestUtils.setField(request, "gender", gender);
        }
        return request;
    }

    @Test
    @DisplayName("정상적인 상품 옵션 수정 성공")
    void updateProductOption_Success() {
        // given
        Long optionId = 1L;

        given(userRepository.findByLoginId("seller123")).willReturn(Optional.of(seller));
        given(userRepository.findByLoginId("seller123")).willReturn(Optional.of(seller));
        given(productOptionRepository.findByOptionId(optionId)).willReturn(Optional.of(productOption));

        // when
        productOptionService.updateProductOption(optionId, request, "seller123");

        // then
        verify(productOption, times(1)).updateStock(50);
        verify(productOption, times(1)).updateColor(Color.BLACK);
        verify(productOption, times(1)).updateSize(Size.SIZE_245);
        verify(productOption, times(1)).updateGender(Gender.UNISEX);
        verify(productOptionRepository, times(1)).save(productOption);
    }

    @Test
    @DisplayName("일반 사용자의 상품 옵션 수정 시도 시 권한 에러")
    void updateProductOption_Forbidden_NotSeller() {
        // given
        Long optionId = 1L;

        given(userRepository.findByLoginId("seller123")).willReturn(Optional.of(buyer));

        // when & then
        ProductException exception = assertThrows(ProductException.class, () -> {
            productOptionService.updateProductOption(optionId, request, "seller123");
        });

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.FORBIDDEN);
        verify(productOptionRepository, never()).save(any());
    }

    @Test
    @DisplayName("다른 판매자 상품 옵션 수정 시도 시 권한 에러")
    void updateProductOption_Forbidden_NotOwner() {
        // given
        Long optionId = 1L;

        // 다른 판매자 생성
        User otherSeller = new User("other@test.com", "password123", "other@test.com", UserRole.SELLER);
        ReflectionTestUtils.setField(otherSeller, "id", 99L);

        given(userRepository.findByLoginId("seller123")).willReturn(Optional.of(otherSeller));
        given(productOptionRepository.findByOptionId(optionId)).willReturn(Optional.of(productOption));

        // when & then
        ProductException exception = assertThrows(ProductException.class, () -> {
            productOptionService.updateProductOption(optionId, request, "seller123");
        });

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.FORBIDDEN);
    }

    @Test
    @DisplayName("존재하지 않는 상품 옵션 수정 시도 시 에러")
    void updateProductOption_OptionNotFound() {
        // given
        Long notExistOptionId = 999L;

        given(userRepository.findByLoginId("seller123")).willReturn(Optional.of(seller));
        given(userRepository.findByLoginId("seller123")).willReturn(Optional.of(seller));
        given(productOptionRepository.findByOptionId(notExistOptionId)).willReturn(Optional.empty());

        // when & then
        ProductException exception = assertThrows(ProductException.class, () -> {
            productOptionService.updateProductOption(notExistOptionId, request, "seller123");
        });

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.PRODUCT_OPTION_NOT_FOUND);
    }

    @Test
    @DisplayName("존재하지 않는 사용자로 상품 옵션 수정 시도 시 에러")
    void updateProductOption_UserNotFound() {
        // given
        Long optionId = 1L;

        given(userRepository.findByLoginId("seller123")).willReturn(Optional.empty());

        // when & then
        ProductException exception = assertThrows(ProductException.class, () -> {
            productOptionService.updateProductOption(optionId, request, "seller123");
        });

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.USER_NOT_FOUND);
    }

    @Test
    @DisplayName("재고만 수정하는 경우 성공")
    void updateProductOption_OnlyStock() {
        // given
        Long optionId = 1L;

        ProductOptionUpdateRequest stockOnlyRequest = createUpdateRequest(100, null, null, null);

        given(userRepository.findByLoginId("seller123")).willReturn(Optional.of(seller));
        given(userRepository.findByLoginId("seller123")).willReturn(Optional.of(seller));
        given(productOptionRepository.findByOptionId(optionId)).willReturn(Optional.of(productOption));

        // when
        productOptionService.updateProductOption(optionId, stockOnlyRequest, "seller123");

        // then
        verify(productOption, times(1)).updateStock(100);
        verify(productOption, never()).updateColor(any());
        verify(productOption, never()).updateSize(any());
        verify(productOption, never()).updateGender(any());
        verify(productOptionRepository, times(1)).save(productOption);
    }

    @Test
    @DisplayName("잘못된 색상 값으로 수정 시도 시 에러")
    void updateProductOption_InvalidColor() {
        // given
        Long optionId = 1L;

        ProductOptionUpdateRequest invalidRequest = createUpdateRequest(50, "RAINBOW", null, null);

        given(userRepository.findByLoginId("seller123")).willReturn(Optional.of(seller));
        given(userRepository.findByLoginId("seller123")).willReturn(Optional.of(seller));
        given(productOptionRepository.findByOptionId(optionId)).willReturn(Optional.of(productOption));

        // when & then
        ProductException exception = assertThrows(ProductException.class, () -> {
            productOptionService.updateProductOption(optionId, invalidRequest, "seller123");
        });

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INVALID_INPUT_VALUE);
    }

    @Test
    @DisplayName("빈 문자열 필드는 업데이트하지 않음")
    void updateProductOption_EmptyStringFieldsIgnored() {
        // given
        Long optionId = 1L;

        ProductOptionUpdateRequest emptyStringRequest = createUpdateRequest(50, "", "   ", null);

        given(userRepository.findByLoginId("seller123")).willReturn(Optional.of(seller));
        given(userRepository.findByLoginId("seller123")).willReturn(Optional.of(seller));
        given(productOptionRepository.findByOptionId(optionId)).willReturn(Optional.of(productOption));

        // when
        productOptionService.updateProductOption(optionId, emptyStringRequest, "seller123");

        // then
        verify(productOption, times(1)).updateStock(50);
        verify(productOption, never()).updateColor(any());
        verify(productOption, never()).updateSize(any());
        verify(productOptionRepository, times(1)).save(productOption);
    }
}