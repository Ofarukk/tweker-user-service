package com.tweker.user.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tweker.user.dto.AccountDto;
import com.tweker.user.usecase.account.CreateAccountUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;


import java.util.Map;
import java.util.UUID;

@RequiredArgsConstructor
@Slf4j
@Component
public class UserEventListener {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final CreateAccountUseCase createAccountUseCase;

    @KafkaListener(topics = "user-events", groupId = "user-service-group")
    public void listen(ConsumerRecord<String, String> record) {
        log.info("📥 Received Kafka record: {}", record.value());
        try {
            Map<String, Object> map = objectMapper.readValue(record.value(), Map.class);
            String eventType = (String) map.get("eventType");
            log.debug("🔍 Event type: {}", eventType);
            Map<String, Object> data = (Map<String, Object>) map.get("data");

            if ("UserRegistered".equals(eventType)) {

                UUID userId = UUID.fromString((String) data.get("userId"));
                String username = (String) data.get("username");

                log.info("🧩 Creating Account for userId={} username={}", userId, username);

                AccountDto dto = AccountDto.builder()
                        .userId(userId)
                        .username(username)
                        .displayName(username + " (profile)")
                        .avatarUrl("/assets/avatar/default.jpg")
                        .bannerUrl("/assets/banner/default.jpg")
                        .bio("This is the profile of user @" + username + ".")
                        .location("")
                        .website("")
                        .build();
                log.info("here check the dto: {}", dto);

                createAccountUseCase.create(dto).subscribe(
                        r -> log.info("✅ Account created & event published for: {}", userId),
                        e -> log.error("❌ Failed to create account", e)
                );
            }

        } catch (Exception e) {
            log.error("❌ Failed to parse UserRegistered event", e);
        }
    }
}