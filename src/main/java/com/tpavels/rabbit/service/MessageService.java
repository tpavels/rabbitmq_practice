package com.tpavels.rabbit.service;

import com.tpavels.rabbit.configuration.RabbitConfiguration;
import com.tpavels.rabbit.event.Message;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static com.tpavels.rabbit.configuration.RabbitConfiguration.*;

@Service
@Slf4j
public class MessageService {

    private final RabbitTemplate rabbitTemplatePublisher;
    private final RabbitTemplate rabbitTemplatePublisherFan;
    private final RabbitTemplate rabbitTemplateConsumer;

    public MessageService(
            @Qualifier(RABBIT_TEMPLATE_PUBLISHER) RabbitTemplate rabbitTemplatePublisher,
            @Qualifier(RABBIT_TEMPLATE_PUBLISHER_FAN) RabbitTemplate rabbitTemplatePublisherFan,
            @Qualifier(RABBIT_TEMPLATE_CONSUMER) RabbitTemplate rabbitTemplateConsumer
    ) {
        this.rabbitTemplatePublisher = rabbitTemplatePublisher;
        this.rabbitTemplateConsumer = rabbitTemplateConsumer;
        this.rabbitTemplatePublisherFan = rabbitTemplatePublisherFan;
    }

    public void publishMessage() {
        Message msg = new Message(
                UUID.randomUUID().toString(),
                LocalDateTime.now()
        );
//
//        int unconfirmedCount = rabbitTemplatePublisher.getUnconfirmedCount();
//        log.info("unconfirmedCnt = {}", unconfirmedCount);
//        Collection<CorrelationData> unconfirmed = rabbitTemplatePublisher.getUnconfirmed(5000);
//        int numUnconfirmed = 0;
//        if (unconfirmed != null ) {
//            numUnconfirmed = unconfirmed.size();
//            log.info("unconfirmed = {}", numUnconfirmed);
//            for (CorrelationData u :unconfirmed) {
//                log.info("corr = {}", u);
//            }
//        }
//        rabbitTemplatePublisher.setConfirmCallback((data, ack, c) -> {
//            log.info("data = {}" , data);
//            log.info("ack = {}" , ack);
//            log.info("c = {}" , c);
//        });
        rabbitTemplatePublisher.invoke(t -> {
            rabbitTemplatePublisher.convertAndSend(RabbitConfiguration.ROUTING_KEY, msg);
            t.waitForConfirmsOrDie(TimeUnit.SECONDS.toMillis(60));
            return true;
        });
    }

    public Message receiveMessage() {
        return rabbitTemplateConsumer.receiveAndConvert(
                RabbitConfiguration.PING_QUEUE,
                new ParameterizedTypeReference<>() {
                }
        );
    }
}
