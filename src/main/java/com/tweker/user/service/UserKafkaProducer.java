package com.tweker.user.service;

import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.SenderRecord;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserKafkaProducer {

    private final KafkaSender<String, String> kafkaSender;

    public Mono<Void> sendUserCreatedEvent(UUID userId, String username) {
        String eventJson = String.format("""
        {
          "eventType": "USER_CREATED",
          "timestamp": "%s",
          "data": {
            "userId": "%s",
            "username": "%s"
          }
        }
        """, Instant.now(), userId, username);

        ProducerRecord<String, String> producerRecord = new ProducerRecord<>("user-events", null, eventJson);

        SenderRecord<String, String, String> senderRecord = SenderRecord.create(producerRecord, null);

        return kafkaSender.send(Mono.just(senderRecord))
                .doOnNext(result -> System.out.println("✅ Kafka event sent: " + eventJson))
                .then();
    }
    public Mono<Void> sendUserUpdatedEvent(UUID userId, String username, String avatarUrl) {
        String eventJson = String.format("""
    {
      "eventType": "USER_UPDATED",
      "timestamp": "%s",
      "data": {
        "userId": "%s",
        "username": "%s",
        "avatarUrl": "%s"
      }
    }
    """, Instant.now(), userId, username, avatarUrl == null ? "" : avatarUrl);

        ProducerRecord<String, String> record = new ProducerRecord<>("user-events", null, eventJson);
        SenderRecord<String, String, String> senderRecord = SenderRecord.create(record, null);

        return kafkaSender.send(Mono.just(senderRecord))
                .doOnNext(result -> System.out.println("✅ Kafka USER_UPDATED sent"))
                .then();
    }

    public Mono<Void> sendUserFollowedEvent(UUID followerId, UUID followedId) {
        String eventJson = String.format("""
    {
      "eventType": "USER_FOLLOWED",
      "timestamp": "%s",
      "data": {
        "followerId": "%s",
        "followedId": "%s"
      }
    }
    """, Instant.now(), followerId, followedId);

        ProducerRecord<String, String> record = new ProducerRecord<>("user-events", null, eventJson);
        SenderRecord<String, String, String> senderRecord = SenderRecord.create(record, null);
        return kafkaSender.send(Mono.just(senderRecord)).then();
    }

    public Mono<Void> sendUserUnfollowedEvent(UUID followerId, UUID followedId) {
        String eventJson = String.format("""
    {
      "eventType": "USER_UNFOLLOWED",
      "timestamp": "%s",
      "data": {
        "followerId": "%s",
        "followedId": "%s"
      }
    }
    """, Instant.now(), followerId, followedId);

        ProducerRecord<String, String> record = new ProducerRecord<>("user-events", null, eventJson);
        SenderRecord<String, String, String> senderRecord = SenderRecord.create(record, null);
        return kafkaSender.send(Mono.just(senderRecord)).then();
    }
}
