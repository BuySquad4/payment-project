package com.bootcamp.paymentproject.point.service;

import com.bootcamp.paymentproject.point.entity.PointTransaction;
import com.bootcamp.paymentproject.point.enums.PointType;
import com.bootcamp.paymentproject.point.repository.PointTransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class PointService {

    private final PointTransactionRepository pointTransactionRepository;

    public void processPendingPoints() {
        // 엔티티에 earnAt이 없으므로 createdAt 기준으로 처리
        List<PointTransaction> holdings = pointTransactionRepository
                .findAllByTypeAndSwitchToTypeEarnAtBefore(PointType.HOLDING, LocalDateTime.now());

        for (PointTransaction tx : holdings) {
            tx.updateType(PointType.EARN);
            tx.getUser().updatePointBalance(tx.getPoints());
        }
    }

    public void expirePoints() {
        // 필드명 expiredAt에 맞춰 조회
        List<PointTransaction> expiredOnes = pointTransactionRepository
                .findAllByTypeAndExpiredAtBefore(PointType.EARN, LocalDateTime.now());

        for (PointTransaction tx : expiredOnes) {
            tx.updateType(PointType.EXPIRE);
            tx.getUser().updatePointBalance(tx.getPoints().negate());

            // 만료 내역 생성 (필수 필드인 order와 remainingPoints 포함)
            PointTransaction expireHistory = PointTransaction.builder()
                    .user(tx.getUser())
                    .order(tx.getOrder()) // 엔티티 설정상 필수(optional=false)
                    .points(tx.getPoints().negate())
                    .remainingPoints(BigDecimal.ZERO) // 필수 필드 값 채움
                    .type(PointType.EXPIRE)
                    .build();
            pointTransactionRepository.save(expireHistory);
        }
    }
}