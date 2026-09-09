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

    public Optional<User> findViewer(Long viewerId) {
        if (viewerId == null || viewerId <= 0) {
            return Optional.empty();
        }

        return userRepository
                .findById(viewerId)
                .filter(User::isActive);
    }

    public Optional<User> findNextProfile(Long viewerId) {
        if (viewerId == null || viewerId <= 0) {
            return Optional.empty();
        }

        return userRepository.findNextDiscoverableProfile(viewerId);
    }
}