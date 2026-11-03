package com.jankominek.disenchantment.utils;

import com.jankominek.disenchantment.config.Config;
import com.jankominek.disenchantment.types.ParticleScopeType;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static com.jankominek.disenchantment.Disenchantment.logger;

/**
 * Resolves configured particle names and spawns the success particles of an anvil operation.
 * <p>
 * Names are resolved through {@code Particle.class.getField(name)} — never {@code valueOf} or
 * {@code values} — so a future change of {@link Particle} from an enum to an interface (as happened to
 * {@code Sound} in 1.21.3) cannot throw an {@link IncompatibleClassChangeError}. Only particles that
 * carry no extra data are accepted: dust, block, item and similar particles need a data object that a
 * plain config name cannot supply.
 */
public final class ParticleUtils {

    private ParticleUtils() {
    }

    // Keyed by the configured name list, so a config change is picked up without an explicit reset.
    // ConcurrentHashMap for Folia region-thread safety.
    private static final Map<List<String>, List<Particle>> resolved = new ConcurrentHashMap<>();

    /**
     * Resolves particle names to the {@link Particle} constants of the running server. Names that do
     * not exist (a legacy name on a new server and vice versa) and particles that need extra data are
     * skipped; duplicates collapse. The result is cached per name list, and a single warning is logged
     * the first time a list resolves to nothing.
     *
     * @param names the configured particle names, in priority order
     * @return the distinct usable particles, possibly empty
     */
    public static List<Particle> resolve(List<String> names) {
        if (names == null || names.isEmpty()) return List.of();

        List<String> key = List.copyOf(names);
        List<Particle> cached = resolved.get(key);
        if (cached != null) return cached;

        Set<Particle> particles = new LinkedHashSet<>();
        for (String name : key) {
            Particle particle = lookup(name);
            if (particle != null) particles.add(particle);
        }

        List<Particle> result = List.copyOf(new ArrayList<>(particles));
        if (resolved.putIfAbsent(key, result) == null && result.isEmpty()) {
            logger.warning("Config: none of the feedback particles " + key + " exist on this server — particles are skipped.");
        }

        DiagnosticUtils.debug("FEEDBACK", () -> "Particles " + key + " resolved to " + result);
        return resolved.get(key);
    }

    private static Particle lookup(String name) {
        if (name == null || name.isBlank()) return null;

        try {
            Object value = Particle.class.getField(name.trim().toUpperCase()).get(null);
            if (!(value instanceof Particle particle)) return null;
            if (particle.getDataType() != Void.class) {
                DiagnosticUtils.debug("FEEDBACK", () -> "Particle " + name + " needs extra data → skipped");
                return null;
            }
            return particle;
        } catch (ReflectiveOperationException | LinkageError e) {
            return null;
        }
    }

    /**
     * Forgets every cached resolution (and with it the "already warned" state). Intended for tests and reloads.
     */
    public static void clearCache() {
        resolved.clear();
    }

    /**
     * Returns the point above the anvil the particles are spawned at: the centre top of the anvil block,
     * or one block above the player for a virtual anvil that has no world location.
     *
     * @param p         the player using the anvil
     * @param inventory the anvil inventory
     * @return a fresh location, never {@code null}
     */
    public static Location anvilEffectLocation(Player p, Inventory inventory) {
        Location anvil = inventory == null ? null : inventory.getLocation();
        if (anvil != null && anvil.getWorld() != null) return anvil.clone().add(0.5, 1.0, 0.5);

        return p.getLocation().add(0, 1.0, 0);
    }

    /**
     * Spawns {@code particles} at {@code location} for the player alone or for everyone nearby.
     *
     * @param p         the player who performed the operation
     * @param location  where to spawn
     * @param particles the particles to spawn
     * @param count     how many of each
     * @param spread    random offset on every axis, in blocks
     * @param scope     who sees them
     */
    public static void spawn(Player p, Location location, List<Particle> particles, int count, double spread, ParticleScopeType scope) {
        for (Particle particle : particles) {
            if (scope == ParticleScopeType.WORLD && location.getWorld() != null) {
                location.getWorld().spawnParticle(particle, location, count, spread, spread, spread, 0.0);
            } else {
                p.spawnParticle(particle, location, count, spread, spread, spread, 0.0);
            }
        }
    }

    /**
     * Spawns the configured success particles ({@code feedback.particles.types}) at the anvil.
     * Does nothing when particles are disabled or none of the configured names exist.
     *
     * @param p         the player who completed the operation
     * @param inventory the anvil inventory
     */
    public static void playSuccess(Player p, Inventory inventory) {
        play(p, inventory, Config.Feedback.Particles.getTypes());
    }

    /**
     * Spawns the configured erase particles ({@code feedback.particles.erase-types}) at the anvil.
     *
     * @param p         the player who completed the operation
     * @param inventory the anvil inventory
     */
    public static void playErase(Player p, Inventory inventory) {
        play(p, inventory, Config.Feedback.Particles.getEraseTypes());
    }

    private static void play(Player p, Inventory inventory, List<String> names) {
        if (!Config.Feedback.Particles.isEnabled()) return;

        List<Particle> particles = resolve(names);
        if (particles.isEmpty()) return;

        spawn(p, anvilEffectLocation(p, inventory), particles,
                Config.Feedback.Particles.getCount(), Config.Feedback.Particles.getSpread(), Config.Feedback.Particles.getScope());
    }
}
