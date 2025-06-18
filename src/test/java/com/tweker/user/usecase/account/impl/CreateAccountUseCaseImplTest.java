package com.tweker.user.usecase.account.impl;

import com.tweker.user.dto.AccountDto;
import com.tweker.user.entity.Account;
import com.tweker.user.mapper.AccountMapper;
import com.tweker.user.repository.AccountRepository;
import com.tweker.user.service.UserKafkaProducer;
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

class CreateAccountUseCaseImplTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private UserKafkaProducer kafkaProducer;

    @InjectMocks
    private CreateAccountUseCaseImpl createAccountUseCase;

    private AccountDto accountDto;
    private Account account;

    @BeforeEach
    void setUp() {
        UUID userId = UUID.randomUUID();
        accountDto = AccountDto.builder()
                .userId(userId)
                .username("jinuser")
                .displayName("Jin")
                .avatarUrl("avatar.jpg")
                .bannerUrl("banner.jpg")
                .bio("Hello world")
                .location("Germany")
                .website("https://example.com")
                .build();

        account = AccountMapper.toEntity(accountDto);
        account.setId(UUID.randomUUID());
    }

    @Test
    void create_shouldSaveAccountAndSendKafkaEvent() {
        // arrange
        when(accountRepository.save(any(Account.class))).thenReturn(Mono.just(account));
        when(kafkaProducer.sendUserCreatedEvent(any(), any())).thenReturn(Mono.empty());

        // act
        Mono<AccountDto> result = createAccountUseCase.create(accountDto);

        // assert
        StepVerifier.create(result)
                .expectNextMatches(dto -> dto.getUsername().equals("jinuser"))
                .verifyComplete();

        verify(accountRepository).save(any(Account.class));
        verify(kafkaProducer).sendUserCreatedEvent(account.getUserId(), account.getUsername());
    }
}
