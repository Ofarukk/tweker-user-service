package com.tweker.user.usecase.account;

import com.tweker.user.dto.AccountDto;
import reactor.core.publisher.Mono;

public interface CreateAccountUseCase {
    Mono<AccountDto> create(AccountDto dto);
}