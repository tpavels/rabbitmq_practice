package com.tpavels.rabbit.service;

import com.tpavels.rabbit.configuration.RabbitConfiguration;
import com.tpavels.rabbit.event.Message;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MessageService {

    private final RabbitTemplate rabbitTemplate;

    public void publishMessage() {
        Message msg = new Message(
                UUID.randomUUID().toString(),
                LocalDateTime.now()
        );
        rabbitTemplate.setExchange(RabbitConfiguration.TOPIC_EXCHANGE);
        rabbitTemplate.convertAndSend(RabbitConfiguration.ROUTING_KEY, msg);
    }

    public Message receiveMessage() {
        return rabbitTemplate.receiveAndConvert(
                RabbitConfiguration.PING_QUEUE,
                new ParameterizedTypeReference<>() {
                }
        );
    }
}
