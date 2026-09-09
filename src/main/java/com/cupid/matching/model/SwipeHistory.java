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

import java.time.OffsetDateTime;

/**
 * A historical swipe decision for one directed pair of users.
 */
@Entity
@Table(name = "swipe_history")
public class SwipeHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "swiper_id", nullable = false)
    private User swiper;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "target_user_id", nullable = false)
    private User targetUser;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private SwipeDecision decision;

    @Column(name = "swiped_at", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime swipedAt;

    protected SwipeHistory() {
        // Required by JPA.
    }

    public SwipeHistory(User swiper, User targetUser, SwipeDecision decision) {
        this.swiper = swiper;
        this.targetUser = targetUser;
        this.decision = decision;
    }

    public Long getId() {
        return id;
    }

    public User getSwiper() {
        return swiper;
    }

    public User getTargetUser() {
        return targetUser;
    }

    public SwipeDecision getDecision() {
        return decision;
    }

    public OffsetDateTime getSwipedAt() {
        return swipedAt;
    }
}
