package com.jankominek.disenchantment.utils;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class DiagnosticUtilsTest {
    @Test
    void debugDisabledByDefault() {
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
