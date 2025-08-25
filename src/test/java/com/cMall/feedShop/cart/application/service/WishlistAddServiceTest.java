package com.cMall.feedShop.cart.application.service;

import com.cMall.feedShop.cart.application.dto.request.WishListRequest;
import com.cMall.feedShop.cart.application.dto.response.WishListAddResponse;
import com.cMall.feedShop.cart.domain.exception.CartException;
import com.cMall.feedShop.cart.domain.model.WishList;
import com.cMall.feedShop.cart.domain.repository.WishlistRepository;
import com.cMall.feedShop.common.exception.ErrorCode;
import org.springframework.dao.DataIntegrityViolationException;
import com.cMall.feedShop.product.domain.enums.CategoryType;
import com.cMall.feedShop.product.domain.enums.DiscountType;
import com.cMall.feedShop.product.domain.model.Category;
import com.cMall.feedShop.product.domain.model.Product;
import com.cMall.feedShop.product.domain.repository.ProductRepository;
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
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("WishlistService 찜 등록 기능 테스트")
class WishlistAddServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private WishlistRepository wishlistRepository;

    @InjectMocks
    private WishlistService wishlistService;

    // 테스트 데이터
    private User testUser;
    private Product testProduct;
    private Store testStore;
    private Category testCategory;
    private WishListRequest testRequest;
    private WishList savedWishList;

    @BeforeEach
    void setUp() {
        setupTestData();
    }

    private void setupTestData() {
        // 테스트용 사용자 생성
        testUser = new User("testLogin", "password123", "test@test.com", UserRole.USER);
        ReflectionTestUtils.setField(testUser, "id", 1L);

        // 테스트용 스토어 생성
        testStore = Store.builder()
                .storeName("테스트 스토어")
                .sellerId(1L)
                .build();
        ReflectionTestUtils.setField(testStore, "storeId", 1L);

        // 테스트용 카테고리 생성
        testCategory = new Category(CategoryType.SNEAKERS, "운동화");
        ReflectionTestUtils.setField(testCategory, "categoryId", 1L);

        // 테스트용 상품 생성
        testProduct = Product.builder()
                .name("나이키 에어맥스")
                .price(new BigDecimal("150000"))
                .store(testStore)
                .category(testCategory)
                .discountType(DiscountType.NONE)
                .description("편안한 운동화")
                .build();
        ReflectionTestUtils.setField(testProduct, "productId", 1L);

        // 테스트용 찜 요청 생성
        testRequest = new WishListRequest(1L);

        // 테스트용 저장된 찜 생성
        savedWishList = WishList.builder()
                .user(testUser)
                .product(testProduct)
                .build();
        ReflectionTestUtils.setField(savedWishList, "wishlistId", 1L);
        ReflectionTestUtils.setField(savedWishList, "createdAt", LocalDateTime.now());
    }

    // ==================== 성공 케이스 ====================

    @Test
    @DisplayName("찜 등록 성공")
    void addWishList_Success() {
        // given
        given(userRepository.findByLoginId("testLogin")).willReturn(Optional.of(testUser));
        given(productRepository.findByProductId(1L)).willReturn(Optional.of(testProduct));
        given(wishlistRepository.save(any(WishList.class))).willReturn(savedWishList);

        // when
        WishListAddResponse response = wishlistService.addWishList(testRequest, "testLogin");

        // then
        assertThat(response).isNotNull();
        assertThat(response.getWishlistId()).isEqualTo(1L);
        assertThat(response.getProductId()).isEqualTo(1L);
        assertThat(response.getCreatedAt()).isNotNull();

        // Mock 호출 검증
        verify(userRepository, times(1)).findByLoginId("testLogin");
        verify(productRepository, times(1)).findByProductId(1L);
        verify(wishlistRepository, times(1)).save(any(WishList.class));
    }

    @Test
    @DisplayName("찜 등록 시 상품 찜 수 증가 확인")
    void addWishList_Success_IncreaseWishNumber() {
        // given
        Product spyProduct = spy(testProduct); // spy 객체로 메서드 호출 추적
        given(userRepository.findByLoginId("testLogin")).willReturn(Optional.of(testUser));
        given(productRepository.findByProductId(1L)).willReturn(Optional.of(spyProduct));
        given(wishlistRepository.save(any(WishList.class))).willReturn(savedWishList);

        // when
        wishlistService.addWishList(testRequest, "testLogin");

        // then
        verify(wishlistRepository, times(1)).increaseWishCount(1L);
    }

    // ==================== 실패 케이스 - 사용자 관련 ====================

    @Test
    @DisplayName("찜 등록 실패 - 사용자 없음")
    void addWishList_Fail_UserNotFound() {
        // given
        given(userRepository.findByLoginId("nonExistentUser")).willReturn(Optional.empty());

        // when & then
        CartException thrown = assertThrows(
                CartException.class,
                () -> wishlistService.addWishList(testRequest, "nonExistentUser")
        );

        assertThat(thrown.getErrorCode()).isEqualTo(ErrorCode.USER_NOT_FOUND);
        verify(productRepository, never()).findByProductId(any());
        verify(wishlistRepository, never()).save(any());
    }

    // ==================== 실패 케이스 - 상품 관련 ====================

    @Test
    @DisplayName("찜 등록 실패 - 상품 없음")
    void addWishList_Fail_ProductNotFound() {
        // given
        given(userRepository.findByLoginId("testLogin")).willReturn(Optional.of(testUser));
        given(productRepository.findByProductId(999L)).willReturn(Optional.empty());

        WishListRequest invalidRequest = new WishListRequest(999L);

        // when & then
        CartException thrown = assertThrows(
                CartException.class,
                () -> wishlistService.addWishList(invalidRequest, "testLogin")
        );

        assertThat(thrown.getErrorCode()).isEqualTo(ErrorCode.PRODUCT_NOT_FOUND);
        verify(wishlistRepository, never()).save(any());
    }

    // ==================== 실패 케이스 - 중복 찜 ====================

    @Test
    @DisplayName("찜 등록 실패 - 이미 찜한 상품")
    void addWishList_Fail_AlreadyWished() {
        // given
        given(userRepository.findByLoginId("testLogin")).willReturn(Optional.of(testUser));
        given(productRepository.findByProductId(1L)).willReturn(Optional.of(testProduct));
        given(wishlistRepository.save(any(WishList.class))).willThrow(new DataIntegrityViolationException("중복 찜"));

        // when & then
        CartException thrown = assertThrows(
                CartException.class,
                () -> wishlistService.addWishList(testRequest, "testLogin")
        );

        assertThat(thrown.getErrorCode()).isEqualTo(ErrorCode.ALREADY_WISHED_PRODUCT);
        verify(wishlistRepository, times(1)).save(any(WishList.class));
    }

    // ==================== 경계값 테스트 ====================

    @Test
    @DisplayName("찜 등록 시 null 값 처리")
    void addWishList_HandleNullValues() {
        // given - loginId가 null인 경우
        given(userRepository.findByLoginId(null)).willReturn(Optional.empty());

        // when & then
        CartException thrown = assertThrows(
                CartException.class,
                () -> wishlistService.addWishList(testRequest, null)
        );

        assertThat(thrown.getErrorCode()).isEqualTo(ErrorCode.USER_NOT_FOUND);
    }

    @Test
    @DisplayName("찜 등록 시 빈 문자열 loginId 처리")
    void addWishList_HandleEmptyLoginId() {
        // given
        given(userRepository.findByLoginId("")).willReturn(Optional.empty());

        // when & then
        CartException thrown = assertThrows(
                CartException.class,
                () -> wishlistService.addWishList(testRequest, "")
        );

        assertThat(thrown.getErrorCode()).isEqualTo(ErrorCode.USER_NOT_FOUND);
    }

    // ==================== Mock 검증 테스트 ====================

    @Test
    @DisplayName("찜 등록 시 Repository 호출 순서 검증")
    void addWishList_VerifyRepositoryCallOrder() {
        // given
        given(userRepository.findByLoginId("testLogin")).willReturn(Optional.of(testUser));
        given(productRepository.findByProductId(1L)).willReturn(Optional.of(testProduct));
        given(wishlistRepository.save(any(WishList.class))).willReturn(savedWishList);

        // when
        wishlistService.addWishList(testRequest, "testLogin");

        // then - 호출 순서 검증
        var inOrder = inOrder(userRepository, productRepository, wishlistRepository);
        inOrder.verify(userRepository).findByLoginId("testLogin");
        inOrder.verify(productRepository).findByProductId(1L);
        inOrder.verify(wishlistRepository).save(any(WishList.class));
    }

    @Test
    @DisplayName("찜 등록 실패 시 상품 찜 수 증가하지 않음 검증")
    void addWishList_VerifyNoIncreaseWishNumberOnFailure() {
        // given - DataIntegrityViolationException 발생 상황
        Product spyProduct = spy(testProduct);
        given(userRepository.findByLoginId("testLogin")).willReturn(Optional.of(testUser));
        given(productRepository.findByProductId(1L)).willReturn(Optional.of(spyProduct));
        given(wishlistRepository.save(any(WishList.class))).willThrow(new DataIntegrityViolationException("중복 찜"));

        // when & then
        assertThrows(CartException.class, () ->
                wishlistService.addWishList(testRequest, "testLogin")
        );

        // 실패 시 increaseWishNumber가 호출되지 않았는지 검증
        verify(wishlistRepository, never()).increaseWishCount(1L);
    }
}