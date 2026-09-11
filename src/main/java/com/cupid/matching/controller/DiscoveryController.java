package com.cupid.matching.controller;

import com.cupid.matching.model.User;
import com.cupid.matching.service.DiscoveryService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;

@Controller
public class DiscoveryController {

    private final DiscoveryService discoveryService;

    public DiscoveryController(DiscoveryService discoveryService) {
        this.discoveryService = discoveryService;
    }

    /**
     * Displays the next available profile.
     *
     * Supports FR_Swipe and FR_Web_UI.
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

        model.addAttribute("viewer", viewer.get());

        model.addAttribute(
                "profile",
                discoveryService.findNextProfile(userId).orElse(null)
        );

        return "discover";
    }
}
