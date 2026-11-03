package com.jankominek.disenchantment.commands;

import com.jankominek.disenchantment.DisenchantmentTestBase;
import com.jankominek.disenchantment.commands.impl.Status;
import com.jankominek.disenchantment.utils.DiagnosticUtils;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.entity.PlayerMock;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The status command and the diagnostic report show the special anvils mode.
 */
class SpecialAnvilsStatusTest extends DisenchantmentTestBase {

    private List<String> statusLines(PlayerMock player, String... args) {
        Status.execute(player, args);
        List<String> lines = new ArrayList<>();
        String line;
        while ((line = player.nextMessage()) != null) lines.add(line);
        return lines;
    }

    @Test
    void givenDefaultMode_whenStatus_thenShowsAll() {
        PlayerMock admin = server.addPlayer("Admin");

        List<String> lines = statusLines(admin, "status");

        assertTrue(lines.stream().anyMatch(l -> l.contains("Special anvils") && l.contains("ALL")), lines.toString());
    }

    @Test
    void givenMarkedMode_whenStatusJson_thenContainsMode() {
        setConfig("special-anvils.mode", "MARKED");
        PlayerMock admin = server.addPlayer("Admin");

        List<String> lines = statusLines(admin, "status", "--json");

        assertTrue(lines.get(0).contains("\"special_anvils_mode\":\"MARKED\""), lines.get(0));
    }

    @Test
    void givenBlockBelowMode_whenDiagnostic_thenReportListsModeAndBlock() {
        setConfig("special-anvils.mode", "BLOCK_BELOW");
        PlayerMock admin = server.addPlayer("Admin");

        String report = DiagnosticUtils.getReport(false, admin);

        assertTrue(report.contains("Special Anvils Mode: BLOCK_BELOW (LAPIS_BLOCK)"), report);
    }
}
