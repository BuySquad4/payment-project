package com.bootcamp.paymentproject.portone.client;

import com.bootcamp.paymentproject.common.config.PortOneProperties;
import com.bootcamp.paymentproject.common.exception.ErrorCode;
import com.bootcamp.paymentproject.common.exception.ServiceException;
import com.bootcamp.paymentproject.portone.PortOnePaymentResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
public class PortOneClientImpl implements PortOneClient {

    private final RestTemplate restTemplate;
    private final PortOneProperties portOneProperties;

    @Value("${portone.api.base-url:https://api.portone.io}")
    private String baseUrl;

    @Value("${portone.api.secret:}")
    private String apiSecret;

    /**
     * PortOne 결제 조회 API 호출
     *
     * 흐름:
     * 1) paymentId로 PortOne 결제 조회 API 요청
     * 2) Authorization 헤더에 Secret Key 포함
     * 3) 응답을 PortOnePaymentResponse로 변환하여 반환
     */
    @Override
    public PortOnePaymentResponse getPayment(String paymentId) {

        // 1. 결제 조회 API URL 생성
        String url = baseUrl + "/payments/" + paymentId +
                "?storeId=" + portOneProperties.getStore().getId();

        // 2. Authorization 헤더 설정 (PortOne 전용 방식)
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "PortOne " + apiSecret);

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            // 3. PortOne API 호출 (결제 정보 조회)
            ResponseEntity<PortOnePaymentResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    PortOnePaymentResponse.class
            );

            PortOnePaymentResponse body = response.getBody();

            if (body == null) {
                throw new ServiceException(ErrorCode.PORTONE_RESPONSE_NULL);
            }

            return body;

        } catch (HttpClientErrorException.Unauthorized e) {
            // 401 Unauthorized → 인증 실패(Secret Key 오류)
            throw new ServiceException(ErrorCode.PORTONE_UNAUTHORIZED, e);

        } catch (HttpClientErrorException.NotFound e) {
            // 404 Not Found → 해당 paymentId 없음(결제 정보 없음)
            throw new ServiceException(ErrorCode.PORTONE_PAYMENT_NOT_FOUND, e);

        } catch (HttpClientErrorException e) {
            // 기타 4xx 에러 → PortOne API 요청 오류
            throw new ServiceException(ErrorCode.PORTONE_API_ERROR, e);

        } catch (Exception e) {
            // 네트워크 오류 또는 기타 예외
            throw new ServiceException(ErrorCode.PORTONE_API_ERROR, e);
        }
    }
}
