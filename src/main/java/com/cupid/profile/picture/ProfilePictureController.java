package com.cupid.profile.picture;

import com.cupid.profile.picture.model.ProfilePicture;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Thymeleaf and REST endpoints for profile pictures.
 *
 * Requirements: FR_Profile_Picture, FR_Web_UI, NFR_Traceability.
 */
@Controller
public class ProfilePictureController {

    private final ProfilePictureService pictureService;

    public ProfilePictureController(ProfilePictureService pictureService) {
        this.pictureService = pictureService;
    }

    /**
     * FR_Profile_Picture: accepts an upload and returns to the photo manager
     * rather than the View Profile page, so the user can immediately select a
     * primary picture or remove an image.
     */
    @PostMapping("/profiles/{profileId}/pictures")
    public String uploadPicture(
            @PathVariable Long profileId,
            @RequestParam("file") MultipartFile file,
            RedirectAttributes redirectAttributes
    ) {
        try {
            pictureService.uploadPicture(profileId, file);
            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Picture uploaded successfully."
            );
        } catch (ProfilePictureException exception) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    exception.getMessage()
            );
        }

        return "redirect:/profiles/" + profileId + "/edit#profile-photos";
    }

    /**
     * FR_Profile_Picture: serves raw picture bytes with the stored content
     * type so the browser can render them from an img tag.
     */
    @GetMapping("/profiles/{profileId}/pictures/{pictureId}")
    public ResponseEntity<byte[]> getPicture(
            @PathVariable Long profileId,
            @PathVariable Long pictureId
    ) {
        ProfilePicture picture = pictureService.getPicture(profileId, pictureId);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(picture.getContentType()));
        headers.setContentLength(picture.getFileSize());
        headers.setCacheControl("private, max-age=3600");

        return new ResponseEntity<>(picture.getData(), headers, 200);
    }

    @PostMapping("/profiles/{profileId}/pictures/{pictureId}/primary")
    public String setPrimary(
            @PathVariable Long profileId,
            @PathVariable Long pictureId,
            RedirectAttributes redirectAttributes
    ) {
        try {
            pictureService.setPrimary(profileId, pictureId);
            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Primary picture updated."
            );
        } catch (ProfilePictureException exception) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    exception.getMessage()
            );
        }

        return "redirect:/profiles/" + profileId + "/edit#profile-photos";
    }

    @PostMapping("/profiles/{profileId}/pictures/{pictureId}/delete")
    public String deletePicture(
            @PathVariable Long profileId,
            @PathVariable Long pictureId,
            RedirectAttributes redirectAttributes
    ) {
        try {
            pictureService.deletePicture(profileId, pictureId);
            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Picture removed."
            );
        } catch (ProfilePictureException exception) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    exception.getMessage()
            );
        }

        return "redirect:/profiles/" + profileId + "/edit#profile-photos";
    }
}
