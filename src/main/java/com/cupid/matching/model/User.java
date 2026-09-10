package com.cupid.matching.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;

/**
 * Represents the minimum user information required by
 * the Cupid Matching component.
 */
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "display_name", nullable = false, length = 100)
    private String displayName;
    @Column(nullable = false)
    private int age;
    @Column(length = 500)
    private String bio;
    @Column(nullable = false)
    private boolean active;
    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime createdAt;

    protected User() {
        // Required by JPA; application callers retain the full constructor.
    }

    public User(
            Long id,
            String displayName,
            int age,
            String bio,
            boolean active,
            OffsetDateTime createdAt
    ) {
        this.id = id;
        this.displayName = displayName;
        this.age = age;
        this.bio = bio;
        this.active = active;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getAge() {
        return age;
    }

    public String getBio() {
        return bio;
    }

    public boolean isActive() {
        return active;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * FR_Profile: Changes the information that a person has chosen to show
     * on their Cupid profile. Validation and input sanitisation happen in
     * ProfileService before this domain operation is called.
     */
    public void updateProfile(
            String displayName,
            int age,
            String bio
    ) {
        this.displayName = displayName;
        this.age = age;
        this.bio = bio;
    }

    /**
     * FR_Profile_Keep_Ethics: performs a soft account deletion. Keeping the
     * record protects swipe and match history while removing the account from
     * all active-profile and discovery queries.
     */
    public void deactivate() {
        this.active = false;
    }
}
