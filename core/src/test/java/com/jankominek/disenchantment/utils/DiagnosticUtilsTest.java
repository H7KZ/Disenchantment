package com.jankominek.disenchantment.utils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DiagnosticUtilsTest {
    @BeforeEach
    void resetDebugState() {
        DiagnosticUtils.setDebugEnabled(false);
    }

    @Test
    void debugCanBeDisabled() {
        DiagnosticUtils.setDebugEnabled(false);
        assertFalse(DiagnosticUtils.isDebugEnabled());
    }

    @Test
    void debugEnabledAfterSet() {
        DiagnosticUtils.setDebugEnabled(true);
        assertTrue(DiagnosticUtils.isDebugEnabled());
        DiagnosticUtils.setDebugEnabled(false); // cleanup
    }
}
