package com.bootcamp.paymentproject.payment.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

public enum PaymentStatus {
    PENDING,
    APPROVED,
    FAILED,
    CANCELED;

    public boolean canTransitToTargetStatus(PaymentStatus targetStatus){
        if(targetStatus == null){
            return false;
        }

        return switch (this){
            case PENDING -> targetStatus == APPROVED || targetStatus == FAILED || targetStatus == CANCELED;
            case APPROVED -> targetStatus == CANCELED;
            case FAILED, CANCELED -> false;
        };

    }
}
