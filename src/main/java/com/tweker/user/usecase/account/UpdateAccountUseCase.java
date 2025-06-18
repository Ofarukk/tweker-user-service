package com.tweker.user.usecase.account;

import com.tweker.user.dto.AccountDto;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface UpdateAccountUseCase {
    Mono<AccountDto> update(UUID id, AccountDto dto);
}