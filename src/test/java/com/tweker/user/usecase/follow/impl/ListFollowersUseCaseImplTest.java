package com.tweker.user.usecase.follow.impl;

import com.tweker.user.entity.UserFollower;
import com.tweker.user.repository.UserFollowerRepository;
import com.tweker.user.usecase.follower.impl.ListFollowersUseCaseImpl;
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
class ListFollowersUseCaseImplTest {

    @Mock
    private UserFollowerRepository followerRepository;

    @InjectMocks
    private ListFollowersUseCaseImpl listFollowersUseCase;

    @Test
    void getFollowers_shouldReturnFluxOfFollowers() {
        UUID followedId = UUID.randomUUID();
        UserFollower follower1 = UserFollower.builder().followerId(UUID.randomUUID()).followedId(followedId).build();
        UserFollower follower2 = UserFollower.builder().followerId(UUID.randomUUID()).followedId(followedId).build();

        when(followerRepository.findAllByFollowedIdAndActiveTrue(followedId))
                .thenReturn(Flux.fromIterable(List.of(follower1, follower2)));

        Flux<UserFollower> result = listFollowersUseCase.getFollowers(followedId);

        StepVerifier.create(result)
                .expectNextCount(2)
                .verifyComplete();
    }
}