package com.tweker.user.usecase.account.impl;

import com.tweker.user.dto.AccountDto;
import com.tweker.user.entity.Account;
import com.tweker.user.mapper.AccountMapper;
import com.tweker.user.repository.AccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.annotation.Import;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.UUID;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)

class GetAccountByUsernameUseCaseImplTest {

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private GetAccountByUsernameUseCaseImpl getAccountByUsernameUseCase;

    private Account account;

    @BeforeEach
    void setUp() {
        account = Account.builder()
                .id(UUID.randomUUID())
                .userId(UUID.randomUUID())
                .username("jinuser")
                .displayName("Jin")
                .avatarUrl("avatar.jpg")
                .bannerUrl("banner.jpg")
                .bio("Hello world")
                .location("Germany")
                .website("https://example.com")
                .build();
    }

    @Test
    void getByUsername_shouldReturnAccountDto() {
        when(accountRepository.findByUsername("jinuser")).thenReturn(Mono.just(account));

        Mono<AccountDto> result = getAccountByUsernameUseCase.getByUsername("jinuser");

        StepVerifier.create(result)
                .expectNextMatches(dto -> dto.getUsername().equals("jinuser") && dto.getBio().equals("Hello world"))
                .verifyComplete();

        verify(accountRepository).findByUsername("jinuser");
    }
}
