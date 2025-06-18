package com.tweker.user.mapper;

import com.tweker.user.dto.AccountDto;
import com.tweker.user.entity.Account;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

@Slf4j
public class AccountMapper {

    public static Account toEntity(AccountDto dto) {
        log.info("here it comes to mapper ");
        return Account.builder()
                .userId(dto.getUserId())
                .username(dto.getUsername())
                .displayName(dto.getDisplayName())
                .avatarUrl(dto.getAvatarUrl())
                .bannerUrl(dto.getBannerUrl())
                .bio(dto.getBio())
                .location(dto.getLocation())
                .website(dto.getWebsite())
                .build();
    }

    public static AccountDto toDto(Account entity) {
        return AccountDto.builder()
                .userId(entity.getUserId())
                .username(entity.getUsername())
                .displayName(entity.getDisplayName())
                .avatarUrl(entity.getAvatarUrl())
                .bannerUrl(entity.getBannerUrl())
                .bio(entity.getBio())
                .location(entity.getLocation())
                .website(entity.getWebsite())
                .build();
    }
}
