package com.cupid.profile;

import com.cupid.matching.model.User;
import com.cupid.profile.picture.ProfilePictureService;
import com.cupid.profile.picture.model.ProfilePicture;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

/**
 * Thymeleaf interface for the Profile lifecycle.
 *
 * Supports: FR_Profile, FR_Profile_Fetch, FR_Profile_Keep_Ethics,
 * FR_Profile_Picture, FR_Web_UI, NFR_Input_Sanitise, and NFR_Traceability.
 */
@Controller
public class ProfileController {

    private final ProfileService profileService;
    private final ProfilePictureService pictureService;

    public ProfileController(
            ProfileService profileService,
            ProfilePictureService pictureService
    ) {
        this.profileService = profileService;
        this.pictureService = pictureService;
    }

    @GetMapping("/profiles/new")
    public String newProfile(Model model) {
        prepareEditModel(model, new ProfileForm(), null);
        return "profile-edit";
    }

    @PostMapping("/profiles")
    public String createProfile(
            @Valid @ModelAttribute("profileForm") ProfileForm profileForm,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            prepareEditModel(model, profileForm, null);
            return "profile-edit";
        }

        try {
            User createdProfile = profileService.createProfile(profileForm);
            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Profile created successfully. Add a photo to complete it."
            );

            return "redirect:/profiles/" + createdProfile.getId() + "#photo-upload";
        } catch (ProfileException exception) {
            model.addAttribute("errorMessage", exception.getMessage());
            prepareEditModel(model, profileForm, null);
            return "profile-edit";
        }
    }

    @GetMapping("/profiles/{profileId}")
    public String showProfile(
            @PathVariable Long profileId,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        Optional<User> profile = profileService.findActiveProfile(profileId);

        if (profile.isEmpty()) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "The requested profile is unavailable."
            );

            return "redirect:/";
        }

        model.addAttribute("profile", profile.get());
        boolean globalAccountDeletionEnabled =
                profileService.isAccountDeletionEnabled();
        model.addAttribute(
                "globalAccountDeletionEnabled",
                globalAccountDeletionEnabled
        );
        model.addAttribute(
                "accountDeletionEnabled",
                globalAccountDeletionEnabled
                        && profile.get().isAccountDeactivationEnabled()
        );

        List<ProfilePicture> pictures = pictureService.listPictures(profileId);
        model.addAttribute("pictures", pictures);
        pictures.stream()
                .filter(ProfilePicture::isPrimaryPicture)
                .findFirst()
                .ifPresent(primary ->
                        model.addAttribute("primaryPictureId", primary.getId())
                );

        return "profile";
    }

    /**
     * FR_Profile_Keep_Ethics: a user-facing preference screen makes account
     * account deletion difficult but not impossible.
     */
    @GetMapping("/profiles/{profileId}/settings")
    public String accountSettings(
            @PathVariable Long profileId,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        Optional<User> profile = profileService.findActiveProfile(profileId);

        if (profile.isEmpty()) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "The requested profile is unavailable."
            );
            return "redirect:/";
        }

        model.addAttribute("profile", profile.get());
        model.addAttribute(
                "globalAccountDeletionEnabled",
                profileService.isAccountDeletionEnabled()
        );
        return "account-settings";
    }

    @PostMapping("/profiles/{profileId}/settings")
    public String updateAccountSettings(
            @PathVariable Long profileId,
            @RequestParam(defaultValue = "false") boolean accountDeactivationEnabled,
            RedirectAttributes redirectAttributes
    ) {
        try {
            profileService.updateAccountDeactivationPreference(
                    profileId,
                    accountDeactivationEnabled
            );
            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    accountDeactivationEnabled
                            ? "Account deletion is now available after final confirmation."
                            : "Account deletion has been turned off."
            );
            return "redirect:/profiles/" + profileId + "/settings";
        } catch (ProfileException exception) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    exception.getMessage()
            );
            return "redirect:/profiles/" + profileId;
        }
    }

    @GetMapping("/profiles/{profileId}/edit")
    public String editProfile(
            @PathVariable Long profileId,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        Optional<User> profile = profileService.findActiveProfile(profileId);

        if (profile.isEmpty()) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "The requested profile is unavailable."
            );

            return "redirect:/";
        }

        prepareEditModel(model, ProfileForm.from(profile.get()), profileId);
        return "profile-edit";
    }

    @PostMapping("/profiles/{profileId}")
    public String updateProfile(
            @PathVariable Long profileId,
            @Valid @ModelAttribute("profileForm") ProfileForm profileForm,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            prepareEditModel(model, profileForm, profileId);
            return "profile-edit";
        }

        try {
            profileService.updateProfile(profileId, profileForm);
            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Profile updated successfully."
            );

            return "redirect:/profiles/" + profileId;
        } catch (ProfileException exception) {
            model.addAttribute("errorMessage", exception.getMessage());
            prepareEditModel(model, profileForm, profileId);
            return "profile-edit";
        }
    }

    @PostMapping("/profiles/{profileId}/deactivate")
    public String deactivateProfile(
            @PathVariable Long profileId,
            @RequestParam(defaultValue = "false") boolean confirmed,
            @RequestParam(required = false) String deletionPhrase,
            RedirectAttributes redirectAttributes
    ) {
        try {
            profileService.deactivateProfile(
                    profileId,
                    confirmed,
                    deletionPhrase
            );
            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Your account has been deleted and is no longer shown in Cupid."
            );

            return "redirect:/";
        } catch (ProfileException exception) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    exception.getMessage()
            );

            return "redirect:/profiles/" + profileId;
        }
    }

    private void prepareEditModel(
            Model model,
            ProfileForm profileForm,
            Long profileId
    ) {
        model.addAttribute("profileForm", profileForm);
        model.addAttribute("isNew", profileId == null);
        model.addAttribute("profileId", profileId);
        model.addAttribute(
                "formAction",
                profileId == null ? "/profiles" : "/profiles/" + profileId
        );
    }
}
