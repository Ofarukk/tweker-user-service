package com.tweker.user.controller;

import com.tweker.user.dto.AccountDto;
import com.tweker.user.dto.UserFollowerDto;
import com.tweker.user.entity.UserFollower;
import com.tweker.user.usecase.account.CreateAccountUseCase;
import com.tweker.user.usecase.account.GetAccountByUsernameUseCase;
import com.tweker.user.usecase.account.UpdateAccountUseCase;
import com.tweker.user.usecase.follower.FollowUserUseCase;
import com.tweker.user.usecase.follower.UnfollowUserUseCase;
import com.tweker.user.usecase.follower.ListFollowersUseCase;
import com.tweker.user.usecase.follower.ListFollowingUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Flux;

import java.util.UUID;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final CreateAccountUseCase createAccountUseCase;
    private final GetAccountByUsernameUseCase getAccountByUsernameUseCase;
    private final UpdateAccountUseCase updateAccountUseCase;

    private final FollowUserUseCase followUserUseCase;
    private final UnfollowUserUseCase unfollowUserUseCase;
    private final ListFollowersUseCase listFollowersUseCase;
    private final ListFollowingUseCase listFollowingUseCase;

    // ──────────────────── Account Endpoints ────────────────────

    @PostMapping("/account")
    public Mono<AccountDto> createAccount(@RequestBody AccountDto dto) {
        return createAccountUseCase.create(dto);
    }

    @PutMapping("/account/{id}")
    public Mono<AccountDto> updateAccount(@PathVariable UUID id, @RequestBody AccountDto dto) {
        return updateAccountUseCase.update(id, dto);
    }

    @GetMapping("/account/by-username/{username}")
    public Mono<AccountDto> getByUsername(@PathVariable String username) {
        return getAccountByUsernameUseCase.getByUsername(username);
    }

    // ──────────────────── Follower Endpoints ────────────────────

    @PostMapping("/follow")
    public Mono<Void> follow(@RequestBody UserFollowerDto dto) {
        return followUserUseCase.follow(dto);
    }

    @PostMapping("/unfollow")
    public Mono<Void> unfollow(@RequestBody UserFollowerDto dto) {
        return unfollowUserUseCase.unfollow(dto);
    }

    @GetMapping("/followers/{userId}")
    public Flux<UserFollower> getFollowers(@PathVariable UUID userId) {
        return listFollowersUseCase.getFollowers(userId);
    }

    @GetMapping("/following/{userId}")
    public Flux<UserFollower> getFollowing(@PathVariable UUID userId) {
        return listFollowingUseCase.getFollowing(userId);
    }
}