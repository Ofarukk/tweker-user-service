package com.tweker.user.usecase.follower.impl;

import com.tweker.user.dto.UserFollowerDto;
import com.tweker.user.repository.UserFollowerRepository;
import com.tweker.user.service.UserKafkaProducer;
import com.tweker.user.usecase.follower.UnfollowUserUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UnfollowUserUseCaseImpl implements UnfollowUserUseCase {

    private final UserFollowerRepository followerRepository;
    private final UserKafkaProducer kafkaProducer;

    @Override
    public Mono<Void> unfollow(UserFollowerDto dto) {
        return followerRepository.findByFollowerIdAndFollowedId(dto.getFollowerId(), dto.getFollowedId())
                .flatMap(existing -> {
                    existing.setActive(false);
                    existing.setUpdatedAt(LocalDateTime.now());
                    return followerRepository.save(existing);
                })
                .flatMap(saved ->
                        kafkaProducer.sendUserUnfollowedEvent(dto.getFollowerId(), dto.getFollowedId())
                );
    }
}