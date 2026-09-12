package com.cupid.matching.repository;

import com.cupid.matching.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * User persistence through JPA, retaining the discovery and swipe API.
 */
public interface UserRepository extends JpaRepository<User, Long> {

    @Query("select u from User u where u.active = true order by u.id")
    List<User> findAllActive();

    /**
     * Discovery query: excludes the viewer, inactive profiles, and profiles
     * the viewer has already swiped on. It deliberately does not use stored
     * demo defaults until filtering is part of the matching requirement.
     */
    @Query(value = """
            SELECT u.*
            FROM users u
            WHERE u.active = TRUE
              AND u.id <> :viewerId
              AND NOT EXISTS (
                  SELECT 1 FROM current_swipes cs
                  WHERE cs.swiper_id = :viewerId
                    AND cs.target_user_id = u.id
              )
            ORDER BY u.id
            LIMIT 1
            """, nativeQuery = true)
    Optional<User> findNextDiscoverableProfile(@Param("viewerId") Long viewerId);

    /**
     * Consume the locking query while preserving the service's void API.
     * Locks must remain held by the surrounding swipe transaction.
     */
    @Transactional(propagation = Propagation.MANDATORY)
    default void lockUserPair(Long firstUserId, Long secondUserId) {
        findUserIdsForUpdate(firstUserId, secondUserId);
    }

    @Transactional(propagation = Propagation.MANDATORY)
    @Query(value = """
            SELECT id FROM users
            WHERE id IN (:firstUserId, :secondUserId)
            ORDER BY id
            FOR UPDATE
            """, nativeQuery = true)
    List<Long> findUserIdsForUpdate(
            @Param("firstUserId") Long firstUserId,
            @Param("secondUserId") Long secondUserId
    );
}
