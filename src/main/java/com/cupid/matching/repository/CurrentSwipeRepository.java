package com.cupid.matching.repository;

import com.cupid.matching.model.CurrentSwipe;
import com.cupid.matching.model.SwipeDecision;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * JPA access to current swipes, alongside the existing JDBC swipe repository.
 */
public interface CurrentSwipeRepository extends JpaRepository<CurrentSwipe, Long> {

    Optional<CurrentSwipe> findBySwiperIdAndTargetUserId(Long swiperId, Long targetUserId);

    boolean existsBySwiperIdAndTargetUserIdAndDecision(
            Long swiperId,
            Long targetUserId,
            SwipeDecision decision
    );

    /**
     * Checks whether the target user previously liked the viewer.
     */
    default boolean hasReciprocalLike(Long viewerId, Long targetUserId) {
        return existsBySwiperIdAndTargetUserIdAndDecision(
                targetUserId, viewerId, SwipeDecision.LIKE
        );
    }
}
