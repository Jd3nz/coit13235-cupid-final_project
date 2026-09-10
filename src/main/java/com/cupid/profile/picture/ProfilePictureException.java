package com.cupid.profile.picture;

/**
 * Domain exception raised when a profile picture cannot be accepted
 * or served. Message text is safe to expose to Thymeleaf.
 *
 * Requirement: FR_Profile_Picture.
 */
public class ProfilePictureException extends RuntimeException {

    public ProfilePictureException(String message) {
        super(message);
    }

    public ProfilePictureException(String message, Throwable cause) {
        super(message, cause);
    }
}
