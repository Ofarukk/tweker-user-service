package com.tweker.user.integration;

import com.tweker.user.entity.UserFollower;
import com.tweker.user.repository.UserFollowerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import reactor.test.StepVerifier;

import java.util.UUID;

@Testcontainers
@DataR2dbcTest
@ActiveProfiles("test")
public class UserFollowerRepositoryIT {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("test-db")
            .withUsername("testuser")
            .withPassword("testpass");

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
    }

    @BeforeEach
    void setUp() {
        userFollowerRepository.deleteAll().block();
    }

    @Test
    void findAllByFollowerIdAndActiveTrue_shouldReturnFollowingUsers() {
        // Arrange
        UUID user1 = UUID.randomUUID();
        UUID user2 = UUID.randomUUID();
        UUID user3 = UUID.randomUUID();
        UUID user4 = UUID.randomUUID();

        UserFollower follow1 = UserFollower.builder().followerId(user1).followedId(user2).active(true).build();
        UserFollower follow2 = UserFollower.builder().followerId(user1).followedId(user3).active(true).build();
        UserFollower follow3 = UserFollower.builder().followerId(user1).followedId(user4).active(false).build();
        UserFollower follow4 = UserFollower.builder().followerId(user2).followedId(user1).active(true).build();

        userFollowerRepository.saveAll(java.util.List.of(follow1, follow2, follow3, follow4)).blockLast();

        // Act
        var followingFlux = userFollowerRepository.findAllByFollowerIdAndActiveTrue(user1);

        // Assert
        StepVerifier.create(followingFlux)
                .expectNextCount(2)
                .verifyComplete();
    }
}