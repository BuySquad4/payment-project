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
            case PENDING -> targetStatus == APPROVED || targetStatus == FAILED || targetStatus == CANCELED || targetStatus == REFUNDED;
            case APPROVED -> targetStatus == REFUNDED || targetStatus == CANCELED;
            case REFUND_FAILED -> targetStatus == CANCELED || targetStatus == REFUNDED;
            case CANCELED -> targetStatus == REFUNDED || targetStatus == REFUND_FAILED || targetStatus == CANCELED;
            case FAILED, REFUNDED -> false;
        };
    }
}
