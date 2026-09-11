package com.cupid.matching.controller;

import com.cupid.matching.dto.SwipeRequest;
import com.cupid.matching.dto.SwipeResult;
import com.cupid.matching.exception.InvalidSwipeException;
import com.cupid.matching.model.SwipeDecision;
import com.cupid.matching.service.SwipeService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class SwipeController {

    private final SwipeService swipeService;

    public SwipeController(SwipeService swipeService) {
        this.swipeService = swipeService;
    }

    /**
     * Receives Like and Dislike form submissions.
     *
     * Supports: FR_Swipe FR_Match FR_Web_UI
     */
    @PostMapping("/swipes")
    public String recordSwipe(
            @Valid @ModelAttribute SwipeRequest request,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "The swipe request contained invalid information."
            );

            return redirectAfterSwipe(request);
        }

        try {
            SwipeResult result
                    = swipeService.recordSwipe(request);

            if (result.matched()) {
                redirectAttributes.addFlashAttribute(
                        "matchMessage",
                        "It's a match! You both liked each other."
                );
            } else {
                String message
                        = request.getDecision() == SwipeDecision.LIKE
                        ? "Like recorded successfully."
                        : "Dislike recorded successfully.";

                redirectAttributes.addFlashAttribute(
                        "successMessage",
                        message
                );
            }
        } catch (InvalidSwipeException exception) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    exception.getMessage()
            );
        }

        return redirectAfterSwipe(request);
    }

    private String redirectAfterSwipe(SwipeRequest request) {
        if (request.getViewerId() == null
                || request.getViewerId() <= 0) {
            return "redirect:/";
        }

        return "redirect:/discover?userId="
                + request.getViewerId();
    }
}
