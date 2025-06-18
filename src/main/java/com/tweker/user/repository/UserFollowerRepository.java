package com.tweker.user.repository;

import com.tweker.user.entity.UserFollower;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface UserFollowerRepository extends ReactiveCrudRepository<UserFollower, UUID> {

    Mono<UserFollower> findByFollowerIdAndFollowedId(UUID followerId, UUID followedId);

    Flux<UserFollower> findAllByFollowedIdAndActiveTrue(UUID followedId);

    Flux<UserFollower> findAllByFollowerIdAndActiveTrue(UUID followerId);
}