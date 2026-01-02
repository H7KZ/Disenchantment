package com.jankominek.disenchantment.utils;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Method;
import java.util.function.Consumer;

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