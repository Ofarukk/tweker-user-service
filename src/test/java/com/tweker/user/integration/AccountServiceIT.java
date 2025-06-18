package com.tweker.user.integration;

import com.tweker.user.UserServiceApplication;
import com.tweker.user.dto.AccountDto;
import com.tweker.user.entity.Account;
import com.tweker.user.repository.AccountRepository;
import com.tweker.user.usecase.account.CreateAccountUseCase;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.deser.std.StringDeserializer;
import org.testcontainers.utility.DockerImageName;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Testcontainers
@SpringBootTest(classes = UserServiceApplication.class)
@ActiveProfiles("test")
public class AccountServiceIT {

    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("test-db")
            .withUsername("testuser")
            .withPassword("testpass");

    static KafkaContainer kafka = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.5.0"));

    static {
        postgres.start();
        kafka.start();
    }

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        String r2dbcUrl = "r2dbc:postgresql://" +
                postgres.getHost() + ":" +
                postgres.getMappedPort(5432) + "/" +
                postgres.getDatabaseName();

        registry.add("spring.r2dbc.url", () -> r2dbcUrl);
        registry.add("spring.r2dbc.username", postgres::getUsername);
        registry.add("spring.r2dbc.password", postgres::getPassword);
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
    }

    @Autowired
    CreateAccountUseCase createAccountUseCase;

    @Autowired
    AccountRepository accountRepository;

    @Test
    void saveAccount_shouldPersistToDatabase() {
        UUID userId = UUID.randomUUID();
        AccountDto dto = AccountDto.builder()
                .userId(userId)
                .username("jintest")
                .displayName("Jin Test")
                .avatarUrl("https://example.com/avatar.png")
                .bannerUrl("https://example.com/banner.png")
                .bio("Test bio")
                .location("Test city")
                .website("https://jin.dev")
                .build();

        StepVerifier.create(createAccountUseCase.create(dto))
                .expectNextMatches(saved -> saved.getUserId().equals(userId))
                .verifyComplete();

        StepVerifier.create(accountRepository.findByUserId(userId))
                .expectNextMatches(account ->
                        account.getUsername().equals("jintest") &&
                                account.getDisplayName().equals("Jin Test")
                )
                .verifyComplete();
    }
    @Test
    void saveAccount_shouldProduceKafkaEvent() {
        UUID userId = UUID.randomUUID();
        AccountDto dto = AccountDto.builder()
                .userId(userId)
                .username("jinkafka")
                .displayName("Kafka Test")
                .avatarUrl("https://example.com/avatar.png")
                .bannerUrl("https://example.com/banner.png")
                .bio("Testing Kafka event")
                .location("Kafka City")
                .website("https://kafka.jin")
                .build();

        StepVerifier.create(createAccountUseCase.create(dto))
                .expectNextMatches(saved -> saved.getUserId().equals(userId))
                .verifyComplete();

        // Kafka event kontrolü
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.getBootstrapServers());
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "test-group");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);

        try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props)) {
            consumer.subscribe(Collections.singletonList("user-events"));
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofSeconds(5));

            boolean found = false;
            for (ConsumerRecord<String, String> record : records) {
                if (record.value().contains("jinkafka")) {
                    found = true;
                    break;
                }
            }

            assert found : "Kafka event not found!";
        }
    }
}
