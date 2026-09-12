package com.cupid.matching.service;

import com.cupid.matching.dto.MatchSummary;
import com.cupid.matching.model.Match;
import com.cupid.matching.model.User;
import com.cupid.matching.repository.MatchRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Business logic for retrieving a user's matches.
 *
 * Supports:
 * FR_Match
 * FR_Web_UI
 */
@Service
public class MatchService {

    private final MatchRepository matchRepository;

    public MatchService(MatchRepository matchRepository) {
        this.matchRepository = matchRepository;
    }

    /**
     * FR_Match: returns display-ready data rather than JPA entities so the web
     * UI does not need to know how a canonical match pair is stored.
     */
    public List<MatchSummary> findActiveMatches(Long userId) {
        if (userId == null || userId <= 0) {
            return List.of();
        }

        return matchRepository.findActiveMatchesForUser(userId)
                .stream()
            .map(match -> toSummary(match, userId))
            .toList();
    }

    private MatchSummary toSummary(Match match, Long viewerId) {
        // Match stores user IDs in canonical order; this chooses the other person.
        User matchedUser = match.getUserOne().getId().equals(viewerId)
                ? match.getUserTwo()
                : match.getUserOne();

        return new MatchSummary(
                match.getId(),
                matchedUser.getId(),
                matchedUser.getDisplayName(),
                matchedUser.getAge(),
                matchedUser.getBio(),
                match.getMatchedAt(),
                match.getStatus()
        );
    }
}
