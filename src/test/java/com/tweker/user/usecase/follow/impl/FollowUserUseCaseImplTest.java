package com.tweker.user.usecase.follow.impl;

import com.tweker.user.dto.UserFollowerDto;
import com.tweker.user.entity.UserFollower;
import com.tweker.user.repository.UserFollowerRepository;
import com.tweker.user.service.UserKafkaProducer;
import com.tweker.user.usecase.follower.impl.FollowUserUseCaseImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FollowUserUseCaseImplTest {

    @Mock
    private UserFollowerRepository followerRepository;

    @Mock
    private UserKafkaProducer kafkaProducer;

    @InjectMocks
    private FollowUserUseCaseImpl followUserUseCase;

    private UserFollowerDto userFollowerDto;
    private UUID followerId;
    private UUID followedId;

    @BeforeEach
    void setUp() {
        followerId = UUID.randomUUID();
        followedId = UUID.randomUUID();
        userFollowerDto = new UserFollowerDto(followerId, followedId);
    }

    @Test
    void follow_whenNewFollow_shouldSaveAndSendKafkaEvent() {
        // Arrange
        when(followerRepository.findByFollowerIdAndFollowedId(followerId, followedId)).thenReturn(Mono.empty());
        when(followerRepository.save(any(UserFollower.class))).thenReturn(Mono.just(new UserFollower()));
        when(kafkaProducer.sendUserFollowedEvent(followerId, followedId)).thenReturn(Mono.empty());

        // Act
        Mono<Void> result = followUserUseCase.follow(userFollowerDto);

        // Assert
        StepVerifier.create(result)
                .verifyComplete();

        verify(followerRepository).findByFollowerIdAndFollowedId(followerId, followedId);
        verify(followerRepository).save(any(UserFollower.class));
        verify(kafkaProducer).sendUserFollowedEvent(followerId, followedId);
    }

    @Test
    void follow_whenReFollowing_shouldUpdateAndSendKafkaEvent() {
        UserFollower existingInactiveFollow = UserFollower.builder()
                .id(UUID.randomUUID())
                .followerId(followerId)
                .followedId(followedId)
                .active(false)
                .build();

        when(followerRepository.findByFollowerIdAndFollowedId(followerId, followedId))
                .thenReturn(Mono.just(existingInactiveFollow));
        when(followerRepository.save(any(UserFollower.class))).thenReturn(Mono.just(existingInactiveFollow));
        when(kafkaProducer.sendUserFollowedEvent(followerId, followedId)).thenReturn(Mono.empty());

        // Act
        Mono<Void> result = followUserUseCase.follow(userFollowerDto);

        // Assert
        StepVerifier.create(result)
                .verifyComplete();

        verify(followerRepository).save(argThat(savedFollower -> savedFollower.getActive()));
        verify(kafkaProducer).sendUserFollowedEvent(followerId, followedId);
    }
}