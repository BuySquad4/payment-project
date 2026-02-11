package com.bootcamp.paymentproject.portone;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.beans.factory.annotation.Value;

import java.math.BigDecimal;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public record PortOneCancelRequest(

        String storeId,
        String reason,
        BigDecimal amount
) {
    public static PortOneCancelRequest fullCancel(String storeId ,String reason) {
        return new PortOneCancelRequest(storeId ,reason, null);
    }
}
