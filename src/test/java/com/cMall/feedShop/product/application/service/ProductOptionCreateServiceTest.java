package com.cMall.feedShop.product.application.service;

import com.cMall.feedShop.common.exception.ErrorCode;
import com.cMall.feedShop.product.application.dto.request.ProductOptionCreateRequest;
import com.cMall.feedShop.product.application.dto.response.ProductOptionCreateResponse;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductOptionCreateService 테스트")
class ProductOptionCreateServiceTest {

    @Mock
    private ProductOptionRepository productOptionRepository;

    @Mock
    private ProductOptionHelper productOptionHelper;

    @InjectMocks
    private ProductOptionCreateService productOptionCreateService;

    private User sellerUser;
    private Store testStore;
    private Product testProduct;
    private ProductOptionCreateRequest createRequest;
    private ProductOption testProductOption;

    @BeforeEach
    void setUp() {
        setupTestData();
    }

    private void setupTestData() {
        // 판매자 사용자
        sellerUser = new User("seller", "password", "seller@test.com", UserRole.SELLER);
        ReflectionTestUtils.setField(sellerUser, "id", 1L);

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

        // 생성 요청
        createRequest = new ProductOptionCreateRequest();
        ReflectionTestUtils.setField(createRequest, "gender", Gender.MEN);
        ReflectionTestUtils.setField(createRequest, "size", Size.SIZE_260);
        ReflectionTestUtils.setField(createRequest, "color", Color.BLUE);
        ReflectionTestUtils.setField(createRequest, "stock", 20);

        // 테스트 상품 옵션
        testProductOption = new ProductOption(Gender.MEN, Size.SIZE_260, Color.BLUE, 20, testProduct);
        ReflectionTestUtils.setField(testProductOption, "optionId", 1L);
    }

    @Test
    @DisplayName("상품 옵션 추가 성공")
    void addProductOption_Success() {
        // given
        given(productOptionHelper.getCurrentUser("seller")).willReturn(sellerUser);
        given(productOptionHelper.getProductOwnership(1L, 1L)).willReturn(testProduct);
        given(productOptionHelper.createNewOption(createRequest, testProduct)).willReturn(testProductOption);
        given(productOptionRepository.save(any(ProductOption.class))).willReturn(testProductOption);

        // when
        ProductOptionCreateResponse result = productOptionCreateService.addProductOption(1L, createRequest, "seller");

        // then
        assertThat(result).isNotNull();
        assertThat(result.getOptionId()).isEqualTo(1L);

        verify(productOptionHelper).getCurrentUser("seller");
        verify(productOptionHelper).getProductOwnership(1L, 1L);
        verify(productOptionHelper).validateDuplicateOption(testProduct, createRequest);
        verify(productOptionHelper).createNewOption(createRequest, testProduct);
        verify(productOptionRepository).save(testProductOption);
    }

    @Test
    @DisplayName("상품 옵션 추가 실패 - 사용자 없음")
    void addProductOption_Fail_UserNotFound() {
        // given
        given(productOptionHelper.getCurrentUser("nonexistent"))
                .willThrow(new ProductException(ErrorCode.USER_NOT_FOUND));

        // when & then
        assertThatThrownBy(() -> productOptionCreateService.addProductOption(1L, createRequest, "nonexistent"))
                .isInstanceOf(ProductException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_NOT_FOUND);

        verify(productOptionHelper).getCurrentUser("nonexistent");
        verify(productOptionRepository, never()).save(any());
    }

    @Test
    @DisplayName("상품 옵션 추가 실패 - 상품 소유권 없음")
    void addProductOption_Fail_NoProductOwnership() {
        // given
        given(productOptionHelper.getCurrentUser("seller")).willReturn(sellerUser);
        given(productOptionHelper.getProductOwnership(1L, 1L))
                .willThrow(new ProductException(ErrorCode.PRODUCT_NOT_BELONG_TO_STORE));

        // when & then
        assertThatThrownBy(() -> productOptionCreateService.addProductOption(1L, createRequest, "seller"))
                .isInstanceOf(ProductException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PRODUCT_NOT_BELONG_TO_STORE);

        verify(productOptionHelper).getCurrentUser("seller");
        verify(productOptionHelper).getProductOwnership(1L, 1L);
        verify(productOptionRepository, never()).save(any());
    }

    @Test
    @DisplayName("상품 옵션 추가 실패 - 중복 옵션")
    void addProductOption_Fail_DuplicateOption() {
        // given
        given(productOptionHelper.getCurrentUser("seller")).willReturn(sellerUser);
        given(productOptionHelper.getProductOwnership(1L, 1L)).willReturn(testProduct);
        doThrow(new ProductException(ErrorCode.DUPLICATE_PRODUCT_OPTION))
                .when(productOptionHelper).validateDuplicateOption(testProduct, createRequest);

        // when & then
        assertThatThrownBy(() -> productOptionCreateService.addProductOption(1L, createRequest, "seller"))
                .isInstanceOf(ProductException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.DUPLICATE_PRODUCT_OPTION);

        verify(productOptionHelper).getCurrentUser("seller");
        verify(productOptionHelper).getProductOwnership(1L, 1L);
        verify(productOptionHelper).validateDuplicateOption(testProduct, createRequest);
        verify(productOptionRepository, never()).save(any());
    }

    @Test
    @DisplayName("상품 옵션 추가 메서드 호출 순서 검증")
    void addProductOption_CallOrderVerification() {
        // given
        given(productOptionHelper.getCurrentUser("seller")).willReturn(sellerUser);
        given(productOptionHelper.getProductOwnership(1L, 1L)).willReturn(testProduct);
        given(productOptionHelper.createNewOption(createRequest, testProduct)).willReturn(testProductOption);
        given(productOptionRepository.save(any(ProductOption.class))).willReturn(testProductOption);

        // when
        productOptionCreateService.addProductOption(1L, createRequest, "seller");

        // then
        var inOrder = inOrder(productOptionHelper, productOptionRepository);
        inOrder.verify(productOptionHelper).getCurrentUser("seller");
        inOrder.verify(productOptionHelper).getProductOwnership(1L, 1L);
        inOrder.verify(productOptionHelper).validateDuplicateOption(testProduct, createRequest);
        inOrder.verify(productOptionHelper).createNewOption(createRequest, testProduct);
        inOrder.verify(productOptionRepository).save(testProductOption);
    }

    @Test
    @DisplayName("상품 옵션 추가 성공 - 다양한 옵션 조합")
    void addProductOption_Success_VariousOptions() {
        // given
        ProductOptionCreateRequest womenRequest = new ProductOptionCreateRequest();
        ReflectionTestUtils.setField(womenRequest, "gender", Gender.WOMEN);
        ReflectionTestUtils.setField(womenRequest, "size", Size.SIZE_240);
        ReflectionTestUtils.setField(womenRequest, "color", Color.RED);
        ReflectionTestUtils.setField(womenRequest, "stock", 15);
        ProductOption womenOption = new ProductOption(Gender.WOMEN, Size.SIZE_240, Color.RED, 15, testProduct);
        ReflectionTestUtils.setField(womenOption, "optionId", 2L);

        given(productOptionHelper.getCurrentUser("seller")).willReturn(sellerUser);
        given(productOptionHelper.getProductOwnership(1L, 1L)).willReturn(testProduct);
        given(productOptionHelper.createNewOption(womenRequest, testProduct)).willReturn(womenOption);
        given(productOptionRepository.save(any(ProductOption.class))).willReturn(womenOption);

        // when
        ProductOptionCreateResponse result = productOptionCreateService.addProductOption(1L, womenRequest, "seller");

        // then
        assertThat(result).isNotNull();
        assertThat(result.getOptionId()).isEqualTo(2L);

        verify(productOptionHelper).getCurrentUser("seller");
        verify(productOptionHelper).getProductOwnership(1L, 1L);
        verify(productOptionHelper).validateDuplicateOption(testProduct, womenRequest);
        verify(productOptionHelper).createNewOption(womenRequest, testProduct);
        verify(productOptionRepository).save(womenOption);
    }

    @Test
    @DisplayName("상품 옵션 추가 성공 - 재고 0개")
    void addProductOption_Success_ZeroStock() {
        // given
        ProductOptionCreateRequest zeroStockRequest = new ProductOptionCreateRequest();
        ReflectionTestUtils.setField(zeroStockRequest, "gender", Gender.UNISEX);
        ReflectionTestUtils.setField(zeroStockRequest, "size", Size.SIZE_250);
        ReflectionTestUtils.setField(zeroStockRequest, "color", Color.BLACK);
        ReflectionTestUtils.setField(zeroStockRequest, "stock", 0);
        ProductOption zeroStockOption = new ProductOption(Gender.UNISEX, Size.SIZE_250, Color.BLACK, 0, testProduct);
        ReflectionTestUtils.setField(zeroStockOption, "optionId", 3L);

        given(productOptionHelper.getCurrentUser("seller")).willReturn(sellerUser);
        given(productOptionHelper.getProductOwnership(1L, 1L)).willReturn(testProduct);
        given(productOptionHelper.createNewOption(zeroStockRequest, testProduct)).willReturn(zeroStockOption);
        given(productOptionRepository.save(any(ProductOption.class))).willReturn(zeroStockOption);

        // when
        ProductOptionCreateResponse result = productOptionCreateService.addProductOption(1L, zeroStockRequest, "seller");

        // then
        assertThat(result).isNotNull();
        assertThat(result.getOptionId()).isEqualTo(3L);

        verify(productOptionRepository).save(zeroStockOption);
    }
}