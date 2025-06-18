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

class UpdateAccountUseCaseImplTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private UserKafkaProducer kafkaProducer;

    @InjectMocks
    private UpdateAccountUseCaseImpl updateAccountUseCase;

    private Account existing;
    private AccountDto incomingDto;

    @BeforeEach
    void setUp() {
        existing = Account.builder()
                .id(UUID.randomUUID())
                .userId(UUID.randomUUID())
                .username("oldUser")
                .displayName("Old Name")
                .avatarUrl("old.jpg")
                .bannerUrl("oldBanner.jpg")
                .bio("Old bio")
                .location("Oldtown")
                .website("https://oldsite.com")
                .build();

        incomingDto = AccountDto.builder()
                .userId(existing.getUserId()) // aynı userId
                .username("newUser")
                .displayName("New Name")
                .avatarUrl("new.jpg")
                .bannerUrl("newBanner.jpg")
                .bio("New bio")
                .location("Newtown")
                .website("https://newsite.com")
                .build();
    }

    @Test
    void update_shouldUpdateAndSendKafkaEventIfUsernameOrAvatarChanged() {
        when(accountRepository.findById(existing.getId())).thenReturn(Mono.just(existing));
        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));
        when(kafkaProducer.sendUserUpdatedEvent(any(), any(), any())).thenReturn(Mono.empty());

        Mono<AccountDto> result = updateAccountUseCase.update(existing.getId(), incomingDto);

        StepVerifier.create(result)
                .expectNextMatches(dto -> dto.getUsername().equals("newUser") && dto.getAvatarUrl().equals("new.jpg"))
                .verifyComplete();

        verify(accountRepository).findById(existing.getId());
        verify(accountRepository).save(any(Account.class));
        verify(kafkaProducer).sendUserUpdatedEvent(existing.getUserId(), "newUser", "new.jpg");
    }

    @Test
    void update_shouldNotSendKafkaEventIfNoChange() {
        incomingDto.setUsername(existing.getUsername());
        incomingDto.setAvatarUrl(existing.getAvatarUrl());

        when(accountRepository.findById(existing.getId())).thenReturn(Mono.just(existing));
        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        Mono<AccountDto> result = updateAccountUseCase.update(existing.getId(), incomingDto);

        StepVerifier.create(result)
                .expectNextMatches(dto -> dto.getUsername().equals("oldUser"))
                .verifyComplete();

        verify(accountRepository).findById(existing.getId());
        verify(accountRepository).save(any(Account.class));
        verify(kafkaProducer, never()).sendUserUpdatedEvent(any(), any(), any());
    }
}
