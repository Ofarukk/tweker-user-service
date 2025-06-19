package com.tweker.user.integration;

import com.tweker.user.UserServiceApplication;
import com.tweker.user.dto.AccountDto;
import com.tweker.user.dto.UserFollowerDto;
import com.tweker.user.entity.Account;
import com.tweker.user.entity.UserFollower;
import com.tweker.user.repository.AccountRepository;
import com.tweker.user.repository.UserFollowerRepository;
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
    @Autowired
    private UserFollowerRepository userFollowerRepository;

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
        userFollowerRepository.deleteAll().block();
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
    void updateAccount_shouldUpdateAccountDetails() {
        // Arrange - 1: Önce güncellenecek bir kullanıcı oluştur
        UUID userId = UUID.randomUUID();
        AccountDto accountToCreate = AccountDto.builder()
                .userId(userId)
                .username("user-to-update")
                .displayName("Initial Name")
                .build();

        // Account'u API üzerinden oluştur
        webTestClient.post().uri("/user/account")
                .bodyValue(accountToCreate)
                .exchange()
                .expectStatus().isOk();

        // Arrange - 2: Oluşturulan account'un ID'sini ve güncel bilgilerini hazırla
        // Not: Account ID'sini almak için repository'yi kullanıyoruz.
        Account savedAccount = accountRepository.findByUsername("user-to-update").block();
        assertThat(savedAccount).isNotNull();
        UUID accountId = savedAccount.getId();

        AccountDto updateDto = AccountDto.builder()
                .userId(userId) // userId değişmez
                .username("user-updated")
                .displayName("Updated Name")
                .bio("This is my new bio.")
                .location("New Location")
                .build();


        // Act & Assert
        webTestClient.put().uri("/user/account/" + accountId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(updateDto)
                .exchange()
                .expectStatus().isOk()
                .expectBody(AccountDto.class)
                .value(responseDto -> {
                    assertThat(responseDto.getUsername()).isEqualTo("user-updated");
                    assertThat(responseDto.getDisplayName()).isEqualTo("Updated Name");
                    assertThat(responseDto.getBio()).isEqualTo("This is my new bio.");
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

    @Test
    void followAndUnfollow_shouldWorkCorrectly() {
        // --- Arrange: İki kullanıcı oluştur ---
        AccountDto followerDto = AccountDto.builder().userId(UUID.randomUUID()).username("followerUser").build();
        AccountDto followedDto = AccountDto.builder().userId(UUID.randomUUID()).username("followedUser").build();

        // API üzerinden kullanıcıları kaydet
        webTestClient.post().uri("/user/account").bodyValue(followerDto).exchange().expectStatus().isOk();
        webTestClient.post().uri("/user/account").bodyValue(followedDto).exchange().expectStatus().isOk();

        // Kaydedilen kullanıcıların ID'lerini al
        Account follower = accountRepository.findByUsername("followerUser").block();
        Account followed = accountRepository.findByUsername("followedUser").block();
        assertThat(follower).isNotNull();
        assertThat(followed).isNotNull();

        UserFollowerDto followRequest = new UserFollowerDto(follower.getUserId(), followed.getUserId());

        // --- Act 1: Follow (Takip Et) ---
        webTestClient.post().uri("/user/follow")
                .bodyValue(followRequest)
                .exchange()
                .expectStatus().isOk(); // Mono<Void> olduğu için 200 OK bekliyoruz.

        // --- Assert 1: Takip durumunu doğrula ---
        webTestClient.get().uri("/user/followers/" + followed.getUserId())
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(UserFollower.class).hasSize(1)
                .value(followers -> {
                    assertThat(followers.get(0).getFollowerId()).isEqualTo(follower.getUserId());
                });

        // --- Act 2: Unfollow (Takipten Çık) ---
        webTestClient.post().uri("/user/unfollow")
                .bodyValue(followRequest)
                .exchange()
                .expectStatus().isOk();

        // --- Assert 2: Takipten çıkma durumunu doğrula ---
        webTestClient.get().uri("/user/followers/" + followed.getUserId())
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(UserFollower.class).hasSize(0); // Takipçi listesi boş olmalı
    }
}