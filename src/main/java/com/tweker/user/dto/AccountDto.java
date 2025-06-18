package com.tweker.user.dto;

import lombok.*;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountDto {

    private UUID userId;

    private String username;

    private String displayName;

    private String avatarUrl;

    private String bannerUrl;

    private String bio;

    private String location;

    private String website;
}
