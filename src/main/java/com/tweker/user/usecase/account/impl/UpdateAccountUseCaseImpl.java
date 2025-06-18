package com.tweker.user.usecase.account.impl;

import com.tweker.user.dto.AccountDto;
import com.tweker.user.entity.Account;
import com.tweker.user.mapper.AccountMapper;
import com.tweker.user.repository.AccountRepository;
import com.tweker.user.service.UserKafkaProducer;
import com.tweker.user.usecase.account.UpdateAccountUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UpdateAccountUseCaseImpl implements UpdateAccountUseCase {

    private final AccountRepository accountRepository;

    private final UserKafkaProducer kafkaProducer;

    @Override
    public Mono<AccountDto> update(UUID id, AccountDto dto) {
        return accountRepository.findById(id)
                .flatMap(existing -> {
                    boolean usernameChanged = !existing.getUsername().equals(dto.getUsername());
                    boolean avatarChanged = dto.getAvatarUrl() != null &&
                            !dto.getAvatarUrl().equals(existing.getAvatarUrl());

                    existing.setUsername(dto.getUsername());
                    existing.setDisplayName(dto.getDisplayName());
                    existing.setAvatarUrl(dto.getAvatarUrl());
                    existing.setBannerUrl(dto.getBannerUrl());
                    existing.setBio(dto.getBio());
                    existing.setLocation(dto.getLocation());
                    existing.setWebsite(dto.getWebsite());

                    return accountRepository.save(existing)
                            .flatMap(saved -> {
                                if (usernameChanged || avatarChanged) {
                                    return kafkaProducer.sendUserUpdatedEvent(saved.getUserId(), saved.getUsername(), saved.getAvatarUrl())
                                            .thenReturn(saved);
                                } else {
                                    return Mono.just(saved);
                                }
                            });
                })
                .map(AccountMapper::toDto);
    }
}