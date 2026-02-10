package com.bootcamp.paymentproject.portone;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record PortOneError(
        String type,
        String message
) {
}
