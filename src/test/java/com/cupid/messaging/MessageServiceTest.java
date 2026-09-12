package com.cupid.messaging;

import com.cupid.matching.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verifyNoInteractions;

/**
 * Unit tests for Messaging business rules.
 *
 * NFR_Input_Sanitise: client-side selector rules are backed by a service-level
 * test because browser values can be altered before an HTTP request is sent.
 */
@ExtendWith(MockitoExtension.class)
class MessageServiceTest {

    @Mock
    private MessageRepository messageRepository;

    @Mock
    private UserRepository userRepository;

    private MessageService messageService;

    @BeforeEach
    void setUp() {
        messageService = new MessageService(messageRepository, userRepository);
    }

    @Test
    void inputValidation_shouldRejectSelfConversationBeforeDatabaseAccess() {
        IllegalArgumentException sendException = assertThrows(
                IllegalArgumentException.class,
                () -> messageService.sendMessage(1L, 1L, "Hello")
        );
        IllegalArgumentException viewException = assertThrows(
                IllegalArgumentException.class,
                () -> messageService.getConversation(1L, 1L)
        );

        assertTrue(sendException.getMessage().contains("another user"));
        assertTrue(viewException.getMessage().contains("another user"));
        verifyNoInteractions(messageRepository, userRepository);
    }
}
