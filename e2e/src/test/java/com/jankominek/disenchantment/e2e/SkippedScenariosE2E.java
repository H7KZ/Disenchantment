package com.jankominek.disenchantment.e2e;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * Scenarios that cannot be run honestly with the pinned Proofstone. Each is a reported JUnit skip with its reason, never a
 * pass, and none of them boots a server. The precise API each one needs is listed under "Proofstone gaps" in
 * docs/engineering/E2E.md. When the reason goes away, replace the method with a real {@code @CellTest}.
 */
class SkippedScenariosE2E {

    @Test
    @Disabled("grindstone tick-fallback refresh on drags (commit 5460349): the fallback and its InventoryDragEvent listener are "
            + "registered only when the server has no PrepareGrindstoneEvent; every Paper and Folia cell has one (Paper ships "
            + "its own since 1.16), so on those cells a drag into the grindstone is answered by the normal prepare-event path "
            + "and a test would pass with or without the fallback code (a mutation of it cannot go red). Window.drag now "
            + "exists in the pinned Proofstone; the remaining blocker is a cell with a server that has no PrepareGrindstoneEvent "
            + "(Spigot below 1.19.4). Covered by GrindstoneListenerTest in core.")
    void grindstoneTickFallbackRefreshOnDrag() {
    }
}
