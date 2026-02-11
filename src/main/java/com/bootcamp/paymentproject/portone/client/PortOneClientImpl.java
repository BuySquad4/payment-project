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

    @Override
    public PortOnePaymentResponse getPayment(String paymentId) {

        // 결제 조회 API URL 생성
        String url = baseUrl + "/payments/" + paymentId +
                "?storeId=" + portOneProperties.getStore().getId();

        // Authorization 헤더 설정 (PortOne 전용 방식)
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "PortOne " + apiSecret);

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            // PortOne 결제 조회 요청 (SSOT)
            ResponseEntity<PortOnePaymentResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    PortOnePaymentResponse.class
            );

            // 응답 body 추출 및 null 체크
            PortOnePaymentResponse body = response.getBody();
            if (body == null) {
                // 응답이 비어있는 경우 → 시스템 예외로 변환
                throw new ServiceException(ErrorCode.PORTONE_RESPONSE_NULL);
            }

            return body;

        } catch (HttpClientErrorException.Unauthorized e) {
            // 401 Unauthorized → 인증 실패
            throw new ServiceException(ErrorCode.PORTONE_UNAUTHORIZED, e);

        } catch (HttpClientErrorException.NotFound e) {
            // 404 Not Found → 해당 paymentId 없음
            throw new ServiceException(ErrorCode.PORTONE_PAYMENT_NOT_FOUND, e);

        } catch (HttpClientErrorException e) {
            // 기타 4xx 에러 → PortOne API 요청 오류
            throw new ServiceException(ErrorCode.PORTONE_API_ERROR, e);

        } catch (Exception e) {
            // 네트워크 오류, JSON 파싱 오류 등 예상 못한 예외
            throw new ServiceException(ErrorCode.PORTONE_API_ERROR, e);
        }
    }
}
