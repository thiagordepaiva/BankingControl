package com.bankingcontrol.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RedirectControllerTest {

    @InjectMocks
    private RedirectController redirectController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void redirectToLogin_DeveRedirecionarParaTelaLogin() {
        String viewName = redirectController.redirectToLogin();

        assertEquals("redirect:/login", viewName);
    }
}
