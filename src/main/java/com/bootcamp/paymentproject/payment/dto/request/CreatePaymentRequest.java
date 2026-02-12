package com.bootcamp.paymentproject.payment.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class CreatePaymentRequest {
    private Long orderId;
    private BigDecimal totalAmount;
    private BigDecimal pointsToUse;

    public void checkPointsToUseNull(){
        if(this.pointsToUse == null){
            this.pointsToUse = BigDecimal.ZERO;
        }
    }
}
