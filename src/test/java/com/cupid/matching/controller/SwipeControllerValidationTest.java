package com.cupid.matching.controller;

import com.cupid.matching.service.SwipeService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests HTTP input validation and sanitisation.
 *
 * Supports:
 * NFR_Input_Sanitise
 * FR_Swipe
 * NFR_Traceability
 */
@ExtendWith(MockitoExtension.class)
class SwipeControllerValidationTest {

    @Mock
    private SwipeService swipeService;

    private MockMvc mockMvc;
    private LocalValidatorFactoryBean validator;

    @BeforeEach
    void setUp() {
        validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders
                .standaloneSetup(
                        new SwipeController(swipeService)
                )
                .setValidator(validator)
                .build();
    }

    @AfterEach
    void tearDown() {
        validator.close();
    }

    /**
     * NFR_Input_Sanitise:
     * Only LIKE and DISLIKE are valid enum values.
     */
    @Test
    void shouldRejectUnsupportedDecision() throws Exception {
        mockMvc.perform(
                        post("/swipes")
                                .param("viewerId", "1")
                                .param("targetUserId", "2")
                                .param(
                                        "decision",
                                        "LIKE; DROP TABLE matches"
                                )
                )
                .andExpect(status().is3xxRedirection())
                .andExpect(
                        redirectedUrl(
                                "/discover?userId=1"
                        )
                )
                .andExpect(
                        flash().attribute(
                                "errorMessage",
                                "The swipe request contained "
                                        + "invalid information."
                        )
                );

        verifyNoInteractions(swipeService);
    }

    /**
     * NFR_Input_Sanitise:
     * Script content cannot be converted into a numeric user ID.
     */
    @Test
    void shouldRejectScriptAsTargetUserId() throws Exception {
        mockMvc.perform(
                        post("/swipes")
                                .param("viewerId", "1")
                                .param(
                                        "targetUserId",
                                        "<script>alert('x')</script>"
                                )
                                .param("decision", "LIKE")
                )
                .andExpect(status().is3xxRedirection())
                .andExpect(
                        redirectedUrl(
                                "/discover?userId=1"
                        )
                )
                .andExpect(
                        flash().attribute(
                                "errorMessage",
                                "The swipe request contained "
                                        + "invalid information."
                        )
                );

        verifyNoInteractions(swipeService);
    }

    /**
     * NFR_Input_Sanitise:
     * IDs must be positive.
     */
    @Test
    void shouldRejectNegativeViewerId() throws Exception {
        mockMvc.perform(
                        post("/swipes")
                                .param("viewerId", "-1")
                                .param("targetUserId", "2")
                                .param("decision", "LIKE")
                )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andExpect(
                        flash().attribute(
                                "errorMessage",
                                "The swipe request contained "
                                        + "invalid information."
                        )
                );

        verifyNoInteractions(swipeService);
    }

    /**
     * NFR_Input_Sanitise:
     * A swipe decision is mandatory.
     */
    @Test
    void shouldRejectMissingDecision() throws Exception {
        mockMvc.perform(
                        post("/swipes")
                                .param("viewerId", "1")
                                .param("targetUserId", "2")
                )
                .andExpect(status().is3xxRedirection())
                .andExpect(
                        redirectedUrl(
                                "/discover?userId=1"
                        )
                )
                .andExpect(
                        flash().attribute(
                                "errorMessage",
                                "The swipe request contained "
                                        + "invalid information."
                        )
                );

        verifyNoInteractions(swipeService);
    }
}