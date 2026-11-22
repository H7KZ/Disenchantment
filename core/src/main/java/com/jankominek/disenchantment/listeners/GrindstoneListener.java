package com.jankominek.disenchantment.listeners;

import com.jankominek.disenchantment.config.Config;
import com.jankominek.disenchantment.events.GrindstoneClickEvent;
import com.jankominek.disenchantment.events.GrindstoneEvent;
import com.jankominek.disenchantment.types.LogLevelType;
import com.jankominek.disenchantment.utils.DiagnosticUtils;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventException;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.GrindstoneInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.EventExecutor;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

import static com.jankominek.disenchantment.Disenchantment.logger;
import static com.jankominek.disenchantment.Disenchantment.plugin;
import static org.bukkit.Bukkit.getServer;

/**
 * Registers the grindstone listeners: the click listener (book placement + result click) and the
 * prepare listener.
 * <p>
 * {@code PrepareGrindstoneEvent} is not part of the Spigot 1.18 API the core is compiled against
 * (Spigot added it in 1.19.4; Paper has had its own {@code com.destroystokyo.paper.event.inventory}
 * version since 1.16), so it is looked up by class name at runtime and registered through the
 * {@link EventExecutor} pattern. When neither class exists the prepare listener is not registered and
 * the result is computed by a one-tick task after every grindstone click instead
 * ({@link GrindstoneEvent#scheduleRefresh}).
 */
public class GrindstoneListener implements EventExecutor {
    /**
     * Class names tried in order. Registering only the first one found avoids handling one slot change twice
     * on Paper builds that ship both.
     */
    static final String[] PREPARE_EVENT_CLASSES = {
            "org.bukkit.event.inventory.PrepareGrindstoneEvent",
            "com.destroystokyo.paper.event.inventory.PrepareGrindstoneEvent"
    };

    private static final Listener clickListener = new Listener() {
    };
    private static final Listener prepareListener = new Listener() {
    };
    private static final Listener dragListener = new Listener() {
    };

    private final boolean prepareEventAvailable;

    /**
     * Registers the click listener and, when the server has one, the prepare listener.
     *
     * @param prepareEventPriority priority of the prepare handler
     * @param clickEventPriority   priority of the click handler
     */
    public GrindstoneListener(EventPriority prepareEventPriority, EventPriority clickEventPriority) {
        this(prepareEventPriority, clickEventPriority, GrindstoneListener::loadClass);
    }

    /**
     * Same as {@link #GrindstoneListener(EventPriority, EventPriority)} with an injectable class lookup (tests).
     */
    GrindstoneListener(EventPriority prepareEventPriority, EventPriority clickEventPriority, Function<String, Class<?>> classLookup) {
        getServer().getPluginManager().registerEvent(
                InventoryClickEvent.class,
                clickListener,
                clickEventPriority,
                this,
                plugin,
                false
        );

        Class<? extends Event> prepareClass = findPrepareEventClass(classLookup);
        this.prepareEventAvailable = prepareClass != null;

        if (prepareClass != null) {
            getServer().getPluginManager().registerEvent(
                    prepareClass,
                    prepareListener,
                    prepareEventPriority,
                    this,
                    plugin,
                    false
            );
            DiagnosticUtils.debug("STARTUP", () -> "Grindstone: using " + prepareClass.getName());
        } else {
            // Dragging a stack across the input slots changes them without a click event, so the fallback
            // refresh needs the drag too (a cancelled drag changes nothing).
            getServer().getPluginManager().registerEvent(
                    InventoryDragEvent.class,
                    dragListener,
                    clickEventPriority,
                    this,
                    plugin,
                    true
            );

            if (Config.Logging.getLevel().isAtLeast(LogLevelType.INFO)) {
                logger.info("Grindstone: this server has no PrepareGrindstoneEvent (added in Spigot 1.19.4); "
                        + "grindstone results are computed one tick after each click or drag instead.");
            }
        }
    }

    /**
     * Returns whether a native prepare event is registered (otherwise the tick-based fallback is used).
     */
    public boolean isPrepareEventAvailable() {
        return prepareEventAvailable;
    }

    @SuppressWarnings("unchecked")
    static Class<? extends Event> findPrepareEventClass(Function<String, Class<?>> classLookup) {
        for (String name : PREPARE_EVENT_CLASSES) {
            Class<?> candidate = classLookup.apply(name);
            if (candidate != null && Event.class.isAssignableFrom(candidate)) return (Class<? extends Event>) candidate;
        }
        return null;
    }

    private static Class<?> loadClass(String name) {
        try {
            return Class.forName(name);
        } catch (ClassNotFoundException | LinkageError e) {
            return null;
        }
    }

    /**
     * {@inheritDoc}
     * Routes click events to {@link GrindstoneClickEvent}, drag events to the fallback refresh and everything else to {@link GrindstoneEvent}.
     */
    @Override
    public void execute(@NotNull Listener l, @NotNull Event e) throws EventException {
        if (e instanceof InventoryClickEvent click) {
            GrindstoneClickEvent.onEvent(e);
            if (!prepareEventAvailable) scheduleFallbackRefresh(click.getInventory(), click.getWhoClicked());
            return;
        }

        if (e instanceof InventoryDragEvent drag) {
            if (!prepareEventAvailable) scheduleFallbackRefresh(drag.getInventory(), drag.getWhoClicked());
            return;
        }

        GrindstoneEvent.onEvent(e);
    }

    /**
     * Without a prepare event, recompute the result one tick after any click or drag on a grindstone (the tick
     * lets vanilla finish its own slot change first).
     */
    private static void scheduleFallbackRefresh(Inventory top, HumanEntity who) {
        if (top.getType() != InventoryType.GRINDSTONE) return;
        if (!(top instanceof GrindstoneInventory inventory)) return;
        if (!(who instanceof Player p)) return;
        if (!Config.isPluginEnabled()) return;
        if (!Config.Grindstone.isEnabled() && !Config.Grindstone.Transfer.isEnabled()) return;

        GrindstoneEvent.scheduleRefresh(p, inventory);
    }
}
