package com.tweker.user.integration;

import com.tweker.user.entity.Account;
import com.tweker.user.repository.AccountRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import reactor.test.StepVerifier;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@DataR2dbcTest
public class AccountRepositoryIT {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("test-db")
            .withUsername("testuser")
            .withPassword("testpass");

    @Autowired
    private AccountRepository accountRepository; //

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        String r2dbcUrl = String.format("r2dbc:postgresql://%s:%d/%s",
                postgres.getHost(),
                postgres.getMappedPort(5432),
                postgres.getDatabaseName());

        registry.add("spring.r2dbc.url", () -> r2dbcUrl);
        registry.add("spring.r2dbc.username", postgres::getUsername);
        registry.add("spring.r2dbc.password", postgres::getPassword);

        // Bu test sadece DB'ye odaklandığı için Kafka'ya gerek yok.
    }

    @Test
    void findByUsername_whenAccountExists_shouldReturnAccount() {
        // Arrange
        UUID userId = UUID.randomUUID();
        Account accountToSave = Account.builder() //
                .userId(userId)
                .username("repo-test-user")
                .displayName("Repo Test")
                .build();

        // Önce veriyi kaydet
        accountRepository.save(accountToSave).block(); // block() kullanarak test verisinin yazılmasını bekle

        // Act
        // Kaydedilen kullanıcıyı username ile bul
        var foundAccountMono = accountRepository.findByUsername("repo-test-user"); //

        // Assert
        StepVerifier.create(foundAccountMono)
                .assertNext(account -> {
                    assertThat(account.getUserId()).isEqualTo(userId);
                    assertThat(account.getUsername()).isEqualTo("repo-test-user");
                    assertThat(account.getId()).isNotNull();
                })
                .verifyComplete();
    }

    @Test
    void findByUsername_whenAccountDoesNotExist_shouldReturnEmpty() {
        // Act
        var notFoundMono = accountRepository.findByUsername("non-existent-repo-user"); //

        // Assert
        StepVerifier.create(notFoundMono)
                .verifyComplete();
    }
}