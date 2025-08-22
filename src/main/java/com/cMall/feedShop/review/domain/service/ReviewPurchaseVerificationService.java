package com.cMall.feedShop.review.domain.service;

import com.cMall.feedShop.order.application.dto.response.info.PurchasedItemInfo;
import com.cMall.feedShop.order.application.service.PurchasedItemService;
import com.cMall.feedShop.review.domain.exception.ReviewPurchaseRequiredException;
import com.cMall.feedShop.user.domain.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewPurchaseVerificationService {

    private final PurchasedItemService purchasedItemService;

    /**
     * 사용자가 특정 상품을 구매했는지 검증
     * @param user 검증할 사용자
     * @param productId 상품 ID
     * @throws ReviewPurchaseRequiredException 구매하지 않은 상품에 리뷰 작성 시
     */
    public void validateUserPurchasedProduct(User user, Long productId) {
        log.info("구매이력 검증 시작: userId={}, productId={}", user.getId(), productId);
        
        try {
            // 사용자의 구매 상품 목록 조회
            var purchasedItemResponse = purchasedItemService.getPurchasedItems(user.getLoginId());
            
            // Null 체크
            if (purchasedItemResponse == null) {
                log.warn("구매 상품 목록 조회 결과가 null: userId={}", user.getId());
                throw new ReviewPurchaseRequiredException(
                        "구매이력을 확인할 수 없습니다. 잠시 후 다시 시도해주세요.");
            }
            
            List<PurchasedItemInfo> purchasedItems = purchasedItemResponse.getItems();
            
            // items가 null인 경우 빈 리스트로 처리
            if (purchasedItems == null) {
                log.warn("구매 상품 목록이 null: userId={}", user.getId());
                purchasedItems = List.of();
            }
            
            // 해당 상품을 구매했는지 확인
            boolean hasPurchased = purchasedItems.stream()
                    .anyMatch(item -> item != null && item.getProductId() != null && 
                             item.getProductId().equals(productId));
            
            if (!hasPurchased) {
                log.warn("구매하지 않은 상품에 리뷰 작성 시도: userId={}, productId={}", 
                        user.getId(), productId);
                throw new ReviewPurchaseRequiredException(
                        "구매한 상품에만 리뷰를 작성할 수 있습니다.");
            }
            
            log.info("구매이력 검증 성공: userId={}, productId={}", user.getId(), productId);
            
        } catch (ReviewPurchaseRequiredException e) {
            // 이미 처리된 예외는 그대로 던짐
            throw e;
        } catch (com.cMall.feedShop.user.domain.exception.UserException e) {
            // 사용자 관련 예외 (예: USER_NOT_FOUND)
            log.warn("사용자 조회 실패: userId={}, loginId={}", user.getId(), user.getLoginId(), e);
            throw new ReviewPurchaseRequiredException(
                    "사용자 정보를 확인할 수 없습니다.", e);
        } catch (org.springframework.dao.DataAccessException e) {
            // 데이터베이스 관련 예외
            log.error("데이터베이스 오류 발생: userId={}, productId={}", 
                    user.getId(), productId, e);
            throw new ReviewPurchaseRequiredException(
                    "구매이력 조회 중 시스템 오류가 발생했습니다. 잠시 후 다시 시도해주세요.", e);
        } catch (Exception e) {
            // 예상치 못한 예외만 여기서 처리 (최후의 방어선)
            log.error("예상치 못한 오류 발생: userId={}, productId={}", 
                    user.getId(), productId, e);
            throw new ReviewPurchaseRequiredException(
                    "구매이력 확인 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.", e);
        }
    }
}