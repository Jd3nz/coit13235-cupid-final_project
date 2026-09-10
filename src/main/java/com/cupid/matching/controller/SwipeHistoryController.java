package com.cupid.matching.controller;

import com.cupid.matching.model.User;
import com.cupid.matching.service.DiscoveryService;
import com.cupid.matching.service.SwipeHistoryService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;

@Controller
public class SwipeHistoryController {

    private final DiscoveryService discoveryService;
    private final SwipeHistoryService swipeHistoryService;

    public SwipeHistoryController(
            DiscoveryService discoveryService,
            SwipeHistoryService swipeHistoryService
    ) {
        this.discoveryService = discoveryService;
        this.swipeHistoryService = swipeHistoryService;
    }

    /**
     * Displays the complete swipe history for the selected user.
     *
     * Supports: FR_Swipe_History FR_Web_UI
     */
    @GetMapping("/history")
    public String history(
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
                "history",
                swipeHistoryService.findHistory(userId)
        );

        return "history";
    }
}
