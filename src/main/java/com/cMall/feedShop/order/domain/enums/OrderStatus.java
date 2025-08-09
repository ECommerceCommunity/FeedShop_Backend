package com.cMall.feedShop.order.domain.enums;

/**
 * 주문 상태를 나타내는 열거형 클래스입니다.
 * 각 상태에 따라 상태 변경 가능 여부를 정의합니다.
 */
public enum OrderStatus {
    // 주문됨
    ORDERED {
        // 판매자는 주문됨 상태에서
        // 배송 중, 취소로 변경 가능하다.
        @Override
        public boolean canChangeTo(OrderStatus newStatus) {
            return newStatus == SHIPPED || newStatus == CANCELLED;
        }

        // 유저는 주문됨 상태에서
        // 취소로만 변경 가능하다.
        @Override
        public boolean canUserChangeTo(OrderStatus newStatus) {
            return newStatus == CANCELLED;
        }
    },
    // 배송중
    SHIPPED {
        // 판매자는 배송 중 상태에서
        // 배송 완료로 변경 가능하다.
        @Override
        public boolean canChangeTo(OrderStatus newStatus) {
            return newStatus == DELIVERED;
        }

        // 유저는 배송 중 상태에서
        // 변경할 수 없다.
        @Override
        public boolean canUserChangeTo(OrderStatus newStatus) {
            return false;
        }
    },
    // 배송완료
    DELIVERED {
        // 판매자는 배송 완료 상태에서
        // 반품으로 변경 가능하다.
        @Override
        public boolean canChangeTo(OrderStatus newStatus) {
            return newStatus == RETURNED;
        }

        // 유저는 배송 완료 상태에서
        // 반품으로 변경 가능하다.
        @Override
        public boolean canUserChangeTo(OrderStatus newStatus) {
            return newStatus == RETURNED;
        }
    },
    // 취소됨
    CANCELLED {
        // 판매자는 취소된 상태에서
        // 변경할 수 없다.
        @Override
        public boolean canChangeTo(OrderStatus newStatus) {
            return false;
        }

        // 유저는 취소된 상태에서
        // 변경할 수 없다.
        @Override
        public boolean canUserChangeTo(OrderStatus newStatus) {
            return false;
        }
    },
    // 반품됨
    RETURNED {
        // 판매자는 반품된 상태에서
        // 변경할 수 없다.
        @Override
        public boolean canChangeTo(OrderStatus newStatus) {
            return false;
        }

        // 유저는 반품된 상태에서
        // 변경할 수 없다.
        @Override
        public boolean canUserChangeTo(OrderStatus newStatus) {
            return false;
        }
    };

    // 판매자가 주문 상태 변경 가능 여부를 정의하는 메소드
    public abstract boolean canChangeTo(OrderStatus newStatus);

    // 유저가 주문 상태 변경 가능 여부를 정의하는 메소드
    public abstract boolean canUserChangeTo(OrderStatus newStatus);
}
