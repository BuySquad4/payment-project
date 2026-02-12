package com.bootcamp.paymentproject.point.scheduler;

import com.bootcamp.paymentproject.point.service.PointService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PointScheduler {

    private final PointService pointService;

    @Scheduled(cron = "0 0 0 * * *")
    public void processPointBatch() {
        log.info("=== [포인트 배치] 작업 시작 ===");
        try {
            pointService.processPendingPoints();
            pointService.expirePoints();
            log.info("=== [포인트 배치] 작업 성공 ===");
        } catch (Exception e) {
            log.error("!!! [포인트 배치] 작업 실패: {} !!!", e.getMessage());
        }
    }
}