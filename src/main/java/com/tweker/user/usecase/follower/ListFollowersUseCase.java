package com.tweker.user.usecase.follower;

import com.tweker.user.entity.UserFollower;
import reactor.core.publisher.Flux;

import java.util.UUID;

public interface ListFollowersUseCase {
    Flux<UserFollower> getFollowers(UUID userId);
}