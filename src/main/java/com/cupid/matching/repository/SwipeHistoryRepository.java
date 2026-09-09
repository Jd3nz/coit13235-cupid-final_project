package com.cupid.matching.repository;

import com.cupid.matching.model.SwipeHistory;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * JPA access to swipe history.
 */
public interface SwipeHistoryRepository extends JpaRepository<SwipeHistory, Long> {

    @EntityGraph(attributePaths = "targetUser")
    List<SwipeHistory> findBySwiperIdOrderBySwipedAtDesc(Long swiperId);
}
