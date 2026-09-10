package com.cupid.messaging;

import com.cupid.matching.repository.UserRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Displays conversations and processes the send-message form.
 *
 * Uses the same userId selection approach as the Matching UI.
 * This is a demonstration user selector, not authentication.
 */
@Controller
@RequestMapping("/messages")
public class MessageController {

    private final MessageService messageService;
    private final UserRepository userRepository;

    public MessageController(
            MessageService messageService,
            UserRepository userRepository
    ) {
        this.messageService = messageService;
        this.userRepository = userRepository;
    }

    @GetMapping
    public String showMessages(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Long recipientId,
            Model model
    ) {
        model.addAttribute("users", userRepository.findAllActive());
        model.addAttribute("userId", userId);
        model.addAttribute("recipientId", recipientId);

        if (userId != null && recipientId != null) {
            try {
                model.addAttribute(
                        "messages",
                        messageService.getConversation(userId, recipientId)
                );
            } catch (IllegalArgumentException exception) {
                model.addAttribute("error", exception.getMessage());
            }
        }

        return "messaging";
    }

    @PostMapping
    public String sendMessage(
            @RequestParam Long userId,
            @RequestParam Long recipientId,
            @RequestParam String messageText,
            RedirectAttributes redirect
    ) {
        try {
            messageService.sendMessage(userId, recipientId, messageText);
            redirect.addFlashAttribute("success", "Message sent.");
        } catch (IllegalArgumentException exception) {
            // Preserve the draft so the user can correct invalid input.
            redirect.addFlashAttribute("error", exception.getMessage());
            redirect.addFlashAttribute("draft", messageText);
        }

        redirect.addAttribute("userId", userId);
        redirect.addAttribute("recipientId", recipientId);

        // Refreshing the resulting GET page will not resend the message.
        return "redirect:/messages";
    }
}