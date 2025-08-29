package com.cMall.feedShop.product.application.service;

import com.cMall.feedShop.common.exception.ErrorCode;
import com.cMall.feedShop.product.application.dto.response.info.ProductOptionInfo;
import com.cMall.feedShop.product.domain.enums.Color;
import com.cMall.feedShop.product.domain.enums.Gender;
import com.cMall.feedShop.product.domain.enums.Size;
import com.cMall.feedShop.product.domain.exception.ProductException;
import com.cMall.feedShop.product.domain.model.Product;
import com.cMall.feedShop.product.domain.model.ProductOption;
import com.cMall.feedShop.store.domain.model.Store;
import com.cMall.feedShop.user.domain.enums.UserRole;
import com.cMall.feedShop.user.domain.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductOptionReadService 테스트")
class ProductOptionReadServiceTest {

    @Mock
    private ProductOptionHelper productOptionHelper;

    @InjectMocks
    private ProductOptionReadService productOptionReadService;

    private User sellerUser;
    private User normalUser;
    private Store testStore;
    private Product testProduct;
    private List<ProductOption> testOptions;

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

        // 테스트 상품 옵션들
        ProductOption option1 = new ProductOption(Gender.MEN, Size.SIZE_250, Color.BLACK, 10, testProduct);
        ReflectionTestUtils.setField(option1, "optionId", 1L);
        
        ProductOption option2 = new ProductOption(Gender.WOMEN, Size.SIZE_240, Color.WHITE, 5, testProduct);
        ReflectionTestUtils.setField(option2, "optionId", 2L);

        testOptions = Arrays.asList(option1, option2);
        ReflectionTestUtils.setField(testProduct, "productOptions", testOptions);
    }

    @Test
    @DisplayName("상품 옵션 조회 성공")
    void getProductOptions_Success() {
        // given
        given(productOptionHelper.getCurrentUser("seller")).willReturn(sellerUser);
        given(productOptionHelper.getProductOwnership(1L, 1L)).willReturn(testProduct);

        // when
        List<ProductOptionInfo> result = productOptionReadService.getProductOptions(1L, "seller");

        // then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getOptionId()).isEqualTo(1L);
        assertThat(result.get(0).getGender()).isEqualTo(Gender.MEN);
        assertThat(result.get(0).getSize()).isEqualTo(Size.SIZE_250);
        assertThat(result.get(0).getColor()).isEqualTo(Color.BLACK);
        assertThat(result.get(0).getStock()).isEqualTo(10);

        assertThat(result.get(1).getOptionId()).isEqualTo(2L);
        assertThat(result.get(1).getGender()).isEqualTo(Gender.WOMEN);
        assertThat(result.get(1).getSize()).isEqualTo(Size.SIZE_240);
        assertThat(result.get(1).getColor()).isEqualTo(Color.WHITE);
        assertThat(result.get(1).getStock()).isEqualTo(5);

        verify(productOptionHelper).getCurrentUser("seller");
        verify(productOptionHelper).validateSellerRole(sellerUser);
        verify(productOptionHelper).getProductOwnership(1L, 1L);
    }

    @Test
    @DisplayName("상품 옵션 조회 실패 - 사용자 없음")
    void getProductOptions_Fail_UserNotFound() {
        // given
        given(productOptionHelper.getCurrentUser("nonexistent"))
                .willThrow(new ProductException(ErrorCode.USER_NOT_FOUND));

        // when & then
        assertThatThrownBy(() -> productOptionReadService.getProductOptions(1L, "nonexistent"))
                .isInstanceOf(ProductException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_NOT_FOUND);

        verify(productOptionHelper).getCurrentUser("nonexistent");
    }

    @Test
    @DisplayName("상품 옵션 조회 실패 - 판매자 권한 없음")
    void getProductOptions_Fail_NotSeller() {
        // given
        given(productOptionHelper.getCurrentUser("user")).willReturn(normalUser);
        doThrow(new ProductException(ErrorCode.FORBIDDEN))
                .when(productOptionHelper).validateSellerRole(normalUser);

        // when & then
        assertThatThrownBy(() -> productOptionReadService.getProductOptions(1L, "user"))
                .isInstanceOf(ProductException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.FORBIDDEN);

        verify(productOptionHelper).getCurrentUser("user");
        verify(productOptionHelper).validateSellerRole(normalUser);
    }

    @Test
    @DisplayName("상품 옵션 조회 실패 - 상품 소유권 없음")
    void getProductOptions_Fail_NoProductOwnership() {
        // given
        given(productOptionHelper.getCurrentUser("seller")).willReturn(sellerUser);
        given(productOptionHelper.getProductOwnership(1L, 1L))
                .willThrow(new ProductException(ErrorCode.PRODUCT_NOT_BELONG_TO_STORE));

        // when & then
        assertThatThrownBy(() -> productOptionReadService.getProductOptions(1L, "seller"))
                .isInstanceOf(ProductException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PRODUCT_NOT_BELONG_TO_STORE);

        verify(productOptionHelper).getCurrentUser("seller");
        verify(productOptionHelper).validateSellerRole(sellerUser);
        verify(productOptionHelper).getProductOwnership(1L, 1L);
    }

    @Test
    @DisplayName("상품 옵션 조회 성공 - 빈 옵션 리스트")
    void getProductOptions_Success_EmptyOptions() {
        // given
        Product emptyProduct = Product.builder()
                .name("빈 상품")
                .price(new BigDecimal("5000"))
                .store(testStore)
                .build();
        ReflectionTestUtils.setField(emptyProduct, "productId", 2L);
        ReflectionTestUtils.setField(emptyProduct, "productOptions", Arrays.asList());

        given(productOptionHelper.getCurrentUser("seller")).willReturn(sellerUser);
        given(productOptionHelper.getProductOwnership(2L, 1L)).willReturn(emptyProduct);

        // when
        List<ProductOptionInfo> result = productOptionReadService.getProductOptions(2L, "seller");

        // then
        assertThat(result).isEmpty();

        verify(productOptionHelper).getCurrentUser("seller");
        verify(productOptionHelper).validateSellerRole(sellerUser);
        verify(productOptionHelper).getProductOwnership(2L, 1L);
    }

    @Test
    @DisplayName("상품 옵션 조회 메서드 호출 순서 검증")
    void getProductOptions_CallOrderVerification() {
        // given
        given(productOptionHelper.getCurrentUser("seller")).willReturn(sellerUser);
        given(productOptionHelper.getProductOwnership(1L, 1L)).willReturn(testProduct);

        // when
        productOptionReadService.getProductOptions(1L, "seller");

        // then
        var inOrder = org.mockito.Mockito.inOrder(productOptionHelper);
        inOrder.verify(productOptionHelper).getCurrentUser("seller");
        inOrder.verify(productOptionHelper).validateSellerRole(sellerUser);
        inOrder.verify(productOptionHelper).getProductOwnership(1L, 1L);
    }
}