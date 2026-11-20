package com.jankominek.disenchantment.e2e;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * Scenarios from card T-M6-05 that cannot be run honestly today. Each is a reported JUnit skip with its reason, never a
 * pass, and none of them boots a server. When the reason goes away, replace the method with a real {@code @CellTest}.
 */
class SkippedScenariosE2E {

    private static final String NOT_ON_BASE = "the feature is not in the base of this branch (origin/master 6cd4f51); "
            + "it is on fix/audit-findings, which is not merged. Add the scenario after that merge.";

    @Test
    @Disabled("grindstone extract and transfer: " + NOT_ON_BASE)
    void grindstoneExtractAndTransfer() {
    }

    @Test
    @Disabled("erase mode: " + NOT_ON_BASE)
    void eraseMode() {
    }

    @Test
    @Disabled("special anvils: " + NOT_ON_BASE)
    void specialAnvils() {
    }

    @Test
    @Disabled("Bedrock-prefix chest-menu fallback: " + NOT_ON_BASE + " Also, Proofstone 0.1.0 has no server view of an "
            + "open chest window (PlayerState.openWindow is post-v0) and a simulated-Bedrock cell runs only paper@26.3, "
            + "not on a bridged cell.")
    void bedrockPrefixChestMenuFallback() {
    }

    @Test
    @Disabled("per-cell NMS-module expectation: the module is named only in the startup log line 'NMS module: "
            + "NMS_v1_21_R5' (Disenchantment.java:153); no command, config key or event exposes it, and Proofstone "
            + "0.1.0 has no log read (Agent.logLines is post-v0, spec test-api section 8.4), so a CellExpectation "
            + "cannot observe it. Every other test here runs the cell's NMS module end to end instead.")
    void nmsModuleMatchesTheCellVersion() {
    }
}
