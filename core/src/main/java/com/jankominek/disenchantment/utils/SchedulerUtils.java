package com.jankominek.disenchantment.utils;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Method;
import java.util.function.Consumer;

/**
 * Abstraction layer for task scheduling that supports both standard Bukkit/Spigot servers
 * and Folia (Paper's regionized multithreading). Uses reflection to detect and invoke
 * Folia-specific scheduler APIs at runtime, falling back to the Bukkit scheduler.
 */
public class SchedulerUtils {
    private static final boolean IS_FOLIA;

    // Schedulers
    private static Method getGlobalRegionScheduler;
    private static Method getAsyncScheduler;
    private static Method entityGetScheduler;

    // Execution Methods
    private static Method globalRun;
    private static Method entityRun;
    private static Method entityRunDelayed;
    private static Method asyncRunAtFixedRate;

    // Cancellation Methods
    private static Method taskCancel;
    private static Method globalCancelTasks;
    private static Method asyncCancelTasks;

    static {
        boolean folia = false;
        try {
            Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
            folia = true;

            // Classes
            Class<?> globalSchedulerClass = Class.forName("io.papermc.paper.threadedregions.scheduler.GlobalRegionScheduler");
            Class<?> asyncSchedulerClass = Class.forName("io.papermc.paper.threadedregions.scheduler.AsyncScheduler");
            Class<?> entitySchedulerClass = Class.forName("io.papermc.paper.threadedregions.scheduler.EntityScheduler");
            Class<?> scheduledTaskClass = Class.forName("io.papermc.paper.threadedregions.scheduler.ScheduledTask");

            // Getters
            getGlobalRegionScheduler = Bukkit.class.getMethod("getGlobalRegionScheduler");
            getAsyncScheduler = Bukkit.class.getMethod("getAsyncScheduler");
            entityGetScheduler = Entity.class.getMethod("getScheduler");

            // Runners
            globalRun = globalSchedulerClass.getMethod("run", Plugin.class, Consumer.class);
            entityRun = entitySchedulerClass.getMethod("run", Plugin.class, Consumer.class, Runnable.class);
            entityRunDelayed = entitySchedulerClass.getMethod("runDelayed", Plugin.class, Consumer.class, Runnable.class, long.class);
            asyncRunAtFixedRate = asyncSchedulerClass.getMethod("runAtFixedRate", Plugin.class, Consumer.class, long.class, long.class, java.util.concurrent.TimeUnit.class);

            // Cancellers
            taskCancel = scheduledTaskClass.getMethod("cancel");
            globalCancelTasks = globalSchedulerClass.getMethod("cancelTasks", Plugin.class);
            asyncCancelTasks = asyncSchedulerClass.getMethod("cancelTasks", Plugin.class);
        } catch (Exception e) {
            folia = false;
        }

        IS_FOLIA = folia;
    }

    /**
     * Cancels the given scheduled task. Supports both Folia ScheduledTask and standard BukkitTask.
     *
     * @param task the task object to cancel, or null (no-op)
     */
    public static void cancelTask(Object task) {
        if (task == null) return;

        if (IS_FOLIA) {
            try {
                // Check if it's a Folia task and cancel it via reflection
                if (task.getClass().getName().contains("ScheduledTask")) {
                    taskCancel.invoke(task);
                    return;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // Fallback: If it's a standard BukkitTask (Spigot or Folia Shim)
        if (task instanceof org.bukkit.scheduler.BukkitTask) {
            ((org.bukkit.scheduler.BukkitTask) task).cancel();
        }
    }

    /**
     * Runs a task on the entity's owning region thread (Folia) or the main server thread (Spigot).
     *
     * @param plugin the owning plugin
     * @param entity the entity whose region thread to run on
     * @param task   the task to execute
     */
    public static void runForEntity(Plugin plugin, Entity entity, Runnable task) {
        if (IS_FOLIA) {
            try {
                Object scheduler = entityGetScheduler.invoke(entity);
                entityRun.invoke(scheduler, plugin, (Consumer<Object>) (t) -> task.run(), null);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            Bukkit.getScheduler().runTask(plugin, task);
        }
    }

    /**
     * Runs a delayed task on the entity's owning region thread (Folia) or the main server thread (Spigot).
     *
     * @param plugin     the owning plugin
     * @param entity     the entity whose region thread to run on
     * @param task       the task to execute
     * @param delayTicks the delay in ticks before execution
     */
    public static void runForEntityLater(Plugin plugin, Entity entity, Runnable task, long delayTicks) {
        if (IS_FOLIA) {
            try {
                Object scheduler = entityGetScheduler.invoke(entity);
                entityRunDelayed.invoke(scheduler, plugin, (Consumer<Object>) (t) -> task.run(), null, delayTicks);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            Bukkit.getScheduler().runTaskLater(plugin, task, delayTicks);
        }
    }

    /**
     * Runs a task on the global region scheduler (Folia) or the main server thread (Spigot).
     *
     * @param plugin the owning plugin
     * @param task   the task to execute
     */
    public static void runGlobal(Plugin plugin, Runnable task) {
        if (IS_FOLIA) {
            try {
                Object scheduler = getGlobalRegionScheduler.invoke(Bukkit.getServer());
                globalRun.invoke(scheduler, plugin, (Consumer<Object>) (t) -> task.run());
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            Bukkit.getScheduler().runTask(plugin, task);
        }
    }

    /**
     * Runs a repeating asynchronous task. On Folia, uses the async scheduler with millisecond
     * conversion (1 tick = 50ms). On Spigot, uses the standard async timer.
     *
     * @param plugin      the owning plugin
     * @param task        the task to execute repeatedly
     * @param delayTicks  the initial delay in ticks
     * @param periodTicks the period in ticks between executions
     * @return the scheduled task object, or null if scheduling failed on Folia
     */
    public static Object runAsyncTimer(Plugin plugin, Runnable task, long delayTicks, long periodTicks) {
        if (IS_FOLIA) {
            try {
                Object scheduler = getAsyncScheduler.invoke(Bukkit.getServer());
                // Folia uses Milliseconds, Spigot uses Ticks. We convert (1 tick = 50ms).
                return asyncRunAtFixedRate.invoke(scheduler, plugin, (Consumer<Object>) (t) -> task.run(),
                        delayTicks * 50L, periodTicks * 50L, java.util.concurrent.TimeUnit.MILLISECONDS);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        } else {
            return Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, task, delayTicks, periodTicks);
        }
    }

    /**
     * Cancels all tasks scheduled by the given plugin on both global and async schedulers.
     *
     * @param plugin the owning plugin whose tasks to cancel
     */
    public static void cancelAllTasks(Plugin plugin) {
        if (IS_FOLIA) {
            try {
                Object globalScheduler = getGlobalRegionScheduler.invoke(Bukkit.getServer());
                Object asyncScheduler = getAsyncScheduler.invoke(Bukkit.getServer());

                globalCancelTasks.invoke(globalScheduler, plugin);
                asyncCancelTasks.invoke(asyncScheduler, plugin);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            Bukkit.getScheduler().cancelTasks(plugin);
        }
    }
}