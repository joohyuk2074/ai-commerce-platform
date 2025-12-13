package com.spartaecommerce.refund.domain.entity;

import com.spartaecommerce.common.exception.BusinessException;
import com.spartaecommerce.common.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Refund 도메인 엔티티")
class RefundTest {

    @Nested
    @DisplayName("환불 생성 시")
    class CreateNew {

        @Test
        @DisplayName("유효한 정보로 환불을 생성한다")
        void createNew_WithValidData_CreatesRefund() {
            // given
            Long userId = 1L;
            Long orderId = 100L;
            String reason = "상품 불량";

            // when
            Refund refund = Refund.createNew(userId, orderId, reason);

            // then
            assertThat(refund.getUserId()).isEqualTo(userId);
            assertThat(refund.getOrderId()).isEqualTo(orderId);
            assertThat(refund.getReason()).isEqualTo(reason);
            assertThat(refund.getStatus()).isEqualTo(RefundStatus.PENDING);
        }
    }

    @Nested
    @DisplayName("환불 승인 시")
    class Approve {

        @Test
        @DisplayName("대기 상태의 환불을 승인한다")
        void approve_PendingRefund_ApprovesSuccessfully() {
            // given
            Refund refund = createPendingRefund();
            assertThat(refund.getStatus()).isEqualTo(RefundStatus.PENDING);

            // when
            refund.approve();

            // then
            assertThat(refund.getStatus()).isEqualTo(RefundStatus.APPROVED);
        }

        @Test
        @DisplayName("이미 승인된 환불은 다시 승인할 수 없다")
        void approve_AlreadyApprovedRefund_ThrowsException() {
            // given
            Refund refund = createPendingRefund();
            refund.approve();

            // when & then
            assertThatThrownBy(() -> refund.approve())
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> {
                    BusinessException businessException = (BusinessException) ex;
                    assertThat(businessException.getErrorCode()).isEqualTo(ErrorCode.REFUND_INVALID_STATE_TRANSITION);
                    assertThat(businessException.getMessage())
                        .contains("APPROVED -> APPROVED");
                });
        }

        @Test
        @DisplayName("거부된 환불은 승인할 수 없다")
        void approve_RejectedRefund_ThrowsException() {
            // given
            Refund refund = createPendingRefund();
            refund.reject();

            // when & then
            assertThatThrownBy(() -> refund.approve())
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> {
                    BusinessException businessException = (BusinessException) ex;
                    assertThat(businessException.getErrorCode()).isEqualTo(ErrorCode.REFUND_INVALID_STATE_TRANSITION);
                    assertThat(businessException.getMessage())
                        .contains("REJECTED -> APPROVED");
                });
        }
    }

    @Nested
    @DisplayName("환불 거부 시")
    class Reject {

        @Test
        @DisplayName("대기 상태의 환불을 거부한다")
        void reject_PendingRefund_RejectsSuccessfully() {
            // given
            Refund refund = createPendingRefund();
            assertThat(refund.getStatus()).isEqualTo(RefundStatus.PENDING);

            // when
            refund.reject();

            // then
            assertThat(refund.getStatus()).isEqualTo(RefundStatus.REJECTED);
        }

        @Test
        @DisplayName("이미 거부된 환불은 다시 거부할 수 없다")
        void reject_AlreadyRejectedRefund_ThrowsException() {
            // given
            Refund refund = createPendingRefund();
            refund.reject();

            // when & then
            assertThatThrownBy(() -> refund.reject())
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> {
                    BusinessException businessException = (BusinessException) ex;
                    assertThat(businessException.getErrorCode()).isEqualTo(ErrorCode.REFUND_INVALID_STATE_TRANSITION);
                    assertThat(businessException.getMessage())
                        .contains("REJECTED -> REJECTED");
                });
        }

        @Test
        @DisplayName("승인된 환불은 거부할 수 없다")
        void reject_ApprovedRefund_ThrowsException() {
            // given
            Refund refund = createPendingRefund();
            refund.approve();

            // when & then
            assertThatThrownBy(() -> refund.reject())
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> {
                    BusinessException businessException = (BusinessException) ex;
                    assertThat(businessException.getErrorCode()).isEqualTo(ErrorCode.REFUND_INVALID_STATE_TRANSITION);
                    assertThat(businessException.getMessage())
                        .contains("APPROVED -> REJECTED");
                });
        }
    }

    private Refund createPendingRefund() {
        return Refund.builder()
            .refundId(1L)
            .userId(1L)
            .orderId(100L)
            .reason("상품 불량")
            .status(RefundStatus.PENDING)
            .build();
    }
}
