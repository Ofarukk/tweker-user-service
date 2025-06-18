package com.tweker.user.repository;

import com.tweker.user.entity.Account;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface AccountRepository extends ReactiveCrudRepository<Account, UUID> {
    Mono<Account> findByUserId(UUID userId);
    Mono<Account> findByUsername(String username);
}