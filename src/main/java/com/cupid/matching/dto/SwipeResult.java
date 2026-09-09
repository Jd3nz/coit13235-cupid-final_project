package com.cupid.matching.dto;

/**
 * Result returned after processing a swipe.
 *
 * Supports:
 * FR_Swipe
 * FR_Match
 */
public record SwipeResult(boolean matched) {

    public static SwipeResult recorded() {
        return new SwipeResult(false);
    }

    public static SwipeResult matchCreated() {
        return new SwipeResult(true);
    }
}