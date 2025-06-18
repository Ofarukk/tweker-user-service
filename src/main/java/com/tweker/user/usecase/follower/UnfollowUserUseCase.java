package com.tweker.user.usecase.follower;

import com.tweker.user.dto.UserFollowerDto;
import reactor.core.publisher.Mono;

public interface UnfollowUserUseCase {
    Mono<Void> unfollow(UserFollowerDto dto);
}