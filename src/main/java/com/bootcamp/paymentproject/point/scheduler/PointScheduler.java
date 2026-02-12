package com.bootcamp.paymentproject.point.scheduler;

import com.bootcamp.paymentproject.point.repository.PointTransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PointScheduler {
    private final PointTransactionRepository pointTransactionRepository;

    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void expirePoints(){

    }
}
