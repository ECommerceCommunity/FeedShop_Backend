package com.cMall.feedShop.product.application.service;

import com.cMall.feedShop.common.exception.ErrorCode;
import com.cMall.feedShop.order.domain.repository.OrderItemRepository;
import com.cMall.feedShop.product.application.dto.request.ProductOptionCreateRequest;
import com.cMall.feedShop.product.application.dto.request.ProductOptionUpdateRequest;
import com.cMall.feedShop.product.domain.enums.Color;
import com.cMall.feedShop.product.domain.enums.Gender;
import com.cMall.feedShop.product.domain.enums.Size;
import com.cMall.feedShop.product.domain.exception.ProductException;
import com.cMall.feedShop.product.domain.model.Product;
import com.cMall.feedShop.product.domain.model.ProductOption;
import com.cMall.feedShop.product.domain.repository.ProductOptionRepository;
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
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductOptionHelper 테스트")
class ProductOptionHelperTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductOptionRepository productOptionRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private StoreRepository storeRepository;

    @Mock
    private OrderItemRepository orderItemRepository;

    @InjectMocks
    private ProductOptionHelper productOptionHelper;

    private User sellerUser;
    private User normalUser;
    private Store testStore;
    private Product testProduct;
    private ProductOption testProductOption;

    @BeforeEach
    void setUp() {
        setupTestData();
    }

    private void setupTestData() {
        // 판매자 사용자
        sellerUser = new User("seller", "password", "seller@test.com", UserRole.SELLER);
        ReflectionTestUtils.setField(sellerUser, "id", 1L);

        // 일반 사용자
        normalUser = new User("user", "password", "user@test.com", UserRole.USER);
        ReflectionTestUtils.setField(normalUser, "id", 2L);

        // 테스트 스토어
        testStore = Store.builder()
                .storeName("테스트 스토어")
                .sellerId(1L)
                .build();
        ReflectionTestUtils.setField(testStore, "storeId", 1L);

        // 테스트 상품
        testProduct = Product.builder()
                .name("테스트 상품")
                .price(new BigDecimal("10000"))
                .store(testStore)
                .build();
        ReflectionTestUtils.setField(testProduct, "productId", 1L);

        // 테스트 상품 옵션
        testProductOption = new ProductOption(Gender.UNISEX, Size.SIZE_250, Color.BLACK, 10, testProduct);
        ReflectionTestUtils.setField(testProductOption, "optionId", 1L);
    }

    @Test
    @DisplayName("현재 사용자 조회 성공")
    void getCurrentUser_Success() {
        // given
        given(userRepository.findByLoginId("seller")).willReturn(Optional.of(sellerUser));

        // when
        User result = productOptionHelper.getCurrentUser("seller");

        // then
        assertThat(result).isEqualTo(sellerUser);
        verify(userRepository).findByLoginId("seller");
    }

    @Test
    @DisplayName("현재 사용자 조회 실패 - 사용자 없음")
    void getCurrentUser_Fail_UserNotFound() {
        // given
        given(userRepository.findByLoginId("nonexistent")).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> productOptionHelper.getCurrentUser("nonexistent"))
                .isInstanceOf(ProductException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_NOT_FOUND);
    }

    @Test
    @DisplayName("판매자 권한 검증 성공")
    void validateSellerRole_Success() {
        // when & then
        productOptionHelper.validateSellerRole(sellerUser);
        // 예외가 발생하지 않으면 성공
    }

    @Test
    @DisplayName("판매자 권한 검증 실패 - 일반 사용자")
    void validateSellerRole_Fail_NormalUser() {
        // when & then
        assertThatThrownBy(() -> productOptionHelper.validateSellerRole(normalUser))
                .isInstanceOf(ProductException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.FORBIDDEN);
    }

    @Test
    @DisplayName("상품 소유권 검증 성공")
    void getProductOwnership_Success() {
        // given
        given(productRepository.findByProductId(1L)).willReturn(Optional.of(testProduct));
        given(storeRepository.findBySellerId(1L)).willReturn(Optional.of(testStore));

        // when
        Product result = productOptionHelper.getProductOwnership(1L, 1L);

        // then
        assertThat(result).isEqualTo(testProduct);
    }

    @Test
    @DisplayName("상품 소유권 검증 실패 - 상품 없음")
    void getProductOwnership_Fail_ProductNotFound() {
        // given
        given(productRepository.findByProductId(999L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> productOptionHelper.getProductOwnership(999L, 1L))
                .isInstanceOf(ProductException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PRODUCT_NOT_FOUND);
    }

    @Test
    @DisplayName("중복 옵션 검증 성공 - 중복 없음")
    void validateDuplicateOption_Success() {
        // given
        ProductOptionCreateRequest request = new ProductOptionCreateRequest();
        ReflectionTestUtils.setField(request, "gender", Gender.UNISEX);
        ReflectionTestUtils.setField(request, "size", Size.SIZE_260);
        ReflectionTestUtils.setField(request, "color", Color.WHITE);
        ReflectionTestUtils.setField(request, "stock", 5);
        given(productOptionRepository.existsByProduct_ProductIdAndGenderAndSizeAndColor(
                1L, Gender.UNISEX, Size.SIZE_260, Color.WHITE)).willReturn(false);

        // when & then
        productOptionHelper.validateDuplicateOption(testProduct, request);
        // 예외가 발생하지 않으면 성공
    }

    @Test
    @DisplayName("중복 옵션 검증 실패 - 중복 존재")
    void validateDuplicateOption_Fail_Duplicate() {
        // given
        ProductOptionCreateRequest request = new ProductOptionCreateRequest();
        ReflectionTestUtils.setField(request, "gender", Gender.UNISEX);
        ReflectionTestUtils.setField(request, "size", Size.SIZE_250);
        ReflectionTestUtils.setField(request, "color", Color.BLACK);
        ReflectionTestUtils.setField(request, "stock", 5);
        given(productOptionRepository.existsByProduct_ProductIdAndGenderAndSizeAndColor(
                1L, Gender.UNISEX, Size.SIZE_250, Color.BLACK)).willReturn(true);

        // when & then
        assertThatThrownBy(() -> productOptionHelper.validateDuplicateOption(testProduct, request))
                .isInstanceOf(ProductException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.DUPLICATE_PRODUCT_OPTION);
    }

    @Test
    @DisplayName("새 옵션 생성 성공")
    void createNewOption_Success() {
        // given
        ProductOptionCreateRequest request = new ProductOptionCreateRequest();
        ReflectionTestUtils.setField(request, "gender", Gender.MEN);
        ReflectionTestUtils.setField(request, "size", Size.SIZE_260);
        ReflectionTestUtils.setField(request, "color", Color.RED);
        ReflectionTestUtils.setField(request, "stock", 15);

        // when
        ProductOption result = productOptionHelper.createNewOption(request, testProduct);

        // then
        assertThat(result.getGender()).isEqualTo(Gender.MEN);
        assertThat(result.getSize()).isEqualTo(Size.SIZE_260);
        assertThat(result.getColor()).isEqualTo(Color.RED);
        assertThat(result.getStock()).isEqualTo(15);
        assertThat(result.getProduct()).isEqualTo(testProduct);
    }

    @Test
    @DisplayName("상품 옵션 조회 성공")
    void getProductOption_Success() {
        // given
        given(productOptionRepository.findByOptionId(1L)).willReturn(Optional.of(testProductOption));

        // when
        ProductOption result = productOptionHelper.getProductOption(1L);

        // then
        assertThat(result).isEqualTo(testProductOption);
    }

    @Test
    @DisplayName("상품 옵션 조회 실패 - 옵션 없음")
    void getProductOption_Fail_OptionNotFound() {
        // given
        given(productOptionRepository.findByOptionId(999L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> productOptionHelper.getProductOption(999L))
                .isInstanceOf(ProductException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PRODUCT_OPTION_NOT_FOUND);
    }

    @Test
    @DisplayName("판매자 권한 검증 성공 - 옵션 소유자")
    void validateSellerPermission_Success() {
        // when & then
        productOptionHelper.validateSellerPermission(sellerUser, testProductOption);
        // 예외가 발생하지 않으면 성공
    }

    @Test
    @DisplayName("판매자 권한 검증 실패 - 다른 판매자")
    void validateSellerPermission_Fail_DifferentSeller() {
        // given
        User otherSeller = new User("other", "password", "other@test.com", UserRole.SELLER);
        ReflectionTestUtils.setField(otherSeller, "id", 3L);

        // when & then
        assertThatThrownBy(() -> productOptionHelper.validateSellerPermission(otherSeller, testProductOption))
                .isInstanceOf(ProductException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.FORBIDDEN);
    }

    @Test
    @DisplayName("옵션 정보 업데이트 성공")
    void updateOptionInfo_Success() {
        // given
        ProductOptionUpdateRequest request = new ProductOptionUpdateRequest();
        ReflectionTestUtils.setField(request, "stock", 20);
        ReflectionTestUtils.setField(request, "gender", "WOMEN");
        ReflectionTestUtils.setField(request, "size", "270");
        ReflectionTestUtils.setField(request, "color", "BLUE");

        // when
        productOptionHelper.updateOptionInfo(testProductOption, request);

        // then
        assertThat(testProductOption.getStock()).isEqualTo(20);
        assertThat(testProductOption.getGender()).isEqualTo(Gender.WOMEN);
        assertThat(testProductOption.getSize()).isEqualTo(Size.SIZE_270);
        assertThat(testProductOption.getColor()).isEqualTo(Color.BLUE);
    }

    @Test
    @DisplayName("주문된 옵션 검증 성공 - 주문 없음")
    void validateNotOrderedOption_Success() {
        // given
        given(orderItemRepository.existsByProductOption(testProductOption)).willReturn(false);

        // when & then
        productOptionHelper.validateNotOrderedOption(testProductOption);
        // 예외가 발생하지 않으면 성공
    }

    @Test
    @DisplayName("주문된 옵션 검증 실패 - 주문 존재")
    void validateNotOrderedOption_Fail_OrderExists() {
        // given
        given(orderItemRepository.existsByProductOption(testProductOption)).willReturn(true);

        // when & then
        assertThatThrownBy(() -> productOptionHelper.validateNotOrderedOption(testProductOption))
                .isInstanceOf(ProductException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PRODUCT_IN_ORDER);
    }
}