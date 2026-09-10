package com.cupid.matching.controller;

import com.cupid.matching.model.User;
import com.cupid.matching.service.DiscoveryService;
import com.cupid.matching.service.MatchService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;

@Controller
public class MatchController {

    private final DiscoveryService discoveryService;
    private final MatchService matchService;

    public MatchController(
            DiscoveryService discoveryService,
            MatchService matchService
    ) {
        this.discoveryService = discoveryService;
        this.matchService = matchService;
    }

    /**
     * Displays all active matches for the selected user.
     *
     * Supports: FR_Match FR_Web_UI
     */
    @GetMapping("/matches")
    public String matches(
            @RequestParam Long userId,
            Model model
    ) {
        Optional<User> viewer
                = discoveryService.findViewer(userId);

        if (viewer.isEmpty()) {
            return "redirect:/";
        }

        model.addAttribute(
                "viewer",
                viewer.get()
        );

        model.addAttribute(
                "matches",
                matchService.findActiveMatches(userId)
        );

        return "matches";
    }
}
