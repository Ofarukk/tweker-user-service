package com.tweker.user.usecase.follow.impl;

import com.tweker.user.dto.UserFollowerDto;
import com.tweker.user.entity.UserFollower;
import com.tweker.user.repository.UserFollowerRepository;
import com.tweker.user.usecase.follower.impl.UnfollowUserUseCaseImpl;
import com.tweker.user.service.UserKafkaProducer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UnfollowUserUseCaseImplTest {

    @Mock
    private UserFollowerRepository followerRepository;

    @Mock
    private UserKafkaProducer kafkaProducer;

    @InjectMocks
    private UnfollowUserUseCaseImpl unfollowUserUseCase;

    private UserFollowerDto userFollowerDto;
    private UserFollower existingFollow;
    private UUID followerId;
    private UUID followedId;

    @BeforeEach
    void setUp() {
        followerId = UUID.randomUUID();
        followedId = UUID.randomUUID();
        userFollowerDto = new UserFollowerDto(followerId, followedId);
        existingFollow = UserFollower.builder()
                .id(UUID.randomUUID())
                .followerId(followerId)
                .followedId(followedId)
                .active(true)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    void unfollow_whenRelationshipExists_shouldDeactivateAndSendEvent() {
        when(followerRepository.findByFollowerIdAndFollowedId(followerId, followedId)).thenReturn(Mono.just(existingFollow));
        when(followerRepository.save(any(UserFollower.class))).thenReturn(Mono.just(existingFollow));
        when(kafkaProducer.sendUserUnfollowedEvent(followerId, followedId)).thenReturn(Mono.empty());

        Mono<Void> result = unfollowUserUseCase.unfollow(userFollowerDto);

        StepVerifier.create(result).verifyComplete();

        ArgumentCaptor<UserFollower> captor = ArgumentCaptor.forClass(UserFollower.class);
        verify(followerRepository).save(captor.capture());

        assertThat(captor.getValue().getActive()).isFalse();
        verify(kafkaProducer).sendUserUnfollowedEvent(followerId, followedId);
    }

    @Test
    void unfollow_whenRelationshipDoesNotExist_shouldDoNothing() {
        when(followerRepository.findByFollowerIdAndFollowedId(followerId, followedId)).thenReturn(Mono.empty());


        Mono<Void> result = unfollowUserUseCase.unfollow(userFollowerDto);

        StepVerifier.create(result).verifyComplete();
        verify(followerRepository, never()).save(any());
        verify(kafkaProducer, never()).sendUserUnfollowedEvent(any(), any());
    }
}