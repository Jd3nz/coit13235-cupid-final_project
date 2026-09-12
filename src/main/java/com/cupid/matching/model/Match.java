package com.cupid.matching.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.OffsetDateTime;

/**
 * A match between two users stored in ascending user ID order.
 *
 * Architecture: canonical ordering and the unique pair constraint prevent the
 * reciprocal Like flow from creating duplicate matches.
 */
@Entity
@Table(name = "matches", uniqueConstraints = @UniqueConstraint(
        name = "uq_match_pair",
        columnNames = {"user_one_id", "user_two_id"}
))
public class Match {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_one_id", nullable = false)
    private User userOne;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_two_id", nullable = false)
    private User userTwo;

    @Column(name = "matched_at", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime matchedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MatchStatus status;

    protected Match() {
        // Required by JPA.
    }

    public Match(User userOne, User userTwo, MatchStatus status) {
        this.userOne = userOne;
        this.userTwo = userTwo;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public User getUserOne() {
        return userOne;
    }

    public User getUserTwo() {
        return userTwo;
    }

    public OffsetDateTime getMatchedAt() {
        return matchedAt;
    }

    public MatchStatus getStatus() {
        return status;
    }

    public void setStatus(MatchStatus status) {
        this.status = status;
    }
}
