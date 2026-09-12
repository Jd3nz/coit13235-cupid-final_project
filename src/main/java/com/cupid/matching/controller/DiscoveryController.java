package com.cupid.matching.controller;

import com.cupid.matching.config.MatchingProperties;
import com.cupid.matching.model.User;
import com.cupid.matching.service.DiscoveryService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;

/**
 * Web adapter for the discovery screen.
 *
 * Architecture: this controller only prepares Thymeleaf model data; the
 * selection rules stay in {@link DiscoveryService}. Supports FR_Swipe and
 * FR_Web_UI.
 */
@Controller
public class DiscoveryController {

    private final DiscoveryService discoveryService;
    private final MatchingProperties matchingProperties;

    public DiscoveryController(
            DiscoveryService discoveryService,
            MatchingProperties matchingProperties
    ) {
        this.discoveryService = discoveryService;
        this.matchingProperties = matchingProperties;
    }

    /**
     * Displays the next available profile.
     *
     * The viewer is checked first so a forged or inactive profile ID returns to
     * the demo chooser rather than exposing another profile. Supports FR_Swipe
     * and FR_Web_UI.
     */
    @GetMapping("/discover")
    public String discover(
            @RequestParam Long userId,
            Model model
    ) {
        Optional<User> viewer
                = discoveryService.findViewer(userId);

        if (viewer.isEmpty()) {
            return "redirect:/";
        }

        User activeViewer = viewer.get();
        model.addAttribute("viewer", activeViewer);

        // FR_Swipe_More_Ethics: the prompt exists only when both the one
        // application-level setting and this profile's saved preference permit it.
        model.addAttribute(
                "swipeReminderEnabled",
                matchingProperties.isSwipeEncouragementEnabled()
                        && activeViewer.isSwipeEncouragementEnabled()
        );

        model.addAttribute(
                "profile",
                discoveryService.findNextProfile(userId).orElse(null)
        );

        return "discover";
    }
}
