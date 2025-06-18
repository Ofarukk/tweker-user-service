package com.tweker.user.usecase.account.impl;

import com.tweker.user.dto.AccountDto;
import com.tweker.user.mapper.AccountMapper;
import com.tweker.user.repository.AccountRepository;
import com.tweker.user.service.UserKafkaProducer;
import com.tweker.user.usecase.account.CreateAccountUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class CreateAccountUseCaseImpl implements CreateAccountUseCase {

    private final AccountRepository accountRepository;
    private final UserKafkaProducer kafkaProducer;

    @Override
    public Mono<AccountDto> create(AccountDto dto) {
        log.info("here it comes");
        return accountRepository.save(AccountMapper.toEntity(dto))
                .doOnNext(saved -> log.info("here converted dto {}", saved))
                .doOnNext(saved -> log.info("🆔 Saved account with generated ID: {}", saved.getId()))
                .flatMap(saved ->
                        kafkaProducer.sendUserCreatedEvent(saved.getUserId(), saved.getUsername())
                                .thenReturn(saved)
                )
                .map(AccountMapper::toDto);
    }
}