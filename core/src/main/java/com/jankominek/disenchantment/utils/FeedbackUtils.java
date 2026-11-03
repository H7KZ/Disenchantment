package com.jankominek.disenchantment.utils;

import com.jankominek.disenchantment.config.Config;
import com.jankominek.disenchantment.config.I18n;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.LongSupplier;

/**
 * Player feedback for refused anvil clicks: a per-player debounced chat message and the configurable
 * failure sound.
 * <p>
 * A refused click can be repeated many times per second (the player keeps clicking the result slot), so
 * messages go through a one-second per-player debounce shared by every refusal reason. The failure sound
 * is intentionally not debounced — it is the immediate "no" the player expects from a click.
 */
public final class FeedbackUtils {

    private FeedbackUtils() {
    }

    private static final long DEBOUNCE_MILLIS = 1000L;

    // ConcurrentHashMap for Folia region-thread safety.
    private static final Map<UUID, Long> lastRefusalMessage = new ConcurrentHashMap<>();

    // Overridable time source so the debounce window can be tested without sleeping.
    private static volatile LongSupplier clock = System::currentTimeMillis;

    /**
     * Replaces the time source used by the debounce. Intended for tests.
     *
     * @param newClock supplies the current time in milliseconds, or {@code null} to restore the system clock
     */
    public static void setClock(LongSupplier newClock) {
        clock = newClock == null ? System::currentTimeMillis : newClock;
    }

    /**
     * Returns {@code true} when a refusal message may be sent to {@code p} now, and starts a new
     * one-second window. Returns {@code false} while the previous refusal message is less than a second old.
     *
     * @param p the player
     * @return whether the message should be sent
     */
    public static boolean tryAcquireMessageSlot(Player p) {
        long now = clock.getAsLong();
        boolean[] acquired = {false};

        lastRefusalMessage.compute(p.getUniqueId(), (id, last) -> {
            if (last != null && now - last < DEBOUNCE_MILLIS && now >= last) return last;
            acquired[0] = true;
            return now;
        });

        return acquired[0];
    }

    /**
     * Sends {@code message} (with the plugin prefix) unless the player already received a refusal
     * message within the last second.
     *
     * @param p       the player
     * @param message the already-localized message
     * @return {@code true} if the message was sent
     */
    public static boolean sendRefusalMessage(Player p, String message) {
        if (!tryAcquireMessageSlot(p)) return false;

        p.sendMessage(I18n.getPrefix() + " " + message);
        return true;
    }

    /**
     * Refuses a click: sends the debounced message and plays the failure sound.
     *
     * @param p       the player whose click was refused
     * @param message the already-localized reason
     */
    public static void refuse(Player p, String message) {
        sendRefusalMessage(p, message);
        playFailureSound(p);
    }

    /**
     * Refuses a click because the player lacks experience levels. The message is debounced and gated by
     * {@code feedback.not-enough-xp-message}; the failure sound plays regardless.
     *
     * @param p      the player
     * @param levels the levels the operation costs
     */
    public static void refuseNotEnoughXp(Player p, int levels) {
        if (Config.Feedback.isNotEnoughXpMessageEnabled()) {
            sendRefusalMessage(p, I18n.Messages.notEnoughLevels(String.valueOf(levels)));
        }
        playFailureSound(p);
    }

    /**
     * Plays the configured failure sound to {@code p}. The sound is a vanilla key string played through
     * {@link Player#playSound(org.bukkit.Location, String, float, float)}, so no {@code Sound} constant is touched.
     *
     * @param p the player
     */
    public static void playFailureSound(Player p) {
        if (!Config.Feedback.FailureSound.isEnabled()) return;

        p.playSound(p.getLocation(), Config.Feedback.FailureSound.getSound(),
                Config.Feedback.FailureSound.getVolume(), Config.Feedback.FailureSound.getPitch());
    }

    /**
     * Forgets the player's debounce state. Called when the player quits.
     *
     * @param p the player
     */
    public static void forget(Player p) {
        lastRefusalMessage.remove(p.getUniqueId());
    }

    /**
     * Forgets every player's debounce state.
     */
    public static void reset() {
        lastRefusalMessage.clear();
    }
}
