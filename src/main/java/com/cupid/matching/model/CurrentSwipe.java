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
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.OffsetDateTime;

/**
 * The latest swipe decision for one directed pair of users.
 *
 * Architecture: this is mutable state used by matching; {@link SwipeHistory}
 * separately retains every decision for FR_Swipe_History. The unique pair
 * constraint ensures one current decision per direction.
 */
@Entity
@Table(name = "current_swipes", uniqueConstraints = @UniqueConstraint(
        name = "uq_current_swipe",
        columnNames = {"swiper_id", "target_user_id"}
))
public class CurrentSwipe {

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

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    protected CurrentSwipe() {
        // Required by JPA.
    }

    public CurrentSwipe(User swiper, User targetUser, SwipeDecision decision) {
        this.swiper = swiper;
        this.targetUser = targetUser;
        this.decision = decision;
    }

    @PrePersist
    private void initializeTimestamps() {
        OffsetDateTime now = OffsetDateTime.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    private void updateTimestamp() {
        updatedAt = OffsetDateTime.now();
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

    public void setDecision(SwipeDecision decision) {
        // Updating this row preserves the latest state without erasing history.
        this.decision = decision;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }
}
