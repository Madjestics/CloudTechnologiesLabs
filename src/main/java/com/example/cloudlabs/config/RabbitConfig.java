package com.example.cloudlabs.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableRabbit
@ConditionalOnProperty(prefix = "broker", name = "type", havingValue = "rabbit")
public class RabbitConfig {

    @Value("${rabbitmq.exchange.movie:movies.exchange}")
    private String movieExchange;
    @Value("${rabbitmq.queue.movie:movies.queue}")
    private String movieQueue;
    @Value("${rabbitmq.routing.key.movie:movies.routingKey}")
    private String movieRoutingKey;
    @Value("${rabbitmq.queue.watch:watch.queue}")
    private String watchQueue;
    @Value("${rabbitmq.routing.key.watch:watch.routingKey}")
    private String watchEventRoutingKey;

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jackson2JsonMessageConverter());
        return rabbitTemplate;
    }

    @Bean
    public Jackson2JsonMessageConverter jackson2JsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public DirectExchange movieExchange() {
        return new DirectExchange(movieExchange);
    }

    @Bean
    public Queue movieQueue() {
        return new Queue(movieQueue, true);
    }

    @Bean
    public Binding movieBinding(DirectExchange movieExchange) {
        return BindingBuilder.bind(movieQueue()).to(movieExchange).with(movieRoutingKey);
    }

    @Bean
    public Queue watchQueue() {
        return new Queue(watchQueue, true);
    }

    @Bean
    public Binding watchBinding(DirectExchange movieExchange) {
        return BindingBuilder.bind(watchQueue()).to(movieExchange).with(watchEventRoutingKey);
    }
}
