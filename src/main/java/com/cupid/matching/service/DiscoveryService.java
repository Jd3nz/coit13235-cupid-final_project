package com.cupid.matching.service;

import com.cupid.matching.model.User;
import com.cupid.matching.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Contains business logic for discovering matching profiles.
 *
 * Supports FR_Swipe.
 */
@Service
public class DiscoveryService {

    private final UserRepository userRepository;

    public DiscoveryService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Resolves the selected demo viewer without treating the URL parameter as
     * trusted. Active-profile filtering keeps deactivated users out of every
     * Matching screen.
     */
    public Optional<User> findViewer(Long viewerId) {
        if (viewerId == null || viewerId <= 0) {
            return Optional.empty();
        }

        return userRepository
                .findById(viewerId)
                .filter(User::isActive);
    }

    /**
     * Asks the repository for one unswiped active profile. Discovery defaults
     * are stored for future work but are intentionally not filtering this
     * assessment demo yet.
     */
    public Optional<User> findNextProfile(Long viewerId) {
        if (viewerId == null || viewerId <= 0) {
            return Optional.empty();
        }

        return userRepository.findNextDiscoverableProfile(viewerId);
    }
}
