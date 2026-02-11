package com.bootcamp.paymentproject.payment.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

public enum PaymentStatus {
    PENDING,
    APPROVED,
    FAILED,
    CANCELED,
    REFUND_FAILED,
    REFUNDED;

    public boolean canTransitToTargetStatus(PaymentStatus targetStatus){
        if(targetStatus == null){
            return false;
        }

        return switch (this){
            case PENDING -> targetStatus == APPROVED || targetStatus == FAILED || targetStatus == CANCELED;
            case APPROVED, REFUND_FAILED -> targetStatus == CANCELED;
            case CANCELED -> targetStatus == REFUNDED || targetStatus == REFUND_FAILED;
            case FAILED, REFUNDED -> false;
        };

    }
}
