package com.jankominek.disenchantment.e2e;

import io.github.h7kz.proofstone.api.Agent;
import io.github.h7kz.proofstone.api.PluginHandle;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A scoped Disenchantment config change. Proofstone's restore after a test writes the file back but the plugin keeps the
 * object it read at enable, so a patch would leak into later tests on the same server. This scope remembers the value at
 * every patched path, applies the patch, runs {@code /disenchantment reload} (which also clears the plugin's cached
 * enchantment states, costs and chances) and, on close, patches the originals back and reloads again. Use it in a
 * try-with-resources so the revert runs on failure too.
 */
final class Cfg implements AutoCloseable {

    private final Agent agent;
    private final PluginHandle plugin;
    private final Map<String, Object> originals = new LinkedHashMap<>();

    private Cfg(Agent agent, Map<String, Object> patches) {
        this.agent = agent;
        this.plugin = agent.plugin("Disenchantment");
        try {
            patches.forEach((path, value) -> {
                originals.put(path, plugin.config(path));
                plugin.patchConfig(path, value);
            });
            agent.command("disenchantment reload");
        } catch (RuntimeException e) {
            close();
            throw e;
        }
    }

    static Cfg with(Agent agent, Map<String, Object> patches) {
        return new Cfg(agent, patches);
    }

    /** Patches that need an initial structure the default config lacks are not supported: every path must exist. */
    @Override
    public void close() {
        RuntimeException failure = null;
        for (Map.Entry<String, Object> original : originals.entrySet()) {
            try {
                plugin.patchConfig(original.getKey(), original.getValue());
            } catch (RuntimeException e) {
                failure = e;
            }
        }
        try {
            agent.command("disenchantment reload");
        } catch (RuntimeException e) {
            failure = e;
        }
        originals.clear();
        if (failure != null) {
            throw failure;
        }
    }
}
