package com.cMall.feedShop.product.application.service;

import com.cMall.feedShop.common.exception.ErrorCode;
import com.cMall.feedShop.product.application.dto.request.ProductUpdateRequest;
import com.cMall.feedShop.product.domain.enums.ImageType;
import com.cMall.feedShop.product.domain.exception.ProductException;
import com.cMall.feedShop.product.domain.model.Category;
import com.cMall.feedShop.product.domain.model.Product;
import com.cMall.feedShop.product.domain.repository.ProductRepository;
import com.cMall.feedShop.store.domain.model.Store;
import com.cMall.feedShop.user.domain.enums.UserRole;
import com.cMall.feedShop.user.domain.model.User;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductUpdateService 테스트")
class ProductUpdateServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductHelper productHelper;

    @Mock
    private ProductImageService productImageService;

    @InjectMocks
    private ProductUpdateService productUpdateService;

    private User sellerUser;
    private Product testProduct;
    private Category testCategory;
    private ProductUpdateRequest updateRequest;
    private List<MultipartFile> mockMainImages;
    private List<MultipartFile> mockDetailImages;

    @BeforeEach
    void setUp() {
        setupTestData();
        setupMockFiles();
    }

    private void setupTestData() {
        sellerUser = new User("seller", "password", "seller@test.com", UserRole.SELLER);
        ReflectionTestUtils.setField(sellerUser, "id", 1L);

        Store testStore = Store.builder()
                .storeName("테스트 스토어")
                .sellerId(1L)
                .build();
        ReflectionTestUtils.setField(testStore, "storeId", 1L);

        testCategory = new Category(null, "테스트 카테고리");
        ReflectionTestUtils.setField(testCategory, "categoryId", 1L);

        testProduct = Product.builder()
                .name("테스트 상품")
                .price(new BigDecimal("10000"))
                .store(testStore)
                .category(testCategory)
                .build();
        ReflectionTestUtils.setField(testProduct, "productId", 1L);

        updateRequest = new ProductUpdateRequest();
        ReflectionTestUtils.setField(updateRequest, "name", "수정된 상품명");
        ReflectionTestUtils.setField(updateRequest, "categoryId", 1L);
    }

    private void setupMockFiles() {
        MockMultipartFile mainImage = new MockMultipartFile(
                "mainImage", "main.jpg", "image/jpeg", "main image content".getBytes());
        MockMultipartFile detailImage = new MockMultipartFile(
                "detailImage", "detail.jpg", "image/jpeg", "detail image content".getBytes());

        mockMainImages = List.of(mainImage);
        mockDetailImages = List.of(detailImage);
    }

    @Test
    @DisplayName("이미지 있는 상품 수정 성공")
    void updateProduct_WithImages_Success() {
        // given
        List<String> uploadedMainUrls = List.of("https://storage/main1.jpg");
        List<String> uploadedDetailUrls = List.of("https://storage/detail1.jpg");

        given(productHelper.getCurrentUser("seller")).willReturn(sellerUser);
        given(productHelper.getProductOwnership(1L, 1L)).willReturn(testProduct);
        given(productHelper.getCategory(1L)).willReturn(testCategory);
        given(productImageService.uploadImagesOnly(mockMainImages, ImageType.MAIN))
                .willReturn(uploadedMainUrls);
        given(productImageService.uploadImagesOnly(mockDetailImages, ImageType.DETAIL))
                .willReturn(uploadedDetailUrls);
        given(productRepository.save(any(Product.class))).willReturn(testProduct);

        // when
        productUpdateService.updateProduct(1L, updateRequest, mockMainImages, mockDetailImages, "seller");

        // then
        verify(productHelper).getCurrentUser("seller");
        verify(productHelper).validateSellerRole(sellerUser);
        verify(productHelper).getProductOwnership(1L, 1L);
        verify(productHelper).getCategory(1L);
        verify(productHelper).validateNameChange(testProduct, updateRequest.getName(), 1L);
        verify(productImageService).uploadImagesOnly(mockMainImages, ImageType.MAIN);
        verify(productImageService).uploadImagesOnly(mockDetailImages, ImageType.DETAIL);
        verify(productHelper).updateBasicInfo(testProduct, updateRequest, testCategory);
        verify(productImageService).replaceImageRecords(testProduct, uploadedMainUrls, ImageType.MAIN);
        verify(productImageService).replaceImageRecords(testProduct, uploadedDetailUrls, ImageType.DETAIL);
        verify(productRepository).save(testProduct);

        // 보상 트랜잭션은 호출되지 않음
        verify(productImageService, never()).deleteUploadedImages(any());
    }

    @Test
    @DisplayName("이미지 업로드 실패 시 예외 발생")
    void updateProduct_ImageUploadFails_ThrowsException() {
        // given
        given(productHelper.getCurrentUser("seller")).willReturn(sellerUser);
        given(productHelper.getProductOwnership(1L, 1L)).willReturn(testProduct);
        given(productHelper.getCategory(1L)).willReturn(testCategory);
        given(productImageService.uploadImagesOnly(mockMainImages, ImageType.MAIN))
                .willThrow(new ProductException(ErrorCode.FILE_UPLOAD_ERROR, "이미지 업로드 실패"));

        // when & then
        assertThatThrownBy(() -> productUpdateService.updateProduct(
                1L, updateRequest, mockMainImages, null, "seller"))
                .isInstanceOf(ProductException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.FILE_UPLOAD_ERROR);

        // DB 작업은 실행되지 않음
        verify(productRepository, never()).save(any());
        // 보상 트랜잭션도 호출되지 않음 (이미지 업로드 자체가 실패했으므로)
        verify(productImageService, never()).deleteUploadedImages(any());
    }

    @Test
    @DisplayName("DB 저장 실패 시 업로드된 이미지 삭제 (보상 트랜잭션)")
    void updateProduct_DbSaveFails_DeletesUploadedImages() {
        // given
        List<String> uploadedUrls = List.of("https://storage/main1.jpg", "https://storage/detail1.jpg");

        given(productHelper.getCurrentUser("seller")).willReturn(sellerUser);
        given(productHelper.getProductOwnership(1L, 1L)).willReturn(testProduct);
        given(productHelper.getCategory(1L)).willReturn(testCategory);
        given(productImageService.uploadImagesOnly(mockMainImages, ImageType.MAIN))
                .willReturn(List.of("https://storage/main1.jpg"));
        given(productImageService.uploadImagesOnly(mockDetailImages, ImageType.DETAIL))
                .willReturn(List.of("https://storage/detail1.jpg"));

        // DB 저장 실패 시뮬레이션
        given(productRepository.save(any(Product.class)))
                .willThrow(new RuntimeException("DB 저장 실패"));

        // when & then
        assertThatThrownBy(() -> productUpdateService.updateProduct(
                1L, updateRequest, mockMainImages, mockDetailImages, "seller"))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("DB 저장 실패");

        // 보상 트랜잭션: 업로드된 이미지들 삭제 확인
        verify(productImageService).deleteUploadedImages(uploadedUrls);
    }

    @Test
    @DisplayName("이미지 없는 상품 수정 성공")
    void updateProduct_WithoutImages_Success() {
        // given
        given(productHelper.getCurrentUser("seller")).willReturn(sellerUser);
        given(productHelper.getProductOwnership(1L, 1L)).willReturn(testProduct);
        given(productHelper.getCategory(1L)).willReturn(testCategory);
        given(productRepository.save(any(Product.class))).willReturn(testProduct);

        // when
        productUpdateService.updateProduct(1L, updateRequest, null, null, "seller");

        // then
        verify(productHelper).getCurrentUser("seller");
        verify(productHelper).validateSellerRole(sellerUser);
        verify(productHelper).getProductOwnership(1L, 1L);
        verify(productHelper).getCategory(1L);
        verify(productHelper).validateNameChange(testProduct, updateRequest.getName(), 1L);
        verify(productHelper).updateBasicInfo(testProduct, updateRequest, testCategory);
        verify(productImageService, never()).uploadImagesOnly(any(), any());
        verify(productImageService, never()).replaceImageRecords(any(), any(), any());
        verify(productImageService, never()).deleteUploadedImages(any());
        verify(productRepository).save(testProduct);
    }
}