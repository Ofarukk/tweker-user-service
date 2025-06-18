package com.tweker.user.dto;

import lombok.*;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserFollowerDto {

    private UUID followerId;

    private UUID followedId;
}
