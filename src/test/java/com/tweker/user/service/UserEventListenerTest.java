package com.tweker.user.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tweker.user.dto.AccountDto;
import com.tweker.user.usecase.account.CreateAccountUseCase;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.context.annotation.Import;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;


class UserEventListenerTest {

    @Test
    void listen_shouldTriggerCreateAccountOnUserRegisteredEvent() throws Exception {
        // Arrange
        CreateAccountUseCase useCase = mock(CreateAccountUseCase.class);
        UserEventListener listener = new UserEventListener(useCase);

        UUID userId = UUID.randomUUID();
        String username = "jin";

        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> event = Map.of(
                "eventType", "UserRegistered",
                "data", Map.of(
                        "userId", userId.toString(),
                        "username", username
                )
        );

        String json = mapper.writeValueAsString(event);
        ConsumerRecord<String, String> record = new ConsumerRecord<>("user-events", 0, 0, null, json);

        when(useCase.create(any(AccountDto.class))).thenReturn(Mono.empty());

        // Act
        listener.listen(record);

        // Assert
        ArgumentCaptor<AccountDto> captor = ArgumentCaptor.forClass(AccountDto.class);
        verify(useCase).create(captor.capture());

        AccountDto dto = captor.getValue();
        assertEquals(userId, dto.getUserId());
        assertEquals(username, dto.getUsername());
    }
}
