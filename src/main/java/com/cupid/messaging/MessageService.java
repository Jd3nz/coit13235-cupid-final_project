package com.cupid.messaging;

import com.cupid.matching.model.User;
import com.cupid.matching.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Coordinates sending messages and retrieving conversations.
 *
 * Supports FR_Messages, FR_Messages_History and FR_Messages_Threads.
 * Uses the shared UserRepository so Messaging and Matching reference
 * the same users.
 */
@Service
@Transactional(readOnly = true)
public class MessageService {

    /**
     * NFR_Input_Sanitise: matches the database column and browser limit so a
     * crafted request cannot bypass the UI's maximum length.
     */
    private static final int MAX_MESSAGE_LENGTH = 2000;

    private final MessageRepository messageRepository;
    private final UserRepository userRepository;

    public MessageService(
            MessageRepository messageRepository,
            UserRepository userRepository
    ) {
        this.messageRepository = messageRepository;
        this.userRepository = userRepository;
    }

    /**
     * Validates and saves a message between two active users.
     * The transaction groups user lookups and saving into one operation.
     */
    @Transactional
    public Message sendMessage(
            Long senderId,
            Long receiverId,
            String messageText
    ) {
        if (messageText == null || messageText.isBlank()) {
            throw new IllegalArgumentException("Enter a message.");
        }

        String trimmedText = messageText.strip();

        if (trimmedText.length() > MAX_MESSAGE_LENGTH) {
            throw new IllegalArgumentException(
                    "Messages must be 2000 characters or fewer."
            );
        }

        rejectSelfConversation(senderId, receiverId);
        User sender = requireActiveUser(senderId);
        User receiver = requireActiveUser(receiverId);

        // Store plain text. The Thymeleaf page must escape it with th:text.
        Message message = new Message(sender, receiver, trimmedText);
        return messageRepository.save(message);
    }

    /**
     * Retrieves both directions of a conversation, oldest first.
     * The controller must supply the current user's ID as viewerId. A
     * self-conversation is rejected here as well as during sending so no route
     * can present a profile as messaging itself.
     */
    public List<Message> getConversation(Long viewerId, Long otherUserId) {
        rejectSelfConversation(viewerId, otherUserId);
        User viewer = requireActiveUser(viewerId);
        User otherUser = requireActiveUser(otherUserId);

        return messageRepository.findConversation(
                viewer.getId(),
                otherUser.getId()
        );
    }

    private User requireActiveUser(Long userId) {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("Select a valid user.");
        }

        return userRepository.findById(userId)
                .filter(User::isActive)
                .orElseThrow(() -> new IllegalArgumentException(
                        "User does not exist or is inactive."
                ));
    }

    /**
     * NFR_Input_Sanitise: this is enforced on the server for both GET and POST
     * requests, because browser selectors and hidden form fields are editable.
     */
    private void rejectSelfConversation(Long senderId, Long receiverId) {
        if (senderId != null && senderId.equals(receiverId)) {
            throw new IllegalArgumentException(
                    "Choose another user as the recipient."
            );
        }
    }
}
