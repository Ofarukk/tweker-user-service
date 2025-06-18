package com.tweker.user.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table("user_followers")
public class UserFollower {

    @Id
    private UUID id;

    private UUID followerId;

    private UUID followedId;

    private Boolean active;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}