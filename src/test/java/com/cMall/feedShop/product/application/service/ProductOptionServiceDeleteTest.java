package com.cMall.feedShop.product.application.service;

import com.cMall.feedShop.common.exception.ErrorCode;
import com.cMall.feedShop.order.domain.repository.OrderItemRepository;
import com.cMall.feedShop.product.domain.exception.ProductException;
import com.cMall.feedShop.product.domain.model.Category;
import com.cMall.feedShop.product.domain.model.Product;
import com.cMall.feedShop.product.domain.model.ProductOption;
import com.cMall.feedShop.product.domain.repository.ProductOptionRepository;
import com.cMall.feedShop.product.domain.enums.*;
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
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

/**
 * ProductOptionService의 상품 옵션 삭제 기능을 테스트하는 클래스
 *
 * 테스트 목표:
 * 1. 정상적인 상품 옵션 삭제 성공 케이스
 * 2. 다양한 실패 케이스들 (권한 없음, 데이터 없음 등)
 *
 * 초등학생도 이해할 수 있도록 자세한 주석을 달았어요!
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("상품 옵션 삭제 서비스 테스트")
class ProductOptionServiceDeleteTest {

    // ========== 가짜 객체들 (실제 데이터베이스 없이 테스트하기 위해) ==========
    @Mock
    private UserRepository userRepository; // 사용자 정보 저장소

    @Mock
    private OrderItemRepository orderItemRepository;

    @Mock
    private ProductOptionRepository productOptionRepository; // 상품 옵션 저장소

    @Mock
    private UserDetails userDetails; // 로그인한 사용자 정보

    // ========== 테스트할 실제 서비스 ==========
    @InjectMocks
    private ProductOptionService productOptionService;

    // ========== 테스트에서 사용할 데이터들 ==========
    private User seller; // 판매자 사용자
    private User normalUser; // 일반 사용자
    private Store store; // 스토어 (가게)
    private Category category; // 상품 카테고리
    private Product product; // 상품
    private ProductOption productOption; // 상품 옵션

    private final String LOGIN_ID = "seller123"; // 로그인 아이디
    private final Long OPTION_ID = 1L; // 상품 옵션 ID

    /**
     * 각 테스트 실행 전에 공통으로 사용할 테스트 데이터를 준비합니다.
     * 이 메서드는 각 @Test 메서드 실행 전에 자동으로 호출됩니다.
     */
    @BeforeEach
    void setUp() {
        // 테스트 데이터 준비
        setupTestData();

        // 로그인 사용자 정보 설정
        setupUserDetails();
    }

    /**
     * 테스트에 필요한 데이터들을 만드는 메서드
     * 판매자, 스토어, 상품, 상품옵션 등을 준비합니다.
     */
    private void setupTestData() {
        // 1. 판매자 사용자 생성
        seller = new User(LOGIN_ID, "password123", "seller@test.com", UserRole.SELLER);
        ReflectionTestUtils.setField(seller, "id", 1L);

        // 2. 일반 사용자 생성 (권한 테스트용)
        normalUser = new User("user123", "password123", "user@test.com", UserRole.USER);
        ReflectionTestUtils.setField(normalUser, "id", 2L);

        // 3. 스토어(가게) 생성
        store = Store.builder()
                .storeName("테스트 스토어")
                .sellerId(1L) // 판매자 ID와 연결
                .build();
        ReflectionTestUtils.setField(store, "storeId", 1L);

        // 4. 카테고리 생성
        category = new Category(CategoryType.SNEAKERS, "운동화");
        ReflectionTestUtils.setField(category, "categoryId", 1L);

        // 5. 상품 생성
        product = Product.builder()
                .name("테스트 신발")
                .price(new BigDecimal("50000"))
                .description("테스트용 신발입니다")
                .discountType(DiscountType.NONE)
                .store(store) // 위에서 만든 스토어와 연결
                .category(category) // 위에서 만든 카테고리와 연결
                .build();
        ReflectionTestUtils.setField(product, "productId", 1L);

        // 6. 상품 옵션 생성
        productOption = new ProductOption(
                Gender.UNISEX, // 성별
                Size.SIZE_250, // 사이즈
                Color.WHITE,   // 색깔
                100,           // 재고 수량
                product        // 위에서 만든 상품과 연결
        );
        ReflectionTestUtils.setField(productOption, "optionId", OPTION_ID);
    }

    /**
     * 로그인한 사용자 정보를 설정하는 메서드
     */
    private void setupUserDetails() {
        // 로그인한 사용자의 아이디를 반환하도록 설정
        given(userDetails.getUsername()).willReturn(LOGIN_ID);
    }

    // ========== 성공 케이스 테스트 ==========

    /**
     * 테스트 1: 정상적으로 상품 옵션을 삭제하는 경우
     *
     * 시나리오:
     * - 판매자가 로그인함
     * - 자신의 스토어에 등록된 상품의 옵션을 삭제함
     * - 성공적으로 삭제됨
     */
    @Test
    @DisplayName("정상적인 상품 옵션 삭제 - 성공")
    void deleteProductOption_Success() {
        // given - 테스트 조건 준비

        // 1. 로그인한 사용자를 찾을 때 판매자를 반환
        given(userRepository.findByLoginId(LOGIN_ID))
                .willReturn(Optional.of(seller));

        // 2. 상품 옵션을 찾을 때 위에서 만든 옵션을 반환
        given(productOptionRepository.findByOptionId(OPTION_ID))
                .willReturn(Optional.of(productOption));

        // when - 실제 테스트 실행
        productOptionService.deleteProductOption(OPTION_ID, userDetails);

        // then - 결과 검증
        // 1. 사용자를 제대로 찾았는지 확인
        verify(userRepository, times(1)).findByLoginId(LOGIN_ID);

        // 2. 상품 옵션을 제대로 찾았는지 확인
        verify(productOptionRepository, times(1)).findByOptionId(OPTION_ID);

        // 3. 상품 옵션이 실제로 삭제되었는지 확인
        verify(productOptionRepository, times(1)).delete(productOption);
    }

    // ========== 실패 케이스 테스트들 ==========

    /**
     * 테스트 2: 존재하지 않는 사용자인 경우
     *
     * 시나리오:
     * - 잘못된 로그인 ID로 요청
     * - 사용자를 찾을 수 없어서 실패
     */
    @Test
    @DisplayName("존재하지 않는 사용자 - 실패")
    void deleteProductOption_UserNotFound() {
        // given - 사용자를 찾을 수 없도록 설정
        given(userRepository.findByLoginId(LOGIN_ID))
                .willReturn(Optional.empty());

        // when & then - 예외가 발생하는지 확인
        ProductException exception = assertThrows(
                ProductException.class,
                () -> productOptionService.deleteProductOption(OPTION_ID, userDetails)
        );

        // 올바른 에러 코드가 나오는지 확인
        assert(exception.getErrorCode().equals(ErrorCode.USER_NOT_FOUND));

        // 상품 옵션 삭제가 호출되지 않았는지 확인
        verify(productOptionRepository, never()).delete(any());
    }

    /**
     * 테스트 3: 판매자가 아닌 일반 사용자인 경우
     *
     * 시나리오:
     * - 일반 사용자가 상품 옵션 삭제 시도
     * - 권한이 없어서 실패
     */
    @Test
    @DisplayName("판매자 권한 없음 - 실패")
    void deleteProductOption_NotSeller() {
        // given - 일반 사용자를 반환하도록 설정
        given(userRepository.findByLoginId(LOGIN_ID))
                .willReturn(Optional.of(normalUser));

        // when & then - 권한 예외가 발생하는지 확인
        ProductException exception = assertThrows(
                ProductException.class,
                () -> productOptionService.deleteProductOption(OPTION_ID, userDetails)
        );

        // 권한 오류 코드가 나오는지 확인
        assert(exception.getErrorCode().equals(ErrorCode.FORBIDDEN));

        // 상품 옵션 조회 및 삭제가 호출되지 않았는지 확인
        verify(productOptionRepository, never()).findByOptionId(any());
        verify(productOptionRepository, never()).delete(any());
    }

    /**
     * 테스트 4: 존재하지 않는 상품 옵션인 경우
     *
     * 시나리오:
     * - 판매자가 존재하지 않는 옵션 ID로 삭제 시도
     * - 옵션을 찾을 수 없어서 실패
     */
    @Test
    @DisplayName("존재하지 않는 상품 옵션 - 실패")
    void deleteProductOption_OptionNotFound() {
        // given
        // 1. 판매자 사용자는 정상적으로 찾아짐
        given(userRepository.findByLoginId(LOGIN_ID))
                .willReturn(Optional.of(seller));

        // 2. 상품 옵션은 찾을 수 없음
        given(productOptionRepository.findByOptionId(OPTION_ID))
                .willReturn(Optional.empty());

        // when & then - 상품 옵션을 찾을 수 없다는 예외가 발생하는지 확인
        ProductException exception = assertThrows(
                ProductException.class,
                () -> productOptionService.deleteProductOption(OPTION_ID, userDetails)
        );

        // 올바른 에러 코드가 나오는지 확인
        assert(exception.getErrorCode().equals(ErrorCode.PRODUCT_OPTION_NOT_FOUND));

        // 삭제가 호출되지 않았는지 확인
        verify(productOptionRepository, never()).delete(any());
    }

    /**
     * 테스트 5: 다른 판매자의 상품 옵션을 삭제하려는 경우
     *
     * 시나리오:
     * - 판매자 A가 판매자 B의 상품 옵션을 삭제 시도
     * - 소유권이 없어서 실패
     */
    @Test
    @DisplayName("상품 옵션 소유권 없음 - 실패")
    void deleteProductOption_NoPermission() {
        // given
        // 1. 다른 판매자 생성 (ID가 다름)
        User otherSeller = new User("other_seller", "password", "other@test.com", UserRole.SELLER);
        ReflectionTestUtils.setField(otherSeller, "id", 999L); // 다른 ID

        // 2. 현재 로그인한 사용자는 다른 판매자
        given(userRepository.findByLoginId(LOGIN_ID))
                .willReturn(Optional.of(otherSeller));

        // 3. 상품 옵션은 원래 판매자(ID=1L)의 것
        given(productOptionRepository.findByOptionId(OPTION_ID))
                .willReturn(Optional.of(productOption));

        // when & then - 권한 예외가 발생하는지 확인
        ProductException exception = assertThrows(
                ProductException.class,
                () -> productOptionService.deleteProductOption(OPTION_ID, userDetails)
        );

        // 권한 오류 코드와 메시지가 올바른지 확인
        assert(exception.getErrorCode().equals(ErrorCode.FORBIDDEN));
        assert(exception.getMessage().contains("해당 상품 옵션에 대한 권한이 없습니다"));

        // 삭제가 호출되지 않았는지 확인
        verify(productOptionRepository, never()).delete(any());
    }

    // ========== 경계값 테스트 ==========

    /**
     * 테스트 6: null 값 처리 테스트
     *
     * 시나리오:
     * - null 옵션 ID나 null 사용자 정보로 요청
     * - 적절하게 예외 처리되는지 확인
     */
    @Test
    @DisplayName("null 옵션 ID - 실패")
    void deleteProductOption_NullOptionId() {
        // given - 판매자는 정상적으로 찾아짐
        given(userRepository.findByLoginId(LOGIN_ID))
                .willReturn(Optional.of(seller));

        // when & then - null ID로 요청했을 때 예외 발생
        assertThrows(
                Exception.class, // ProductException 또는 다른 예외
                () -> productOptionService.deleteProductOption(null, userDetails)
        );

        // 삭제가 호출되지 않았는지 확인
        verify(productOptionRepository, never()).delete(any());
    }
}