package com.bootcamp.paymentproject.payment.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

public enum PaymentStatus {
    PENDING,
    APPROVED,
    FAILED,
    REFUNDED;

    public boolean canTransitToTargetStatus(PaymentStatus targetStatus){
        if(targetStatus == null){
            return false;
        }

        return switch (this){
            case PENDING -> targetStatus == APPROVED || targetStatus == FAILED || targetStatus == REFUNDED;
            case APPROVED -> targetStatus == REFUNDED;
            case FAILED, REFUNDED -> false;
        };

    }
}
