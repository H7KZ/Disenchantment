package com.jankominek.disenchantment.utils;

import org.bukkit.entity.Player;

/**
 * Cooldown delegate that routes to CMI's per-player cooldown API via reflection,
 * so the module compiles without the CMI jar on the classpath.
 * <p>
 * CMI does not ship a stable public Maven artifact usable offline in this project;
 * the reflective calls below target the commonly documented
 * {@code com.Zrips.CMI.CMI} / {@code CMIUser} cooldown methods. If CMI's actual
 * API shape differs, every call fails closed (returns "not on cooldown" /
 * no-op) and {@link CooldownManager} falls back to the internal delegate.
 */
public class CmiCooldownDelegate implements CooldownDelegate {
    private static final String COOLDOWN_KEY = "disenchantment";

    private Boolean apiResolved = null;

    /**
     * Attempts to resolve the CMI API classes/methods this delegate relies on.
     * Cached after the first attempt.
     *
     * @return {@code true} if the reflective API surface was resolved successfully
     */
    public boolean isAvailable() {
        if (apiResolved == null) {
            try {
                Class.forName("com.Zrips.CMI.CMI");
                Class.forName("com.Zrips.CMI.Modules.Cooldowns.CooldownManager");
                apiResolved = true;
            } catch (Throwable t) {
                DiagnosticUtils.debug("COOLDOWN_CMI", "API not resolvable: " + t);
                apiResolved = false;
            }
        }
        return apiResolved;
    }

    @Override
    public boolean isOnCooldown(Player player) {
        return getRemainingSeconds(player) > 0;
    }

    @Override
    public long getRemainingSeconds(Player player) {
        try {
            Object cmi = Class.forName("com.Zrips.CMI.CMI").getMethod("getInstance").invoke(null);
            Object cooldownManager = cmi.getClass().getMethod("getCooldownManager").invoke(cmi);
            Object remaining = cooldownManager.getClass()
                    .getMethod("getRemainingCooldown", Player.class, String.class)
                    .invoke(cooldownManager, player, COOLDOWN_KEY);
            if (remaining instanceof Number number) return Math.max(0, number.longValue());
            return 0;
        } catch (Throwable t) {
            DiagnosticUtils.debug("COOLDOWN_CMI", "getRemainingSeconds failed, treating as no cooldown: " + t);
            return 0;
        }
    }

    @Override
    public void recordOperation(Player player) {
        try {
            Object cmi = Class.forName("com.Zrips.CMI.CMI").getMethod("getInstance").invoke(null);
            Object cooldownManager = cmi.getClass().getMethod("getCooldownManager").invoke(cmi);
            cooldownManager.getClass()
                    .getMethod("setCooldown", Player.class, String.class, long.class)
                    .invoke(cooldownManager, player, COOLDOWN_KEY, (long) com.jankominek.disenchantment.config.Config.Cooldown.getSeconds());
        } catch (Throwable t) {
            DiagnosticUtils.debug("COOLDOWN_CMI", "recordOperation failed, no-op: " + t);
        }
    }
}
