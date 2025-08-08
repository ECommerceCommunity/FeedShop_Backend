package com.cMall.feedShop.product.application.service;

import com.cMall.feedShop.common.exception.ErrorCode;
import com.cMall.feedShop.product.application.dto.response.info.ProductOptionInfo;
import com.cMall.feedShop.product.domain.enums.Color;
import com.cMall.feedShop.product.domain.enums.Gender;
import com.cMall.feedShop.product.domain.enums.Size;
import com.cMall.feedShop.product.domain.exception.ProductException;
import com.cMall.feedShop.product.domain.model.Product;
import com.cMall.feedShop.product.domain.model.ProductOption;
import com.cMall.feedShop.product.domain.repository.ProductRepository;
import com.cMall.feedShop.store.domain.model.Store;
import com.cMall.feedShop.store.domain.repository.StoreRepository;
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
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductOptionService 테스트")
class ProductOptionServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private StoreRepository storeRepository;

    @Mock
    private UserDetails userDetails;

    @InjectMocks
    private ProductOptionService productOptionService;

    private User seller;
    private User normalUser;
    private Store store;
    private Store otherStore;
    private Product product;
    private ProductOption option1;
    private ProductOption option2;

    @BeforeEach
    void setUp() {
        // 판매자 사용자 생성 - 생성자 사용
        seller = new User("seller@test.com", "password", "seller@test.com", UserRole.SELLER);
        ReflectionTestUtils.setField(seller, "id", 1L);

        // 일반 사용자 생성 - 생성자 사용
        normalUser = new User("user@test.com", "password", "user@test.com", UserRole.USER);
        ReflectionTestUtils.setField(normalUser, "id", 2L);

        // 판매자의 가게 생성
        store = Store.builder()
                .storeName("테스트 스토어")
                .sellerId(1L)
                .build();
        ReflectionTestUtils.setField(store, "storeId", 1L);

        // 다른 판매자의 가게 생성
        otherStore = Store.builder()
                .storeName("다른 스토어")
                .sellerId(3L)
                .build();
        ReflectionTestUtils.setField(otherStore, "storeId", 2L);

        // 상품 생성
        product = Product.builder()
                .name("테스트 상품")
                .price(new BigDecimal("50000"))
                .store(store)
                .build();
        ReflectionTestUtils.setField(product, "productId", 1L);

        // 상품 옵션 생성
        option1 = new ProductOption(Gender.UNISEX, Size.SIZE_250, Color.BLACK, 50, product);
        ReflectionTestUtils.setField(option1, "optionId", 1L);

        option2 = new ProductOption(Gender.WOMEN, Size.SIZE_240, Color.WHITE, 30, product);
        ReflectionTestUtils.setField(option2, "optionId", 2L);

        // 상품에 옵션 추가
        product.getProductOptions().addAll(Arrays.asList(option1, option2));
    }

    @Test
    @DisplayName("판매자가 자신의 상품 옵션 조회 성공")
    void getProductOptions_Success() {
        // given
        given(userDetails.getUsername()).willReturn("seller@test.com");
        given(userRepository.findByLoginId("seller@test.com")).willReturn(Optional.of(seller));
        given(productRepository.findByProductId(1L)).willReturn(Optional.of(product));
        given(storeRepository.findBySellerId(1L)).willReturn(Optional.of(store));

        // when
        List<ProductOptionInfo> result = productOptionService.getProductOptions(1L, userDetails);

        // then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getOptionId()).isEqualTo(1L);
        assertThat(result.get(0).getGender()).isEqualTo(Gender.UNISEX);
        assertThat(result.get(0).getSize()).isEqualTo(Size.SIZE_250);
        assertThat(result.get(0).getColor()).isEqualTo(Color.BLACK);
        assertThat(result.get(0).getStock()).isEqualTo(50);

        assertThat(result.get(1).getOptionId()).isEqualTo(2L);
        assertThat(result.get(1).getGender()).isEqualTo(Gender.WOMEN);
        assertThat(result.get(1).getSize()).isEqualTo(Size.SIZE_240);
        assertThat(result.get(1).getColor()).isEqualTo(Color.WHITE);
        assertThat(result.get(1).getStock()).isEqualTo(30);
    }

    @Test
    @DisplayName("비로그인 사용자 접근 시 예외 발생")
    void getProductOptions_Fail_UserNotFound() {
        // given
        given(userDetails.getUsername()).willReturn("nonexistent@test.com");
        given(userRepository.findByLoginId("nonexistent@test.com")).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> productOptionService.getProductOptions(1L, userDetails))
                .isInstanceOf(ProductException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_NOT_FOUND);

        // 검증
        verify(productRepository, never()).findByProductId(1L);
        verify(storeRepository, never()).findBySellerId(1L);
    }

    @Test
    @DisplayName("일반 사용자(USER 권한) 접근 시 예외 발생")
    void getProductOptions_Fail_NotSeller() {
        // given
        given(userDetails.getUsername()).willReturn("user@test.com");
        given(userRepository.findByLoginId("user@test.com")).willReturn(Optional.of(normalUser));

        // when & then
        assertThatThrownBy(() -> productOptionService.getProductOptions(1L, userDetails))
                .isInstanceOf(ProductException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.FORBIDDEN);

        // 검증
        verify(productRepository, never()).findByProductId(1L);
        verify(storeRepository, never()).findBySellerId(2L);
    }

    @Test
    @DisplayName("존재하지 않는 상품 조회 시 예외 발생")
    void getProductOptions_Fail_ProductNotFound() {
        // given
        given(userDetails.getUsername()).willReturn("seller@test.com");
        given(userRepository.findByLoginId("seller@test.com")).willReturn(Optional.of(seller));
        given(productRepository.findByProductId(999L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> productOptionService.getProductOptions(999L, userDetails))
                .isInstanceOf(ProductException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PRODUCT_NOT_FOUND);

        // 검증
        verify(storeRepository, never()).findBySellerId(1L);
    }

    @Test
    @DisplayName("판매자 가게 없음 시 예외 발생")
    void getProductOptions_Fail_StoreNotFound() {
        // given
        given(userDetails.getUsername()).willReturn("seller@test.com");
        given(userRepository.findByLoginId("seller@test.com")).willReturn(Optional.of(seller));
        given(productRepository.findByProductId(1L)).willReturn(Optional.of(product));
        given(storeRepository.findBySellerId(1L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> productOptionService.getProductOptions(1L, userDetails))
                .isInstanceOf(ProductException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.STORE_NOT_FOUND);
    }

    @Test
    @DisplayName("다른 판매자의 상품 접근 시 예외 발생")
    void getProductOptions_Fail_NotProductOwner() {
        // given
        // 다른 판매자의 상품 설정
        Product otherProduct = Product.builder()
                .name("다른 상품")
                .price(new BigDecimal("30000"))
                .store(otherStore)  // 다른 가게의 상품
                .build();
        ReflectionTestUtils.setField(otherProduct, "productId", 2L);

        given(userDetails.getUsername()).willReturn("seller@test.com");
        given(userRepository.findByLoginId("seller@test.com")).willReturn(Optional.of(seller));
        given(productRepository.findByProductId(2L)).willReturn(Optional.of(otherProduct));
        given(storeRepository.findBySellerId(1L)).willReturn(Optional.of(store));

        // when & then
        assertThatThrownBy(() -> productOptionService.getProductOptions(2L, userDetails))
                .isInstanceOf(ProductException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PRODUCT_NOT_BELONG_TO_STORE);
    }

    @Test
    @DisplayName("상품에 옵션이 없는 경우 빈 리스트 반환")
    void getProductOptions_Success_EmptyOptions() {
        // given
        Product productWithoutOptions = Product.builder()
                .name("옵션 없는 상품")
                .price(new BigDecimal("20000"))
                .store(store)
                .build();
        ReflectionTestUtils.setField(productWithoutOptions, "productId", 3L);

        given(userDetails.getUsername()).willReturn("seller@test.com");
        given(userRepository.findByLoginId("seller@test.com")).willReturn(Optional.of(seller));
        given(productRepository.findByProductId(3L)).willReturn(Optional.of(productWithoutOptions));
        given(storeRepository.findBySellerId(1L)).willReturn(Optional.of(store));

        // when
        List<ProductOptionInfo> result = productOptionService.getProductOptions(3L, userDetails);

        // then
        assertThat(result).isEmpty();
    }
}