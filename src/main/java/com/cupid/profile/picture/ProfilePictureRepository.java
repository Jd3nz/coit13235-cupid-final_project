package com.cupid.profile.picture;

import com.cupid.profile.picture.model.ProfilePicture;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Persistence contract for profile pictures.
 *
 * Requirement: FR_Profile_Picture.
 */
public interface ProfilePictureRepository extends JpaRepository<ProfilePicture, Long> {

    List<ProfilePicture> findByUserIdOrderByUploadedAtDesc(Long userId);

    Optional<ProfilePicture> findByUserIdAndPrimaryPictureTrue(Long userId);

    long countByUserId(Long userId);
}
