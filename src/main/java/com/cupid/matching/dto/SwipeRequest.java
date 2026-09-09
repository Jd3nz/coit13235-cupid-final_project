package com.cupid.matching.dto;

import com.cupid.matching.model.SwipeDecision;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * Validated input for FR_Swipe.
 *
 * Hidden form fields are still controlled by the browser, so all values
 * must be validated again on the server.
 */
public class SwipeRequest {

    @NotNull(message = "Viewer ID is required")
    @Positive(message = "Viewer ID must be positive")
    private Long viewerId;

    @NotNull(message = "Target user ID is required")
    @Positive(message = "Target user ID must be positive")
    private Long targetUserId;

    @NotNull(message = "Swipe decision is required")
    private SwipeDecision decision;

    public SwipeRequest() {
    }

    public SwipeRequest(
            Long viewerId,
            Long targetUserId,
            SwipeDecision decision
    ) {
        this.viewerId = viewerId;
        this.targetUserId = targetUserId;
        this.decision = decision;
    }

    public Long getViewerId() {
        return viewerId;
    }

    public void setViewerId(Long viewerId) {
        this.viewerId = viewerId;
    }

    public Long getTargetUserId() {
        return targetUserId;
    }

    public void setTargetUserId(Long targetUserId) {
        this.targetUserId = targetUserId;
    }

    public SwipeDecision getDecision() {
        return decision;
    }

    public void setDecision(SwipeDecision decision) {
        this.decision = decision;
    }
}