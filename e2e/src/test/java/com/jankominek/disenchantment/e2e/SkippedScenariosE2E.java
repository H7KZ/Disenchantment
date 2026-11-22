package com.jankominek.disenchantment.e2e;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * Scenarios that cannot be run honestly with Proofstone 0.1.0. Each is a reported JUnit skip with its reason, never a
 * pass, and none of them boots a server. The precise API each one needs is listed under "Proofstone gaps" in
 * docs/engineering/E2E.md. When the reason goes away, replace the method with a real {@code @CellTest}.
 */
class SkippedScenariosE2E {

    @Test
    @Disabled("special anvil MARKED mode with a placed special anvil, the chunk-PDC mark of SpecialAnvilStore and the drop swap "
            + "on breaking: the mark is written only by a BlockPlaceEvent from a player placing the special item. Proofstone "
            + "0.1.0 has no client block placement or breaking (needs Client.placeBlock / Client.breakBlock). Faking the event "
            + "from a test plugin would test the fake, not the plugin.")
    void specialAnvilPlacementMarkAndDropSwap() {
    }

    @Test
    @Disabled("grindstone tick-fallback refresh on drags (commit 5460349): the fallback and its InventoryDragEvent listener are "
            + "registered only when the server has no PrepareGrindstoneEvent; every Paper and Folia cell has one (Paper ships "
            + "its own since 1.16), so the code path is not reachable on any cell. A drag also needs Window.drag, which "
            + "0.1.0 lacks. Covered by GrindstoneListenerTest in core.")
    void grindstoneTickFallbackRefreshOnDrag() {
    }

    @Test
    @Disabled("refund when writing the result fails (commit 4352679): needs an enchantment adapter that throws while the result "
            + "is written, on demand. No installable plugin does, and the e2e hooks plugin cannot reach inside the write. "
            + "Covered by GrindstoneClickEventTest and AnvilOperations tests in core.")
    void refundWhenTheWriteFails() {
    }

    @Test
    @Disabled("Bedrock-prefix chest-menu fallback: Proofstone 0.1.0 has no server view of an open chest window "
            + "(PlayerState.openWindow is post-v0), so the menu contents cannot be asserted on the server; a "
            + "simulated-Bedrock cell runs only paper@26.3, not on a bridged cell. Needs PlayerHandle.openWindow().")
    void bedrockPrefixChestMenuFallback() {
    }

    @Test
    @Disabled("per-cell NMS-module expectation: the module is named only in the startup log line 'NMS module: "
            + "NMS_v1_21_R5'; no command, config key or event exposes it, and Proofstone 0.1.0 has no log read "
            + "(Agent.logLines is post-v0). Every other test here runs the cell's NMS module end to end instead. "
            + "Needs Agent.logLines().")
    void nmsModuleMatchesTheCellVersion() {
    }
}
