package com.cupid.matching.controller;

import com.cupid.matching.config.MatchingProperties;
import com.cupid.matching.repository.UserRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    private final MatchingProperties matchingProperties;
    private final UserRepository userRepository;

    public HomeController(
            MatchingProperties matchingProperties,
            UserRepository userRepository
    ) {
        this.matchingProperties = matchingProperties;
        this.userRepository = userRepository;
    }

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute(
                "swipeEncouragementEnabled",
                matchingProperties.isSwipeEncouragementEnabled()
        );

        model.addAttribute(
                "users",
                userRepository.findAllActive()
        );

        return "index";
    }
}