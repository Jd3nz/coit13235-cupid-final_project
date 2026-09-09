package com.cupid.profile;

import com.cupid.matching.model.User;
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

import java.util.Optional;

/**
 * Thymeleaf interface for the Profile lifecycle.
 *
 * Supports: FR_Profile, FR_Profile_Fetch, FR_Profile_Keep_Ethics,
 * FR_Web_UI, NFR_Input_Sanitise, and NFR_Traceability.
 */
@Controller
public class ProfileController {

    private final ProfileService profileService;

    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
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
                    "Profile created successfully."
            );

            return "redirect:/profiles/" + createdProfile.getId();
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
        model.addAttribute(
                "accountDeletionEnabled",
                profileService.isAccountDeletionEnabled()
        );

        return "profile";
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
            RedirectAttributes redirectAttributes
    ) {
        try {
            profileService.deactivateProfile(profileId, confirmed);
            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Your profile has been deactivated and is no longer shown in Cupid."
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
