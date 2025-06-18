package com.tweker.user.usecase.follower;

import com.tweker.user.dto.UserFollowerDto;
import reactor.core.publisher.Mono;

public interface FollowUserUseCase {
    Mono<Void> follow(UserFollowerDto dto);
}