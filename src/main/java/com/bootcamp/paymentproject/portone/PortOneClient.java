package com.bootcamp.paymentproject.portone;

import com.bootcamp.paymentproject.common.properties.PortOneProperties;
import com.bootcamp.paymentproject.portone.exception.PortOneApiException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@RequiredArgsConstructor
public class PortOneClient {

    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final PortOneProperties portOneProperties;

    public PortOnePaymentResponse getPayment(String paymentId) {
        return restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/payments/{paymentId}")
                        .queryParam("storeId",portOneProperties.getStore().getId())
                        .build(paymentId)
                )
                .retrieve()
                .onStatus(HttpStatusCode::isError, (req, res) -> {
                    PortOneError error = parseErrorResponse(res);
                    throw new PortOneApiException(
                            error != null ? error.type() : "UNKNOWN_ERROR",
                            error != null ? error.message() : "Unknown error occurred",
                            res.getStatusCode().value()
                    );
                })
                .body(PortOnePaymentResponse.class);
    }

    public PortOnePaymentResponse cancelPayment(String paymentId, PortOneCancelRequest request) {
        return restClient.post()
                .uri("/payments/{paymentId}/cancel", paymentId)
                .body(request)
                .retrieve()
                .onStatus(HttpStatusCode::isError, (req, res) -> {
                    PortOneError error = parseErrorResponse(res);
                    throw new PortOneApiException(
                            error != null ? error.type() : "UNKNOWN_ERROR",
                            error != null ? error.message() : "Unknown error occurred",
                            res.getStatusCode().value()
                    );
                })
                .body(PortOnePaymentResponse.class);
    }

    private PortOneError parseErrorResponse(org.springframework.http.client.ClientHttpResponse response) {
        try {
            return objectMapper.readValue(response.getBody(), PortOneError.class);
        } catch (Exception e) {
            return null;
        }
    }
}