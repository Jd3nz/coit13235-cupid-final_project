package com.cupid.matching.controller;

import com.cupid.matching.repository.UserRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Entry point for the assessment's profile-demo chooser.
 *
 * Architecture: the app is not a login system, so the home page deliberately
 * supplies active profiles for the user to choose before entering a component.
 */
@Controller
public class HomeController {

    private final UserRepository userRepository;

    public HomeController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute(
                "users",
                userRepository.findAllActive()
        );

        return "index";
    }
}
