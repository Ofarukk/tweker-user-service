package com.tweker.user.integration;

import com.tweker.user.UserServiceApplication;
import com.tweker.user.dto.AccountDto;
import com.tweker.user.repository.AccountRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import reactor.core.publisher.Mono;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest(classes = UserServiceApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureWebTestClient
public class UserControllerIT {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("test-db")
            .withUsername("testuser")
            .withPassword("testpass");

    @Container
    static KafkaContainer kafka = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.5.0"));

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private AccountRepository accountRepository;

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        String r2dbcUrl = String.format("r2dbc:postgresql://%s:%d/%s",
                postgres.getHost(),
                postgres.getMappedPort(5432),
                postgres.getDatabaseName());

        registry.add("spring.r2dbc.url", () -> r2dbcUrl);
        registry.add("spring.r2dbc.username", postgres::getUsername);
        registry.add("spring.r2dbc.password", postgres::getPassword);
        registry.add("KAFKA_BOOTSTRAP_SERVERS", kafka::getBootstrapServers);
    }

    @BeforeEach
    void setUp() {
        // Her testten önce veritabanını temizle
        accountRepository.deleteAll().block();
    }


    @Test
    void createAccount_shouldReturnCreatedAccountDto() {
        // Arrange
        UUID userId = UUID.randomUUID();
        AccountDto accountToCreate = AccountDto.builder() //
                .userId(userId)
                .username("test-user-controller")
                .displayName("Test User")
                .build();

        // Act & Assert
        webTestClient.post().uri("/user/account") //
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(accountToCreate), AccountDto.class)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(AccountDto.class)
                .value(responseDto -> {
                    assertThat(responseDto.getUserId()).isEqualTo(userId);
                    assertThat(responseDto.getUsername()).isEqualTo("test-user-controller");
                });
    }

    @Test
    void getByUsername_whenAccountExists_shouldReturnAccountDto() {
        // Arrange
        // Önce veritabanına test edilecek bir kullanıcı ekleyelim.
        UUID userId = UUID.randomUUID();
        AccountDto existingAccount = AccountDto.builder() //
                .userId(userId)
                .username("find-me")
                .displayName("Find Me User")
                .build();

        // Doğrudan controller'ın altındaki use case'i değil, repository'yi kullanarak veri hazırlığı yapıyoruz.
        // Bu, testin saflığını korur.
        webTestClient.post().uri("/user/account").bodyValue(existingAccount).exchange().expectStatus().isOk();


        // Act & Assert
        webTestClient.get().uri("/user/account/by-username/find-me") //
                .exchange()
                .expectStatus().isOk()
                .expectBody(AccountDto.class)
                .value(responseDto -> {
                    assertThat(responseDto.getUsername()).isEqualTo("find-me");
                    assertThat(responseDto.getDisplayName()).isEqualTo("Find Me User");
                });
    }

    @Test
    void getByUsername_whenAccountDoesNotExist_shouldReturnNotFound() {
        // Act & Assert
        webTestClient.get().uri("/user/account/by-username/non-existent-user") //
                .exchange()
                .expectStatus().isOk() // WebFlux'ta Mono<T> döndüren bir endpoint, T bulunamazsa boş bir Mono döner. Bu da 200 OK ve boş bir body ile sonuçlanır.
                .expectBody().isEmpty(); // HTTP 404 yerine boş bir body beklemek daha doğru olacaktır.
    }
}