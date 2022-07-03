package com.tpavels.rabbit.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.support.RetryTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Configuration
@RequiredArgsConstructor
@AutoConfigureAfter(ObjectMapperConfiguration.class)
public class RabbitConfiguration {
    public static final String HOSTNAME = "localhost";
    public static final String TOPIC_EXCHANGE = "tpavels_exchange";
    public static final String FAN_EXCHANGE = "tpavels_exchange_fan";
    public static final String PING_QUEUE = "tpavels_ping_queue";
    public static final String ROUTING_KEY = "message.ping";
    public static final String RABBIT_TEMPLATE_PUBLISHER = "rabbitPublisher";
    public static final String RABBIT_TEMPLATE_PUBLISHER_FAN = "rabbitPublisherFan";
    public static final String RABBIT_TEMPLATE_CONSUMER = "rabbitConsumer";

    private final ObjectMapper objectMapper;

    @Bean
    public Queue queue() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-message-ttl", TimeUnit.MINUTES.toMillis(1));
        return new Queue(PING_QUEUE, false, false, false, args);
    }

    @Bean
    public TopicExchange exchange() {
        return new TopicExchange(TOPIC_EXCHANGE);
    }

    @Bean
    public FanoutExchange exchange2() {
        return new FanoutExchange(FAN_EXCHANGE);
    }

    @Bean
    public Binding pingBinding() {
        return BindingBuilder
                .bind(queue())
                .to(exchange())
                .with(ROUTING_KEY);
    }

    @Bean
    public ConnectionFactory connectionFactory() {
        CachingConnectionFactory factory = new CachingConnectionFactory(HOSTNAME);
        factory.setPublisherConfirmType(CachingConnectionFactory.ConfirmType.SIMPLE);
        factory.getRabbitConnectionFactory().setChannelRpcTimeout((int)TimeUnit.MINUTES.toMillis(1));
        factory.setChannelCheckoutTimeout(1);
        factory.setChannelCacheSize(25);
        factory.getRabbitConnectionFactory().setRequestedChannelMax(25);
        return factory;
    }

    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter(objectMapper);
    }

    @Bean(RABBIT_TEMPLATE_PUBLISHER)
    public RabbitTemplate rabbitTemplatePublisher() {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory());
        rabbitTemplate.setMessageConverter(messageConverter());
        rabbitTemplate.setExchange(TOPIC_EXCHANGE);
        rabbitTemplate.setUsePublisherConnection(true);
        return rabbitTemplate;
    }

    @Bean(RABBIT_TEMPLATE_PUBLISHER_FAN)
    public RabbitTemplate rabbitTemplatePublisherFan() {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory());
        rabbitTemplate.setMessageConverter(messageConverter());
        rabbitTemplate.setExchange(FAN_EXCHANGE);
        rabbitTemplate.setUsePublisherConnection(true);
        return rabbitTemplate;
    }

    @Bean(RABBIT_TEMPLATE_CONSUMER)
    public RabbitTemplate rabbitTemplateConsumer() {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory());
        rabbitTemplate.setMessageConverter(messageConverter());
        rabbitTemplate.setRetryTemplate(retry());
        rabbitTemplate.setExchange(TOPIC_EXCHANGE);
        return rabbitTemplate;
    }

    @Bean
    public RetryTemplate retry() {
        RetryTemplate retryTemplate = new RetryTemplate();
        ExponentialBackOffPolicy backOffPolicy = new ExponentialBackOffPolicy();
        backOffPolicy.setInitialInterval(500);
        backOffPolicy.setMultiplier(10.0);
        backOffPolicy.setMaxInterval(10000);
        retryTemplate.setBackOffPolicy(backOffPolicy);
        return retryTemplate;
    }

}
