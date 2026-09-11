package com.cupid.messaging;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Provides database access for messages through Spring Data JPA.
 *
 * JpaRepository supplies save() and findById().
 * The custom query supports FR_Messages_Threads.
 */
public interface MessageRepository extends JpaRepository<Message, Long> {

    /**
     * Retrieves messages in both directions between two users.
     *
     * Oldest messages appear first. The ID provides a consistent
     * order when two messages have the same timestamp.
     * Fetching both users also makes their names available to the page.
     */
    @Query("""
            SELECT m
            FROM Message m
            JOIN FETCH m.sender
            JOIN FETCH m.receiver
            WHERE (m.sender.id = :user1Id AND m.receiver.id = :user2Id)
               OR (m.sender.id = :user2Id AND m.receiver.id = :user1Id)
            ORDER BY m.sentAt ASC, m.id ASC
            """)
    List<Message> findConversation(
            @Param("user1Id") Long user1Id,
            @Param("user2Id") Long user2Id
    );
}