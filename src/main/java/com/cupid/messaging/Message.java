package com.cupid.messaging;

import com.cupid.matching.model.User;
import jakarta.persistence.*;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

/**
 * Represents one message between two existing Cupid users.
 *
 * Supports FR_Messages and FR_Messages_History.
 * Sender and receiver reference the shared users table.
 */
@Entity
@Table(name = "messages")
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Many messages can belong to the same sender or receiver.
    // Saving a message must not create or delete its users.
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "receiver_id", nullable = false)
    private User receiver;

    @Column(name = "message_text", nullable = false, length = 2000)
    private String messageText;

    @Column(name = "sent_at", nullable = false, updatable = false)
    private OffsetDateTime sentAt;

    protected Message() {
        // Required by JPA when loading messages from the database.
    }

    public Message(User sender, User receiver, String messageText) {
        this.sender = sender;
        this.receiver = receiver;
        this.messageText = messageText;
    }

    @PrePersist
    private void recordSentTime() {
        // Record the time on the server when the message is first saved.
        this.sentAt = OffsetDateTime.now(ZoneOffset.UTC);
    }

    public Long getId() {
        return id;
    }

    public User getSender() {
        return sender;
    }

    public User getReceiver() {
        return receiver;
    }

    public String getMessageText() {
        return messageText;
    }

    public OffsetDateTime getSentAt() {
        return sentAt;
    }
}