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
import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

/**
 * ProductOptionService 테스트 클래스
 * 상품 옵션 추가 기능을 테스트합니다
 */
@ExtendWith(MockitoExtension.class)
class ProductOptionServiceAddTest {

    // Mock 객체들 - 실제 객체 대신 가짜 객체를 사용
    @Mock private ProductRepository productRepository;
    @Mock private UserRepository userRepository;
    @Mock private StoreRepository storeRepository;
    @Mock private ProductOptionRepository productOptionRepository;
    @Mock private UserDetails userDetails;

    // 테스트할 실제 서비스 객체
    @InjectMocks
    private ProductOptionService productOptionService;

    // 테스트에서 공통으로 사용할 데이터들
    private User seller;           // 판매자 계정
    private Store store;           // 판매자의 가게
    private Product product;       // 상품
    private ProductOptionCreateRequest request;  // 옵션 추가 요청

    /**
     * 각 테스트 실행 전에 기본 데이터를 준비하는 메서드
     */
    @BeforeEach
    void setUp() {
        // 판매자 계정 만들기
        seller = createSeller();

        // 판매자의 가게 만들기
        store = createStore();

        // 테스트용 상품 만들기
        product = createProduct();

        // 옵션 추가 요청 데이터 만들기
        request = createOptionRequest();
    }

    /**
     * 테스트 1: 정상적으로 상품 옵션이 추가되는 경우
     */
    @Test
    @DisplayName("상품 옵션 추가 성공")
    void addProductOption_Success() {
        // given - 테스트 준비
        Long productId = 1L;
        Long savedOptionId = 10L;

        // Mock 객체들이 어떻게 동작할지 정의
        given(userDetails.getUsername()).willReturn("seller123");
        given(userRepository.findByLoginId("seller123")).willReturn(Optional.of(seller));
        given(productRepository.findByProductId(productId)).willReturn(Optional.of(product));
        given(storeRepository.findBySellerId(seller.getId())).willReturn(Optional.of(store));

        // 저장될 옵션 객체 만들기
        ProductOption savedOption = new ProductOption(
                Gender.UNISEX, Size.SIZE_250, Color.BLACK, 50, product
        );
        ReflectionTestUtils.setField(savedOption, "optionId", savedOptionId);
        given(productOptionRepository.save(any(ProductOption.class))).willReturn(savedOption);

        // when - 실제 메서드 실행
        ProductOptionCreateResponse response = productOptionService.addProductOption(
                productId, request, userDetails
        );

        // then - 결과 확인
        assertThat(response).isNotNull();                    // 응답이 null이 아닌지 확인
        assertThat(response.getOptionId()).isEqualTo(savedOptionId);  // 옵션 ID가 맞는지 확인

        // Mock 메서드들이 정확히 1번씩 호출되었는지 확인
        verify(userRepository, times(1)).findByLoginId("seller123");
        verify(productRepository, times(1)).findByProductId(productId);
        verify(storeRepository, times(1)).findBySellerId(seller.getId());
        verify(productOptionRepository, times(1)).save(any(ProductOption.class));
    }

    /**
     * 테스트 2: 사용자가 존재하지 않는 경우
     */
    @Test
    @DisplayName("사용자를 찾을 수 없는 경우 예외 발생")
    void addProductOption_UserNotFound() {
        // given - 존재하지 않는 사용자 ID로 테스트
        Long productId = 1L;
        given(userDetails.getUsername()).willReturn("unknown_user");
        given(userRepository.findByLoginId("unknown_user")).willReturn(Optional.empty());

        // when & then - 예외가 발생하는지 확인
        assertThatThrownBy(() ->
                productOptionService.addProductOption(productId, request, userDetails))
                .isInstanceOf(ProductException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_NOT_FOUND);
    }

    /**
     * 테스트 3: 사용자가 판매자 권한이 없는 경우
     */
    @Test
    @DisplayName("판매자 권한이 없는 경우 예외 발생")
    void addProductOption_NotSeller() {
        // given - 일반 사용자(구매자) 계정으로 테스트
        Long productId = 1L;
        User buyer = createBuyer();  // 판매자가 아닌 일반 구매자

        given(userDetails.getUsername()).willReturn("buyer123");
        given(userRepository.findByLoginId("buyer123")).willReturn(Optional.of(buyer));

        // when & then - 권한 없음 예외가 발생하는지 확인
        assertThatThrownBy(() ->
                productOptionService.addProductOption(productId, request, userDetails))
                .isInstanceOf(ProductException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.FORBIDDEN);
    }

    /**
     * 테스트 4: 상품이 존재하지 않는 경우
     */
    @Test
    @DisplayName("상품을 찾을 수 없는 경우 예외 발생")
    void addProductOption_ProductNotFound() {
        // given - 존재하지 않는 상품 ID로 테스트
        Long productId = 999L;

        given(userDetails.getUsername()).willReturn("seller123");
        given(userRepository.findByLoginId("seller123")).willReturn(Optional.of(seller));
        given(productRepository.findByProductId(productId)).willReturn(Optional.empty());

        // when & then - 상품을 찾을 수 없다는 예외가 발생하는지 확인
        assertThatThrownBy(() ->
                productOptionService.addProductOption(productId, request, userDetails))
                .isInstanceOf(ProductException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PRODUCT_NOT_FOUND);
    }

    /**
     * 테스트 5: 다른 판매자의 상품에 옵션을 추가하려는 경우
     */
    @Test
    @DisplayName("다른 판매자의 상품인 경우 예외 발생")
    void addProductOption_NotOwnerProduct() {
        // given - 다른 판매자의 상품으로 설정
        Long productId = 1L;

        // 다른 판매자의 가게 만들기
        Store otherStore = new Store();
        ReflectionTestUtils.setField(otherStore, "storeId", 999L);
        ReflectionTestUtils.setField(otherStore, "sellerId", 999L);

        // 상품의 소유 가게를 다른 가게로 설정
        ReflectionTestUtils.setField(product, "store", otherStore);

        given(userDetails.getUsername()).willReturn("seller123");
        given(userRepository.findByLoginId("seller123")).willReturn(Optional.of(seller));
        given(productRepository.findByProductId(productId)).willReturn(Optional.of(product));
        given(storeRepository.findBySellerId(seller.getId())).willReturn(Optional.of(store));

        // when & then - 상품 소유권 없음 예외가 발생하는지 확인
        assertThatThrownBy(() ->
                productOptionService.addProductOption(productId, request, userDetails))
                .isInstanceOf(ProductException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PRODUCT_NOT_BELONG_TO_STORE);
    }

    /**
     * 테스트 6: 이미 같은 옵션이 존재하는 경우
     */
    @Test
    @DisplayName("중복된 상품 옵션인 경우 예외 발생")
    void addProductOption_DuplicateOption() {
        // given - 이미 같은 옵션이 존재하는 상황 만들기
        Long productId = 1L;

        given(userDetails.getUsername()).willReturn("seller123");
        given(userRepository.findByLoginId("seller123")).willReturn(Optional.of(seller));
        given(productRepository.findByProductId(productId)).willReturn(Optional.of(product));
        given(storeRepository.findBySellerId(seller.getId())).willReturn(Optional.of(store));

        // 중복 검증 쿼리를 mock으로 설정 - 중복된 옵션이 존재한다고 반환
        given(productOptionRepository.existsByProduct_ProductIdAndGenderAndSizeAndColor(
                productId, Gender.UNISEX, Size.SIZE_250, Color.BLACK
        )).willReturn(true);

        // when & then - 중복 옵션 예외가 발생하는지 확인
        assertThatThrownBy(() ->
                productOptionService.addProductOption(productId, request, userDetails))
                .isInstanceOf(ProductException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.DUPLICATE_PRODUCT_OPTION);

        // save 메서드가 호출되지 않았는지 확인
        verify(productOptionRepository, never()).save(any(ProductOption.class));
    }

    /**
     * 테스트 7: 판매자의 가게가 존재하지 않는 경우
     */
    @Test
    @DisplayName("가게를 찾을 수 없는 경우 예외 발생")
    void addProductOption_StoreNotFound() {
        // given - 가게가 없는 판매자로 테스트
        Long productId = 1L;

        given(userDetails.getUsername()).willReturn("seller123");
        given(userRepository.findByLoginId("seller123")).willReturn(Optional.of(seller));
        given(productRepository.findByProductId(productId)).willReturn(Optional.of(product));
        given(storeRepository.findBySellerId(seller.getId())).willReturn(Optional.empty());

        // when & then - 가게를 찾을 수 없다는 예외가 발생하는지 확인
        assertThatThrownBy(() ->
                productOptionService.addProductOption(productId, request, userDetails))
                .isInstanceOf(ProductException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.STORE_NOT_FOUND);
    }

    // ==================== 테스트 데이터 생성 메서드들 ====================

    /**
     * 테스트용 판매자 계정 만들기
     */
    private User createSeller() {
        User seller = new User();
        ReflectionTestUtils.setField(seller, "id", 1L);
        ReflectionTestUtils.setField(seller, "loginId", "seller123");
        ReflectionTestUtils.setField(seller, "email", "seller123@test.com");
        ReflectionTestUtils.setField(seller, "role", UserRole.SELLER);
        return seller;
    }

    /**
     * 테스트용 구매자 계정 만들기
     */
    private User createBuyer() {
        User buyer = new User();
        ReflectionTestUtils.setField(buyer, "id", 2L);
        ReflectionTestUtils.setField(buyer, "loginId", "buyer123");
        ReflectionTestUtils.setField(buyer, "email", "buyer123@test.com");
        ReflectionTestUtils.setField(buyer, "role", UserRole.USER);
        return buyer;
    }

    /**
     * 테스트용 가게 만들기
     */
    private Store createStore() {
        Store store = new Store();
        ReflectionTestUtils.setField(store, "storeId", 1L);
        ReflectionTestUtils.setField(store, "sellerId", 1L);
        ReflectionTestUtils.setField(store, "storeName", "테스트 신발가게");
        return store;
    }

    /**
     * 테스트용 상품 만들기
     */
    private Product createProduct() {
        Product product = new Product();
        ReflectionTestUtils.setField(product, "productId", 1L);
        ReflectionTestUtils.setField(product, "name", "테스트 운동화");
        ReflectionTestUtils.setField(product, "price", new BigDecimal("100000"));
        ReflectionTestUtils.setField(product, "store", store);
        ReflectionTestUtils.setField(product, "productOptions", Collections.emptyList());
        return product;
    }

    /**
     * 테스트용 옵션 추가 요청 데이터 만들기
     */
    private ProductOptionCreateRequest createOptionRequest() {
        ProductOptionCreateRequest request = new ProductOptionCreateRequest();
        ReflectionTestUtils.setField(request, "gender", Gender.UNISEX);
        ReflectionTestUtils.setField(request, "size", Size.SIZE_250);
        ReflectionTestUtils.setField(request, "color", Color.BLACK);
        ReflectionTestUtils.setField(request, "stock", 50);
        return request;
    }
}