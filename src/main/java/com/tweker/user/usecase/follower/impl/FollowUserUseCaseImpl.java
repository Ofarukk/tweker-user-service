package com.tweker.user.usecase.follower.impl;

import com.tweker.user.dto.UserFollowerDto;
import com.tweker.user.entity.UserFollower;
import com.tweker.user.mapper.UserFollowerMapper;
import com.tweker.user.repository.UserFollowerRepository;
import com.tweker.user.service.UserKafkaProducer;
import com.tweker.user.usecase.follower.FollowUserUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class FollowUserUseCaseImpl implements FollowUserUseCase {

    private final UserFollowerRepository followerRepository;
    private final UserKafkaProducer kafkaProducer;

    @Override
    public Mono<Void> follow(UserFollowerDto dto) {
        return followerRepository.findByFollowerIdAndFollowedId(dto.getFollowerId(), dto.getFollowedId())
                .flatMap(existing -> {
                    existing.setActive(true);
                    existing.setUpdatedAt(LocalDateTime.now());
                    return followerRepository.save(existing);
                })
                .switchIfEmpty(Mono.defer(() ->
                        followerRepository.save(UserFollowerMapper.toEntity(dto))
                ))
                .then(kafkaProducer.sendUserFollowedEvent(dto.getFollowerId(), dto.getFollowedId()));
    }
}
