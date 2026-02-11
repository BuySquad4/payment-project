package com.bootcamp.paymentproject.refund.enums;

import com.bootcamp.paymentproject.payment.enums.PaymentStatus;
import lombok.Getter;

@Getter
public enum RefundState {
    REQUESTED,
    FAILED,
    COMPLETED;

    public boolean canTransitToTargetState(RefundState targetState){
        if(targetState == null){
            return false;
        }

        return switch (this){
            case REQUESTED -> targetState == FAILED || targetState == COMPLETED;
            case FAILED -> targetState == REQUESTED;
            case COMPLETED -> false;
        };

    }
}
