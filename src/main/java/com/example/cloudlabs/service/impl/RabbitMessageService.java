package com.example.cloudlabs.service.impl;

import com.example.cloudlabs.dto.MovieEvent;
import com.example.cloudlabs.dto.WatchEvent;
import com.example.cloudlabs.service.MessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "broker", name = "type", havingValue = "rabbit")
public class RabbitMessageService implements MessageService {

    private final RabbitTemplate rabbitTemplate;
    @Value("${rabbitmq.exchange.movie:movies.exchange}")
    private String movieExchange;
    @Value("${rabbitmq.routing.key.movie:movies.routingKey}")
    private String movieEventRoutingKey;
    @Value("${rabbitmq.routing.key.watch:watch.routingKey}")
    private String watchEventRoutingKey;

    @Override
    public void sendMovieEvent(MovieEvent movieEvent) {
        rabbitTemplate.convertAndSend(movieExchange, movieEventRoutingKey, movieEvent);
        log.info("В RabbitMQ отправлено сообщение о фильме: " + movieEvent);
    }

    @Override
    public void sendWatchEvent(WatchEvent watchEvent) {
        rabbitTemplate.convertAndSend(movieExchange, watchEventRoutingKey, watchEvent);
        log.info(String.format("В RabbitMQ отправлено сообщение о просмотре фильма %s пользователем %s", watchEvent.getMovie(), watchEvent.getUserId()));
    }
}
