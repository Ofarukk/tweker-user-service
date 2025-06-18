package com.tweker.user.mapper;

import com.tweker.user.dto.AccountDto;
import com.tweker.user.entity.Account;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class AccountMapperTest {

    @Test
    void toEntity_shouldMapDtoToEntityCorrectly() {
        UUID userId = UUID.randomUUID();
        AccountDto dto = AccountDto.builder()
                .userId(userId)
                .username("jinuser")
                .displayName("Jin")
                .avatarUrl("avatar.jpg")
                .bannerUrl("banner.jpg")
                .bio("bio")
                .location("Germany")
                .website("https://site.com")
                .build();

        Account entity = AccountMapper.toEntity(dto);

        assertEquals(dto.getUserId(), entity.getUserId());
        assertEquals(dto.getUsername(), entity.getUsername());
        assertEquals(dto.getDisplayName(), entity.getDisplayName());
        assertEquals(dto.getAvatarUrl(), entity.getAvatarUrl());
        assertEquals(dto.getBannerUrl(), entity.getBannerUrl());
        assertEquals(dto.getBio(), entity.getBio());
        assertEquals(dto.getLocation(), entity.getLocation());
        assertEquals(dto.getWebsite(), entity.getWebsite());
    }

    @Test
    void toDto_shouldMapEntityToDtoCorrectly() {
        UUID userId = UUID.randomUUID();
        Account entity = Account.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .username("jinuser")
                .displayName("Jin")
                .avatarUrl("avatar.jpg")
                .bannerUrl("banner.jpg")
                .bio("bio")
                .location("Germany")
                .website("https://site.com")
                .build();

        AccountDto dto = AccountMapper.toDto(entity);

        assertEquals(entity.getUserId(), dto.getUserId());
        assertEquals(entity.getUsername(), dto.getUsername());
        assertEquals(entity.getDisplayName(), dto.getDisplayName());
        assertEquals(entity.getAvatarUrl(), dto.getAvatarUrl());
        assertEquals(entity.getBannerUrl(), dto.getBannerUrl());
        assertEquals(entity.getBio(), dto.getBio());
        assertEquals(entity.getLocation(), dto.getLocation());
        assertEquals(entity.getWebsite(), dto.getWebsite());
    }
}