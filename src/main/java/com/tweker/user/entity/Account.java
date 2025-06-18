package com.tweker.user.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table("accounts")
public class Account {

    @Id
    private UUID id;

    private UUID userId;

    private String username;

    private String displayName;

    private String avatarUrl;

    private String bannerUrl;

    private String bio;

    private String location;

    private String website;
}