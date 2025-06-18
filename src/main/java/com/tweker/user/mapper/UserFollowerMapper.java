package com.tweker.user.mapper;

import com.tweker.user.dto.UserFollowerDto;
import com.tweker.user.entity.UserFollower;

import java.time.LocalDateTime;
import java.util.UUID;

public class UserFollowerMapper {

    public static UserFollower toEntity(UserFollowerDto dto) {
        return UserFollower.builder()
                .followerId(dto.getFollowerId())
                .followedId(dto.getFollowedId())
                .active(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }
}