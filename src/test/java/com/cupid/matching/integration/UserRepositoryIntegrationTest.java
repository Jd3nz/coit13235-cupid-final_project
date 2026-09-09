package com.cupid.matching.integration;

import com.cupid.matching.model.User;
import com.cupid.matching.repository.UserRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class UserRepositoryIntegrationTest {

    @Autowired
    private UserRepository users;

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private EntityManager entityManager;

    @Test
    void shouldPersistAndReadUserUsingExistingSchemaDefaults() {
        User saved = users.saveAndFlush(new User(null, "JPA test", 30, null, true, null));
        assertNotNull(saved.getId());
        entityManager.clear();

        User loaded = users.findById(saved.getId()).orElseThrow();
        assertEquals("JPA test", loaded.getDisplayName());
        assertEquals(30, loaded.getAge());
        assertNull(loaded.getBio());
        assertTrue(loaded.isActive());
        assertNotNull(loaded.getCreatedAt());
        assertTrue(users.findById(-1L).isEmpty());
    }

    @Test
    void shouldReturnActiveUsersInIdOrder() {
        User inactive = users.saveAndFlush(new User(null, "Inactive test", 31, "Hidden", false, null));
        var active = users.findAllActive();
        assertFalse(active.isEmpty());
        assertTrue(active.stream().allMatch(User::isActive));
        assertFalse(active.stream().anyMatch(user -> user.getId().equals(inactive.getId())));
        assertEquals(active.stream().map(User::getId).sorted(Comparator.naturalOrder()).toList(),
                active.stream().map(User::getId).toList());
        assertFalse(users.findById(inactive.getId()).orElseThrow().isActive());
    }

    @Test
    void shouldExcludeSelfInactiveAndSwipedProfilesFromDiscovery() {
        var active = users.findAllActive();
        Long viewerId = active.get(0).getId();
        Long inactiveId = active.get(1).getId();
        jdbc.update("DELETE FROM current_swipes WHERE swiper_id = ?", viewerId);
        jdbc.update("UPDATE users SET active = FALSE WHERE id = ?", inactiveId);
        entityManager.clear();

        var expected = active.stream().map(User::getId)
                .filter(id -> !id.equals(viewerId) && !id.equals(inactiveId)).toList();
        for (Long targetId : expected) {
            assertEquals(targetId, users.findNextDiscoverableProfile(viewerId).orElseThrow().getId());
            jdbc.update("""
                    INSERT INTO current_swipes (swiper_id, target_user_id, decision)
                    VALUES (?, ?, 'DISLIKE')
                    """, viewerId, targetId);
        }
        assertTrue(users.findNextDiscoverableProfile(viewerId).isEmpty());
    }
}
