package com.jankominek.disenchantment.e2e;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.h7kz.proofstone.api.Agent;
import io.github.h7kz.proofstone.api.Cell;
import io.github.h7kz.proofstone.api.CellTest;
import io.github.h7kz.proofstone.api.Matrix;
import io.github.h7kz.proofstone.api.Platform;
import io.github.h7kz.proofstone.api.Proofstone;
import java.util.List;
import java.util.Map;

/**
 * The per-cell NMS-module expectation, read from the plugin's start-up line {@code NMS module: <class>} with
 * {@code Agent.bootLogLines()} (Proofstone T-M11-03; the agent loads at server startup, before the plugin enables).
 * The expected module of each Minecraft version is {@code MinecraftVersion}'s mapping, written out here as literals.
 */
@Proofstone(plugins = "target/plugin-under-test/Disenchantment-*.jar")
@Matrix(platform = Platform.PAPER, versions = {"1.18.2", "1.20.4", "1.20.6", "1.21.4", "1.21.7", "1.21.11", "26.3"})
@Matrix(platform = Platform.FOLIA, versions = {"26.3"})
class NmsModuleE2E {

    private static final Map<String, String> MODULE = Map.of(
            "1.18.2", "NMS_v1_18_R1",
            "1.20.4", "NMS_v1_18_R1",
            "1.20.6", "NMS_v1_20_R4",
            "1.21.4", "NMS_v1_21_R1",
            "1.21.7", "NMS_v1_21_R4",
            "1.21.11", "NMS_v1_21_R5",
            "26.3", "NMS_v1_21_R5");

    @CellTest
    void theStartupLineNamesTheCellsNmsModule(Cell cell, Agent agent) {
        String expected = MODULE.get(cell.version());
        assertThat(expected).as("a module literal for " + cell.version()).isNotNull();
        List<String> lines = agent.bootLogLines().stream().filter(line -> line.contains("NMS module: ")).toList();
        assertThat(lines).as("the plugin logs its NMS module once at enable").hasSize(1);
        assertThat(lines.get(0)).endsWith("NMS module: " + expected);
    }
}
