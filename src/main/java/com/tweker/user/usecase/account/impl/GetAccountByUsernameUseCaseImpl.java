package com.tweker.user.usecase.account.impl;

import com.tweker.user.dto.AccountDto;
import com.tweker.user.mapper.AccountMapper;
import com.tweker.user.repository.AccountRepository;
import com.tweker.user.usecase.account.GetAccountByUsernameUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class GetAccountByUsernameUseCaseImpl implements GetAccountByUsernameUseCase {

    private final AccountRepository accountRepository;

    @Override
    public Mono<AccountDto> getByUsername(String username) {
        return accountRepository.findByUsername(username)
                .map(AccountMapper::toDto);
    }
}