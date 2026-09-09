package com.cupid.matching.repository;

import com.cupid.matching.model.Match;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * JPA access to matches.
 */
public interface MatchRepository extends JpaRepository<Match, Long> {

    @Query("""
            SELECT match
            FROM Match match
            WHERE match.status = com.cupid.matching.model.MatchStatus.ACTIVE
              AND (match.userOne.id = :userId OR match.userTwo.id = :userId)
            ORDER BY match.matchedAt DESC
            """)
    @EntityGraph(attributePaths = {"userOne", "userTwo"})
    List<Match> findActiveMatchesForUser(@Param("userId") Long userId);

    Optional<Match> findByUserOneIdAndUserTwoId(Long userOneId, Long userTwoId);

    boolean existsByUserOneIdAndUserTwoId(Long userOneId, Long userTwoId);
}
