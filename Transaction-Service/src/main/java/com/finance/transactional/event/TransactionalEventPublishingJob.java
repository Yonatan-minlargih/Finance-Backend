package com.finance.transactional.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.core.LockAssert;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
@RequiredArgsConstructor
@Slf4j
public class TransactionalEventPublishingJob {

    @Scheduled(cron = "${rabbitmq.publish-transactional-events-job-cron}")
    @SchedulerLock(name = "publishTransactionalEvents")
    public void publish() {
        LockAssert.assertLocked();
        log.info("Transactional event publishing job ran at: {}", Instant.now());
    }
}
