package com.tweker.user.usecase.follower;

import com.tweker.user.entity.UserFollower;
import reactor.core.publisher.Flux;

import java.util.UUID;

public interface ListFollowingUseCase {
    Flux<UserFollower> getFollowing(UUID userId);
}