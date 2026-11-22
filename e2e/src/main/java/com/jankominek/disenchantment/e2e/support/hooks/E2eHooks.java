package com.jankominek.disenchantment.e2e.support.hooks;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.AbstractList;
import java.util.List;
import java.util.logging.Filter;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * A third-party listener on the plugin's cancellable Pre events, the way an API consumer would write one. The classes are
 * resolved through Disenchantment's class loader at runtime, so this plugin has no compile dependency on it. A Pre event
 * is cancelled while {@code cancel.<name>} is true in this plugin's live config (a test sets it with patchConfig).
 *
 * <p>It can also make the plugin's next write of an operation result fail ({@code failWrite.<name>} true, see
 * {@link #armWriteFailure}). Disenchantment fires the Pre event before it charges and before it writes the result, so a
 * failure armed from a Pre listener lands after the charge, which is the path the refund must cover. The failure is
 * injected the way a third party could: through reflection on the server's anvil container, found by type, with no
 * mapped names. Counters of what happened are kept in this plugin's live config for the test to read:
 * {@code observed.armed} (failures armed), {@code observed.thrown} (writes that really threw) and
 * {@code observed.swallowedReports} and {@code observed.swallowedSaveNotices} (the plugin's own crash-report log record
 * for that exception and the line naming the saved report, which this plugin drops from the plugin's logger so the
 * harness log guard does not read the expected failure as an unexpected error).
 */
public final class E2eHooks extends JavaPlugin implements Listener {

    private static final String API = "com.jankominek.disenchantment.events.api.";
    private static final String[] PRE_EVENTS = {"PreDisenchantEvent", "PreShatterEvent", "PreEraseEvent",
            "PreGrindstoneExtractEvent", "PreGrindstoneTransferEvent"};

    /** Marks the injected exception; the log filter drops only records that carry it. */
    static final String MARKER = "DisenchantmentE2eHooks injected write failure";

    @Override
    @SuppressWarnings("unchecked")
    public void onEnable() {
        saveDefaultConfig();
        Plugin target = getServer().getPluginManager().getPlugin("Disenchantment");
        if (target == null) {
            getLogger().warning("Disenchantment is not installed; no hooks registered");
            return;
        }
        // The plugin logs its crash report (carrying the exception message) and then, on the same thread, one line naming the
        // saved file. Only that pair is dropped, and each is counted, so a test can assert both were seen.
        ThreadLocal<Boolean> afterReport = ThreadLocal.withInitial(() -> false);
        Filter swallow = record -> {
            String message = record.getMessage();
            if (message != null && message.contains(MARKER)) {
                getConfig().set("observed.swallowedReports", getConfig().getInt("observed.swallowedReports", 0) + 1);
                afterReport.set(true);
                return false;
            }
            if (afterReport.get()) {
                afterReport.set(false);
                if (message != null && message.startsWith("Crash report saved to: ")) {
                    getConfig().set("observed.swallowedSaveNotices", getConfig().getInt("observed.swallowedSaveNotices", 0) + 1);
                    return false;
                }
            }
            return true;
        };
        target.getLogger().setFilter(swallow);
        for (String name : PRE_EVENTS) {
            try {
                Class<? extends Event> type = (Class<? extends Event>) Class.forName(API + name, true, target.getClass().getClassLoader());
                getServer().getPluginManager().registerEvent(type, this, EventPriority.NORMAL, (listener, event) -> {
                    if (getConfig().getBoolean("cancel." + name, false) && event instanceof Cancellable cancellable) {
                        cancellable.setCancelled(true);
                        return;
                    }
                    if (getConfig().getBoolean("failWrite." + name, false)) {
                        try {
                            armWriteFailure(event);
                        } catch (ReflectiveOperationException | RuntimeException e) {
                            getLogger().severe("could not arm the write failure: " + e);
                        }
                    }
                }, this, false);
            } catch (ClassNotFoundException e) {
                getLogger().warning("no " + name + " in this Disenchantment build");
            }
        }
    }

    /**
     * Swaps the {@code java.util.List} inside the NonNullList of the anvil's input container for one whose next
     * {@code set(...)} throws (once, then it puts the original list back). The plugin writes slot 0 first, so the write
     * fails before anything is changed. Runs on the thread that fired the event (the player's region thread on Folia),
     * which is also the thread that does the write.
     */
    private void armWriteFailure(Event event) throws ReflectiveOperationException {
        Player player = (Player) event.getClass().getMethod("getPlayer").invoke(event);
        Object inventory = player.getOpenInventory().getTopInventory();
        Method getHandle = inventory.getClass().getMethod("getInventory");
        Object container = getHandle.invoke(inventory);

        Object nonNullList = null;
        for (Class<?> c = container.getClass(); c != null && nonNullList == null; c = c.getSuperclass()) {
            for (Field f : c.getDeclaredFields()) {
                if (!Modifier.isStatic(f.getModifiers()) && f.getType().getSimpleName().equals("NonNullList")) {
                    f.setAccessible(true);
                    nonNullList = f.get(container);
                    break;
                }
            }
        }
        if (nonNullList == null) throw new IllegalStateException("no NonNullList in " + container.getClass());

        Field listField = null;
        for (Class<?> c = nonNullList.getClass(); c != null && listField == null; c = c.getSuperclass()) {
            for (Field f : c.getDeclaredFields()) {
                if (!Modifier.isStatic(f.getModifiers()) && f.getType() == List.class) {
                    listField = f;
                    break;
                }
            }
        }
        if (listField == null) throw new IllegalStateException("no java.util.List in " + nonNullList.getClass());
        listField.setAccessible(true);
        @SuppressWarnings("unchecked")
        List<Object> original = (List<Object>) listField.get(nonNullList);
        if (original instanceof FailingList) return; // already armed
        listField.set(nonNullList, new FailingList(original, nonNullList, listField));
        getConfig().set("observed.armed", getConfig().getInt("observed.armed", 0) + 1);
    }

    /** Delegates to the original list; the first set(...) restores the original and throws. */
    private final class FailingList extends AbstractList<Object> {
        private final List<Object> original;
        private final Object owner;
        private final Field field;

        FailingList(List<Object> original, Object owner, Field field) {
            this.original = original;
            this.owner = owner;
            this.field = field;
        }

        @Override
        public Object get(int index) {
            return original.get(index);
        }

        @Override
        public int size() {
            return original.size();
        }

        @Override
        public Object set(int index, Object element) {
            try {
                field.set(owner, original);
            } catch (IllegalAccessException e) {
                throw new IllegalStateException(e);
            }
            getConfig().set("observed.thrown", getConfig().getInt("observed.thrown", 0) + 1);
            throw new IllegalStateException(MARKER);
        }

        @Override
        public void add(int index, Object element) {
            original.add(index, element);
        }

        @Override
        public Object remove(int index) {
            return original.remove(index);
        }

        @Override
        public void clear() {
            original.clear();
        }
    }
}
