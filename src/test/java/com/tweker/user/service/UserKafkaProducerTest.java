package com.tweker.user.service;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.context.annotation.Import;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.SenderRecord;
import reactor.kafka.sender.SenderResult;
import reactor.test.StepVerifier;

import java.util.UUID;

import static org.mockito.Mockito.*;


class UserKafkaProducerTest {

    @Test
    void sendUserCreatedEvent_shouldSendKafkaMessage() {
        KafkaSender<String, String> kafkaSender = mock(KafkaSender.class);
        UserKafkaProducer producer = new UserKafkaProducer(kafkaSender);

        UUID userId = UUID.randomUUID();
        String username = "jin";

        @SuppressWarnings("unchecked")
        SenderResult<String> senderResult = mock(SenderResult.class);

        when(kafkaSender.send(any(Mono.class)))
                .thenReturn(Flux.just(senderResult));

        Mono<Void> result = producer.sendUserCreatedEvent(userId, username);

        StepVerifier.create(result)
                .verifyComplete();

        verify(kafkaSender, times(1)).send(any(Mono.class));
    }
}