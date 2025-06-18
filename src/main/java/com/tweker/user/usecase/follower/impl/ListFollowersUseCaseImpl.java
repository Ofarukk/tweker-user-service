package com.tweker.user.usecase.follower.impl;

import com.tweker.user.entity.UserFollower;
import com.tweker.user.repository.UserFollowerRepository;
import com.tweker.user.usecase.follower.ListFollowersUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ListFollowersUseCaseImpl implements ListFollowersUseCase {

    private final UserFollowerRepository followerRepository;

    @Override
    public Flux<UserFollower> getFollowers(UUID userId) {
        return followerRepository.findAllByFollowedIdAndActiveTrue(userId);
    }
}