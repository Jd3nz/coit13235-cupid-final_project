package com.cupid.matching.dto;

import com.cupid.matching.model.MatchStatus;

import java.time.OffsetDateTime;

/**
 * Information displayed for one successful match.
 *
 * Supports:
 * FR_Match
 * FR_Web_UI
 */
public record MatchSummary(
        Long matchId,
        Long matchedUserId,
        String displayName,
        int age,
        String bio,
        OffsetDateTime matchedAt,
        MatchStatus status
) {
}