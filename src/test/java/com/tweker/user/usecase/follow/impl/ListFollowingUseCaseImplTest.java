package com.tweker.user.usecase.follow.impl;

import com.tweker.user.entity.UserFollower;
import com.tweker.user.repository.UserFollowerRepository;
import com.tweker.user.usecase.follower.impl.ListFollowingUseCaseImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListFollowingUseCaseImplTest {

    @Mock
    private UserFollowerRepository followerRepository;

    @InjectMocks
    private ListFollowingUseCaseImpl listFollowingUseCase;

    @Test
    void getFollowing_shouldReturnFluxOfFollowedUsers() {
        UUID followerId = UUID.randomUUID();
        UserFollower following1 = UserFollower.builder().followerId(followerId).followedId(UUID.randomUUID()).build();
        UserFollower following2 = UserFollower.builder().followerId(followerId).followedId(UUID.randomUUID()).build();

        when(followerRepository.findAllByFollowerIdAndActiveTrue(followerId))
                .thenReturn(Flux.fromIterable(List.of(following1, following2)));

        Flux<UserFollower> result = listFollowingUseCase.getFollowing(followerId);

        StepVerifier.create(result)
                .expectNextCount(2)
                .verifyComplete();
    }
}