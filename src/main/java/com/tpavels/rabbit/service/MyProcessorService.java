package com.tpavels.rabbit.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class MyProcessorService {

    private final MessageService messageService;

    public void startPublishing() {
        ExecutorService executorService = Executors.newFixedThreadPool(4);
        executorService.submit(publish());
    }

    private Runnable publish() {
        return () -> {
            while (true) {
                messageService.publishMessage();
                log.info("msg sent");
                try {
                    Thread.sleep(TimeUnit.SECONDS.toMillis(5));
                } catch (InterruptedException e) {
                    log.info("interrupted wait", e);
                }
            }
        };
    }

    public void startConsuming() {
        ExecutorService executorService = Executors.newFixedThreadPool(4);
        executorService.submit(consumer());
    }

    private Runnable consumer() {
        return () -> {
            while (true) {
                messageService.receiveMessage();
                log.info("msg received");
                try {
                    Thread.sleep(TimeUnit.SECONDS.toMillis(7));
                } catch (InterruptedException e) {
                    log.info("interrupted wait", e);
                }
            }
        };
    }
}
